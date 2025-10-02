package com.example.laptrinhgame2d

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix

class SkillProjectile(
    private var x: Float,
    private var y: Float,
    private val direction: Float, // 1f = right, -1f = left
    private val skillFrames: List<Bitmap>,
    private val damage: Int,
    private val yOffset: Float = 0f // Offset cho vị trí Y khi vẽ
) {
    private val speed = 12f
    private var isActive = true
    private val maxDistance = 800f
    private var travelDistance = 0f
    
    private var currentFrame = 0
    private var frameCounter = 0
    private val frameDelay = 3 // Animation nhanh hơn arrow
    
    fun update() {
        if (!isActive) return
        
        // Di chuyển
        x += speed * direction
        travelDistance += speed
        
        // Update animation
        frameCounter++
        if (frameCounter >= frameDelay) {
            frameCounter = 0
            currentFrame++
            if (currentFrame >= skillFrames.size) {
                currentFrame = 0 // Loop animation
            }
        }
        
        // Deactivate nếu bay quá xa
        if (travelDistance >= maxDistance) {
            isActive = false
        }
    }
    
    fun draw(canvas: Canvas) {
        if (!isActive || skillFrames.isEmpty()) return
        
        val bitmap = skillFrames[currentFrame]
        
        if (direction < 0) {
            // Lật skill khi bay sang trái
            val matrix = Matrix()
            matrix.preScale(-1f, 1f)
            val flippedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)
            canvas.drawBitmap(flippedBitmap, x - bitmap.width / 2, y - bitmap.height / 2 + yOffset, null)
        } else {
            canvas.drawBitmap(bitmap, x - bitmap.width / 2, y - bitmap.height / 2 + yOffset, null)
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
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)
        return distance < range
    }
}
