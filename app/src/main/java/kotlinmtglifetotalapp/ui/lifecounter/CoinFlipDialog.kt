package kotlinmtglifetotalapp.ui.lifecounter

import SettingsDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kotlinmtglifetotalapp.R
import com.wajahatkarim.flippable.FlipAnimationType
import com.wajahatkarim.flippable.Flippable
import com.wajahatkarim.flippable.FlippableState
import com.wajahatkarim.flippable.rememberFlipController
import kotlin.random.Random

//class CoinFlipDialog : DialogFragment() {
//    private var _binding: CoinFlipLayoutBinding? = null
//    private val binding get() = _binding!!
//    private val gridLayout get() = binding.gridLayout
//
//    private val coinImage get() = binding.coin
//    private var curSide = R.drawable.heads
//    private var targetSide = R.drawable.heads
//    private val sides = listOf(R.drawable.heads, R.drawable.tails)
//    private val numFlips = 2
//    private val animationDuration = 200L
//
//    private val history = mutableStateListOf<String>()
//
//    private val animationInterpolator = LinearInterpolator()
//    private val headsToHeads
//        get() = Rotate3dAnimation(
//            coinImage,
//            R.drawable.heads,
//            R.drawable.tails,
//            0f,
//            180f,
//            0f,
//            0f,
//            0f,
//            0f
//        ).apply {
//            repeatCount = numFlips
//            duration = animationDuration
//            interpolator = animationInterpolator
//        }
//    private val headsToTails
//        get() = Rotate3dAnimation(
//            coinImage,
//            R.drawable.heads,
//            R.drawable.tails,
//            0f,
//            180f,
//            0f,
//            0f,
//            0f,
//            0f
//        ).apply {
//            repeatCount = numFlips
//            duration = animationDuration
//            interpolator = animationInterpolator
//        }
//    private val tailsToHeads
//        get() = Rotate3dAnimation(
//            coinImage,
//            R.drawable.tails,
//            R.drawable.heads,
//            0f,
//            180f,
//            0f,
//            0f,
//            0f,
//            0f
//        ).apply {
//            repeatCount = numFlips
//            duration = animationDuration
//            interpolator = animationInterpolator
//        }
//    private val tailsToTails
//        get() = Rotate3dAnimation(
//            coinImage,
//            R.drawable.tails,
//            R.drawable.heads,
//            0f,
//            180f,
//            0f,
//            0f,
//            0f,
//            0f
//        ).apply {
//            repeatCount = numFlips
//            duration = animationDuration
//            interpolator = animationInterpolator
//        }
//
//    private val flipAnimation
//        get() =
//            if (curSide == R.drawable.heads && targetSide == R.drawable.heads) {
//                headsToHeads
//            } else if (curSide == R.drawable.heads && targetSide == R.drawable.tails) {
//                headsToTails
//            } else if (curSide == R.drawable.tails && targetSide == R.drawable.tails) {
//                tailsToTails
//            } else {
//                tailsToHeads
//            }
//
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = CoinFlipLayoutBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
//
//        generateInvisibleGrid()
//
//        binding.composeView.apply {
//
//            setContent {
//                FlipHistory(history)
//            }
//        }
//
//        coinImage.isClickable = true
//        coinImage.setOnClickListener {
//            flip()
//        }
//    }
//
//    private fun flip() {
//        targetSide = sides.random()
//        coinImage.startAnimation(flipAnimation)
//        curSide = targetSide
//        if (targetSide == R.drawable.heads) {
//            addToHistory("H")
//            println("H")
//        } else {
//            addToHistory("T")
//            println("T")
//        }
//    }
//
//    private fun addToHistory(v: String) {
//        if (history.size >= 20) {
//            history.removeAt(0)
//        }
//        history.add(v)
//
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//
//    private fun generateInvisibleGrid() {
//        gridLayout.columnCount = 2
//        gridLayout.rowCount = 3
//        for (i in 1..6) {
//            val placeholderButton = SettingsButton(requireContext(), null).apply {
//                imageResource = R.drawable.one_icon
//                text = ""
//                visibility = View.INVISIBLE
//            }
//            gridLayout.addView(placeholderButton)
//
//        }
//    }
//}

@Composable
fun CoinFlipDialog(onDismiss: () -> Unit = {}) {
    val history = remember { mutableStateListOf<String>() }
    SettingsDialog(
        content = {
            Box(modifier = Modifier.fillMaxSize()) {
                CoinFlippable(history)
                FlipHistory(
                    history,
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 0.dp)
                )
            }
        },
        onDismiss = onDismiss
    )
}

@Composable
fun CoinFlippable(history: MutableList<String>) {
    val flipEnabled by remember { mutableStateOf(true) }
    val initialDuration = 275
    var duration by remember { mutableIntStateOf(initialDuration) }
    var flipAnimationType by remember { mutableStateOf(FlipAnimationType.VERTICAL_ANTI_CLOCKWISE) }
    val totalFlips = 3 // + 1
    var flipCount by remember { mutableIntStateOf(totalFlips) }
    val flipController = rememberFlipController()

    fun addToHistory(v: String) {
        if (history.size > 17) {
            history.removeAt(0)
        }
        history.add(v)
    }

    fun decrementFlipCount() {
        flipCount--
        duration += 75
    }

    fun resetCount() {
        flipCount = totalFlips
        duration = initialDuration
    }

    fun continueFlip(currentSide: FlippableState) {
        if (flipCount == totalFlips) {
            if (Random.nextBoolean()) {
                decrementFlipCount()
            }
        }
        if (flipCount > 0) {
            flipAnimationType =
                if (flipAnimationType == FlipAnimationType.VERTICAL_ANTI_CLOCKWISE) {
                    FlipAnimationType.VERTICAL_CLOCKWISE
                } else {
                    FlipAnimationType.VERTICAL_ANTI_CLOCKWISE
                }
            flipController.flip()
            decrementFlipCount()
        } else {
            addToHistory(if (currentSide == FlippableState.FRONT) "H" else "T")
            resetCount()
        }
    }

    Flippable(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 50.dp, top = 0.dp, start = 30.dp, end = 30.dp),
        flipController = flipController,
        flipDurationMs = duration,
        flipEnabled = flipEnabled,
        flipAnimationType = flipAnimationType,
        frontSide = {
            Image(
                painter = painterResource(id = R.drawable.heads),
                contentDescription = "Front Side",
                modifier = Modifier.fillMaxSize()
            )
        },
        backSide = {
            Image(
                painter = painterResource(id = R.drawable.tails),
                contentDescription = "Back Side",
                modifier = Modifier.fillMaxSize()
            )
        },
        onFlippedListener = { currentSide ->
            continueFlip(currentSide)
        }
    )

}

@Composable
fun FlipHistory(coinFlipHistory: MutableList<String>, modifier: Modifier = Modifier) {
    val hPadding = 10.dp
    val vPadding = 5.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
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
                .height(35.dp)
                .clip(RoundedCornerShape(0.dp)),
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
                        .padding(vertical = 0.dp, horizontal = hPadding)
                )

            }
        }
    }
}