package com.example.laptrinhgame2d.heroes

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import kotlin.math.sqrt

class Arrow(
    private var x: Float,
    private var y: Float,
    private val direction: Float, // 1f = right, -1f = left
    private val bitmap: Bitmap,
    private val damage: Int
) {
    private val speed = 15f
    private var isActive = true
    private val maxDistance = 1000f
    private var travelDistance = 0f

    fun update() {
        if (!isActive) return

        x += speed * direction
        travelDistance += speed

        // Deactivate nếu bay quá xa
        if (travelDistance >= maxDistance) {
            isActive = false
        }
    }

    fun draw(canvas: Canvas) {
        if (!isActive) return

        if (direction < 0) {
            // Lật mũi tên khi bay sang trái
            val matrix = Matrix()
            matrix.preScale(-1f, 1f)
            val flippedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)
            canvas.drawBitmap(flippedBitmap, x - bitmap.width / 2, y - bitmap.height / 2 + 50f, null)
        } else {
            canvas.drawBitmap(bitmap, x - bitmap.width / 2, y - bitmap.height / 2 + 50f, null)
        }
    }

    fun isActive() = isActive
    fun getX() = x
    fun getY() = y
    fun getDamage() = damage

    fun deactivate() {
        isActive = false
    }

    // Kiểm tra va chạm với target
    fun checkCollision(targetX: Float, targetY: Float, range: Float): Boolean {
        if (!isActive) return false

        val dx = targetX - x
        val dy = targetY - y
        val distance = sqrt(dx * dx + dy * dy)
        return distance < range
    }
}