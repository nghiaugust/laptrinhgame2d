package com.example.laptrinhgame2d.enemies

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import java.io.IOException
import kotlin.math.sqrt

class DragonFire(
    private val context: Context,
    private var x: Float,
    private var y: Float,
    private val targetX: Float,
    private val targetY: Float,
    private val damage: Int,
    private val facingRight: Boolean // Hướng của rồng khi phun lửa
) {
    private val fireFrames = mutableListOf<Bitmap>()

    private var currentFrame = 0
    private var frameCounter = 0
    private val frameDelay = 2

    private var velocityX = 0f
    private var velocityY = 0f
    private val speed = 7f // Chậm hơn vì tầm ngắn

    private var isDead = false
    private var hasDealtDamage = false

    private val maxRange = 250f // Tầm ngắn - chỉ bay 250 pixels
    private var distanceTraveled = 0f
    private val startX = x
    private val startY = y

    init {
        loadFrames()

        // Tính velocity bay về phía target
        val dx = targetX - x
        val dy = targetY - y
        val distance = sqrt(dx * dx + dy * dy)

        if (distance > 0) {
            velocityX = (dx / distance) * speed
            velocityY = (dy / distance) * speed
        }
    }

    private fun loadFrames() {
        try {
            // Load fire frames (Fire_Attack1-6) - animation lửa bay
            for (i in 1..6) {
                val bitmap = loadBitmap("enemies/dragon/skill/Fire_Attack$i.png")
                fireFrames.add(bitmap)
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun loadBitmap(path: String): Bitmap {
        return try {
            val inputStream = context.assets.open(path)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: IOException) {
            e.printStackTrace()
            Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888)
        }
    }

    fun update() {
        if (isDead) return

        // Bay về phía target
        x += velocityX
        y += velocityY

        // Tính khoảng cách đã bay
        val dx = x - startX
        val dy = y - startY
        distanceTraveled = sqrt(dx * dx + dy * dy)

        // Kiểm tra quá tầm → biến mất
        if (distanceTraveled >= maxRange) {
            isDead = true
            return
        }

        // Update animation (loop liên tục)
        frameCounter++
        if (frameCounter >= frameDelay) {
            frameCounter = 0
            currentFrame = (currentFrame + 1) % fireFrames.size
        }
    }

    fun draw(canvas: Canvas) {
        if (fireFrames.isEmpty() || isDead) return

        val bitmap = fireFrames[currentFrame]

        // Flip ảnh khi rồng quay trái (vì frame mặc định hướng phải)
        if (!facingRight) {
            val matrix = Matrix()
            matrix.preScale(-1f, 1f)
            val flippedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)
            canvas.drawBitmap(flippedBitmap, x - bitmap.width / 2, y - bitmap.height / 2, null)
        } else {
            canvas.drawBitmap(bitmap, x - bitmap.width / 2, y - bitmap.height / 2, null)
        }
    }

    fun canDealDamage(): Boolean {
        // Có thể gây sát thương khi chưa chết và chưa gây damage
        return !isDead && !hasDealtDamage
    }

    fun markDamageDealt() {
        hasDealtDamage = true
        isDead = true // Biến mất sau khi gây damage
    }

    fun getDamage(): Int = damage

    fun isCollidingWith(targetX: Float, targetY: Float, range: Float): Boolean {
        if (isDead) return false

        val dx = targetX - x
        val dy = targetY - y
        val distance = sqrt(dx * dx + dy * dy)
        return distance < range
    }

    fun getX(): Float = x
    fun getY(): Float = y
    fun isDead(): Boolean = isDead
}