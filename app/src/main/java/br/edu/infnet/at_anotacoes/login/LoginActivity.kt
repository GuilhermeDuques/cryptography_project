package br.edu.infnet.at_anotacoes.login

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import br.edu.infnet.at_anotacoes.MainActivity
import br.edu.infnet.at_anotacoes.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity(), LocationListener {
    lateinit var binding: ActivityLoginBinding
    lateinit var firebaseAuth: FirebaseAuth
    private lateinit var locationManager: LocationManager

    private var isLocationPermissionGranted = false

    private lateinit var singlePermissionLauncher: ActivityResultLauncher<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupPermissionsLaunchers()
        firebaseAuth = FirebaseAuth.getInstance()
        binding.btnCadastro.setOnClickListener {
            val intent = Intent(this, CadastroActivity::class.java)
            startActivity(intent)
        }
        binding.btnLocation.setOnClickListener {
            getLocation()
        }
        binding.btnLogar.setOnClickListener {
            val email = binding.edEmailLogin.text.toString()
            val senha = binding.edSenhaLogin.text.toString()

            if(email.isNotEmpty() && senha.isNotEmpty()){

                firebaseAuth.signInWithEmailAndPassword(email, senha).addOnCompleteListener{
                    if(it.isSuccessful){
                        val intent= Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    }else{
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                }


            }else{
                Toast.makeText(this, "Complete todas as colunas", Toast.LENGTH_SHORT).show()}
        }
    }





    fun setupPermissionsLaunchers() {
        singlePermissionLauncher = registerForActivityResult(
            ActivityResultContracts
                .RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                isLocationPermissionGranted = true
                //toast("Permissão consedida")
            } else {
                //toast("Permissão negada")
                isLocationPermissionGranted = false
            }
        }
    }

    private fun getLocation() {
        if (!isLocationPermissionGranted) {
            singlePermissionLauncher.launch(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {

            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

            if (
                (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED)
            ) {
            }

            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000,
                5f,
                this
            )
        }
    }

    override fun onLocationChanged(location: Location) {

        val lat = "%.4f".format(location.latitude)
        val lon = "%.4f".format(location.longitude)
        binding.tvLocation.text = "Lat: $lat \n Lon: $lon"


//        toast("Minha localização mudou para:\n latitude: ${location.latitude}\n longitude: ${location.longitude} ")
    }



}
