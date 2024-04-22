package com.sijuru.receipt

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.kioser.receipt.product.AdapterAvailablePrinter
import com.sijuru.PrinterListener
import com.sijuru.R
import com.sijuru.core.data.local.entity.*
import com.sijuru.core.data.response.BaseResponseObject
import com.sijuru.core.utils.FragmentHelper
import com.sijuru.core.utils.PrinterCommands
import com.sijuru.core.utils.SessionManager
import com.sijuru.core.utils.Utils
import com.sijuru.login.MainListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.OutputStream
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
@SuppressLint("MissingPermission")
class ReceiptFragment : Fragment(), Runnable, MainListener {

    companion object {
        fun newInstance() = ReceiptFragment()
        const val FONT_NORMAL = 0
        const val FONT_BOLD = 1
        const val FONT_BOLD_MEDIUM = 2
        const val FONT_BOLD_LARGE = 3

        const val ALIGN_LEFT = 0
        const val ALIGN_CENTER = 1
        const val ALIGN_RIGHT = 2
    }

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var viewModel: ReceiptViewModel

    @Inject
    lateinit var fragmentHelper: FragmentHelper

    private var textview_location: TextView? = null
    private var textview_ticket_number: TextView? = null
    private var textview_plate: TextView? = null
    private var textview_operator_name: TextView? = null
    private var textview_vehicle_name: TextView? = null
    private var textview_phone_number: TextView? = null
    private var textview_first_time: TextView? = null
    private var textview_end_time: TextView? = null
    private var textview_parking_fee: TextView? = null
    private var textview_ticket_number_value: TextView? = null
    private var textview_plate_value: TextView? = null
    private var textview_operator_name_value: TextView? = null
    private var textview_vehicle_name_value: TextView? = null
    private var textview_phone_number_value: TextView? = null
    private var textview_first_time_value: TextView? = null
    private var textview_end_time_value: TextView? = null
    private var textview_parking_fee_value: TextView? = null
    private var imageview_logo: ImageView? = null
    private var textview_link: TextView? = null
    private var button_print: Button? = null
    private var button_cancel: Button? = null

    private var location: String? = null
    private var type: String? = null
    private var address: String? = null
    private var ticket_number: String? = null
    private var plate: String? = null
    private var operator_name: String? = null
    private var vehicle_name: String? = null
    private var phone_number: String? = null
    private var first_time: String? = null
    private var end_time: String? = null
    private var parking_fee: String? = null
    private var data_vehicle: VehicleSijuruParkingTypeDetai? = null

    private val LOCATION_PERMISSION1 = Manifest.permission.ACCESS_COARSE_LOCATION
    private val LOCATION_PERMISSION2 = Manifest.permission.ACCESS_FINE_LOCATION
    private val applicationUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var mBluetoothSocket: BluetoothSocket? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mBluetoothDevice: BluetoothDevice? = null
    private var pairingStatus = ""
    private var printIsConnected = false
    private var list : MutableList<Printer> = ArrayList()
    private val adapterPrinter by lazy { AdapterAvailablePrinter() }

    private var dialog : AlertDialog? = null
    private var device : BluetoothDevice?= null
    var receiver: BroadcastReceiver? = null
    var recyclerview_list_printer : RecyclerView? = null
    var button : Button? = null
    var deviceName:String=""
    var printingThread: Thread? = null
    var mBluetoothConnectThread: Thread? = null
    var onProgressPrint = false
    var isButton = false
    var sizeKertas : Int = 32


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        receiver = object : BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                val action = intent?.action

                if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                    //discovery starts, we can show progress dialog or perform other tasks
                    Log.d(TAG,"Sedang Mencari Perangkat")
                }
                else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    //discovery finishes, dismis progress dialog
                    Log.d(TAG,"Pencarian Selesai")
                }
                else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    //bluetooth device found
                    device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        val printer = Printer(mac = it.address, nama_printer = it.name)
                        val deviceBTMajorClass = it.bluetoothClass.majorDeviceClass
                        if(printer.nama_printer.isNullOrBlank()) printer.nama_printer = printer.mac

                        if(list?.contains(printer)==false) {
                            Log.d("Anjay","${it.name}")
                            printer.nama_printer
                            list?.add(printer)
                            adapterPrinter?.notifyDataSetChanged()
                            recyclerview_list_printer?.scrollToPosition(list?.size?.minus(1)?:0)
                        }
                    }
                }
            }
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND)
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        activity?.registerReceiver(receiver, intentFilter)

//        ListPairedDevices()
        mBluetoothAdapter?.startDiscovery()
    }

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_receipt, container, false)
        receiver = object : BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                val action = intent?.action

                if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                    //discovery starts, we can show progress dialog or perform other tasks
                    Log.d(TAG,"Sedang Mencari Perangkat")
                }
                else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    //discovery finishes, dismis progress dialog
                    Log.d(TAG,"Pencarian Selesai")
                }
                else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    //bluetooth device found
                    device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        val printer = Printer(mac = it.address, nama_printer = it.name)
                        val deviceBTMajorClass = it.bluetoothClass.majorDeviceClass
                        if(printer.nama_printer.isNullOrBlank()) printer.nama_printer = printer.mac

                        if(list?.contains(printer)==false) {
                            Log.d("Anjay","${it.name}")
                            printer.nama_printer
                            list?.add(printer)
                            adapterPrinter?.notifyDataSetChanged()
                            recyclerview_list_printer?.scrollToPosition(list?.size?.minus(1)?:0)
                        }
                    }
                }
            }
        }
        textview_location = view.findViewById(R.id.textview_location)
        textview_ticket_number = view.findViewById(R.id.textview_ticket_number)
        textview_plate = view.findViewById(R.id.textview_plate)
        textview_operator_name = view.findViewById(R.id.textview_operator_name)
        textview_vehicle_name = view.findViewById(R.id.textview_vehicle_name)
        textview_phone_number = view.findViewById(R.id.textview_phone_number)
        textview_first_time = view.findViewById(R.id.textview_first_time)
        textview_end_time = view.findViewById(R.id.textview_end_time)
        textview_parking_fee = view.findViewById(R.id.textview_parking_fee)
        textview_ticket_number_value = view.findViewById(R.id.textview_ticket_number_value)
        textview_plate_value = view.findViewById(R.id.textview_plate_value)
        textview_operator_name_value = view.findViewById(R.id.textview_operator_name_value)
        textview_vehicle_name_value = view.findViewById(R.id.textview_vehicle_name_value)
        textview_phone_number_value = view.findViewById(R.id.textview_phone_number_value)
        textview_first_time_value = view.findViewById(R.id.textview_first_time_value)
        textview_end_time_value = view.findViewById(R.id.textview_end_time_value)
        textview_parking_fee_value = view.findViewById(R.id.textview_parking_fee_value)
        imageview_logo = view.findViewById(R.id.imageview_logo)
        textview_link = view.findViewById(R.id.textview_link)
        button_print = view.findViewById(R.id.button_print)
        button_cancel = view.findViewById(R.id.button_cancel)

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        arguments?.run {
            location = getString("location")?:""
            type = getString("type")?:""
            address = getString("address")?:""
            ticket_number = getString("ticket_number")?:""
            plate = getString("plate")?:""
            operator_name = getString("operator_name")?:""
            vehicle_name = getString("vehicle_name")?:""
            phone_number = getString("phone_number")?:""
            first_time = getString("first_time")?:""
            end_time = getString("end_time")?:""
            parking_fee = getString("parking_fee")?:""
            data_vehicle = getParcelable("data_vehicle")
        }

        val register = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
            if (result.resultCode == 2){

            }
        }

        textview_location?.text = location
        textview_ticket_number_value?.text = ticket_number
        textview_plate_value?.text = if (type=="retribution") address else plate
        textview_operator_name_value?.text = operator_name
        textview_vehicle_name_value?.text = vehicle_name
        textview_phone_number_value?.text = phone_number
        textview_first_time_value?.text = first_time
        textview_end_time_value?.text = end_time
        textview_parking_fee_value?.text = fragmentHelper.rupiah((parking_fee?:"0").toInt())

        button_print?.setOnClickListener {
            //requestBluetoothPermission()
            if(!isActiveBluetooth()){
                register.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            }
            else if(pairingStatus == "pairing"){
                Toast.makeText(requireContext(),"Sedang menghubungkan, Silakan tunggu sejenak",Toast.LENGTH_LONG).show()
            }
            else {
                try {
                    if(mBluetoothDevice==null || printIsConnected==false || mBluetoothDevice?.bondState!=BluetoothDevice.BOND_BONDED || mBluetoothSocket?.isConnected == false) {
                        if(hasLocationPermission(requireActivity())) {
                            if (checkGPS()){
                                if (sessionManager.getPrinter().equals("null")){
                                    showAvailablePrinter()
                                }
                                else{
                                    Log.e(TAG, "cek : getPrinter ${sessionManager.getPrinter().equals("null")}") //true
                                    Log.e(TAG, "cek : getPrinter ${sessionManager.getPrinter()}")
                                    Log.e(TAG, "cek : getPrinter ${sessionManager.getPrinter() == list.filter { it.mac == sessionManager.getPrinter() }.get(0).mac?:""}")
                                    if (!sessionManager.getPrinter().equals("null") && sessionManager.getPrinter() == list.filter { it.mac == sessionManager.getPrinter() }.get(0).mac?:""){
                                        try {
                                            if(mBluetoothDevice==null || mBluetoothDevice?.address!=list.get(0).mac || mBluetoothDevice?.bondState!=BluetoothDevice.BOND_BONDED || printIsConnected==false || mBluetoothSocket?.isConnected == false) {
                                                mBluetoothDevice = mBluetoothAdapter?.getRemoteDevice(list.get(0).mac)
                                                deviceName = mBluetoothDevice?.name ?: ""
                                                if (mBluetoothDevice?.bondState == BluetoothDevice.BOND_NONE) {
                                                    Toast.makeText(requireActivity(), "pairing", Toast.LENGTH_SHORT).show()
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                                        mBluetoothDevice?.createBond()
                                                    }
                                                } else if (mBluetoothDevice?.bondState == BluetoothDevice.BOND_BONDING) {
                                                    Toast.makeText(requireActivity(), "Selesaikan Pairing Device", Toast.LENGTH_SHORT).show()
                                                } else if (mBluetoothDevice?.bondState == BluetoothDevice.BOND_BONDED){
                                                    threadCetak()
                                                }
                                                else {
                                                    Toast.makeText(requireContext(),"Perangkat Tidak Terhubung,Pastikan perangkat bluetooth anda sedang kondisi aktif",Toast.LENGTH_LONG).show()
                                                }
                                            }
                                            else if (mBluetoothDevice?.bondState == BluetoothDevice.BOND_BONDED){
                                                threadCetak()
                                            }

                                        } catch (e: InterruptedException) {
                                            Log.e(TAG, "onCreateView dalam 1: ${e}")
                                            e.printStackTrace()
                                        } catch (e: Exception) {
                                            Log.e(TAG, "onCreateView dalam 2: ${e}")
                                            e.printStackTrace()
                                        }
                                    }
                                    else {
                                        Log.e(TAG, "getPrinter showAvailablePrinter")
                                        showAvailablePrinter()
                                    }
                                }
                            }else{
                                Toast.makeText(requireContext(),"Akses Lokasi , Aktifkan lokasi terlebih dahulu",Toast.LENGTH_LONG).show()
                            }
                        }
                        else {
                            AlertDialog.Builder(requireActivity())
                                .setMessage("Akses lokasi dibutuhkan untuk mencari perangkat bluetooth, Tambah perangkat baru ?")
                                .setPositiveButton("Iya") { dialog, which ->
                                    requestLocationPermission(this)
                                }
                                .setNegativeButton("Tidak") { dialog, which -> }
                                .show()
                        }
                    }
                    else {
                        Log.e(TAG, "cek : cetak")
                        findNavController().navigate(R.id.action_receiptFragment_to_mainMenuFragment)
                        threadCetak2()
                    }
                }
                catch (e: InterruptedException) {
                    e.printStackTrace()
                    Log.e(TAG, "onCreateView dalam 3: ${e}")
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e(TAG, "onCreateView dalam 4: ${e}")
                    showAvailablePrinter()
                }
            }
        }

        button_cancel?.setOnClickListener {
            findNavController().popBackStack()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (sessionManager.getParkingType().equals("progressive")) {
            textview_end_time?.visibility = View.VISIBLE
            textview_end_time_value?.visibility = View.VISIBLE
            //totalPrice()
        }else if (sessionManager.getParkingType().equals("flat")) {
            textview_end_time?.visibility = View.GONE
            textview_end_time_value?.visibility = View.GONE
        } else {
            textview_end_time?.visibility = View.GONE
            textview_end_time_value?.visibility = View.GONE
            textview_plate?.text = "Alamat"
            textview_vehicle_name?.text = "Jenis"
            textview_first_time?.text = "Waktu"
            textview_parking_fee?.text = "Tarif Retribusi"
        }
    }

    private fun totalPrice(): Int {
        var harga_dasar = sessionManager.getPriceBase().replace("[","").replace("]","").split(",").get(0).toInt()
        var harga_tambah = sessionManager.getPriceIncrement_price().replace("[","").replace("]","").split(",").get(0).toInt()
        var harga_max = sessionManager.getPriceMaxPrice().replace("[","").replace("]","").split(",").get(0).toInt()
        var waktu = sessionManager.getPriceIncrement().replace("[","").replace("]","").split(",").get(0).toInt()

        try {
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

                val b = if (waktu!=0) total_range_time.div(waktu) else 0
                println("hasil : $b")

                //jika total_range_time > 1
                if (b>0){
                    for (i in 1..b){
                        if (harga_dasar>=harga_max) {
                            harga_dasar = harga_max
                            println("hasil harga 1 = $harga_dasar")
                            break
                        }
                        else {
                            harga_dasar += harga_tambah
                            println("hasil harga 2 = $harga_dasar")
                        }
                    }
                }else{
                    harga_dasar = harga_dasar
                }

                println("hasil harga = $harga_dasar")
            }
        } catch (e: Exception) {
        }
        return harga_dasar
    }

    private fun threadCetak2() {
        if(mBluetoothSocket?.isConnected == true){
            printingThread = Thread { cetak() }
            printingThread?.start()
            printingThread?.join()
        }
        else {
            Toast.makeText(requireContext(),"Perangkat Tidak Terhubung, Pastikan perangkat bluetooth anda sedang kondisi aktif",Toast.LENGTH_LONG).show()
        }
    }

    private fun threadCetak() {
        //thread untuk koneksi
        mBluetoothConnectThread = Thread(this)
        mBluetoothConnectThread?.start()
        mBluetoothConnectThread?.join()

        //thread untuk cetak
        //hanya melakukan printing jika thread berhasil konek
        if(mBluetoothSocket?.isConnected == true){
            printingThread = Thread { cetak() }
            printingThread?.start()
            printingThread?.join()
        }
        else {
            Toast.makeText(requireContext(),"Perangkat Tidak Terhubung, Pastikan perangkat bluetooth anda sedang kondisi aktif",Toast.LENGTH_LONG).show()
        }
    }

    private fun showAvailablePrinter() {
        receiver = object : BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                val action = intent?.action

                if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                    //discovery starts, we can show progress dialog or perform other tasks
                    Log.d(TAG,"Sedang Mencari Perangkat")
                }
                else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    //discovery finishes, dismis progress dialog
                    Log.d(TAG,"Pencarian Selesai")
                }
                else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    //bluetooth device found
                    device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        val printer = Printer(mac = it.address, nama_printer = it.name)
                        val deviceBTMajorClass = it.bluetoothClass.majorDeviceClass
                        if(printer.nama_printer.isNullOrBlank()) printer.nama_printer = printer.mac

                        if(list?.contains(printer)==false) {
                            Log.d("Anjay","${it.name}")
                            printer.nama_printer
                            list?.add(printer)
                            adapterPrinter?.notifyDataSetChanged()
                            recyclerview_list_printer?.scrollToPosition(list?.size?.minus(1)?:0)
                        }
                    }
                }
            }
        }

        dialog = AlertDialog.Builder(requireActivity())
            .setView(R.layout.view_available_printer)
            .show()
        recyclerview_list_printer = dialog?.findViewById(R.id.recyclerview_list_printer)
        button = dialog?.findViewById(R.id.button)

        recyclerview_list_printer?.apply {
            setHasFixedSize(true)
            adapter = adapterPrinter.also { it.setAdapter(list) }
        }

        adapterPrinter.setOnPrintClickListener(object : PrinterListener {
            override fun printClick(printer: Printer) {
                Log.e(TAG, "printClick : ${printer.mac}")

                try {
                    if(mBluetoothDevice==null || mBluetoothDevice?.address!=printer.mac || mBluetoothDevice?.bondState!=BluetoothDevice.BOND_BONDED || printIsConnected==false || mBluetoothSocket?.isConnected == false) {
                        mBluetoothDevice = mBluetoothAdapter?.getRemoteDevice(printer.mac)
                        deviceName = mBluetoothDevice?.name ?: ""
                        if (mBluetoothDevice?.bondState == BluetoothDevice.BOND_NONE) {
                            Toast.makeText(requireActivity(), "pairing", Toast.LENGTH_SHORT).show()
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                mBluetoothDevice?.createBond()
                            }
                        } else if (mBluetoothDevice?.bondState == BluetoothDevice.BOND_BONDING) {
                            Toast.makeText(requireActivity(), "Selesaikan Pairing Device", Toast.LENGTH_SHORT).show()
                        } else if (mBluetoothDevice?.bondState == BluetoothDevice.BOND_BONDED){
                            sessionManager.setPrinter(printer.mac.toString())
                            threadCetak()
                        }
                        else {
                            Toast.makeText(requireContext(),"Perangkat Tidak Terhubung,Pastikan perangkat bluetooth anda sedang kondisi aktif",Toast.LENGTH_LONG).show()
                        }
                    }
                    else if (mBluetoothDevice?.bondState == BluetoothDevice.BOND_BONDED){
                        sessionManager.setPrinter(printer.mac.toString())
                        threadCetak()
                    }

                } catch (e: InterruptedException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })

        if (!arguments?.getString("from").toString().equals("history")){
            button?.visibility = View.VISIBLE
        }else{
            button?.visibility = View.GONE
        }
        button?.setOnClickListener {
            isButton = true
            if (!arguments?.getString("from").toString().equals("history")){
                fragmentHelper.showLoading()
                if (!sessionManager.getParkingType().equals("progressive")) {
                    viewModel.postVehicleRecord(
                        BodyVehicle(
                            mutableListOf(
                                Vehicle(
                                    "",
                                    null,
                                    first_time!!,
                                    if (type=="retribution") null else plate?.split("-")?.last(),
                                    if (type=="retribution") address else plate?.split("-")?.first(),
                                    if (type=="retribution") null else plate?.split("-")?.get(plate?.split("-")?.size!!.div(3)),
                                    sessionManager.getOperatorId(),
                                    sessionManager.getOperatorShift(),
                                    first_time!!,
                                    parking_fee!!,
                                    vehicle_name!!,
                                    "",
                                    "",
                                    null
                                )
                            )
                        ), this)
                }
            }
        }
    }

    private fun requestBluetoothPermission() {
        if(!isActiveBluetooth()){
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(intent,2)
        }
        else if(pairingStatus == "pairing"){
            Toast.makeText(requireContext(),"Sedang menghubungkan, Silakan tunggu sejenak",Toast.LENGTH_LONG).show()
        }
        else {

        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    fun isActiveBluetooth() : Boolean{
        if (mBluetoothAdapter == null) {
            Toast.makeText(requireContext(),"Perangkat Ini tidak memiliki bluetooth",Toast.LENGTH_LONG).show()
            return false
        }

        if (mBluetoothAdapter?.isEnabled == false) {
            Toast.makeText(requireContext(),"Bluetooth belum diaktifkan",Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }

    fun hasLocationPermission(activity: Activity): Boolean {
        return ContextCompat.checkSelfPermission(activity, LOCATION_PERMISSION1) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(activity, LOCATION_PERMISSION2) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkGPS(): Boolean {
        val lm = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gps_enable = false
        try {
            gps_enable = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }catch (e:Exception){}
        return gps_enable
    }

    fun requestLocationPermission(fragment: Fragment) {
        fragment.requestPermissions(arrayOf(LOCATION_PERMISSION1, LOCATION_PERMISSION2), 101)
    }

/*
    override fun printClick(printer: Printer) {
        Log.e(TAG, "printClick: $printer")
        try {
            if(mBluetoothDevice==null || mBluetoothDevice?.address!=printer.mac || mBluetoothDevice?.bondState!=BluetoothDevice.BOND_BONDED || printIsConnected==false || mBluetoothSocket?.isConnected == false) {
                mBluetoothDevice = mBluetoothAdapter?.getRemoteDevice(printer.mac)
                deviceName = mBluetoothDevice?.name ?: ""
                if (mBluetoothDevice?.bondState == BluetoothDevice.BOND_NONE) {
                    Toast.makeText(requireActivity(), "pairing", Toast.LENGTH_SHORT).show()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        mBluetoothDevice?.createBond()
                    }
                } else if (mBluetoothDevice?.bondState == BluetoothDevice.BOND_BONDING) {
                    Toast.makeText(requireActivity(), "Selesaikan Pairing Device", Toast.LENGTH_SHORT).show()
                } else if (mBluetoothDevice?.bondState == BluetoothDevice.BOND_BONDED){
                    threadCetak()
                }
                else {
                    Toast.makeText(requireContext(),"Perangkat Tidak Terhubung,Pastikan perangkat bluetooth anda sedang kondisi aktif",Toast.LENGTH_LONG).show()
                }
            }
            else if (mBluetoothDevice?.bondState == BluetoothDevice.BOND_BONDED){
                threadCetak()
            }

        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }*/

    override fun run() {
        try {
            mBluetoothSocket = mBluetoothDevice?.createRfcommSocketToServiceRecord(applicationUUID)
            mBluetoothAdapter?.cancelDiscovery()
            mBluetoothSocket?.connect()
            onProgressPrint = true
        } catch (eConnectException: IOException) {
            mBluetoothSocket?.let { closeSocket(it) }
            return
        }
    }

    private fun closeSocket(nOpenSocket: BluetoothSocket) {
        try {
            nOpenSocket.close()
            onProgressPrint = false
            try {
                // To avoid eating up all the device's CPU when trying to perform multiple reconnects
                Thread.sleep(1000)
            } catch (ex2: InterruptedException) {
            }
        } catch (ex: IOException) {
            Log.d(TAG, "CouldNotCloseSocket")
        }
    }

    private fun cetak() {
        try {
            dialog?.cancel()
            if(onProgressPrint) {
                if (!arguments?.getString("from").toString().equals("history")){
                    if (!sessionManager.getParkingType().equals("progressive")) {
                        viewModel.postVehicleRecord(
                            BodyVehicle(
                                mutableListOf(
                                    Vehicle(
                                        "",
                                        null,
                                        first_time!!,
                                        if (type=="retribution") null else plate?.split("-")?.last(),
                                        if (type=="retribution") address else plate?.split("-")?.first(),
                                        if (type=="retribution") null else plate?.split("-")?.get(plate?.split("-")?.size!!.div(3)),
                                        sessionManager.getOperatorId(),
                                        sessionManager.getOperatorShift(),
                                        first_time!!,
                                        parking_fee!!,
                                        vehicle_name!!,
                                        "",
                                        "",
                                        null
                                    )
                                )
                            ), this
                        )
                    }
                }
                onProgressPrint = false
                val outputStream = mBluetoothSocket?.outputStream// Notify printer it should be printed with given alignment:
                printheader(outputStream)
                outputStream?.write(PrinterCommands.FEED_LINE)
                lifecycleScope.launch {
                    delay(1000)
                    printCustom("${location?.uppercase(Locale.getDefault())}", FONT_BOLD, ALIGN_CENTER,outputStream)
                    outputStream?.write(PrinterCommands.FEED_LINE)
                    printCustom(garis(), FONT_BOLD, ALIGN_CENTER, outputStream)
                    outputStream?.write(PrinterCommands.FEED_LINE)
//                    printCustom(textKananKiri(textview_ticket_number?.text.toString(), "$ticket_number\n"), FONT_BOLD, ALIGN_CENTER,outputStream)
                    printCustom(textKananKiri(textview_plate?.text.toString(), "${if (type=="retribution") address else plate}\n"), FONT_BOLD, ALIGN_CENTER,outputStream)
                    printCustom(textKananKiri(textview_operator_name?.text.toString(), "$operator_name\n"), FONT_BOLD, ALIGN_CENTER,outputStream)
                    printCustom(textKananKiri(textview_vehicle_name?.text.toString(), "$vehicle_name\n"), FONT_BOLD, ALIGN_CENTER,outputStream)
                    printCustom(textKananKiri(textview_phone_number?.text.toString(), "$phone_number\n"), FONT_BOLD, ALIGN_CENTER,outputStream)
                    val totalLength = (textview_first_time?.text?.length ?: 0) + (first_time?.length ?: 0)
                    if (totalLength > 30) {
                        printCustom(textview_first_time?.text.toString(), FONT_BOLD, ALIGN_LEFT,outputStream)
                        outputStream?.write(PrinterCommands.FEED_LINE)
                        printCustom("$first_time\n", FONT_BOLD, ALIGN_RIGHT,outputStream)
                    }else {
                        printCustom(
                            textKananKiri(
                                textview_first_time?.text.toString(),
                                "$first_time\n"
                            ), FONT_BOLD, ALIGN_CENTER, outputStream
                        )
                    }
                    if (sessionManager.getParkingType().equals("progressive")) {
                        val totalLengthEnd = (textview_end_time?.text?.length ?: 0) + (end_time?.length ?: 0)
                        if (totalLengthEnd > 30) {
                            printCustom(textview_end_time?.text.toString(), FONT_BOLD, ALIGN_LEFT,outputStream)
                            outputStream?.write(PrinterCommands.FEED_LINE)
                            printCustom("$end_time\n", FONT_BOLD, ALIGN_RIGHT,outputStream)
                        }else {
                            printCustom(textKananKiri(textview_end_time?.text.toString(), "$end_time\n"), FONT_BOLD, ALIGN_CENTER,outputStream)
                        }
                    }
                    printCustom(textKananKiri(textview_parking_fee?.text.toString(), "Rp $parking_fee" ?: ""), FONT_BOLD, ALIGN_CENTER,outputStream)
                    outputStream?.write(PrinterCommands.FEED_LINE)
                    printCustom(garis(), FONT_BOLD, ALIGN_CENTER, outputStream)
                    outputStream?.write(PrinterCommands.FEED_LINE)
                    printCustom(
                        "Jangan meninggalkan barang berharga di kendaraan anda, segala bentuk kehilangan di luar tanggung jawab kami.",
                        FONT_BOLD,
                        ALIGN_CENTER,
                        outputStream
                    )
                    outputStream?.write(PrinterCommands.FEED_LINE)
                    outputStream?.write(PrinterCommands.FEED_LINE)
                    printCustom(
                        textview_link?.text.toString(),
                        FONT_BOLD,
                        ALIGN_CENTER,
                        outputStream
                    )
                    outputStream?.write(PrinterCommands.FEED_LINE)
                    outputStream?.write(PrinterCommands.FEED_LINE)
                    outputStream?.write(PrinterCommands.FEED_LINE)
                    outputStream?.write(PrinterCommands.FEED_LINE)

                    //close socket setelah selesai print, biar print dari awal sj
                    mBluetoothSocket?.let { closeSocket(it) }
                    findNavController().popBackStack()
                }
            }
        }
        catch (e: IOException) {
            printIsConnected = false
            Toast.makeText(requireContext(),"Perangkat Tidak Terhubung, Pastikan perangkat bluetooth anda sedang kondisi aktif",Toast.LENGTH_LONG).show()
        }
    }

    private fun printheader(outputStream: OutputStream?) {
//        val outputStream = mBluetoothSocket?.outputStream// Notify printer it should be printed with given alignment:
        val bmp = BitmapFactory.decodeResource(activity?.resources, R.drawable.logo_print)
        if (bmp != null) {
            val width = Math.round(0.5 * bmp.getWidth()).toInt()
            val height = Math.round(0.5 * bmp.getHeight()).toInt()

            val bmp2 = Bitmap.createScaledBitmap(bmp,width, height, false)
            //val command = Utils.decodeBitmap(pad(bmp2,100,0))
            val command = Utils.decodeBitmap(bmp2)
            outputStream?.write(PrinterCommands.ESC_ALIGN_CENTER)
            outputStream?.write(command)
        } else {
            Log.e("Print Photo error", "the file isn't exists")
        }
    }

    private fun printCustom(msg: String, size: Int, align: Int, outputStream: OutputStream?) {

        val bb4 = byteArrayOf(0x1B,0x21,0x00) // 0- normal size text
        val bb5 = byteArrayOf(0x1B, 0x4D, 1) // 1- only bold text
//        val bb2 = byteArrayOf(0x1B, 0x4D, 1) // 2- bold with medium text
//        val bb3 = byteArrayOf(0x1B, 0x4D, 1) // 3- bold with large text

//        OLD SETTING
        val cc = byteArrayOf(0x1B, 0x21, 0x03) // 0- normal size text
        val bb = byteArrayOf(0x1B, 0x21, 0x08) // 1- only bold text
        val bb2 = byteArrayOf(0x1B, 0x21, 0x20) // 2- bold with medium text
        val bb3 = byteArrayOf(0x1B, 0x21, 0x10) // 3- bold with large text

        try {
            when (size) {
                0 -> outputStream?.write(cc)
                1 -> outputStream?.write(bb)
                2 -> outputStream?.write(bb2)
                3 -> outputStream?.write(bb3)
                4 -> outputStream?.write(PrinterCommands.ESC_ITALIC)
                5 -> outputStream?.write(PrinterCommands.SELECT_FONT_A)
                6 -> outputStream?.write(bb4)
                7 -> outputStream?.write(bb5)
            }
            when (align) {
                0 ->  //left align
                    outputStream?.write(PrinterCommands.ESC_ALIGN_LEFT)
                1 ->  //center align
                    outputStream?.write(PrinterCommands.ESC_ALIGN_CENTER)
                2 ->  //right align
                    outputStream?.write(PrinterCommands.ESC_ALIGN_RIGHT)
            }
            outputStream?.write(msg.toByteArray(Charset.defaultCharset()))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    //menampilkan garis --------- (dash)
    private fun garis(): String {
        var garis1 = ""
        for (a in 0 until sizeKertas) {
            garis1 = "$garis1-"
        }
        return garis1
    }

    private fun leftRightAlign(str1: String, str2: String): String? {
        var ans = str1 + str2
        Log.e(TAG, "leftRightAlign: $ans.")
        if (ans.length < 31) {
            val n = 31 - str1.length + str2.length
            ans = str1 + String(CharArray(n)).replace("\u0000", " ") + str2
        }else{
            ans = "$str1 $str2"
        }
        return ans
    }

    private fun textKananKiri(text1: String, text2: String): String {
        val space = getDataSpace(text1.length, text2.length, sizeKertas)
        return "$text1$space$text2"
    }

    private fun getDataSpace(lengthKiri: Int, lengthKanan: Int, sizeKertas: Int): String {
        var hasil = ""
        var jumlah = 0
        val hasil_jumlah: Int
        jumlah = lengthKanan + lengthKiri
        hasil_jumlah = this.sizeKertas - jumlah
        for (a in 0 until hasil_jumlah) {
            hasil = "$hasil "
        }
        return hasil
    }

    override fun onSuccessRecord(value: BaseResponseObject<VehicleRecord>) {
        super.onSuccessRecord(value)
        Log.e(TAG, "onSuccessRecord: $value")
        if (isButton) {
            fragmentHelper.dismissLoading()
            dialog?.cancel()
            findNavController().popBackStack()
        }
        /*if (!arguments?.getString("from").toString().equals("history")) {
            if (!sessionManager.getParkingType().equals("progressive")) {
                fragmentHelper.dismissLoading()
                dialog?.cancel()
                findNavController().popBackStack()
            }
        }*/
    }

    override fun onFailed(message: String) {
        super.onFailed(message)
        Log.e(TAG, "onFailedRecord: $message")
    }

}