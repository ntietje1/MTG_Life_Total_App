package kotlinmtglifetotalapp.ui.lifecounter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.DialogFragment
import com.example.kotlinmtglifetotalapp.R
import com.example.kotlinmtglifetotalapp.databinding.StartingLifeLayoutBinding
import kotlinmtglifetotalapp.ui.lifecounter.playerButton.Player

class StartingLifeDialog : DialogFragment() {
    private var _binding: StartingLifeLayoutBinding? = null
    private val binding get() = _binding!!
    private val gridLayout get() = binding.gridLayout
    private val gridLayout2 get() = binding.gridLayout2
    private val parentFrag get() = parentFragment as LifeCounterFragment

    private fun dismissMiddleButtonDialog() {
        val middleButtonDialog = parentFrag.childFragmentManager.findFragmentByTag("middle_button_fragment_tag")
        if (middleButtonDialog != null) {
            (middleButtonDialog as DialogFragment).dismiss()
        }
    }

    private val fourtyButton get() = SettingsButton(requireContext()).apply {
        imageResource = R.drawable.forty_icon
        text = "fourty"
        setOnClickListener {
            Player.startingLife = 40
            parentFrag.resetPlayers()
            dismiss()
            dismissMiddleButtonDialog()
        }
    }

    private val thirtyButton get() = SettingsButton(requireContext()).apply {
        imageResource = R.drawable.thirty_icon
        text = "thirty"
        setOnClickListener {
            Player.startingLife = 30
            parentFrag.resetPlayers()
            dismiss()
            dismissMiddleButtonDialog()
        }
    }

    private val twentyButton get() = SettingsButton(requireContext()).apply {
        imageResource = R.drawable.twenty_icon
        text = "twenty"
        setOnClickListener {
            Player.startingLife = 20
            parentFrag.resetPlayers()
            dismiss()
            dismissMiddleButtonDialog()
        }
    }

    private val customButton get() = SettingsButton(requireContext()).apply {
        imageResource = R.drawable.placeholder_icon
        text = "custom"
        setOnClickListener {
            Player.startingLife = -1
            parentFrag.resetPlayers()
            dismiss()
            dismissMiddleButtonDialog()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = StartingLifeLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        generateInvisibleGrid()

        gridLayout2.columnCount = 2
        gridLayout2.rowCount = 3
        gridLayout2.addView(twentyButton)
        gridLayout2.addView(thirtyButton)
        gridLayout2.addView(fourtyButton)
        gridLayout2.addView(customButton)
        gridLayout2.addView(ComposeView(requireContext()).apply{
            setContent {
                SettingsButton()
            }
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun generateInvisibleGrid() {
        gridLayout.columnCount = 2
        gridLayout.rowCount = 3
        for (i in 1..6) {
            val placeholderButton = SettingsButton(requireContext(), null).apply {
                imageResource = R.drawable.one_icon
                text = ""
                visibility = View.INVISIBLE
            }
            gridLayout.addView(placeholderButton)
        }
    }
}


