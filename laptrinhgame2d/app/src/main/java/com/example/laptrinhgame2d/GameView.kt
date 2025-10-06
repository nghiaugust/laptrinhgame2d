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
    private val mapType: Int = 1  // ===== THÊM: Nhận map type =====
) : SurfaceView(context), SurfaceHolder.Callback {
    
    // ==================== ENEMY SPAWN CONFIGURATION ====================
    // Cấu hình số lượng quái cho TỪNG MÀN CHƠI
    companion object {
        // MAP 1: GRASSLAND (Dễ) - Ít quái, cân bằng
        const val MAP1_SKELETONS = 2
        const val MAP1_DEMONS = 2
        const val MAP1_MEDUSAS = 2
        const val MAP1_JINNS = 1
        const val MAP1_SMALL_DRAGONS = 1
        const val MAP1_DRAGONS = 1
        
        // MAP 2: DESERT (Trung bình) - Tăng số lượng
        const val MAP2_SKELETONS = 3
        const val MAP2_DEMONS = 3
        const val MAP2_MEDUSAS = 3
        const val MAP2_JINNS = 2
        const val MAP2_SMALL_DRAGONS = 2
        const val MAP2_DRAGONS = 2
        
        // MAP 3: VOLCANO (Khó) - Nhiều quái nhất
        const val MAP3_SKELETONS = 4
        const val MAP3_DEMONS = 4
        const val MAP3_MEDUSAS = 4
        const val MAP3_JINNS = 3
        const val MAP3_SMALL_DRAGONS = 3
        const val MAP3_DRAGONS = 3
    }
    // ===================================================================
    
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
    private val jinns = mutableListOf<Jinn>()
    private val smallDragons = mutableListOf<SmallDragon>()
    private val dragons = mutableListOf<Dragon>()
    
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
    private val totalEnemies: Int
        get() {
            // Tính tổng số quái theo map hiện tại
            return when (mapType) {
                1 -> MAP1_SKELETONS + MAP1_DEMONS + MAP1_MEDUSAS + MAP1_JINNS + MAP1_SMALL_DRAGONS + MAP1_DRAGONS
                2 -> MAP2_SKELETONS + MAP2_DEMONS + MAP2_MEDUSAS + MAP2_JINNS + MAP2_SMALL_DRAGONS + MAP2_DRAGONS
                3 -> MAP3_SKELETONS + MAP3_DEMONS + MAP3_MEDUSAS + MAP3_JINNS + MAP3_SMALL_DRAGONS + MAP3_DRAGONS
                else -> MAP1_SKELETONS + MAP1_DEMONS + MAP1_MEDUSAS + MAP1_JINNS + MAP1_SMALL_DRAGONS + MAP1_DRAGONS
            }
        }
    
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
        spawnJinns()
        spawnSmallDragons()
        spawnDragons()
    }

    private fun spawnSkeletons() {
        // Lấy số lượng skeleton theo map hiện tại
        val numSkeletons = when (mapType) {
            1 -> MAP1_SKELETONS
            2 -> MAP2_SKELETONS
            3 -> MAP3_SKELETONS
            else -> MAP1_SKELETONS
        }
        
        // Spawn skeletons
        for (i in 0 until numSkeletons) {
            val x = 800f + (i * 400f) + (Math.random() * 200f).toFloat() // Cách đều khoảng 400px, random thêm 0-200px
            val y = 400f + (Math.random() * 400f).toFloat() // Random Y từ 400-800
            skeletons.add(Skeleton(gameContext, x, y))
        }
    }

    private fun spawnDemons() {
        // Lấy số lượng demon theo map hiện tại
        val numDemons = when (mapType) {
            1 -> MAP1_DEMONS
            2 -> MAP2_DEMONS
            3 -> MAP3_DEMONS
            else -> MAP1_DEMONS
        }
        
        // Spawn demons
        for (i in 0 until numDemons) {
            val x = 900f + (i * 400f) + (Math.random() * 300f).toFloat() // Cách đều khoảng 600px
            val y = 400f + (Math.random() * 400f).toFloat() // Random Y từ 400-800
            demons.add(Demon(gameContext, x, y))
        }
    }

    private fun spawnMedusas() {
        // Lấy số lượng medusa theo map hiện tại
        val numMedusas = when (mapType) {
            1 -> MAP1_MEDUSAS
            2 -> MAP2_MEDUSAS
            3 -> MAP3_MEDUSAS
            else -> MAP1_MEDUSAS
        }
        
        // Spawn medusas
        for (i in 0 until numMedusas) {
            val x = 1000f + (i * 400f) + (Math.random() * 300f).toFloat() // Cách đều khoảng 700px
            val y = 400f + (Math.random() * 400f).toFloat() // Random Y từ 400-800
            medusas.add(Medusa(gameContext, x, y))
        }
    }
    
    private fun spawnJinns() {
        // Lấy số lượng jinn theo map hiện tại
        val numJinns = when (mapType) {
            1 -> MAP1_JINNS
            2 -> MAP2_JINNS
            3 -> MAP3_JINNS
            else -> MAP1_JINNS
        }
        
        // Spawn jinns
        for (i in 0 until numJinns) {
            val x = 1100f + (i * 400f) + (Math.random() * 400f).toFloat() // Cách đều khoảng 800px
            val y = 400f + (Math.random() * 400f).toFloat() // Random Y từ 400-800
            jinns.add(Jinn(gameContext, x, y))
        }
    }
    
    private fun spawnSmallDragons() {
        // Lấy số lượng small dragon theo map hiện tại
        val numSmallDragons = when (mapType) {
            1 -> MAP1_SMALL_DRAGONS
            2 -> MAP2_SMALL_DRAGONS
            3 -> MAP3_SMALL_DRAGONS
            else -> MAP1_SMALL_DRAGONS
        }
        
        // Spawn small dragons (rồng nhỏ - tầm xa)
        for (i in 0 until numSmallDragons) {
            val x = 1200f + (i * 400f) + (Math.random() * 300f).toFloat() // Cách đều khoảng 600px, spawn xa
            val y = 400f + (Math.random() * 400f).toFloat() // Random Y từ 400-800
            smallDragons.add(SmallDragon(gameContext, x, y))
        }
    }
    
    private fun spawnDragons() {
        // Lấy số lượng dragon theo map hiện tại
        val numDragons = when (mapType) {
            1 -> MAP1_DRAGONS
            2 -> MAP2_DRAGONS
            3 -> MAP3_DRAGONS
            else -> MAP1_DRAGONS
        }
        
        // Spawn dragons (rồng lớn - phun lửa cận chiến)
        for (i in 0 until numDragons) {
            val x = 1300f + (i * 500f) + (Math.random() * 200f).toFloat() // Cách đều khoảng 700px
            val y = 400f + (Math.random() * 400f).toFloat() // Random Y từ 400-800
            dragons.add(Dragon(gameContext, x, y))
        }
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
            medusa.y = groundY - 200f // Cùng độ cao với nhân vật
        }
        
        // Cập nhật vị trí spawn của jinns (cùng độ cao với skeleton)
        jinns.forEach { jinn ->
            jinn.y = groundY - 200f
        }
        
        // Cập nhật vị trí spawn của small dragons (cùng độ cao)
        smallDragons.forEach { dragon ->
            dragon.y = groundY - 200f
        }
        
        // Cập nhật vị trí spawn của dragons (cùng độ cao)
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
            medusa.y = groundY - 200f
        }
        
        // Giữ jinns luôn ở trên mặt đất (cùng độ cao với skeleton để dễ đánh)
        jinns.forEach { jinn ->
            jinn.y = groundY - 200f
        }
        
        // Giữ small dragons luôn ở trên mặt đất
        smallDragons.forEach { dragon ->
            dragon.y = groundY - 200f
        }
        
        // Giữ dragons luôn ở trên mặt đất
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

        skeletons.removeAll { it.shouldBeRemoved() }
        demons.removeAll { it.shouldBeRemoved() }
        medusas.removeAll { it.shouldRemove() }
        
        // Xóa jinns đã chết và hết thời gian
        jinns.removeAll { it.shouldBeRemoved() }
        
        // Xóa small dragons đã chết và hết thời gian
        smallDragons.removeAll { it.shouldBeRemoved() }
        
        // Xóa dragons đã chết và hết thời gian
        dragons.removeAll { it.shouldBeRemoved() }
        
        // Kiểm tra chiến thắng (tất cả quái đã chết - không cần chờ animation)
        if (!isVictory) {
            val allSkeletonsDead = skeletons.all { it.isDead() }
            val allDemonsDead = demons.all { it.isDead() }
            val allMedusasDead = medusas.isEmpty() || medusas.all { it.isDead() }
            val allJinnsDead = jinns.isEmpty() || jinns.all { it.isDead() }
            val allSmallDragonsDead = smallDragons.isEmpty() || smallDragons.all { it.isDead() }
            val allDragonsDead = dragons.isEmpty() || dragons.all { it.isDead() }
            
            if (allSkeletonsDead && allDemonsDead && allMedusasDead && allJinnsDead && allSmallDragonsDead && allDragonsDead) {
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
                // Kiểm tra va chạm stone projectiles với player
                for (projectile in medusa.projectiles) {
                    // Kiểm tra collision khi đang bay hoặc đang nổ
                    if (projectile.isCollidingWith(playerX, playerY, 100f)) {
                        // Nếu chưa nổ, trigger explosion
                        if (!projectile.canDealDamage()) {
                            projectile.startExploding()
                        }
                        
                        // Gây damage khi đang nổ và chưa deal damage
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
        
        // Cập nhật jinns
        for (jinn in jinns) {
            jinn.update(playerX, playerY)
            
            if (!jinn.isDead()) {
                // Kiểm tra va chạm projectiles của jinn với player
                for (projectile in jinn.projectiles) {
                    // Tăng range lên 100f để dễ chạm hơn khi explosion
                    if (projectile.canDealDamage() && projectile.isCollidingWith(playerX, playerY, 100f)) {
                        // Projectile hit player
                        fighter?.takeDamage(projectile.getDamage())
                        samuraiArcher?.takeDamage(projectile.getDamage())
                        samuraiCommander?.takeDamage(projectile.getDamage())
                        projectile.markDamageDealt()
                    }
                }
            }
        }
        
        // Cập nhật small dragons
        for (dragon in smallDragons) {
            dragon.update(playerX, playerY)
            
            if (!dragon.isDead()) {
                // Kiểm tra va chạm fireballs của small dragon với player
                for (projectile in dragon.projectiles) {
                    // Tăng range lên 110f (fireball lớn hơn)
                    if (projectile.canDealDamage() && projectile.isCollidingWith(playerX, playerY, 110f)) {
                        // Fireball hit player
                        fighter?.takeDamage(projectile.getDamage())
                        samuraiArcher?.takeDamage(projectile.getDamage())
                        samuraiCommander?.takeDamage(projectile.getDamage())
                        projectile.markDamageDealt()
                    }
                }
            }
        }
        
        // Cập nhật dragons (phun lửa cận chiến)
        for (dragon in dragons) {
            dragon.update(playerX, playerY)
            
            if (!dragon.isDead()) {
                // Kiểm tra va chạm fire projectiles với player
                for (fire in dragon.fireProjectiles) {
                    if (fire.canDealDamage() && fire.isCollidingWith(playerX, playerY, 90f)) {
                        // Fire hit player
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
            
            // Kiểm tra player attack hit jinns
            for (jinn in jinns) {
                if (!jinn.isDead()) {
                    if (jinn.isCollidingWith(playerX, playerY, attackRange)) {
                        // Kiểm tra hướng tấn công
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
            }
            
            // Kiểm tra player attack hit small dragons
            for (dragon in smallDragons) {
                if (!dragon.isDead()) {
                    if (dragon.isCollidingWith(playerX, playerY, attackRange)) {
                        // Kiểm tra hướng tấn công
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
            
            // Kiểm tra player attack hit dragons
            for (dragon in dragons) {
                if (!dragon.isDead()) {
                    if (dragon.isCollidingWith(playerX, playerY, attackRange)) {
                        // Kiểm tra hướng tấn công
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
        }
        
        // Kiểm tra arrows của Samurai_Archer hit skeletons
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
                    if (arrow.checkCollision(medusa.getX(), medusa.y, 80f)) {
                        medusa.takeDamage(15)
                        arrow.deactivate()
                        break
                    }
                }
            }
            
            // Kiểm tra arrow hit jinns
            for (jinn in jinns) {
                if (!jinn.isDead() && arrow.isActive()) {
                    if (arrow.checkCollision(jinn.getX(), jinn.y, 80f)) {
                        jinn.takeDamage(15)
                        arrow.deactivate()
                        break
                    }
                }
            }

            // Kiểm tra arrow hit small dragons
            for (smallDragon in smallDragons) {
                if (!smallDragon.isDead() && arrow.isActive()) {
                    if (arrow.checkCollision(smallDragon.getX(), smallDragon.y, 120f)) {
                        smallDragon.takeDamage(15)
                        arrow.deactivate()
                        break
                    }
                }
            }
            
            // Kiểm tra arrow hit dragons
            for (dragon in dragons) {
                if (!dragon.isDead() && arrow.isActive()) {
                    if (arrow.checkCollision(dragon.getX(), dragon.y, 120f)) {
                        dragon.takeDamage(15)
                        arrow.deactivate()
                        break
                    }
                }
            }
        }
        
        // Kiểm tra skill projectiles của Samurai_Archer hit skeletons
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
                    if (skillProj.checkCollision(medusa.getX(), medusa.y, 80f)) {
                        medusa.takeDamage(skillProj.getDamage())
                        skillProj.deactivate()
                        break
                    }
                }
            }
            
            // Kiểm tra skill projectile hit jinns
            for (jinn in jinns) {
                if (!jinn.isDead() && skillProj.isActive()) {
                    if (skillProj.checkCollision(jinn.getX(), jinn.y, 80f)) {
                        jinn.takeDamage(skillProj.getDamage())
                        skillProj.deactivate()
                        break
                    }
                }
            }

            // Kiểm tra skill projectile hit small dragons
            for (smallDragon in smallDragons) {
                if (!smallDragon.isDead() && skillProj.isActive()) {
                    if (skillProj.checkCollision(smallDragon.getX(), smallDragon.y, 120f)) {
                        smallDragon.takeDamage(skillProj.getDamage())
                        skillProj.deactivate()
                        break
                    }
                }
            }
            
            // Kiểm tra skill projectile hit dragons
            for (dragon in dragons) {
                if (!dragon.isDead() && skillProj.isActive()) {
                    if (skillProj.checkCollision(dragon.getX(), dragon.y, 120f)) {
                        dragon.takeDamage(skillProj.getDamage())
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
            medusa.draw(canvas)
        }
        
        // Vẽ jinns
        for (jinn in jinns) {
            jinn.draw(canvas)
        }

        // Vẽ small dragons
        for (smallDragon in smallDragons) {
            smallDragon.draw(canvas)
        }
        
        // Vẽ dragons (phun lửa cận chiến)
        for (dragon in dragons) {
            dragon.draw(canvas)
        }
        
        // Vẽ nhân vật
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
        
        // Xóa tất cả jinns cũ và spawn lại
        jinns.clear()
        spawnJinns()

        // Xóa tất cả small dragons cũ và spawn lại
        smallDragons.clear()
        spawnSmallDragons()
        
        // Xóa tất cả dragons cũ và spawn lại
        dragons.clear()
        spawnDragons()
        
        // Reset thời gian
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