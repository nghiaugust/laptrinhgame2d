package com.example.laptrinhgame2d.items.skills

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import kotlin.math.cos
import kotlin.math.sin

/**
 * Shield Effect - Hiệu ứng khiên bảo vệ xoay quanh nhân vật
 * Chặn tối đa 100 damage trong 10 giây
 */
class ShieldEffect(
    private var ownerX: Float,
    private var ownerY: Float
) {
    private var isActive = true
    private var lifetime = 0
    private val maxLifetime = 600  // 10 giây (60 FPS)
    
    // Shield stats
    private var remainingHealth = 100  // Chặn được 100 damage
    private val maxHealth = 100
    
    // Animation
    private var rotationAngle = 0f
    private var pulseScale = 1f
    
    // Size
    private val shieldRadius = 120f  // Bán kính khiên quanh nhân vật
    
    // Paint
    private val shieldPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    
    private val edgePaint = Paint().apply {
        color = Color.argb(200, 100, 200, 255)
        style = Paint.Style.STROKE
        strokeWidth = 5f
        isAntiAlias = true
    }
    
    private val particlePaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    
    private val healthBarPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }
    
    // Shield segments (6 phần xoay quanh)
    private data class ShieldSegment(
        var angle: Float,
        var size: Float,
        var alpha: Int
    )
    private val segments = mutableListOf<ShieldSegment>()
    
    // Energy particles
    private data class EnergyParticle(
        var angle: Float,
        var distance: Float,
        var speed: Float,
        var size: Float
    )
    private val energyParticles = mutableListOf<EnergyParticle>()
    
    init {
        // Tạo 6 shield segments
        for (i in 0 until 6) {
            segments.add(
                ShieldSegment(
                    angle = i * 60f,
                    size = 50f,
                    alpha = 150
                )
            )
        }
        
        // Tạo energy particles
        for (i in 0 until 24) {
            energyParticles.add(
                EnergyParticle(
                    angle = i * 15f,
                    distance = shieldRadius,
                    speed = 2f + (i % 3) * 0.5f,
                    size = 2f + (i % 2) * 1f
                )
            )
        }
    }
    
    fun update(newOwnerX: Float, newOwnerY: Float) {
        if (!isActive) return
        
        // Cập nhật vị trí theo nhân vật
        ownerX = newOwnerX
        ownerY = newOwnerY
        
        lifetime++
        
        // Hết thời gian hoặc hết máu
        if (lifetime >= maxLifetime || remainingHealth <= 0) {
            isActive = false
            return
        }
        
        // Animation
        rotationAngle += 2f
        if (rotationAngle >= 360f) rotationAngle = 0f
        
        pulseScale = 1f + sin(lifetime * 0.08f) * 0.05f
        
        // Update segments
        segments.forEach { segment ->
            segment.angle += 0.5f
            if (segment.angle >= 360f) segment.angle -= 360f
            
            // Alpha thay đổi theo health
            segment.alpha = (150 * (remainingHealth.toFloat() / maxHealth)).toInt().coerceIn(50, 200)
        }
        
        // Update energy particles
        energyParticles.forEach { particle ->
            particle.angle += particle.speed
            if (particle.angle >= 360f) particle.angle -= 360f
            
            // Distance thay đổi nhẹ
            particle.distance = shieldRadius + sin(lifetime * 0.1f + particle.angle) * 10f
        }
    }
    
    fun draw(canvas: Canvas) {
        if (!isActive) return
        
        canvas.save()
        canvas.translate(ownerX, ownerY)
        
        // Vẽ energy particles
        drawEnergyParticles(canvas)
        
        // Vẽ shield segments
        canvas.save()
        canvas.scale(pulseScale, pulseScale)
        drawShieldSegments(canvas)
        canvas.restore()
        
        // Vẽ vòng tròn viền
        drawEdgeCircle(canvas)
        
        // Vẽ health bar
        drawHealthBar(canvas)
        
        canvas.restore()
    }
    
    private fun drawShieldSegments(canvas: Canvas) {
        segments.forEach { segment ->
            val rad = Math.toRadians(segment.angle.toDouble())
            val x = (cos(rad) * shieldRadius * 0.8f).toFloat()
            val y = (sin(rad) * shieldRadius * 0.8f).toFloat()
            
            canvas.save()
            canvas.translate(x, y)
            canvas.rotate(segment.angle)
            
            // Vẽ gradient shield piece
            val gradient = RadialGradient(
                0f, 0f, segment.size,
                intArrayOf(
                    Color.argb(segment.alpha, 120, 200, 255),
                    Color.argb(segment.alpha / 2, 80, 160, 230),
                    Color.argb(0, 50, 120, 200)
                ),
                floatArrayOf(0f, 0.6f, 1f),
                Shader.TileMode.CLAMP
            )
            shieldPaint.shader = gradient
            canvas.drawCircle(0f, 0f, segment.size, shieldPaint)
            
            // Vẽ hình hexagon nhỏ ở giữa
            shieldPaint.shader = null
            shieldPaint.color = Color.argb(segment.alpha + 50, 150, 220, 255)
            drawHexagon(canvas, 20f)
            
            canvas.restore()
        }
    }
    
    private fun drawHexagon(canvas: Canvas, radius: Float) {
        val path = android.graphics.Path()
        for (i in 0 until 6) {
            val angle = Math.toRadians((i * 60).toDouble())
            val x = (cos(angle) * radius).toFloat()
            val y = (sin(angle) * radius).toFloat()
            
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        path.close()
        
        canvas.drawPath(path, shieldPaint)
    }
    
    private fun drawEdgeCircle(canvas: Canvas) {
        // Vẽ vòng tròn viền gradient
        val healthRatio = remainingHealth.toFloat() / maxHealth
        
        // Màu thay đổi theo health
        val r = (100 + (1f - healthRatio) * 100).toInt()
        val g = (200 * healthRatio).toInt()
        val b = 255
        
        edgePaint.color = Color.argb(200, r, g, b)
        edgePaint.strokeWidth = 5f + sin(lifetime * 0.15f) * 2f
        
        canvas.drawCircle(0f, 0f, shieldRadius, edgePaint)
        
        // Vẽ vòng tròn trong
        edgePaint.strokeWidth = 3f
        edgePaint.color = Color.argb(100, r, g, b)
        canvas.drawCircle(0f, 0f, shieldRadius * 0.85f, edgePaint)
    }
    
    private fun drawEnergyParticles(canvas: Canvas) {
        energyParticles.forEach { particle ->
            val rad = Math.toRadians(particle.angle.toDouble())
            val px = (cos(rad) * particle.distance).toFloat()
            val py = (sin(rad) * particle.distance).toFloat()
            
            // Màu xanh dương sáng
            val healthRatio = remainingHealth.toFloat() / maxHealth
            val alpha = (180 * healthRatio).toInt().coerceIn(50, 200)
            
            particlePaint.color = Color.argb(alpha, 120, 200, 255)
            canvas.drawCircle(px, py, particle.size, particlePaint)
            
            // Glow
            particlePaint.color = Color.argb(alpha / 2, 180, 230, 255)
            canvas.drawCircle(px, py, particle.size * 1.5f, particlePaint)
        }
    }
    
    private fun drawHealthBar(canvas: Canvas) {
        // Vẽ health bar dạng vòng tròn ở trên đầu khiên
        val barY = -shieldRadius - 30f
        val barWidth = 80f
        val barHeight = 8f
        
        // Background
        healthBarPaint.style = Paint.Style.FILL
        healthBarPaint.color = Color.argb(150, 50, 50, 50)
        canvas.drawRoundRect(
            -barWidth/2, barY - barHeight/2,
            barWidth/2, barY + barHeight/2,
            4f, 4f, healthBarPaint
        )
        
        // Health
        val healthWidth = barWidth * (remainingHealth.toFloat() / maxHealth)
        val healthRatio = remainingHealth.toFloat() / maxHealth
        val r = (100 + (1f - healthRatio) * 155).toInt()
        val g = (200 * healthRatio).toInt()
        
        healthBarPaint.color = Color.argb(255, r, g, 255)
        canvas.drawRoundRect(
            -barWidth/2, barY - barHeight/2,
            -barWidth/2 + healthWidth, barY + barHeight/2,
            4f, 4f, healthBarPaint
        )
        
        // Border
        healthBarPaint.style = Paint.Style.STROKE
        healthBarPaint.strokeWidth = 2f
        healthBarPaint.color = Color.argb(200, 150, 200, 255)
        canvas.drawRoundRect(
            -barWidth/2, barY - barHeight/2,
            barWidth/2, barY + barHeight/2,
            4f, 4f, healthBarPaint
        )
    }
    
    /**
     * Nhận damage - Giảm shield health
     * Trả về damage còn lại sau khi khiên chặn (nếu có)
     */
    fun absorbDamage(damage: Int): Int {
        if (!isActive) return damage
        
        val absorbed = damage.coerceAtMost(remainingHealth)
        remainingHealth -= absorbed
        
        // Nếu hết máu thì deactivate
        if (remainingHealth <= 0) {
            isActive = false
        }
        
        // Trả về damage còn lại (nếu damage > shield health)
        return damage - absorbed
    }
    
    /**
     * Kiểm tra shield còn active không
     */
    fun isActive(): Boolean = isActive
    
    /**
     * Lấy shield health còn lại
     */
    fun getRemainingHealth(): Int = remainingHealth
    
    /**
     * Lấy vị trí hiện tại của shield (theo owner)
     */
    fun getX(): Float = ownerX
    fun getY(): Float = ownerY
}
