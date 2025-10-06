package com.example.laptrinhgame2d

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
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
    private var lastWaveClearedTime: Long = 0 // Thời điểm wave trước bị clear
    private var isWaitingForNextWave: Boolean = false // Đang chờ spawn wave tiếp theo
    private var wavesCompleted: Int = 0
    private var spawnConfig: SpawnConfig.LevelSpawnConfig? = null
    private var currentWaveEnemyCount: Int = 0 // Số quái của wave hiện tại

    // Lấy tổng số quái từ LevelConfig thay vì hardcode
    private val totalEnemies: Int
        get() = levelConfig.totalEnemies

    init {
        holder.addCallback(this)

        // Khởi tạo Level Management
        currentLevel = levelManager.getLevelFromMapType(mapType)
        levelConfig = levelManager.getLevelConfig(currentLevel)
        
        // Khởi tạo Spawn Config
        spawnConfig = SpawnConfig.getSpawnConfig(currentLevel)
        currentWaveNumber = 1
        lastWaveClearedTime = 0
        isWaitingForNextWave = false
        currentWaveEnemyCount = 0

        victoryManager = VictoryManager(context)

        joystick = Joystick(200f, 0f, 120f, 100f)

        attackButton = GameButton(0f, 0f, 90f, "Attack", Color.rgb(255, 100, 100))
        shieldButton = GameButton(0f, 0f, 90f, "Shield", Color.rgb(100, 100, 255))
        jumpButton = GameButton(0f, 0f, 90f, "Jump", Color.rgb(100, 255, 100))
        bowButton = GameButton(0f, 0f, 90f, "Bow", Color.rgb(255, 165, 0))
        settingsButton = GameButton(0f, 0f, 70f, "⚙", Color.rgb(150, 150, 150))

        when (characterType) {
            "Fighter" -> fighter = Fighter(context, 500f, 400f)
            "Samurai_Archer" -> samuraiArcher = SamuraiArcher(context, 500f, 400f)
            "Samurai_Commander" -> samuraiCommander = SamuraiCommander(context, 500f, 400f)
        }

        // KHÔNG spawn enemies ở đây nữa, spawn theo wave trong update()
        // spawnEnemiesByLevel() <- XÓA DÒNG NÀY
    }

    // ===== WAVE SPAWN SYSTEM - SPAWN KHI WAVE TRƯỚC CHẾT HẾT =====
    private fun spawnWave(waveConfig: SpawnConfig.WaveConfig) {
        var spawnIndex = 0
        var enemiesSpawned = 0
        
        // Spawn từng loại quái theo config của wave
        val enemyTypes = waveConfig.enemies.entries.toList()
        
        for ((enemyType, count) in enemyTypes) {
            for (i in 0 until count) {
                val (x, y) = SpawnConfig.calculateSpawnPosition(
                    waveConfig,
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
            // Đếm cả quái sống và quái đã chết (chưa bị remove)
            val currentAliveEnemies = skeletons.count { !it.isDead() } +
                                     demons.count { !it.isDead() } +
                                     medusas.count { !it.isDead() } +
                                     jinns.count { !it.isDead() } +
                                     smallDragons.count { !it.isDead() } +
                                     dragons.count { !it.isDead() }
            
            val totalEnemiesInLists = skeletons.size + demons.size + medusas.size + 
                                      jinns.size + smallDragons.size + dragons.size
            
            // Debug log (có thể bỏ sau)
            // android.util.Log.d("WaveSystem", "Wave: $currentWaveNumber, Alive: $currentAliveEnemies, Total: $totalEnemiesInLists, WaveCount: $currentWaveEnemyCount")
            
            // Nếu wave hiện tại đã chết hết (không còn quái sống) và còn wave tiếp theo
            if (currentAliveEnemies == 0 && currentWaveEnemyCount > 0 && currentWaveNumber <= config.totalWaves) {
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

    // ===== LEGACY METHOD - KHÔNG DÙNG NỮA =====
    @Deprecated("Sử dụng Wave System thay vì spawn tất cả cùng lúc")
    private fun spawnEnemiesByLevel() {
        // Lấy spawn config cho level hiện tại
        val spawnConfig = SpawnConfig.getSpawnConfig(currentLevel)

        // Spawn Skeletons theo config
        for (i in 0 until levelConfig.skeletons) {
            val (x, y) = SpawnConfig.calculateSpawnPosition(
                spawnConfig.waves.firstOrNull() ?: return,
                i,
                levelConfig.skeletons
            )
            skeletons.add(Skeleton(gameContext, x, y))
        }

        // Spawn Demons theo config
        for (i in 0 until levelConfig.demons) {
            val (x, y) = SpawnConfig.calculateSpawnPosition(
                spawnConfig.waves.firstOrNull() ?: return,
                i,
                levelConfig.demons
            )
            demons.add(Demon(gameContext, x, y))
        }

        // Spawn Medusas theo config (chỉ từ Desert trở lên)
        for (i in 0 until levelConfig.medusas) {
            val (x, y) = SpawnConfig.calculateSpawnPosition(
                spawnConfig.waves.firstOrNull() ?: return,
                i,
                levelConfig.medusas
            )
            medusas.add(Medusa(gameContext, x, y))
        }

        // Spawn Jinns theo config (chỉ ở Volcano)
        for (i in 0 until levelConfig.jinns) {
            val (x, y) = SpawnConfig.calculateSpawnPosition(
                spawnConfig.waves.firstOrNull() ?: return,
                i,
                levelConfig.jinns
            )
            jinns.add(Jinn(gameContext, x, y))
        }

        // Spawn SmallDragons theo config (nếu cần mở rộng trong tương lai)
        for (i in 0 until levelConfig.smallDragons) {
            val (x, y) = SpawnConfig.calculateSpawnPosition(
                spawnConfig.waves.firstOrNull() ?: return,
                i,
                levelConfig.smallDragons
            )
            smallDragons.add(SmallDragon(gameContext, x, y))
        }

        // Spawn Dragons theo config (nếu cần mở rộng trong tương lai)
        for (i in 0 until levelConfig.dragons) {
            val (x, y) = SpawnConfig.calculateSpawnPosition(
                spawnConfig.waves.firstOrNull() ?: return,
                i,
                levelConfig.dragons
            )
            dragons.add(Dragon(gameContext, x, y))
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

        settingsButton.x = 300f
        settingsButton.y = 35f

        if (characterType == "Fighter") {
            attackButton.x = screenWidth - 230f
            attackButton.y = screenHeight - 200f

            shieldButton.x = screenWidth - 120f
            shieldButton.y = screenHeight - 200f

            jumpButton.x = screenWidth - 175f
            jumpButton.y = screenHeight - 320f
        } else {
            attackButton.x = screenWidth - 230f
            attackButton.y = screenHeight - 200f

            bowButton.x = screenWidth - 120f
            bowButton.y = screenHeight - 200f

            jumpButton.x = screenWidth - 175f
            jumpButton.y = screenHeight - 320f
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

                if (attackButton.isPressed(x, y)) {
                    attackButton.onTouch(pointerId)
                    fighter?.attack()
                    samuraiArcher?.attack()
                    samuraiCommander?.attack()
                }

                if (characterType == "Samurai_Archer" && bowButton.isPressed(x, y)) {
                    bowButton.onTouch(pointerId)
                    samuraiArcher?.let { archer ->
                        if (archer.getCombatMode() == SamuraiArcher.CombatMode.MELEE) {
                            archer.switchCombatMode()
                        }
                        archer.attack()
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
                    samuraiArcher?.releaseAttack()
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
            }
        }
        return true
    }

    fun update() {
        if (isGameOver || isPaused || isVictory) return

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

        // Sau đó mới remove enemies
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
                    fighter?.takeDamage(skeleton.getAttackDamage())
                    samuraiArcher?.takeDamage(skeleton.getAttackDamage())
                    samuraiCommander?.takeDamage(skeleton.getAttackDamage())
                    skeleton.markDamageDealt()
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
                    fighter?.takeDamage(demon.getAttackDamage())
                    samuraiArcher?.takeDamage(demon.getAttackDamage())
                    samuraiCommander?.takeDamage(demon.getAttackDamage())
                    demon.markDamageDealt()
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
                            fighter?.takeDamage(projectile.getDamage())
                            samuraiArcher?.takeDamage(projectile.getDamage())
                            samuraiCommander?.takeDamage(projectile.getDamage())
                            projectile.markDamageDealt()
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
                        fighter?.takeDamage(projectile.getDamage())
                        samuraiArcher?.takeDamage(projectile.getDamage())
                        samuraiCommander?.takeDamage(projectile.getDamage())
                        projectile.markDamageDealt()
                    }
                }
            }
        }

        for (dragon in smallDragons) {
            dragon.update(playerX, playerY)
            if (!dragon.isDead()) {
                for (projectile in dragon.projectiles) {
                    if (projectile.canDealDamage() && projectile.isCollidingWith(playerX, playerY, 110f)) {
                        fighter?.takeDamage(projectile.getDamage())
                        samuraiArcher?.takeDamage(projectile.getDamage())
                        samuraiCommander?.takeDamage(projectile.getDamage())
                        projectile.markDamageDealt()
                    }
                }
            }
        }

        for (dragon in dragons) {
            dragon.update(playerX, playerY)
            if (!dragon.isDead()) {
                for (fire in dragon.fireProjectiles) {
                    if (fire.canDealDamage() && fire.isCollidingWith(playerX, playerY, 90f)) {
                        fighter?.takeDamage(fire.getDamage())
                        samuraiArcher?.takeDamage(fire.getDamage())
                        samuraiCommander?.takeDamage(fire.getDamage())
                        fire.markDamageDealt()
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
                        skeleton.takeDamage(damage)
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
                        demon.takeDamage(damage)
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
                        medusa.takeDamage(damage)
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
                        jinn.takeDamage(damage)
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
                        dragon.takeDamage(damage)
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
                        dragon.takeDamage(damage)
                        fighter?.markDamageDealt()
                        samuraiArcher?.markDamageDealt()
                        samuraiCommander?.markDamageDealt()
                        break
                    }
                }
            }
        }

        // Kiểm tra arrows của Samurai_Archer (viết tắt để tiết kiệm chỗ)
        samuraiArcher?.getArrows()?.forEach { arrow ->
            // Check collision với tất cả enemies
            for (skeleton in skeletons) {
                if (!skeleton.isDead() && arrow.isActive() && arrow.checkCollision(skeleton.getX(), skeleton.y, 80f)) {
                    skeleton.takeDamage(15)
                    arrow.deactivate()
                    break
                }
            }
            for (demon in demons) {
                if (!demon.isDead() && arrow.isActive() && arrow.checkCollision(demon.getX(), demon.y, 80f)) {
                    demon.takeDamage(15)
                    arrow.deactivate()
                    break
                }
            }
            for (medusa in medusas) {
                if (!medusa.isDead() && arrow.isActive() && arrow.checkCollision(medusa.getX(), medusa.y, 80f)) {
                    medusa.takeDamage(15)
                    arrow.deactivate()
                    break
                }
            }
            for (jinn in jinns) {
                if (!jinn.isDead() && arrow.isActive() && arrow.checkCollision(jinn.getX(), jinn.y, 80f)) {
                    jinn.takeDamage(15)
                    arrow.deactivate()
                    break
                }
            }
            for (smallDragon in smallDragons) {
                if (!smallDragon.isDead() && arrow.isActive() && arrow.checkCollision(smallDragon.getX(), smallDragon.y, 120f)) {
                    smallDragon.takeDamage(15)
                    arrow.deactivate()
                    break
                }
            }
            for (dragon in dragons) {
                if (!dragon.isDead() && arrow.isActive() && arrow.checkCollision(dragon.getX(), dragon.y, 120f)) {
                    dragon.takeDamage(15)
                    arrow.deactivate()
                    break
                }
            }
        }

        // Tương tự cho skill projectiles
        samuraiArcher?.getSkillProjectiles()?.forEach { skillProj ->
            for (skeleton in skeletons) {
                if (!skeleton.isDead() && skillProj.isActive() && skillProj.checkCollision(skeleton.getX(), skeleton.y, 80f)) {
                    skeleton.takeDamage(skillProj.getDamage())
                    skillProj.deactivate()
                    break
                }
            }
            for (demon in demons) {
                if (!demon.isDead() && skillProj.isActive() && skillProj.checkCollision(demon.getX(), demon.y, 80f)) {
                    demon.takeDamage(skillProj.getDamage())
                    skillProj.deactivate()
                    break
                }
            }
            for (medusa in medusas) {
                if (!medusa.isDead() && skillProj.isActive() && skillProj.checkCollision(medusa.getX(), medusa.y, 80f)) {
                    medusa.takeDamage(skillProj.getDamage())
                    skillProj.deactivate()
                    break
                }
            }
            for (jinn in jinns) {
                if (!jinn.isDead() && skillProj.isActive() && skillProj.checkCollision(jinn.getX(), jinn.y, 80f)) {
                    jinn.takeDamage(skillProj.getDamage())
                    skillProj.deactivate()
                    break
                }
            }
            for (smallDragon in smallDragons) {
                if (!smallDragon.isDead() && skillProj.isActive() && skillProj.checkCollision(smallDragon.getX(), smallDragon.y, 120f)) {
                    smallDragon.takeDamage(skillProj.getDamage())
                    skillProj.deactivate()
                    break
                }
            }
            for (dragon in dragons) {
                if (!dragon.isDead() && skillProj.isActive() && skillProj.checkCollision(dragon.getX(), dragon.y, 120f)) {
                    dragon.takeDamage(skillProj.getDamage())
                    skillProj.deactivate()
                    break
                }
            }
        }
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

        fighter?.draw(canvas)
        samuraiArcher?.draw(canvas)
        samuraiCommander?.draw(canvas)

        canvas.restore()

        fighter?.drawUI(canvas)
        samuraiArcher?.drawUI(canvas)
        samuraiCommander?.drawUI(canvas)

        joystick.draw(canvas)
        settingsButton.draw(canvas)
        attackButton.draw(canvas)
        jumpButton.draw(canvas)

        if (characterType == "Fighter") {
            shieldButton.draw(canvas)
        } else if (characterType == "Samurai_Archer") {
            bowButton.draw(canvas)
        }
    }

    private fun showGameOver() {
        handler.postDelayed({
            gameOverDialog = GameOverDialog(
                context,
                onContinue = {
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
        handler.post {
            pauseMenuDialog = PauseMenuDialog(
                context,
                onContinue = {
                    isPaused = false
                },
                onRestart = {
                    isPaused = false
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

        val victoryRecord = VictoryRecord(
            completionTimeMs = completionTime,
            characterType = characterType,
            enemiesKilled = totalEnemies, // Sử dụng totalEnemies từ LevelConfig
            timestamp = System.currentTimeMillis()
        )

        victoryManager.saveVictory(victoryRecord)

        handler.postDelayed({
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
        handler.post {
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
        handler.post {
            levelVictoryDialog = LevelVictoryDialog(
                context,
                victoryRecord,
                currentLevel,
                onNextLevel = {
                    // Chuyển sang level tiếp theo
                    levelManager.proceedToNextLevel(currentLevel, characterType)
                },
                onViewHistory = {
                    (context as? MainActivity)?.let { activity ->
                        val intent = Intent(context, VictoryHistoryActivity::class.java)
                        activity.startActivity(intent)
                    }
                },
                onPlayAgain = {
                    isVictory = false
                    resetGame()
                },
                onMainMenu = {
                    (context as? MainActivity)?.let { activity ->
                        val intent = Intent(context, MainMenuActivity::class.java)
                        activity.startActivity(intent)
                        activity.finish()
                    }
                }
            )
            levelVictoryDialog?.show()
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

        // Reset wave system
        currentWaveNumber = 1
        wavesCompleted = 0
        lastWaveClearedTime = 0
        isWaitingForNextWave = false
        currentWaveEnemyCount = 0

        gameStartTime = System.currentTimeMillis()
    }

    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
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