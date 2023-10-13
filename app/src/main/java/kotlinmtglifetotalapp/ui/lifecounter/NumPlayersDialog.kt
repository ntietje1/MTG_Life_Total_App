package kotlinmtglifetotalapp.ui.lifecounter

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.kotlinmtglifetotalapp.R
import com.example.kotlinmtglifetotalapp.databinding.MiddleButtonLayoutBinding

class NumPlayersDialog: DialogFragment() {
    private var _binding: MiddleButtonLayoutBinding? = null
    private val binding get() = _binding!!

    private val middleMenu get() = binding.middleMenu

    private val button1 get() = SettingsButton(requireContext(), null).apply {
        imageResource = R.drawable.one_icon
        text = ""
        setOnClickListener {
            println("CHANGE PLAYER NUM")
            (parentFragment as LifeCounterFragment).setPlayerNum(1)
        }
    }

    private val button2 get() = SettingsButton(requireContext(), null).apply {
        imageResource = R.drawable.two_icon
        text = ""
        setOnClickListener {
            println("CHANGE PLAYER NUM")
            (parentFragment as LifeCounterFragment).setPlayerNum(2)
        }
    }

    private val button3 get() = SettingsButton(requireContext(), null).apply {
        imageResource = R.drawable.three_icon
        text = ""
        setOnClickListener {
            println("CHANGE PLAYER NUM")
            (parentFragment as LifeCounterFragment).setPlayerNum(3)
        }
    }

    private val button4 get() = SettingsButton(requireContext(), null).apply {
        imageResource = R.drawable.four_icon
        text = ""
        setOnClickListener {
            println("CHANGE PLAYER NUM")
            (parentFragment as LifeCounterFragment).setPlayerNum(4)
        }
    }

    private val button5 get() = SettingsButton(requireContext(), null).apply {
        imageResource = R.drawable.five_icon
        text = ""
        setOnClickListener {
            println("CHANGE PLAYER NUM")
            (parentFragment as LifeCounterFragment).setPlayerNum(5)
        }
    }

    private val button6 get() = SettingsButton(requireContext(), null).apply {
        imageResource = R.drawable.six_icon
        text = ""
        setOnClickListener {
            println("CHANGE PLAYER NUM")
            (parentFragment as LifeCounterFragment).setPlayerNum(6)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MiddleButtonLayoutBinding.inflate(inflater, container, false)
        middleMenu.columnCount = 2
        middleMenu.rowCount = 3
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        addButtons()
    }

    private fun addButtons() {
        middleMenu.addView(button1)
        middleMenu.addView(button2)
        middleMenu.addView(button3)
        middleMenu.addView(button4)
        middleMenu.addView(button5)
        middleMenu.addView(button6)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        with(parentFragment as LifeCounterFragment) {
            toggleImageViewVis()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}