package com.example.laptrinhgame2d.items

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import kotlin.math.abs

class DamageBoost(
    private val context: Context,
    private var x: Float,
    private var y: Float
) {

    private var isCollected = false
    private var lifetime = 0
    private val maxLifetime = 1800 // 30 seconds at 60fps
    private var animationFrame = 0

    private val paint = Paint().apply {
        isAntiAlias = true
    }

    fun update() {
        if (isCollected) return

        lifetime++
        animationFrame++

        // Float animation
        y += kotlin.math.sin(animationFrame * 0.12f) * 0.7f
    }

    fun draw(canvas: Canvas) {
        if (isCollected) return

        val alpha = if (lifetime > maxLifetime - 300) {
            ((kotlin.math.sin(lifetime * 0.3f) + 1) * 127 + 128).toInt()
        } else {
            255
        }

        // Rotation effect
        val rotation = animationFrame * 2f

        // Draw crossed swords
        drawCrossedSwords(canvas, alpha, rotation)

        // Draw glow effect
        drawSwordGlow(canvas, alpha)
    }

    private fun drawCrossedSwords(canvas: Canvas, alpha: Int, rotation: Float) {
        canvas.save()
        canvas.rotate(rotation, x, y)

        // Draw first sword (diagonal /)
        drawSword(canvas, alpha, x, y, 45f, Color.argb(alpha, 192, 57, 43)) // Dark red

        // Draw second sword (diagonal \)
        drawSword(canvas, alpha, x, y, -45f, Color.argb(alpha, 231, 76, 60)) // Bright red

        canvas.restore()

        // Draw center gem/orb
        paint.color = Color.argb(alpha, 241, 196, 15) // Gold
        canvas.drawCircle(x, y, 8f, paint)

        paint.color = Color.argb(alpha, 255, 255, 255) // White highlight
        canvas.drawCircle(x - 2f, y - 2f, 3f, paint)
    }

    private fun drawSword(canvas: Canvas, alpha: Int, centerX: Float, centerY: Float, angle: Float, bladeColor: Int) {
        canvas.save()
        canvas.rotate(angle, centerX, centerY)

        val swordLength = 30f
        val bladeWidth = 8f
        val handleLength = 12f

        // Draw blade
        paint.color = bladeColor
        val bladePath = Path().apply {
            // Blade tip
            moveTo(centerX, centerY - swordLength)
            lineTo(centerX - bladeWidth/2, centerY - swordLength + 8f)
            lineTo(centerX - bladeWidth/2, centerY - handleLength/2)
            lineTo(centerX + bladeWidth/2, centerY - handleLength/2)
            lineTo(centerX + bladeWidth/2, centerY - swordLength + 8f)
            close()
        }
        canvas.drawPath(bladePath, paint)

        // Draw blade highlight
        paint.color = Color.argb(alpha, 255, 255, 255)
        canvas.drawLine(
            centerX - 2f, centerY - swordLength + 5f,
            centerX - 2f, centerY - handleLength/2 + 2f,
            paint.apply { strokeWidth = 2f }
        )

        // Draw handle
        paint.color = Color.argb(alpha, 101, 67, 33) // Brown
        canvas.drawRect(
            centerX - 4f, centerY - handleLength/2,
            centerX + 4f, centerY + handleLength/2,
            paint
        )

        // Draw crossguard
        paint.color = Color.argb(alpha, 149, 165, 166) // Gray
        canvas.drawRect(
            centerX - 12f, centerY - handleLength/2 - 2f,
            centerX + 12f, centerY - handleLength/2 + 2f,
            paint
        )

        // Draw pommel
        paint.color = Color.argb(alpha, 52, 73, 94) // Dark gray
        canvas.drawCircle(centerX, centerY + handleLength/2 + 3f, 5f, paint)

        canvas.restore()
    }

    private fun drawSwordGlow(canvas: Canvas, alpha: Int) {
        // Red glow effect around swords
        paint.color = Color.argb(alpha / 5, 231, 76, 60)
        for (i in 1..4) {
            canvas.drawCircle(x, y, 25f + i * 6f, paint)
        }

        // Sparks effect
        for (i in 0..6) {
            val angle = (i * 51.4f + animationFrame * 3f) * Math.PI / 180f
            val distance = 40f + kotlin.math.sin(animationFrame * 0.1f + i) * 8f
            val sparkX = x + kotlin.math.cos(angle).toFloat() * distance
            val sparkY = y + kotlin.math.sin(angle).toFloat() * distance

            paint.color = Color.argb(alpha / 3, 255, 215, 0) // Gold sparks
            canvas.drawCircle(sparkX, sparkY, 2f, paint)
        }
    }

    fun isCollidingWith(otherX: Float, otherY: Float, range: Float): Boolean {
        val dx = x - otherX
        val dy = y - otherY
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)
        return distance <= range
    }

    fun collect() {
        isCollected = true
    }

    fun isCollected(): Boolean = isCollected
    fun shouldBeRemoved(): Boolean = isCollected || lifetime >= maxLifetime
    fun getX(): Float = x
    fun getY(): Float = y
}