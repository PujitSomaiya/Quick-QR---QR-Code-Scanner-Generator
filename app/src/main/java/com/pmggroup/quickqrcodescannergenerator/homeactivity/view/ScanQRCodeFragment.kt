package com.pmggroup.quickqrcodescannergenerator.homeactivity.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.pmggroup.quickqrcodescannergenerator.R
import com.pmggroup.quickqrcodescannergenerator.homeactivity.adapter.HistoryListAdapter
import java.lang.reflect.Type


class ScanQRCodeFragment : Fragment() {

    lateinit var rvHistory: RecyclerView
    lateinit var tvNoHistory: TextView
    var historyListAll = arrayListOf<String>()
    var historyList = arrayListOf<String>()
    lateinit var prefs: SharedPreferences
    lateinit var prefsEditor: SharedPreferences.Editor
    lateinit var historyListAdapter: HistoryListAdapter
    lateinit var floatingActionButton: FloatingActionButton
    lateinit var adRequest: AdRequest

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_generate, container, false)
        initControls(view)
        return view
    }

    private fun initControls(view: View) {
        initViews(view)
        initRecyclerView()
    }

    @SuppressLint("CommitPrefEdits")
    private fun initViews(view: View) {
        MobileAds.initialize(HomeActivity.homeActivity) {}
        adRequest = AdRequest.Builder().build()
        scanQRCodeFragment = this
        rvHistory = view.findViewById(R.id.rvHistory)
        tvNoHistory = view.findViewById(R.id.tvNoHistory)
        floatingActionButton = view.findViewById(R.id.fab)
        prefs = HomeActivity.homeActivity.getSharedPreferences("historyFile", Context.MODE_PRIVATE)
        prefsEditor = prefs.edit()

        floatingActionButton.setOnClickListener {
            checkPermissions()
        }
    }

    private fun initRecyclerView() {
        historyListAll = arrayListOf()
        historyListAll = getListFromLocal()

        if (historyListAll.size == 0) {
            tvNoHistory.visibility = View.VISIBLE
            rvHistory.visibility = View.GONE
        } else {
            tvNoHistory.visibility = View.GONE
            rvHistory.visibility = View.VISIBLE
        }

        historyListAdapter = HistoryListAdapter(historyListAll, {
            //copy
            copyUrl = it
            HomeActivity.homeActivity.copyText()
        }, {
            //open
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(it)
            startActivity(i)
        },{
            deleteDialog(it)

        })
        rvHistory.layoutManager = LinearLayoutManager(HomeActivity.homeActivity)
        rvHistory.adapter = historyListAdapter
    }

    companion object {
        lateinit var scanQRCodeFragment: ScanQRCodeFragment
        lateinit var copyUrl: String
        fun ScanQRCodeFragment() {
            // Required empty public constructor
        }
    }

    private fun checkPermissions() {
        Dexter.withContext(activity)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            HomeActivity.homeActivity.startScan()
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied) {
                            // permission is denied permenantly, navigate user to app settings
                            showSettingsDialog()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                            permissions: List<com.karumi.dexter.listener.PermissionRequest?>?,
                            token: PermissionToken
                    ) {
                        if (!permissions.isNullOrEmpty()) {
                            token.continuePermissionRequest()
                        }
                    }
                })
                .onSameThread()
                .check()
    }

    private fun showSettingsDialog() {
        val builder =
                AlertDialog.Builder(HomeActivity.homeActivity)
        builder.setTitle("Need Permissions")
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.")
        builder.setPositiveButton("GOTO SETTINGS"
        ) { dialog, which ->
            dialog.cancel()
            openSettings()
        }
        builder.setNegativeButton("Cancel"
        ) { dialog, which -> dialog.cancel() }
        builder.show()
    }

    fun deleteDialog(i: Int) {
        val dialog = Dialog(HomeActivity.homeActivity)
        if (!HomeActivity.homeActivity.isFinishing && dialog.isShowing) {
            dialog.dismiss()
        }

        dialog.setContentView(R.layout.dialog_result)
        dialog.setCancelable(true)

        val window: Window? = dialog.window
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.CENTER)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnCancel = dialog.findViewById<TextView>(R.id.btnCopy)
        val btnDelete = dialog.findViewById<TextView>(R.id.btnOpen)
        val tvMessage = dialog.findViewById<TextView>(R.id.tvUrl)
        val adView = dialog.findViewById<AdView>(R.id.adView)
        adView.loadAd(adRequest)

        tvMessage.text = "Are you sure you want to delete?"
        btnCancel.text = "Cancel"
        btnDelete.text = "Delete"

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnDelete.setOnClickListener {
            historyListAll.removeAt(i)
            saveListInLocal()
            dialog.dismiss()
        }

        dialog.show()
    }

    // navigating user to app settings
    private fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", HomeActivity.homeActivity.packageName, null)
        intent.data = uri
        startActivityForResult(intent, 101)
    }

    private fun getListFromLocal(): ArrayList<String> {
        val prefs: SharedPreferences = HomeActivity.homeActivity.getSharedPreferences("HistoryList", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = prefs.getString("history", null)
        val type: Type = object : TypeToken<ArrayList<String?>?>() {}.type
        return if (json != null)
            gson.fromJson(json, type)
        else arrayListOf()
    }

    fun saveListInLocal(string: String) {
        historyList.clear()
        historyList.addAll(historyListAll)
        historyList.add(string)
        val prefs: SharedPreferences = HomeActivity.homeActivity.getSharedPreferences("HistoryList", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val gson = Gson()
        val json = gson.toJson(historyList)
        editor.putString("history", json)
        editor.apply() // This line is IMPORTANT !!!
        initRecyclerView()
    }

    fun saveListInLocal() {
        historyList.clear()
        historyList.addAll(historyListAll)
        val prefs: SharedPreferences = HomeActivity.homeActivity.getSharedPreferences("HistoryList", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val gson = Gson()
        val json = gson.toJson(historyList)
        editor.putString("history", json)
        editor.apply() // This line is IMPORTANT !!!
        initRecyclerView()
    }


}