package com.sijuru

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.ViewfinderView
import org.w3c.dom.Text
import java.util.*


class ScanBarcodeActivity : AppCompatActivity(), DecoratedBarcodeView.TorchListener,
    View.OnClickListener {

    private var captureManager: CaptureManager?=null
    private var barcodeScannerView : DecoratedBarcodeView?=null
    private var switchFlashLightButton : ImageButton?=null
    private var zxing_status_view : TextView?=null
    private var viewfinderView : ViewfinderView?=null
    private var active : Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_barcode)

        barcodeScannerView = findViewById(R.id.zxing_barcode_scanner)
        switchFlashLightButton = findViewById(R.id.switch_flashlight)
        zxing_status_view = findViewById(R.id.zxing_status_view)

//        if (hasFlash()) switchFlashLightButton?.visibility = View.GONE
        zxing_status_view?.text = "Silakan pindai Karcis / QRCode"
        captureManager = CaptureManager(this, barcodeScannerView)
        captureManager?.initializeFromIntent(intent, savedInstanceState)
        captureManager?.setShowMissingCameraPermissionDialog(false)
        captureManager?.decode()

        barcodeScannerView?.setTorchListener(this)
        switchFlashLightButton?.setOnClickListener(this)
    }

    private fun hasFlash(): Boolean {
        return applicationContext.packageManager
            .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
    }

    fun changeMaskColor(view: View?) {
        val rnd = Random()
        val color: Int = Color.argb(100, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
        viewfinderView!!.setMaskColor(color)
    }

    fun changeLaserVisibility(visible: Boolean) {
        viewfinderView!!.setLaserVisibility(visible)
    }

    override fun onResume() {
        super.onResume()
        captureManager?.onResume()
    }

    override fun onPause() {
        super.onPause()
        captureManager?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        captureManager?.onDestroy()
    }

    override fun onTorchOn() {
        switchFlashLightButton?.setImageResource(R.drawable.ic_flash_on)
        active = false
    }

    override fun onTorchOff() {
        switchFlashLightButton?.setImageResource(R.drawable.ic_flash_off)
        active = true
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onClick(v: View?) {
        when(v){
            switchFlashLightButton->{
                if (active) {
                    barcodeScannerView?.setTorchOn()
                } else {
                    barcodeScannerView?.setTorchOff()
                }
            }
        }
    }
}