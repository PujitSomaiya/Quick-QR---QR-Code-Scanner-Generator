package com.pmggroup.quickqrcodescannergenerator.homeactivity.view

import android.Manifest
import android.app.AlertDialog
import android.content.Context.WINDOW_SERVICE
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.*
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.google.zxing.WriterException
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.pmggroup.quickqrcodescannergenerator.R
import com.pmggroup.quickqrcodescannergenerator.Utilities


class GenerateQRCodeFragment : Fragment() {

    var bitmap: Bitmap? = null
    var qrgEncoder: QRGEncoder? = null
    lateinit var btnGenerate:Button
    lateinit var edURL:EditText
    lateinit var imgClose:ImageView
    lateinit var imgQRCode:ImageView
    lateinit var imgShare:ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_scan, container, false)
        initControls(view)
        return view
    }

    private fun initControls(view: View) {
        initViews(view)
        onClickListeners()
    }

    private fun onClickListeners() {

        imgShare.setOnClickListener {
            checkPermissions()
        }

        imgClose.setOnClickListener {
            edURL.setText("")
            imgShare.visibility = View.GONE
            imgQRCode.visibility = View.GONE
            Utilities.hideSoftKeyboard(requireActivity())
        }

        btnGenerate.setOnClickListener {
            Utilities.hideSoftKeyboard(requireActivity())
            if (TextUtils.isEmpty(edURL.text.trim().toString())) {
                Toast.makeText(
                    HomeActivity.homeActivity,
                    "Enter some text to generate QR Code",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                HomeActivity.homeActivity.generateCode()
            }
        }
    }


    fun generateQRCode() {
        val display = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            HomeActivity.homeActivity.display
        } else {
            val manager = HomeActivity.homeActivity.getSystemService(WINDOW_SERVICE) as WindowManager?
             manager!!.defaultDisplay
        }
        val point = Point()
        display?.getSize(point)
        val width: Int = point.x
        val height: Int = point.y
        var dimen = if (width < height) width else height
        dimen = dimen * 3 / 4
        qrgEncoder =
                QRGEncoder(edURL.text.trim().toString(), null, QRGContents.Type.TEXT, dimen)
        try {
            bitmap = qrgEncoder!!.encodeAsBitmap()
            imgQRCode.setImageBitmap(bitmap)
            imgShare.visibility = View.VISIBLE
            imgQRCode.visibility = View.VISIBLE
        } catch (e: WriterException) {
            Log.e("Tag", e.toString())
        }
    }


    private fun sharePalette(bitmap: Bitmap, url: String) {
        val bitmapPath = MediaStore.Images.Media.insertImage(
            activity?.contentResolver,
            bitmap,
            "QR Code",
            "Generate QR Code"
        )
        val bitmapUri: Uri = Uri.parse(bitmapPath)
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/png"
        intent.putExtra(Intent.EXTRA_STREAM, bitmapUri)
        intent.putExtra(
            Intent.EXTRA_TEXT, "Check out free QR Code scanner and generator App\n" +
                    "Free to download, free to use directly\n" +
                    "\n" +
                    "So what are you waiting for..?\n" +
                    "Download the app free now... " + "\n" + "https://play.google.com/store/apps/details?id=${HomeActivity.homeActivity.packageName}"
        );
        startActivity(Intent.createChooser(intent, url))
    }

    private fun initViews(view: View) {
        generateQRCodeFragment = this
        btnGenerate = view.findViewById(R.id.btnGenerate)
        edURL = view.findViewById(R.id.edURL)
        imgClose = view.findViewById(R.id.imgClose)
        imgQRCode = view.findViewById(R.id.imgQRCode)
        imgShare = view.findViewById(R.id.imgShare)
        edURL.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(
                HomeActivity.homeActivity,
                R.color.black
            )
        )
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
                        sharePalette(bitmap!!, edURL.text.toString().trim())
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
        builder.setPositiveButton(
            "GOTO SETTINGS"
        ) { dialog, which ->
            dialog.cancel()
            openSettings()
        }
        builder.setNegativeButton(
            "Cancel"
        ) { dialog, which -> dialog.cancel() }
        builder.show()
    }

    // navigating user to app settings
    private fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", HomeActivity.homeActivity.packageName, null)
        intent.data = uri
        startActivityForResult(intent, 101)
    }

    companion object {
        lateinit var generateQRCodeFragment: GenerateQRCodeFragment
        fun GenerateQRCodeFragment() {
            // Required empty public constructor
        }
    }
}