package com.example.laptrinhgame2d.items

/**
 * Cấu hình tỉ lệ rơi vật phẩm khi giết quái
 */
object ItemDropConfig {
    
    /**
     * Loại vật phẩm có thể rơi
     */
    enum class ItemType {
        HEALTH_HEART,       // Trái tim hồi máu
        BLACK_HOLE_SKILL,   // Skill hố đen
        LASER_BEAM_SKILL,   // Skill laser beam
        SHIELD_SKILL,       // Skill khiên bảo vệ
        // Có thể mở rộng thêm: MANA_POTION, COIN, POWER_UP, etc.
    }
    
    /**
     * Cấu hình rơi cho từng loại quái
     */
    data class DropConfig(
        val itemType: ItemType,
        val dropChance: Float,      // Tỉ lệ rơi (0.0 - 1.0), ví dụ: 0.5 = 50%
        val healAmount: Int = 20    // Số máu hồi (chỉ cho HEALTH_HEART)
    )
    
    /**
     * Tỉ lệ rơi cho Skeleton
     */
    val SKELETON_DROPS = listOf(
        DropConfig(
            itemType = ItemType.HEALTH_HEART,
            dropChance = 0.5f,  // 50% rơi trái tim
            healAmount = 20
        )
    )
    
    /**
     * Tỉ lệ rơi cho Demon
     */
    val DEMON_DROPS = listOf(
        DropConfig(
            itemType = ItemType.HEALTH_HEART,
            dropChance = 0.5f,  // 50% rơi trái tim
            healAmount = 20
        )
    )
    
    /**
     * Tỉ lệ rơi cho Medusa
     */
    val MEDUSA_DROPS = listOf(
        DropConfig(
            itemType = ItemType.HEALTH_HEART,
            dropChance = 0.5f,  // 50% rơi trái tim
            healAmount = 20
        )
    )
    
    /**
     * Tỉ lệ rơi cho Jinn
     */
    val JINN_DROPS = listOf(
        DropConfig(
            itemType = ItemType.HEALTH_HEART,
            dropChance = 0.5f,  // 50% rơi trái tim
            healAmount = 20
        )
    )
    
    /**
     * Tỉ lệ rơi cho Small Dragon
     */
    val SMALL_DRAGON_DROPS = listOf(
        DropConfig(
            itemType = ItemType.HEALTH_HEART,
            dropChance = 0.5f,  // 50% rơi trái tim
            healAmount = 20
        )
    )
    
    /**
     * Tỉ lệ rơi cho Dragon (Boss)
     */
    val DRAGON_DROPS = listOf(
        DropConfig(
            itemType = ItemType.HEALTH_HEART,
            dropChance = 0.5f,  // 50% rơi trái tim
            healAmount = 20
        )
    )
    
    /**
     * Kiểm tra xem có rơi item không dựa trên tỉ lệ
     */
    fun shouldDropItem(dropChance: Float): Boolean {
        return Math.random() < dropChance
    }
    
    /**
     * Kiểm tra xem có rơi skill Black Hole không (50% tỉ lệ)
     */
    fun shouldDropBlackHoleSkill(): Boolean {
        return Math.random() < 0.5  // 50%
    }
    
    /**
     * Kiểm tra xem có rơi skill Laser Beam không (50% tỉ lệ)
     */
    fun shouldDropLaserBeamSkill(): Boolean {
        return Math.random() < 0.5  // 50%
    }
    
    /**
     * Kiểm tra xem có rơi skill Shield không (50% tỉ lệ)
     */
    fun shouldDropShieldSkill(): Boolean {
        return Math.random() < 0.5  // 50%
    }
    
    /**
     * Lấy danh sách drop config theo loại quái
     */
    fun getDropsForEnemy(enemyType: String): List<DropConfig> {
        return when (enemyType.uppercase()) {
            "SKELETON" -> SKELETON_DROPS
            "DEMON" -> DEMON_DROPS
            "MEDUSA" -> MEDUSA_DROPS
            "JINN" -> JINN_DROPS
            "SMALLDRAGON", "SMALL_DRAGON" -> SMALL_DRAGON_DROPS
            "DRAGON" -> DRAGON_DROPS
            else -> emptyList()
        }
    }
}
