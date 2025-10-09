package com.example.laptrinhgame2d.effects

/**
 * Temporary Effect System - Hệ thống hiệu ứng tạm thời cho hero
 */
class TemporaryEffectManager {

    enum class EffectType {
        SPEED_BOOST,     // Tăng tốc độ
        DAMAGE_BOOST,    // Tăng sát thương
        HEAL_BURST,      // Hồi máu tức thì
        FIREBALL_SKILL,  // Skill quả cầu lửa
        ICE_SHARD_SKILL  // Skill gai băng
    }

    data class ActiveEffect(
        val type: EffectType,
        var remainingTime: Int,  // frames
        val value: Float         // giá trị effect
    )

    private val activeEffects = mutableMapOf<EffectType, ActiveEffect>()

    fun addEffect(type: EffectType, duration: Int, value: Float) {
        activeEffects[type] = ActiveEffect(type, duration, value)
    }

    fun update() {
        val toRemove = mutableListOf<EffectType>()

        activeEffects.forEach { (type, effect) ->
            effect.remainingTime--
            if (effect.remainingTime <= 0) {
                toRemove.add(type)
            }
        }

        toRemove.forEach { activeEffects.remove(it) }
    }

    fun hasEffect(type: EffectType): Boolean = activeEffects.containsKey(type)

    fun getEffectValue(type: EffectType): Float = activeEffects[type]?.value ?: 0f

    fun getSpeedMultiplier(): Float {
        return if (hasEffect(EffectType.SPEED_BOOST)) {
            1f + getEffectValue(EffectType.SPEED_BOOST)
        } else 1f
    }

    fun getDamageMultiplier(): Float {
        return if (hasEffect(EffectType.DAMAGE_BOOST)) {
            1f + getEffectValue(EffectType.DAMAGE_BOOST)
        } else 1f
    }

    fun getRemainingTime(type: EffectType): Int = activeEffects[type]?.remainingTime ?: 0

    fun clear() {
        activeEffects.clear()
    }
}