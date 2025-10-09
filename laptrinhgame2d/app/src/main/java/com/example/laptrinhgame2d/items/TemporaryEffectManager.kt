package com.example.laptrinhgame2d.items

class TemporaryEffectManager {

    enum class EffectType {
        SPEED_BOOST,
        DAMAGE_BOOST
    }

    private data class Effect(
        val type: EffectType,
        var remainingTime: Int,
        val multiplier: Float
    )

    private val activeEffects = mutableMapOf<EffectType, Effect>()

    fun addEffect(type: EffectType, duration: Int, multiplier: Float) {
        activeEffects[type] = Effect(type, duration, multiplier)
    }

    fun update() {
        val toRemove = mutableListOf<EffectType>()

        activeEffects.values.forEach { effect ->
            effect.remainingTime--
            if (effect.remainingTime <= 0) {
                toRemove.add(effect.type)
            }
        }

        toRemove.forEach { type ->
            activeEffects.remove(type)
        }
    }

    fun hasEffect(type: EffectType): Boolean {
        return activeEffects.containsKey(type)
    }

    fun getRemainingTime(type: EffectType): Int {
        return activeEffects[type]?.remainingTime ?: 0
    }

    fun getSpeedMultiplier(): Float {
        return 1f + (activeEffects[EffectType.SPEED_BOOST]?.multiplier ?: 0f)
    }

    fun getDamageMultiplier(): Float {
        return 1f + (activeEffects[EffectType.DAMAGE_BOOST]?.multiplier ?: 0f)
    }

    fun reset() {
        activeEffects.clear()
    }
}