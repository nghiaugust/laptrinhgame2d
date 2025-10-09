package com.example.laptrinhgame2d.items.skills

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Item skill Bomb - Vật phẩm rơi ra khi đánh quái
 * Nhặt được sẽ unlock skill ném bom
 */
class BombSkillItem(
    private val context: Context,
    private var x: Float,
    private var y: Float
) {
    private var isPickedUp = false
    private var lifetime = 0
    private val maxLifetime = 900  // 15 giây (60 FPS)
    
    // Animation
    private var currentFrame = 0
    private var animationTimer = 0
    private val animationSpeed = 8  // Đổi frame mỗi 8 frames
    private var floatOffset = 0f
    private var pulseScale = 1f
    
    // Frames
    private val frames = mutableListOf<Bitmap>()
    private val totalFrames = 10
    
    // Size
    private val baseSize = 60f
    
    init {
        loadFrames()
    }
    
    private fun loadFrames() {
        try {
            for (i in 1..totalFrames) {
                val inputStream = context.assets.open("items/skills/boom1/$i.png")
                val bitmap = BitmapFactory.decodeStream(inputStream)
                frames.add(bitmap)
                inputStream.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun update() {
        if (isPickedUp) return
        
        lifetime++
        
        // Animation float lên xuống
        floatOffset = sin(lifetime * 0.05f) * 10f
        
        // Pulse effect
        pulseScale = 1f + sin(lifetime * 0.1f) * 0.15f
        
        // Update animation frame
        animationTimer++
        if (animationTimer >= animationSpeed) {
            animationTimer = 0
            currentFrame = (currentFrame + 1) % totalFrames
        }
    }
    
    fun draw(canvas: Canvas) {
        if (isPickedUp) return
        if (frames.isEmpty()) return
        
        canvas.save()
        canvas.translate(x, y + floatOffset)
        canvas.scale(pulseScale, pulseScale)
        
        // Vẽ bomb sprite
        val frame = frames[currentFrame]
        val scaledWidth = baseSize * 2
        val scaledHeight = baseSize * 2
        
        canvas.drawBitmap(
            frame,
            null,
            android.graphics.RectF(
                -scaledWidth / 2,
                -scaledHeight / 2,
                scaledWidth / 2,
                scaledHeight / 2
            ),
            null
        )
        
        canvas.restore()
    }
    
    fun isCollidingWith(targetX: Float, targetY: Float, range: Float): Boolean {
        if (isPickedUp) return false
        
        val dx = targetX - x
        val dy = targetY - y
        val distance = sqrt(dx * dx + dy * dy)
        return distance < range + baseSize
    }
    
    fun pickup() {
        isPickedUp = true
    }
    
    fun isPickedUp(): Boolean = isPickedUp
    
    fun shouldBeRemoved(): Boolean {
        return isPickedUp || lifetime >= maxLifetime
    }
    
    fun getX(): Float = x
    fun getY(): Float = y
    
    fun cleanup() {
        frames.forEach { it.recycle() }
        frames.clear()
    }
}
