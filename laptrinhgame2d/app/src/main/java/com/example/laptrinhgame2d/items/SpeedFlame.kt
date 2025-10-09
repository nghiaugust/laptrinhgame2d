package com.example.laptrinhgame2d.items

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import kotlin.math.abs

class SpeedFlame(
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
        y += kotlin.math.sin(animationFrame * 0.15f) * 0.8f
    }

    fun draw(canvas: Canvas) {
        if (isCollected) return

        val alpha = if (lifetime > maxLifetime - 300) {
            ((kotlin.math.sin(lifetime * 0.3f) + 1) * 127 + 128).toInt()
        } else {
            255
        }

        // Vẽ ngọn lửa với hình dạng thực tế
        drawFlameShape(canvas, alpha)

        // Vẽ particles xung quanh
        drawFlameParticles(canvas, alpha)
    }

    private fun drawFlameShape(canvas: Canvas, alpha: Int) {
        // Vẽ ngọn lửa chính với path
        val flamePath = Path().apply {
            moveTo(x, y + 25f)        // Bottom center
            cubicTo(x - 20f, y + 10f, x - 15f, y - 10f, x - 5f, y - 20f)  // Left curve
            cubicTo(x - 2f, y - 30f, x + 2f, y - 30f, x + 5f, y - 20f)    // Top curve
            cubicTo(x + 15f, y - 10f, x + 20f, y + 10f, x, y + 25f)       // Right curve
            close()
        }

        // Outer flame (red-orange)
        paint.color = Color.argb(alpha, 255, 69, 0)
        canvas.drawPath(flamePath, paint)

        // Middle flame (orange)
        val middlePath = Path().apply {
            moveTo(x, y + 15f)
            cubicTo(x - 15f, y + 5f, x - 10f, y - 5f, x - 3f, y - 15f)
            cubicTo(x - 1f, y - 20f, x + 1f, y - 20f, x + 3f, y - 15f)
            cubicTo(x + 10f, y - 5f, x + 15f, y + 5f, x, y + 15f)
            close()
        }
        paint.color = Color.argb(alpha, 255, 140, 0)
        canvas.drawPath(middlePath, paint)

        // Inner flame (yellow)
        val innerPath = Path().apply {
            moveTo(x, y + 8f)
            cubicTo(x - 8f, y, x - 5f, y - 8f, x - 2f, y - 12f)
            cubicTo(x - 1f, y - 15f, x + 1f, y - 15f, x + 2f, y - 12f)
            cubicTo(x + 5f, y - 8f, x + 8f, y, x, y + 8f)
            close()
        }
        paint.color = Color.argb(alpha, 255, 255, 100)
        canvas.drawPath(innerPath, paint)

        // Core (white-yellow)
        paint.color = Color.argb(alpha, 255, 255, 200)
        canvas.drawCircle(x, y + 2f, 6f, paint)
    }

    private fun drawFlameParticles(canvas: Canvas, alpha: Int) {
        // Vẽ các hạt lửa nhỏ bay xung quanh
        for (i in 0..5) {
            val angle = (i * 60f + animationFrame * 3f) * Math.PI / 180f
            val distance = 35f + kotlin.math.sin(animationFrame * 0.1f + i) * 5f
            val sparkX = x + kotlin.math.cos(angle).toFloat() * distance
            val sparkY = y + kotlin.math.sin(angle).toFloat() * distance

            val sparkAlpha = (alpha * 0.7f).toInt()
            paint.color = Color.argb(sparkAlpha, 255, 200, 50)
            canvas.drawCircle(sparkX, sparkY, 3f, paint)
        }

        // Vẽ smoke/heat waves phía trên
        for (i in 0..3) {
            val waveY = y - 35f - i * 8f
            val waveAlpha = (alpha * (0.3f - i * 0.05f)).toInt().coerceAtLeast(0)
            paint.color = Color.argb(waveAlpha, 200, 200, 200)

            val waveOffset = kotlin.math.sin(animationFrame * 0.15f + i) * 3f
            canvas.drawCircle(x + waveOffset, waveY, 4f - i, paint)
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