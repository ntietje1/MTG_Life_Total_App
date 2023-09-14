package com.example.kotlinmtglifetotalapp.ui.lifecounter


import android.os.Bundle
import android.view.Gravity

import android.view.LayoutInflater

import android.view.View

import android.view.ViewGroup

import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatButton

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_CONSTRAINT

import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_PARENT

import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import androidx.constraintlayout.widget.ConstraintSet

import androidx.core.view.setMargins
import androidx.core.view.updateLayoutParams

import androidx.fragment.app.Fragment

import androidx.lifecycle.LiveData

import androidx.lifecycle.MutableLiveData

import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation

import com.example.kotlinmtglifetotalapp.R

import com.example.kotlinmtglifetotalapp.databinding.LifeCounterLayout4Binding

import com.example.kotlinmtglifetotalapp.databinding.LifeCounterLayoutBinding
import com.google.android.material.button.MaterialButton


class LifeCounterFragment : Fragment() {


    private var _binding: LifeCounterLayoutBinding? = null

    private val binding get() = _binding!!

    private var numPlayers = 2

    private var buttons = arrayListOf<PlayerButton>()

    private lateinit var viewModel: LifeCounterViewModel

    private val playerStates: ArrayList<Player> = arrayListOf()


    override fun onCreateView(

        inflater: LayoutInflater,

        container: ViewGroup?,

        savedInstanceState: Bundle?

    ): View {


        _binding = LifeCounterLayoutBinding.inflate(inflater, container, false)

        val root: View = binding.root

        viewModel = ViewModelProvider(this)[LifeCounterViewModel::class.java]

        val arguments = arguments

        if (arguments != null) {

            val num = arguments.getInt("numPlayers")

            // Now you have the data, do whatever you need to do with it
            // For example, if the data is a String, you can display it in a TextView
            println("GOT PLAYERS: $num")
            numPlayers = num
        }


        for (i in 0 until numPlayers) {

            val button = generateButton()

            button.id = View.generateViewId()

            println(button.id)

            buttons.add(button)

        }


        if (savedInstanceState != null) {

            loadPlayerStates(savedInstanceState)

        } else {

            loadPlayerStates(Bundle())

        }

        return root

    }


    override fun onResume() {

        super.onResume()

        for (button in buttons) {
            if (button.parent != null) {
                val parent = button.parent as RotateLayout
                parent.removeAllViews()
            }
            generateButton(button)
        }

        placeButtons()

    }


    private fun generateButton(
        button: PlayerButton = PlayerButton(
            requireContext(),
            null
        )
    ): PlayerButton {

        button.setBackgroundResource(R.drawable.rounded_corners)

        val rotateParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT, 1f
        )

        val rotateLayout = RotateLayout(context)
        rotateLayout.addView(button)
        rotateLayout.layoutParams = rotateParams

        val buttonParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        button.layoutParams = buttonParams

        return button

    }


    override fun onSaveInstanceState(outState: Bundle) {

        super.onSaveInstanceState(outState)

        binding.linearLayout.removeAllViews()

        for (button in buttons) {
            (button.parent as RotateLayout).removeAllViews()
        }

        savePlayers(outState)

    }

    private fun loadPlayerStates(bundle: Bundle) {

        for (i in 1..numPlayers) {

            var player = bundle.getParcelable("P$i", Player::class.java)

            if (player == null) {

                println("Couldn't load player P$i, generating new player")

                player = Player.generatePlayer()

            } else {

                println("Successfully loaded P$i")

            }

            buttons[i - 1].player = player

            playerStates.add(player)

        }

    }

    private fun placeButtons() {
        val vLayout = binding.linearLayout

        val angleConfigurations = when (numPlayers) {
            2 -> arrayOf(90, 270)
            3 -> arrayOf(180, 0, 270)
            4 -> arrayOf(180, 0, 180, 0)
            5 -> arrayOf(180, 0, 180, 0, 270)
            6 -> arrayOf(180, 0, 180, 0, 180, 0)
            else -> return // Handle unsupported numPlayers value
        }

        val weights = when (numPlayers) {
            2 -> arrayOf(1f)
            3 -> arrayOf(1f,1.67f)
            4 -> arrayOf(1f,1f)
            5 -> arrayOf(1f,1f,1.15f)
            6 -> arrayOf(1f,1f,1f)
            else -> return // Handle unsupported numPlayers value
        }

        for (i in 0 until numPlayers step 2) {
            val hLayout = LinearLayout(context)
            val weight = weights[(i + 1) / 2]
            hLayout.orientation = LinearLayout.HORIZONTAL
            val hLayoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f / numPlayers * weight
            )
            hLayout.layoutParams = hLayoutParams
            if (numPlayers == 2) {
                hLayout.orientation = LinearLayout.VERTICAL
            }

            for (j in i until minOf(i + 2, numPlayers)) {
                val angle = angleConfigurations[j]
                val button = buttons[j]
                val rotateLayout = button.parent as RotateLayout
                rotateLayout.angle = angle
                hLayout.addView(rotateLayout)
            }
            vLayout.addView(hLayout)
        }

        val button = Button(context)
        button.text = "Go to player select"
        button.setOnClickListener {
            val bundle = Bundle()
            savePlayers(bundle)
            Navigation.findNavController(button).navigate(R.id.navigation_home, bundle)
        }
        vLayout.addView(button)
    }

    private fun savePlayers(outstate: Bundle) {
        for (player in playerStates) {
            println("Saved $player")
            outstate.putParcelable(player.toString(), player)
        }
    }
}