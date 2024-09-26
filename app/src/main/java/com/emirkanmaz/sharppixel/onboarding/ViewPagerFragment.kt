package com.emirkanmaz.sharppixel.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.emirkanmaz.sharppixel.databinding.FragmentViewPagerBinding
import com.emirkanmaz.sharppixel.onboarding.adapter.ViewPagerAdapter
import com.emirkanmaz.sharppixel.onboarding.screens.FirstScreenFragment
import com.emirkanmaz.sharppixel.onboarding.screens.SecondScreenFragment

class ViewPagerFragment : Fragment() {
    private var _binding: FragmentViewPagerBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewPagerBinding.inflate(inflater, container, false)


        val fragmentList = arrayListOf<Fragment>(
            FirstScreenFragment(),
            SecondScreenFragment()
        )

        val adapter = ViewPagerAdapter(fragmentList, requireActivity().supportFragmentManager, lifecycle)

        binding.viewPager.adapter = adapter
        binding.dotsIndicator.attachTo(binding.viewPager)

        return binding.root
    }

}