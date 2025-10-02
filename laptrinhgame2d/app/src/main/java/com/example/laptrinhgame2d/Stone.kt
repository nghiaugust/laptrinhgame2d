package com.example.laptrinhgame2d

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import java.io.IOException

class Stone(
    private val context: Context,
    private var x: Float, // Tâm của stone (giống Medusa)
    private var y: Float, // Tâm của stone (giống Medusa)
    private val velocityX: Float,
    private val velocityY: Float,
    private val damage: Int
) {
    private val flyingFrames = mutableListOf<Bitmap>() // Stone1-2
    private val breakFrames = mutableListOf<Bitmap>()  // Stone3-8
    
    private var currentFrames = mutableListOf<Bitmap>()
    private var currentFrame = 0
    private var frameCounter = 0
    private val frameDelay = 5
    
    private var isBreaking = false
    private var isDead = false
    private var hasDealtDamage = false
    
    private val width = 50f
    private val height = 50f
    
    init {
        loadFrames()
        currentFrames = flyingFrames
    }
    
    private fun loadFrames() {
        try {
            // Load flying frames (Stone1-2)
            for (i in 1..2) {
                val bitmap = loadBitmap("enemies/medusa/skill/Stone$i.png")
                flyingFrames.add(bitmap)
            }
            
            // Load break frames (Stone3-8)
            for (i in 3..8) {
                val bitmap = loadBitmap("enemies/medusa/skill/Stone$i.png")
                breakFrames.add(bitmap)
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
        
        if (!isBreaking) {
            // Di chuyển khi đang bay
            x += velocityX
            y += velocityY
            
            // Animate flying
            frameCounter++
            if (frameCounter >= frameDelay) {
                frameCounter = 0
                currentFrame = (currentFrame + 1) % currentFrames.size
            }
        } else {
            // Animate breaking
            frameCounter++
            if (frameCounter >= frameDelay) {
                frameCounter = 0
                currentFrame++
                if (currentFrame >= currentFrames.size) {
                    isDead = true
                }
            }
        }
    }
    
    fun draw(canvas: Canvas, cameraX: Float, cameraY: Float) {
        if (isDead || currentFrames.isEmpty()) return
        
        val frame = currentFrames[currentFrame]
        val matrix = Matrix()
        
        val scaledWidth = width * 2
        val scaledHeight = height * 2
        
        matrix.postScale(scaledWidth / frame.width, scaledHeight / frame.height)
        // Vẽ với tâm tại (x, y) - giống Medusa
        matrix.postTranslate(x - scaledWidth / 2 - cameraX, y - scaledHeight / 2 - cameraY)
        
        canvas.drawBitmap(frame, matrix, null)
    }
    
    fun startBreaking() {
        if (!isBreaking) {
            isBreaking = true
            currentFrames = breakFrames
            currentFrame = 0
            frameCounter = 0
        }
    }
    
    fun getX(): Float = x // Tâm của stone
    fun getY(): Float = y // Tâm của stone
    fun getWidth(): Float = width
    fun getHeight(): Float = height
    fun isDead(): Boolean = isDead
    fun isBreaking(): Boolean = isBreaking
    fun getDamage(): Int = damage
    fun hasDealtDamage(): Boolean = hasDealtDamage
    fun setDamageDealt() { hasDealtDamage = true }
    
    // Kiểm tra va chạm với nhân vật
    fun checkCollision(targetX: Float, targetY: Float, targetWidth: Float, targetHeight: Float): Boolean {
        if (isBreaking || isDead || hasDealtDamage) return false
        
        // Stone có tâm tại (x, y)
        val stoneLeft = x - width / 2
        val stoneRight = x + width / 2
        val stoneTop = y - height / 2
        val stoneBottom = y + height / 2
        
        val targetLeft = targetX - targetWidth / 2
        val targetRight = targetX + targetWidth / 2
        val targetTop = targetY - targetHeight / 2
        val targetBottom = targetY + targetHeight / 2
        
        return stoneLeft < targetRight && stoneRight > targetLeft &&
               stoneTop < targetBottom && stoneBottom > targetTop
    }
}
