package com.sijuru.main_menu

import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sijuru.R
import com.sijuru.core.data.local.entity.BodyVehicle
import com.sijuru.core.data.local.entity.Vehicle
import com.sijuru.core.data.local.entity.VehicleRecord
import com.sijuru.core.data.local.entity.VehicleSijuruParkingTypeDetai
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
class DialogInputVehicle : BottomSheetDialogFragment(),TextWatcher, MainListener {

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var viewModel: MainMenuViewModel

    @Inject
    lateinit var fragmentHelper: FragmentHelper

    private var constraintlayout_input_vehicle: ConstraintLayout? = null
    private var edittext_first: EditText? = null
    private var edittext_middle: EditText? = null
    private var edittext_end: EditText? = null
    private var edittext_location: EditText? = null
    private var imageview_vehicle: ImageView? = null
    private var textview_info: TextView? = null
    private var button_cancel: Button? = null
    private var button_done: Button? = null
    private var data_vehicle: VehicleSijuruParkingTypeDetai? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog
            val bottomSheet =
                d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
//            BottomSheetBehavior.from<FrameLayout?>(bottomSheet!!).state = BottomSheetBehavior.STATE_EXPANDED
            val behavior = BottomSheetBehavior.from(bottomSheet!!)
            val layoutParams = bottomSheet.layoutParams
            val windowHeight = getWindowHeight()
            if (layoutParams != null) {
                layoutParams.height = windowHeight
            }
//            bottomSheet.layoutParams = layoutParams
            behavior.peekHeight = Resources.getSystem().displayMetrics.heightPixels
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true

        }
        return dialog
    }

    private fun getWindowHeight(): Int {
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        setFragmentResult("picture", bundleOf("from" to "dialog"))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_input_vehicle, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        constraintlayout_input_vehicle = view.findViewById(R.id.constraintlayout_input_vehicle)
        textview_info = view.findViewById(R.id.textview_info)
        imageview_vehicle = view.findViewById(R.id.imageview_vehicle)
        edittext_first = view.findViewById(R.id.edittext_first)
        edittext_middle = view.findViewById(R.id.edittext_middle)
        edittext_end = view.findViewById(R.id.edittext_end)
        edittext_location = view.findViewById(R.id.edittext_location)
        button_cancel = view.findViewById(R.id.button_cancel)
        button_done = view.findViewById(R.id.button_done)

        data_vehicle = arguments?.getParcelable("data_vehicle")
        Log.e(TAG, "onViewCreated: $data_vehicle")

        if(sessionManager.getParkingType().equals("retribution")){
            textview_info?.text = "Masukkan Lokasi"
            edittext_first?.visibility = View.GONE
            edittext_middle?.visibility = View.GONE
            edittext_end?.visibility = View.GONE
            edittext_location?.visibility = View.VISIBLE
        }
        else{
            edittext_first?.visibility = View.VISIBLE
            edittext_middle?.visibility = View.VISIBLE
            edittext_end?.visibility = View.VISIBLE
            edittext_location?.visibility = View.GONE
        }

        val imgPath = arguments?.getString("vehicle_img")
        activity?.applicationContext?.let {
            Glide.with(it)
                .load(imgPath)
                .placeholder(R.drawable.bg_rounded_orange)
                .into(imageview_vehicle!!)
        }

        edittext_first?.addTextChangedListener(this)
        edittext_middle?.addTextChangedListener(this)
        edittext_end?.addTextChangedListener(this)

        /*edittext_middle?.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                imm!!.hideSoftInputFromWindow(view.windowToken, 0)


                val imm2 = activity?.getSystemService(
                    Context.INPUT_METHOD_SERVICE
                ) as InputMethodManager?
                imm2!!.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
            }
        }*/

        button_cancel?.setOnClickListener {
            setFragmentResult("picture", bundleOf("from" to "dialog"))
            dismiss()
        }

        button_done?.setOnClickListener {
            fragmentHelper.showLoading()
            if(sessionManager.getParkingType().equals("retribution")){
                if (edittext_location?.text?.isEmpty() == true){
                    fragmentHelper.dismissLoading()
                    Toast.makeText(
                        requireContext(),
                        "Silakan masukkan alamat",
                        Toast.LENGTH_LONG
                    ).show()
                }
                else {
                    fragmentHelper.dismissLoading()
                    val address = "${edittext_location?.text}"
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss a", Locale.getDefault())
                    val currentDateandTime: String = sdf.format(Date())
                    arguments?.run {
                        findNavController().navigate(
                            R.id.action_dialogInputVehicle_to_receiptFragment,
                            bundleOf(
                                "type" to "retribution",
                                "location" to sessionManager.getOperatorLocation(),
                                "address" to address,
                                "operator_name" to sessionManager.getOperatorName(),
                                "vehicle_name" to data_vehicle?.type,
                                "phone_number" to sessionManager.getOperatorPhone(),
                                "first_time" to currentDateandTime,
                                "parking_fee" to data_vehicle?.price_base,
                                "data_vehicle" to data_vehicle
                            )
                        )
                    }
                }
            }
            else{
                if (edittext_first?.text?.isEmpty() == true ||
                    edittext_middle?.text?.isEmpty() == true ||
                    edittext_end?.text?.isEmpty() == true
                ) {
                    fragmentHelper.dismissLoading()
                    Toast.makeText(
                        requireContext(),
                        "Silakan masukkan plat kendaraan",
                        Toast.LENGTH_LONG
                    ).show()
                }
                else {
                    val plate = "${edittext_first?.text}-${edittext_middle?.text}-${edittext_end?.text}"
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss a", Locale.getDefault())
                    val currentDateandTime: String = sdf.format(Date())
                    arguments?.run {
                        Log.e(TAG, "onViewCreated: ${sessionManager.getParkingType()}")
                        if (sessionManager.getParkingType().equals("flat")) {
                            fragmentHelper.dismissLoading()
                            findNavController().navigate(
                                R.id.action_dialogInputVehicle_to_receiptFragment,
                                bundleOf(
                                    "ticket_number" to "Sijuru-01",
                                    "location" to sessionManager.getOperatorLocation().replace("[","").replace("]","").split(",").get(0),
                                    "plate" to plate,
                                    "operator_name" to sessionManager.getOperatorName(),
                                    "vehicle_name" to data_vehicle?.type,
                                    "phone_number" to sessionManager.getOperatorPhone(),
                                    "first_time" to currentDateandTime,
                                    "parking_fee" to data_vehicle?.price_base,
                                    "data_vehicle" to data_vehicle
                                )
                            )
                        }
                        else {
                            viewModel.postVehicleRecord(BodyVehicle(
                                mutableListOf(
                                Vehicle("",
                                    null,
                                    currentDateandTime, //vehicle_end_time_record
                                    plate.split("-").last(), //vehicle_plat_front
                                    plate.split("-").first(), //vehicle_plat_back
                                    plate.split("-").get(plate.split("-").size.div(3)), //vehicle_plat_middle
                                    sessionManager.getOperatorId(), //vehicle_sijuru_operator_id
                                    sessionManager.getOperatorShift(), //vehicle_sijuru_operator_shift
                                    currentDateandTime, //vehicle_start_time_record
                                    "0", //vehicle_total
                                    data_vehicle?.type, //vehicle_type
                                    null,
                                    null,
                                    null))
                            )
                                ,this@DialogInputVehicle)
                        }
                    }
                }
            }
        }
    }

    override fun getTheme(): Int {
        return R.style.SheetDialog
    }

    override fun beforeTextChanged(charsequence: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun onTextChanged(charsequence: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun afterTextChanged(editable: Editable?) {
        Log.e(TAG, "afterTextChanged: ${editable.toString()}")
        when (editable.hashCode()){
            edittext_first?.text.hashCode()->{
                if (edittext_first?.text.toString().length > 1){
                    edittext_middle?.requestFocus()
                }

            }
            edittext_middle?.text.hashCode()->{
                if (edittext_middle?.text.toString().length > 3){
                    val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                    imm!!.hideSoftInputFromWindow(view?.windowToken, 0)
                    lifecycleScope.launch{
                        delay(50)
                        edittext_end?.requestFocus()
                        val imm2 = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                        imm2!!.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
                    }
                }

            }
            edittext_end?.text.hashCode()->{
                if (edittext_end?.text.toString().length > 2){
                    val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                    imm!!.hideSoftInputFromWindow(view?.windowToken, 0)
                }

            }
        }
    }

    override fun onSuccessRecord(value: BaseResponseObject<VehicleRecord>) {
        super.onSuccessRecord(value)
        fragmentHelper.dismissLoading()
        val plate = "${edittext_first?.text}-${edittext_middle?.text}-${edittext_end?.text}"
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss a", Locale.getDefault())
        val currentDateandTime: String = sdf.format(Date())
        Log.e(TAG, "onSuccessRecord: ${sessionManager.getOperatorLocation().replace("[","").replace("]","").split(",").get(0)}")
        when(value.responseCode){
            1000->{
                findNavController().navigate(
                    R.id.action_dialogInputVehicle_to_progressiveReceiptFragment,
                    bundleOf(
                        "id" to value.responseData._id.get(0),
                        "location" to sessionManager.getOperatorLocation().replace("[","").replace("]","").split(",").get(0),
                        "plate" to plate,
                        "operator_name" to sessionManager.getOperatorName(),
                        "vehicle_name" to arguments?.getString("vehicle").toString(),
                        "phone_number" to sessionManager.getOperatorPhone(),
                        "first_time" to currentDateandTime,
                        "parking_fee" to "",
                        "data_vehicle" to data_vehicle,
                    )
                )}
            else->{}
        }
    }

    override fun onFailed(message: String) {
        super.onFailed(message)
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
    }
}