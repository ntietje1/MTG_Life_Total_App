import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.example.kotlinmtglifetotalapp.R
import com.example.kotlinmtglifetotalapp.databinding.MiddleButtonLayoutBinding
import kotlinmtglifetotalapp.ui.lifecounter.CoinFlipDialog
import kotlinmtglifetotalapp.ui.lifecounter.LifeCounterFragment
import kotlinmtglifetotalapp.ui.lifecounter.NumPlayersDialog
import kotlinmtglifetotalapp.ui.lifecounter.SettingsButton

/**
 * TODO: implement these features in settings
 *
 *
 * dice
 *
 *
 */
class MiddleButtonDialog : DialogFragment() {

    private var _binding: MiddleButtonLayoutBinding? = null
    private val binding get() = _binding!!

    private val middleMenu get() = binding.middleMenu
    private val parentFrag get() = parentFragment as LifeCounterFragment

    private val playerSelectButton get() = SettingsButton(requireContext()).apply {
        imageResource = R.drawable.player_select_icon
        text = "Player Select"
        setOnClickListener {
            (parentFragment as LifeCounterFragment).goToPlayerSelect()
        }
    }

    private val resetButton get() = SettingsButton(requireContext()).apply {
        imageResource = R.drawable.reset_icon
        text = "Reset game"
        setOnClickListener {
            parentFrag.resetPlayers()
            dismiss()
        }
    }

    private val changeNumPlayersButton get() = SettingsButton(requireContext()).apply {
        imageResource = R.drawable.player_count_icon
        text = "Player number"
        setOnClickListener {
            val fragment = NumPlayersDialog()
            fragment.show(
                parentFrag.childFragmentManager, "num_players_dialog_tag"
            )
        }
    }

    private val diceRollButton get() = SettingsButton(requireContext()).apply {
        imageResource = R.drawable.six_icon
        text = "Dice roll"
        setOnClickListener {

        }
    }

    private val coinFlipButton get() = SettingsButton(requireContext(), null).apply {
        imageResource = R.drawable.coin_icon
        text = "Coin flip"
        setOnClickListener {
            val fragment = CoinFlipDialog()

//            val width = this@MiddleButtonDialog.view?.width ?: 0
//            val height = this@MiddleButtonDialog.view?.height ?: 0
            val width = 2000
            val height = 4000

            // Set the dimensions of the CoinFlipDialog
            val params = WindowManager.LayoutParams()
            params.width = width
            params.height = height
            fragment.dialog?.window?.attributes = params

            //fragment.dialog?.window?.attributes = this@MiddleButtonDialog.dialog?.window?.attributes


            fragment.show(
                parentFrag.childFragmentManager, "coin_flip_dialog_tag"
            )
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
        middleMenu.addView(playerSelectButton)
        middleMenu.addView(resetButton)
        middleMenu.addView(changeNumPlayersButton)
        middleMenu.addView(diceRollButton)
        middleMenu.addView(coinFlipButton)
        for (i in 0 until 1) {
            middleMenu.addView(SettingsButton(requireContext(), null))
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        parentFrag.toggleImageViewVis()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
