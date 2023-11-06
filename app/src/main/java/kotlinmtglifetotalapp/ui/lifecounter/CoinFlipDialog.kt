package kotlinmtglifetotalapp.ui.lifecounter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import androidx.compose.foundation.background

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.DialogFragment
import com.example.kotlinmtglifetotalapp.R
import com.example.kotlinmtglifetotalapp.databinding.CoinFlipLayoutBinding
import kotlinmtglifetotalapp.utils.Rotate3dAnimation

class CoinFlipDialog : DialogFragment() {
    private var _binding: CoinFlipLayoutBinding? = null
    private val binding get() = _binding!!
    private val gridLayout get() = binding.gridLayout

    private val coinImage get() = binding.coin
    private var curSide = R.drawable.heads
    private var targetSide = R.drawable.heads
    private val sides = listOf(R.drawable.heads, R.drawable.tails)
    private val numFlips = 2
    private val animationDuration = 200L

    private val history = mutableStateListOf<String>()

    private val animationInterpolator = LinearInterpolator()
    private val headsToHeads get() = Rotate3dAnimation(
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
        repeatCount = numFlips
        duration = animationDuration
        interpolator = animationInterpolator
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
        repeatCount = numFlips
        duration = animationDuration
        interpolator = animationInterpolator
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
        repeatCount = numFlips
        duration = animationDuration
        interpolator = animationInterpolator
    }
    private val tailsToTails get() = Rotate3dAnimation(
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
        repeatCount = numFlips
        duration = animationDuration
        interpolator = animationInterpolator
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

        generateInvisibleGrid()

        binding.composeView.apply {

            setContent {
                FlipHistory(history)
            }
        }

        coinImage.isClickable = true
        coinImage.setOnClickListener {
            flip()
        }
    }

    private fun flip() {
        targetSide = sides.random()
        coinImage.startAnimation(flipAnimation)
        curSide = targetSide
        if (targetSide == R.drawable.heads) {
            addToHistory("H")
            println("H")
        } else {
            addToHistory("T")
            println("T")
        }
    }

    private fun addToHistory(v: String) {
        if (history.size >= 20) {
            history.removeAt(0)
        }
        history.add(v)

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

@Composable
fun FlipHistory(coinFlipHistory: MutableList<String>) {
    val hPadding = 5.dp
    val vPadding = 5.dp

    Column() {
        Text(
            text = "Flip History",
            color = Color.White, // Set the text color to white or another contrasting color
            fontWeight = FontWeight.Bold,
            style = TextStyle(fontSize = 20.sp),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = vPadding)
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(30.dp)),
            color = Color(0x60, 0x60, 0x60)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = vPadding)
            ) {

                Text(
                    text = buildAnnotatedString {
                        coinFlipHistory.forEach { result ->
                            withStyle(style = SpanStyle(color = if (result == "H") Color.Green else Color.Red)) {
                                append("$result ")
                            }
                        }
                    },
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(fontSize = 16.sp),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = vPadding)
                )

            }
        }
    }
}