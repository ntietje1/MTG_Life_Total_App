package com.example.kotlinmtglifetotalapp.ui.lifecounter

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.withMatrix
import com.example.kotlinmtglifetotalapp.R

class PlayerButtonDrawer (private val playerButtonBase: PlayerButtonBase){

    private val context = playerButtonBase.context

    private val heartIcon = AppCompatResources.getDrawable(context, R.drawable.heart_solid_icon)?.toBitmap()!!
    private val heartIconLeft get() = centerX - heartIcon.width.toFloat() / 2
    private val heartIconTop get() = midLineY + paintLarge.descent() - playerButtonBase.height / 20

//    private val commanderIcon = AppCompatResources.getDrawable(context, R.drawable.commander_solid_icon)?.toBitmap()!!
//    private val commanderIconLeft get() = - playerButtonBase.height * 0.45f + playerButtonBase.width * 0.49f
//    private val commanderIconTop get() = playerButtonBase.height - playerButtonBase.width * 1f + playerButtonBase.height * 0.1f

    private val paintSmall: Paint
        get() = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = (playerButtonBase.height / 15f) + (playerButtonBase.width / 30f)
            textAlign = Paint.Align.CENTER
            color = Color.WHITE
        }

    private val paintLarge: Paint
        get() = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = (playerButtonBase.height / 5f) + (playerButtonBase.width / 4f)
            textAlign = Paint.Align.CENTER
            color = Color.WHITE
            typeface = context.resources.getFont(R.font.robotobold)
        }

    private val centerX: Float
        get() = playerButtonBase.width / 2f

    private val centerY: Float
        get() = playerButtonBase.height / 2f

    private val topLineY: Float
        get() = centerY - playerButtonBase.height * 0.1f - playerButtonBase.width / 10

    private val midLineY: Float
        get() = centerY * 0.675f + (paintLarge.descent() - paintLarge.ascent()) - playerButtonBase.width / 5.25f

    private val rotatedMatrix
        get(): Matrix {
            return Matrix().apply {
                setRotate(playerButtonBase.rotation - 90, centerX, centerY)
            }
        }

    fun draw(canvas: Canvas) {
        val player = playerButtonBase.player!!
        with(canvas) {
            save()
            rotate(playerButtonBase.rotation, centerX, centerY)
            withMatrix(rotatedMatrix) {
                if (player.recentChange != 0) {
                    var recentChangeString = if (player.recentChange > 0) "+" else ""
                    recentChangeString += player.recentChange.toString()
                    drawText(
                        recentChangeString,
                        centerX + paintLarge.measureText(player.life.toString()) / 2 + 100,
                        midLineY - 75,
                        paintSmall
                    )
                }
                drawText(player.toString(), centerX, topLineY, paintSmall)
                drawText(player.life.toString(), centerX, midLineY, paintLarge)

                drawBitmap(heartIcon, heartIconLeft, heartIconTop, paintSmall)
                //drawBitmap(commanderIcon, heartIconLeft, heartIconTop, paintSmall)
//                drawBitmap(commanderIcon, commanderIconLeft, commanderIconTop, paintSmall)
            }
            restore()
        }
    }
}