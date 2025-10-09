package com.example.laptrinhgame2d.items.skills

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import kotlin.math.sin

/**
 * Lightning Skill Item - Item kỹ năng tia sét
 */
class LightningSkillItem(
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
    
    // Frames
    private val frames = mutableListOf<Bitmap>()
    private val totalFrames = 5
    
    // Floating animation
    private var floatOffset = 0f
    private val floatSpeed = 0.05f
    private val floatAmplitude = 10f
    
    // Scale pulse effect
    private var pulseScale = 1f
    private val pulseSpeed = 0.03f
    
    // Size
    private val baseSize = 60f
    
    init {
        loadFrames()
    }
    
    private fun loadFrames() {
        try {
            for (i in 1..totalFrames) {
                val inputStream = context.assets.open("items/skills/lightning/$i.png")
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
        
        // Update animation frame
        animationTimer++
        if (animationTimer >= animationSpeed) {
            animationTimer = 0
            currentFrame = (currentFrame + 1) % totalFrames
        }
        
        // Update floating animation
        floatOffset = sin(lifetime * floatSpeed) * floatAmplitude
        
        // Update pulse scale
        pulseScale = 1f + sin(lifetime * pulseSpeed) * 0.1f
    }
    
    fun draw(canvas: Canvas) {
        if (isPickedUp) return
        if (frames.isEmpty()) return
        
        canvas.save()
        canvas.translate(x, y + floatOffset)
        
        val frame = frames[currentFrame]
        val size = baseSize * pulseScale
        
        canvas.drawBitmap(
            frame,
            null,
            android.graphics.RectF(
                -size / 2,
                -size / 2,
                size / 2,
                size / 2
            ),
            null
        )
        
        canvas.restore()
    }
    
    fun isCollidingWith(playerX: Float, playerY: Float, range: Float): Boolean {
        if (isPickedUp) return false
        
        val dx = x - playerX
        val dy = y - playerY
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)
        
        return distance <= range
    }
    
    fun pickup() {
        isPickedUp = true
    }
    
    fun isPickedUp(): Boolean = isPickedUp
    
    fun shouldBeRemoved(): Boolean = isPickedUp || lifetime >= maxLifetime
    
    fun getX(): Float = x
    fun getY(): Float = y
    
    fun cleanup() {
        frames.forEach { it.recycle() }
        frames.clear()
    }
}
