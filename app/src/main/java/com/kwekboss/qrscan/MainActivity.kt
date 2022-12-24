package com.kwekboss.qrscan

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.kwekboss.qrscan.Constants.CAMERA_REQUEST_CODE
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog


class MainActivity : AppCompatActivity(),EasyPermissions.PermissionCallbacks{

    private lateinit var scanner: CodeScannerView
    private lateinit var codeScan: CodeScanner
    private lateinit var scanResult: TextView
    private lateinit var visitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        scanner = findViewById(R.id.code_scanner)
        scanResult = findViewById(R.id.scannedresult)
        visitButton = findViewById(R.id.visit_link)

      requestCameraPermission()
           startScanning()

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,
            this@MainActivity)
    }

 private fun startScanning(){

    codeScan = CodeScanner(this,scanner)
     codeScan.apply {
         camera = CodeScanner.CAMERA_BACK
         formats = CodeScanner.ALL_FORMATS
         autoFocusMode = AutoFocusMode.SAFE
         scanMode = ScanMode.CONTINUOUS
         isAutoFocusEnabled = true
         isFlashEnabled = false

         decodeCallback = DecodeCallback { scannedResult->
             runOnUiThread {
              scanResult.text = scannedResult.text
                 if(scanResult.text.contains("https://") || scanResult.text.contains(".com")){
                     visitButton.visibility = View.VISIBLE

                     visitButton.setOnClickListener {
                       val url = Uri.parse(scannedResult.text)
                         val urlIntent = Intent(Intent.ACTION_VIEW,url)

                         val chooser = Intent.createChooser(urlIntent,"Browse Using...")
                         startActivity(chooser)
                     }
                 } else{
                     visitButton.visibility = View.GONE
                 }

             }
         }

         errorCallback = ErrorCallback { errorMessage->
             runOnUiThread {
           Toast.makeText(this@MainActivity, "$errorMessage", Toast.LENGTH_SHORT).show()
             }
         }
         scanner.setOnClickListener {
             codeScan.startPreview()
         }
     }
}

    override fun onResume() {
        super.onResume()
        codeScan.startPreview()
    }

    override fun onPause() {
      codeScan.releaseResources()
        super.onPause()
    }

    // Handling Runtime Permission with the Easy Library
  private fun requestCameraPermission(){
        EasyPermissions.requestPermissions(this,
            getString(R.string.message),
            CAMERA_REQUEST_CODE,Manifest.permission.CAMERA)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
       if(EasyPermissions.somePermissionDenied(this,perms.first())){
           SettingsDialog.Builder(this@MainActivity).build().show()
       }
        else{
            requestCameraPermission()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {

    }


}