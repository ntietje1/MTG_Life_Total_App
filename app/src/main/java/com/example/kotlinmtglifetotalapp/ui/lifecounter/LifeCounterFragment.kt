package com.example.kotlinmtglifetotalapp.ui.lifecounter


import android.os.Bundle

import android.view.LayoutInflater

import android.view.View

import android.view.ViewGroup

import android.widget.Button
import android.widget.LinearLayout

import androidx.fragment.app.Fragment

import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation

import com.example.kotlinmtglifetotalapp.R

import com.example.kotlinmtglifetotalapp.databinding.LifeCounterLayoutBinding


class LifeCounterFragment : Fragment() {


    private var _binding: LifeCounterLayoutBinding? = null

    private val binding get() = _binding!!

    private val numPlayers get() = viewModel.playerStates.value!!.size

    private var playerButtons = arrayListOf<PlayerButton>()

    private lateinit var viewModel: LifeCounterViewModel

    private val angleConfigurations: Array<Int>
        get() = when (numPlayers) {
            2 -> arrayOf(90, 270)
            3 -> arrayOf(180, 0, 270)
            4 -> arrayOf(180, 0, 180, 0)
            5 -> arrayOf(180, 0, 180, 0, 270)
            6 -> arrayOf(180, 0, 180, 0, 180, 0)
            else -> throw IllegalArgumentException("invalid number of players")
        }

    private val weights: Array<Float>
        get() = when (numPlayers) {
            2 -> arrayOf(1f)
            3 -> arrayOf(1f, 1.67f)
            4 -> arrayOf(1f, 1f)
            5 -> arrayOf(1f, 1f, 1.15f)
            6 -> arrayOf(1f, 1f, 1f)
            else -> throw IllegalArgumentException("invalid number of players")
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LifeCounterLayoutBinding.inflate(inflater, container, false)
        val root: View = binding.root
        viewModel = ViewModelProvider(this)[LifeCounterViewModel::class.java]
        val arguments = arguments

        if (savedInstanceState != null) {
            viewModel.acceptPlayerBundle(savedInstanceState)
            println("got bundle")
        } else if (arguments != null) {
            viewModel.acceptPlayerBundle(arguments)
            println("no bundle, got arguments")
        } else {
            viewModel.acceptPlayerBundle(Bundle())
            println("no bundle or arguments, using empty bundle")
        }

        for (i in 0 until numPlayers) {
            val playerButton = generateButton()
            playerButton.id = View.generateViewId()
            playerButtons.add(playerButton)
        }

        viewModel.playerStates.observe(viewLifecycleOwner) { playerStates ->
            for ((i, player) in playerStates.withIndex()) {
                playerButtons[i].buttonBase.player = player
            }
        }
        return root

    }


    override fun onResume() {
        super.onResume()
        generateButtons()
        placeButtons()
    }

    /**
     * Remove any previous parental layouts and create new ones
     */
    private fun generateButtons() {
        for (playerButton in playerButtons) {
            if (playerButton.parent != null) {
                val parent = playerButton.parent as RotateLayout
                parent.removeAllViews()
            }
            generateButton(playerButton)
        }
    }

    /**
     * Make a rotateLayout to wrap this playerButton and set layoutParams accordingly
     */
    private fun generateButton(
        playerButton: PlayerButton = PlayerButton(
            requireContext(),
            PlayerButtonBase(requireContext(), null)
        )
    ): PlayerButton {

        playerButton.buttonBase.setBackgroundResource(R.drawable.rounded_corners)

        val rotateLayout = RotateLayout(context)
        rotateLayout.addView(playerButton)
        rotateLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT, 1f
        )

        playerButton.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        return playerButton

    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        removeAllViews()
        viewModel.fillPlayerBundle(outState)
    }

    private fun removeAllViews() {
        binding.linearLayout.removeAllViews()

        for (button in playerButtons) {
            (button.parent as RotateLayout).removeAllViews()
        }
    }


    private fun placeButtons() {
        val vLayout = binding.linearLayout

        for (i in 0 until numPlayers step 2) {
            val hLayout = LinearLayout(context)
            val weight = weights[(i + 1) / 2]
            hLayout.orientation =
                if (numPlayers != 2) LinearLayout.HORIZONTAL else LinearLayout.VERTICAL

            hLayout.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f / numPlayers * weight
            )

            for (j in i until minOf(i + 2, numPlayers)) {
                val angle = angleConfigurations[j]
                val button = playerButtons[j]
                val rotateLayout = button.parent as RotateLayout
                rotateLayout.angle = angle
                hLayout.addView(rotateLayout)
            }
            vLayout.addView(hLayout)
        }

        makeGoPlayerSelectButton(vLayout)
    }

    private fun makeGoPlayerSelectButton(vLayout: LinearLayout) {
        val button = Button(context)
        button.text = "Go to player select"
        button.setOnClickListener {
            val bundle = Bundle()
            viewModel.fillPlayerBundle(bundle)
            Navigation.findNavController(button).navigate(R.id.navigation_home, bundle)
        }
        vLayout.addView(button)
    }

}