package com.example.kotlinmtglifetotalapp.ui.lifecounter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_PARENT
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import androidx.core.view.setMargins
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.example.kotlinmtglifetotalapp.R
import com.example.kotlinmtglifetotalapp.databinding.LifeCounterLayout4Binding
import com.example.kotlinmtglifetotalapp.databinding.LifeCounterLayoutBinding

class LifeCounterFragment : Fragment() {

    private var _binding: LifeCounterLayoutBinding? = null
    private val binding get() = _binding!!
    private val numPlayers = 4

    private var buttons = arrayListOf<PlayerButton>()
    private lateinit var viewModel: LifeCounterViewModel

//    private val _playerStates = MutableLiveData<MutableList<Player>>()
//    private val playerStates: LiveData<MutableList<Player>> = _playerStates
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
            buttons.add(generateButton())
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
//        android:layout_width="0dp"
//        android:layout_height="0dp"
//        android:layout_margin="5dp"
//        android:rotation="180"
//        android:contentDescription="@string/content_desc"
//        android:background="@drawable/rounded_corners"
        return button
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        for (player in playerStates) {
            outState.putParcelable(player.toString(), player)
            println("Saved $player")
        }
    }

    // load player states into buttons
    private fun loadPlayerStates(bundle: Bundle) {
        for (i in 1 .. numPlayers) {
            var player = bundle.getParcelable("P$i", Player::class.java)
            if (player == null) {
                println("Couldn't load player P$i, generating new player")
                player = Player.generatePlayer()
            } else {
                println("Successfully loaded P$i")
            }

            buttons[i-1].player = player
            playerStates.add(player)
        }
    }

    private fun placeButtons() {
        val constraintLayout = binding.constraintLayout
        for (button in buttons) {
            constraintLayout.addView(button)
        }
        when (numPlayers) {
            4 -> {
                val params1 = buttons[0].layoutParams as ConstraintLayout.LayoutParams
                buttons[0].rotation = 180f
                params1.width = (constraintLayout.width / 2.05).toInt()
                params1.height = (constraintLayout.height / 2.03).toInt()
                params1.setMargins(5)
                params1.startToStart = PARENT_ID
                params1.topToTop = PARENT_ID
                params1.bottomToTop = buttons[3].id
                params1.endToStart = buttons[1].id

                val params2 = buttons[1].layoutParams as ConstraintLayout.LayoutParams
                params2.width = (constraintLayout.width / 2.05).toInt()
                params2.height = (constraintLayout.height / 2.03).toInt()
                params2.setMargins(5)
                params2.startToEnd = buttons[0].id
                params2.topToTop = PARENT_ID
                params2.bottomToTop = buttons[2].id
                params2.endToEnd = PARENT_ID

                val params3 = buttons[2].layoutParams as ConstraintLayout.LayoutParams
                params3.width = (constraintLayout.width / 2.05).toInt()
                params3.height = (constraintLayout.height / 2.03).toInt()
                params3.setMargins(5)
                params3.startToEnd = buttons[3].id
                params3.topToBottom = buttons[1].id
                params3.endToEnd = PARENT_ID
                params3.bottomToBottom = PARENT_ID

                val params4 = buttons[3].layoutParams as ConstraintLayout.LayoutParams
                buttons[3].rotation = 180f
                params4.width = (constraintLayout.width / 2.05).toInt()
                params4.height = (constraintLayout.height / 2.03).toInt()
                params4.setMargins(5)
                params4.startToStart = PARENT_ID
                params4.topToBottom = buttons[0].id
                params4.endToStart = buttons[2].id
                params4.bottomToBottom = PARENT_ID
            }
        }
    }


}
