package com.example.laptrinhgame2d

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import java.io.IOException
import kotlin.math.sqrt

class MedusaProjectile(
    private val context: Context,
    private var x: Float,
    private var y: Float,
    private val targetX: Float,
    private val targetY: Float,
    private val damage: Int
) {
    private val flyFrames = mutableListOf<Bitmap>()    // Stone1-2
    private val explodeFrames = mutableListOf<Bitmap>() // Stone3-8
    
    private var currentFrames = mutableListOf<Bitmap>()
    private var currentFrame = 0
    private var frameCounter = 0
    private val frameDelay = 2 // Nhanh hơn để mượt
    
    // Interpolation cho smooth movement
    private var previousX = x
    private var previousY = y
    private var interpolatedX = x
    private var interpolatedY = y
    private val interpolationAlpha = 0.7f
    
    private var velocityX = 0f
    private var velocityY = 0f
    private val speed = 9f
    
    private var isExploding = false
    private var isDead = false
    private var hasDealtDamage = false
    
    private val maxRange = 650f
    private var distanceTraveled = 0f
    private val startX = x
    private val startY = y
    
    init {
        loadFrames()
        currentFrames = flyFrames
        
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
            // Load fly frames (Stone1-2)
            for (i in 1..2) {
                val bitmap = loadBitmap("enemies/medusa/skill/Stone$i.png")
                flyFrames.add(bitmap)
            }
            
            // Load explode frames (Stone3-8)
            for (i in 3..8) {
                val bitmap = loadBitmap("enemies/medusa/skill/Stone$i.png")
                explodeFrames.add(bitmap)
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
        
        if (!isExploding) {
            // Lưu vị trí trước đó cho interpolation
            previousX = x
            previousY = y
            
            // Bay về phía target
            x += velocityX
            y += velocityY
            
            // Interpolation cho smooth movement
            interpolatedX = previousX + (x - previousX) * interpolationAlpha
            interpolatedY = previousY + (y - previousY) * interpolationAlpha
            
            // Tính khoảng cách đã bay
            val dx = x - startX
            val dy = y - startY
            distanceTraveled = sqrt(dx * dx + dy * dy)
            
            // Kiểm tra quá tầm → nổ
            if (distanceTraveled >= maxRange) {
                startExploding()
            }
            
            // Update fly animation (loop frames 1-2)
            frameCounter++
            if (frameCounter >= frameDelay) {
                frameCounter = 0
                currentFrame = (currentFrame + 1) % flyFrames.size
            }
        } else {
            // Exploding animation
            frameCounter++
            if (frameCounter >= frameDelay) {
                frameCounter = 0
                
                if (currentFrame < explodeFrames.size - 1) {
                    currentFrame++
                } else {
                    // Animation kết thúc
                    isDead = true
                }
            }
        }
    }
    
    fun draw(canvas: Canvas) {
        if (currentFrames.isEmpty()) return
        
        val bitmap = currentFrames[currentFrame]
        
        // Sử dụng interpolated position cho smooth rendering
        val drawX = if (isExploding) x else interpolatedX
        val drawY = if (isExploding) y else interpolatedY
        
        canvas.drawBitmap(bitmap, drawX - bitmap.width / 2, drawY - bitmap.height / 2, null)
    }
    
    fun startExploding() {
        if (isExploding) return
        
        isExploding = true
        currentFrames = explodeFrames
        currentFrame = 0
        frameCounter = 0
    }
    
    fun canDealDamage(): Boolean {
        // Có thể gây sát thương khi đang nổ và chưa gây damage
        return isExploding && !hasDealtDamage && currentFrame >= 0
    }
    
    fun markDamageDealt() {
        hasDealtDamage = true
    }
    
    fun getDamage(): Int = damage
    
    fun isCollidingWith(targetX: Float, targetY: Float, range: Float): Boolean {
        // Kiểm tra collision ngay cả khi chưa nổ (đang bay)
        val dx = targetX - x
        val dy = targetY - y
        val distance = sqrt(dx * dx + dy * dy)
        return distance < range
    }
    
    fun getX(): Float = x
    fun getY(): Float = y
    fun isDead(): Boolean = isDead
}
