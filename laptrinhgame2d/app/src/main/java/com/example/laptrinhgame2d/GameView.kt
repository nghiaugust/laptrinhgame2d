package com.example.laptrinhgame2d

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.laptrinhgame2d.enemies.Demon
import com.example.laptrinhgame2d.enemies.Dragon
import com.example.laptrinhgame2d.enemies.Jinn
import com.example.laptrinhgame2d.enemies.Medusa
import com.example.laptrinhgame2d.enemies.Skeleton
import com.example.laptrinhgame2d.enemies.SmallDragon
import com.example.laptrinhgame2d.heroes.Fighter
import com.example.laptrinhgame2d.heroes.SamuraiArcher
import com.example.laptrinhgame2d.heroes.SamuraiCommander
import com.example.laptrinhgame2d.items.HealthHeart
import com.example.laptrinhgame2d.items.ItemDropConfig
import com.example.laptrinhgame2d.items.skills.BlackHoleSkillItem
import com.example.laptrinhgame2d.items.skills.BlackHoleEffect
import com.example.laptrinhgame2d.items.skills.BlackHoleSkillButton
import com.example.laptrinhgame2d.items.skills.LaserBeamSkillItem
import com.example.laptrinhgame2d.items.skills.LaserBeamEffect
import com.example.laptrinhgame2d.items.skills.LaserBeamSkillButton
import com.example.laptrinhgame2d.items.skills.ShieldSkillItem
import com.example.laptrinhgame2d.items.skills.ShieldEffect
import com.example.laptrinhgame2d.items.skills.ShieldSkillButton
import com.example.laptrinhgame2d.items.skills.PickupButton
import com.example.laptrinhgame2d.maps.DesertMap
import com.example.laptrinhgame2d.maps.GrasslandMap
import com.example.laptrinhgame2d.maps.VolcanoMap
import com.example.laptrinhgame2d.victory.VictoryDialog
import com.example.laptrinhgame2d.victory.VictoryHistoryActivity
import com.example.laptrinhgame2d.victory.VictoryManager
import com.example.laptrinhgame2d.victory.VictoryRecord

class GameView(
    context: Context,
    private val characterType: String = "Fighter",
    private val mapType: Int = 1
) : SurfaceView(context), SurfaceHolder.Callback {

    private var gameThread: GameThread? = null
    private val gameContext: Context = context
    private var fighter: Fighter? = null
    private var samuraiArcher: SamuraiArcher? = null
    private var samuraiCommander: SamuraiCommander? = null
    private val joystick: Joystick
    private val attackButton: GameButton
    private val shieldButton: GameButton
    private val jumpButton: GameButton
    private val bowButton: GameButton
    private val settingsButton: GameButton

    // ===== LEVEL MANAGEMENT SYSTEM =====
    private var levelManager: LevelManager = LevelManager(context)
    private var currentLevel: LevelManager.Level = levelManager.getLevelFromMapType(mapType)
    private var levelConfig: LevelManager.LevelConfig = levelManager.getLevelConfig(currentLevel)

    // ===== MAP SYSTEM =====
    private var grasslandMap: GrasslandMap? = null
    private var desertMap: DesertMap? = null
    private var volcanoMap: VolcanoMap? = null

    // Enemies - Sử dụng config từ LevelManager
    private val skeletons = mutableListOf<Skeleton>()
    private val demons = mutableListOf<Demon>()
    private val medusas = mutableListOf<Medusa>()
    private val jinns = mutableListOf<Jinn>()
    private val smallDragons = mutableListOf<SmallDragon>()
    private val dragons = mutableListOf<Dragon>()

    // ===== ITEM SYSTEM =====
    private val healthHearts = mutableListOf<HealthHeart>()
    
    // ===== SKILL SYSTEM =====
    private val blackHoleSkillItems = mutableListOf<BlackHoleSkillItem>()
    private val blackHoleEffects = mutableListOf<BlackHoleEffect>()
    private var hasBlackHoleSkill = false
    private lateinit var pickupButton: PickupButton
    private var blackHoleSkillButton: BlackHoleSkillButton? = null
    
    // Laser Beam Skill
    private val laserBeamSkillItems = mutableListOf<LaserBeamSkillItem>()
    private val laserBeamEffects = mutableListOf<LaserBeamEffect>()
    private var hasLaserBeamSkill = false
    private var laserBeamSkillButton: LaserBeamSkillButton? = null
    
    // Shield Skill
    private val shieldSkillItems = mutableListOf<ShieldSkillItem>()
    private var shieldEffect: ShieldEffect? = null
    private var hasShieldSkill = false
    private var shieldSkillButton: ShieldSkillButton? = null

    // Camera
    private var cameraX = 0f
    private var cameraY = 0f

    // Game States
    private var gameOverDialog: GameOverDialog? = null
    private var isGameOver = false
    private var pauseMenuDialog: PauseMenuDialog? = null
    private var isPaused = false

    // Victory system
    private var victoryDialog: VictoryDialog? = null
    private var levelVictoryDialog: LevelVictoryDialog? = null
    private var gameCompleteDialog: GameCompleteDialog? = null
    private var isVictory = false
    private var gameStartTime: Long = 0
    private var victoryManager: VictoryManager

    // ===== WAVE SYSTEM =====
    private var currentWaveNumber: Int = 0
    private var lastWaveClearedTime: Long = 0
    private var isWaitingForNextWave: Boolean = false
    private var wavesCompleted: Int = 0
    private var spawnConfig: SpawnConfig.LevelSpawnConfig? = null
    private var currentWaveEnemyCount: Int = 0

    // ===== SOUND SYSTEM =====
    private var soundManager: SoundManager = SoundManager(context, characterType)
    private var isPlayerRunning: Boolean = false
    private var previousSkillCount: Int = 0

    // ===== GAME MODE SYSTEM =====
    private var gameModeConfig: GameModeConfig.LevelModeConfig? = null
    private var gameTimer: Int = 0
    private var frameCounter: Int = 0
    private var remainingTime: Int = 0
    private var flawlessScore: Int = 2000
    private var maxComboAchieved: Int = 0
    private var enemiesKilled: Int = 0

    // Lấy tổng số quái từ LevelConfig thay vì hardcode
    private val totalEnemies: Int
        get() = levelConfig.totalEnemies

    // ===== FIX: CHỈ MỘT HANDLER DUY NHẤT =====
    private val mainHandler = Handler(Looper.getMainLooper())

    init {
        holder.addCallback(this)

        // Khởi tạo Level Management
        currentLevel = levelManager.getLevelFromMapType(mapType)
        levelConfig = levelManager.getLevelConfig(currentLevel)

        // Khởi tạo Game Mode Config
        gameModeConfig = GameModeConfig.getConfigForLevel(mapType)
        remainingTime = gameModeConfig?.timeLimit ?: 0
        gameTimer = 0
        frameCounter = 0
        flawlessScore = 2000
        maxComboAchieved = 0
        enemiesKilled = 0

        // Khởi tạo Spawn Config
        spawnConfig = SpawnConfig.getSpawnConfig(currentLevel)
        currentWaveNumber = 1
        lastWaveClearedTime = 0
        isWaitingForNextWave = false
        currentWaveEnemyCount = 0

        victoryManager = VictoryManager(context)

        joystick = Joystick(200f, 0f, 120f, 100f)

        // Giảm kích thước các nút cho phù hợp
        attackButton = GameButton(0f, 0f, 70f, "Attack", Color.rgb(255, 100, 100))
        shieldButton = GameButton(0f, 0f, 70f, "Shield", Color.rgb(100, 100, 255))
        jumpButton = GameButton(0f, 0f, 70f, "Jump", Color.rgb(100, 255, 100))
        bowButton = GameButton(0f, 0f, 70f, "Bow", Color.rgb(255, 165, 0))
        settingsButton = GameButton(0f, 0f, 50f, "⚙", Color.rgb(150, 150, 150))
        
        // Khởi tạo pickup button (ẩn mặc định)
        pickupButton = PickupButton(0f, 0f)
        pickupButton.hide()

        when (characterType) {
            "Fighter" -> {
                fighter = Fighter(context, 500f, 400f)
                getSavedFighterHP()?.let { savedHP ->
                    fighter?.setHealth(savedHP)
                }
            }
            "Samurai_Archer" -> {
                samuraiArcher = SamuraiArcher(context, 500f, 400f)
                getSavedSamuraiArcherHP()?.let { savedHP ->
                    samuraiArcher?.setHealth(savedHP)
                }
            }
            "Samurai_Commander" -> {
                samuraiCommander = SamuraiCommander(context, 500f, 400f)
                getSavedSamuraiCommanderHP()?.let { savedHP ->
                    samuraiCommander?.setHealth(savedHP)
                }
            }
        }
    }

    // ===== WAVE SPAWN SYSTEM - SPAWN KHI WAVE TRƯỚC CHẾT HẾT =====
    private fun spawnWave(waveConfig: SpawnConfig.WaveConfig) {
        var spawnIndex = 0
        var enemiesSpawned = 0

        // Lấy vị trí player hiện tại
        val playerX = fighter?.getX() ?: samuraiArcher?.getX() ?: samuraiCommander?.getX() ?: 0f

        // Chỉ điều chỉnh baseX nếu nó quá xa camera (> 2000) - spawn ở phía trước player
        val adjustedBaseX = if (waveConfig.baseX > 2000f) {
            // Spawn trước mặt player khoảng 600-1000 pixels
            playerX + 800f
        } else {
            // Giữ nguyên vị trí config gốc
            waveConfig.baseX
        }

        // Tạo config tạm với baseX đã điều chỉnh
        val adjustedConfig = SpawnConfig.WaveConfig(
            waveNumber = waveConfig.waveNumber,
            spawnOnPreviousWaveCleared = waveConfig.spawnOnPreviousWaveCleared,
            delaySeconds = waveConfig.delaySeconds,
            enemies = waveConfig.enemies,
            baseX = adjustedBaseX,
            baseY = waveConfig.baseY,
            spacingX = waveConfig.spacingX,
            randomRangeX = waveConfig.randomRangeX,
            randomRangeY = waveConfig.randomRangeY,
            pattern = waveConfig.pattern
        )

        // Spawn từng loại quái theo config của wave
        val enemyTypes = waveConfig.enemies.entries.toList()

        for ((enemyType, count) in enemyTypes) {
            for (i in 0 until count) {
                val (x, y) = SpawnConfig.calculateSpawnPosition(
                    adjustedConfig,
                    spawnIndex,
                    waveConfig.totalEnemies
                )
                spawnIndex++

                when (enemyType) {
                    SpawnConfig.EnemyType.SKELETON -> {
                        skeletons.add(Skeleton(gameContext, x, y))
                        enemiesSpawned++
                    }
                    SpawnConfig.EnemyType.DEMON -> {
                        demons.add(Demon(gameContext, x, y))
                        enemiesSpawned++
                    }
                    SpawnConfig.EnemyType.MEDUSA -> {
                        medusas.add(Medusa(gameContext, x, y))
                        enemiesSpawned++
                    }
                    SpawnConfig.EnemyType.JINN -> {
                        jinns.add(Jinn(gameContext, x, y))
                        enemiesSpawned++
                    }
                    SpawnConfig.EnemyType.SMALL_DRAGON -> {
                        smallDragons.add(SmallDragon(gameContext, x, y))
                        enemiesSpawned++
                    }
                    SpawnConfig.EnemyType.DRAGON -> {
                        dragons.add(Dragon(gameContext, x, y))
                        enemiesSpawned++
                    }
                }
            }
        }

        currentWaveEnemyCount = enemiesSpawned
        wavesCompleted++
    }

    // ===== KIỂM TRA VÀ SPAWN WAVE TIẾP THEO - DỰA TRÊN WAVE TRƯỚC CHẾT HẾT =====
    private fun checkAndSpawnNextWave() {
        spawnConfig?.let { config ->
            // Nếu chưa spawn wave đầu tiên
            if (currentWaveNumber == 1 && currentWaveEnemyCount == 0) {
                val firstWave = config.waves.find { it.waveNumber == 1 }
                firstWave?.let { wave ->
                    spawnWave(wave)
                    currentWaveNumber = 2 // Chuẩn bị cho wave tiếp theo
                }
                return
            }

            // Kiểm tra wave hiện tại đã chết hết chưa
            val currentAliveEnemies = skeletons.count { !it.isDead() } +
                    demons.count { !it.isDead() } +
                    medusas.count { !it.isDead() } +
                    jinns.count { !it.isDead() } +
                    smallDragons.count { !it.isDead() } +
                    dragons.count { !it.isDead() }

            // Nếu wave hiện tại đã chết hết (không còn quái sống) và còn wave tiếp theo
            if (currentAliveEnemies == 0 && currentWaveNumber <= config.totalWaves) {
                if (!isWaitingForNextWave) {
                    // Bắt đầu đợi để spawn wave tiếp theo
                    lastWaveClearedTime = System.currentTimeMillis()
                    isWaitingForNextWave = true
                } else {
                    // Đang chờ - kiểm tra đã đủ delay chưa
                    val nextWave = config.waves.find { it.waveNumber == currentWaveNumber }
                    nextWave?.let { wave ->
                        val currentTime = System.currentTimeMillis()
                        val elapsedSeconds = (currentTime - lastWaveClearedTime) / 1000f

                        if (elapsedSeconds >= wave.delaySeconds) {
                            // Đã đủ delay, spawn wave mới
                            spawnWave(wave)
                            currentWaveNumber++
                            isWaitingForNextWave = false
                        }
                    }
                }
            }
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        val screenWidth = width.toFloat()
        val screenHeight = height.toFloat()

        // Khởi tạo 3 maps
        grasslandMap = GrasslandMap(gameContext, width, height)
        desertMap = DesertMap(gameContext, width, height)
        volcanoMap = VolcanoMap(gameContext, width, height)

        // Lấy groundY từ map hiện tại
        val groundY = when (mapType) {
            2 -> desertMap?.groundY ?: (screenHeight * 0.75f)
            3 -> volcanoMap?.groundY ?: (screenHeight * 0.75f)
            else -> grasslandMap?.groundY ?: (screenHeight * 0.75f)
        }

        when (characterType) {
            "Fighter" -> {
                fighter?.y = groundY - 200f
                fighter?.setGroundY(groundY - 200f)
            }
            "Samurai_Archer" -> {
                samuraiArcher?.y = groundY - 200f
                samuraiArcher?.setGroundY(groundY - 200f)
            }
            "Samurai_Commander" -> {
                samuraiCommander?.y = groundY - 200f
                samuraiCommander?.setGroundY(groundY - 200f)
            }
        }

        skeletons.forEach { skeleton ->
            skeleton.y = groundY - 200f
        }

        demons.forEach { demon ->
            demon.y = groundY - 180f
        }

        medusas.forEach { medusa ->
            medusa.y = groundY - 200f
        }

        jinns.forEach { jinn ->
            jinn.y = groundY - 200f
        }

        smallDragons.forEach { dragon ->
            dragon.y = groundY - 200f
        }

        dragons.forEach { dragon ->
            dragon.y = groundY - 200f
        }

        joystick.centerY = screenHeight - 200f

        // Settings button - góc trên bên phải (nhỏ gọn hơn)
        settingsButton.x = screenWidth - 80f
        settingsButton.y = 60f

        if (characterType == "Fighter") {
            // Attack button - góc dưới bên phải
            attackButton.x = screenWidth - 100f
            attackButton.y = screenHeight - 150f

            // Shield button - bên trái attack button
            shieldButton.x = screenWidth - 220f
            shieldButton.y = screenHeight - 150f

            // Jump button - phía trên giữa attack và shield
            jumpButton.x = screenWidth - 160f
            jumpButton.y = screenHeight - 270f
        } else {
            // Attack button - góc dưới bên phải
            attackButton.x = screenWidth - 100f
            attackButton.y = screenHeight - 150f

            // Bow button - bên trái attack button
            bowButton.x = screenWidth - 220f
            bowButton.y = screenHeight - 150f

            // Jump button - phía trên giữa attack và bow
            jumpButton.x = screenWidth - 160f
            jumpButton.y = screenHeight - 270f
        }
        
        // Khởi tạo black hole skill button nếu đã unlock
        if (hasBlackHoleSkill && blackHoleSkillButton == null) {
            blackHoleSkillButton = BlackHoleSkillButton(
                screenWidth - 350f,  // Bên trái nút attack
                screenHeight - 200f
            )
        }

        gameThread = GameThread(holder, this)
        gameThread?.running = true
        gameThread?.start()

        gameStartTime = System.currentTimeMillis()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        var retry = true
        gameThread?.running = false
        while (retry) {
            try {
                gameThread?.join()
                retry = false
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

        // Giải phóng tài nguyên âm thanh
        soundManager.release()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val index = event.actionIndex
                val x = event.getX(index)
                val y = event.getY(index)
                val pointerId = event.getPointerId(index)

                if (x < width / 2) {
                    joystick.onTouchDown(x, y, pointerId)
                }

                if (settingsButton.isPressed(x, y)) {
                    settingsButton.onTouch(pointerId)
                    showPauseMenu()
                    return true
                }
                
                // Pickup button
                if (pickupButton.isVisible() && pickupButton.isPressed(x, y)) {
                    pickupButton.onTouch(pointerId)
                    tryPickupSkill()
                    return true
                }
                
                // Black Hole Skill button
                if (hasBlackHoleSkill && blackHoleSkillButton?.isPressed(x, y) == true) {
                    if (blackHoleSkillButton?.isReady() == true) {
                        blackHoleSkillButton?.onTouch(pointerId)
                        castBlackHole()
                    }
                    return true
                }
                
                // Laser Beam Skill button
                if (hasLaserBeamSkill && laserBeamSkillButton?.isPressed(x, y) == true) {
                    if (laserBeamSkillButton?.isReady() == true) {
                        laserBeamSkillButton?.onTouch(pointerId)
                        castLaserBeam()
                    }
                    return true
                }
                
                // Shield Skill button
                if (hasShieldSkill && shieldSkillButton?.isPressed(x, y) == true) {
                    if (shieldSkillButton?.isReady() == true) {
                        shieldSkillButton?.onTouch(pointerId)
                        castShield()
                    }
                    return true
                }

                if (attackButton.isPressed(x, y)) {
                    attackButton.onTouch(pointerId)

                    // Kiểm tra xem hero có thể tấn công không TRƯỚC khi phát âm thanh
                    val canAttack = when (characterType) {
                        "Fighter" -> fighter?.canAttack() ?: false
                        "Samurai_Commander" -> samuraiCommander?.canAttack() ?: false
                        "Samurai_Archer" -> samuraiArcher?.canAttack() ?: false
                        else -> false
                    }

                    // Chỉ phát âm thanh và tấn công nếu hero KHÔNG bị khóa animation
                    if (canAttack) {
                        // Phát âm thanh tấn công theo loại hero
                        when (characterType) {
                            "Fighter", "Samurai_Commander" -> {
                                soundManager.playAttackSound()
                            }
                            "Samurai_Archer" -> {
                                // Archer phức tạp hơn, kiểm tra combat mode
                                samuraiArcher?.let { archer ->
                                    if (archer.getCombatMode() == SamuraiArcher.CombatMode.MELEE) {
                                        // Chế độ cận chiến - phát âm thanh chém
                                        soundManager.playSlashSound()
                                    } else {
                                        // Chế độ xa - phát âm thanh bắn cung
                                        soundManager.playBowSound()
                                    }
                                }
                            }
                        }

                        // Thực hiện tấn công
                        fighter?.attack()
                        samuraiArcher?.attack()
                        samuraiCommander?.attack()
                    }
                }

                if (characterType == "Samurai_Archer" && bowButton.isPressed(x, y)) {
                    bowButton.onTouch(pointerId)
                    samuraiArcher?.let { archer ->
                        // Kiểm tra có thể tấn công không
                        if (archer.canAttack()) {
                            if (archer.getCombatMode() == SamuraiArcher.CombatMode.MELEE) {
                                archer.switchCombatMode()
                            }
                            archer.attack()

                            // Phát âm thanh bắn cung (bow button luôn là ranged mode)
                            soundManager.playBowSound()
                        }
                    }
                }

                if (characterType == "Fighter" && shieldButton.isPressed(x, y)) {
                    shieldButton.onTouch(pointerId)
                    fighter?.activateShield()
                }

                if (jumpButton.isPressed(x, y)) {
                    jumpButton.onTouch(pointerId)
                    fighter?.jump()
                    samuraiArcher?.jump()
                    samuraiCommander?.jump()
                }
            }

            MotionEvent.ACTION_MOVE -> {
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                val index = event.actionIndex
                val pointerId = event.getPointerId(index)

                joystick.onTouchUp(pointerId)

                if (attackButton.pointerId == pointerId) {
                    attackButton.reset()

                    // Archer: Khi release attack (giữ lâu) → skill sẽ được bắn
                    // Âm thanh sẽ được phát trong SamuraiArcher khi skill được bắn
                    if (characterType == "Samurai_Archer") {
                        samuraiArcher?.releaseAttack()
                    }
                }

                if (bowButton.pointerId == pointerId) {
                    bowButton.reset()
                }

                if (shieldButton.pointerId == pointerId) {
                    shieldButton.reset()
                    fighter?.deactivateShield()
                }

                if (jumpButton.pointerId == pointerId) {
                    jumpButton.reset()
                }
                
                // Reset skill buttons
                pickupButton.reset()
                blackHoleSkillButton?.reset()
            }
        }
        return true
    }

    fun update() {
        if (isGameOver || isPaused || isVictory) return

        // ===== GAME TIMER & TIME LIMIT CHECK =====
        frameCounter++
        if (frameCounter >= 60) { // 60 FPS = 1 giây
            frameCounter = 0
            gameTimer++

            // Kiểm tra time limit (nếu có)
            gameModeConfig?.let { config ->
                if (config.timeLimit != null) {
                    remainingTime = config.timeLimit - gameTimer

                    // Hết thời gian
                    if (remainingTime <= 0 && !isGameOver && !isVictory) {
                        // SURVIVAL mode: hết thời gian = THẮNG
                        if (config.gameMode == GameModeConfig.GameMode.SURVIVAL) {
                            isVictory = true
                            mainHandler.postDelayed({
                                showVictory()
                            }, 500)
                        } else {
                            // Các mode khác: hết thời gian = THUA
                            isGameOver = true
                            mainHandler.postDelayed({
                                showGameOver()
                            }, 500)
                        }
                        return
                    }
                }
            }
        }

        // ===== WAVE SPAWN SYSTEM =====
        checkAndSpawnNextWave()

        // Update map theo loại
        when (mapType) {
            1 -> grasslandMap?.update(cameraX, cameraY)
            2 -> desertMap?.update(cameraX, cameraY)
            3 -> volcanoMap?.update(cameraX, cameraY)
        }

        val joystickX = joystick.getX()
        val joystickY = 0f

        fighter?.update(joystickX, joystickY)
        samuraiArcher?.update(joystickX, joystickY)
        samuraiCommander?.update(joystickX, joystickY)

        // ===== QUẢN LÝ ÂM THANH CHẠY =====
        val isMoving = joystickX != 0f || joystickY != 0f
        if (isMoving && !isPlayerRunning) {
            // Bắt đầu chạy, phát âm thanh
            soundManager.playRunSound()
            isPlayerRunning = true
        } else if (!isMoving && isPlayerRunning) {
            // Dừng chạy, tắt âm thanh
            soundManager.stopRunSound()
            isPlayerRunning = false
        }

        // ===== QUẢN LÝ ÂM THANH KIẾM BAY (ARCHER) =====
        if (characterType == "Samurai_Archer") {
            val activeSkills = samuraiArcher?.getSkillProjectiles()?.count { it.isActive() } ?: 0

            if (activeSkills > 0 && previousSkillCount == 0) {
                // Có skill mới được bắn ra -> phát âm thanh
                soundManager.playFlyingSwordSound()
            } else if (activeSkills == 0 && previousSkillCount > 0) {
                // Tất cả skill đã hết (va chạm hoặc hết tầm) -> dừng âm thanh
                soundManager.stopFlyingSwordSound()
            }

            previousSkillCount = activeSkills
        }

        // Lấy groundY từ map hiện tại
        val groundY = when (mapType) {
            2 -> desertMap?.groundY ?: (height * 0.75f)
            3 -> volcanoMap?.groundY ?: (height * 0.75f)
            else -> grasslandMap?.groundY ?: (height * 0.75f)
        }

        skeletons.forEach { skeleton ->
            skeleton.y = groundY - 200f
        }

        demons.forEach { demon ->
            demon.y = groundY - 180f
        }

        medusas.forEach { medusa ->
            medusa.y = groundY - 200f
        }

        jinns.forEach { jinn ->
            jinn.y = groundY - 200f
        }

        smallDragons.forEach { dragon ->
            dragon.y = groundY - 200f
        }

        dragons.forEach { dragon ->
            dragon.y = groundY - 200f
        }

        // Lấy vị trí và trạng thái của nhân vật đang chơi
        val playerX = fighter?.getX() ?: samuraiArcher?.getX() ?: samuraiCommander?.getX() ?: 0f
        val playerY = fighter?.y ?: samuraiArcher?.y ?: samuraiCommander?.y ?: 0f
        val playerIsDead = fighter?.isDead() ?: samuraiArcher?.isDead() ?: samuraiCommander?.isDead() ?: false
        val playerFacingRight = fighter?.getFacingRight() ?: samuraiArcher?.getFacingRight() ?: samuraiCommander?.getFacingRight() ?: true

        if (playerIsDead && !isGameOver) {
            isGameOver = true
            showGameOver()
        }

        val cameraDeadZoneLeft = width * 0.35f
        val cameraDeadZoneRight = width * 0.65f

        val playerScreenX = playerX - cameraX

        if (playerScreenX < cameraDeadZoneLeft) {
            cameraX = playerX - cameraDeadZoneLeft
        } else if (playerScreenX > cameraDeadZoneRight) {
            cameraX = playerX - cameraDeadZoneRight
        }

        cameraY = 0f

        // ===== KIỂM TRA VÀ SPAWN WAVE TIẾP THEO (TRƯỚC KHI REMOVE) =====
        checkAndSpawnNextWave()

        // Remove enemies đã chết (counter đã được tăng trong damageSkeletonAndCheck, damageDemonAndCheck, etc.)
        skeletons.removeAll { it.shouldBeRemoved() }
        demons.removeAll { it.shouldBeRemoved() }
        medusas.removeAll { it.shouldRemove() }
        jinns.removeAll { it.shouldBeRemoved() }
        smallDragons.removeAll { it.shouldBeRemoved() }
        dragons.removeAll { it.shouldBeRemoved() }

        // ===== KIỂM TRA CHIẾN THẮNG VỚI WAVE SYSTEM =====
        if (!isVictory) {
            // Chỉ check victory khi đã spawn HẾT tất cả wave
            val allWavesSpawned = spawnConfig?.let { config ->
                currentWaveNumber > config.totalWaves
            } ?: false

            if (allWavesSpawned) {
                // Kiểm tra tất cả quái đã chết hết chưa
                val allEnemiesDead =
                    skeletons.all { it.isDead() } &&
                            demons.all { it.isDead() } &&
                            (medusas.isEmpty() || medusas.all { it.isDead() }) &&
                            (jinns.isEmpty() || jinns.all { it.isDead() }) &&
                            (smallDragons.isEmpty() || smallDragons.all { it.isDead() }) &&
                            (dragons.isEmpty() || dragons.all { it.isDead() })

                if (allEnemiesDead) {
                    isVictory = true
                    showVictory()
                }
            }
        }

        // Update enemy behaviors (giữ nguyên logic cũ - tôi sẽ viết tắt để tiết kiệm chỗ)
        for (skeleton in skeletons) {
            skeleton.update(playerX, playerY)
            if (!skeleton.isDead() && skeleton.canDealDamage()) {
                val attackRange = 200f
                val isPlayerColliding = fighter?.isCollidingWith(skeleton.getX(), skeleton.y, attackRange)
                    ?: samuraiArcher?.isCollidingWith(skeleton.getX(), skeleton.y, attackRange)
                    ?: samuraiCommander?.isCollidingWith(skeleton.getX(), skeleton.y, attackRange)
                    ?: false
                if (isPlayerColliding) {
                    // Phát âm thanh Skeleton tấn công
                    soundManager.playSkeletonAttackSound()

                    applyDamageToPlayer(skeleton.getAttackDamage())
                    skeleton.markDamageDealt()

                    // Trừ điểm Flawless và phát âm thanh bị đánh
                    onPlayerHit()
                }
            }
        }

        for (demon in demons) {
            demon.update(playerX, playerY)
            if (!demon.isDead() && demon.canDealDamage()) {
                val attackRange = 250f
                val isPlayerColliding = fighter?.isCollidingWith(demon.getX(), demon.y, attackRange)
                    ?: samuraiArcher?.isCollidingWith(demon.getX(), demon.y, attackRange)
                    ?: samuraiCommander?.isCollidingWith(demon.getX(), demon.y, attackRange)
                    ?: false
                if (isPlayerColliding) {
                    // Phát âm thanh Demon tấn công
                    soundManager.playDemonAttackSound()

                    applyDamageToPlayer(demon.getAttackDamage())
                    demon.markDamageDealt()

                    // Phát âm thanh bị đánh
                    onPlayerHit()
                }
            }
        }

        for (medusa in medusas) {
            medusa.update(playerX, playerY, playerIsDead)
            if (!medusa.isDead()) {
                for (projectile in medusa.projectiles) {
                    if (projectile.isCollidingWith(playerX, playerY, 100f)) {
                        if (!projectile.canDealDamage()) {
                            projectile.startExploding()
                        }
                        if (projectile.canDealDamage()) {
                            // Phát âm thanh Medusa tấn công
                            soundManager.playMedusaAttackSound()

                            applyDamageToPlayer(projectile.getDamage())
                            projectile.markDamageDealt()

                            // Phát âm thanh bị đánh
                            onPlayerHit()
                        }
                    }
                }
            }
        }

        for (jinn in jinns) {
            jinn.update(playerX, playerY)
            if (!jinn.isDead()) {
                for (projectile in jinn.projectiles) {
                    if (projectile.canDealDamage() && projectile.isCollidingWith(playerX, playerY, 100f)) {
                        // Phát âm thanh Jinn tấn công
                        soundManager.playJinnAttackSound()

                        applyDamageToPlayer(projectile.getDamage())
                        projectile.markDamageDealt()

                        // Phát âm thanh bị đánh
                        onPlayerHit()
                    }
                }
            }
        }

        for (dragon in smallDragons) {
            dragon.update(playerX, playerY)
            if (!dragon.isDead()) {
                for (projectile in dragon.projectiles) {
                    if (projectile.canDealDamage() && projectile.isCollidingWith(playerX, playerY, 110f)) {
                        // Phát âm thanh SmallDragon tấn công
                        soundManager.playSmallDragonAttackSound()

                        applyDamageToPlayer(projectile.getDamage())
                        projectile.markDamageDealt()

                        // Phát âm thanh bị đánh
                        onPlayerHit()
                    }
                }
            }
        }

        for (dragon in dragons) {
            dragon.update(playerX, playerY)
            if (!dragon.isDead()) {
                for (fire in dragon.fireProjectiles) {
                    if (fire.canDealDamage() && fire.isCollidingWith(playerX, playerY, 90f)) {
                        // Phát âm thanh Dragon tấn công
                        soundManager.playDragonAttackSound()

                        applyDamageToPlayer(fire.getDamage())
                        fire.markDamageDealt()

                        // Phát âm thanh bị đánh
                        onPlayerHit()
                    }
                }
            }
        }

        val canPlayerDealDamage = fighter?.canDealDamage() ?: samuraiArcher?.canDealDamage() ?: samuraiCommander?.canDealDamage() ?: false

        if (canPlayerDealDamage) {
            val attackRange = 200f
            val damage = fighter?.getAttackDamage() ?: samuraiArcher?.getAttackDamage() ?: samuraiCommander?.getAttackDamage() ?: 0

            for (skeleton in skeletons) {
                if (!skeleton.isDead() && skeleton.isCollidingWith(playerX, playerY, attackRange)) {
                    val dx = skeleton.getX() - playerX
                    val isFacingSkeleton = (dx > 0 && playerFacingRight) || (dx < 0 && !playerFacingRight)
                    if (isFacingSkeleton) {
                        damageSkeletonAndCheck(skeleton, damage)
                        fighter?.markDamageDealt()
                        samuraiArcher?.markDamageDealt()
                        samuraiCommander?.markDamageDealt()
                    }
                }
            }

            for (demon in demons) {
                if (!demon.isDead() && demon.isCollidingWith(playerX, playerY, attackRange)) {
                    val dx = demon.getX() - playerX
                    val isFacingDemon = (dx > 0 && playerFacingRight) || (dx < 0 && !playerFacingRight)
                    if (isFacingDemon) {
                        damageDemonAndCheck(demon, damage)
                        fighter?.markDamageDealt()
                        samuraiArcher?.markDamageDealt()
                        samuraiCommander?.markDamageDealt()
                        break
                    }
                }
            }

            for (medusa in medusas) {
                if (!medusa.isDead() && medusa.isCollidingWith(playerX, playerY, attackRange)) {
                    val dx = medusa.getX() - playerX
                    val isFacingMedusa = (dx > 0 && playerFacingRight) || (dx < 0 && !playerFacingRight)
                    if (isFacingMedusa) {
                        damageMedusaAndCheck(medusa, damage)
                        fighter?.markDamageDealt()
                        samuraiArcher?.markDamageDealt()
                        samuraiCommander?.markDamageDealt()
                        break
                    }
                }
            }

            for (jinn in jinns) {
                if (!jinn.isDead() && jinn.isCollidingWith(playerX, playerY, attackRange)) {
                    val dx = jinn.getX() - playerX
                    val isFacingJinn = (dx > 0 && playerFacingRight) || (dx < 0 && !playerFacingRight)
                    if (isFacingJinn) {
                        damageJinnAndCheck(jinn, damage)
                        fighter?.markDamageDealt()
                        samuraiArcher?.markDamageDealt()
                        samuraiCommander?.markDamageDealt()
                        break
                    }
                }
            }

            for (dragon in smallDragons) {
                if (!dragon.isDead() && dragon.isCollidingWith(playerX, playerY, attackRange)) {
                    val dx = dragon.getX() - playerX
                    val isFacingDragon = (dx > 0 && playerFacingRight) || (dx < 0 && !playerFacingRight)
                    if (isFacingDragon) {
                        damageSmallDragonAndCheck(dragon, damage)
                        fighter?.markDamageDealt()
                        samuraiArcher?.markDamageDealt()
                        samuraiCommander?.markDamageDealt()
                        break
                    }
                }
            }

            for (dragon in dragons) {
                if (!dragon.isDead() && dragon.isCollidingWith(playerX, playerY, attackRange)) {
                    val dx = dragon.getX() - playerX
                    val isFacingDragon = (dx > 0 && playerFacingRight) || (dx < 0 && !playerFacingRight)
                    if (isFacingDragon) {
                        damageDragonAndCheck(dragon, damage)
                        fighter?.markDamageDealt()
                        samuraiArcher?.markDamageDealt()
                        samuraiCommander?.markDamageDealt()
                        break
                    }
                }
            }
        }

        // Kiểm tra arrows của Samurai_Archer
        samuraiArcher?.getArrows()?.forEach { arrow ->
            // Check collision với tất cả enemies
            for (skeleton in skeletons) {
                if (!skeleton.isDead() && arrow.isActive() && arrow.checkCollision(skeleton.getX(), skeleton.y, 80f)) {
                    damageSkeletonAndCheck(skeleton, 15)
                    arrow.deactivate()
                    break
                }
            }
            for (demon in demons) {
                if (!demon.isDead() && arrow.isActive() && arrow.checkCollision(demon.getX(), demon.y, 80f)) {
                    damageDemonAndCheck(demon, 15)
                    arrow.deactivate()
                    break
                }
            }
            for (medusa in medusas) {
                if (!medusa.isDead() && arrow.isActive() && arrow.checkCollision(medusa.getX(), medusa.y, 80f)) {
                    damageMedusaAndCheck(medusa, 15)
                    arrow.deactivate()
                    break
                }
            }
            for (jinn in jinns) {
                if (!jinn.isDead() && arrow.isActive() && arrow.checkCollision(jinn.getX(), jinn.y, 80f)) {
                    damageJinnAndCheck(jinn, 15)
                    arrow.deactivate()
                    break
                }
            }
            for (smallDragon in smallDragons) {
                if (!smallDragon.isDead() && arrow.isActive() && arrow.checkCollision(smallDragon.getX(), smallDragon.y, 120f)) {
                    damageSmallDragonAndCheck(smallDragon, 15)
                    arrow.deactivate()
                    break
                }
            }
            for (dragon in dragons) {
                if (!dragon.isDead() && arrow.isActive() && arrow.checkCollision(dragon.getX(), dragon.y, 120f)) {
                    damageDragonAndCheck(dragon, 15)
                    arrow.deactivate()
                    break
                }
            }
        }

        // Tương tự cho skill projectiles
        samuraiArcher?.getSkillProjectiles()?.forEach { skillProj ->
            for (skeleton in skeletons) {
                if (!skeleton.isDead() && skillProj.isActive() && skillProj.checkCollision(skeleton.getX(), skeleton.y, 80f)) {
                    damageSkeletonAndCheck(skeleton, skillProj.getDamage())
                    skillProj.deactivate()
                    break
                }
            }
            for (demon in demons) {
                if (!demon.isDead() && skillProj.isActive() && skillProj.checkCollision(demon.getX(), demon.y, 80f)) {
                    damageDemonAndCheck(demon, skillProj.getDamage())
                    skillProj.deactivate()
                    break
                }
            }
            for (medusa in medusas) {
                if (!medusa.isDead() && skillProj.isActive() && skillProj.checkCollision(medusa.getX(), medusa.y, 80f)) {
                    damageMedusaAndCheck(medusa, skillProj.getDamage())
                    skillProj.deactivate()
                    break
                }
            }
            for (jinn in jinns) {
                if (!jinn.isDead() && skillProj.isActive() && skillProj.checkCollision(jinn.getX(), jinn.y, 80f)) {
                    damageJinnAndCheck(jinn, skillProj.getDamage())
                    skillProj.deactivate()
                    break
                }
            }
            for (smallDragon in smallDragons) {
                if (!smallDragon.isDead() && skillProj.isActive() && skillProj.checkCollision(smallDragon.getX(), smallDragon.y, 120f)) {
                    damageSmallDragonAndCheck(smallDragon, skillProj.getDamage())
                    skillProj.deactivate()
                    break
                }
            }
            for (dragon in dragons) {
                if (!dragon.isDead() && skillProj.isActive() && skillProj.checkCollision(dragon.getX(), dragon.y, 120f)) {
                    damageDragonAndCheck(dragon, skillProj.getDamage())
                    skillProj.deactivate()
                    break
                }
            }
        }

        // ===== UPDATE & PICKUP ITEMS =====
        updateItems()
        checkItemPickup(playerX, playerY)
        checkSkillPickupRange(playerX, playerY)
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        // Vẽ map theo loại
        when (mapType) {
            1 -> grasslandMap?.draw(canvas, cameraX, cameraY)
            2 -> desertMap?.draw(canvas, cameraX, cameraY)
            3 -> volcanoMap?.draw(canvas, cameraX, cameraY)
        }

        canvas.save()
        canvas.translate(-cameraX, -cameraY)

        for (skeleton in skeletons) {
            skeleton.draw(canvas)
        }

        for (demon in demons) {
            demon.draw(canvas)
        }

        for (medusa in medusas) {
            medusa.draw(canvas)
        }

        for (jinn in jinns) {
            jinn.draw(canvas)
        }

        for (smallDragon in smallDragons) {
            smallDragon.draw(canvas)
        }

        for (dragon in dragons) {
            dragon.draw(canvas)
        }

        // ===== VẼ ITEMS =====
        for (heart in healthHearts) {
            heart.draw(canvas)
        }
        
        // ===== VẼ SKILL ITEMS =====
        for (skillItem in blackHoleSkillItems) {
            skillItem.draw(canvas)
        }
        
        for (skillItem in laserBeamSkillItems) {
            skillItem.draw(canvas)
        }
        
        for (skillItem in shieldSkillItems) {
            skillItem.draw(canvas)
        }
        
        // ===== VẼ BLACK HOLE EFFECTS =====
        for (effect in blackHoleEffects) {
            effect.draw(canvas)
        }
        
        // ===== VẼ LASER BEAM EFFECTS =====
        for (effect in laserBeamEffects) {
            effect.draw(canvas)
        }
        
        // ===== VẼ SHIELD EFFECT =====
        shieldEffect?.draw(canvas)

        fighter?.draw(canvas)
        samuraiArcher?.draw(canvas)
        samuraiCommander?.draw(canvas)

        canvas.restore()

        fighter?.drawUI(canvas)
        samuraiArcher?.drawUI(canvas)
        samuraiCommander?.drawUI(canvas)

        // ===== VẼ TIMER (nếu có time limit) =====
        gameModeConfig?.let { config ->
            if (config.timeLimit != null) {
                val paint = Paint().apply {
                    color = when {
                        remainingTime <= 10 -> Color.RED     // 10 giây cuối: Đỏ
                        remainingTime <= 30 -> Color.YELLOW  // 30 giây cuối: Vàng
                        else -> Color.WHITE                  // Còn nhiều thời gian: Trắng
                    }
                    textSize = 60f
                    typeface = Typeface.DEFAULT_BOLD
                    textAlign = Paint.Align.CENTER
                    setShadowLayer(5f, 0f, 0f, Color.BLACK)
                }

                val timeText = GameModeConfig.formatTime(remainingTime)
                val timerY = 100f

                // Vẽ background cho timer
                val bgPaint = Paint().apply {
                    color = Color.argb(180, 0, 0, 0)
                    style = Paint.Style.FILL
                }
                val textBounds = android.graphics.Rect()
                paint.getTextBounds(timeText, 0, timeText.length, textBounds)
                canvas.drawRoundRect(
                    width / 2f - textBounds.width() / 2f - 30f,
                    timerY - 60f,
                    width / 2f + textBounds.width() / 2f + 30f,
                    timerY + 15f,
                    15f, 15f,
                    bgPaint
                )

                canvas.drawText(timeText, width / 2f, timerY, paint)

                // Vẽ label "TIME"
                val labelPaint = Paint().apply {
                    color = Color.LTGRAY
                    textSize = 30f
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("TIME", width / 2f, timerY - 35f, labelPaint)
            }
        }

        // ===== VẼ FLAWLESS SCORE =====
        val flawlessPaint = Paint().apply {
            color = when {
                flawlessScore >= 1500 -> Color.parseColor("#2ECC71") // Xanh lá
                flawlessScore >= 1000 -> Color.parseColor("#F39C12") // Vàng cam
                flawlessScore >= 500 -> Color.parseColor("#E67E22")  // Cam
                flawlessScore > 0 -> Color.parseColor("#E74C3C")     // Đỏ
                else -> Color.GRAY                                    // Hết điểm
            }
            textSize = 40f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.RIGHT
            setShadowLayer(3f, 0f, 0f, Color.BLACK)
        }

        // Vẽ background cho Flawless Score
        val flawlessBgPaint = Paint().apply {
            color = Color.argb(180, 0, 0, 0)
            style = Paint.Style.FILL
        }

        val flawlessText = "❤ $flawlessScore"
        val flawlessTextBounds = android.graphics.Rect()
        flawlessPaint.getTextBounds(flawlessText, 0, flawlessText.length, flawlessTextBounds)

        val flawlessX = width - 30f
        val flawlessY = 80f

        canvas.drawRoundRect(
            flawlessX - flawlessTextBounds.width() - 40f,
            flawlessY - 50f,
            flawlessX + 10f,
            flawlessY + 10f,
            10f, 10f,
            flawlessBgPaint
        )

        canvas.drawText(flawlessText, flawlessX, flawlessY, flawlessPaint)

        // Vẽ label "FLAWLESS"
        val flawlessLabelPaint = Paint().apply {
            color = Color.LTGRAY
            textSize = 22f
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("FLAWLESS", flawlessX, flawlessY - 30f, flawlessLabelPaint)

        // ===== VẼ ENEMY COUNTER (SỐ QUÁI ĐÃ TIÊU DIỆT) =====
        val enemyCounterPaint = Paint().apply {
            color = Color.parseColor("#3498DB") // Xanh dương
            textSize = 40f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.RIGHT
            setShadowLayer(3f, 0f, 0f, Color.BLACK)
        }

        val enemyCounterBgPaint = Paint().apply {
            color = Color.argb(180, 0, 0, 0)
            style = Paint.Style.FILL
        }

        val enemyCounterText = "☠ $enemiesKilled/$totalEnemies"
        val enemyCounterBounds = android.graphics.Rect()
        enemyCounterPaint.getTextBounds(enemyCounterText, 0, enemyCounterText.length, enemyCounterBounds)

        val enemyCounterX = width - 30f
        val enemyCounterY = 160f

        canvas.drawRoundRect(
            enemyCounterX - enemyCounterBounds.width() - 40f,
            enemyCounterY - 50f,
            enemyCounterX + 10f,
            enemyCounterY + 10f,
            10f, 10f,
            enemyCounterBgPaint
        )

        canvas.drawText(enemyCounterText, enemyCounterX, enemyCounterY, enemyCounterPaint)

        // Vẽ label "ENEMIES"
        val enemyLabelPaint = Paint().apply {
            color = Color.LTGRAY
            textSize = 22f
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("ENEMIES", enemyCounterX, enemyCounterY - 30f, enemyLabelPaint)

        joystick.draw(canvas)
        settingsButton.draw(canvas)
        attackButton.draw(canvas)
        jumpButton.draw(canvas)

        if (characterType == "Fighter") {
            shieldButton.draw(canvas)
        } else if (characterType == "Samurai_Archer") {
            bowButton.draw(canvas)
        }
        
        // ===== VẼ SKILL BUTTONS =====
        pickupButton.draw(canvas)
        blackHoleSkillButton?.draw(canvas)
        laserBeamSkillButton?.draw(canvas)
        shieldSkillButton?.draw(canvas)
    }

    private fun showGameOver() {
        soundManager.pauseBackgroundMusic() // Tạm dừng nhạc nền khi game over

        mainHandler.postDelayed({
            gameOverDialog = GameOverDialog(
                context,
                onContinue = {
                    soundManager.playBackgroundMusic() // Tiếp tục nhạc nền khi chơi lại
                    resetGame()
                },
                onRestart = {
                }
            )
            gameOverDialog?.show()
        }, 1000)
    }

    private fun showPauseMenu() {
        isPaused = true
        soundManager.pauseAllSounds() // Tạm dừng âm thanh khi pause
        soundManager.pauseBackgroundMusic() // Tạm dừng nhạc nền khi pause

        mainHandler.post {
            pauseMenuDialog = PauseMenuDialog(
                context,
                onContinue = {
                    isPaused = false
                    soundManager.resumeAllSounds() // Tiếp tục âm thanh khi continue
                    soundManager.resumeBackgroundMusic() // Tiếp tục nhạc nền khi continue
                },
                onRestart = {
                    isPaused = false
                    soundManager.stopAllSounds() // Dừng hết âm thanh khi restart
                    soundManager.playBackgroundMusic() // Phát lại nhạc nền từ đầu
                    resetGame()
                },
                onCharacterSelect = {
                    (context as? MainActivity)?.let { activity ->
                        val intent = Intent(context, CharacterSelectionActivity::class.java)
                        activity.startActivity(intent)
                        activity.finish()
                    }
                },
                onMainMenu = {
                    (context as? MainActivity)?.let { activity ->
                        val intent = Intent(context, MainMenuActivity::class.java)
                        activity.startActivity(intent)
                        activity.finish()
                    }
                }
            )
            pauseMenuDialog?.show()
        }
    }

    // ===== VICTORY SYSTEM VỚI LEVEL PROGRESSION =====
    private fun showVictory() {
        val completionTime = System.currentTimeMillis() - gameStartTime
        val completionTimeSeconds = (completionTime / 1000).toInt()

        // Lưu HP hiện tại của hero để chuyển sang màn tiếp theo
        fighter?.let { saveFighterHP(it.getHealth()) }
        samuraiArcher?.let { saveSamuraiArcherHP(it.getHealth()) }
        samuraiCommander?.let { saveSamuraiCommanderHP(it.getHealth()) }

        // Tính điểm bonus dựa trên performance
        val bonusResult = GameModeConfig.calculateBonus(
            mapType,
            completionTimeSeconds,
            flawlessScore,
            maxComboAchieved
        )

        // Điểm cơ bản: mỗi quái = 100 điểm
        val baseScore = totalEnemies * 100
        val totalScore = baseScore + bonusResult.totalBonusScore

        val victoryRecord = VictoryRecord(
            completionTimeMs = completionTime,
            characterType = characterType,
            enemiesKilled = totalEnemies, // Sử dụng totalEnemies từ LevelConfig
            timestamp = System.currentTimeMillis(),

            // Score data
            baseScore = baseScore,
            bonusScore = bonusResult.totalBonusScore,
            totalScore = totalScore,
            flawlessScore = flawlessScore,

            // Bonus achievements
            achievedTimeBonus = bonusResult.achievedTimeBonus,
            achievedNoHitBonus = bonusResult.achievedNoHitBonus,
            achievedComboBonus = bonusResult.achievedComboBonus
        )

        victoryManager.saveVictory(victoryRecord)

        mainHandler.postDelayed({
            // Kiểm tra nếu đây là level cuối cùng (Volcano)
            if (levelManager.isFinalLevel(currentLevel)) {
                // Hiển thị dialog hoàn thành toàn bộ game
                showGameCompleteDialog()
            } else {
                // Hiển thị victory dialog bình thường với nút Next Level
                showLevelVictoryDialog(victoryRecord)
            }
        }, 500)
    }

    private fun showGameCompleteDialog() {
        mainHandler.post {
            gameCompleteDialog = GameCompleteDialog(
                context,
                onPlayAgain = {
                    // Reset về level đầu (Grassland)
                    isVictory = false
                },
                onMainMenu = {
                    // Về main menu
                }
            )
            gameCompleteDialog?.show()
        }
    }

    private fun showLevelVictoryDialog(victoryRecord: VictoryRecord) {
        soundManager.pauseBackgroundMusic()
        mainHandler.post {
            (context as? MainActivity)?.showLevelVictoryDialog(victoryRecord, currentLevel)
        }
    }

    // ===== RESET GAME VỚI WAVE SYSTEM =====
    private fun resetGame() {
        isGameOver = false
        isVictory = false

        fighter?.reset()
        samuraiArcher?.reset()
        samuraiCommander?.reset()

        // Clear all enemies
        skeletons.clear()
        demons.clear()
        medusas.clear()
        jinns.clear()
        smallDragons.clear()
        dragons.clear()

        // Clear all items
        healthHearts.clear()
        
        // Clear all skill items and effects
        blackHoleSkillItems.clear()
        blackHoleEffects.clear()
        laserBeamSkillItems.clear()
        laserBeamEffects.clear()
        shieldSkillItems.clear()
        shieldEffect = null
        pickupButton.hide()

        // Reset wave system
        currentWaveNumber = 1
        wavesCompleted = 0
        lastWaveClearedTime = 0
        isWaitingForNextWave = false
        currentWaveEnemyCount = 0

        // Reset game mode tracking
        gameTimer = 0
        frameCounter = 0
        flawlessScore = 2000
        maxComboAchieved = 0
        enemiesKilled = 0
        gameModeConfig?.let {
            remainingTime = it.timeLimit ?: 0
        }

        gameStartTime = System.currentTimeMillis()
    }

    /**
     * Helper function khi player bị đánh
     */
    private fun onPlayerHit() {
        // Trừ 100 điểm, tối thiểu là 0
        flawlessScore = maxOf(0, flawlessScore - 100)
        soundManager.playHitSound()
    }

    /**
     * Helper function: Tấn công quái và kiểm tra nếu chết thì tăng counter ngay
     */
    private fun damageSkeletonAndCheck(skeleton: Skeleton, damage: Int) {
        val wasAlive = !skeleton.isDead()
        skeleton.takeDamage(damage)
        if (wasAlive && skeleton.isDead()) {
            enemiesKilled++
            // Spawn item nếu may mắn
            trySpawnItem(skeleton.getX(), skeleton.y)
        }
    }

    private fun damageDemonAndCheck(demon: Demon, damage: Int) {
        val wasAlive = !demon.isDead()
        demon.takeDamage(damage)
        if (wasAlive && demon.isDead()) {
            enemiesKilled++
            // Spawn item nếu may mắn
            trySpawnItem(demon.getX(), demon.y)
        }
    }

    private fun damageMedusaAndCheck(medusa: Medusa, damage: Int) {
        val wasAlive = !medusa.isDead()
        medusa.takeDamage(damage)
        if (wasAlive && medusa.isDead()) {
            enemiesKilled++
            // Spawn item nếu may mắn
            trySpawnItem(medusa.getX(), medusa.y)
        }
    }

    private fun damageJinnAndCheck(jinn: Jinn, damage: Int) {
        val wasAlive = !jinn.isDead()
        jinn.takeDamage(damage)
        if (wasAlive && jinn.isDead()) {
            enemiesKilled++
            // Spawn item nếu may mắn
            trySpawnItem(jinn.getX(), jinn.y)
        }
    }

    private fun damageSmallDragonAndCheck(dragon: SmallDragon, damage: Int) {
        val wasAlive = !dragon.isDead()
        dragon.takeDamage(damage)
        if (wasAlive && dragon.isDead()) {
            enemiesKilled++
            // Spawn item nếu may mắn
            trySpawnItem(dragon.getX(), dragon.y)
        }
    }
    private fun damageDragonAndCheck(dragon: Dragon, damage: Int) {
        val wasAlive = !dragon.isDead()
        dragon.takeDamage(damage)
        if (wasAlive && dragon.isDead()) {
            enemiesKilled++
            // Spawn item nếu may mắn
            trySpawnItem(dragon.getX(), dragon.y)
        }
    }

    // ===== ITEM SYSTEM FUNCTIONS =====
    /**
     * Thử spawn item khi quái chết
     */
    private fun trySpawnItem(x: Float, y: Float) {
        val groundY = when (mapType) {
            2 -> desertMap?.groundY ?: (height * 0.75f)
            3 -> volcanoMap?.groundY ?: (height * 0.75f)
            else -> grasslandMap?.groundY ?: (height * 0.75f)
        }
        
        // Kiểm tra 50% rơi trái tim
        if (ItemDropConfig.shouldDropItem(0.5f)) {
            healthHearts.add(HealthHeart(gameContext, x, groundY - 150f))
        }
        
        // Kiểm tra 50% rơi skill Black Hole
        if (ItemDropConfig.shouldDropBlackHoleSkill()) {
            blackHoleSkillItems.add(BlackHoleSkillItem(gameContext, x, groundY - 150f))
        }
        
        // Kiểm tra 50% rơi skill Laser Beam
        if (ItemDropConfig.shouldDropLaserBeamSkill()) {
            laserBeamSkillItems.add(LaserBeamSkillItem(gameContext, x, groundY - 150f))
        }
        
        // Kiểm tra 50% rơi skill Shield
        if (ItemDropConfig.shouldDropShieldSkill()) {
            shieldSkillItems.add(ShieldSkillItem(gameContext, x, groundY - 150f))
        }
    }

    /**
     * Update tất cả items
     */
    private fun updateItems() {
        // Update health hearts
        healthHearts.forEach { it.update() }
        
        // Xóa items đã hết thời gian tồn tại
        healthHearts.removeAll { it.shouldBeRemoved() }
        
        // Update black hole skill items
        blackHoleSkillItems.forEach { it.update() }
        blackHoleSkillItems.removeAll { it.shouldBeRemoved() }
        
        // Update laser beam skill items
        laserBeamSkillItems.forEach { it.update() }
        laserBeamSkillItems.removeAll { it.shouldBeRemoved() }
        
        // Update shield skill items
        shieldSkillItems.forEach { it.update() }
        shieldSkillItems.removeAll { it.shouldBeRemoved() }
        
        // Update black hole effects
        blackHoleEffects.forEach { effect ->
            effect.update()
            
            if (effect.isActive()) {
                applyBlackHoleEffects(effect)
            }
        }
        blackHoleEffects.removeAll { !it.isActive() }
        
        // Update laser beam effects
        laserBeamEffects.forEach { effect ->
            effect.update()
            
            if (effect.isActive() && !effect.hasDamageBeenDealt()) {
                applyLaserBeamDamage(effect)
            }
        }
        laserBeamEffects.removeAll { !it.isActive() }
        
        // Update shield effect
        shieldEffect?.let { shield ->
            val playerX = fighter?.getX() ?: samuraiArcher?.getX() ?: samuraiCommander?.getX() ?: 0f
            val playerY = fighter?.y ?: samuraiArcher?.y ?: samuraiCommander?.y ?: 0f
            shield.update(playerX, playerY)
            
            if (!shield.isActive()) {
                shieldEffect = null
            }
        }
        
        // Update buttons
        pickupButton.update()
        blackHoleSkillButton?.update()
        laserBeamSkillButton?.update()
        shieldSkillButton?.update()
    }

    /**
     * Kiểm tra và nhặt item
     */
    private fun checkItemPickup(playerX: Float, playerY: Float) {
        val pickupRange = 100f
        
        // Kiểm tra nhặt health heart
        healthHearts.forEach { heart ->
            if (!heart.isCollected() && heart.isCollidingWith(playerX, playerY, pickupRange)) {
                // Hồi máu cho hero (20 HP)
                fighter?.heal(20)
                samuraiArcher?.heal(20)
                samuraiCommander?.heal(20)
                
                // Đánh dấu đã nhặt
                heart.collect()
            }
        }
    }
    
    /**
     * Kiểm tra xem player có gần skill item không để hiện nút nhặt
     */
    private fun checkSkillPickupRange(playerX: Float, playerY: Float) {
        val pickupRange = 150f
        var nearestSkill: BlackHoleSkillItem? = null
        var nearestLaserSkill: LaserBeamSkillItem? = null
        
        // Tìm black hole skill item gần nhất
        for (skill in blackHoleSkillItems) {
            if (!skill.isPickedUp() && skill.isCollidingWith(playerX, playerY, pickupRange)) {
                nearestSkill = skill
                break
            }
        }
        
        // Tìm laser beam skill item gần nhất
        for (skill in laserBeamSkillItems) {
            if (!skill.isPickedUp() && skill.isCollidingWith(playerX, playerY, pickupRange)) {
                nearestLaserSkill = skill
                break
            }
        }
        
        // Tìm shield skill item gần nhất
        var nearestShieldSkill: ShieldSkillItem? = null
        for (skill in shieldSkillItems) {
            if (!skill.isPickedUp() && skill.isCollidingWith(playerX, playerY, pickupRange)) {
                nearestShieldSkill = skill
                break
            }
        }
        
        // Ưu tiên hiển thị nút cho skill gần nhất
        if (nearestSkill != null) {
            // Hiện nút nhặt tại vị trí skill (theo camera)
            val buttonX = nearestSkill.getX() - cameraX
            val buttonY = nearestSkill.getY() - cameraY - 100f
            pickupButton.show(buttonX, buttonY)
        } else if (nearestLaserSkill != null) {
            // Hiện nút nhặt tại vị trí laser skill
            val buttonX = nearestLaserSkill.getX() - cameraX
            val buttonY = nearestLaserSkill.getY() - cameraY - 100f
            pickupButton.show(buttonX, buttonY)
        } else if (nearestShieldSkill != null) {
            // Hiện nút nhặt tại vị trí shield skill
            val buttonX = nearestShieldSkill.getX() - cameraX
            val buttonY = nearestShieldSkill.getY() - cameraY - 100f
            pickupButton.show(buttonX, buttonY)
        } else {
            pickupButton.hide()
        }
    }
    
    /**
     * Thử nhặt skill khi bấm nút
     */
    private fun tryPickupSkill() {
        val playerX = fighter?.getX() ?: samuraiArcher?.getX() ?: samuraiCommander?.getX() ?: 0f
        val playerY = fighter?.y ?: samuraiArcher?.y ?: samuraiCommander?.y ?: 0f
        val pickupRange = 150f
        
        // Thử nhặt Black Hole skill
        for (skill in blackHoleSkillItems) {
            if (!skill.isPickedUp() && skill.isCollidingWith(playerX, playerY, pickupRange)) {
                skill.pickup()
                unlockBlackHoleSkill()
                pickupButton.hide()
                return
            }
        }
        
        // Thử nhặt Laser Beam skill
        for (skill in laserBeamSkillItems) {
            if (!skill.isPickedUp() && skill.isCollidingWith(playerX, playerY, pickupRange)) {
                skill.pickup()
                unlockLaserBeamSkill()
                pickupButton.hide()
                return
            }
        }
        
        // Thử nhặt Shield skill
        for (skill in shieldSkillItems) {
            if (!skill.isPickedUp() && skill.isCollidingWith(playerX, playerY, pickupRange)) {
                skill.pickup()
                unlockShieldSkill()
                pickupButton.hide()
                return
            }
        }
    }
    
    /**
     * Unlock Black Hole Skill
     */
    private fun unlockBlackHoleSkill() {
        // Luôn tạo button mới mỗi khi nhặt (cho phép dùng 1 lần mỗi lần nhặt)
        hasBlackHoleSkill = true
        
        val screenWidth = width.toFloat()
        val screenHeight = height.toFloat()
        // Đặt phía trên joystick, phía trái màn hình
        blackHoleSkillButton = BlackHoleSkillButton(
            250f,  // Phía trên joystick
            screenHeight - 350f
        )
    }
    
    /**
     * Cast Black Hole Skill
     */
    private fun castBlackHole() {
        val playerX = fighter?.getX() ?: samuraiArcher?.getX() ?: samuraiCommander?.getX() ?: 0f
        val playerY = fighter?.y ?: samuraiArcher?.y ?: samuraiCommander?.y ?: 0f
        val playerFacingRight = fighter?.getFacingRight() ?: samuraiArcher?.getFacingRight() ?: samuraiCommander?.getFacingRight() ?: true
        
        // Spawn hố đen cách player 300 pixels theo hướng đang nhìn
        val spawnDistance = 300f
        val direction = if (playerFacingRight) 1 else -1
        val spawnX = playerX + spawnDistance * direction
        val spawnY = playerY
        
        blackHoleEffects.add(BlackHoleEffect(spawnX, spawnY))
        // Đánh dấu skill đã dùng -> Ẩn nút (chỉ dùng 1 lần)
        blackHoleSkillButton?.markAsUsed()
    }
    
    /**
     * Áp dụng hiệu ứng hố đen lên enemies
     * Chỉ gây damage, không hút (vì x, y là private)
     */
    private fun applyBlackHoleEffects(effect: BlackHoleEffect) {
        // Áp dụng lên skeletons
        skeletons.forEach { skeleton ->
            if (!skeleton.isDead() && effect.isInRange(skeleton.getX(), skeleton.y)) {
                // Gây damage mỗi giây
                if (effect.shouldDealDamage()) {
                    damageSkeletonAndCheck(skeleton, effect.getDamage())
                }
            }
        }
        
        // Áp dụng lên demons
        demons.forEach { demon ->
            if (!demon.isDead() && effect.isInRange(demon.getX(), demon.y)) {
                if (effect.shouldDealDamage()) {
                    damageDemonAndCheck(demon, effect.getDamage())
                }
            }
        }
        
        // Áp dụng lên medusas
        medusas.forEach { medusa ->
            if (!medusa.isDead() && effect.isInRange(medusa.getX(), medusa.y)) {
                if (effect.shouldDealDamage()) {
                    damageMedusaAndCheck(medusa, effect.getDamage())
                }
            }
        }
        
        // Áp dụng lên jinns
        jinns.forEach { jinn ->
            if (!jinn.isDead() && effect.isInRange(jinn.getX(), jinn.y)) {
                if (effect.shouldDealDamage()) {
                    damageJinnAndCheck(jinn, effect.getDamage())
                }
            }
        }
        
        // Áp dụng lên small dragons
        smallDragons.forEach { dragon ->
            if (!dragon.isDead() && effect.isInRange(dragon.getX(), dragon.y)) {
                if (effect.shouldDealDamage()) {
                    damageSmallDragonAndCheck(dragon, effect.getDamage())
                }
            }
        }
        
        // Áp dụng lên dragons
        dragons.forEach { dragon ->
            if (!dragon.isDead() && effect.isInRange(dragon.getX(), dragon.y)) {
                if (effect.shouldDealDamage()) {
                    damageDragonAndCheck(dragon, effect.getDamage())
                }
            }
        }
    }
    
    /**
     * Unlock Laser Beam Skill
     */
    private fun unlockLaserBeamSkill() {
        // Luôn tạo button mới mỗi khi nhặt (cho phép dùng 1 lần mỗi lần nhặt)
        hasLaserBeamSkill = true
        
        val screenWidth = width.toFloat()
        val screenHeight = height.toFloat()
        // Đặt bên phải Black Hole button
        laserBeamSkillButton = LaserBeamSkillButton(
            gameContext,
            380f,  // Bên phải Black Hole
            screenHeight - 350f
        )
    }
    
    /**
     * Cast Laser Beam Skill - Bắn laser vào quái gần nhất
     */
    private fun castLaserBeam() {
        val playerX = fighter?.getX() ?: samuraiArcher?.getX() ?: samuraiCommander?.getX() ?: 0f
        val playerY = fighter?.y ?: samuraiArcher?.y ?: samuraiCommander?.y ?: 0f
        
        // Tìm quái gần nhất
        val nearestEnemy = findNearestEnemy(playerX, playerY)
        
        if (nearestEnemy != null) {
            // Tạo laser beam effect
            laserBeamEffects.add(
                LaserBeamEffect(
                    gameContext,
                    playerX,
                    playerY,
                    nearestEnemy.x,
                    nearestEnemy.y
                )
            )
            // Đánh dấu skill đã dùng -> Ẩn nút (chỉ dùng 1 lần)
            laserBeamSkillButton?.markAsUsed()
        }
    }
    
    /**
     * Tìm quái gần nhất
     */
    // Data class để lưu thông tin enemy
    data class EnemyInfo(val enemy: Any, val x: Float, val y: Float, val distance: Float)
    
    private fun findNearestEnemy(playerX: Float, playerY: Float): EnemyInfo? {
        var nearestEnemyInfo: EnemyInfo? = null
        var minDistance = Float.MAX_VALUE
        
        val allEnemies = mutableListOf<EnemyInfo>()
        
        // Thêm skeletons
        skeletons.forEach { skeleton ->
            if (!skeleton.isDead()) {
                val dx = skeleton.getX() - playerX
                val dy = skeleton.y - playerY
                val distance = kotlin.math.sqrt(dx * dx + dy * dy)
                allEnemies.add(EnemyInfo(skeleton, skeleton.getX(), skeleton.y, distance))
            }
        }
        
        // Thêm demons
        demons.forEach { demon ->
            if (!demon.isDead()) {
                val dx = demon.getX() - playerX
                val dy = demon.y - playerY
                val distance = kotlin.math.sqrt(dx * dx + dy * dy)
                allEnemies.add(EnemyInfo(demon, demon.getX(), demon.y, distance))
            }
        }
        
        // Thêm medusas
        medusas.forEach { medusa ->
            if (!medusa.isDead()) {
                val dx = medusa.getX() - playerX
                val dy = medusa.y - playerY
                val distance = kotlin.math.sqrt(dx * dx + dy * dy)
                allEnemies.add(EnemyInfo(medusa, medusa.getX(), medusa.y, distance))
            }
        }
        
        // Thêm jinns
        jinns.forEach { jinn ->
            if (!jinn.isDead()) {
                val dx = jinn.getX() - playerX
                val dy = jinn.y - playerY
                val distance = kotlin.math.sqrt(dx * dx + dy * dy)
                allEnemies.add(EnemyInfo(jinn, jinn.getX(), jinn.y, distance))
            }
        }
        
        // Thêm small dragons
        smallDragons.forEach { dragon ->
            if (!dragon.isDead()) {
                val dx = dragon.getX() - playerX
                val dy = dragon.y - playerY
                val distance = kotlin.math.sqrt(dx * dx + dy * dy)
                allEnemies.add(EnemyInfo(dragon, dragon.getX(), dragon.y, distance))
            }
        }
        
        // Thêm dragons
        dragons.forEach { dragon ->
            if (!dragon.isDead()) {
                val dx = dragon.getX() - playerX
                val dy = dragon.y - playerY
                val distance = kotlin.math.sqrt(dx * dx + dy * dy)
                allEnemies.add(EnemyInfo(dragon, dragon.getX(), dragon.y, distance))
            }
        }
        
        // Tìm enemy gần nhất
        allEnemies.forEach { info ->
            if (info.distance < minDistance) {
                minDistance = info.distance
                nearestEnemyInfo = info
            }
        }
        
        return nearestEnemyInfo
    }
    
    /**
     * Gây damage từ Laser Beam lên quái
     */
    private fun applyLaserBeamDamage(effect: LaserBeamEffect) {
        val damage = effect.getDamage()
        if (damage <= 0) return
        
        val targetX = effect.getTargetX()
        val targetY = effect.getTargetY()
        val tolerance = 150f  // Tăng tolerance để đảm bảo trúng kể cả khi quái di chuyển do Black Hole
        
        var damageApplied = false
        
        // Áp dụng lên skeletons
        skeletons.forEach { skeleton ->
            if (!skeleton.isDead()) {
                val dx = kotlin.math.abs(skeleton.getX() - targetX)
                val dy = kotlin.math.abs(skeleton.y - targetY)
                if (dx < tolerance && dy < tolerance) {
                    damageSkeletonAndCheck(skeleton, damage)
                    damageApplied = true
                    return@forEach  // Chỉ damage 1 enemy
                }
            }
        }
        
        if (damageApplied) {
            effect.markDamageAsDealt()
            return
        }
        
        // Áp dụng lên demons
        demons.forEach { demon ->
            if (!demon.isDead()) {
                val dx = kotlin.math.abs(demon.getX() - targetX)
                val dy = kotlin.math.abs(demon.y - targetY)
                if (dx < tolerance && dy < tolerance) {
                    damageDemonAndCheck(demon, damage)
                    damageApplied = true
                    return@forEach
                }
            }
        }
        
        if (damageApplied) {
            effect.markDamageAsDealt()
            return
        }
        
        // Áp dụng lên medusas
        medusas.forEach { medusa ->
            if (!medusa.isDead()) {
                val dx = kotlin.math.abs(medusa.getX() - targetX)
                val dy = kotlin.math.abs(medusa.y - targetY)
                if (dx < tolerance && dy < tolerance) {
                    damageMedusaAndCheck(medusa, damage)
                    damageApplied = true
                    return@forEach
                }
            }
        }
        
        if (damageApplied) {
            effect.markDamageAsDealt()
            return
        }
        
        // Áp dụng lên jinns
        jinns.forEach { jinn ->
            if (!jinn.isDead()) {
                val dx = kotlin.math.abs(jinn.getX() - targetX)
                val dy = kotlin.math.abs(jinn.y - targetY)
                if (dx < tolerance && dy < tolerance) {
                    damageJinnAndCheck(jinn, damage)
                    damageApplied = true
                    return@forEach
                }
            }
        }
        
        if (damageApplied) {
            effect.markDamageAsDealt()
            return
        }
        
        // Áp dụng lên small dragons
        smallDragons.forEach { dragon ->
            if (!dragon.isDead()) {
                val dx = kotlin.math.abs(dragon.getX() - targetX)
                val dy = kotlin.math.abs(dragon.y - targetY)
                if (dx < tolerance && dy < tolerance) {
                    damageSmallDragonAndCheck(dragon, damage)
                    damageApplied = true
                    return@forEach
                }
            }
        }
        
        if (damageApplied) {
            effect.markDamageAsDealt()
            return
        }
        
        // Áp dụng lên dragons
        dragons.forEach { dragon ->
            if (!dragon.isDead()) {
                val dx = kotlin.math.abs(dragon.getX() - targetX)
                val dy = kotlin.math.abs(dragon.y - targetY)
                if (dx < tolerance && dy < tolerance) {
                    damageDragonAndCheck(dragon, damage)
                    damageApplied = true
                    return@forEach
                }
            }
        }
        
        // Đánh dấu đã deal damage
        if (damageApplied) {
            effect.markDamageAsDealt()
        }
    }
    
    /**
     * Unlock Shield Skill
     */
    private fun unlockShieldSkill() {
        // Luôn tạo button mới mỗi khi nhặt (cho phép dùng 1 lần mỗi lần nhặt)
        hasShieldSkill = true
        
        val screenWidth = width.toFloat()
        val screenHeight = height.toFloat()
        // Đặt bên phải Laser Beam button
        shieldSkillButton = ShieldSkillButton(
            gameContext,
            510f,  // Bên phải Laser Beam
            screenHeight - 350f
        )
    }
    
    /**
     * Cast Shield Skill - Tạo khiên bảo vệ quanh nhân vật
     */
    private fun castShield() {
        val playerX = fighter?.getX() ?: samuraiArcher?.getX() ?: samuraiCommander?.getX() ?: 0f
        val playerY = fighter?.y ?: samuraiArcher?.y ?: samuraiCommander?.y ?: 0f
        
        // Tạo shield effect
        shieldEffect = ShieldEffect(playerX, playerY)
        
        // Đánh dấu skill đã dùng -> Ẩn nút (chỉ dùng 1 lần)
        shieldSkillButton?.markAsUsed()
    }
    
    /**
     * Áp dụng damage lên player, có tính shield chặn
     */
    private fun applyDamageToPlayer(damage: Int) {
        var actualDamage = damage
        
        // Kiểm tra xem có shield active không
        shieldEffect?.let { shield ->
            if (shield.isActive()) {
                // Shield chặn damage
                actualDamage = shield.absorbDamage(damage)
            }
        }
        
        // Áp dụng damage còn lại (nếu có) lên player
        if (actualDamage > 0) {
            fighter?.takeDamage(actualDamage)
            samuraiArcher?.takeDamage(actualDamage)
            samuraiCommander?.takeDamage(actualDamage)
        }
    }

    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    
    companion object {
        // Lưu HP của hero qua các màn
        private var persistedFighterHP: Int? = null
        private var persistedSamuraiArcherHP: Int? = null
        private var persistedSamuraiCommanderHP: Int? = null
        
        fun saveFighterHP(hp: Int) {
            persistedFighterHP = hp
        }
        
        fun getSavedFighterHP(): Int? = persistedFighterHP
        
        fun saveSamuraiArcherHP(hp: Int) {
            persistedSamuraiArcherHP = hp
        }
        
        fun getSavedSamuraiArcherHP(): Int? = persistedSamuraiArcherHP
        
        fun saveSamuraiCommanderHP(hp: Int) {
            persistedSamuraiCommanderHP = hp
        }
        
        fun getSavedSamuraiCommanderHP(): Int? = persistedSamuraiCommanderHP
        
        // Reset tất cả HP khi bắt đầu từ màn 1
        fun resetAllHP() {
            persistedFighterHP = null
            persistedSamuraiArcherHP = null
            persistedSamuraiCommanderHP = null
        }
    }

    fun pauseGame() {
        // Dừng thread game loop
        gameThread?.running = false
        try {
            gameThread?.join()
        } catch (e: Exception) {
            // ignore
        }
    }

    fun releaseResources() {
        // Dừng và giải phóng âm thanh, bitmap, animation (nếu có)
        soundManager.release()
        // Nếu có resource, bitmap, enemy asset lớn thì giải phóng ở đây
    }
}

class GameThread(private val surfaceHolder: SurfaceHolder, private val gameView: GameView) : Thread() {
    var running = false
    private val targetFPS = 60
    private val targetTime = 1000 / targetFPS

    override fun run() {
        while (running) {
            val startTime = System.currentTimeMillis()
            var canvas: Canvas? = null

            try {
                canvas = surfaceHolder.lockCanvas()
                synchronized(surfaceHolder) {
                    gameView.update()
                    gameView.draw(canvas)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (canvas != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            val timeMillis = System.currentTimeMillis() - startTime
            val waitTime = targetTime - timeMillis

            if (waitTime > 0) {
                try {
                    sleep(waitTime)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }
}
