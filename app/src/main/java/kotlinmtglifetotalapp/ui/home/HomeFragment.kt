package com.example.kotlinmtglifetotalapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

import com.example.kotlinmtglifetotalapp.databinding.FragmentHomeBinding
import kotlinmtglifetotalapp.ui.lifecounter.playerButton.Player

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val arguments = arguments

        if (arguments != null) {

            for (key in arguments.keySet()) {
                println("$key -> " + arguments.getParcelable(key, Player::class.java))
            }
            //val myData = arguments.getString("data_key")

            // Now you have the data, do whatever you need to do with it
            // For example, if the data is a String, you can display it in a TextView
            //println(myData)
        }

        return root
    }

    //val playerSelectScreen = binding.playerSelectScreen

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

}