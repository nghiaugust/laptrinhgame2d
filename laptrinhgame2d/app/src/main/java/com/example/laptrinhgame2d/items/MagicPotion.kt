package com.example.laptrinhgame2d.items

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import kotlin.math.abs

class MagicPotion(
    private val context: Context,
    private var x: Float,
    private var y: Float
) {

    enum class SkillType {
        HEAL_BURST,    // Hồi 50 HP ngay
        SPEED_BOOST,   // Tăng tốc độ 5 giây
        DAMAGE_BOOST,  // Tăng damage 7.5 giây
        FIREBALL,      // TODO: Implement sau
        ICE_SHARD      // TODO: Implement sau
    }

    private var isCollected = false
    private var lifetime = 0
    private val maxLifetime = 1800 // 30 seconds at 60fps
    private var animationFrame = 0
    private val skillType: SkillType

    private val paint = Paint().apply {
        isAntiAlias = true
    }

    init {
        skillType = SkillType.values().random()
    }

    fun update() {
        if (isCollected) return

        lifetime++
        animationFrame++

        // Float animation
        y += kotlin.math.sin(animationFrame * 0.1f) * 0.5f
    }

    fun draw(canvas: Canvas) {
        if (isCollected) return

        val alpha = if (lifetime > maxLifetime - 300) {
            ((kotlin.math.sin(lifetime * 0.3f) + 1) * 127 + 128).toInt()
        } else {
            255
        }

        // Vẽ bình thuốc với hình dạng thực tế
        drawPotionBottle(canvas, alpha)

        // Vẽ glow effect
        drawGlowEffect(canvas, alpha)
    }

    private fun drawPotionBottle(canvas: Canvas, alpha: Int) {
        val bottleColor = when (skillType) {
            SkillType.HEAL_BURST -> Color.argb(alpha, 231, 76, 60)     // Đỏ
            SkillType.SPEED_BOOST -> Color.argb(alpha, 243, 156, 18)   // Cam
            SkillType.DAMAGE_BOOST -> Color.argb(alpha, 142, 68, 173)  // Tím
            SkillType.FIREBALL -> Color.argb(alpha, 230, 126, 34)      // Cam đậm
            SkillType.ICE_SHARD -> Color.argb(alpha, 52, 152, 219)     // Xanh dương
        }

        // Vẽ thân bình
        paint.color = bottleColor
        val bottlePath = Path().apply {
            moveTo(x - 15f, y + 20f)  // Bottom left
            lineTo(x - 15f, y - 10f)  // Middle left
            lineTo(x - 8f, y - 20f)   // Neck left
            lineTo(x - 8f, y - 30f)   // Top left
            lineTo(x + 8f, y - 30f)   // Top right
            lineTo(x + 8f, y - 20f)   // Neck right
            lineTo(x + 15f, y - 10f)  // Middle right
            lineTo(x + 15f, y + 20f)  // Bottom right
            close()
        }
        canvas.drawPath(bottlePath, paint)

        // Vẽ nắp bình
        paint.color = Color.argb(alpha, 101, 67, 33) // Nâu
        canvas.drawRect(x - 10f, y - 35f, x + 10f, y - 28f, paint)

        // Vẽ liquid bên trong
        val liquidColor = when (skillType) {
            SkillType.HEAL_BURST -> Color.argb(alpha, 255, 100, 100)
            SkillType.SPEED_BOOST -> Color.argb(alpha, 255, 200, 50)
            SkillType.DAMAGE_BOOST -> Color.argb(alpha, 180, 100, 200)
            SkillType.FIREBALL -> Color.argb(alpha, 255, 150, 50)
            SkillType.ICE_SHARD -> Color.argb(alpha, 100, 200, 255)
        }
        paint.color = liquidColor
        canvas.drawRect(x - 12f, y + 15f, x + 12f, y - 5f, paint)

        // Vẽ bubbles trong liquid
        paint.color = Color.argb(alpha / 2, 255, 255, 255)
        val bubbleOffset = kotlin.math.sin(animationFrame * 0.2f) * 3f
        canvas.drawCircle(x - 5f, y + bubbleOffset, 2f, paint)
        canvas.drawCircle(x + 3f, y + 5f + bubbleOffset, 1.5f, paint)
    }

    private fun drawGlowEffect(canvas: Canvas, alpha: Int) {
        val glowColor = when (skillType) {
            SkillType.HEAL_BURST -> Color.argb(alpha / 4, 231, 76, 60)
            SkillType.SPEED_BOOST -> Color.argb(alpha / 4, 243, 156, 18)
            SkillType.DAMAGE_BOOST -> Color.argb(alpha / 4, 142, 68, 173)
            SkillType.FIREBALL -> Color.argb(alpha / 4, 230, 126, 34)
            SkillType.ICE_SHARD -> Color.argb(alpha / 4, 52, 152, 219)
        }

        paint.color = glowColor
        for (i in 1..3) {
            canvas.drawCircle(x, y, 25f + i * 5f, paint)
        }
    }

    fun isCollidingWith(otherX: Float, otherY: Float, range: Float): Boolean {
        val dx = abs(x - otherX)
        val dy = abs(y - otherY)
        return dx < range && dy < range
    }

    fun collect() {
        isCollected = true
    }

    fun isCollected(): Boolean = isCollected
    fun shouldBeRemoved(): Boolean = isCollected || lifetime >= maxLifetime
    fun getSkillType(): SkillType = skillType
    fun getX(): Float = x
    fun getY(): Float = y
}