package com.example.laptrinhgame2d

import android.content.Context
import android.content.Intent

class LevelManager(private val context: Context) {

    enum class Level(val displayName: String) {
        GRASSLAND("Grassland"),
        DESERT("Desert"),
        VOLCANO("Volcano"),
        ICE("Frozen Wasteland"),
        SPACE("Cosmic Station")
    }

    data class LevelConfig(
        val level: Level,
        val skeletons: Int,
        val demons: Int,
        val medusas: Int,
        val jinns: Int,
        val smallDragons: Int,
        val dragons: Int
    ) {
        val totalEnemies: Int get() = skeletons + demons + medusas + jinns + smallDragons + dragons
    }

    companion object {
        // Cấu hình số lượng quái cho từng level theo yêu cầu
        private val LEVEL_CONFIGS = mapOf(
            Level.GRASSLAND to LevelConfig(
                level = Level.GRASSLAND,
                skeletons = 4,        // 4 Skeleton
                demons = 4,           // 4 Demon
                medusas = 0,          // Không có Medusa
                jinns = 0,            // Không có Jinn
                smallDragons = 0,     // Không có SmallDragon
                dragons = 0           // Không có Dragon
            ), // Tổng: 8 con

            Level.DESERT to LevelConfig(
                level = Level.DESERT,
                skeletons = 4,        // 4 Skeleton
                demons = 4,           // 4 Demon
                medusas = 5,          // 5 Medusa (thêm mới)
                jinns = 0,            // Không có Jinn
                smallDragons = 0,     // Không có SmallDragon
                dragons = 0           // Không có Dragon
            ), // Tổng: 13 con

            Level.VOLCANO to LevelConfig(
                level = Level.VOLCANO,
                skeletons = 4,        // 4 Skeleton
                demons = 4,           // 4 Demon
                medusas = 4,          // 4 Medusa
                jinns = 3,            // 3 Jinn (thêm mới)
                smallDragons = 0,     // Không có SmallDragon
                dragons = 0           // Không có Dragon
            ), // Tổng: 15 con

            // THÊM ICE LEVEL
            Level.ICE to LevelConfig(
                level = Level.ICE,
                skeletons = 3,        // 3 Skeleton
                demons = 5,           // 5 Demon
                medusas = 5,          // 5 Medusa
                jinns = 3,            // 3 Jinn
                smallDragons = 1,     // 1 SmallDragon (thêm mới)
                dragons = 0           // Không có Dragon
            ), // Tổng: 17 con

            // THÊM SPACE LEVEL
            Level.SPACE to LevelConfig(
                level = Level.SPACE,
                skeletons = 3,        // 3 Skeleton
                demons = 4,           // 4 Demon
                medusas = 4,          // 4 Medusa
                jinns = 4,            // 4 Jinn
                smallDragons = 2,     // 2 SmallDragon
                dragons = 1           // 1 Dragon (boss cuối)
            ) // Tổng: 18 con
        )

        /**
         * Convert Level enum sang mapType integer
         */
        fun getMapTypeFromLevel(level: Level): Int {
            return when (level) {
                Level.GRASSLAND -> 1
                Level.DESERT -> 2
                Level.VOLCANO -> 3
                Level.ICE -> 4
                Level.SPACE -> 5
            }
        }
    }

    /**
     * Convert mapType integer sang Level enum
     */
    fun getLevelFromMapType(mapType: Int): Level {
        return when (mapType) {
            1 -> Level.GRASSLAND
            2 -> Level.DESERT
            3 -> Level.VOLCANO
            4 -> Level.ICE           // Map 4
            5 -> Level.SPACE         // Map 5
            else -> Level.GRASSLAND
        }
    }

    /**
     * Lấy config cho level cụ thể
     */
    fun getLevelConfig(level: Level): LevelConfig {
        return LEVEL_CONFIGS[level] ?: LEVEL_CONFIGS[Level.GRASSLAND]!!
    }

    /**
     * Lấy level tiếp theo
     */
    fun getNextLevel(currentLevel: Level): Level? {
        return when (currentLevel) {
            Level.GRASSLAND -> Level.DESERT
            Level.DESERT -> Level.VOLCANO
            Level.VOLCANO -> Level.ICE        // Thêm ICE
            Level.ICE -> Level.SPACE          // Thêm SPACE
            Level.SPACE -> null               // SPACE là level cuối cùng
        }
    }

    /**
     * Kiểm tra level cuối cùng
     */
    fun isFinalLevel(level: Level): Boolean {
        return level == Level.SPACE
    }

    /**
     * Chuyển sang level tiếp theo
     */
    fun proceedToNextLevel(currentLevel: Level, characterType: String) {
        val nextLevel = getNextLevel(currentLevel)

        if (nextLevel != null) {
            // Chuyển sang level tiếp theo
            val intent = Intent(context, MainActivity::class.java).apply {
                putExtra("CHARACTER_TYPE", characterType)
                putExtra("MAP_TYPE", getMapTypeFromLevel(nextLevel))  // SỬA: dùng function helper
                putExtra("IS_LEVEL_PROGRESSION", true)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)

            // Đóng activity hiện tại
            (context as? MainActivity)?.finish()
        }
    }

    /**
     * Lấy thông tin hiển thị level
     */
    fun getLevelDisplayInfo(level: Level): String {
        val config = getLevelConfig(level)
        return "Level: ${level.displayName}\nEnemies: ${config.totalEnemies}"
    }

    /**
     * Lấy level trước đó (để back)
     */
    fun getPreviousLevel(currentLevel: Level): Level? {
        return when (currentLevel) {
            Level.GRASSLAND -> null           // GRASSLAND là level đầu
            Level.DESERT -> Level.GRASSLAND
            Level.VOLCANO -> Level.DESERT
            Level.ICE -> Level.VOLCANO
            Level.SPACE -> Level.ICE
        }
    }

    /**
     * Lấy tất cả levels để hiển thị menu
     */
    fun getAllLevels(): List<Level> {
        return listOf(
            Level.GRASSLAND,
            Level.DESERT,
            Level.VOLCANO,
            Level.ICE,
            Level.SPACE
        )
    }

    /**
     * Kiểm tra level đã unlock chưa (tuỳ chọn - để làm progression system)
     */
    fun isLevelUnlocked(level: Level, unlockedMapType: Int): Boolean {
        val levelMapType = getMapTypeFromLevel(level)
        return levelMapType <= unlockedMapType
    }
}