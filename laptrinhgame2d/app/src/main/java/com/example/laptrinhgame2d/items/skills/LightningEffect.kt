package com.example.laptrinhgame2d.items.skills

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas

/**
 * Lightning Effect - Hiệu ứng tia sét
 * Đánh 3 đợt cách nhau 0.5s, mỗi đợt 30 damage
 */
class LightningEffect(
    private val context: Context,
    private var targetX: Float,  // Vị trí quái (trên đầu)
    private var targetY: Float
) {
    private var isActive = true
    private var lifetime = 0
    
    // 3 đợt đánh, mỗi đợt 1 animation + 0.5s pause
    // Mỗi đợt: 5 frames * 6 = 30 frames animation + 30 frames pause = 60 frames
    // Tổng: 60 * 3 = 180 frames (3 giây)
    private val maxLifetime = 180  // 3 giây (60 FPS)
    
    // Damage
    private val damagePerStrike = 30
    private var strikesDealt = 0
    private val totalStrikes = 3
    
    // Strike timing (mỗi strike cách nhau 0.5s = 30 frames, nhưng thực tế là 60 frames bao gồm animation)
    private val strikeInterval = 60  // 1 giây (animation + pause)
    private var strikeTimer = 60  // Bắt đầu = 60 để trigger ngay lập tức
    private var damageDealtThisStrike = false  // Track xem đã deal damage cho strike hiện tại chưa
    
    // Animation
    private var currentFrame = 0
    private var animationTimer = 0
    private val animationSpeed = 6  // Đổi frame mỗi 6 frames
    private var animationFinished = false
    private var currentStrikeAnimating = false
    
    // Frames
    private val frames = mutableListOf<Bitmap>()
    private val totalFrames = 5
    
    // Size
    private val lightningHeight = 400f
    private val lightningWidth = 200f
    
    // Enemy được target
    private var targetEnemy: Any? = null
    
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
        if (!isActive) return
        
        lifetime++
        strikeTimer++
        
        // Hết thời gian
        if (lifetime >= maxLifetime) {
            isActive = false
            return
        }
        
        // Kiểm tra xem đã đến lúc strike mới chưa
        if (strikeTimer >= strikeInterval && strikesDealt < totalStrikes) {
            strikeTimer = 0
            currentStrikeAnimating = true
            currentFrame = 0
            animationTimer = 0
            animationFinished = false
            damageDealtThisStrike = false  // Reset flag cho strike mới
        }
        
        // Update animation nếu đang strike
        if (currentStrikeAnimating && !animationFinished) {
            animationTimer++
            if (animationTimer >= animationSpeed) {
                animationTimer = 0
                currentFrame++
                if (currentFrame >= totalFrames) {
                    currentFrame = totalFrames - 1
                    animationFinished = true
                    currentStrikeAnimating = false
                }
            }
        }
    }
    
    fun draw(canvas: Canvas) {
        if (!isActive) return
        if (frames.isEmpty()) return
        if (!currentStrikeAnimating) {
            // Không vẽ khi không đang strike
            return
        }
        
        canvas.save()
        // Vẽ lightning từ trên xuống, neo tại chân (bottom) của lightning ở vị trí đầu quái
        canvas.translate(targetX, targetY)
        
        val frame = frames[currentFrame]
        
        canvas.drawBitmap(
            frame,
            null,
            android.graphics.RectF(
                -lightningWidth / 2,
                -lightningHeight,  // Vẽ từ trên xuống, bottom tại targetY
                lightningWidth / 2,
                0f  // Bottom tại targetY (đầu quái)
            ),
            null
        )
        
        canvas.restore()
    }
    
    /**
     * Kiểm tra xem có nên deal damage không (mỗi khi animation bắt đầu)
     */
    fun shouldDealDamage(): Boolean {
        // Deal damage ngay khi bắt đầu strike mới
        if (currentStrikeAnimating && !damageDealtThisStrike) {
            damageDealtThisStrike = true
            strikesDealt++
            return true
        }
        return false
    }
    
    /**
     * Lấy damage
     */
    fun getDamage(): Int = damagePerStrike
    
    /**
     * Set target enemy để track vị trí
     */
    fun setTargetEnemy(enemy: Any) {
        targetEnemy = enemy
    }
    
    /**
     * Get target enemy
     */
    fun getTargetEnemy(): Any? = targetEnemy
    
    /**
     * Update target position (gọi từ GameView để update vị trí quái)
     */
    fun updateTargetPosition(x: Float, y: Float) {
        targetX = x
        targetY = y
    }
    
    fun isActive(): Boolean = isActive
    
    fun getTargetX(): Float = targetX
    fun getTargetY(): Float = targetY
    
    fun cleanup() {
        frames.forEach { it.recycle() }
        frames.clear()
    }
}
