package com.example.laptrinhgame2d

import android.content.Context
import android.content.Intent

class LevelManager(private val context: Context) {

    enum class Level(val displayName: String, val mapType: Int) {
        GRASSLAND("Grassland", 1),
        DESERT("Desert", 2),
        VOLCANO("Volcano", 3)
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
            ) // Tổng: 15 con
        )
    }

    fun getLevelFromMapType(mapType: Int): Level {
        return when (mapType) {
            1 -> Level.GRASSLAND
            2 -> Level.DESERT
            3 -> Level.VOLCANO
            else -> Level.GRASSLAND
        }
    }

    fun getLevelConfig(level: Level): LevelConfig {
        return LEVEL_CONFIGS[level] ?: LEVEL_CONFIGS[Level.GRASSLAND]!!
    }

    fun getNextLevel(currentLevel: Level): Level? {
        return when (currentLevel) {
            Level.GRASSLAND -> Level.DESERT
            Level.DESERT -> Level.VOLCANO
            Level.VOLCANO -> null // Không có level tiếp theo
        }
    }

    fun isFinalLevel(level: Level): Boolean {
        return level == Level.VOLCANO
    }

    fun proceedToNextLevel(currentLevel: Level, characterType: String) {
        val nextLevel = getNextLevel(currentLevel)

        if (nextLevel != null) {
            // Chuyển sang level tiếp theo
            val intent = Intent(context, MainActivity::class.java).apply {
                putExtra("CHARACTER_TYPE", characterType)
                putExtra("MAP_TYPE", nextLevel.mapType)
                putExtra("IS_LEVEL_PROGRESSION", true)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)

            // Đóng activity hiện tại
            (context as? MainActivity)?.finish()
        }
    }

    fun getLevelDisplayInfo(level: Level): String {
        val config = getLevelConfig(level)
        return "Level: ${level.displayName}\nEnemies: ${config.totalEnemies}"
    }
}