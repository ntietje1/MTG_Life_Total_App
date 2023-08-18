package com.example.kotlinmtglifetotalapp.ui.lifecounter


import android.os.Bundle

import android.view.LayoutInflater

import android.view.View

import android.view.ViewGroup

import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
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

import com.example.kotlinmtglifetotalapp.R

import com.example.kotlinmtglifetotalapp.databinding.LifeCounterLayout4Binding

import com.example.kotlinmtglifetotalapp.databinding.LifeCounterLayoutBinding
import com.google.android.material.button.MaterialButton


class LifeCounterFragment : Fragment() {


    private var _binding: LifeCounterLayoutBinding? = null

    private val binding get() = _binding!!

    private val numPlayers = 2


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

        placeButtons()

    }


    private fun generateButton(): PlayerButton {

        val button = PlayerButton(requireContext(), null)

        button.setBackgroundResource(R.drawable.rounded_corners)

        return button

//        val button = MaterialButton(this.requireContext())
//        button.text = "test"
//        button.id = View.generateViewId()
//        button.rotation = 90f
//        button.updateLayoutParams<ConstraintLayout.LayoutParams> {
//            width = ConstraintLayout.LayoutParams.WRAP_CONTENT
//            height = ConstraintLayout.LayoutParams.WRAP_CONTENT
//            setMargins(8.dpToPx(), 0, 8.dpToPx(), 0)
//        }
//        return button

    }


    override fun onSaveInstanceState(outState: Bundle) {

        super.onSaveInstanceState(outState)

        val constraintLayout = binding.constraintLayout

        for (button in buttons) {
            constraintLayout.removeView(button)
        }

        for (player in playerStates) {

            outState.putParcelable(player.toString(), player)

            println("Saved $player")

        }

    }


    // load player states into buttons

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

        val constraintLayout = binding.constraintLayout

        val constraintSet = ConstraintSet()

        for ((index, button) in buttons.withIndex()) {

            button.layoutParams = ConstraintLayout.LayoutParams(0, 0)

            constraintLayout.addView(button, index)

        }

        constraintSet.clone(constraintLayout)

        when (numPlayers) {

            4 -> {
                constraintSet.setRotation(buttons[0].id, 180f)
                constraintSet.connect(buttons[0].id, ConstraintSet.START, constraintLayout.id, ConstraintSet.START, 8)
                constraintSet.connect(buttons[0].id, ConstraintSet.TOP, constraintLayout.id, ConstraintSet.TOP, 8)
                constraintSet.connect(buttons[0].id, ConstraintSet.BOTTOM, buttons[3].id, ConstraintSet.TOP, 8)
                constraintSet.connect(buttons[0].id, ConstraintSet.END, buttons[1].id, ConstraintSet.START, 8)

                constraintSet.connect(buttons[1].id, ConstraintSet.START, buttons[0].id, ConstraintSet.END, 8)
                constraintSet.connect(buttons[1].id, ConstraintSet.TOP, constraintLayout.id, ConstraintSet.TOP, 8)
                constraintSet.connect(buttons[1].id, ConstraintSet.BOTTOM, buttons[2].id, ConstraintSet.TOP, 11)
                constraintSet.connect(buttons[1].id, ConstraintSet.END, constraintLayout.id, ConstraintSet.END, 8)

                constraintSet.connect(buttons[2].id, ConstraintSet.START, buttons[3].id, ConstraintSet.END, 8)
                constraintSet.connect(buttons[2].id, ConstraintSet.TOP, buttons[1].id, ConstraintSet.BOTTOM, 8)
                constraintSet.connect(buttons[2].id, ConstraintSet.END, constraintLayout.id, ConstraintSet.END, 8)
                constraintSet.connect(buttons[2].id, ConstraintSet.BOTTOM, constraintLayout.id, ConstraintSet.BOTTOM, 8)

                constraintSet.setRotation(buttons[3].id, 180f)
                constraintSet.connect(buttons[3].id, ConstraintSet.START, constraintLayout.id, ConstraintSet.START, 8)
                constraintSet.connect(buttons[3].id, ConstraintSet.TOP, buttons[0].id, ConstraintSet.BOTTOM, 11)
                constraintSet.connect(buttons[3].id, ConstraintSet.END, buttons[2].id, ConstraintSet.START, 8)
                constraintSet.connect(buttons[3].id, ConstraintSet.BOTTOM, constraintLayout.id, ConstraintSet.BOTTOM, 8)
            }

            2 -> {

                //constraintSet.setRotation(buttons[0].id, 90f)
                constraintSet.connect(buttons[0].id, ConstraintSet.START, constraintLayout.id, ConstraintSet.START, 8)
                constraintSet.connect(buttons[0].id, ConstraintSet.TOP, constraintLayout.id, ConstraintSet.TOP, 8)
                constraintSet.connect(buttons[0].id, ConstraintSet.BOTTOM, buttons[1].id, ConstraintSet.TOP, 8)
                constraintSet.connect(buttons[0].id, ConstraintSet.END, constraintLayout.id, ConstraintSet.END, 8)

                constraintSet.setRotation(buttons[1].id, 180f)
                constraintSet.connect(buttons[1].id, ConstraintSet.START, constraintLayout.id, ConstraintSet.START, 8)
                constraintSet.connect(buttons[1].id, ConstraintSet.TOP, buttons[0].id, ConstraintSet.BOTTOM, 8)
                constraintSet.connect(buttons[1].id, ConstraintSet.BOTTOM, constraintLayout.id, ConstraintSet.BOTTOM, 8)
                constraintSet.connect(buttons[1].id, ConstraintSet.END, constraintLayout.id, ConstraintSet.END, 8)
            }

            3 -> {

                constraintSet.setRotation(buttons[0].id, 180f)
                constraintSet.connect(buttons[0].id, ConstraintSet.START, constraintLayout.id, ConstraintSet.START, 5)
                constraintSet.connect(buttons[0].id, ConstraintSet.TOP, constraintLayout.id, ConstraintSet.TOP, 5)
                constraintSet.connect(buttons[0].id, ConstraintSet.BOTTOM, buttons[2].id, ConstraintSet.TOP, 5)
                constraintSet.connect(buttons[0].id, ConstraintSet.END, buttons[1].id, ConstraintSet.START, 5)

                constraintSet.connect(buttons[1].id, ConstraintSet.START, buttons[0].id, ConstraintSet.END, 5)
                constraintSet.connect(buttons[1].id, ConstraintSet.TOP, constraintLayout.id, ConstraintSet.TOP, 5)
                constraintSet.connect(buttons[1].id, ConstraintSet.BOTTOM, buttons[2].id, ConstraintSet.TOP, 5)
                constraintSet.connect(buttons[1].id, ConstraintSet.END, constraintLayout.id, ConstraintSet.END, 5)

                constraintSet.setRotation(buttons[2].id, 270f)
                constraintSet.connect(buttons[2].id, ConstraintSet.START, constraintLayout.id, ConstraintSet.START, 5)
                constraintSet.connect(buttons[2].id, ConstraintSet.TOP, buttons[1].id, ConstraintSet.BOTTOM, 5)
                constraintSet.connect(buttons[2].id, ConstraintSet.BOTTOM, constraintLayout.id, ConstraintSet.BOTTOM, 5)
                constraintSet.connect(buttons[2].id, ConstraintSet.END, constraintLayout.id, ConstraintSet.END, 5)


            }

//            4 -> {
//
//                val params1 = buttons[0].layoutParams as ConstraintLayout.LayoutParams
//
//                buttons[0].rotation = 180f
//
//                params1.width = (constraintLayout.width / 2.1).toInt()
//
//                params1.height = (constraintLayout.height / 2.1).toInt()
//
//                params1.startToStart = PARENT_ID
//
//                params1.topToTop = PARENT_ID
//
//                params1.bottomToTop = buttons[3].id
//
//                params1.endToStart = buttons[1].id
//
//
//                val params2 = buttons[1].layoutParams as ConstraintLayout.LayoutParams
//
//                params2.width = (constraintLayout.width / 2.1).toInt()
//
//                params2.height = (constraintLayout.height / 2.1).toInt()
//
//                params2.startToEnd = buttons[0].id
//
//                params2.topToTop = PARENT_ID
//
//                params2.bottomToTop = buttons[2].id
//
//                params2.endToEnd = PARENT_ID
//
//
//                val params3 = buttons[2].layoutParams as ConstraintLayout.LayoutParams
//
//                params3.width = (constraintLayout.width / 2.1).toInt()
//
//                params3.height = (constraintLayout.height / 2.1).toInt()
//
//                params3.startToEnd = buttons[3].id
//
//                params3.topToBottom = buttons[1].id
//
//                params3.endToEnd = PARENT_ID
//
//                params3.bottomToBottom = PARENT_ID
//
//
//                val params4 = buttons[3].layoutParams as ConstraintLayout.LayoutParams
//
//                buttons[3].rotation = 180f
//
//                params4.width = (constraintLayout.width / 2.1).toInt()
//
//                params4.height = (constraintLayout.height / 2.1).toInt()
//
//                params4.startToStart = PARENT_ID
//
//                params4.topToBottom = buttons[0].id
//
//                params4.endToStart = buttons[2].id
//
//                params4.bottomToBottom = PARENT_ID
//
//            }

            5 -> {

                val params1 = buttons[0].layoutParams as ConstraintLayout.LayoutParams

                buttons[0].rotation = 180f

                params1.width = (constraintLayout.width / 2.05).toInt()

                params1.height = (constraintLayout.height / 3.03).toInt()

                params1.startToStart = PARENT_ID

                params1.topToBottom = buttons[4].id

                params1.bottomToTop = buttons[3].id

                params1.endToStart = buttons[1].id


                val params2 = buttons[1].layoutParams as ConstraintLayout.LayoutParams

                params2.width = (constraintLayout.width / 2.05).toInt()

                params2.height = (constraintLayout.height / 3.03).toInt()

                params2.startToEnd = buttons[0].id

                params2.topToBottom = buttons[4].id

                params2.bottomToTop = buttons[2].id

                params2.endToEnd = PARENT_ID


                val params3 = buttons[2].layoutParams as ConstraintLayout.LayoutParams

                params3.width = (constraintLayout.width / 2.05).toInt()

                params3.height = (constraintLayout.height / 3.03).toInt()

                params3.startToEnd = buttons[3].id

                params3.topToBottom = buttons[1].id

                params3.endToEnd = PARENT_ID

                params3.bottomToBottom = PARENT_ID


                val params4 = buttons[3].layoutParams as ConstraintLayout.LayoutParams

                buttons[3].rotation = 180f

                params4.width = (constraintLayout.width / 2.05).toInt()

                params4.height = (constraintLayout.height / 3.03).toInt()

                params4.startToStart = PARENT_ID

                params4.topToBottom = buttons[0].id

                params4.endToStart = buttons[2].id

                params4.bottomToBottom = PARENT_ID


                val params5 = buttons[4].layoutParams as ConstraintLayout.LayoutParams

                buttons[4].rotation = 90f

                params3.width = (constraintLayout.height / 3.03).toInt()

                params3.height = (constraintLayout.width / 2.05).toInt()

                params3.startToStart = PARENT_ID

                params3.topToTop = PARENT_ID

                params3.endToEnd = PARENT_ID

                params3.bottomToTop = buttons[0].id

            }

            6 -> {

                val params1 = buttons[0].layoutParams as ConstraintLayout.LayoutParams

                buttons[0].rotation = 180f

                params1.width = (constraintLayout.width / 2.05).toInt()

                params1.height = (constraintLayout.height / 4.03).toInt()

                params1.startToStart = PARENT_ID

                params1.topToBottom = buttons[4].id

                params1.bottomToTop = buttons[3].id

                params1.endToStart = buttons[1].id


                val params2 = buttons[1].layoutParams as ConstraintLayout.LayoutParams

                params2.width = (constraintLayout.width / 2.05).toInt()

                params2.height = (constraintLayout.height / 4.03).toInt()

                params2.startToEnd = buttons[0].id

                params2.topToBottom = buttons[4].id

                params2.bottomToTop = buttons[2].id

                params2.endToEnd = PARENT_ID


                val params3 = buttons[2].layoutParams as ConstraintLayout.LayoutParams

                params3.width = (constraintLayout.width / 2.05).toInt()

                params3.height = (constraintLayout.height / 4.03).toInt()

                params3.startToEnd = buttons[3].id

                params3.topToBottom = buttons[1].id

                params3.endToEnd = PARENT_ID

                params3.bottomToBottom = PARENT_ID


                val params4 = buttons[3].layoutParams as ConstraintLayout.LayoutParams

                buttons[3].rotation = 180f

                params4.width = (constraintLayout.width / 2.05).toInt()

                params4.height = (constraintLayout.height / 4.03).toInt()

                params4.startToStart = PARENT_ID

                params4.topToBottom = buttons[0].id

                params4.endToStart = buttons[2].id

                params4.bottomToBottom = PARENT_ID


                val params5 = buttons[4].layoutParams as ConstraintLayout.LayoutParams

                buttons[4].rotation = 90f

                params3.width = (constraintLayout.height / 4.03).toInt()

                params3.height = (constraintLayout.width / 2.05).toInt()

                params3.startToStart = PARENT_ID

                params3.topToTop = PARENT_ID

                params3.endToEnd = PARENT_ID

                params3.bottomToTop = buttons[0].id


                val params6 = buttons[5].layoutParams as ConstraintLayout.LayoutParams

                buttons[5].rotation = 270f

                params3.width = (constraintLayout.height / 4.03).toInt()

                params3.height = (constraintLayout.width / 2.05).toInt()

                params3.startToStart = PARENT_ID

                params3.topToBottom = buttons[3].id

                params3.endToEnd = PARENT_ID

                params3.bottomToBottom = PARENT_ID

            }

        }

        constraintSet.applyTo(constraintLayout)

    }

    private fun Int.dpToPx(): Int {
        val scale = resources.displayMetrics.density
        return (this * scale + 0.5f).toInt()
    }


}