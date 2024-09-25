package com.emirkanmaz.sharppixel.onboarding.screens

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.emirkanmaz.sharppixel.R
import com.emirkanmaz.sharppixel.databinding.FragmentSecondScreenBinding

class SecondScreenFragment : Fragment() {
    private var _binding: FragmentSecondScreenBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSecondScreenBinding.inflate(inflater, container, false)


        binding.finishButton.setOnClickListener {
            setOnboardingCompleted(true)
            findNavController().navigate(R.id.action_viewPagerFragment_to_processFragment)
        }

        return binding.root

    }
    private fun setOnboardingCompleted(completed: Boolean) {
        val sharedPref = requireActivity().getSharedPreferences("onboarding_pref", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("onboarding_completed", completed)
        editor.apply()
    }

}