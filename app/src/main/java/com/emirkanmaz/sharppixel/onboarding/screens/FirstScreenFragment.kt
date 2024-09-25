package com.emirkanmaz.sharppixel.onboarding.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.emirkanmaz.sharppixel.R
import com.emirkanmaz.sharppixel.databinding.FragmentFirstScreenBinding

class FirstScreenFragment : Fragment() {
    private var _binding: FragmentFirstScreenBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFirstScreenBinding.inflate(inflater, container, false)

        val viewPager = activity?.findViewById<ViewPager2>(R.id.viewPager)

        binding.nextButton.setOnClickListener {
            viewPager?.currentItem = 1
        }

        return binding.root
    }

}