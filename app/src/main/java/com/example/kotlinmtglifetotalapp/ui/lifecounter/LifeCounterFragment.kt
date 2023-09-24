package com.example.kotlinmtglifetotalapp.ui.lifecounter


import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity

import android.view.LayoutInflater

import android.view.View

import android.view.ViewGroup

import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.setPadding

import androidx.fragment.app.Fragment

import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation

import com.example.kotlinmtglifetotalapp.R

import com.example.kotlinmtglifetotalapp.databinding.LifeCounterLayoutBinding


class LifeCounterFragment : Fragment() {


    private var _binding: LifeCounterLayoutBinding? = null

    private val binding get() = _binding!!

    private val numPlayers get() = Player.currentPlayers.size

    private var playerButtons = arrayListOf<PlayerButton>()

    //private lateinit var viewModel: LifeCounterViewModel

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
            5 -> arrayOf(1f, 1f, 1.2f)
            6 -> arrayOf(1f, 1f, 1f)
            else -> throw IllegalArgumentException("invalid number of players")
        }

    private val middlePos: Float
        get() = when(numPlayers) {
            2 -> 0.0f
            3 -> 0.12f
            4 -> 0.0f
            5 -> -0.124f
            6 -> -0.166f
            else -> throw IllegalArgumentException("invalid number of players")
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LifeCounterLayoutBinding.inflate(inflater, container, false)
        val root: View = binding.root
        //viewModel = ViewModelProvider(this)[LifeCounterViewModel::class.java]
        val arguments = arguments

        if (savedInstanceState != null) {
            unpackBundle(savedInstanceState)
            println("got bundle")
        } else if (arguments != null) {
            unpackBundle(arguments)
            println("no bundle, got arguments")
        } else {
            unpackBundle(Bundle())
            println("no bundle or arguments, using empty bundle")
        }

        for (i in 0 until numPlayers) {
            val playerButton = generateButton()
            playerButton.id = View.generateViewId()
            playerButtons.add(playerButton)
        }

        for ((i, player) in Player.currentPlayers.withIndex()) {
            playerButtons[i].buttonBase.player = player
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

        packBundle(outState)
    }

    private fun packBundle(outState: Bundle) {
        outState.putInt("numPlayers", Player.currentPlayers.size)
        for (player in Player.currentPlayers) {
            println("Saved $player")
            outState.putParcelable(player.toString(), player)
        }
    }

    private fun unpackBundle(inState: Bundle) {
        val players = arrayListOf<Player>()

        val numPlayers = inState.getInt("numPlayers")
        println("bundle says: $numPlayers players")

        for (i in 1..numPlayers) {

            var player = inState.getParcelable("P$i", Player::class.java)

            if (player == null) {
                println("Couldn't load player P$i, generating new player")
                player = Player.generatePlayer()

            } else {
                println("Successfully loaded P$i")
            }

            players.add(player)

        }

        Player.currentPlayers = players
    }

    private fun removeAllViews() {
        binding.frameLayout.removeAllViews()
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

        val fLayout = binding.frameLayout
        val middleButton = ImageButton(context).apply {
            setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.middle_solid_icon))
            background = AppCompatResources.getDrawable(context, R.drawable.circular_background_black)

            //rotation -= 90f
            stateListAnimator = null
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
//                setMargins(20, 20, 20, 20)

            }
            setPadding(10)
            setOnClickListener {
                val bundle = Bundle()
                packBundle(bundle)
                Navigation.findNavController(this).navigate(R.id.navigation_home, bundle)
            }
        }
        fLayout.addView(middleButton)


        val screenHeight = resources.displayMetrics.heightPixels
        val middleButtonY = (screenHeight * middlePos).toInt()
        val middleButtonLayoutParams = middleButton.layoutParams as FrameLayout.LayoutParams
        middleButtonLayoutParams.topMargin = middleButtonY
        middleButton.layoutParams = middleButtonLayoutParams

    }





}