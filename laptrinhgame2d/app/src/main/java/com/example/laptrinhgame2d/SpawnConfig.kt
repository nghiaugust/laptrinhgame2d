package com.example.laptrinhgame2d

/**
 * SpawnConfig - Cấu hình chi tiết cách spawn quái theo WAVE SYSTEM
 * 
 * File này định nghĩa:
 * - Wave System: Spawn quái theo đợt với delay
 * - Vị trí spawn (baseX, baseY)
 * - Khoảng cách giữa các con (spacing)
 * - Độ random (randomRangeX, randomRangeY)
 * - Pattern spawn (LINE, WAVE, CLUSTER, SCATTERED)
 */
class SpawnConfig {

    /**
     * Pattern spawn - Kiểu bố trí quái
     */
    enum class SpawnPattern {
        LINE,       // Spawn theo hàng ngang
        WAVE,       // Spawn theo sóng (zigzag)
        CLUSTER,    // Spawn theo cụm
        SCATTERED,  // Spawn ngẫu nhiên hoàn toàn
        ARENA       // Spawn vòng tròn (boss fight)
    }

    /**
     * Loại quái
     */
    enum class EnemyType {
        SKELETON,
        DEMON,
        MEDUSA,
        JINN,
        SMALL_DRAGON,
        DRAGON
    }

    /**
     * Cấu hình 1 wave spawn
     */
    data class WaveConfig(
        val waveNumber: Int,                    // Số thứ tự wave (1, 2, 3...)
        val spawnOnPreviousWaveCleared: Boolean = true, // Spawn khi wave trước chết hết (true) hoặc theo thời gian (false)
        val delaySeconds: Float = 0f,           // Delay sau khi wave trước chết hết (giây) - chỉ dùng khi spawnOnPreviousWaveCleared = true
        val enemies: Map<EnemyType, Int>,       // Map loại quái -> số lượng
        val baseX: Float = 800f,                // Vị trí X spawn
        val baseY: Float = 400f,                // Vị trí Y spawn
        val spacingX: Float = 300f,             // Khoảng cách giữa các con
        val randomRangeX: Float = 200f,         // Random X
        val randomRangeY: Float = 150f,         // Random Y
        val pattern: SpawnPattern = SpawnPattern.SCATTERED
    ) {
        val totalEnemies: Int get() = enemies.values.sum()
        
        fun getEnemyCount(type: EnemyType): Int = enemies[type] ?: 0
    }

    /**
     * Cấu hình spawn cho toàn bộ level với WAVE SYSTEM
     */
    data class LevelSpawnConfig(
        val level: LevelManager.Level,
        val waves: List<WaveConfig>,
        val useWaveSystem: Boolean = true  // true = spawn theo wave, false = spawn tất cả cùng lúc
    ) {
        val totalWaves: Int get() = waves.size
        val totalEnemies: Int get() = waves.sumOf { it.totalEnemies }
    }

    companion object {
        // ===== 🌱 GRASSLAND LEVEL (Easy) - 3 WAVES =====
        private val GRASSLAND_SPAWN = LevelSpawnConfig(
            level = LevelManager.Level.GRASSLAND,
            useWaveSystem = true,
            waves = listOf(
                // Wave 1: 2 Skeleton + 1 Demon (3 con) - Spawn ngay khi bắt đầu
                WaveConfig(
                    waveNumber = 1,
                    spawnOnPreviousWaveCleared = false, // Wave đầu tiên spawn ngay
                    delaySeconds = 0f,
                    enemies = mapOf(
                        EnemyType.SKELETON to 2,
                        EnemyType.DEMON to 1
                    ),
                    baseX = 800f,
                    baseY = 400f,
                    spacingX = 300f,
                    randomRangeX = 150f,
                    randomRangeY = 100f,
                    pattern = SpawnPattern.LINE
                ),
                
                // Wave 2: 1 Skeleton + 2 Demon (3 con) - Spawn sau khi Wave 1 chết hết
                WaveConfig(
                    waveNumber = 2,
                    spawnOnPreviousWaveCleared = true,
                    delaySeconds = 1f, // Delay 1s sau khi wave 1 chết hết
                    enemies = mapOf(
                        EnemyType.SKELETON to 1,
                        EnemyType.DEMON to 2
                    ),
                    baseX = 1400f,
                    baseY = 400f,
                    spacingX = 350f,
                    randomRangeX = 180f,
                    randomRangeY = 120f,
                    pattern = SpawnPattern.SCATTERED
                ),
                
                // Wave 3: 1 Skeleton + 1 Demon (2 con) - Spawn sau khi Wave 2 chết hết
                WaveConfig(
                    waveNumber = 3,
                    spawnOnPreviousWaveCleared = true,
                    delaySeconds = 1f, // Delay 1s sau khi wave 2 chết hết
                    enemies = mapOf(
                        EnemyType.SKELETON to 1,
                        EnemyType.DEMON to 1
                    ),
                    baseX = 2000f,
                    baseY = 400f,
                    spacingX = 400f,
                    randomRangeX = 200f,
                    randomRangeY = 150f,
                    pattern = SpawnPattern.LINE
                )
            )
        ) // Total: 8 con, 3 waves

        // ===== 🏜️ DESERT LEVEL (Medium) - 3 WAVES =====
        private val DESERT_SPAWN = LevelSpawnConfig(
            level = LevelManager.Level.DESERT,
            useWaveSystem = true,
            waves = listOf(
                // Wave 1: 2 Skeleton + 1 Demon + 1 Medusa (4 con) - Spawn ngay khi bắt đầu
                WaveConfig(
                    waveNumber = 1,
                    spawnOnPreviousWaveCleared = false,
                    delaySeconds = 0f,
                    enemies = mapOf(
                        EnemyType.SKELETON to 2,
                        EnemyType.DEMON to 1,
                        EnemyType.MEDUSA to 1
                    ),
                    baseX = 800f,
                    baseY = 400f,
                    spacingX = 350f,
                    randomRangeX = 200f,
                    randomRangeY = 120f,
                    pattern = SpawnPattern.SCATTERED
                ),
                
                // Wave 2: 1 Skeleton + 2 Demon + 2 Medusa (5 con) - Spawn sau khi Wave 1 chết hết
                WaveConfig(
                    waveNumber = 2,
                    spawnOnPreviousWaveCleared = true,
                    delaySeconds = 2f, // Delay 2s sau khi wave 1 chết hết
                    enemies = mapOf(
                        EnemyType.SKELETON to 1,
                        EnemyType.DEMON to 2,
                        EnemyType.MEDUSA to 2
                    ),
                    baseX = 1000f,
                    baseY = 400f,
                    spacingX = 400f,
                    randomRangeX = 250f,
                    randomRangeY = 150f,
                    pattern = SpawnPattern.CLUSTER
                ),
                
                // Wave 3: 1 Skeleton + 1 Demon + 2 Medusa (4 con) - Spawn sau khi Wave 2 chết hết
                WaveConfig(
                    waveNumber = 3,
                    spawnOnPreviousWaveCleared = true,
                    delaySeconds = 2f, // Delay 2s sau khi wave 2 chết hết
                    enemies = mapOf(
                        EnemyType.SKELETON to 1,
                        EnemyType.DEMON to 1,
                        EnemyType.MEDUSA to 2
                    ),
                    baseX = 1200f,
                    baseY = 400f,
                    spacingX = 450f,
                    randomRangeX = 300f,
                    randomRangeY = 180f,
                    pattern = SpawnPattern.WAVE
                )
            )
        ) // Total: 13 con, 3 waves

        // ===== 🌋 VOLCANO LEVEL (Hard) - 4 WAVES =====
        private val VOLCANO_SPAWN = LevelSpawnConfig(
            level = LevelManager.Level.VOLCANO,
            useWaveSystem = true,
            waves = listOf(
                // Wave 1: 2 Skeleton + 1 Demon + 1 Medusa (4 con) - Spawn ngay khi bắt đầu
                WaveConfig(
                    waveNumber = 1,
                    spawnOnPreviousWaveCleared = false,
                    delaySeconds = 0f,
                    enemies = mapOf(
                        EnemyType.SKELETON to 2,
                        EnemyType.DEMON to 1,
                        EnemyType.MEDUSA to 1
                    ),
                    baseX = 800f,
                    baseY = 400f,
                    spacingX = 350f,
                    randomRangeX = 250f,
                    randomRangeY = 150f,
                    pattern = SpawnPattern.SCATTERED
                ),
                
                // Wave 2: 1 Skeleton + 2 Demon + 1 Medusa + 1 Jinn (5 con) - Spawn sau khi Wave 1 chết hết
                WaveConfig(
                    waveNumber = 2,
                    spawnOnPreviousWaveCleared = true,
                    delaySeconds = 2f, // Delay 2s sau khi wave 1 chết hết
                    enemies = mapOf(
                        EnemyType.SKELETON to 1,
                        EnemyType.DEMON to 2,
                        EnemyType.MEDUSA to 1,
                        EnemyType.JINN to 1
                    ),
                    baseX = 1000f,
                    baseY = 400f,
                    spacingX = 450f,
                    randomRangeX = 300f,
                    randomRangeY = 200f,
                    pattern = SpawnPattern.CLUSTER
                ),
                
                // Wave 3: 1 Skeleton + 1 Demon + 1 Medusa + 1 Jinn (4 con) - Spawn sau khi Wave 2 chết hết
                WaveConfig(
                    waveNumber = 3,
                    spawnOnPreviousWaveCleared = true,
                    delaySeconds = 3f, // Delay 3s sau khi wave 2 chết hết
                    enemies = mapOf(
                        EnemyType.SKELETON to 1,
                        EnemyType.DEMON to 1,
                        EnemyType.MEDUSA to 1,
                        EnemyType.JINN to 1
                    ),
                    baseX = 1200f,
                    baseY = 400f,
                    spacingX = 500f,
                    randomRangeX = 350f,
                    randomRangeY = 250f,
                    pattern = SpawnPattern.WAVE
                ),
                
                // Wave 4: 1 Medusa + 1 Jinn (2 con) - BOSS WAVE - Spawn sau khi Wave 3 chết hết
                WaveConfig(
                    waveNumber = 4,
                    spawnOnPreviousWaveCleared = true,
                    delaySeconds = 3f, // Delay 3s sau khi wave 3 chết hết
                    enemies = mapOf(
                        EnemyType.MEDUSA to 1,
                        EnemyType.JINN to 1
                    ),
                    baseX = 1400f,
                    baseY = 400f,
                    spacingX = 600f,
                    randomRangeX = 400f,
                    randomRangeY = 300f,
                    pattern = SpawnPattern.ARENA
                )
            )
        ) // Total: 15 con, 4 waves

        // ===== ❄️ ICE LEVEL (Very Hard) - 4 WAVES =====
        private val ICE_SPAWN = LevelSpawnConfig(
            level = LevelManager.Level.ICE,
            useWaveSystem = true,
            waves = listOf(
                // Wave 1: 2 Skeleton + 2 Demon (4 con) - Spawn ngay khi bắt đầu
                WaveConfig(
                    waveNumber = 1,
                    spawnOnPreviousWaveCleared = false,
                    delaySeconds = 0f,
                    enemies = mapOf(
                        EnemyType.SKELETON to 2,
                        EnemyType.DEMON to 2
                    ),
                    baseX = 800f,
                    baseY = 400f,
                    spacingX = 350f,
                    randomRangeX = 250f,
                    randomRangeY = 150f,
                    pattern = SpawnPattern.LINE
                ),

                // Wave 2: 1 Skeleton + 2 Demon + 2 Medusa (5 con) - Spawn sau khi Wave 1 chết hết
                WaveConfig(
                    waveNumber = 2,
                    spawnOnPreviousWaveCleared = true,
                    delaySeconds = 2f,
                    enemies = mapOf(
                        EnemyType.SKELETON to 1,
                        EnemyType.DEMON to 2,
                        EnemyType.MEDUSA to 2
                    ),
                    baseX = 1000f,
                    baseY = 400f,
                    spacingX = 400f,
                    randomRangeX = 300f,
                    randomRangeY = 200f,
                    pattern = SpawnPattern.SCATTERED
                ),

                // Wave 3: 1 Demon + 2 Medusa + 2 Jinn (5 con) - Spawn sau khi Wave 2 chết hết
                WaveConfig(
                    waveNumber = 3,
                    spawnOnPreviousWaveCleared = true,
                    delaySeconds = 3f,
                    enemies = mapOf(
                        EnemyType.DEMON to 1,
                        EnemyType.MEDUSA to 2,
                        EnemyType.JINN to 2
                    ),
                    baseX = 1200f,
                    baseY = 400f,
                    spacingX = 450f,
                    randomRangeX = 350f,
                    randomRangeY = 250f,
                    pattern = SpawnPattern.CLUSTER
                ),

                // Wave 4: 1 Jinn + 1 Small Dragon (2 con) - ICE BOSS WAVE
                WaveConfig(
                    waveNumber = 4,
                    spawnOnPreviousWaveCleared = true,
                    delaySeconds = 3f,
                    enemies = mapOf(
                        EnemyType.JINN to 1,
                        EnemyType.SMALL_DRAGON to 1
                    ),
                    baseX = 1500f,
                    baseY = 400f,
                    spacingX = 600f,
                    randomRangeX = 400f,
                    randomRangeY = 300f,
                    pattern = SpawnPattern.ARENA
                )
            )
        ) // Total: 16 con, 4 waves

        // ===== 🚀 SPACE LEVEL (Extreme) - 5 WAVES =====
        private val SPACE_SPAWN = LevelSpawnConfig(
            level = LevelManager.Level.SPACE,
            useWaveSystem = true,
            waves = listOf(
                // Wave 1: 2 Skeleton + 2 Demon (4 con) - Spawn ngay khi bắt đầu
                WaveConfig(
                    waveNumber = 1,
                    spawnOnPreviousWaveCleared = false,
                    delaySeconds = 0f,
                    enemies = mapOf(
                        EnemyType.SKELETON to 2,
                        EnemyType.DEMON to 2
                    ),
                    baseX = 800f,
                    baseY = 400f,
                    spacingX = 350f,
                    randomRangeX = 250f,
                    randomRangeY = 150f,
                    pattern = SpawnPattern.LINE
                ),

                // Wave 2: 1 Skeleton + 2 Demon + 1 Medusa (4 con)
                WaveConfig(
                    waveNumber = 2,
                    spawnOnPreviousWaveCleared = true,
                    delaySeconds = 2f,
                    enemies = mapOf(
                        EnemyType.SKELETON to 1,
                        EnemyType.DEMON to 2,
                        EnemyType.MEDUSA to 1
                    ),
                    baseX = 1000f,
                    baseY = 400f,
                    spacingX = 400f,
                    randomRangeX = 300f,
                    randomRangeY = 200f,
                    pattern = SpawnPattern.SCATTERED
                ),

                // Wave 3: 2 Medusa + 2 Jinn (4 con)
                WaveConfig(
                    waveNumber = 3,
                    spawnOnPreviousWaveCleared = true,
                    delaySeconds = 3f,
                    enemies = mapOf(
                        EnemyType.MEDUSA to 2,
                        EnemyType.JINN to 2
                    ),
                    baseX = 1200f,
                    baseY = 400f,
                    spacingX = 450f,
                    randomRangeX = 350f,
                    randomRangeY = 250f,
                    pattern = SpawnPattern.WAVE
                ),

                // Wave 4: 1 Jinn + 2 Small Dragon (3 con)
                WaveConfig(
                    waveNumber = 4,
                    spawnOnPreviousWaveCleared = true,
                    delaySeconds = 3f,
                    enemies = mapOf(
                        EnemyType.JINN to 1,
                        EnemyType.SMALL_DRAGON to 2
                    ),
                    baseX = 1400f,
                    baseY = 400f,
                    spacingX = 500f,
                    randomRangeX = 400f,
                    randomRangeY = 300f,
                    pattern = SpawnPattern.CLUSTER
                ),

                // Wave 5: 1 Small Dragon + 1 Dragon (2 con) - FINAL SPACE BOSS
                WaveConfig(
                    waveNumber = 5,
                    spawnOnPreviousWaveCleared = true,
                    delaySeconds = 4f,
                    enemies = mapOf(
                        EnemyType.SMALL_DRAGON to 1,
                        EnemyType.DRAGON to 1
                    ),
                    baseX = 1600f,
                    baseY = 400f,
                    spacingX = 700f,
                    randomRangeX = 500f,
                    randomRangeY = 400f,
                    pattern = SpawnPattern.ARENA
                )
            )
        ) // Total: 17 con, 5 waves

        // Map tất cả config theo level
        private val SPAWN_CONFIGS = mapOf(
            LevelManager.Level.GRASSLAND to GRASSLAND_SPAWN,
            LevelManager.Level.DESERT to DESERT_SPAWN,
            LevelManager.Level.VOLCANO to VOLCANO_SPAWN,
            LevelManager.Level.ICE to ICE_SPAWN,        // Thêm mới
            LevelManager.Level.SPACE to SPACE_SPAWN     // Thêm mới
        )

        /**
         * Lấy spawn config cho level
         */
        fun getSpawnConfig(level: LevelManager.Level): LevelSpawnConfig {
            return SPAWN_CONFIGS[level] ?: GRASSLAND_SPAWN
        }

        /**
         * Lấy wave config theo số thứ tự
         */
        fun getWaveConfig(level: LevelManager.Level, waveNumber: Int): WaveConfig? {
            val config = getSpawnConfig(level)
            return config.waves.find { it.waveNumber == waveNumber }
        }

        /**
         * Tính toán delay time tích lũy đến wave hiện tại
         */
        fun getCumulativeDelay(level: LevelManager.Level, waveNumber: Int): Float {
            val config = getSpawnConfig(level)
            return config.waves
                .filter { it.waveNumber <= waveNumber }
                .sumOf { it.delaySeconds.toDouble() }
                .toFloat()
        }

        /**
         * Tính toán vị trí spawn theo pattern
         */
        fun calculateSpawnPosition(
            waveConfig: WaveConfig,
            index: Int,
            totalCount: Int
        ): Pair<Float, Float> {
            return when (waveConfig.pattern) {
                SpawnPattern.LINE -> {
                    // Spawn theo hàng ngang
                    val x = waveConfig.baseX + (index * waveConfig.spacingX) + 
                            (Math.random() * waveConfig.randomRangeX).toFloat()
                    val y = waveConfig.baseY + (Math.random() * waveConfig.randomRangeY).toFloat()
                    Pair(x, y)
                }

                SpawnPattern.WAVE -> {
                    // Spawn theo sóng (sin wave)
                    val x = waveConfig.baseX + (index * waveConfig.spacingX) + 
                            (Math.random() * waveConfig.randomRangeX).toFloat()
                    val waveOffset = Math.sin(index * 0.5) * 100f
                    val y = waveConfig.baseY + waveOffset.toFloat() + 
                            (Math.random() * waveConfig.randomRangeY).toFloat()
                    Pair(x, y)
                }

                SpawnPattern.CLUSTER -> {
                    // Spawn theo cụm (2-3 con gần nhau)
                    val clusterSize = 2
                    val clusterIndex = index / clusterSize
                    val positionInCluster = index % clusterSize
                    
                    val clusterX = waveConfig.baseX + (clusterIndex * waveConfig.spacingX * clusterSize)
                    val clusterY = waveConfig.baseY
                    
                    val x = clusterX + (positionInCluster * 150f) + 
                            (Math.random() * waveConfig.randomRangeX).toFloat()
                    val y = clusterY + (Math.random() * waveConfig.randomRangeY).toFloat()
                    Pair(x, y)
                }

                SpawnPattern.SCATTERED -> {
                    // Spawn hoàn toàn ngẫu nhiên
                    val x = waveConfig.baseX + (index * waveConfig.spacingX) + 
                            (Math.random() * waveConfig.randomRangeX * 2).toFloat() - waveConfig.randomRangeX
                    val y = waveConfig.baseY + 
                            (Math.random() * waveConfig.randomRangeY * 2).toFloat() - waveConfig.randomRangeY
                    Pair(x, y)
                }

                SpawnPattern.ARENA -> {
                    // Spawn theo vòng tròn (boss arena)
                    val angle = (index.toFloat() / totalCount) * 2 * Math.PI
                    val radius = waveConfig.spacingX
                    
                    val x = waveConfig.baseX + (Math.cos(angle) * radius).toFloat() + 
                            (Math.random() * waveConfig.randomRangeX).toFloat()
                    val y = waveConfig.baseY + (Math.sin(angle) * radius).toFloat() + 
                            (Math.random() * waveConfig.randomRangeY).toFloat()
                    Pair(x, y)
                }
            }
        }
    }
}
