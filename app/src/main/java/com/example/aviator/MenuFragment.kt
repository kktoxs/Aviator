package com.example.aviator

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.aviator.databinding.FragmentMenuBinding
import kotlin.system.exitProcess


class MenuFragment : Fragment() {

  private val binding by lazy {
    FragmentMenuBinding.inflate(layoutInflater)
  }
  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.startButton.setOnClickListener {
      findNavController().navigate(R.id.action_menuFragment_to_gameFragment)
    }
    binding.exitButton.setOnClickListener {
      requireActivity().finish()
    }
  }
}