package com.example.laptrinhgame2d.items.skills

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Black Hole Effect - Hiệu ứng hố đen triệu hồi từ skill
 * Hút quái vào và gây damage theo thời gian
 */
class BlackHoleEffect(
    private var x: Float,
    private var y: Float
) {
    private var isActive = true
    private var lifetime = 0
    private val maxLifetime = 300  // 5 giây (60 FPS)
    
    // Damage - Gây 4 damage mỗi 0.2 giây để gián đoạn quái
    private val damagePerTick = 4
    private var damageTimer = 0
    private val damageInterval = 12  // 0.2 giây (12 frames @ 60 FPS)
    
    // Range
    private val pullRange = 600f  // Phạm vi hút
    private val coreSize = 80f    // Kích thước tâm hố đen
    
    // Animation
    private var rotationAngle = 0f
    private var pulseScale = 1f
    
    // Paint
    private val blackHolePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    
    private val pullRangePaint = Paint().apply {
        color = Color.argb(30, 150, 100, 255)
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    
    private val edgePaint = Paint().apply {
        color = Color.argb(150, 200, 100, 255)
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }
    
    private val spiralPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        isAntiAlias = true
    }
    
    // Spiral particles
    private data class SpiralParticle(
        var angle: Float,
        var distance: Float,
        var speed: Float,
        var size: Float
    )
    private val spiralParticles = mutableListOf<SpiralParticle>()
    
    init {
        // Tạo spiral particles
        for (i in 0 until 30) {
            spiralParticles.add(
                SpiralParticle(
                    angle = i * 12f,
                    distance = pullRange * 0.8f - (i % 5) * 50f,
                    speed = 3f + (i % 3) * 1f,
                    size = 4f + (i % 3) * 2f
                )
            )
        }
    }
    
    fun update() {
        if (!isActive) return
        
        lifetime++
        damageTimer++
        
        // Hết thời gian
        if (lifetime >= maxLifetime) {
            isActive = false
            return
        }
        
        // Reset damage timer
        if (damageTimer >= damageInterval) {
            damageTimer = 0
        }
        
        // Animation
        rotationAngle += 3f
        if (rotationAngle >= 360f) rotationAngle = 0f
        
        pulseScale = 1f + sin(lifetime * 0.15f) * 0.1f
        
        // Update spiral particles - hút vào tâm
        spiralParticles.forEach { particle ->
            particle.angle += particle.speed
            if (particle.angle >= 360f) particle.angle -= 360f
            
            // Hút dần vào tâm
            particle.distance -= particle.speed * 0.5f
            
            // Reset khi đến tâm
            if (particle.distance < coreSize) {
                particle.distance = pullRange * 0.9f
            }
        }
    }
    
    fun draw(canvas: Canvas) {
        if (!isActive) return
        
        canvas.save()
        canvas.translate(x, y)
        
        // Vẽ phạm vi hút (mờ)
        canvas.drawCircle(0f, 0f, pullRange, pullRangePaint)
        
        // Vẽ spiral particles
        drawSpiralParticles(canvas)
        
        // Vẽ vòng tròn viền
        edgePaint.strokeWidth = 4f + sin(lifetime * 0.1f) * 2f
        canvas.drawCircle(0f, 0f, pullRange, edgePaint)
        
        // Vẽ hố đen chính
        canvas.save()
        canvas.scale(pulseScale, pulseScale)
        drawBlackHoleCore(canvas)
        canvas.restore()
        
        // Vẽ rotating rings
        drawRotatingRings(canvas)
        
        canvas.restore()
    }
    
    private fun drawBlackHoleCore(canvas: Canvas) {
        // Vẽ nhiều lớp gradient
        for (i in 5 downTo 1) {
            val radius = coreSize * i / 5f
            val gradient = RadialGradient(
                0f, 0f, radius,
                intArrayOf(
                    Color.argb(255, 20, 0, 40),      // Đen tím ở giữa
                    Color.argb(255, 60, 20, 100),    // Tím đậm
                    Color.argb(200, 120, 60, 180),   // Tím
                    Color.argb(150, 180, 100, 230),  // Tím sáng
                    Color.argb(0, 200, 150, 255)     // Trong suốt
                ),
                floatArrayOf(0f, 0.2f, 0.5f, 0.8f, 1f),
                Shader.TileMode.CLAMP
            )
            blackHolePaint.shader = gradient
            canvas.drawCircle(0f, 0f, radius, blackHolePaint)
        }
        
        // Tâm đen tuyền
        blackHolePaint.shader = null
        blackHolePaint.color = Color.BLACK
        canvas.drawCircle(0f, 0f, coreSize * 0.2f, blackHolePaint)
    }
    
    private fun drawRotatingRings(canvas: Canvas) {
        for (i in 0 until 4) {
            val angle = rotationAngle + i * 90f
            val radius = coreSize * 1.5f + i * 15f
            
            canvas.save()
            canvas.rotate(angle)
            
            spiralPaint.color = Color.argb(120 - i * 20, 200, 100, 255)
            spiralPaint.strokeWidth = 4f - i * 0.5f
            
            canvas.drawArc(
                -radius, -radius, radius, radius,
                0f, 90f, false, spiralPaint
            )
            
            canvas.restore()
        }
    }
    
    private fun drawSpiralParticles(canvas: Canvas) {
        spiralParticles.forEach { particle ->
            val rad = Math.toRadians(particle.angle.toDouble())
            val px = (cos(rad) * particle.distance).toFloat()
            val py = (sin(rad) * particle.distance).toFloat()
            
            // Màu và độ sáng phụ thuộc vào khoảng cách
            val alpha = (200 * (1f - particle.distance / pullRange)).toInt().coerceIn(50, 255)
            spiralPaint.color = Color.argb(alpha, 220, 120, 255)
            spiralPaint.style = Paint.Style.FILL
            
            canvas.drawCircle(px, py, particle.size, spiralPaint)
            spiralPaint.style = Paint.Style.STROKE
        }
    }
    
    /**
     * Kiểm tra xem enemy có trong phạm vi damage không
     */
    fun isInRange(enemyX: Float, enemyY: Float): Boolean {
        val dx = enemyX - x
        val dy = enemyY - y
        val distance = sqrt(dx * dx + dy * dy)
        return distance <= pullRange
    }
    
    /**
     * Kiểm tra xem đã đến lúc gây damage chưa
     */
    fun shouldDealDamage(): Boolean {
        return damageTimer == 0
    }
    
    /**
     * Lấy damage để gây lên enemy
     */
    fun getDamage(): Int = damagePerTick
    
    fun isActive(): Boolean = isActive
    
    fun getX(): Float = x
    fun getY(): Float = y
    fun getRange(): Float = pullRange
}
