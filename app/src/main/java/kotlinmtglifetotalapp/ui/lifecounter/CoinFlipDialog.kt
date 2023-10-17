package kotlinmtglifetotalapp.ui.lifecounter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.fragment.app.DialogFragment
import com.example.kotlinmtglifetotalapp.R
import com.example.kotlinmtglifetotalapp.databinding.CoinFlipLayoutBinding
import kotlinmtglifetotalapp.utils.Rotate3dAnimation

// TODO: FIX SAME SIDE FLIPPING
class CoinFlipDialog : DialogFragment() {
    private var _binding: CoinFlipLayoutBinding? = null
    private val binding get() = _binding!!

    private val coinImage get() = binding.coin
    private var curSide = R.drawable.heads
    private var targetSide = R.drawable.heads
    private val sides = listOf(R.drawable.heads, R.drawable.tails)
    private val numFlips = 3
    private val headsToHeads get() = Rotate3dAnimation(
        coinImage,
        R.drawable.heads,
        R.drawable.heads,
        0f,
        180f,
        0f,
        0f,
        0f,
        0f
    ).apply {
        repeatCount = numFlips
        duration = 120
        interpolator = LinearInterpolator()
    }
    private val headsToTails get() = Rotate3dAnimation(
        coinImage,
        R.drawable.heads,
        R.drawable.tails,
        0f,
        180f,
        0f,
        0f,
        0f,
        0f
    ).apply {
        repeatCount = numFlips + 1
        duration = 120
        interpolator = LinearInterpolator()
    }
    private val tailsToHeads get() = Rotate3dAnimation(
        coinImage,
        R.drawable.tails,
        R.drawable.heads,
        0f,
        180f,
        0f,
        0f,
        0f,
        0f
    ).apply {
        repeatCount = numFlips + 1
        duration = 120
        interpolator = LinearInterpolator()
    }
    private val tailsToTails get() = Rotate3dAnimation(
        coinImage,
        R.drawable.tails,
        R.drawable.tails,
        0f,
        180f,
        0f,
        0f,
        0f,
        0f
    ).apply {
        repeatCount = numFlips
        duration = 120
        interpolator = LinearInterpolator()
    }

    private val flipAnimation
        get() =
            if (curSide == R.drawable.heads && targetSide == R.drawable.heads) {
                headsToHeads
            } else if (curSide == R.drawable.heads && targetSide == R.drawable.tails) {
                headsToTails
            } else if (curSide == R.drawable.tails && targetSide == R.drawable.tails) {
                tailsToTails
            } else {
                tailsToHeads
            }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CoinFlipLayoutBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        coinImage.isClickable = true
        coinImage.setOnClickListener {
            flip()
        }
    }

    private fun flip() {
        targetSide = sides.random()
        coinImage.startAnimation(flipAnimation)
        curSide = targetSide
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}