package com.example.laptrinhgame2d

/**
 * GameModeConfig - Cấu hình các cơ chế chơi cho từng map/level
 * 
 * Các loại GameMode:
 * - ELIMINATION: Tiêu diệt hết quái để thắng
 * - TIME_ATTACK: Tiêu diệt hết quái trong thời gian giới hạn
 * - SURVIVAL: Sống sót trong khoảng thời gian nhất định
 * - BOSS_BATTLE: Đánh bại boss cuối cùng
 * - WAVE_DEFENSE: Chống đỡ các đợt quái tấn công
 */
object GameModeConfig {
    
    /**
     * Các loại game mode
     */
    enum class GameMode {
        ELIMINATION,      // Tiêu diệt hết quái
        TIME_ATTACK,      // Tiêu diệt hết quái trong thời gian giới hạn
        SURVIVAL,         // Sống sót trong thời gian nhất định
        BOSS_BATTLE,      // Đánh bại boss
        WAVE_DEFENSE      // Chống đỡ các wave
    }
    
    /**
     * Điều kiện thất bại
     */
    enum class FailCondition {
        PLAYER_DEATH,     // Hero chết
        TIME_OVER,        // Hết thời gian
        BOSS_ESCAPED,     // Boss thoát khỏi màn
        BASE_DESTROYED    // Căn cứ bị phá hủy
    }
    
    /**
     * Cấu hình cho một level/map
     */
    data class LevelModeConfig(
        val levelName: String,           // Tên level (Grassland, Desert, Volcano)
        val gameMode: GameMode,          // Loại game mode
        val timeLimit: Int? = null,      // Giới hạn thời gian (giây), null = không giới hạn
        val failConditions: List<FailCondition>, // Các điều kiện thua
        val description: String,         // Mô tả cơ chế chơi
        
        // Bonus objectives (optional)
        val bonusTimeTarget: Int? = null,    // Hoàn thành trong thời gian này để bonus
        val bonusNoHitTarget: Boolean = false, // Không bị đánh để bonus
        val bonusComboTarget: Int? = null     // Đạt combo để bonus
    )
    
    /**
     * Cấu hình cho Map 1 - Grassland
     */
    val GRASSLAND_CONFIG = LevelModeConfig(
        levelName = "Grassland",
        gameMode = GameMode.ELIMINATION,
        timeLimit = null, // Không giới hạn thời gian
        failConditions = listOf(FailCondition.PLAYER_DEATH),
        description = "Tiêu diệt tất cả kẻ địch để chiến thắng",
        bonusTimeTarget = 120, // Bonus nếu hoàn thành trong 2 phút
        bonusNoHitTarget = true
    )
    
    /**
     * Cấu hình cho Map 2 - Desert
     */
    val DESERT_CONFIG = LevelModeConfig(
        levelName = "Desert",
        gameMode = GameMode.TIME_ATTACK,
        timeLimit = 180, // 3 phút (180 giây)
        failConditions = listOf(
            FailCondition.PLAYER_DEATH,
            FailCondition.TIME_OVER
        ),
        description = "Tiêu diệt tất cả kẻ địch trong 3 phút",
        bonusTimeTarget = 120, // Bonus nếu hoàn thành trong 2 phút
        bonusComboTarget = 10   // Bonus nếu đạt combo 10
    )
    
    /**
     * Cấu hình cho Map 3 - Volcano
     */
    val VOLCANO_CONFIG = LevelModeConfig(
        levelName = "Volcano",
        gameMode = GameMode.BOSS_BATTLE,
        timeLimit = 300, // 5 phút (300 giây)
        failConditions = listOf(
            FailCondition.PLAYER_DEATH,
            FailCondition.TIME_OVER
        ),
        description = "Đánh bại tất cả kẻ địch và boss trong 5 phút",
        bonusTimeTarget = 240, // Bonus nếu hoàn thành trong 4 phút
        bonusNoHitTarget = true
    )
    
    /**
     * Lấy config theo level number
     */
    fun getConfigForLevel(levelNumber: Int): LevelModeConfig {
        return when (levelNumber) {
            1 -> GRASSLAND_CONFIG
            2 -> DESERT_CONFIG
            3 -> VOLCANO_CONFIG
            else -> GRASSLAND_CONFIG // Default
        }
    }
    
    /**
     * Lấy config theo tên level
     */
    fun getConfigForLevelName(levelName: String): LevelModeConfig {
        return when (levelName.lowercase()) {
            "grassland" -> GRASSLAND_CONFIG
            "desert" -> DESERT_CONFIG
            "volcano" -> VOLCANO_CONFIG
            else -> GRASSLAND_CONFIG
        }
    }
    
    /**
     * Kiểm tra xem có giới hạn thời gian không
     */
    fun hasTimeLimit(levelNumber: Int): Boolean {
        return getConfigForLevel(levelNumber).timeLimit != null
    }
    
    /**
     * Lấy thời gian giới hạn (giây)
     */
    fun getTimeLimit(levelNumber: Int): Int {
        return getConfigForLevel(levelNumber).timeLimit ?: 0
    }
    
    /**
     * Format thời gian còn lại (MM:SS)
     */
    fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }
    
    /**
     * Kiểm tra điều kiện bonus
     */
    data class BonusResult(
        val achievedTimeBonus: Boolean = false,
        val achievedNoHitBonus: Boolean = false,
        val achievedComboBonus: Boolean = false,
        val totalBonusScore: Int = 0
    )
    
    /**
     * Tính điểm bonus dựa trên performance
     */
    fun calculateBonus(
        levelNumber: Int,
        completionTime: Int,      // Thời gian hoàn thành (giây)
        flawlessScore: Int,        // Điểm Flawless còn lại (0-2000)
        maxCombo: Int              // Combo cao nhất
    ): BonusResult {
        val config = getConfigForLevel(levelNumber)
        
        var timeBonus = false
        var noHitBonus = false
        var comboBonus = false
        var totalScore = 0
        
        // Kiểm tra time bonus
        config.bonusTimeTarget?.let { target ->
            if (completionTime <= target) {
                timeBonus = true
                totalScore += 1000
            }
        }
        
        // Cộng điểm Flawless còn lại (0-2000)
        if (config.bonusNoHitTarget) {
            noHitBonus = (flawlessScore > 0) // Có bonus nếu còn điểm
            totalScore += flawlessScore
        }
        
        // Kiểm tra combo bonus
        config.bonusComboTarget?.let { target ->
            if (maxCombo >= target) {
                comboBonus = true
                totalScore += 500
            }
        }
        
        return BonusResult(
            achievedTimeBonus = timeBonus,
            achievedNoHitBonus = noHitBonus,
            achievedComboBonus = comboBonus,
            totalBonusScore = totalScore
        )
    }
}
