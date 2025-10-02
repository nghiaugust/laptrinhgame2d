package com.example.laptrinhgame2d

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.io.IOException

class GameView(context: Context, private val characterType: String = "Fighter") : SurfaceView(context), SurfaceHolder.Callback {
    
    private var gameThread: GameThread? = null
    private val gameContext: Context = context
    private var fighter: Fighter? = null
    private var samuraiArcher: SamuraiArcher? = null
    private var samuraiCommander: SamuraiCommander? = null
    private val joystick: Joystick
    private val attackButton: GameButton
    private val shieldButton: GameButton
    private val jumpButton: GameButton
    private val bowButton: GameButton // Nút bắn cung cho Samurai_Archer
    private val settingsButton: GameButton // Nút settings
    
    // Map system
    private var gameMap: GameMap? = null
    
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
    private val totalEnemies = 8 // 5 skeletons + 3 demons (tạm thời bỏ 3 medusas)
    
    init {
        holder.addCallback(this)
        
        victoryManager = VictoryManager(context)
        
        // Khởi tạo joystick (bên trái màn hình) - 2 nút trái/phải (to hơn)
        joystick = Joystick(200f, 0f, 120f, 100f) // buttonWidth=120, buttonHeight=100
        
        // Khởi tạo các nút (bên phải màn hình)
        attackButton = GameButton(0f, 0f, 90f, "Attack", Color.rgb(255, 100, 100))
        shieldButton = GameButton(0f, 0f, 90f, "Shield", Color.rgb(100, 100, 255))
        jumpButton = GameButton(0f, 0f, 90f, "Jump", Color.rgb(100, 255, 100))
        bowButton = GameButton(0f, 0f, 90f, "Bow", Color.rgb(255, 165, 0)) // Màu cam
        settingsButton = GameButton(0f, 0f, 70f, "⚙", Color.rgb(150, 150, 150)) // Nút settings nhỏ hơn
        
        // Khởi tạo nhân vật theo loại
        when (characterType) {
            "Fighter" -> fighter = Fighter(context, 500f, 400f)
            "Samurai_Archer" -> samuraiArcher = SamuraiArcher(context, 500f, 400f)
            "Samurai_Commander" -> samuraiCommander = SamuraiCommander(context, 500f, 400f)
        }
        
        // Khởi tạo enemies
        spawnSkeletons()
        spawnDemons()
        spawnMedusas()
    }
    
    private fun spawnSkeletons() {
        // Spawn skeletons ở các vị trí khác nhau trong thế giới game rộng hơn
        skeletons.add(Skeleton(gameContext, 800f, 400f))
        skeletons.add(Skeleton(gameContext, 1200f, 600f))
        skeletons.add(Skeleton(gameContext, 1500f, 300f))
        skeletons.add(Skeleton(gameContext, 600f, 800f))
        skeletons.add(Skeleton(gameContext, 2000f, 500f))
    }
    
    private fun spawnDemons() {
        // Spawn demons ở xa hơn và ít hơn (vì mạnh hơn)
        demons.add(Demon(gameContext, 1800f, 400f))
        demons.add(Demon(gameContext, 2500f, 600f))
        demons.add(Demon(gameContext, 3000f, 500f))
    }
    
    private fun spawnMedusas() {
        // Spawn medusas ở khu vực xa (ranged enemies)
        // Tạm thời không spawn medusa để test
        // medusas.add(Medusa(gameContext, 2200f, 400f))
        // medusas.add(Medusa(gameContext, 3500f, 600f))
        // medusas.add(Medusa(gameContext, 4000f, 500f))
    }
    
    override fun surfaceCreated(holder: SurfaceHolder) {
        // Cập nhật vị trí nút khi biết kích thước màn hình
        val screenWidth = width.toFloat()
        val screenHeight = height.toFloat()
        
        // Khởi tạo game map
        gameMap = GameMap(gameContext, width, height)
        
        // Cập nhật vị trí spawn của nhân vật theo groundY
        val groundY = gameMap?.groundY ?: (screenHeight * 0.7f)
        when (characterType) {
            "Fighter" -> {
                fighter?.y = groundY - 200f // Đặt nhân vật đứng trên mặt đất
                fighter?.setGroundY(groundY - 200f) // Set groundY cho jump physics
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
        
        // Cập nhật vị trí spawn của skeletons
        skeletons.forEach { skeleton ->
            skeleton.y = groundY - 200f
        }
        
        // Cập nhật vị trí spawn của demons (thấp hơn để chân chạm đất)
        demons.forEach { demon ->
            demon.y = groundY - 180f // Thấp hơn 20 pixels so với nhân vật
        }
        
        // Cập nhật vị trí spawn của medusas
        medusas.forEach { medusa ->
            medusa.setY(groundY - 200f) // Cùng độ cao với nhân vật
        }
        
        joystick.centerY = screenHeight - 200f
        
        // Vị trí nút settings (góc trên trái, cạnh thanh máu)
        settingsButton.x = 300f // Cách thanh máu 1 khoảng
        settingsButton.y = 35f // Ngang với thanh máu
        
        if (characterType == "Fighter") {
            // Sắp xếp 3 nút theo hình tam giác cho Fighter
            attackButton.x = screenWidth - 230f
            attackButton.y = screenHeight - 200f
            
            shieldButton.x = screenWidth - 120f
            shieldButton.y = screenHeight - 200f
            
            jumpButton.x = screenWidth - 175f
            jumpButton.y = screenHeight - 320f
        } else {
            // Sắp xếp 3 nút cho Samurai_Archer (tam giác)
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
        
        // Bắt đầu tính giờ
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
                
                // Kiểm tra touch vào joystick (trái hoặc phải)
                if (x < width / 2) {
                    joystick.onTouchDown(x, y, pointerId)
                }
                
                // Kiểm tra touch vào nút settings
                if (settingsButton.isPressed(x, y)) {
                    settingsButton.onTouch(pointerId)
                    showPauseMenu()
                    return true
                }
                
                // Kiểm tra touch vào nút attack
                if (attackButton.isPressed(x, y)) {
                    attackButton.onTouch(pointerId)
                    fighter?.attack()
                    samuraiArcher?.attack()
                    samuraiCommander?.attack()
                }
                
                // Kiểm tra touch vào nút bow (chỉ cho Samurai_Archer)
                if (characterType == "Samurai_Archer" && bowButton.isPressed(x, y)) {
                    bowButton.onTouch(pointerId)
                    samuraiArcher?.let { archer ->
                        if (archer.getCombatMode() == SamuraiArcher.CombatMode.MELEE) {
                            archer.switchCombatMode()
                        }
                        archer.attack()
                    }
                }
                
                // Kiểm tra touch vào nút shield (chỉ cho Fighter)
                if (characterType == "Fighter" && shieldButton.isPressed(x, y)) {
                    shieldButton.onTouch(pointerId)
                    fighter?.activateShield()
                }
                
                // Kiểm tra touch vào nút jump
                if (jumpButton.isPressed(x, y)) {
                    jumpButton.onTouch(pointerId)
                    fighter?.jump()
                    samuraiArcher?.jump()
                    samuraiCommander?.jump()
                }
            }
            
            MotionEvent.ACTION_MOVE -> {
                // Không cần xử lý MOVE cho joystick nút bấm
            }
            
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                val index = event.actionIndex
                val pointerId = event.getPointerId(index)
                
                // Reset joystick
                joystick.onTouchUp(pointerId)
                
                if (attackButton.pointerId == pointerId) {
                    attackButton.reset()
                    // Nhả nút attack cho Samurai_Archer (để kiểm tra charged attack)
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
        // Nếu game over, paused, hoặc victory, không update nữa
        if (isGameOver || isPaused || isVictory) return
        
        // Update game map
        gameMap?.update(cameraX, cameraY)
        
        // Cập nhật nhân vật dựa trên joystick
        val joystickX = joystick.getX()
        val joystickY = 0f // Không cho di chuyển theo trục Y nữa
        
        fighter?.update(joystickX, joystickY)
        samuraiArcher?.update(joystickX, joystickY)
        samuraiCommander?.update(joystickX, joystickY)
        
        // Lấy groundY cho enemies (heroes tự quản lý Y thông qua jump physics)
        val groundY = gameMap?.groundY ?: (height * 0.7f)
        
        // Giữ skeletons luôn ở trên mặt đất
        skeletons.forEach { skeleton ->
            skeleton.y = groundY - 200f
        }
        
        // Giữ demons luôn ở trên mặt đất (thấp hơn để chân chạm đất)
        demons.forEach { demon ->
            demon.y = groundY - 180f // Thấp hơn 20 pixels so với nhân vật
        }
        
        // Giữ medusas luôn ở trên mặt đất
        medusas.forEach { medusa ->
            medusa.setY(groundY - 200f)
        }
        
        // Lấy vị trí và trạng thái của nhân vật đang chơi
        val playerX = fighter?.getX() ?: samuraiArcher?.getX() ?: samuraiCommander?.getX() ?: 0f
        val playerY = fighter?.y ?: samuraiArcher?.y ?: samuraiCommander?.y ?: 0f
        val playerIsDead = fighter?.isDead() ?: samuraiArcher?.isDead() ?: samuraiCommander?.isDead() ?: false
        val playerFacingRight = fighter?.getFacingRight() ?: samuraiArcher?.getFacingRight() ?: samuraiCommander?.getFacingRight() ?: true
        
        // Kiểm tra player có chết không
        if (playerIsDead && !isGameOver) {
            isGameOver = true
            showGameOver()
        }
        
        // Cập nhật camera để theo player (chỉ theo trục X, Y cố định ở groundY)
        // Cho phép player di chuyển tự do trong 30% màn hình ở giữa, chỉ di chuyển camera khi ra khỏi vùng này
        val cameraDeadZoneLeft = width * 0.35f  // 35% từ trái
        val cameraDeadZoneRight = width * 0.65f // 65% từ trái (vùng chết giữa là 30%)
        
        val playerScreenX = playerX - cameraX // Vị trí player trên màn hình
        
        // Chỉ di chuyển camera khi player ra khỏi dead zone
        if (playerScreenX < cameraDeadZoneLeft) {
            cameraX = playerX - cameraDeadZoneLeft
        } else if (playerScreenX > cameraDeadZoneRight) {
            cameraX = playerX - cameraDeadZoneRight
        }
        
        // Không giới hạn camera về phía trái (cho phép đi xa vô hạn)
        // Nếu muốn giới hạn, uncomment dòng này:
        // cameraX = cameraX.coerceAtLeast(0f)
        
        // Camera Y cố định ở 0 (nhìn từ trên xuống, không scroll theo Y)
        cameraY = 0f
        
        // Xóa skeletons đã chết và hết thời gian
        skeletons.removeAll { it.shouldBeRemoved() }
        
        // Xóa demons đã chết và hết thời gian
        demons.removeAll { it.shouldBeRemoved() }
        
        // Xóa medusas đã chết và hết thời gian
        medusas.removeAll { it.shouldRemove() }
        
        // Kiểm tra chiến thắng (tất cả quái đã chết - không cần chờ animation)
        if (!isVictory) {
            val allSkeletonsDead = skeletons.all { it.isDead() }
            val allDemonsDead = demons.all { it.isDead() }
            val allMedusasDead = medusas.isEmpty() || medusas.all { it.isDead() }
            
            if (allSkeletonsDead && allDemonsDead && allMedusasDead) {
                isVictory = true
                showVictory()
            }
        }
        
        // Cập nhật skeletons
        for (skeleton in skeletons) {
            skeleton.update(playerX, playerY)
            
            if (!skeleton.isDead()) {
                // Kiểm tra skeleton có thể gây damage không (đang ở frame attack chính)
                if (skeleton.canDealDamage()) {
                    val attackRange = 200f // Tăng từ 120f lên 200f để skeleton không cần đi đè lên player
                    val isPlayerColliding = fighter?.isCollidingWith(skeleton.getX(), skeleton.y, attackRange) 
                        ?: samuraiArcher?.isCollidingWith(skeleton.getX(), skeleton.y, attackRange)
                        ?: samuraiCommander?.isCollidingWith(skeleton.getX(), skeleton.y, attackRange)
                        ?: false
                    
                    if (isPlayerColliding) {
                        // Skeleton attack hit player
                        fighter?.takeDamage(skeleton.getAttackDamage())
                        samuraiArcher?.takeDamage(skeleton.getAttackDamage())
                        samuraiCommander?.takeDamage(skeleton.getAttackDamage())
                        skeleton.markDamageDealt()
                    }
                }
            }
        }
        
        // Cập nhật demons
        for (demon in demons) {
            demon.update(playerX, playerY)
            
            if (!demon.isDead()) {
                // Kiểm tra demon có thể gây damage không (đang ở frame attack chính)
                if (demon.canDealDamage()) {
                    val attackRange = 250f // Demon có phạm vi tấn công xa hơn skeleton
                    val isPlayerColliding = fighter?.isCollidingWith(demon.getX(), demon.y, attackRange) 
                        ?: samuraiArcher?.isCollidingWith(demon.getX(), demon.y, attackRange)
                        ?: samuraiCommander?.isCollidingWith(demon.getX(), demon.y, attackRange)
                        ?: false
                    
                    if (isPlayerColliding) {
                        // Demon attack hit player (30 damage)
                        fighter?.takeDamage(demon.getAttackDamage())
                        samuraiArcher?.takeDamage(demon.getAttackDamage())
                        samuraiCommander?.takeDamage(demon.getAttackDamage())
                        demon.markDamageDealt()
                    }
                }
            }
        }
        
        // Cập nhật medusas
        for (medusa in medusas) {
            medusa.update(playerX, playerY, playerIsDead)
            
            if (!medusa.isDead()) {
                // Kiểm tra va chạm stones với player
                for (stone in medusa.stones) {
                    if (stone.checkCollision(playerX, playerY, 100f, 200f)) {
                        // Stone hit player
                        fighter?.takeDamage(stone.getDamage())
                        samuraiArcher?.takeDamage(stone.getDamage())
                        samuraiCommander?.takeDamage(stone.getDamage())
                        stone.setDamageDealt()
                        stone.startBreaking()
                    }
                }
            }
        }
        
        // Kiểm tra player attack hit skeletons
        val canPlayerDealDamage = fighter?.canDealDamage() ?: samuraiArcher?.canDealDamage() ?: samuraiCommander?.canDealDamage() ?: false
        
        if (canPlayerDealDamage) {
            val attackRange = 200f // Tăng từ 150f lên 200f để dễ đánh hơn
            val damage = fighter?.getAttackDamage() ?: samuraiArcher?.getAttackDamage() ?: samuraiCommander?.getAttackDamage() ?: 0
            
            for (skeleton in skeletons) {
                if (!skeleton.isDead()) {
                    if (skeleton.isCollidingWith(playerX, playerY, attackRange)) {
                        // Kiểm tra hướng tấn công (player phải quay về phía skeleton)
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
            
            // Kiểm tra player attack hit demons
            for (demon in demons) {
                if (!demon.isDead()) {
                    if (demon.isCollidingWith(playerX, playerY, attackRange)) {
                        // Kiểm tra hướng tấn công (player phải quay về phía demon)
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
            
            // Kiểm tra player attack hit medusas
            for (medusa in medusas) {
                if (!medusa.isDead()) {
                    if (medusa.isCollidingWith(playerX, playerY, attackRange)) {
                        // Kiểm tra hướng tấn công
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
            
            // Kiểm tra arrow hit demons
            for (demon in demons) {
                if (!demon.isDead() && arrow.isActive()) {
                    if (arrow.checkCollision(demon.getX(), demon.y, 80f)) {
                        demon.takeDamage(15)
                        arrow.deactivate()
                        break
                    }
                }
            }
            
            // Kiểm tra arrow hit medusas
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
            
            // Kiểm tra skill projectile hit demons
            for (demon in demons) {
                if (!demon.isDead() && skillProj.isActive()) {
                    if (skillProj.checkCollision(demon.getX(), demon.y, 80f)) {
                        demon.takeDamage(skillProj.getDamage())
                        skillProj.deactivate()
                        break
                    }
                }
            }
            
            // Kiểm tra skill projectile hit medusas
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
        
        // Vẽ background map (không bị ảnh hưởng bởi camera translate)
        gameMap?.draw(canvas, cameraX, cameraY)
        
        // Lưu trạng thái canvas và áp dụng camera transform
        canvas.save()
        canvas.translate(-cameraX, -cameraY)
        
        // Vẽ skeletons (vẽ trước để player ở trên)
        for (skeleton in skeletons) {
            skeleton.draw(canvas)
        }
        
        // Vẽ demons
        for (demon in demons) {
            demon.draw(canvas)
        }
        
        // Vẽ medusas
        for (medusa in medusas) {
            medusa.draw(canvas, cameraX, cameraY)
        }
        
        // Vẽ nhân vật
        fighter?.draw(canvas)
        samuraiArcher?.draw(canvas)
        samuraiCommander?.draw(canvas)
        
        // Khôi phục canvas để vẽ UI ở vị trí cố định
        canvas.restore()
        
        // Vẽ UI của nhân vật (health, armor bars) - không bị ảnh hưởng bởi camera
        fighter?.drawUI(canvas)
        samuraiArcher?.drawUI(canvas)
        samuraiCommander?.drawUI(canvas)
        
        // Vẽ joystick
        joystick.draw(canvas)
        
        // Vẽ nút settings (góc trên trái)
        settingsButton.draw(canvas)
        
        // Vẽ các nút
        attackButton.draw(canvas)
        jumpButton.draw(canvas)
        
        if (characterType == "Fighter") {
            shieldButton.draw(canvas)
        } else if (characterType == "Samurai_Archer") {
            bowButton.draw(canvas)
        }
    }
    
    private fun showGameOver() {
        // Hiển thị dialog sau một chút delay để animation chết hoàn thành
        handler.postDelayed({
            gameOverDialog = GameOverDialog(
                context,
                onContinue = {
                    // Continue - Hồi đầy máu và reset game
                    resetGame()
                },
                onRestart = {
                    // Restart sẽ được xử lý trong GameOverDialog
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
                    // Continue - Tiếp tục game
                    isPaused = false
                },
                onRestart = {
                    // Restart - Reset game
                    isPaused = false
                    resetGame()
                },
                onCharacterSelect = {
                    // Về màn hình chọn nhân vật
                    (context as? MainActivity)?.let { activity ->
                        val intent = Intent(context, CharacterSelectionActivity::class.java)
                        activity.startActivity(intent)
                        activity.finish()
                    }
                },
                onMainMenu = {
                    // Về main menu
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
        // Tính thời gian hoàn thành
        val completionTime = System.currentTimeMillis() - gameStartTime
        
        // Tạo victory record
        val victoryRecord = VictoryRecord(
            completionTimeMs = completionTime,
            characterType = characterType,
            enemiesKilled = totalEnemies,
            timestamp = System.currentTimeMillis()
        )
        
        // Lưu vào lịch sử
        victoryManager.saveVictory(victoryRecord)
        
        // Hiển thị dialog sau delay nhỏ
        handler.postDelayed({
            victoryDialog = VictoryDialog(
                context,
                victoryRecord,
                onViewHistory = {
                    // Xem lịch sử
                    (context as? MainActivity)?.let { activity ->
                        val intent = Intent(context, VictoryHistoryActivity::class.java)
                        activity.startActivity(intent)
                    }
                },
                onPlayAgain = {
                    // Chơi lại
                    isVictory = false
                    resetGame()
                },
                onMainMenu = {
                    // Về main menu
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
        
        // Reset nhân vật - hồi đầy máu
        fighter?.reset()
        samuraiArcher?.reset()
        samuraiCommander?.reset()
        
        // Xóa tất cả skeletons cũ và spawn lại
        skeletons.clear()
        spawnSkeletons()
        
        // Xóa tất cả demons cũ và spawn lại
        demons.clear()
        spawnDemons()
        
        // Xóa tất cả medusas cũ và spawn lại
        medusas.clear()
        spawnMedusas()
        
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
