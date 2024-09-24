package com.emirkanmaz.sharppixel.view

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.emirkanmaz.sharppixel.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        lifecycleScope.launch {
            delay(1500)

            if (isOnboardingCompleted()) {
                findNavController().navigate(R.id.action_splashFragment_to_processFragment)
            } else {
                findNavController().navigate(R.id.action_splashFragment_to_viewPagerFragment)
            }
        }

        return inflater.inflate(R.layout.fragment_splash, container, false)
    }


    private fun isOnboardingCompleted(): Boolean {
        val sharedPref = requireActivity().getSharedPreferences("onboarding_pref", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("onboarding_completed", false)
    }

}