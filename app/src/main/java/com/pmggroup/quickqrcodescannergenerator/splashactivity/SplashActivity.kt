package com.pmggroup.quickqrcodescannergenerator.splashactivity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.pmggroup.quickqrcodescannergenerator.R
import com.pmggroup.quickqrcodescannergenerator.Utilities
import com.pmggroup.quickqrcodescannergenerator.databinding.ActivitySplashBinding
import com.pmggroup.quickqrcodescannergenerator.homeactivity.view.HomeActivity


class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initControls()
    }

    private fun initControls() {
        initViews()
    }

    private fun initViews() {
        Utilities.statusBarColor(R.color.colorGrey_5F6C75, true, this@SplashActivity)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
            finish()
        }, 1000)
    }

}