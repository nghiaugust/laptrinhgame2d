package com.example.laptrinhgame2d.enemies

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import java.io.IOException
import kotlin.math.atan2
import kotlin.math.sqrt

class JinnProjectile(
    private val context: Context,
    private var x: Float,
    private var y: Float,
    private val targetX: Float,
    private val targetY: Float,
    private val damage: Int
) {
    private val flyFrames = mutableListOf<Bitmap>()
    private val explodeFrames = mutableListOf<Bitmap>()

    private var currentFrames = mutableListOf<Bitmap>()
    private var currentFrame = 0
    private var frameCounter = 0
    private val frameDelay = 2 // Giảm từ 3 xuống 2 để projectile bay mượt hơn

    private var isExploding = false
    private var isFinished = false
    private var hasPlayedFlyAnimation = false // Đánh dấu đã chạy hết animation bay

    private val speed = 8f
    private val maxRange = 600f
    private var distanceTraveled = 0f

    private val dirX: Float
    private val dirY: Float

    // Interpolation variables for smooth movement
    private var previousX: Float
    private var previousY: Float
    private var interpolatedX: Float
    private var interpolatedY: Float

    private var hasDealtDamage = false

    init {
        // Tính hướng bay
        val dx = targetX - x
        val dy = targetY - y
        val distance = sqrt(dx * dx + dy * dy)

        dirX = if (distance > 0) dx / distance else 1f
        dirY = if (distance > 0) dy / distance else 0f

        // Khởi tạo interpolation
        previousX = x
        previousY = y
        interpolatedX = x
        interpolatedY = y

        loadFrames()
        currentFrames = flyFrames
    }

    private fun loadFrames() {
        try {
            // Load fly frames (1-8: Magic_Attack1-8)
            for (i in 1..8) {
                val bitmap = loadBitmap("enemies/jinn/skill/Magic_Attack$i.png")
                flyFrames.add(bitmap)
            }

            // Load explode frames (9-13: Magic_Attack9-13)
            for (i in 9..13) {
                val bitmap = loadBitmap("enemies/jinn/skill/Magic_Attack$i.png")
                explodeFrames.add(bitmap)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun loadBitmap(path: String): Bitmap {
        val inputStream = context.assets.open(path)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        // Scale nhỏ hơn một chút cho projectile
        return Bitmap.createScaledBitmap(bitmap, bitmap.width * 2, bitmap.height * 2, false)
    }

    fun update(playerX: Float, playerY: Float) {
        if (isFinished) return

        if (!isExploding) {
            // Lưu vị trí cũ cho interpolation
            previousX = x
            previousY = y

            // Di chuyển
            x += dirX * speed
            y += dirY * speed
            distanceTraveled += speed

            // Linear interpolation để làm mượt (alpha = 0.7 cho smooth movement)
            val alpha = 0.7f
            interpolatedX = previousX + (x - previousX) * alpha
            interpolatedY = previousY + (y - previousY) * alpha

            // Kiểm tra va chạm với player (dùng vị trí thực, không phải interpolated)
            val dx = playerX - x
            val dy = playerY - y
            val distanceToPlayer = sqrt(dx * dx + dy * dy)

            if (distanceToPlayer < 50f) {
                explode()
            } else if (distanceTraveled >= maxRange) {
                // Quá tầm bay
                explode()
            }
        }

        // Update animation
        updateAnimation()
    }

    private fun explode() {
        if (isExploding) return

        isExploding = true
        currentFrames = explodeFrames
        currentFrame = 0
        frameCounter = 0
    }

    private fun updateAnimation() {
        frameCounter++

        if (frameCounter >= frameDelay) {
            frameCounter = 0
            currentFrame++

            if (currentFrame >= currentFrames.size) {
                if (isExploding) {
                    // Kết thúc animation nổ
                    isFinished = true
                } else {
                    // Animation bay: chỉ chạy 1 lần, sau đó giữ ở frame cuối (frame 7 - index của frame 8)
                    currentFrame = currentFrames.size - 1
                    hasPlayedFlyAnimation = true
                }
            }
        }
    }

    fun draw(canvas: Canvas) {
        if (currentFrames.isEmpty() || isFinished) return

        val bitmap = currentFrames[currentFrame]

        // Sử dụng vị trí interpolated để vẽ (mượt hơn)
        val drawX = if (isExploding) x else interpolatedX
        val drawY = if (isExploding) y else interpolatedY

        // Xoay projectile theo hướng bay
        val matrix = Matrix()
        val angle = Math.toDegrees(atan2(dirY.toDouble(), dirX.toDouble())).toFloat()
        matrix.postRotate(angle, bitmap.width / 2f, bitmap.height / 2f)
        matrix.postTranslate(drawX - bitmap.width / 2, drawY - bitmap.height / 2)

        canvas.drawBitmap(bitmap, matrix, null)
    }

    fun isFinished() = isFinished

    fun isExploding() = isExploding

    fun getX() = x
    fun getY() = y
    fun getDamage() = damage

    fun canDealDamage(): Boolean {
        // Chỉ gây damage 1 lần duy nhất trong toàn bộ explosion animation
        return isExploding && !hasDealtDamage && currentFrame >= 0
    }

    fun markDamageDealt() {
        hasDealtDamage = true
    }

    // Kiểm tra va chạm với player
    fun isCollidingWith(otherX: Float, otherY: Float, range: Float = 80f): Boolean {
        if (!isExploding) return false

        val dx = otherX - x
        val dy = otherY - y
        val distance = sqrt(dx * dx + dy * dy)
        return distance < range
    }
}