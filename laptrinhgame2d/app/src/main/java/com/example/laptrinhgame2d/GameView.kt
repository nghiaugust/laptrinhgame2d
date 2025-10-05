package com.example.laptrinhgame2d

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

class GameView(
    context: Context,
    private val characterType: String = "Fighter",
    private val mapType: Int = 1  // ===== THÊM: Nhận map type =====
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

    // ===== THAY ĐỔI: Map system với 3 maps =====
    private var grasslandMap: GrasslandMap? = null
    private var desertMap: DesertMap? = null
    private var volcanoMap: VolcanoMap? = null

    // Enemies
    private val skeletons = mutableListOf<Skeleton>()
    private val demons = mutableListOf<Demon>()
    private val medusas = mutableListOf<Medusa>()

    // Camera
    private var cameraX = 0f
    private var cameraY = 0f

    // Game Over
    private var gameOverDialog: GameOverDialog? = null
    private var isGameOver = false

    // Pause Menu
    private var pauseMenuDialog: PauseMenuDialog? = null
    private var isPaused = false

    // Victory system
    private var victoryDialog: VictoryDialog? = null
    private var isVictory = false
    private var gameStartTime: Long = 0
    private var victoryManager: VictoryManager
    private val totalEnemies = 8

    init {
        holder.addCallback(this)

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

        spawnSkeletons()
        spawnDemons()
        spawnMedusas()
    }

    private fun spawnSkeletons() {
        skeletons.add(Skeleton(gameContext, 800f, 400f))
        skeletons.add(Skeleton(gameContext, 1200f, 600f))
        skeletons.add(Skeleton(gameContext, 1500f, 300f))
        skeletons.add(Skeleton(gameContext, 600f, 800f))
        skeletons.add(Skeleton(gameContext, 2000f, 500f))
    }

    private fun spawnDemons() {
        demons.add(Demon(gameContext, 1800f, 400f))
        demons.add(Demon(gameContext, 2500f, 600f))
        demons.add(Demon(gameContext, 3000f, 500f))
    }

    private fun spawnMedusas() {
        // Tạm thời không spawn
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        val screenWidth = width.toFloat()
        val screenHeight = height.toFloat()

        // ===== THAY ĐỔI: Khởi tạo 3 maps =====
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
            medusa.setY(groundY - 200f)
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

        // ===== THAY ĐỔI: Update map theo loại =====
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

        // ===== THAY ĐỔI: Lấy groundY từ map hiện tại =====
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
            medusa.setY(groundY - 200f)
        }

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

        skeletons.removeAll { it.shouldBeRemoved() }
        demons.removeAll { it.shouldBeRemoved() }
        medusas.removeAll { it.shouldRemove() }

        if (!isVictory) {
            val allSkeletonsDead = skeletons.all { it.isDead() }
            val allDemonsDead = demons.all { it.isDead() }
            val allMedusasDead = medusas.isEmpty() || medusas.all { it.isDead() }

            if (allSkeletonsDead && allDemonsDead && allMedusasDead) {
                isVictory = true
                showVictory()
            }
        }

        for (skeleton in skeletons) {
            skeleton.update(playerX, playerY)

            if (!skeleton.isDead()) {
                if (skeleton.canDealDamage()) {
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
        }

        for (demon in demons) {
            demon.update(playerX, playerY)

            if (!demon.isDead()) {
                if (demon.canDealDamage()) {
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
        }

        for (medusa in medusas) {
            medusa.update(playerX, playerY, playerIsDead)

            if (!medusa.isDead()) {
                for (stone in medusa.stones) {
                    if (stone.checkCollision(playerX, playerY, 100f, 200f)) {
                        fighter?.takeDamage(stone.getDamage())
                        samuraiArcher?.takeDamage(stone.getDamage())
                        samuraiCommander?.takeDamage(stone.getDamage())
                        stone.setDamageDealt()
                        stone.startBreaking()
                    }
                }
            }
        }

        val canPlayerDealDamage = fighter?.canDealDamage() ?: samuraiArcher?.canDealDamage() ?: samuraiCommander?.canDealDamage() ?: false

        if (canPlayerDealDamage) {
            val attackRange = 200f
            val damage = fighter?.getAttackDamage() ?: samuraiArcher?.getAttackDamage() ?: samuraiCommander?.getAttackDamage() ?: 0

            for (skeleton in skeletons) {
                if (!skeleton.isDead()) {
                    if (skeleton.isCollidingWith(playerX, playerY, attackRange)) {
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
            }

            for (demon in demons) {
                if (!demon.isDead()) {
                    if (demon.isCollidingWith(playerX, playerY, attackRange)) {
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
            }

            for (medusa in medusas) {
                if (!medusa.isDead()) {
                    if (medusa.isCollidingWith(playerX, playerY, attackRange)) {
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
            }
        }

        samuraiArcher?.getArrows()?.forEach { arrow ->
            for (skeleton in skeletons) {
                if (!skeleton.isDead() && arrow.isActive()) {
                    if (arrow.checkCollision(skeleton.getX(), skeleton.y, 80f)) {
                        skeleton.takeDamage(15)
                        arrow.deactivate()
                        break
                    }
                }
            }

            for (demon in demons) {
                if (!demon.isDead() && arrow.isActive()) {
                    if (arrow.checkCollision(demon.getX(), demon.y, 80f)) {
                        demon.takeDamage(15)
                        arrow.deactivate()
                        break
                    }
                }
            }

            for (medusa in medusas) {
                if (!medusa.isDead() && arrow.isActive()) {
                    if (arrow.checkCollision(medusa.getX(), medusa.getY(), 80f)) {
                        medusa.takeDamage(15)
                        arrow.deactivate()
                        break
                    }
                }
            }
        }

        samuraiArcher?.getSkillProjectiles()?.forEach { skillProj ->
            for (skeleton in skeletons) {
                if (!skeleton.isDead() && skillProj.isActive()) {
                    if (skillProj.checkCollision(skeleton.getX(), skeleton.y, 80f)) {
                        skeleton.takeDamage(skillProj.getDamage())
                        skillProj.deactivate()
                        break
                    }
                }
            }

            for (demon in demons) {
                if (!demon.isDead() && skillProj.isActive()) {
                    if (skillProj.checkCollision(demon.getX(), demon.y, 80f)) {
                        demon.takeDamage(skillProj.getDamage())
                        skillProj.deactivate()
                        break
                    }
                }
            }

            for (medusa in medusas) {
                if (!medusa.isDead() && skillProj.isActive()) {
                    if (skillProj.checkCollision(medusa.getX(), medusa.getY(), 80f)) {
                        medusa.takeDamage(skillProj.getDamage())
                        skillProj.deactivate()
                        break
                    }
                }
            }
        }
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        // ===== THAY ĐỔI: Vẽ map theo loại =====
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
            medusa.draw(canvas, cameraX, cameraY)
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

    private fun showVictory() {
        val completionTime = System.currentTimeMillis() - gameStartTime

        val victoryRecord = VictoryRecord(
            completionTimeMs = completionTime,
            characterType = characterType,
            enemiesKilled = totalEnemies,
            timestamp = System.currentTimeMillis()
        )

        victoryManager.saveVictory(victoryRecord)

        handler.postDelayed({
            victoryDialog = VictoryDialog(
                context,
                victoryRecord,
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
            victoryDialog?.show()
        }, 500)
    }

    private fun resetGame() {
        isGameOver = false
        isVictory = false

        fighter?.reset()
        samuraiArcher?.reset()
        samuraiCommander?.reset()

        skeletons.clear()
        spawnSkeletons()

        demons.clear()
        spawnDemons()

        medusas.clear()
        spawnMedusas()

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