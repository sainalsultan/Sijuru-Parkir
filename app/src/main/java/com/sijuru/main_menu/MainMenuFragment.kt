package com.sijuru.main_menu

import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.Camera
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import android.media.MediaActionSound
import android.os.*
import android.service.controls.ControlsProviderService
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.*
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.graphics.PathParser
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.kioser.profile.camera.v1.CameraV1Util
import com.sijuru.R
import com.sijuru.ScanBarcodeActivity
import com.sijuru.core.data.local.entity.*
import com.sijuru.core.data.response.BaseResponseList
import com.sijuru.core.data.response.BaseResponseObject
import com.sijuru.core.utils.BitmapUtils
import com.sijuru.core.utils.FileUtil
import com.sijuru.core.utils.FragmentHelper
import com.sijuru.core.utils.SessionManager
import com.sijuru.login.MainListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Semaphore
import javax.inject.Inject
import kotlin.math.min


@AndroidEntryPoint
class MainMenuFragment : Fragment(), TextureView.SurfaceTextureListener, View.OnClickListener,
    AdapterDetailList.AdapterDetailListListener, AdapterListButton.AdapterListButtonListener,
    MainListener {

    companion object {
        fun newInstance() = MainMenuFragment()
    }

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var viewModel: MainMenuViewModel

    @Inject
    lateinit var fragmentHelper: FragmentHelper

    private val adapterDetailList by lazy { AdapterDetailList() }
    private val adapterListButton by lazy { AdapterListButton() }

    private var textureView: TextureView? = null
    private var button_1: Button? = null
    private var button_2: Button? = null
    private var button_3: Button? = null
    private var button_4: Button? = null
    private var button_5: Button? = null
    private var button_6: Button? = null
    private var img_detail: ImageView? = null
    private var constraintlayout_detail_list: ConstraintLayout? = null
    private var constraintlayout_main_menu: ConstraintLayout? = null
    private var recyclerview_detail_list: RecyclerView? = null
    private var recyclerview_button: RecyclerView? = null
    private var imagebutton_flash: ImageButton? = null
    private var name: String = ""
    private var uuid: String? = null
    private var parkingFee: String? = null

    private var mCaptureSession: CameraCaptureSession? = null
    private var mCameraDevice: CameraDevice? = null
    private var mPreviewSize: Size? = null
    private var mCameraId: String = ""
    private val mCameraOpenCloseLock: Semaphore = Semaphore(1)
    private var mPreviewRequestBuilder: CaptureRequest.Builder? = null
    private var mImageReader: ImageReader? = null
    private var mPreviewRequest: CaptureRequest? = null
    private var mBackgroundHandler: Handler? = null
    private val REQUEST_CAMERA_PERMISSION = 1
    private val MAX_PREVIEW_WIDTH = 1920
    private val MAX_PREVIEW_HEIGHT = 1080
    private var mBackgroundThread: HandlerThread? = null
    private var detailActivated: Boolean = true
    private var type = ""
    private var vehicle = ""
    private var price_base = ""
//    private var location = ""
    private var price = ""
    private var torchOn = true

    private var camera: Camera? = null
    private var cameraId = Camera.CameraInfo.CAMERA_FACING_BACK
    lateinit var displayMetrics: DisplayMetrics
    private var resolutionList: List<Camera.Size>? = ArrayList()
    private var cameraManager: CameraManager? = null
    private var safeToTakePicture = false   //Handler untuk RuntimeException takePicture
    private var data_vehicle : VehicleSijuruParkingTypeDetai? = null
    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        displayMetrics = requireActivity().resources.displayMetrics
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        cameraManager = activity?.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // in here you can do logic when backPress is clicked
                if (doubleBackToExitPressedOnce) {
                    isEnabled = false
                    activity?.onBackPressed()
                } else {
                    doubleBackToExitPressedOnce = true
                    Toast.makeText(
                        requireActivity(),
                        "Tekan sekali lagi untuk keluar aplikasi!",
                        Toast.LENGTH_SHORT
                    ).apply {
                        setGravity(Gravity.CENTER,0,0)
                    }.show()
                    Handler(Looper.myLooper()!!).postDelayed({
                        doubleBackToExitPressedOnce = false
                    }, 2000)
                }
            }
        })

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerview_detail_list = view.findViewById(R.id.recyclerview_detail_list)

        Log.e(TAG, "onViewCreated: ${sessionManager.getPriceBase()}") //[5000, 10000, 12000]
        Log.e(TAG, "onViewCreated: ${sessionManager.getPriceIncrement()}") //[0, 0, 0]
        Log.e(TAG, "onViewCreated: ${sessionManager.getPriceIncrement_price()}") //[0, 0, 0]
        Log.e(TAG, "onViewCreated: ${sessionManager.getPriceMaxPrice()}") //[10000, 20000, 25000]
        Log.e(TAG, "onViewCreated: ${sessionManager.getType()}") //[roda_2, roda_4, roda_lainnya]
        Log.e(TAG, "onViewCreated: ${sessionManager.getTypeDetail()}") //3
        Log.e(TAG, "onViewCreated: ${sessionManager.getName()}") //[motor, mobil, lainnya]
        Log.e(TAG, "onViewCreated: ${sessionManager.getOperatorLocation()}") //Jalan Perintis Kemerdekaan No 8

        val data = ArrayList<DataUser>()
        data.add(DataUser(1, "Riwayat"))
        data.add(DataUser(2, "Keluar"))

        val dataSizeButton = formatString(sessionManager.getName())
        val type = formatString(sessionManager.getType())
        val price_base = formatString(sessionManager.getPriceBase())
        val price_increment = formatString(sessionManager.getPriceIncrement())
        val price_increment_price = formatString(sessionManager.getPriceIncrement_price())
        val price_max_price = formatString(sessionManager.getPriceMaxPrice())


        //dataButton.add(VehicleSijuruParkingTypeDetai("Motor","","","","",""))
        //dataButton.add(VehicleSijuruParkingTypeDetai("Motor","","","","",""))
        //dataButton.add(VehicleSijuruParkingTypeDetai("Motor","","","","",""))
        //dataButton.add(VehicleSijuruParkingTypeDetai("Motor","","","","",""))
        //dataButton.add(VehicleSijuruParkingTypeDetai("Motor","","","","",""))
        val dataButton = ArrayList<VehicleSijuruParkingTypeDetai>()
        for ((index,value) in dataSizeButton.withIndex()){
            dataButton.add(VehicleSijuruParkingTypeDetai(
                value,price_base.get(index),
                price_increment.get(index),
                price_increment_price.get(index),
                price_max_price.get(index),
                type.get(index)
            ))
        }

        recyclerview_detail_list?.apply {
            setHasFixedSize(true)
            adapter = adapterDetailList.also { it.setAdapter(data) }
        }

        val mlayoutManager = GridLayoutManager(activity, 2)
        mlayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (adapterListButton.getItemViewType(position)) {
                    1 -> {
                        println("1")
                        1
                    }
                    2 -> {
                        println("2")
                        2
                    }
                    else -> {
                        println("else 1")
                        1
                    }
                }
            }
        }

        recyclerview_button?.apply {
            setHasFixedSize(true)
            layoutManager = mlayoutManager
            adapter = adapterListButton.also { it.setAdapter(dataButton) }
        }

        if (sessionManager.getParkingType().equals("progressive")){
            button_6?.visibility = View.VISIBLE
            button_6?.text = "Kendaraan Keluar"
        }
        else{
            button_6?.visibility = View.GONE
        }

        setFragmentResultListener("picture") { key: String, bundle: Bundle ->
            stopCamera()
            if (textureView?.isAvailable!!) {
                //openCamera(textureView?.width!!, textureView?.height!!)
                setupCamera(textureView?.width!!, textureView?.height!!)
                textureView?.surfaceTexture?.let { startCameraPreview(it) }
            } else {
                textureView?.surfaceTextureListener = this
            }
        }
    }

    private fun formatString(name: String): List<String> {
        return name.replace("[","").replace("]","").replace(" ", "").split(",")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main_menu, container, false)
        textureView = view.findViewById(R.id.textureview)
        img_detail = view.findViewById(R.id.img_detail)
        /*button_1 = view.findViewById(R.id.button_1)
        button_2 = view.findViewById(R.id.button_2)
        button_3 = view.findViewById(R.id.button_3)
        button_4 = view.findViewById(R.id.button_4)
        button_5 = view.findViewById(R.id.button_5)*/
        button_6 = view.findViewById(R.id.button_6)
        recyclerview_button = view.findViewById(R.id.recyclerview_button)
        constraintlayout_detail_list = view.findViewById(R.id.constraintlayout_detail_list)
        constraintlayout_main_menu = view.findViewById(R.id.constraintlayout_main_menu)
        imagebutton_flash = view.findViewById(R.id.imagebutton_flash)

        img_detail?.visibility = View.VISIBLE
        name = sessionManager.getName().replace("[", "").replace("]", "").replace(" ", "")
        type = sessionManager.getType().replace("[", "").replace("]", "").replace(" ", "")
        price_base = sessionManager.getPriceBase().replace("[", "").replace("]", "").replace(" ", "")
//        location = sessionManager.getOperatorLocation().replace("[", "").replace("]", "")

        when (sessionManager.getTypeDetail()) {
            "1" -> {
                showButton(1, name)
            }
            "2" -> {
                showButton(2, name)
            }
            "3" -> {
                showButton(3, name)
            }
            "4" -> {
                showButton(4, name)
            }
            "5" -> {
                showButton(5, name)
            }
        }

        button_1?.setOnClickListener(this)
        button_2?.setOnClickListener(this)
        button_3?.setOnClickListener(this)
        button_4?.setOnClickListener(this)
        button_5?.setOnClickListener(this)
        button_6?.setOnClickListener(this)
        img_detail?.setOnClickListener(this)
        constraintlayout_main_menu?.setOnClickListener(this)
        imagebutton_flash?.setOnClickListener(this)
        adapterDetailList.OnClickItemAllProduct(this)
        adapterListButton.OnClickItem(this)

        return view
    }

    private fun showButton(i: Int, name: String) {
        when (i) {
            1 -> {
                button_1?.visibility = View.VISIBLE
                button_2?.visibility = View.GONE
                button_3?.visibility = View.GONE
                button_4?.visibility = View.GONE
                button_5?.visibility = View.GONE
                button_1?.text = name.split(",").get(0)
            }
            2 -> {
                button_1?.visibility = View.VISIBLE
                button_2?.visibility = View.VISIBLE
                button_3?.visibility = View.GONE
                button_4?.visibility = View.GONE
                button_5?.visibility = View.GONE
                button_1?.text = name.split(",").get(0)
                button_2?.text = name.split(",").get(1)
            }
            3 -> {
                button_1?.visibility = View.VISIBLE
                button_2?.visibility = View.VISIBLE
                button_3?.visibility = View.VISIBLE
                button_4?.visibility = View.GONE
                button_5?.visibility = View.GONE
                button_1?.text = name.split(",").get(0)
                button_2?.text = name.split(",").get(1)
                button_3?.text = name.split(",").get(2)
            }
            4 -> {
                button_1?.visibility = View.VISIBLE
                button_2?.visibility = View.VISIBLE
                button_3?.visibility = View.VISIBLE
                button_4?.visibility = View.VISIBLE
                button_5?.visibility = View.GONE
                button_1?.text = name.split(",").get(0)
                button_2?.text = name.split(",").get(1)
                button_3?.text = name.split(",").get(2)
                button_4?.text = name.split(",").get(3)
            }
            5 -> {
                button_1?.visibility = View.VISIBLE
                button_2?.visibility = View.VISIBLE
                button_3?.visibility = View.VISIBLE
                button_4?.visibility = View.VISIBLE
                button_5?.visibility = View.VISIBLE
                button_1?.text = name.split(",").get(0)
                button_2?.text = name.split(",").get(1)
                button_3?.text = name.split(",").get(2)
                button_4?.text = name.split(",").get(3)
                button_4?.text = name.split(",").get(4)
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        Log.e(TAG, "onResume: ")
        startBackgroundThread()

        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // a camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).
        if (textureView?.isAvailable!!) {
            //openCamera(textureView?.width!!, textureView?.height!!)
            setupCamera(textureView?.width!!, textureView?.height!!)
            textureView?.surfaceTexture?.let { startCameraPreview(it) }
        } else {
            textureView?.surfaceTextureListener = this
        }
    }

    override fun onPause() {
        super.onPause()
        closeCamera()
        stopBackgroundThread()
    }

    override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) {
        //openCamera(width,height)
        setupCamera(width, height)
        startCameraPreview(texture)
    }

    override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) {}

    override fun onSurfaceTextureDestroyed(texture: SurfaceTexture): Boolean {
        stopCamera()
        return true
    }

    override fun onSurfaceTextureUpdated(texture: SurfaceTexture) {}

    private fun startCameraPreview(surfaceTexture: SurfaceTexture) {
        try {
            camera?.setPreviewTexture(surfaceTexture)
            camera?.startPreview()
            safeToTakePicture = true
        } catch (e: IOException) {
            Log.e(TAG, "Error start camera preview: " + e.message)
        }
    }

    private fun stopCamera() {
        try {
            camera?.stopPreview()
            camera?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error stop camera preview: " + e.message)
        }
    }

    private fun requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.CAMERA
            )
        ) {
            AlertDialog.Builder(activity?.applicationContext)
                .setMessage("R string request permission")
                .setPositiveButton(
                    android.R.string.ok
                ) { dialog, which ->
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.CAMERA),
                        REQUEST_CAMERA_PERMISSION
                    )
                }
                .setNegativeButton(android.R.string.cancel) { dialog, which -> }
                .create()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.size != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    activity?.applicationContext,
                    "ERROR: Camera permissions not granted",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun setupCamera(width: Int, height: Int, resolutionPicked: Int? = null) {
        camera = CameraV1Util.openCamera(cameraId)
        camera?.let { camera: Camera ->
            val cameraOrientation =
                CameraV1Util.getCameraDisplayOrientation(requireActivity(), cameraId)
            camera.setDisplayOrientation(cameraOrientation)
            val parameters: Camera.Parameters = camera.parameters
            resolutionList = parameters.supportedPreviewSizes

            val bestPreviewSize: Camera.Size? =
                if (resolutionPicked == null)
                    CameraV1Util.getBestPreviewSizeMine(
                        parameters.supportedPreviewSizes,
                        displayMetrics.widthPixels
                    )
                else
                    parameters.supportedPreviewSizes.get(resolutionPicked)

            Log.d(
                "BuildConfig.TAG",
                "selected Size ${bestPreviewSize?.width} x ${bestPreviewSize?.height}"
            )
            bestPreviewSize?.let { previewSize: Camera.Size ->
                textureView?.setTransform(
                    CameraV1Util.getCropCenterScaleMatrix(
                        cameraOrientation,
                        width.toFloat(),
                        height.toFloat(),
                        previewSize.width.toFloat(),
                        previewSize.height.toFloat(),
                    )
                )

                parameters.setPictureSize(previewSize.width, previewSize.height)
                parameters.setPreviewSize(previewSize.width, previewSize.height)
            }

            if (CameraV1Util.isContinuousFocusModeSupported(parameters.supportedFocusModes)) {
                parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
            }

            try {
                camera.parameters = parameters
            } catch (e: java.lang.Exception) {
                //device not support arbitrary preview sizes
            }

            checkFlash(parameters)
        } ?: run {
            requestCameraPermission()
            //Toast.makeText(requireActivity(), "camera_unavailable", Toast.LENGTH_SHORT).show()
        }
    }

    internal class CompareSizesByArea : Comparator<Size> {
        override fun compare(lhs: Size?, rhs: Size?): Int {

            // We cast here to ensure the multiplications won't overflow
            return java.lang.Long.signum(
                lhs?.width?.toLong()!! * lhs.height -
                        rhs?.width?.toLong()!! * rhs.height
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "onDestroy: ")
        textureView?.surfaceTextureListener = null
    }

    override fun onStop() {
        super.onStop()
        stopCamera()
        Log.e(TAG, "onStop: ")
    }

    private fun closeCamera() {
        try {
            mCameraOpenCloseLock.acquire()
            if (null != mCaptureSession) {
                mCaptureSession!!.close()
                mCaptureSession = null
            }
            if (null != mCameraDevice) {
                mCameraDevice!!.close()
                mCameraDevice = null
            }
            if (null != mImageReader) {
                mImageReader!!.close()
                mImageReader = null
            }
        } catch (e: InterruptedException) {
            throw java.lang.RuntimeException("Interrupted while trying to lock camera closing.", e)
        } finally {
            mCameraOpenCloseLock.release()
        }
    }

    private fun chooseOptimalSize(
        choices: Array<Size>, textureViewWidth: Int,
        textureViewHeight: Int, maxWidth: Int, maxHeight: Int, aspectRatio: Size?
    ): Size? {

        // Collect the supported resolutions that are at least as big as the preview Surface
        val bigEnough: MutableList<Size> = ArrayList()
        // Collect the supported resolutions that are smaller than the preview Surface
        val notBigEnough: MutableList<Size> = ArrayList()
        val w = aspectRatio?.width
        val h = aspectRatio?.height
        for (option in choices) {
            if (option.width <= maxWidth && option.height <= maxHeight && option.height == option.width * h!! / w!!) {
                if (option.width >= textureViewWidth &&
                    option.height >= textureViewHeight
                ) {
                    bigEnough.add(option)
                } else {
                    notBigEnough.add(option)
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        return if (bigEnough.size > 0) {
            Collections.min(bigEnough, CompareSizesByArea())
        } else if (notBigEnough.size > 0) {
            Collections.max(notBigEnough, CompareSizesByArea())
        } else {
            Log.e("Camera2", "Couldn't find any suitable preview size")
            choices[0]
        }
    }

    private fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("CameraBackground")
        mBackgroundThread?.start()
        mBackgroundHandler = Handler(mBackgroundThread?.looper!!)
    }

    private fun stopBackgroundThread() {
        mBackgroundThread?.quitSafely()
        try {
            mBackgroundThread?.join()
            mBackgroundThread = null
            mBackgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            /*button_1 -> {
                vehicle = name.split(",").get(0)
                price = price_base.split(",").get(0)
                button_1?.text = name.split(",").get(0)
                takePicture(data)
            }
            button_2 -> {
                vehicle = name.split(",").get(1)
                price = price_base.split (",").get(1)
                button_2?.text = name.split(",").get(1)
                takePicture(data)
            }
            button_3 -> {
                vehicle = name.split(",").get(2)
                price = price_base.split (",").get(2)
                button_3?.text = name.split(",").get(2)
                takePicture(data)
            }
            button_4 -> {
                vehicle = name.split(",").get(3)
                price = price_base.split (",").get(3)
                button_4?.text = name.split(",").get(3)
                takePicture(data)
            }
            button_5 -> {
                vehicle = name.split(",").get(4)
                price = price_base.split (",").get(4)
                button_5?.text = name.split(",").get(4)
                takePicture(data)
            }*/
            button_6 -> {
                /*val integrator = IntentIntegrator(requireActivity())
                integrator.setPrompt("Silakan pindai barcode atau QRCode")
                integrator.setCameraId(0)
                integrator.setOrientationLocked(true)
                integrator.setBeepEnabled(false)
                integrator.setCaptureActivity(ScanBarcodeActivity::class.java)
                integrator.initiateScan()*/
                val options = ScanOptions()
                options.setOrientationLocked(false)
                options.setCaptureActivity(ScanBarcodeActivity::class.java)
                barcodeLauncher.launch(options)

                //findNavController().navigate(R.id.action_mainMenuFragment_to_progressiveScanBarcodeFragment)

                /*val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val currentDateandTime: String = sdf.format(Date())

                findNavController().navigate(
                    R.id.action_mainMenuFragment_to_receiptFragment,
                    bundleOf(
                        "ticket_number" to "Sijuru-01",
                        "plate" to "B591RI",
                        "operator_name" to sessionManager.getOperatorName(),
                        "vehicle_name" to "mobil",
                        "phone_number" to sessionManager.getOperatorPhone(),
                        "first_time" to "2022-04-01 22:00:40",
                        "end_time" to currentDateandTime,
                        "parking_fee" to "Rp 5000"
                    )
                )*/
            }
            img_detail -> {
                if (detailActivated) {
                    detailActivated = false
                    constraintlayout_detail_list?.visibility = View.VISIBLE
                    //constraintlayout_detail_list?.animate()?.alpha(1.0f)
                } else {
                    detailActivated = true
                    constraintlayout_detail_list?.visibility = View.GONE
                    //constraintlayout_detail_list?.animate()?.alpha(1.0f)
                }
            }
            constraintlayout_main_menu -> {
                if (!detailActivated) {
                    detailActivated = true
                    constraintlayout_detail_list?.visibility = View.GONE
                }
            }
            imagebutton_flash -> {
                /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    toogleTorch()
                }else{
                    Toast.makeText(requireActivity(),"Fitur tidak tersedia", Toast.LENGTH_LONG).show()
                }*/
                toogleFlash()
            }
        }
    }

    private fun takePicture(data: VehicleSijuruParkingTypeDetai) {
        data_vehicle = data
        if (safeToTakePicture) camera?.takePicture(shutterCallback, null, null, pictureCallback)
        safeToTakePicture = false
    }

    private val shutterCallback = Camera.ShutterCallback { playShutterSound() }

    private val pictureCallback = object : Camera.PictureCallback {
        override fun onPictureTaken(data: ByteArray, camera: Camera) {
//                textureViewCamera?.visibility = View.GONE

            lifecycleScope?.launch(Dispatchers.IO) {
                val orientation =
                    CameraV1Util.getCameraDisplayOrientation(requireActivity(), cameraId)
                var angle = 90f
                var flipX = false
                val info = Camera.CameraInfo()
                Camera.getCameraInfo(cameraId, info)
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    angle = 270f
                    flipX = true
                }

                try {
                    val kilobytes = data.size / 1024
                    val megabytes = kilobytes / 1024
                    var logSize = "Resolusi Kamera :"
                    resolutionList?.forEach {
                        logSize += "- ${it.width}x${it.height}\n"
                    }

                    //manipulate image
                    val bitmap: Bitmap? = rotateImage(
                        BitmapFactory.decodeByteArray(data, 0, data.size, null),
                        angle,
                        flipX
                    )     //rotate & flip horizontally
                    val shapeCrop =
                        bitmap?.let { convertToHeart(it) }                                                          //crop using preview shape
                    val side = min(shapeCrop!!.width, shapeCrop.height)
                    val xOffset = (shapeCrop.width - side) / 2
                    val yOffset = (shapeCrop.height - side) / 2
                    val bitmapCropped = Bitmap.createBitmap(
                        bitmap,
                        xOffset,
                        yOffset,
                        side,
                        side
                    )                                //crop background height
                    logSize += "resolution before cropped: ${bitmap.width} x ${bitmap.height}\n"
                    logSize += "resolution after cropped: ${bitmapCropped?.width} x ${bitmapCropped?.height}\n"

                    FileUtil.savePicture(requireActivity(), bitmapCropped, orientation)
                        ?.let { file: File ->
                            withContext(Dispatchers.Main) {

                                val kilobytes2 = file.length() / 1024
                                val megabytes2 = kilobytes2 / 1024
                                logSize += "onSaved: ${file.length()} B, ${kilobytes2} KB, ${megabytes2} MB\n"

                                showDialog(file.absolutePath, vehicle,price,data_vehicle)

                                /*when (from) {
                                    "ktp", "selfie" -> {
                                        findNavController().navigate(
                                            R.id.action_fragCamera2_to_fragPostCapture,
                                            bundleOf(
                                                "from" to from,
                                                "filepath" to file.absolutePath
                                            )
                                        )
                                    }
                                    else -> {
                                        setFragmentResult(
                                            "fragmentResult",
                                            bundleOf(
                                                "from" to "camera",
                                                "filepath" to file.absolutePath
                                            )
                                        )
                                        findNavController().popBackStack()
                                    }
                                }*/
                            }
                        } ?: run {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                requireActivity(),
                                "Gagal Menyimpan gambar",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: FileNotFoundException) {
                    Log.d(TAG, "File not found: ${e.message}")
                } catch (e: IOException) {
                    Log.d(TAG, "Error accessing file: ${e.message}")
                }

                safeToTakePicture = true
            }
        }
    }

    private fun convertToHeart(src: Bitmap): Bitmap? {
        return BitmapUtils.getCroppedBitmap(src, getHeartPath(src))
    }

    private fun getHeartPath(src: Bitmap): Path? {
        return resizePath(
            PathParser.createPathFromPathData(getString(R.string.heart3)),
            src.width.toFloat(), src.height.toFloat()
        )
    }

    fun resizePath(path: Path?, width: Float, height: Float): Path? {
        val bounds = RectF(0f, 0f, width, height)
        val resizedPath = Path(path)
        val src = RectF()
        resizedPath.computeBounds(src, true)
        val resizeMatrix = Matrix()
        resizeMatrix.setRectToRect(src, bounds, Matrix.ScaleToFit.CENTER)
        resizedPath.transform(resizeMatrix)
        return resizedPath
    }

    private fun playShutterSound() {
        val sound = MediaActionSound()
        sound.play(MediaActionSound.SHUTTER_CLICK)
    }

    fun rotateImage(source: Bitmap, angle: Float, flipX: Boolean = false): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(angle)
        if (flipX) matrix.postScale(-1f, 1f, source.width / 2f, source.height / 2f)
        return Bitmap.createBitmap(
            source, 0, 0, source.width, source.height,
            matrix, true
        )
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun toogleTorch() {
        /*if (torchOn){
            imagebutton_flash?.setImageResource(
                R.drawable.ic_flash_on
            )
            cameraManager?.setTorchMode(mCameraIds,true)
            torchOn = false
        }else{
            imagebutton_flash?.setImageResource(
                R.drawable.ic_flash_off
            )
            cameraManager?.setTorchMode(mCameraIds,false)
            torchOn = true
        }*/
    }

    private fun toogleFlash(state: String = "") {
        val params: Camera.Parameters? = camera?.parameters
        if (state == "on") {
            params?.flashMode = Camera.Parameters.FLASH_MODE_ON
            camera?.parameters = params
        } else if (state == "off") {
            params?.flashMode = Camera.Parameters.FLASH_MODE_OFF
            camera?.parameters = params
        }
        //just toogle
        else {
            if (params?.flashMode == Camera.Parameters.FLASH_MODE_ON) {
                params.flashMode = Camera.Parameters.FLASH_MODE_OFF
                camera?.parameters = params
            } else if (params?.flashMode == Camera.Parameters.FLASH_MODE_OFF) {
                params.flashMode = Camera.Parameters.FLASH_MODE_ON
                camera?.parameters = params
            }
        }

        params?.let { checkFlash(it) }
    }

    private fun checkFlash(params: Camera.Parameters) {
        if (params.flashMode == Camera.Parameters.FLASH_MODE_ON) {
            imagebutton_flash?.setImageResource(
                R.drawable.ic_flash_on
            )
        } else if (params.flashMode == Camera.Parameters.FLASH_MODE_OFF) {
            imagebutton_flash?.setImageResource(
                R.drawable.ic_flash_off
            )
        }
    }

    private fun showDialog(
        vehicle_img: String,
        vehicle: String,
        price: String,
        data_vehicle: VehicleSijuruParkingTypeDetai?
    ) {
        findNavController().navigate(
            R.id.action_mainMenuFragment_to_dialogInputVehicle,
            bundleOf(
                "vehicle_img" to vehicle_img,
                "vehicle" to vehicle,
                "price_base" to price,
                "data_vehicle" to data_vehicle
            )
        )
    }

    override fun OnClickItemDetailList(id: Int?, name: String?) {
        when (id) {
            1 -> {
                findNavController().navigate(R.id.action_mainMenuFragment_to_historyFragment)
            }
            2 -> {
                AlertDialog.Builder(requireActivity())
                    .setMessage("Kamu yakin mau keluar dari aplikasi?")
                    .setPositiveButton("Iya") { dialog, which ->
                        //clear session and navigate to login
                        sessionManager.logoutUser()
                        findNavController().navigate(R.id.action_mainMenuFragment_to_loginFragment)
                    }
                    .setNegativeButton("Batal") { dialog, which -> }
                    .show()
            }
        }
    }

    override fun OnClickItemListButton(data: VehicleSijuruParkingTypeDetai) {
        Log.e(TAG, "OnClickItemListButton: $data")
        takePicture(data)
    }

    private val barcodeLauncher = registerForActivityResult(
        ScanContract()
    ) { result: ScanIntentResult ->
        if (result.contents == null) {
            Toast.makeText(requireActivity(), "Cancelled", Toast.LENGTH_LONG).show()
        } else {
            updateRecordVehicle(result.contents)
        }
    }

    private fun totalPrice(
        priceBase: String,
        priceIncrementPrice: String,
        priceMaxPrice: String,
        priceIncrement: String
    ): Int {
        var harga_dasar = priceBase.toInt()
        val harga_tambah = priceIncrementPrice.toInt()
        val harga_max = priceMaxPrice.toInt()
        val waktu = priceIncrement.toInt()

        arguments?.run {
            val i1 = getString("first_time")
            val i2 = getString("end_time")

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss a")
            val dateParsed = sdf.parse(i1)
            val dateParsed2 = sdf.parse(i2)
            val formatTime = SimpleDateFormat("hh:mm:ss a", Locale.US)
            val start_time = formatTime.format(dateParsed)
            val end_time = formatTime.format(dateParsed2)
            val range = formatTime.parse(end_time).time - formatTime.parse(start_time).time
            val total_range_time = (range / 60000).toInt()

            val b = if (waktu != 0) total_range_time.div(waktu) else 0

            //jika total_range_time > 1
            if (b > 0) {
                for (i in 1..b) {
                    if (harga_dasar >= harga_max) {
                        harga_dasar = harga_max
                        break
                    } else {
                        harga_dasar += harga_tambah
                    }
                }
            } else {
                harga_dasar
            }
        }
        return harga_dasar
    }

    private fun updateRecordVehicle(text: String?) {
        fragmentHelper.showLoading()
        uuid = text
        viewModel.getHistoryVehicleById(text,this)
    }

    override fun onFailed(message: String) {
        super.onFailed(message)
        fragmentHelper.dismissLoading()
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onSuccessGetHistoryById(value: BaseResponseObject<ResponseVehicleQRCodeItem>) {
        super.onSuccessGetHistoryById(value)
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss a", Locale.getDefault())
            val currentDateandTime: String = sdf.format(Date())
            val data = value.responseData
            parkingFee =  totalPrice(
                data.parking_type_detail.filter { it.type == data.vehicle_type }.get(0).price_base,
                data.parking_type_detail.filter { it.type == data.vehicle_type }.get(0).price_increment_price,
                data.parking_type_detail.filter { it.type == data.vehicle_type }.get(0).price_max_price,
                data.parking_type_detail.filter { it.type == data.vehicle_type }.get(0).price_increment
            ).toString()
            viewModel.updateVehicleRecord(
                uuid, BodyVehicle(
                    mutableListOf(
                        Vehicle(
                            null,
                            null,
                            currentDateandTime,
                            null,
                            null,
                            null,
                            sessionManager.getOperatorId(),
                            sessionManager.getOperatorShift(),
                            null,
                            parkingFee,
                            null,
                            null,
                            null,
                            null
                        )
                    )
                ), this
            )
        } catch (e: Exception) {
            fragmentHelper.dismissLoading()
            Toast.makeText(requireActivity(), e.message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSuccessUpdateRecord(value: BaseResponseObject<Vehicle>) {
        super.onSuccessUpdateRecord(value)
        fragmentHelper.dismissLoading()
        Log.e(ControlsProviderService.TAG, "onSuccessUpdateRecord: $value")
        when (value.responseCode) {
            1000 -> {
                findNavController().navigate(
                    R.id.action_mainMenuFragment_to_receiptFragment,
                    bundleOf(
                        "id" to value.responseData._id,
                        "location" to sessionManager.getOperatorLocation(),
                        "plate" to "${value.responseData.vehicle_plat_front}-${value.responseData.vehicle_plat_middle}-${value.responseData.vehicle_plat_back}",
                        "operator_name" to sessionManager.getOperatorName(),
                        "vehicle_name" to value.responseData.vehicle_type,
                        "phone_number" to sessionManager.getOperatorPhone(),
                        "first_time" to value.responseData.vehicle_start_time_record,
                        "end_time" to value.responseData.vehicle_end_time_record,
                        "parking_fee" to parkingFee
                    )
                )
            }
            else -> {
                Toast.makeText(
                    requireActivity(),
                    value.responseDescription,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }
}