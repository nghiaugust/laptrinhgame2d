package com.example.laptrinhgame2d.items.skills

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import kotlin.math.sqrt

/**
 * Bomb Effect - Hiệu ứng bom nổ
 * Gây 50 damage ban đầu, sau đó 5 damage mỗi 0.5s trong 3s
 */
class BombEffect(
    private val context: Context,
    private var x: Float,
    private var y: Float
) {
    private var isActive = true
    private var lifetime = 0
    private val maxLifetime = 180  // 3 giây (60 FPS)
    
    // Damage
    private val initialDamage = 50
    private val dotDamage = 5  // Damage over time
    private var initialDamageDealt = false
    private var dotTimer = 0
    private val dotInterval = 30  // 0.5 giây (30 frames @ 60 FPS)
    
    // Range
    private val damageRange = 350f
    
    // Animation
    private var currentFrame = 0
    private var animationTimer = 0
    private val animationSpeed = 6  // Đổi frame mỗi 6 frames
    private var animationFinished = false
    
    // Frames
    private val frames = mutableListOf<Bitmap>()
    private val totalFrames = 10
    
    // Size
    private val explosionSize = 1000f  // Phạm vi 500 = radius, nên đường kính 700
    
    // Track enemies hit for DOT
    private val enemiesHit = mutableSetOf<Any>()
    
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
        if (!isActive) return
        
        lifetime++
        dotTimer++
        
        // Hết thời gian
        if (lifetime >= maxLifetime) {
            isActive = false
            return
        }
        
        // Update animation frame (chỉ chạy 1 lần qua tất cả frames)
        if (!animationFinished) {
            animationTimer++
            if (animationTimer >= animationSpeed) {
                animationTimer = 0
                currentFrame++
                if (currentFrame >= totalFrames) {
                    currentFrame = totalFrames - 1  // Giữ ở frame cuối
                    animationFinished = true
                }
            }
        }
    }
    
    fun draw(canvas: Canvas) {
        if (!isActive) return
        if (frames.isEmpty()) return
        
        canvas.save()
        canvas.translate(x, y)
        
        // Vẽ explosion sprite
        val frame = frames[currentFrame]
        
        canvas.drawBitmap(
            frame,
            null,
            android.graphics.RectF(
                -explosionSize / 2,
                -explosionSize / 2,
                explosionSize / 2,
                explosionSize / 2
            ),
            null
        )
        
        canvas.restore()
    }
    
    /**
     * Kiểm tra xem enemy có trong phạm vi damage không
     */
    fun isInRange(enemyX: Float, enemyY: Float): Boolean {
        val dx = enemyX - x
        val dy = enemyY - y
        val distance = sqrt(dx * dx + dy * dy)
        return distance <= damageRange
    }
    
    /**
     * Lấy initial damage (chỉ deal 1 lần)
     */
    fun getInitialDamage(): Int {
        return if (!initialDamageDealt) {
            initialDamageDealt = true
            initialDamage
        } else {
            0
        }
    }
    
    /**
     * Kiểm tra xem đã đến lúc gây DOT damage chưa
     */
    fun shouldDealDotDamage(): Boolean {
        // Deal DOT sau khi initial damage đã xong
        if (!initialDamageDealt) return false
        
        if (dotTimer >= dotInterval) {
            dotTimer = 0
            return true
        }
        return false
    }
    
    /**
     * Lấy DOT damage
     */
    fun getDotDamage(): Int = dotDamage
    
    /**
     * Track enemy đã bị đánh
     */
    fun addHitEnemy(enemy: Any) {
        enemiesHit.add(enemy)
    }
    
    /**
     * Kiểm tra enemy đã bị đánh chưa
     */
    fun hasHitEnemy(enemy: Any): Boolean {
        return enemiesHit.contains(enemy)
    }
    
    fun isActive(): Boolean = isActive
    
    fun getX(): Float = x
    fun getY(): Float = y
    fun getRange(): Float = damageRange
    
    fun cleanup() {
        frames.forEach { it.recycle() }
        frames.clear()
        enemiesHit.clear()
    }
}
