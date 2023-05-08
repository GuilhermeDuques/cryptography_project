package br.edu.infnet.at_anotacoes

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.doOnLayout
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import br.edu.infnet.at_anotacoes.databinding.FragmentAnotacaoBinding
import br.edu.infnet.at_anotacoes.databinding.FragmentListBinding
import java.io.*
import java.security.KeyStore
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec


class AnotacaoFragment : Fragment() {
    var dataAgora: String  =""
    var fotoTirada  =false

    private var _binding: FragmentAnotacaoBinding? = null
    private lateinit var locationManager: LocationManager

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAnotacaoBinding.inflate(inflater, container, false)
        val view = binding.root

        setup()
        return view
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setup() {
        setupView()
        setupClickListeners()
    }

    private fun setupView() {

        // Listing 17.12 Disabling the camera button página 547
        val captureImageIntent = takePhoto.contract.createIntent(
            requireContext(),
            Uri.EMPTY
        )
        binding.fabFoto.isEnabled = true // canResolveIntent(captureImageIntent)

        val formatter = SimpleDateFormat("dd/MM/yyyy - HH:mm")
        val dateNow = Date()
        val mDate = formatter.format(dateNow)

        binding.tvData.text  = mDate

    }

    private fun setupClickListeners() {
        /*******************************************************************************************
         *  Passo 04: Pegar a foto
         *******************************************************************************************
         *  Um timestamp é usado no nome para não sobrescrever uma foto já criada
         *******************************************************************************************
         *******************************************************************************************
         */
        binding.fabFoto.setOnClickListener {
//            val  // removido o val na página 547 - Listing 17.11 Handling the result (CrimeDetailFragment.kt)

            val formatter = SimpleDateFormat("dd_MM_yyyy-HH_mm")
            val dateNow = Date()
            dataAgora = formatter.format(dateNow)

            binding.tvData.text  = dataAgora
            Log.i(TAG, "dataAgora =${dataAgora}")

            photoName = "IMG_${dataAgora}.JPG"

            val fullDirName = "${context?.filesDir}/$photoName"

            Log.i(TAG, "fullDirName =${fullDirName}")

            val photoFile = File(fullDirName)
            val photoUri = FileProvider.getUriForFile(
                requireContext(),                                              // contexto
                "br.edu.infnet.at_anotacoes.fileprovider",// authority do manifesto
                photoFile
            )
            Log.i(TAG, "photoUri =${photoUri}")

            takePhoto.launch(photoUri)

        }


        binding.fabSave.setOnClickListener {

            if (validate())
            {
                val nomePadrao = "${binding.inputTitulo.text.toString()}$dataAgora"
                val nomeArquivoTexto = "$nomePadrao.txt"
                val nomeArquivoFoto = "$nomePadrao.fig"

                val fullDirNameText = "${context?.filesDir}/$nomeArquivoTexto"
                val fullDirNameFoto = "${context?.filesDir}/$nomeArquivoFoto"

                val photoFile = File(fullDirNameFoto)
                val textFile = File(fullDirNameText)



            }
        }
    }


    // Escreve arquivo de forma criptografada
    fun gravarCriptografado(nomeArquivoCompleto: String, conteudo: String) {

        // Gera chave mestra para criptografia
        val masterKeyAlias: String = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        // cria arquivo
        val file = File(nomeArquivoCompleto)
        // Configura arquivo criptografado
        val encryptedFile: EncryptedFile = EncryptedFile.Builder(
            file,
            requireContext(),
            masterKeyAlias,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

        // write to the encrypted file
        val encryptedOutputStream: FileOutputStream = encryptedFile.openFileOutput()
        val writer = BufferedWriter(OutputStreamWriter(encryptedOutputStream))

        //  writer.use fecha o output automaticamente após escrever
        writer.use {
            it.write(conteudo)
        }

    }

    // Lê arquivo de forma simples
    fun lerCriptografado(nomeArquivoCompleto: String): String {

        // Gera chave mestra para criptografia
        val masterKeyAlias: String = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        // cria arquivo
        val file = File(nomeArquivoCompleto)
        // Configura arquivo criptografado
        val encryptedFile: EncryptedFile = EncryptedFile.Builder(
            file,
            requireContext(),
            masterKeyAlias,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

        val content = StringBuilder()
        try {

            // Na leitura simples é
            // val input = openFileInput(nomeArquivo)
            val input = encryptedFile.openFileInput()
            val reader = BufferedReader(InputStreamReader(input))
            reader.use {
                //  reader.use fecha o reader automaticamente após ler
                reader.forEachLine {
                    // lê todas as linhas e adiciona ao fim de content
                    content.append("$it\n")
                    Log.i(TAG, it )
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        Log.i(TAG, content.toString() )
        return content.toString()
    }

    fun validate(): Boolean {
        var resposta = true

        if (binding.inputTitulo.text.toString().isNullOrBlank()){
            Log.i(TAG, "Titulo não preenchido")
            return false
        }

        if (binding.inputDescricao.text.toString().isNullOrBlank()){
            Log.i(TAG, "Descrição não preenchida")
            return false
        }

        if (!fotoTirada) {
            Log.i(TAG, "Foto não tirada")
            return false
        }


        return resposta

    }

    private var photoName: String? = null
    val TAG= "RP"

    private val takePhoto = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { didTakePhoto: Boolean,  ->

        if (didTakePhoto && photoName != null) {

            Log.i(TAG, "photoName = ${photoName}" )

            updatePhoto(photoName)

            // Use a uri para gerenciar onde a foto será usada

            // Exemplo do livro com sqlite:
//            crimeDetailViewModel.updateCrime { oldCrime ->
//                oldCrime.copy(photoFileName = photoName)
//            }


        }
    }

    /*******************************************************************************************
     *  Passo 05: Verifique se o dispositivo pode pegar foto antes de pedir
     *******************************************************************************************
     *  Listing 16.16 Resolving Intents (CrimeDetailFragment.kt) - página 528
     *
     *  Adicione ao manifesto a query:
     *
     *      <queries>
     *          <intent>
     *              <action android:name="android.media.action.IMAGE_CAPTURE" />
     *          </intent>
     *      </queries>
     *
     *******************************************************************************************
     *******************************************************************************************
     */
    private fun canResolveIntent(intent: Intent): Boolean {
        val packageManager: PackageManager = requireContext().packageManager
        val resolvedActivity: ResolveInfo? =
            packageManager.resolveActivity(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
        return resolvedActivity != null
    }


    /*******************************************************************************************
     *  Passo 06: Ajusta a imagem ao tamanho da tela
     *******************************************************************************************
     *  Listing 17.15  Updating
     *  (https://learning.oreilly.com/library/view/android-programming-the/9780137645794/ch17s04.html)
     *
     * doOnLayout consegue dimensionar a view no layout
     *
     * getScaledBitmap() pega as dimensões da tela e redimensiona o bitmap se necessário.
     *
     * Bitmap não tem compressão como o jpeg logo fica bem maior o consumo de memória
     *
     *******************************************************************************************
     *******************************************************************************************
     */

    private fun updatePhoto(photoFileName: String?) {

        val fullDirName = "${context?.filesDir}/$photoName"
        val photoFile = File(fullDirName)

        if (photoFile?.exists() == true) {
            binding.ivFofoca.doOnLayout { measuredView ->
                val scaledBitmap = getScaledBitmap(
                    photoFile.path,
                    measuredView.width,
                    measuredView.height
                )
                binding.ivFofoca.setImageBitmap(scaledBitmap)
                binding.ivFofoca.tag = photoFileName
            }
        } else {
            binding.ivFofoca.setImageBitmap(null)
            binding.ivFofoca.tag = null
        }

        fotoTirada = true


//        if (binding.ivFofoca.tag != photoFileName) {
//            val photoFile = photoFileName?.let {
//                File(requireContext().filesDir, it)
//            }
//
//            if (photoFile?.exists() == true) {
//                binding.ivFofoca.doOnLayout { measuredView ->
//                    val scaledBitmap = getScaledBitmap(
//                        photoFile.path,
//                        measuredView.width,
//                        measuredView.height
//                    )
//                    binding.ivFofoca.setImageBitmap(scaledBitmap)
//                    binding.ivFofoca.tag = photoFileName
//                }
//            } else {
//                binding.ivFofoca.setImageBitmap(null)
//                binding.ivFofoca.tag = null
//            }
//        }
    }

    // Método para cifrar texto:
    fun cipher(original: String): ByteArray {

        val c = Criptografador()

        var chave = c.getSecretKey()
        return cipher(original,chave)
    }

    fun cipher(original: String, chave: SecretKey?): ByteArray {
        if (chave != null) {
            Cipher.getInstance("AES/CBC/PKCS7Padding").run {
                init(Cipher.ENCRYPT_MODE,chave)
                var valorCripto = doFinal(original.toByteArray())
                var ivCripto = ByteArray(16)
                iv.copyInto(ivCripto,0,0,16)
                return ivCripto + valorCripto
            }
        } else return byteArrayOf()
    }

    fun decipher(cripto: ByteArray): String{
        val c = Criptografador()

        var chave = c.getSecretKey()
        return decipher(cripto,chave)
    }

    fun decipher(cripto: ByteArray, chave: SecretKey?): String{
        if (chave != null) {
            Cipher.getInstance("AES/CBC/PKCS7Padding").run {
                var ivCripto = ByteArray(16)
                var valorCripto = ByteArray(cripto.size-16)
                cripto.copyInto(ivCripto,0,0,16)
                cripto.copyInto(valorCripto,0,16,cripto.size)
                val ivParams = IvParameterSpec(ivCripto)
                init(Cipher.DECRYPT_MODE,chave,ivParams)
                return String(doFinal(valorCripto))
            }
        } else return ""
    }

}



class Criptografador {

    val ks: KeyStore =
        KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    fun getSecretKey(): SecretKey? {
        var chave: SecretKey? = null
        if(ks.containsAlias("chaveCripto")) {
            val entrada = ks.getEntry("chaveCripto", null) as?
                    KeyStore.SecretKeyEntry
            chave = entrada?.secretKey
        } else {
            val builder = KeyGenParameterSpec.Builder("chaveCripto",
                KeyProperties.PURPOSE_ENCRYPT or
                        KeyProperties.PURPOSE_DECRYPT)
            val keySpec = builder.setKeySize(256)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(
                    KeyProperties.ENCRYPTION_PADDING_PKCS7).build()
            val kg = KeyGenerator.getInstance("AES", "AndroidKeyStore")
            kg.init(keySpec)
            chave = kg.generateKey()
        }
        return chave
    }
}