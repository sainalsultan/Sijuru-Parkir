package com.sijuru.history

import android.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sijuru.R
import com.sijuru.core.data.local.entity.DataUser
import com.sijuru.core.data.local.entity.ResponseVehicleQRCodeItem
import com.sijuru.core.data.local.entity.Vehicle
import com.sijuru.core.utils.SessionManager
import com.sijuru.login.MainListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HistoryFragment : Fragment(), MainListener, AdapterListHistory.AdapterListHistoryListener {

    @Inject
    lateinit var viewModel: HistoryViewModel

    @Inject
    lateinit var sessionManager: SessionManager
    private val adapterListHistory by lazy { AdapterListHistory() }
    private var swiperefresh_history: SwipeRefreshLayout? = null
    private var toolbar_title: TextView? = null
    private var img_detail: ImageView? = null
    private var constraintlayout_no_data: ConstraintLayout? = null
    private var recyclerview_history: RecyclerView? = null
    private var textview_total_value: TextView? = null
    private var edittext_search: EditText? = null
    private var floating_search: FloatingActionButton? = null
    private var search: Boolean = true
    private var data: List<ResponseVehicleQRCodeItem> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)
        swiperefresh_history        = view.findViewById(R.id.swiperefresh_history)
        toolbar_title               = view.findViewById(R.id.toolbar_title)
        img_detail                  = view.findViewById(R.id.img_detail)
        constraintlayout_no_data    = view.findViewById(R.id.constraintlayout_no_data)
        recyclerview_history        = view.findViewById(R.id.recyclerview_history)
        textview_total_value        = view.findViewById(R.id.textview_total_value)
        edittext_search        = view.findViewById(R.id.edittext_search)
        floating_search        = view.findViewById(R.id.floating_search)

        swiperefresh_history?.isRefreshing = true

        toolbar_title?.setText("Daftar Riwayat")
        img_detail?.visibility = View.VISIBLE
        img_detail?.setImageDrawable(ContextCompat.getDrawable(requireContext(),
            R.drawable.ic_round_sync
        ))

        viewModel.getHistoryVehicle(this)
        viewModel.dataVehicleRecord.observe(viewLifecycleOwner){
            when(it.responseCode){
                1000->{
                    swiperefresh_history?.isRefreshing = false
                    data = it.responseData
                    textview_total_value?.text = "${data.size}"
                    recyclerview_history?.apply {
                        setHasFixedSize(true)
                        adapter = adapterListHistory.also { it.setAdapter(data) }
                    }

                    constraintlayout_no_data?.visibility = if (data.isEmpty()) View.VISIBLE else View.GONE

                }
                else->{
                    swiperefresh_history?.isRefreshing = false
                }
            }
        }

        swiperefresh_history?.setOnRefreshListener {
            swiperefresh_history?.isRefreshing = true
            viewModel.getHistoryVehicle(this)
        }

        img_detail?.setOnClickListener {
            swiperefresh_history?.isRefreshing = true
            viewModel.getHistoryVehicle(this)
        }

        adapterListHistory.also { it.OnClickItemHistory(this) }
        floating_search?.setOnClickListener {
            if (search){
                edittext_search?.visibility = View.VISIBLE
                floating_search?.setImageResource(R.drawable.ic_close)
                search = false
            }else{
                edittext_search?.visibility = View.GONE
                floating_search?.setImageResource(R.drawable.ic_search)
                search = true
            }
        }

        edittext_search?.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

        return view
    }


    override fun OnClickItemHistory(data: ResponseVehicleQRCodeItem) {
        showDialogCheck(data)
    }

    private fun showDialogCheck(data: ResponseVehicleQRCodeItem) {
        val dialog = AlertDialog.Builder(requireActivity())
            .setView(R.layout.view_dialog_check)
            .show()
        val button_punishment = dialog.findViewById<Button>(R.id.button_punishment)
        val button_detail = dialog.findViewById<Button>(R.id.button_detail)
        button_punishment.setOnClickListener {
            //update punishment
            viewModel.postVehicleRecordPunishment(
                Vehicle(
                null,
                data.vehicle_uuid,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null),
                this
            )
            viewModel.dataVehicle.observe(viewLifecycleOwner){
                when(it.responseCode){
                    1000->{
                        dialog.dismiss()
                        Toast.makeText(
                            requireActivity(),
                            "${it.responseDescription}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else->{
                        dialog.dismiss()
                        Toast.makeText(
                            requireActivity(),
                            "${it.responseDescription}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
        button_detail.setOnClickListener {
            dialog.dismiss()
            if (data.vehicle_status){
                findNavController().navigate(R.id.action_historyFragment_to_receiptFragment,
                    bundleOf(
                        "from" to "history",
                        "id" to data.vehicle_uuid,
                        "location" to sessionManager.getOperatorLocation(),
                        "plate" to "${data.vehicle_plat_front}-${data.vehicle_plat_middle}-${data.vehicle_plat_back}",
                        "operator_name" to sessionManager.getOperatorName(),
                        "vehicle_name" to data.vehicle_type,
                        "phone_number" to sessionManager.getOperatorPhone(),
                        "first_time" to data.vehicle_start_time_record,
                        "end_time" to data.vehicle_end_time_record,
                        "parking_fee" to data.vehicle_total
                    ))
            }else{
                Toast.makeText(context, "Data belum tersedia", Toast.LENGTH_SHORT).show()
            }
        }
    }

}