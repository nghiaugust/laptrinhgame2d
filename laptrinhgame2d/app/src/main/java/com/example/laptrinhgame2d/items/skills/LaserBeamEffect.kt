package com.example.laptrinhgame2d.items.skills

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import kotlin.math.sqrt

/**
 * Hiệu ứng Laser Beam - Bắn laser vào quái gần nhất
 * Gây 200 damage
 */
class LaserBeamEffect(
    private val context: Context,
    private val startX: Float,
    private val startY: Float,
    private val targetX: Float,
    private val targetY: Float
) {
    private var isActive = true
    private var lifetime = 0
    private val maxLifetime = 90  // 1.5 giây animation
    
    // Damage
    private val totalDamage = 200
    private var damageDealt = false
    
    // Beam animation
    private var beamWidth = 5f  // Bắt đầu nhỏ
    private val maxBeamWidth = 60f  // To dần đến 60px
    private var beamAlpha = 255
    
    // Paint
    private val beamPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    
    private val corePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = Color.WHITE
    }
    
    private val impactPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    
    // Impact effect
    private var impactRadius = 0f
    private val maxImpactRadius = 80f
    
    // Energy particles at impact
    private data class ImpactParticle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var alpha: Int,
        var size: Float
    )
    private val impactParticles = mutableListOf<ImpactParticle>()
    
    init {
        // Tạo impact particles
        for (i in 0 until 20) {
            val angle = i * 18f  // 360 / 20
            val rad = Math.toRadians(angle.toDouble())
            val speed = 8f + (i % 3) * 2f
            
            impactParticles.add(
                ImpactParticle(
                    x = targetX,
                    y = targetY,
                    vx = (kotlin.math.cos(rad) * speed).toFloat(),
                    vy = (kotlin.math.sin(rad) * speed).toFloat(),
                    alpha = 255,
                    size = 6f + (i % 3) * 2f
                )
            )
        }
    }
    
    fun update() {
        if (!isActive) return
        
        lifetime++
        
        // Phase 1 (0-30 frames): Laser nhỏ xuất hiện và to dần
        if (lifetime <= 30) {
            val progress = lifetime / 30f
            beamWidth = 5f + (maxBeamWidth - 5f) * progress
            beamAlpha = 255
            impactRadius = maxImpactRadius * progress * 0.5f
        }
        // Phase 2 (30-60 frames): Laser ở full size
        else if (lifetime <= 60) {
            beamWidth = maxBeamWidth
            beamAlpha = 255
            impactRadius = maxImpactRadius
        }
        // Phase 3 (60-90 frames): Laser mờ dần
        else {
            val fadeProgress = (lifetime - 60) / 30f
            beamAlpha = (255 * (1f - fadeProgress)).toInt().coerceIn(0, 255)
            beamWidth = maxBeamWidth * (1f - fadeProgress * 0.5f)
            impactRadius = maxImpactRadius * (1f - fadeProgress)
        }
        
        // Update impact particles
        impactParticles.forEach { particle ->
            particle.x += particle.vx
            particle.y += particle.vy
            particle.vx *= 0.95f  // Deceleration
            particle.vy *= 0.95f
            particle.alpha = (particle.alpha * 0.92f).toInt().coerceIn(0, 255)
            particle.size *= 0.98f
        }
        
        if (lifetime >= maxLifetime) {
            isActive = false
        }
    }
    
    fun draw(canvas: Canvas) {
        if (!isActive) return
        
        // Vẽ laser beam từ hero đến target
        drawLaserBeam(canvas)
        
        // Vẽ impact effect tại vị trí target
        drawImpactEffect(canvas)
        
        // Vẽ impact particles
        drawImpactParticles(canvas)
    }
    
    private fun drawLaserBeam(canvas: Canvas) {
        val dx = targetX - startX
        val dy = targetY - startY
        val distance = sqrt(dx * dx + dy * dy)
        val angle = Math.toDegrees(kotlin.math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
        
        canvas.save()
        canvas.translate(startX, startY)
        canvas.rotate(angle)
        
        // Outer glow (đỏ)
        val outerGradient = RadialGradient(
            distance / 2, 0f, beamWidth * 1.5f,
            intArrayOf(
                Color.argb(beamAlpha / 2, 255, 100, 100),
                Color.argb(0, 255, 0, 0)
            ),
            floatArrayOf(0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        beamPaint.shader = outerGradient
        canvas.drawRect(0f, -beamWidth * 1.5f, distance, beamWidth * 1.5f, beamPaint)
        
        // Middle layer (trắng đỏ)
        val middleGradient = RadialGradient(
            distance / 2, 0f, beamWidth,
            intArrayOf(
                Color.argb(beamAlpha, 255, 200, 200),
                Color.argb(beamAlpha / 2, 255, 100, 100)
            ),
            floatArrayOf(0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        beamPaint.shader = middleGradient
        canvas.drawRect(0f, -beamWidth, distance, beamWidth, beamPaint)
        
        // Core (trắng sáng)
        beamPaint.shader = null
        beamPaint.color = Color.argb(beamAlpha, 255, 255, 255)
        canvas.drawRect(0f, -beamWidth * 0.3f, distance, beamWidth * 0.3f, beamPaint)
        
        canvas.restore()
    }
    
    private fun drawImpactEffect(canvas: Canvas) {
        if (impactRadius <= 0) return
        
        canvas.save()
        canvas.translate(targetX, targetY)
        
        // Outer impact ring
        val outerGradient = RadialGradient(
            0f, 0f, impactRadius,
            intArrayOf(
                Color.argb(0, 255, 100, 100),
                Color.argb(beamAlpha / 2, 255, 100, 100),
                Color.argb(beamAlpha, 255, 150, 150),
                Color.argb(beamAlpha, 255, 255, 255)
            ),
            floatArrayOf(0f, 0.6f, 0.9f, 1f),
            Shader.TileMode.CLAMP
        )
        impactPaint.shader = outerGradient
        canvas.drawCircle(0f, 0f, impactRadius, impactPaint)
        
        // Core white flash
        impactPaint.shader = null
        impactPaint.color = Color.argb(beamAlpha, 255, 255, 255)
        canvas.drawCircle(0f, 0f, impactRadius * 0.4f, impactPaint)
        
        canvas.restore()
    }
    
    private fun drawImpactParticles(canvas: Canvas) {
        impactParticles.forEach { particle ->
            if (particle.alpha <= 0) return@forEach
            
            val gradient = RadialGradient(
                particle.x, particle.y, particle.size,
                intArrayOf(
                    Color.argb(particle.alpha, 255, 255, 255),
                    Color.argb(particle.alpha / 2, 255, 150, 150),
                    Color.argb(0, 255, 100, 100)
                ),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
            impactPaint.shader = gradient
            canvas.drawCircle(particle.x, particle.y, particle.size, impactPaint)
        }
    }
    
    fun isActive(): Boolean = isActive
    
    fun getDamage(): Int = totalDamage
    
    fun getTargetX(): Float = targetX
    fun getTargetY(): Float = targetY
    
    fun hasDamageBeenDealt(): Boolean = damageDealt
    
    fun markDamageAsDealt() {
        damageDealt = true
    }
}
