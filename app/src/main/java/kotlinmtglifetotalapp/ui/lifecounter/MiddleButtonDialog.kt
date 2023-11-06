import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.kotlinmtglifetotalapp.R
import com.example.kotlinmtglifetotalapp.databinding.MiddleButtonLayoutBinding
import kotlinmtglifetotalapp.ui.lifecounter.CoinFlipDialog
import kotlinmtglifetotalapp.ui.lifecounter.LifeCounterFragment
import kotlinmtglifetotalapp.ui.lifecounter.NumPlayersDialog
import kotlinmtglifetotalapp.ui.lifecounter.SettingsButton
import kotlinmtglifetotalapp.ui.lifecounter.RoundedCornerDrawable
import kotlinmtglifetotalapp.ui.lifecounter.StartingLifeDialog

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

            fragment.show(
                parentFrag.childFragmentManager, "coin_flip_dialog_tag"
            )
        }
    }

    private val changeStartingLifeButton get() = SettingsButton(requireContext()).apply {
        imageResource = R.drawable.fourty_icon
        text = "Starting Life"
        setOnClickListener {
            val fragment = StartingLifeDialog()

            fragment.show(
                parentFrag.childFragmentManager, "starting_life_dialog_tag"
            )
        }
    }

    private val bground get() = RoundedCornerDrawable.create(requireContext()).apply {
            backgroundColor = Color.DKGRAY
//            backgroundAlpha = 0
        rippleDrawable.alpha = 0
//        rippleDrawable.setColor(ColorStateList.valueOf(Color.DKGRAY))
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

//        val background = RoundedCornerDrawable.create(requireContext()).apply {
////            backgroundColor = Color.DKGRAY
////            backgroundAlpha = 0
//            rippleDrawable.alpha = 0
//            rippleDrawable.setColor(ColorStateList.valueOf(Color.DKGRAY))
//        }


        dialog?.window?.setBackgroundDrawable(bground)

        addButtons()
    }

    private fun addButtons() {
        middleMenu.addView(playerSelectButton)
        middleMenu.addView(resetButton)
        middleMenu.addView(changeNumPlayersButton)
        middleMenu.addView(diceRollButton)
        middleMenu.addView(coinFlipButton)
        middleMenu.addView(changeStartingLifeButton)
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
