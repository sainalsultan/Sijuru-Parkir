package com.sijuru.scan_barcode

import android.content.pm.PackageManager
import android.os.Bundle
import android.service.controls.ControlsProviderService.TAG
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.sijuru.R
import com.sijuru.core.data.local.entity.BodyVehicle
import com.sijuru.core.data.local.entity.ResponseVehicleQRCodeItem
import com.sijuru.core.data.local.entity.Vehicle
import com.sijuru.core.data.local.entity.VehicleSijuruParkingTypeDetai
import com.sijuru.core.data.response.BaseResponseList
import com.sijuru.core.data.response.BaseResponseObject
import com.sijuru.core.utils.FragmentHelper
import com.sijuru.core.utils.SessionManager
import com.sijuru.login.MainListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ProgressiveScanBarcodeFragment : Fragment(), MainListener {

    companion object {
        fun newInstance() = ProgressiveScanBarcodeFragment()
    }

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var fragmentHelper: FragmentHelper

    @Inject
    lateinit var viewModel: ProgressiveScanBarcodeViewModel

    private var codescannerview: CodeScannerView? = null
    private var codescanner: CodeScanner? = null
    private var vehicle: String? = null
    private var type: String? = null
    private var uuid: String? = null
    private var parkingFee: String? = null

    private fun startScanning() {
        codescanner = CodeScanner(requireActivity(), codescannerview!!).apply {
            camera = CodeScanner.CAMERA_BACK
            formats = CodeScanner.ALL_FORMATS
            autoFocusMode = AutoFocusMode.CONTINUOUS
            scanMode = ScanMode.SINGLE
            isAutoFocusEnabled = true
            isFlashEnabled = false
        }

        codescanner?.decodeCallback = DecodeCallback {
            try {
                activity?.runOnUiThread {
                    if (it.text.isNotEmpty()) updateRecordVehicle(it.text)
                }
            } catch (e: Exception) {
                Log.e(TAG, "startScanning: ${e.message}")
            }
        }

        codescanner?.errorCallback = ErrorCallback {
            activity?.runOnUiThread {
                Toast.makeText(
                    requireActivity(),
                    "Camera initialization error ${it.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun totalPrice(
        priceBase: String,
        priceIncrementPrice: String,
        priceMaxPrice: String,
        priceIncrement: String
    ): Int {
        /*var harga_dasar =
            sessionManager.getPriceBase().replace("[", "").replace("]", "").split(",").get(0)
                .toInt()
        var harga_tambah =
            sessionManager.getPriceIncrement_price().replace("[", "").replace("]", "").split(",")
                .get(0).toInt()
        var harga_max =
            sessionManager.getPriceMaxPrice().replace("[", "").replace("]", "").split(",").get(0)
                .toInt()
        var waktu =
            sessionManager.getPriceIncrement().replace("[", "").replace("]", "").split(",").get(0)
                .toInt()*/

        var harga_dasar = priceBase.toInt()
        var harga_tambah = priceIncrementPrice.toInt()
        var harga_max = priceMaxPrice.toInt()
        var waktu = priceIncrement.toInt()

        arguments?.run {
            /*val i1 = "2022-04-05 10:00:00 AM" //"yyyy-MM-dd HH:mm:ss a"
            val i2 = "2022-04-05 10:11:00 AM" //"yyyy-MM-dd HH:mm:ss a"*/
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

            println("hasil range : $range")
            println("hasil dari total_range_time : $total_range_time")
            println("hasil waktu : $waktu")

            val b = if (waktu != 0) total_range_time.div(waktu) else 0
            println("hasil : $b")

            //jika total_range_time > 1
            if (b > 0) {
                for (i in 1..b) {
                    if (harga_dasar >= harga_max) {
                        harga_dasar = harga_max
                        println("hasil harga 1 = $harga_dasar")
                        break
                    } else {
                        harga_dasar += harga_tambah
                        println("hasil harga 2 = $harga_dasar")
                    }
                }
            } else {
                harga_dasar = harga_dasar
            }

            println("hasil harga = $harga_dasar")
        }
        return harga_dasar
    }

    private fun updateRecordVehicle(text: String?) {
        fragmentHelper.showLoading()
        uuid = text
        viewModel.getHistoryVehicleById(text,this)
        /*viewModel.updateVehicleRecord(
            text, BodyVehicle(
                mutableListOf(
                    Vehicle(
                        null,
                        currentDateandTime,
                        null,
                        null,
                        null,
                        sessionManager.getOperatorId(),
                        sessionManager.getOperatorShift(),
                        null,
                        totalPrice().toString(),
                        null,
                        null,
                        null,
                        null
                    )
                )
            ), this
        )*/
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_progressive_scan_barcode, container, false)
        codescannerview = view.findViewById(R.id.codescannerview)

        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.CAMERA),
                123
            )
        } else {
            startScanning()
        }

        codescannerview?.setOnClickListener {
            codescanner?.startPreview()
        }
        return view
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            123 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        requireActivity(),
                        "Camera permission granted",
                        Toast.LENGTH_SHORT
                    ).show()
                    startScanning()
                } else {
                    Toast.makeText(
                        requireActivity(),
                        "Camera permission denied",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (codescanner != null) {
            codescanner?.startPreview()
        }
    }

    override fun onPause() {
        super.onPause()
        if (codescanner != null) {
            codescanner?.releaseResources()
        }
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
        Log.e(TAG, "onSuccessUpdateRecord: $value")
        when (value.responseCode) {
            1000 -> {
                findNavController().navigate(
                    R.id.action_progressiveScanBarcodeFragment_to_receiptFragment,
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
                lifecycleScope.launch {
                    delay(1000)
                    if (codescanner != null) {
                        codescanner?.startPreview()
                    }
                }
            }
        }

    }

}