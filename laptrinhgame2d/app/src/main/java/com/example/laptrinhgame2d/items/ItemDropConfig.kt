package com.example.laptrinhgame2d.items

import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

object ItemDropConfig {

    enum class ItemType {
        HEALTH_HEART,
        DAMAGE_BOOST,      // Thay thế MAGIC_POTION
        SPEED_FLAME,
        BLACK_HOLE_SKILL,
        LASER_BEAM_SKILL,
        SHIELD_SKILL,
        BOMB_SKILL,
        LIGHTNING_SKILL
    }

    data class ItemDropEntry(
        val itemType: ItemType,
        val healAmount: Int = 25
    )

    // Drop configs (giữ nguyên tỷ lệ cao)
    private val dropConfigs = mapOf(
        "SKELETON" to mapOf(
            1 to 0.7f,
            2 to 0.8f,
            3 to 0.9f
        ),
        "DEMON" to mapOf(
            1 to 0.75f,
            2 to 0.85f,
            3 to 0.95f
        ),
        "MEDUSA" to mapOf(
            1 to 0.8f,
            2 to 0.9f,
            3 to 1.0f
        ),
        "JINN" to mapOf(
            1 to 0.8f,
            2 to 0.9f,
            3 to 1.0f
        ),
        "SMALL_DRAGON" to mapOf(
            1 to 0.9f,
            2 to 1.0f,
            3 to 1.0f
        ),
        "DRAGON" to mapOf(
            1 to 1.0f,
            2 to 1.0f,
            3 to 1.0f
        )
    )

    fun shouldDropItem(dropChance: Float): Boolean {
        return Random.nextFloat() < dropChance
    }

    /**
     * Roll drops - BỎ MAGIC_POTION, THÊM DAMAGE_BOOST
     */
    fun rollDropsForEnemy(enemyType: String, level: Int = 1): List<ItemDropEntry> {
        val droppedItems = mutableListOf<ItemDropEntry>()

        val dropChance = dropConfigs[enemyType]?.get(level) ?: 0.7f

        if (!shouldDropItem(dropChance)) {
            return emptyList()
        }

        val numberOfItems = when (enemyType) {
            "SKELETON" -> if (Random.nextFloat() < 0.3f) 2 else 1
            "DEMON" -> if (Random.nextFloat() < 0.4f) 2 else 1
            "MEDUSA" -> if (Random.nextFloat() < 0.5f) 2 else 1
            "JINN" -> if (Random.nextFloat() < 0.5f) 2 else 1
            "SMALL_DRAGON" -> if (Random.nextFloat() < 0.7f) 2 else 1
            "DRAGON" -> 3
            else -> 1
        }

        repeat(numberOfItems) {
            val itemTypeRoll = Random.nextFloat()

            when {
                itemTypeRoll < 0.4f -> {
                    // 40% chance drop health heart
                    droppedItems.add(ItemDropEntry(ItemType.HEALTH_HEART, healAmount = 35))
                }
                itemTypeRoll < 0.6f -> {
                    // 20% chance drop damage boost (thay thế magic potion)
                    droppedItems.add(ItemDropEntry(ItemType.DAMAGE_BOOST))
                }
                itemTypeRoll < 0.75f -> {
                    // 15% chance drop speed flame
                    droppedItems.add(ItemDropEntry(ItemType.SPEED_FLAME))
                }
                else -> {
                    // 25% chance drop skill items (tăng từ 20%)
                    val skillTypes = listOf(
                        ItemType.BLACK_HOLE_SKILL,
                        ItemType.LASER_BEAM_SKILL,
                        ItemType.SHIELD_SKILL,
                        ItemType.BOMB_SKILL,
                        ItemType.LIGHTNING_SKILL
                    )
                    droppedItems.add(ItemDropEntry(skillTypes.random()))
                }
            }
        }

        return droppedItems
    }

    fun calculateExplosionPositions(
        centerX: Float,
        centerY: Float,
        itemCount: Int,
        radius: Float
    ): List<Pair<Float, Float>> {
        val positions = mutableListOf<Pair<Float, Float>>()

        if (itemCount == 1) {
            positions.add(Pair(centerX, centerY))
        } else {
            for (i in 0 until itemCount) {
                val angle = (2 * Math.PI * i / itemCount) + Random.nextFloat() * 0.5f
                val distance = radius + Random.nextFloat() * 50f

                val x = centerX + cos(angle).toFloat() * distance
                val y = centerY + sin(angle).toFloat() * distance

                positions.add(Pair(x, y))
            }
        }

        return positions
    }

    fun getDropChance(enemyType: String, level: Int): Float {
        return dropConfigs[enemyType]?.get(level) ?: 0.7f
    }
}