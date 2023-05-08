package br.edu.infnet.at_anotacoes


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import br.edu.infnet.at_anotacoes.databinding.ActivityMainBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : AppCompatActivity(){
    lateinit var auth: FirebaseAuth
    private var mUser: FirebaseUser? = null
    lateinit var mAdView : AdView




    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root



        binding.btnLogout.setOnClickListener{
            auth .signOut()
            mUser = null
            finish()


        }
        auth = FirebaseAuth.getInstance()
        val emailzinho = auth.currentUser?.email
        binding.textView.text = emailzinho

        setContentView(view)
        MobileAds.initialize(this) {}
        val adView = AdView(this)


        adView.adUnitId = "ca-app-pub-3940256099942544/6300978111"
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }
}
