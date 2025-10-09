package com.example.laptrinhgame2d.items

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import kotlin.math.abs

class HealthHeart(
    private val context: Context,
    private var x: Float,
    private var y: Float,
    private val healAmount: Int = 25  // Tăng từ 20 lên 25
) {

    private var isCollected = false
    private var lifetime = 0
    private val maxLifetime = 1800 // 30 seconds
    private var animationFrame = 0

    private val paint = Paint().apply {
        isAntiAlias = true
    }

    fun update() {
        if (isCollected) return

        lifetime++
        animationFrame++

        // Floating animation
        y += kotlin.math.sin(animationFrame * 0.1f) * 0.6f
    }

    fun draw(canvas: Canvas) {
        if (isCollected) return

        val alpha = if (lifetime > maxLifetime - 300) {
            ((kotlin.math.sin(lifetime * 0.3f) + 1) * 127 + 128).toInt()
        } else {
            255
        }

        // Scale effect (beating heart)
        val scale = 1f + kotlin.math.sin(animationFrame * 0.2f) * 0.1f

        drawHeartShape(canvas, alpha, scale)
        drawHeartGlow(canvas, alpha, scale)
    }

    private fun drawHeartShape(canvas: Canvas, alpha: Int, scale: Float) {
        val size = 20f * scale

        // Vẽ trái tim bằng path
        val heartPath = Path().apply {
            // Bắt đầu từ đỉnh dưới
            moveTo(x, y + size * 0.7f)

            // Nửa trái
            cubicTo(
                x - size * 0.8f, y + size * 0.2f,
                x - size * 0.8f, y - size * 0.3f,
                x - size * 0.3f, y - size * 0.3f
            )
            cubicTo(
                x - size * 0.1f, y - size * 0.5f,
                x, y - size * 0.3f,
                x, y
            )

            // Nửa phải
            cubicTo(
                x, y - size * 0.3f,
                x + size * 0.1f, y - size * 0.5f,
                x + size * 0.3f, y - size * 0.3f
            )
            cubicTo(
                x + size * 0.8f, y - size * 0.3f,
                x + size * 0.8f, y + size * 0.2f,
                x, y + size * 0.7f
            )
            close()
        }

        // Outer heart (dark red)
        paint.color = Color.argb(alpha, 180, 20, 20)
        paint.style = Paint.Style.FILL
        canvas.drawPath(heartPath, paint)

        // Main heart (bright red)
        paint.color = Color.argb(alpha, 231, 76, 60)
        val innerSize = size * 0.85f
        val innerHeartPath = Path().apply {
            moveTo(x, y + innerSize * 0.7f)
            cubicTo(
                x - innerSize * 0.8f, y + innerSize * 0.2f,
                x - innerSize * 0.8f, y - innerSize * 0.3f,
                x - innerSize * 0.3f, y - innerSize * 0.3f
            )
            cubicTo(
                x - innerSize * 0.1f, y - innerSize * 0.5f,
                x, y - innerSize * 0.3f,
                x, y
            )
            cubicTo(
                x, y - innerSize * 0.3f,
                x + innerSize * 0.1f, y - innerSize * 0.5f,
                x + innerSize * 0.3f, y - innerSize * 0.3f
            )
            cubicTo(
                x + innerSize * 0.8f, y - innerSize * 0.3f,
                x + innerSize * 0.8f, y + innerSize * 0.2f,
                x, y + innerSize * 0.7f
            )
            close()
        }
        canvas.drawPath(innerHeartPath, paint)

        // Highlight
        paint.color = Color.argb(alpha, 255, 150, 150)
        val highlightSize = size * 0.4f
        canvas.drawCircle(x - size * 0.2f, y - size * 0.1f, highlightSize, paint)

        // Cross symbol (medical)
        paint.color = Color.argb(alpha, 255, 255, 255)
        paint.strokeWidth = 3f * scale
        canvas.drawLine(x, y - size * 0.15f, x, y + size * 0.15f, paint)
        canvas.drawLine(x - size * 0.15f, y, x + size * 0.15f, y, paint)
    }

    private fun drawHeartGlow(canvas: Canvas, alpha: Int, scale: Float) {
        // Glow effect
        paint.style = Paint.Style.FILL
        paint.color = Color.argb(alpha / 6, 231, 76, 60)
        for (i in 1..4) {
            canvas.drawCircle(x, y, (25f + i * 8f) * scale, paint)
        }

        // Sparkles around heart
        for (i in 0..3) {
            val angle = (i * 90f + animationFrame * 2f) * Math.PI / 180f
            val distance = 35f * scale
            val sparkleX = x + kotlin.math.cos(angle).toFloat() * distance
            val sparkleY = y + kotlin.math.sin(angle).toFloat() * distance

            paint.color = Color.argb(alpha / 2, 255, 200, 200)
            canvas.drawCircle(sparkleX, sparkleY, 3f * scale, paint)
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
    fun getHealAmount(): Int = healAmount
    fun getX(): Float = x
    fun getY(): Float = y
}