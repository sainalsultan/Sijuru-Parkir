package com.sijuru

import android.content.ContentValues
import android.content.Context
import android.hardware.input.InputManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide


class DialogVehicleFragment : Fragment() {

    private var constraintlayout_input_vehicle: ConstraintLayout? = null
    private var edittext_first: EditText? = null
    private var edittext_middle: EditText? = null
    private var edittext_end: EditText? = null
    private var imageview_vehicle: ImageView? = null
    private var button_cancel: Button? = null
    private var button_done: Button? = null

    private fun getWindowHeight(): Int {
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dialog_vehicle, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        constraintlayout_input_vehicle = view.findViewById(R.id.constraintlayout_input_vehicle)
        imageview_vehicle = view.findViewById(R.id.imageview_vehicle)
        edittext_first = view.findViewById(R.id.edittext_first)
        edittext_middle = view.findViewById(R.id.edittext_middle)
        edittext_end = view.findViewById(R.id.edittext_end)
        button_cancel = view.findViewById(R.id.button_cancel)
        button_done = view.findViewById(R.id.button_done)

        val imgPath = arguments?.getString("vehicle")
        Log.e(ContentValues.TAG, "onViewCreated: $imgPath")
        activity?.applicationContext?.let {
            Glide.with(it)
                .load(imgPath)
                .placeholder(R.drawable.bg_rounded_orange)
                .into(imageview_vehicle!!)
        }

        button_cancel?.setOnClickListener {
            setFragmentResult("picture", bundleOf("from" to "dialog"))
        }

        button_done?.setOnClickListener {
            if (edittext_first?.text?.isEmpty() == true ||
                edittext_middle?.text?.isEmpty() == true ||
                edittext_end?.text?.isEmpty() == true
            )
                Toast.makeText(
                    requireContext(),
                    "Silakan masukkan plat kendaraan",
                    Toast.LENGTH_LONG
                ).show()
            else {
                val plate = "${edittext_first?.text}${edittext_middle?.text}${edittext_end?.text}"
                findNavController().navigate(
                    R.id.action_dialogInputVehicle_to_receiptFragment,
                    bundleOf(
                        "ticket_number" to "sijuru-01",
                        "plate" to plate,
                        "operator_name" to "Sainal",
                        "vehicle_name" to "Toyota",
                        "phone_number" to "0823585967",
                        "first_time" to "2022-02-26 10:10:10",
                        "parking_fee" to "2000"
                    )
                )
            }
        }
    }
}