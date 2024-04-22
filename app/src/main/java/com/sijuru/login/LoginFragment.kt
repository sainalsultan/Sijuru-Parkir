package com.sijuru.login

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import com.sijuru.R
import com.sijuru.core.data.local.entity.User
import com.sijuru.core.data.local.entity.VehicleSijuruParkingTypeDetai
import com.sijuru.core.data.response.BaseResponseObject
import com.sijuru.core.utils.FragmentHelper
import com.sijuru.core.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment(), MainListener {

    @Inject
    lateinit var viewModel: LoginViewModel

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var fragmentHelper: FragmentHelper
    private var type = ""

    private var textinputlayout_user_id: TextInputLayout? = null
    private var textinputlayout_password: TextInputLayout? = null
    private var button_login: Button? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        textinputlayout_user_id = view.findViewById(R.id.textinputlayout_user_id)
        textinputlayout_password = view.findViewById(R.id.textinputlayout_password)
        button_login = view.findViewById(R.id.button_login)

        button_login?.setOnClickListener {
            if (textinputlayout_user_id?.editText?.text?.toString().equals("")) {
                Toast.makeText(
                    requireContext(),
                    "Silakan masukkan Nomor ID Anda!",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                fragmentHelper.showLoading()
                viewModel.postLogin(
                    textinputlayout_user_id?.editText?.text?.toString(),
                    textinputlayout_password?.editText?.text?.toString(),
                    this
                )
            }
        }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.dataLogin.observe(viewLifecycleOwner) {
            when (it?.responseCode) {
                1000 -> {
                    Log.e("SijuruJi", it.responseDescription)
                }
                else -> {
                    it?.let { it1 -> Log.e("SijuruJi", it1.responseDescription) }
                }
            }
        }
    }

    override fun onSuccessLogin(value: BaseResponseObject<User>) {
        super.onSuccessLogin(value)
        when (value.responseCode) {
            1000 -> {
                value.responseData.also {
                    Toast.makeText(requireContext(), value.responseDescription, Toast.LENGTH_SHORT).show()
                    fragmentHelper.dismissLoading()
                    sessionManager.createLoginSession(
                        true,
                        it.token_access,
                        it.vehicle_merchant_id,
                        it.vehicle_sijuru_operator_id,
                        it.vehicle_sijuru_operator_name,
                        it.vehicle_sijuru_operator_phone,
                        it.vehicle_sijuru_operator_shift.toString(),
                        it.vehicle_sijuru_operator_location,
                        it.vehicle_user_id,
                        it.vehicle_sijuru_parking_type_detai.map { it.price_base }.toString(),
                        it.vehicle_sijuru_parking_type_detai.map { it.price_increment }.toString(),
                        it.vehicle_sijuru_parking_type_detai.map { it.price_increment_price }.toString(),
                        it.vehicle_sijuru_parking_type_detai.map { it.price_max_price }.toString(),
                        it.vehicle_sijuru_parking_type_detai.map { it.type }.toString(),
                        it.vehicle_sijuru_parking_type_detai.size.toString(),
                        it.vehicle_sijuru_parking_type_detai.map { it.name }.toString(),
                        it.vehicle_sijuru_parking_type
                    )
                    findNavController().navigate(R.id.action_loginFragment_to_mainMenuFragment)
                }
            }
            else -> {
                fragmentHelper.dismissLoading()
                Toast.makeText(requireContext(), value.responseDescription, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onFailedLogin(message: String) {
        super.onFailedLogin(message)
        fragmentHelper.dismissLoading()
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}