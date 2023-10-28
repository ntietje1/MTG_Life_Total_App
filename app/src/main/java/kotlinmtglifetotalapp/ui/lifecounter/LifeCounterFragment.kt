package kotlinmtglifetotalapp.ui.lifecounter

import MiddleButtonDialog
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.kotlinmtglifetotalapp.R
import com.example.kotlinmtglifetotalapp.databinding.LifeCounterLayoutBinding
import kotlinmtglifetotalapp.ui.lifecounter.playerButton.Player
import kotlinmtglifetotalapp.ui.lifecounter.playerButton.PlayerButton
import kotlinmtglifetotalapp.ui.lifecounter.playerButton.PlayerButtonBase
import kotlinmtglifetotalapp.utils.RotateLayout


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
        get() = when (numPlayers) {
            2 -> 0.0f
            3 -> 0.12f
            4 -> 0.0f
            5 -> -0.124f
            6 -> -0.166f
            else -> throw IllegalArgumentException("invalid number of players")
        }

    private val middleButton
        get() = ImageButton(context).apply {
            setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.middle_solid_icon))
            background =
                AppCompatResources.getDrawable(context, R.drawable.circular_background_black)

            stateListAnimator = null
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
//                setMargins(20, 20, 20, 20)

            }
            setPadding(10, 10, 10, 10)
            setOnClickListener {
                //goToPlayerSelect(this)
                //toggleMiddleMenu()


                toggleImageViewVis()
                val fragment = MiddleButtonDialog()
                fragment.show(
                    this@LifeCounterFragment.childFragmentManager, "middle_button_fragment_tag"
                )
            }

            val screenHeight = resources.displayMetrics.heightPixels
            val middleButtonY = (screenHeight * middlePos).toInt()
            val middleButtonLayoutParams = this.layoutParams as FrameLayout.LayoutParams
            middleButtonLayoutParams.topMargin = middleButtonY
            this.layoutParams = middleButtonLayoutParams

        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
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
            playerButton.buttonBase.player = Player.currentPlayers[i]
        }

        println("on create")

        val imageView = binding.imageView

        imageView.setRenderEffect(
            RenderEffect.createBlurEffect(
                15.0f, 15.0f, Shader.TileMode.CLAMP
            )
        )

        return root
    }

    init {
    }


    override fun onResume() {
        super.onResume()
        generateButtons()
        placeButtons()
        println("on resume")
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
            requireContext(), PlayerButtonBase(requireContext(), null)
        )
    ): PlayerButton {

        val rotateLayout = RotateLayout(context)
        rotateLayout.addView(playerButton)
        rotateLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f
        )

        playerButton.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT
        )

        return playerButton

    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        removeAllViews()

        packBundle(outState)
    }

    private fun packBundle(outState: Bundle) {
        Player.packBundle(outState)
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
        fLayout.addView(middleButton)

    }


    private fun setImageView() {
        val fLayout = binding.frameLayout
        val imageView = binding.imageView
        val screenshot = Bitmap.createBitmap(fLayout.width, fLayout.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(screenshot)

        fLayout.draw(canvas)
        imageView.setImageBitmap(screenshot)
    }

    internal fun toggleImageViewVis() {
        setImageView()
        val imageView = binding.imageView
        imageView.visibility = when (imageView.visibility) {
            VISIBLE -> GONE
            GONE -> VISIBLE
            else -> VISIBLE
        }
    }

    internal fun resetPlayers() {
        for (player in Player.currentPlayers) {
            player.resetPlayer()
        }
    }

    internal fun goToPlayerSelect() {
        val bundle = Bundle()
        packBundle(bundle)
        Navigation.findNavController(binding.linearLayout).navigate(R.id.navigation_home, bundle)
    }

    internal fun setPlayerNum(numPlayers: Int) {
        resetPlayers()
        println("WAS ${Player.currentPlayers.size} Players")
        while (Player.currentPlayers.size > numPlayers) {
            Player.currentPlayers.removeLast()
            println("minus 1 player")
        }
        while (Player.currentPlayers.size < numPlayers) {
            Player.generatePlayer()
            println("plus 1 player")
        }

        println("NOW ${Player.currentPlayers.size} Players")
        val bundle = Bundle()
        packBundle(bundle)
        Navigation.findNavController(binding.linearLayout).navigate(R.id.navigation_life_counter, bundle)
    }


}