package com.sijuru.receipt_progressive

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.service.controls.ControlsProviderService.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.kioser.receipt.product.AdapterAvailablePrinter
import com.sijuru.PrinterListener
import com.sijuru.R
import com.sijuru.core.data.local.entity.BodyVehicle
import com.sijuru.core.data.local.entity.Printer
import com.sijuru.core.data.local.entity.Vehicle
import com.sijuru.core.data.local.entity.VehicleRecord
import com.sijuru.core.data.response.BaseResponseObject
import com.sijuru.core.utils.PrinterCommands
import com.sijuru.core.utils.SessionManager
import com.sijuru.core.utils.Utils
import com.sijuru.login.MainListener
import com.sijuru.receipt.ReceiptFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
@SuppressLint("MissingPermission")
class ProgressiveReceiptFragment : Fragment(), Runnable, MainListener  {


    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var viewModel: ProgressiveReceiptViewModel

    private var textview_location: TextView? = null
    private var textview_first_time: TextView? = null
    private var textview_info: TextView? = null
    private var textview_link: TextView? = null
    private var imageview_barcode: ImageView? = null
    private var button_print: Button? = null
    private var button_cancel: Button? = null

    private var id: String? = null
    private var location: String? = null
    private var plate: String? = null
    private var operator_name: String? = null
    private var vehicle_name: String? = null
    private var phone_number: String? = null
    private var first_time: String? = null
    private var end_time: String? = null
    private var parking_fee: String? = null
    private var bitmap: Bitmap? = null

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
    var deviceName:String=""
    var printingThread: Thread? = null
    var mBluetoothConnectThread: Thread? = null
    var onProgressPrint = false
    var sizeKertas : Int = 32

    //mendengarkan printer yang aktif
    /*var receiver = object : BroadcastReceiver(){

        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action

            intent?.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

            if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)){
                val state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)
                val prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR)

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    //kondisi berhasil pairing
                    pairingStatus = ""
                    threadCetak()
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDING){
                    //kondisi gagal pairing
                    pairingStatus = ""
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
                    //kondisi unpair
                    pairingStatus = ""
                }
                else if(state == BluetoothDevice.BOND_BONDING){
                    //kondisi pairing
                    pairingStatus = "pairing"
                }
            }
        }
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        receiver = object : BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                val action = intent?.action

                if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                    //discovery starts, we can show progress dialog or perform other tasks
                    Log.d(ContentValues.TAG,"Sedang Mencari Perangkat")
                }
                else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    //discovery finishes, dismis progress dialog
                    Log.d(ContentValues.TAG,"Pencarian Selesai")
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_progressive_receipt, container, false)
        textview_location = view.findViewById(R.id.textview_location)
        imageview_barcode = view.findViewById(R.id.imageview_barcode)
        textview_first_time = view.findViewById(R.id.textview_first_time)
        textview_info = view.findViewById(R.id.textview_info)
        textview_link = view.findViewById(R.id.textview_link)
        button_print = view.findViewById(R.id.button_print)
        button_cancel = view.findViewById(R.id.button_cancel)

        val register = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if (result.resultCode == 2){

            }
        }

        arguments?.run {
            id = getString("id")?:""
            location = getString("location")?:""
            phone_number = getString("phone_number")?:""
            plate = getString("plate")?:""
            operator_name = getString("operator_name")?:""
            vehicle_name = getString("vehicle_name")?:""
            phone_number = getString("phone_number")?:""
            first_time = getString("first_time")?:""
            end_time = getString("end_time")?:""
            parking_fee = getString("parking_fee")?:""
        }

        textview_location?.text = location
        textview_first_time?.text = "Waktu : $first_time"

        button_print?.setOnClickListener {
            if(!isActiveBluetooth()){
                register.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            }
            else if(pairingStatus == "pairing"){
                Toast.makeText(requireContext(),"Sedang menghubungkan, Silakan tunggu sejenak",
                    Toast.LENGTH_LONG).show()
            }
            else {
                try {
                    if(mBluetoothDevice==null || printIsConnected==false || mBluetoothDevice?.bondState!=BluetoothDevice.BOND_BONDED || mBluetoothSocket?.isConnected == false) {
                        if(hasLocationPermission(requireActivity())) {
                            if (checkGPS()){
                                if (sessionManager.getPrinter().equals("null")){
                                    showAvailablePrinter()
                                }else{
                                    Log.e(ContentValues.TAG, "cek : getPrinter ${sessionManager.getPrinter().equals("null")}") //true
                                    Log.e(ContentValues.TAG, "cek : getPrinter ${sessionManager.getPrinter()}")
                                    Log.e(ContentValues.TAG, "cek : getPrinter ${sessionManager.getPrinter() == list.filter { it.mac == sessionManager.getPrinter() }.get(0).mac?:""}")
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
                                                    Toast.makeText(requireContext(),"Perangkat Tidak Terhubung,Pastikan perangkat bluetooth anda sedang kondisi aktif",
                                                        Toast.LENGTH_LONG).show()
                                                }
                                            }
                                            else if (mBluetoothDevice?.bondState == BluetoothDevice.BOND_BONDED){
                                                threadCetak()
                                            }

                                        } catch (e: InterruptedException) {
                                            Log.e(ContentValues.TAG, "onCreateView dalam 1: ${e}")
                                            e.printStackTrace()
                                        } catch (e: Exception) {
                                            Log.e(ContentValues.TAG, "onCreateView dalam 2: ${e}")
                                            e.printStackTrace()
                                        }
                                    }
                                    else {
                                        Log.e(ContentValues.TAG, "getPrinter showAvailablePrinter")
                                        showAvailablePrinter()
                                    }
                                }
                            }else{
                                Toast.makeText(requireContext(),"Akses Lokasi , Aktifkan lokasi terlebih dahulu",
                                    Toast.LENGTH_LONG).show()
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
                        Log.e(ContentValues.TAG, "cek : cetak")
                        findNavController().navigate(R.id.action_receiptFragment_to_mainMenuFragment)
                        threadCetak2()
                    }
                }
                catch (e: InterruptedException) {
                    e.printStackTrace()
                    Log.e(ContentValues.TAG, "onCreateView dalam 3: ${e}")
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e(ContentValues.TAG, "onCreateView dalam 4: ${e}")
                    showAvailablePrinter()
                }
            }
        }

        button_cancel?.setOnClickListener {
            findNavController().popBackStack()
        }

        bitmap = generatedQRcode(id)
        imageview_barcode?.setImageBitmap(bitmap)

        return view
    }

    private fun generatedQRcode(text : String?): Bitmap? {
        val width = 500
        val height = 500
        val bitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888)
        val codeWriter = MultiFormatWriter()
        try {
            val bitmatrix = codeWriter.encode(text,BarcodeFormat.QR_CODE,width,height)
            for (x in 0 until width){
                for (y in 0 until height){
                    bitmap.setPixel(x,y,if (bitmatrix[x,y]) Color.BLACK else Color.WHITE)
                }
            }
        }catch (e: Exception){
            Log.e(TAG, "generatedQRcode: ${e.message}")
        }
        return bitmap
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
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
        dialog = AlertDialog.Builder(requireActivity())
            .setView(R.layout.view_available_printer)
            .show()
        recyclerview_list_printer = dialog?.findViewById(R.id.recyclerview_list_printer)

        recyclerview_list_printer?.apply {
            setHasFixedSize(true)
            adapter = adapterPrinter.also { it.setAdapter(list) }
        }

        adapterPrinter.setOnPrintClickListener(object : PrinterListener {
            override fun printClick(printer: Printer) {
                Log.e(ContentValues.TAG, "printClick : ${printer.mac}")

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
            Log.d(ContentValues.TAG, "CouldNotCloseSocket")
        }
    }

    private fun cetak() {
        try {
            dialog?.cancel()
            if(onProgressPrint) {
                /*viewModel.postVehicleRecord(
                    BodyVehicle(mutableListOf(
                        Vehicle("",
                        first_time!!,
                        plate?.split("-")?.last(),
                        plate?.split("-")?.first(),
                        plate?.split("-")?.get(plate?.split("-")?.size!!.div(3)),
                        sessionManager.getOperatorId(),
                        sessionManager.getOperatorShift(),
                        first_time!!,
                        parking_fee!!,
                        vehicle_name!!)
                    )
                    ),this)*/
                onProgressPrint = false
                val outputStream = mBluetoothSocket?.outputStream// Notify printer it should be printed with given alignment:
                printheader(outputStream,R.drawable.logo_print)
                outputStream?.write(PrinterCommands.FEED_LINE)
                lifecycleScope.launch {
                    delay(1000)
                    printCustom("${location?.uppercase(Locale.getDefault())}",
                        ReceiptFragment.FONT_BOLD,
                        ReceiptFragment.ALIGN_CENTER,outputStream)
                    outputStream?.write(PrinterCommands.FEED_LINE)
                    printCustom(garis(),
                        ReceiptFragment.FONT_BOLD,
                        ReceiptFragment.ALIGN_CENTER, outputStream)
                    outputStream?.write(PrinterCommands.FEED_LINE)
                    printImage(outputStream,bitmap)
                    outputStream?.write(PrinterCommands.FEED_LINE)
                    printCustom(textview_first_time?.text.toString(),
                        ReceiptFragment.FONT_BOLD,
                        ReceiptFragment.ALIGN_CENTER,outputStream)
                    outputStream?.write(PrinterCommands.FEED_LINE)
                    printCustom(garis(),
                        ReceiptFragment.FONT_BOLD,
                        ReceiptFragment.ALIGN_CENTER, outputStream)
                    outputStream?.write(PrinterCommands.FEED_LINE)
                    printCustom(
                        "Jangan meninggalkan barang berharga di kendaraan anda, segala bentuk kehilangan di luar tanggung jawab kami.",
                        ReceiptFragment.FONT_BOLD,
                        ReceiptFragment.ALIGN_CENTER,
                        outputStream
                    )
                    outputStream?.write(PrinterCommands.FEED_LINE)
                    outputStream?.write(PrinterCommands.FEED_LINE)
                    printCustom(
                        textview_link?.text.toString(),
                        ReceiptFragment.FONT_BOLD,
                        ReceiptFragment.ALIGN_CENTER,
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

    private fun printheader(outputStream: OutputStream?, image : Int) {
        val bmp = BitmapFactory.decodeResource(activity?.resources, image)
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

    private fun printImage(outputStream: OutputStream?, bmp: Bitmap?) {
        try {
            if (bmp != null) {
                val width = Math.round(0.5 * bmp.getWidth()).toInt()
                val height = Math.round(0.5 * bmp.getHeight()).toInt()

                val bmp2 = Bitmap.createScaledBitmap(bmp,width, height, false)
                val command = Utils.decodeBitmap(bmp2)
                outputStream?.write(PrinterCommands.ESC_ALIGN_CENTER)
                outputStream?.write(command)
            } else {
                Log.e("Print Photo error", "the file isn't exists")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("PrintTools", "the file isn't exists")
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
        Log.e(ContentValues.TAG, "leftRightAlign: $ans.")
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

    }

    override fun onDestroy() {
        super.onDestroy()
        receiver.let {  activity?.unregisterReceiver(it) }
    }

}