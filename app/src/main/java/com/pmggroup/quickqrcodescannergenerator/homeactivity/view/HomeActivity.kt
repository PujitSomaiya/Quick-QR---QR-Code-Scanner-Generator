package com.pmggroup.quickqrcodescannergenerator.homeactivity.view

import android.app.Dialog
import android.content.*
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.blikoon.qrcodescanner.QrCodeActivity
import com.google.ads.AdRequest.LOGTAG
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener
import com.pmggroup.quickqrcodescannergenerator.R
import com.pmggroup.quickqrcodescannergenerator.Utilities
import com.pmggroup.quickqrcodescannergenerator.databinding.ActivityHomeBinding
import com.pmggroup.quickqrcodescannergenerator.homeactivity.adapter.ViewPagerAdapter


class HomeActivity : AppCompatActivity() {

    lateinit var binding: ActivityHomeBinding
    lateinit var adRequest: AdRequest
    private var mInterstitialAd: InterstitialAd? = null
    private var mAdIsLoading: Boolean = false
    val TAG = "HomeActivity"
    var addCount = 0
    var redirectId = 0

    companion object {
        lateinit var homeActivity: HomeActivity
        val REQUEST_CODE_QR_SCAN = 101
    }

    fun startScan() {
        addCount += 1
        redirectId = 1
        if (addCount % 3 == 0) {
            showInterstitial()
        } else {
            afterAdAndRedirect(redirectId)
        }
    }

    fun copyText() {
        addCount += 1
        redirectId = 2
        if (addCount % 2 == 0) {
            showInterstitial()
        } else {
            afterAdAndRedirect(redirectId)
        }
    }

    fun generateCode() {
        addCount += 1
        redirectId = 3
        if (addCount % 2 == 0) {
            showInterstitial()
        } else {
            afterAdAndRedirect(redirectId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initControls()

    }


    private fun initControls() {
        initViews()
        initTabLayout()
        initAdapter()
        loadAd()
    }



    private fun showResultDialog(url: String) {
        val dialog = Dialog(this@HomeActivity)
        if (!this@HomeActivity.isFinishing && dialog.isShowing) {
            dialog.dismiss()
        }

        dialog.setContentView(R.layout.dialog_result)
        dialog.setCancelable(true)

        val window: Window? = dialog.window
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.CENTER)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnCopy = dialog.findViewById<TextView>(R.id.btnCopy)
        val btnOpen = dialog.findViewById<TextView>(R.id.btnOpen)
        val tvMessage = dialog.findViewById<TextView>(R.id.tvUrl)
        val adView = dialog.findViewById<AdView>(R.id.adView)
        adView.loadAd(adRequest)

        if (!isValidValue(url)){
            btnOpen.visibility = View.INVISIBLE
        }else{
            btnOpen.visibility = View.VISIBLE
        }

        tvMessage.text = url

        btnCopy.setOnClickListener {
            copyURL(url)
            dialog.dismiss()
        }

        btnOpen.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
            dialog.dismiss()
        }

        dialog.show()
    }



    private fun initAdapter() {
        val adapter =
                ViewPagerAdapter(
                        this,
                        supportFragmentManager,
                        binding.tabLayout.tabCount
                )
        binding.viewPager.adapter = adapter
        binding.viewPager.addOnPageChangeListener(TabLayoutOnPageChangeListener(binding.tabLayout))

        binding.tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                binding.viewPager.currentItem = tab.position
                Utilities.hideSoftKeyboard(this@HomeActivity)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun initTabLayout() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Scan"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Generate"))
        binding.tabLayout.tabGravity = TabLayout.GRAVITY_FILL
    }

    private fun initViews() {
        homeActivity = this
        Utilities.statusBarColor(R.color.black, this@HomeActivity)
        MobileAds.initialize(this) {}
        adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

    }

    private fun loadAd() {
        /*test ca-app-pub-3940256099942544/8691691433*/
        /*live ca-app-pub-6491242549381158/4568608543*/
        InterstitialAd.load(
                this, "ca-app-pub-6491242549381158/4568608543", adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.d("TAG", adError.message)
                        mInterstitialAd = null
                        mAdIsLoading = false
                        val error = "domain: ${adError.domain}, code: ${adError.code}, " +
                                "message: ${adError.message}"
                    }

                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        Log.d("TAG", "Ad was loaded.")
                        mInterstitialAd = interstitialAd
                        mAdIsLoading = false
                        /*Toast.makeText(this@HomeScreenActivity, "onAdLoaded()", Toast.LENGTH_SHORT).show()*/
                    }
                }
        )
    }

     private fun showInterstitial() {
        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Ad was dismissed.")
                    // Don't forget to set the ad reference to null so you
                    // don't show the ad a second time.
                    mInterstitialAd = null
                    loadAd()
                    afterAdAndRedirect(redirectId)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                    Log.d(TAG, "Ad failed to show.")
                    // Don't forget to set the ad reference to null so you
                    // don't show the ad a second time.
                    mInterstitialAd = null
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Ad showed fullscreen content.")
                    // Called when ad is dismissed.
                }
            }
            mInterstitialAd?.show(this)
        } else {
            loadAd()
            /*Toast.makeText(this, "Ad wasn't loaded.", Toast.LENGTH_SHORT).show()*/
            afterAdAndRedirect(redirectId)
        }
    }

    private fun afterAdAndRedirect(id:Int) {
        when(id){
            1 -> {
                val i = Intent(homeActivity, QrCodeActivity::class.java)
                homeActivity.startActivityForResult(i, REQUEST_CODE_QR_SCAN)
            }
            2->{
                copyURL(ScanQRCodeFragment.copyUrl)
            }
            3->{
                GenerateQRCodeFragment.generateQRCodeFragment.generateQRCode()
            }
            4->{
                finishAffinity()
            }
        }

    }

    private fun copyURL(copyUrl: String) {
        val clipboard: ClipboardManager =
                homeActivity.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("url", copyUrl)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(
                homeActivity,
                "URL copy to your clipboard",
                Toast.LENGTH_LONG
        ).show()
    }

    override fun onBackPressed() {
        if (binding.viewPager.currentItem==0){
            exiDialog()
        }else{
            binding.viewPager.currentItem = 0
        }

    }

    private fun exiDialog() {
        val dialog = Dialog(this@HomeActivity)
        if (!this@HomeActivity.isFinishing && dialog.isShowing) {
            dialog.dismiss()
        }

        dialog.setContentView(R.layout.dialog_exit)
        dialog.setCancelable(true)

        val window: Window? = dialog.window
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.CENTER)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnCancel = dialog.findViewById<TextView>(R.id.btnCancel)
        val btnExit = dialog.findViewById<TextView>(R.id.btnExit)
        val adView = dialog.findViewById<AdView>(R.id.adView)
        adView.loadAd(adRequest)

        btnExit.setOnClickListener {
            redirectId = 4
            showInterstitial()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun isValidValue(value:String): Boolean {
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(value)
        return i.resolveActivity(homeActivity.packageManager) != null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) {
            Log.d(LOGTAG, "COULD NOT GET A GOOD RESULT.")
            if (data == null) return
            //Getting the passed result
            val result = data.getStringExtra("com.blikoon.qrcodescanner.error_decoding_image")
            if (result != null) {
                showResultDialog("QR Code could not be scanned")
            }
            return
        }

        if (requestCode == REQUEST_CODE_QR_SCAN) {
            if (data == null) return
            //Getting the passed result
            val result = data.getStringExtra("com.blikoon.qrcodescanner.got_qr_scan_relult")
            Log.d(LOGTAG,"Have scan result in your app activity :$result")
            ScanQRCodeFragment.scanQRCodeFragment.saveListInLocal(result!!)
            showResultDialog(result)
        }
    }


}

