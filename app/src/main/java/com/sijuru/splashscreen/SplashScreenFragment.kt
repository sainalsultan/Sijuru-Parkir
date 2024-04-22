package com.sijuru.splashscreen

import android.content.ContentValues.TAG
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.sijuru.R
import com.sijuru.core.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SplashScreenFragment : Fragment() {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_splash_screen, container, false)
        Log.e(TAG, "onCreateView: ${sessionManager.isLoggedIn}")
        Handler(Looper.myLooper()!!).postDelayed({
            if (sessionManager.isLoggedIn)
                findNavController().navigate(R.id.action_splashScreenFragment_to_mainMenuFragment)
            else
                findNavController().navigate(R.id.action_splashScreenFragment_to_loginFragment)
        },2000)
        return view
    }
}