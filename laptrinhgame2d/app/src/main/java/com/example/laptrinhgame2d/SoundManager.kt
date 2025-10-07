package com.example.laptrinhgame2d

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool

/**
 * SoundManager - Quản lý tất cả âm thanh trong game
 * 
 * Các âm thanh chung:
 * - chay.mp3: Nhân vật chạy
 * - bi_danh.mp3: Nhân vật bị đánh (mất máu)
 * - victory.mp3: Chiến thắng (optional)
 * 
 * Âm thanh riêng cho từng hero:
 * - Fighter: tan_cong.mp3
 * - Samurai_Commander: tan_cong.mp3
 * - Samurai_Archer: 
 *   + ban_cung.mp3: Bắn cung
 *   + kiem_bay.mp3: Kiếm bay (khi giữ tấn công)
 *   + kiem_chem_1.mp3, kiem_chem_2.mp3, kiem_chem_3.mp3: Ba kiểu chém
 */
class SoundManager(context: Context, private val heroType: String = "Fighter") {

    private var soundPool: SoundPool? = null
    private val soundMap = mutableMapOf<String, Int>()
    private val streamMap = mutableMapOf<String, Int>() // Theo dõi stream đang phát
    
    // MediaPlayer for background music
    private var backgroundMusic: MediaPlayer? = null
    private val appContext = context.applicationContext
    
    // Sound IDs - Common sounds
    private var runSoundId: Int = -1
    private var hitSoundId: Int = -1
    private var victorySoundId: Int = -1
    
    // Sound IDs - Hero specific
    private var attackSoundId: Int = -1 // Fighter & Commander
    private var bowSoundId: Int = -1 // Archer: Bắn cung
    private var flyingSwordSoundId: Int = -1 // Archer: Kiếm bay
    private var slash1SoundId: Int = -1 // Archer: Chém 1
    private var slash2SoundId: Int = -1 // Archer: Chém 2
    private var slash3SoundId: Int = -1 // Archer: Chém 3
    
    // Sound IDs - Enemy specific
    private var skeletonAttackSoundId: Int = -1 // Skeleton: gay_dap
    private var demonAttackSoundId: Int = -1 // Demon: dam_giao
    private var medusaAttackSoundId: Int = -1 // Medusa: nem_da
    private var jinnAttackSoundId: Int = -1 // Jinn: cau_gio
    private var smallDragonAttackSoundId: Int = -1 // SmallDragon: lua_bay
    private var dragonAttackSoundId: Int = -1 // Dragon: phun_lua
    
    // Attack combo counter for Archer
    private var archerComboCount: Int = 0
    
    // Volume settings
    private var soundVolume: Float = 1.0f  // Tăng lên 100% để nghe rõ hơn
    private var musicVolume: Float = 0.2f  // Giảm nhạc nền xuống 20%
    
    // Control flags
    private var isSoundEnabled: Boolean = true
    private var isMusicEnabled: Boolean = true

    init {
        // Khởi tạo SoundPool với AudioAttributes
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(10) // Tối đa 10 âm thanh cùng lúc
            .setAudioAttributes(audioAttributes)
            .build()

        // Load âm thanh từ assets
        loadSounds(context)
        
        // Khởi tạo và phát nhạc nền
        initBackgroundMusic()
    }

    private fun loadSounds(context: Context) {
        try {
            // ===== LOAD ÂM THANH CHUNG =====
            
            // Load âm thanh chạy
            val runAssetFd = context.assets.openFd("sound/hero/chay.mp3")
            runSoundId = soundPool?.load(runAssetFd, 1) ?: -1
            soundMap["run"] = runSoundId
            runAssetFd.close()

            // Load âm thanh bị đánh
            val hitAssetFd = context.assets.openFd("sound/hero/bi_danh.mp3")
            hitSoundId = soundPool?.load(hitAssetFd, 1) ?: -1
            soundMap["hit"] = hitSoundId
            hitAssetFd.close()

            // Load âm thanh chiến thắng (nếu có)
            try {
                val victoryAssetFd = context.assets.openFd("sound/victory.mp3")
                victorySoundId = soundPool?.load(victoryAssetFd, 1) ?: -1
                soundMap["victory"] = victorySoundId
                victoryAssetFd.close()
            } catch (e: Exception) {
                // File không tồn tại, bỏ qua
            }

            // ===== LOAD ÂM THANH THEO HERO TYPE =====
            
            when (heroType) {
                "Fighter" -> {
                    // Fighter có 3 âm thanh đấm (dam1, dam2, dam3)
                    try {
                        val attack1AssetFd = context.assets.openFd("sound/hero/Fighter/dam1.mp3")
                        slash1SoundId = soundPool?.load(attack1AssetFd, 1) ?: -1
                        soundMap["slash_1"] = slash1SoundId
                        attack1AssetFd.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    
                    try {
                        val attack2AssetFd = context.assets.openFd("sound/hero/Fighter/dam2.mp3")
                        slash2SoundId = soundPool?.load(attack2AssetFd, 1) ?: -1
                        soundMap["slash_2"] = slash2SoundId
                        attack2AssetFd.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    
                    try {
                        val attack3AssetFd = context.assets.openFd("sound/hero/Fighter/dam3.mp3")
                        slash3SoundId = soundPool?.load(attack3AssetFd, 1) ?: -1
                        soundMap["slash_3"] = slash3SoundId
                        attack3AssetFd.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                
                "Samurai_Commander" -> {
                    // Commander có 3 âm thanh kiếm (kiem1, kiem2, kiem3)
                    try {
                        val attack1AssetFd = context.assets.openFd("sound/hero/Commander/kiem1.mp3")
                        slash1SoundId = soundPool?.load(attack1AssetFd, 1) ?: -1
                        soundMap["slash_1"] = slash1SoundId
                        attack1AssetFd.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    
                    try {
                        val attack2AssetFd = context.assets.openFd("sound/hero/Commander/kiem2.mp3")
                        slash2SoundId = soundPool?.load(attack2AssetFd, 1) ?: -1
                        soundMap["slash_2"] = slash2SoundId
                        attack2AssetFd.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    
                    try {
                        val attack3AssetFd = context.assets.openFd("sound/hero/Commander/kiem3.mp3")
                        slash3SoundId = soundPool?.load(attack3AssetFd, 1) ?: -1
                        soundMap["slash_3"] = slash3SoundId
                        attack3AssetFd.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                
                "Samurai_Archer" -> {
                    // Archer có nhiều âm thanh phức tạp trong thư mục Archer
                    
                    // 1. Âm thanh bắn cung
                    try {
                        val bowAssetFd = context.assets.openFd("sound/hero/Archer/ban_cung.mp3")
                        bowSoundId = soundPool?.load(bowAssetFd, 1) ?: -1
                        soundMap["bow"] = bowSoundId
                        bowAssetFd.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    
                    // 2. Âm thanh kiếm bay (skill - giữ attack)
                    try {
                        val flyingSwordAssetFd = context.assets.openFd("sound/hero/Archer/kiem_bay.mp3")
                        flyingSwordSoundId = soundPool?.load(flyingSwordAssetFd, 1) ?: -1
                        soundMap["flying_sword"] = flyingSwordSoundId
                        flyingSwordAssetFd.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    
                    // 3. Âm thanh kiếm chém 1 (không có dấu gạch dưới)
                    try {
                        val slash1AssetFd = context.assets.openFd("sound/hero/Archer/kiem_chem1.mp3")
                        slash1SoundId = soundPool?.load(slash1AssetFd, 1) ?: -1
                        soundMap["slash_1"] = slash1SoundId
                        slash1AssetFd.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    
                    // 4. Âm thanh kiếm chém 2
                    try {
                        val slash2AssetFd = context.assets.openFd("sound/hero/Archer/kiem_chem2.mp3")
                        slash2SoundId = soundPool?.load(slash2AssetFd, 1) ?: -1
                        soundMap["slash_2"] = slash2SoundId
                        slash2AssetFd.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    
                    // 5. Âm thanh kiếm chém 3
                    try {
                        val slash3AssetFd = context.assets.openFd("sound/hero/Archer/kiem_chem3.mp3")
                        slash3SoundId = soundPool?.load(slash3AssetFd, 1) ?: -1
                        soundMap["slash_3"] = slash3SoundId
                        slash3AssetFd.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            // ===== LOAD ÂM THANH ENEMY =====
            
            // 1. Skeleton: gay_dap
            try {
                val skeletonAssetFd = context.assets.openFd("sound/enemy/gay_dap.mp3")
                skeletonAttackSoundId = soundPool?.load(skeletonAssetFd, 1) ?: -1
                soundMap["skeleton_attack"] = skeletonAttackSoundId
                skeletonAssetFd.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            // 2. Demon: dam_giao
            try {
                val demonAssetFd = context.assets.openFd("sound/enemy/dam_giao.mp3")
                demonAttackSoundId = soundPool?.load(demonAssetFd, 1) ?: -1
                soundMap["demon_attack"] = demonAttackSoundId
                demonAssetFd.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            // 3. Medusa: nem_da
            try {
                val medusaAssetFd = context.assets.openFd("sound/enemy/nem_da.mp3")
                medusaAttackSoundId = soundPool?.load(medusaAssetFd, 1) ?: -1
                soundMap["medusa_attack"] = medusaAttackSoundId
                medusaAssetFd.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            // 4. Jinn: cau_gio
            try {
                val jinnAssetFd = context.assets.openFd("sound/enemy/cau_gio.mp3")
                jinnAttackSoundId = soundPool?.load(jinnAssetFd, 1) ?: -1
                soundMap["jinn_attack"] = jinnAttackSoundId
                jinnAssetFd.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            // 5. SmallDragon: lua_bay
            try {
                val smallDragonAssetFd = context.assets.openFd("sound/enemy/lua_bay.mp3")
                smallDragonAttackSoundId = soundPool?.load(smallDragonAssetFd, 1) ?: -1
                soundMap["small_dragon_attack"] = smallDragonAttackSoundId
                smallDragonAssetFd.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            // 6. Dragon: phun_lua
            try {
                val dragonAssetFd = context.assets.openFd("sound/enemy/phun_lua.mp3")
                dragonAttackSoundId = soundPool?.load(dragonAssetFd, 1) ?: -1
                soundMap["dragon_attack"] = dragonAttackSoundId
                dragonAssetFd.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Phát âm thanh chạy (loop)
     */
    fun playRunSound() {
        if (!isSoundEnabled) return
        
        // Nếu đang phát rồi thì không phát lại
        if (streamMap.containsKey("run")) return
        
        val soundId = soundMap["run"] ?: return
        if (soundId == -1) return
        
        val streamId = soundPool?.play(
            soundId,
            soundVolume,
            soundVolume,
            1, // Priority
            -1, // Loop infinitely
            1.0f // Playback rate
        ) ?: -1
        
        if (streamId != -1) {
            streamMap["run"] = streamId
        }
    }

    /**
     * Dừng âm thanh chạy
     */
    fun stopRunSound() {
        val streamId = streamMap["run"] ?: return
        soundPool?.stop(streamId)
        streamMap.remove("run")
    }

    /**
     * Phát âm thanh bị đánh (one-shot)
     */
    fun playHitSound() {
        if (!isSoundEnabled) return
        
        val soundId = soundMap["hit"] ?: return
        if (soundId == -1) return
        
        soundPool?.play(
            soundId,
            soundVolume,
            soundVolume,
            1,
            0, // No loop
            1.0f
        )
    }

    /**
     * Phát âm thanh tấn công (Fighter & Commander) - Sử dụng combo
     */
    fun playAttackSound() {
        if (!isSoundEnabled) return
        
        // Fighter và Commander cũng dùng combo như Archer
        archerComboCount = (archerComboCount % 3) + 1
        val soundKey = "slash_$archerComboCount"
        val soundId = soundMap[soundKey] ?: return
        if (soundId == -1) return
        
        soundPool?.play(
            soundId,
            soundVolume,
            soundVolume,
            1,
            0,
            1.0f
        )
    }

    /**
     * Phát âm thanh bắn cung (Archer - chế độ ranged)
     */
    fun playBowSound() {
        if (!isSoundEnabled) return
        
        val soundId = soundMap["bow"] ?: return
        if (soundId == -1) return
        
        soundPool?.play(
            soundId,
            soundVolume,
            soundVolume,
            1,
            0,
            1.0f
        )
    }

    /**
     * Phát âm thanh kiếm bay (Archer - skill khi giữ attack)
     * Phát loop để tạo hiệu ứng bay liên tục
     */
    fun playFlyingSwordSound() {
        if (!isSoundEnabled) return
        
        // Nếu đang phát rồi thì không phát lại
        if (streamMap.containsKey("flying_sword")) return
        
        val soundId = soundMap["flying_sword"] ?: return
        if (soundId == -1) return
        
        val streamId = soundPool?.play(
            soundId,
            soundVolume,
            soundVolume,
            1,
            -1, // Loop infinitely
            1.0f
        ) ?: -1
        
        if (streamId != -1) {
            streamMap["flying_sword"] = streamId
        }
    }
    
    /**
     * Dừng âm thanh kiếm bay
     */
    fun stopFlyingSwordSound() {
        val streamId = streamMap["flying_sword"] ?: return
        soundPool?.stop(streamId)
        streamMap.remove("flying_sword")
    }

    /**
     * Phát âm thanh chém (Archer - chế độ melee)
     * Tự động rotate qua 3 âm thanh chém
     */
    fun playSlashSound() {
        if (!isSoundEnabled) return
        
        // Rotate qua 3 âm thanh chém
        archerComboCount = (archerComboCount % 3) + 1
        
        val soundKey = "slash_$archerComboCount"
        val soundId = soundMap[soundKey] ?: return
        if (soundId == -1) return
        
        soundPool?.play(
            soundId,
            soundVolume,
            soundVolume,
            1,
            0,
            1.0f
        )
    }

    /**
     * Reset combo counter (khi dừng tấn công lâu)
     */
    fun resetCombo() {
        archerComboCount = 0
    }

    // ===== ÂM THANH ENEMY =====
    
    /**
     * Phát âm thanh Skeleton tấn công (gay_dap)
     */
    fun playSkeletonAttackSound() {
        if (!isSoundEnabled) return
        
        val soundId = soundMap["skeleton_attack"] ?: return
        if (soundId == -1) return
        
        soundPool?.play(soundId, soundVolume * 0.7f, soundVolume * 0.7f, 0, 0, 1.0f)
    }

    /**
     * Phát âm thanh Demon tấn công (dam_giao)
     */
    fun playDemonAttackSound() {
        if (!isSoundEnabled) return
        
        val soundId = soundMap["demon_attack"] ?: return
        if (soundId == -1) return
        
        soundPool?.play(soundId, soundVolume * 0.7f, soundVolume * 0.7f, 0, 0, 1.0f)
    }

    /**
     * Phát âm thanh Medusa tấn công (nem_da)
     */
    fun playMedusaAttackSound() {
        if (!isSoundEnabled) return
        
        val soundId = soundMap["medusa_attack"] ?: return
        if (soundId == -1) return
        
        soundPool?.play(soundId, soundVolume * 0.7f, soundVolume * 0.7f, 0, 0, 1.0f)
    }

    /**
     * Phát âm thanh Jinn tấn công (cau_gio)
     */
    fun playJinnAttackSound() {
        if (!isSoundEnabled) return
        
        val soundId = soundMap["jinn_attack"] ?: return
        if (soundId == -1) return
        
        soundPool?.play(soundId, soundVolume * 0.7f, soundVolume * 0.7f, 0, 0, 1.0f)
    }

    /**
     * Phát âm thanh SmallDragon tấn công (lua_bay)
     */
    fun playSmallDragonAttackSound() {
        if (!isSoundEnabled) return
        
        val soundId = soundMap["small_dragon_attack"] ?: return
        if (soundId == -1) return
        
        soundPool?.play(soundId, soundVolume * 0.7f, soundVolume * 0.7f, 0, 0, 1.0f)
    }

    /**
     * Phát âm thanh Dragon tấn công (phun_lua)
     */
    fun playDragonAttackSound() {
        if (!isSoundEnabled) return
        
        val soundId = soundMap["dragon_attack"] ?: return
        if (soundId == -1) return
        
        soundPool?.play(soundId, soundVolume * 0.7f, soundVolume * 0.7f, 0, 0, 1.0f)
    }

    /**
     * Phát âm thanh chiến thắng (one-shot)
     */
    fun playVictorySound() {
        if (!isSoundEnabled) return
        
        val soundId = soundMap["victory"] ?: return
        if (soundId == -1) return
        
        soundPool?.play(
            soundId,
            musicVolume,
            musicVolume,
            1,
            0,
            1.0f
        )
    }

    /**
     * Bật/tắt âm thanh
     */
    fun setSoundEnabled(enabled: Boolean) {
        isSoundEnabled = enabled
        if (!enabled) {
            stopAllSounds()
        }
    }

    /**
     * Bật/tắt nhạc nền
     */
    fun setMusicEnabled(enabled: Boolean) {
        isMusicEnabled = enabled
    }

    /**
     * Đặt volume âm thanh (0.0 - 1.0)
     */
    fun setSoundVolume(volume: Float) {
        soundVolume = volume.coerceIn(0f, 1f)
    }

    /**
     * Dừng tất cả âm thanh
     */
    fun stopAllSounds() {
        streamMap.values.forEach { streamId ->
            soundPool?.stop(streamId)
        }
        streamMap.clear()
    }

    /**
     * Tạm dừng tất cả âm thanh
     */
    fun pauseAllSounds() {
        streamMap.values.forEach { streamId ->
            soundPool?.pause(streamId)
        }
    }

    /**
     * Tiếp tục phát âm thanh đã tạm dừng
     */
    fun resumeAllSounds() {
        streamMap.values.forEach { streamId ->
            soundPool?.resume(streamId)
        }
    }

    // ===== BACKGROUND MUSIC MANAGEMENT =====
    
    /**
     * Khởi tạo nhạc nền
     */
    private fun initBackgroundMusic() {
        try {
            val afd = appContext.assets.openFd("music_background/DreamscapeDrift.mp3")
            backgroundMusic = MediaPlayer().apply {
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                isLooping = true // Phát lặp lại
                setVolume(musicVolume, musicVolume)
                prepare()
                
                // Tự động phát nhạc khi game bắt đầu
                if (isMusicEnabled) {
                    start()
                }
            }
            afd.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Phát nhạc nền
     */
    fun playBackgroundMusic() {
        if (isMusicEnabled && backgroundMusic?.isPlaying == false) {
            backgroundMusic?.start()
        }
    }
    
    /**
     * Dừng nhạc nền
     */
    fun stopBackgroundMusic() {
        backgroundMusic?.pause()
        backgroundMusic?.seekTo(0)
    }
    
    /**
     * Tạm dừng nhạc nền
     */
    fun pauseBackgroundMusic() {
        backgroundMusic?.pause()
    }
    
    /**
     * Tiếp tục nhạc nền
     */
    fun resumeBackgroundMusic() {
        if (isMusicEnabled) {
            backgroundMusic?.start()
        }
    }
    
    /**
     * Đặt âm lượng nhạc nền
     */
    fun setMusicVolume(volume: Float) {
        musicVolume = volume.coerceIn(0f, 1f)
        backgroundMusic?.setVolume(musicVolume, musicVolume)
    }
    
    /**
     * Bật/tắt nhạc nền
     */
    fun toggleMusic(enabled: Boolean) {
        isMusicEnabled = enabled
        if (enabled) {
            playBackgroundMusic()
        } else {
            pauseBackgroundMusic()
        }
    }

    /**
     * Giải phóng tài nguyên
     */
    fun release() {
        stopAllSounds()
        stopBackgroundMusic()
        backgroundMusic?.release()
        backgroundMusic = null
        soundPool?.release()
        soundPool = null
        soundMap.clear()
        streamMap.clear()
    }
}
