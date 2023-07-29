package com.example.kotlinmtglifetotalapp.ui.lifecounter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.kotlinmtglifetotalapp.databinding.LifeCounterLayoutBinding

class LifeCounterFragment : Fragment() {

    private var _binding: LifeCounterLayoutBinding? = null
    private val binding get() = _binding!!

    private lateinit var buttons: List<PlayerButton>
    private lateinit var viewModel: LifeCounterViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = LifeCounterLayoutBinding.inflate(inflater, container, false)
        val root: View = binding.root

        viewModel = ViewModelProvider(this)[LifeCounterViewModel::class.java]

        buttons = listOf(binding.button1, binding.button2, binding.button3, binding.button4)
        loadPlayerStates()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        loadPlayerStates()
    }

    override fun onPause() {
        super.onPause()
        for (button in buttons) {
            val player = button.player
            if (player != null) {
                viewModel.savePlayerState(player)
            }
        }
    }

    private fun loadPlayerStates() {
        val playerStates = viewModel.loadPlayerStates()
        for ((index, button) in buttons.withIndex()) {
            if (playerStates.size > index) {
                val player = playerStates[index]
                button.player = (player)
            }
        }
    }
}
