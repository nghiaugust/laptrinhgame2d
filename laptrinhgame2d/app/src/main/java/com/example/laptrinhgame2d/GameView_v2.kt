package com.example.laptrinhgame2d

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.laptrinhgame2d.enemies.*
import com.example.laptrinhgame2d.heroes.*
import com.example.laptrinhgame2d.maps.*
import com.example.laptrinhgame2d.victory.*

/**
 * GameView_v2 - Phiên bản tổ chức lại code
 * 
 * Cấu trúc được chia thành 13 vùng rõ ràng:
 * 1. CONFIGURATION - Cấu hình game (số lượng quái, phạm vi tấn công, camera)
 * 2. GAME STATE - Trạng thái game (game over, pause, victory)
 * 3. HERO SYSTEM - Hệ thống anh hùng (khởi tạo, cập nhật, vẽ nhân vật)
 * 4. ENEMY SYSTEM - Hệ thống quái vật (spawn, update, remove, draw)
 * 5. MAP SYSTEM - Hệ thống bản đồ (3 maps: Grassland, Desert, Volcano)
 * 6. UI SYSTEM - Hệ thống giao diện (joystick, buttons)
 * 7. CAMERA SYSTEM - Hệ thống camera (theo dõi player)
 * 8. LIFECYCLE - Vòng đời game (surfaceCreated, surfaceDestroyed)
 * 9. INPUT HANDLING - Xử lý đầu vào (touch events)
 * 10. UPDATE LOGIC - Logic cập nhật (game loop chính)
 * 11. COLLISION DETECTION - Phát hiện va chạm (melee, arrows, skills)
 * 12. RENDERING - Vẽ màn hình (map, enemies, hero, UI)
 * 13. GAME FLOW - Luồng game (pause, victory, game over, reset)
 */
class GameView_v2(
    context: Context,
    private val characterType: String = "Fighter",  // Loại nhân vật: Fighter, Samurai_Archer, Samurai_Commander
    private val mapType: Int = 1  // Loại map: 1=Grassland, 2=Desert, 3=Volcano
) : SurfaceView(context), SurfaceHolder.Callback {

    // ==================== 1. CẤU HÌNH GAME ====================
    companion object {
        // Cấu hình số lượng quái spawn
        const val NUM_SKELETONS = 3      // Số lượng Skeleton (quái cận chiến yếu)
        const val NUM_DEMONS = 3         // Số lượng Demon (quái cận chiến mạnh)
        const val NUM_MEDUSAS = 3        // Số lượng Medusa (quái tầm xa - ném đá)
        const val NUM_JINNS = 3          // Số lượng Jinn (quái tầm xa - phép thuật)
        const val NUM_SMALL_DRAGONS = 3  // Số lượng Small Dragon (quái tầm xa - cầu lửa)
        const val NUM_DRAGONS = 3        // Số lượng Dragon (quái cận chiến - phun lửa)

        // Phạm vi tấn công (attack ranges)
        const val PLAYER_MELEE_RANGE = 200f      // Phạm vi đánh cận chiến của player
        const val SKELETON_ATTACK_RANGE = 200f   // Phạm vi tấn công của Skeleton
        const val DEMON_ATTACK_RANGE = 250f      // Phạm vi tấn công của Demon (rộng hơn)
        const val ARROW_DAMAGE = 15              // Sát thương của mũi tên

        // Cài đặt camera
        const val CAMERA_DEAD_ZONE_LEFT = 0.35f   // 35% màn hình bên trái - camera không di chuyển
        const val CAMERA_DEAD_ZONE_RIGHT = 0.65f  // 65% màn hình - tạo vùng chết 30% ở giữa
    }

    // ==================== 2. TRẠNG THÁI GAME ====================
    private val gameContext: Context = context
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private var gameThread: GameThread? = null

    // Cờ trạng thái game
    private var isGameOver = false   // Game kết thúc (player chết)
    private var isPaused = false     // Game tạm dừng
    private var isVictory = false    // Chiến thắng (tất cả quái chết)
    private var gameStartTime: Long = 0  // Thời điểm bắt đầu game (để tính thời gian hoàn thành)

    // Các dialog
    private var gameOverDialog: GameOverDialog? = null    // Dialog hiện khi game over
    private var pauseMenuDialog: PauseMenuDialog? = null  // Dialog menu pause
    private var victoryDialog: VictoryDialog? = null      // Dialog chiến thắng

    // Hệ thống victory
    private var victoryManager: VictoryManager = VictoryManager(context)  // Quản lý lịch sử chiến thắng
    private val totalEnemies: Int  // Tổng số quái (để tính % tiêu diệt)
        get() = NUM_SKELETONS + NUM_DEMONS + NUM_MEDUSAS + NUM_JINNS + NUM_SMALL_DRAGONS + NUM_DRAGONS

    // ==================== 3. HỆ THỐNG ANH HÙNG ====================
    private var fighter: Fighter? = null                      // Nhân vật Fighter (đánh cận, có khiên)
    private var samuraiArcher: SamuraiArcher? = null          // Nhân vật Samurai Archer (bắn cung)
    private var samuraiCommander: SamuraiCommander? = null    // Nhân vật Samurai Commander

    init {
        holder.addCallback(this)
        initializeHero()      // Khởi tạo nhân vật
        initializeUI()        // Khởi tạo giao diện
        initializeEnemies()   // Khởi tạo quái vật
    }

    private fun initializeHero() {
        // Tạo nhân vật theo loại được chọn
        when (characterType) {
            "Fighter" -> fighter = Fighter(gameContext, 500f, 400f)
            "Samurai_Archer" -> samuraiArcher = SamuraiArcher(gameContext, 500f, 400f)
            "Samurai_Commander" -> samuraiCommander = SamuraiCommander(gameContext, 500f, 400f)
        }
    }

    private fun getHeroPosition(): HeroState {
        // Lấy thông tin trạng thái hiện tại của nhân vật
        return HeroState(
            x = fighter?.getX() ?: samuraiArcher?.getX() ?: samuraiCommander?.getX() ?: 0f,
            y = fighter?.y ?: samuraiArcher?.y ?: samuraiCommander?.y ?: 0f,
            isDead = fighter?.isDead() ?: samuraiArcher?.isDead() ?: samuraiCommander?.isDead() ?: false,
            facingRight = fighter?.getFacingRight() ?: samuraiArcher?.getFacingRight() 
                ?: samuraiCommander?.getFacingRight() ?: true,
            canDealDamage = fighter?.canDealDamage() ?: samuraiArcher?.canDealDamage() 
                ?: samuraiCommander?.canDealDamage() ?: false,
            attackDamage = fighter?.getAttackDamage() ?: samuraiArcher?.getAttackDamage() 
                ?: samuraiCommander?.getAttackDamage() ?: 0
        )
    }

    private fun updateHero(joystickX: Float, joystickY: Float) {
        // Cập nhật nhân vật mỗi frame
        fighter?.update(joystickX, joystickY)
        samuraiArcher?.update(joystickX, joystickY)
        samuraiCommander?.update(joystickX, joystickY)
    }

    private fun resetHero() {
        // Reset nhân vật về trạng thái ban đầu (hồi đầy máu)
        fighter?.reset()
        samuraiArcher?.reset()
        samuraiCommander?.reset()
    }

    private fun positionHero(groundY: Float) {
        // Đặt vị trí nhân vật trên mặt đất
        val heroY = groundY - 200f
        when (characterType) {
            "Fighter" -> {
                fighter?.y = heroY
                fighter?.setGroundY(heroY)
            }
            "Samurai_Archer" -> {
                samuraiArcher?.y = heroY
                samuraiArcher?.setGroundY(heroY)
            }
            "Samurai_Commander" -> {
                samuraiCommander?.y = heroY
                samuraiCommander?.setGroundY(heroY)
            }
        }
    }

    private fun drawHero(canvas: Canvas) {
        // Vẽ nhân vật
        fighter?.draw(canvas)
        samuraiArcher?.draw(canvas)
        samuraiCommander?.draw(canvas)
    }

    private fun drawHeroUI(canvas: Canvas) {
        // Vẽ UI của nhân vật (thanh máu, armor)
        fighter?.drawUI(canvas)
        samuraiArcher?.drawUI(canvas)
        samuraiCommander?.drawUI(canvas)
    }

    // Các hành động chiến đấu của nhân vật
    private fun heroTakeDamage(damage: Int) {
        // Nhân vật nhận sát thương
        fighter?.takeDamage(damage)
        samuraiArcher?.takeDamage(damage)
        samuraiCommander?.takeDamage(damage)
    }

    private fun heroMarkDamageDealt() {
        // Đánh dấu nhân vật đã gây damage (tránh deal damage nhiều lần)
        fighter?.markDamageDealt()
        samuraiArcher?.markDamageDealt()
        samuraiCommander?.markDamageDealt()
    }

    private fun heroIsCollidingWith(x: Float, y: Float, range: Float): Boolean {
        // Kiểm tra nhân vật có va chạm với vị trí (x, y) trong phạm vi range không
        return fighter?.isCollidingWith(x, y, range)
            ?: samuraiArcher?.isCollidingWith(x, y, range)
            ?: samuraiCommander?.isCollidingWith(x, y, range)
            ?: false
    }

    private fun heroAttack() {
        // Nhân vật thực hiện tấn công
        fighter?.attack()
        samuraiArcher?.attack()
        samuraiCommander?.attack()
    }

    private fun heroJump() {
        // Nhân vật nhảy
        fighter?.jump()
        samuraiArcher?.jump()
        samuraiCommander?.jump()
    }

    // Data class lưu trạng thái nhân vật (để tránh gọi nhiều lần)
    private data class HeroState(
        val x: Float,              // Vị trí X
        val y: Float,              // Vị trí Y
        val isDead: Boolean,       // Đã chết chưa
        val facingRight: Boolean,  // Đang quay mặt sang phải
        val canDealDamage: Boolean, // Có thể gây damage không (đang ở frame tấn công)
        val attackDamage: Int      // Sát thương tấn công
    )

    // ==================== 4. HỆ THỐNG QUÁI VẬT ====================
    // Danh sách các loại quái
    private val skeletons = mutableListOf<Skeleton>()          // Quái xương (cận chiến yếu)
    private val demons = mutableListOf<Demon>()                // Quái ác ma (cận chiến mạnh)
    private val medusas = mutableListOf<Medusa>()              // Quái Medusa (tầm xa - ném đá)
    private val jinns = mutableListOf<Jinn>()                  // Quái Jinn (tầm xa - phép thuật)
    private val smallDragons = mutableListOf<SmallDragon>()    // Rồng nhỏ (tầm xa - cầu lửa)
    private val dragons = mutableListOf<Dragon>()              // Rồng lớn (cận chiến - phun lửa)

    private fun initializeEnemies() {
        // Khởi tạo tất cả quái vật
        spawnSkeletons()
        spawnDemons()
        spawnMedusas()
        spawnJinns()
        spawnSmallDragons()
        spawnDragons()
    }

    private fun spawnSkeletons() {
        // Spawn Skeleton - cách đều 400px, random thêm 0-200px
        for (i in 0 until NUM_SKELETONS) {
            val x = 800f + (i * 400f) + (Math.random() * 200f).toFloat()
            val y = 400f + (Math.random() * 400f).toFloat()
            skeletons.add(Skeleton(gameContext, x, y))
        }
    }

    private fun spawnDemons() {
        // Spawn Demon - spawn xa hơn Skeleton một chút
        for (i in 0 until NUM_DEMONS) {
            val x = 900f + (i * 400f) + (Math.random() * 300f).toFloat()
            val y = 400f + (Math.random() * 400f).toFloat()
            demons.add(Demon(gameContext, x, y))
        }
    }

    private fun spawnMedusas() {
        // Spawn Medusa - quái tầm xa, spawn xa hơn
        for (i in 0 until NUM_MEDUSAS) {
            val x = 1000f + (i * 400f) + (Math.random() * 300f).toFloat()
            val y = 400f + (Math.random() * 400f).toFloat()
            medusas.add(Medusa(gameContext, x, y))
        }
    }

    private fun spawnJinns() {
        // Spawn Jinn - quái phép thuật tầm xa
        for (i in 0 until NUM_JINNS) {
            val x = 1100f + (i * 400f) + (Math.random() * 400f).toFloat()
            val y = 400f + (Math.random() * 400f).toFloat()
            jinns.add(Jinn(gameContext, x, y))
        }
    }

    private fun spawnSmallDragons() {
        // Spawn Small Dragon - rồng nhỏ bắn cầu lửa
        for (i in 0 until NUM_SMALL_DRAGONS) {
            val x = 1200f + (i * 400f) + (Math.random() * 300f).toFloat()
            val y = 400f + (Math.random() * 400f).toFloat()
            smallDragons.add(SmallDragon(gameContext, x, y))
        }
    }

    private fun spawnDragons() {
        // Spawn Dragon - rồng lớn phun lửa cận chiến
        for (i in 0 until NUM_DRAGONS) {
            val x = 1300f + (i * 500f) + (Math.random() * 200f).toFloat()
            val y = 400f + (Math.random() * 400f).toFloat()
            dragons.add(Dragon(gameContext, x, y))
        }
    }

    private fun positionEnemiesOnGround(groundY: Float) {
        // Đặt tất cả quái vật đứng trên mặt đất
        skeletons.forEach { it.y = groundY - 200f }
        demons.forEach { it.y = groundY - 180f }     // Demon thấp hơn một chút
        medusas.forEach { it.y = groundY - 200f }
        jinns.forEach { it.y = groundY - 200f }
        smallDragons.forEach { it.y = groundY - 200f }
        dragons.forEach { it.y = groundY - 200f }
    }

    private fun updateAllEnemies(heroState: HeroState) {
        // Cập nhật tất cả quái vật (AI, tấn công, di chuyển)
        updateSkeletons(heroState)
        updateDemons(heroState)
        updateMedusas(heroState)
        updateJinns(heroState)
        updateSmallDragons(heroState)
        updateDragons(heroState)
    }

    private fun updateSkeletons(heroState: HeroState) {
        // Cập nhật Skeleton - kiểm tra tấn công player
        for (skeleton in skeletons) {
            skeleton.update(heroState.x, heroState.y)
            if (!skeleton.isDead() && skeleton.canDealDamage()) {
                if (heroIsCollidingWith(skeleton.getX(), skeleton.y, SKELETON_ATTACK_RANGE)) {
                    heroTakeDamage(skeleton.getAttackDamage())
                    skeleton.markDamageDealt()
                }
            }
        }
    }

    private fun updateDemons(heroState: HeroState) {
        // Cập nhật Demon - kiểm tra tấn công player
        for (demon in demons) {
            demon.update(heroState.x, heroState.y)
            if (!demon.isDead() && demon.canDealDamage()) {
                if (heroIsCollidingWith(demon.getX(), demon.y, DEMON_ATTACK_RANGE)) {
                    heroTakeDamage(demon.getAttackDamage())
                    demon.markDamageDealt()
                }
            }
        }
    }

    private fun updateMedusas(heroState: HeroState) {
        // Cập nhật Medusa - kiểm tra đạn đá va chạm với player
        for (medusa in medusas) {
            medusa.update(heroState.x, heroState.y, heroState.isDead)
            if (!medusa.isDead()) {
                for (projectile in medusa.projectiles) {
                    if (projectile.isCollidingWith(heroState.x, heroState.y, 100f)) {
                        if (!projectile.canDealDamage()) {
                            projectile.startExploding()  // Trigger nổ khi chạm player
                        }
                        if (projectile.canDealDamage()) {
                            heroTakeDamage(projectile.getDamage())
                            projectile.markDamageDealt()
                        }
                    }
                }
            }
        }
    }

    private fun updateJinns(heroState: HeroState) {
        // Cập nhật Jinn - kiểm tra phép thuật va chạm với player
        for (jinn in jinns) {
            jinn.update(heroState.x, heroState.y)
            if (!jinn.isDead()) {
                for (projectile in jinn.projectiles) {
                    if (projectile.canDealDamage() && projectile.isCollidingWith(heroState.x, heroState.y, 100f)) {
                        heroTakeDamage(projectile.getDamage())
                        projectile.markDamageDealt()
                    }
                }
            }
        }
    }

    private fun updateSmallDragons(heroState: HeroState) {
        // Cập nhật Small Dragon - kiểm tra cầu lửa va chạm với player
        for (dragon in smallDragons) {
            dragon.update(heroState.x, heroState.y)
            if (!dragon.isDead()) {
                for (projectile in dragon.projectiles) {
                    if (projectile.canDealDamage() && projectile.isCollidingWith(heroState.x, heroState.y, 110f)) {
                        heroTakeDamage(projectile.getDamage())
                        projectile.markDamageDealt()
                    }
                }
            }
        }
    }

    private fun updateDragons(heroState: HeroState) {
        // Cập nhật Dragon - kiểm tra lửa phun va chạm với player
        for (dragon in dragons) {
            dragon.update(heroState.x, heroState.y)
            if (!dragon.isDead()) {
                for (fire in dragon.fireProjectiles) {
                    if (fire.canDealDamage() && fire.isCollidingWith(heroState.x, heroState.y, 90f)) {
                        heroTakeDamage(fire.getDamage())
                        fire.markDamageDealt()
                    }
                }
            }
        }
    }

    private fun removeDeadEnemies() {
        // Xóa quái đã chết và hết animation chết
        skeletons.removeAll { it.shouldBeRemoved() }
        demons.removeAll { it.shouldBeRemoved() }
        medusas.removeAll { it.shouldRemove() }
        jinns.removeAll { it.shouldBeRemoved() }
        smallDragons.removeAll { it.shouldBeRemoved() }
        dragons.removeAll { it.shouldBeRemoved() }
    }

    private fun checkAllEnemiesDead(): Boolean {
        // Kiểm tra tất cả quái đã chết chưa (để show victory)
        val allSkeletonsDead = skeletons.all { it.isDead() }
        val allDemonsDead = demons.all { it.isDead() }
        val allMedusasDead = medusas.isEmpty() || medusas.all { it.isDead() }
        val allJinnsDead = jinns.isEmpty() || jinns.all { it.isDead() }
        val allSmallDragonsDead = smallDragons.isEmpty() || smallDragons.all { it.isDead() }
        val allDragonsDead = dragons.isEmpty() || dragons.all { it.isDead() }

        return allSkeletonsDead && allDemonsDead && allMedusasDead && 
               allJinnsDead && allSmallDragonsDead && allDragonsDead
    }

    private fun resetAllEnemies() {
        // Reset tất cả quái (xóa và spawn lại)
        skeletons.clear()
        demons.clear()
        medusas.clear()
        jinns.clear()
        smallDragons.clear()
        dragons.clear()

        initializeEnemies()
    }

    private fun drawAllEnemies(canvas: Canvas) {
        // Vẽ tất cả quái vật
        skeletons.forEach { it.draw(canvas) }
        demons.forEach { it.draw(canvas) }
        medusas.forEach { it.draw(canvas) }
        jinns.forEach { it.draw(canvas) }
        smallDragons.forEach { it.draw(canvas) }
        dragons.forEach { it.draw(canvas) }
    }

    // ==================== 5. HỆ THỐNG BẢN ĐỒ ====================
    private var grasslandMap: GrasslandMap? = null  // Map 1: Cỏ xanh (dễ)
    private var desertMap: DesertMap? = null        // Map 2: Sa mạc (trung bình)
    private var volcanoMap: VolcanoMap? = null      // Map 3: Núi lửa (khó)

    private fun initializeMaps(screenWidth: Int, screenHeight: Int) {
        // Khởi tạo cả 3 maps (chỉ 1 map được dùng tùy theo mapType)
        grasslandMap = GrasslandMap(gameContext, screenWidth, screenHeight)
        desertMap = DesertMap(gameContext, screenWidth, screenHeight)
        volcanoMap = VolcanoMap(gameContext, screenWidth, screenHeight)
    }

    private fun getCurrentGroundY(defaultHeight: Float): Float {
        // Lấy groundY từ map hiện tại
        return when (mapType) {
            2 -> desertMap?.groundY ?: defaultHeight
            3 -> volcanoMap?.groundY ?: defaultHeight
            else -> grasslandMap?.groundY ?: defaultHeight
        }
    }

    private fun updateCurrentMap(cameraX: Float, cameraY: Float) {
        // Cập nhật map hiện tại (parallax scrolling)
        when (mapType) {
            1 -> grasslandMap?.update(cameraX, cameraY)
            2 -> desertMap?.update(cameraX, cameraY)
            3 -> volcanoMap?.update(cameraX, cameraY)
        }
    }

    private fun drawCurrentMap(canvas: Canvas, cameraX: Float, cameraY: Float) {
        // Vẽ map hiện tại (background layers)
        when (mapType) {
            1 -> grasslandMap?.draw(canvas, cameraX, cameraY)
            2 -> desertMap?.draw(canvas, cameraX, cameraY)
            3 -> volcanoMap?.draw(canvas, cameraX, cameraY)
        }
    }

    // ==================== 6. HỆ THỐNG GIAO DIỆN ====================
    // Các nút điều khiển
    private val joystick: Joystick = Joystick(200f, 0f, 120f, 100f)  // Joystick trái/phải
    private val attackButton: GameButton = GameButton(0f, 0f, 90f, "Attack", Color.rgb(255, 100, 100))  // Nút đánh (đỏ)
    private val shieldButton: GameButton = GameButton(0f, 0f, 90f, "Shield", Color.rgb(100, 100, 255))  // Nút khiên (xanh)
    private val jumpButton: GameButton = GameButton(0f, 0f, 90f, "Jump", Color.rgb(100, 255, 100))      // Nút nhảy (xanh lá)
    private val bowButton: GameButton = GameButton(0f, 0f, 90f, "Bow", Color.rgb(255, 165, 0))          // Nút cung (cam)
    private val settingsButton: GameButton = GameButton(0f, 0f, 70f, "⚙", Color.rgb(150, 150, 150))    // Nút settings (xám)

    private fun initializeUI() {
        // UI initialization is done in init block
    }

    private fun positionUIControls(screenWidth: Float, screenHeight: Float) {
        // Đặt vị trí các nút điều khiển trên màn hình
        joystick.centerY = screenHeight - 200f  // Joystick ở dưới bên trái
        settingsButton.x = 300f
        settingsButton.y = 35f  // Settings ở góc trên trái

        if (characterType == "Fighter") {
            // Layout cho Fighter: Attack, Shield, Jump (tam giác)
            attackButton.x = screenWidth - 230f
            attackButton.y = screenHeight - 200f
            shieldButton.x = screenWidth - 120f
            shieldButton.y = screenHeight - 200f
            jumpButton.x = screenWidth - 175f
            jumpButton.y = screenHeight - 320f
        } else {
            // Layout cho Archer/Commander: Attack, Bow, Jump (tam giác)
            attackButton.x = screenWidth - 230f
            attackButton.y = screenHeight - 200f
            bowButton.x = screenWidth - 120f
            bowButton.y = screenHeight - 200f
            jumpButton.x = screenWidth - 175f
            jumpButton.y = screenHeight - 320f
        }
    }

    private fun drawUIControls(canvas: Canvas) {
        // Vẽ các nút điều khiển
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

    // ==================== 7. HỆ THỐNG CAMERA ====================
    private var cameraX = 0f  // Vị trí camera theo trục X
    private var cameraY = 0f  // Vị trí camera theo trục Y (luôn là 0 - không scroll Y)

    private fun updateCamera(heroX: Float) {
        // Cập nhật camera để theo dõi nhân vật (chỉ scroll X)
        // Camera có "dead zone" ở giữa màn hình - chỉ di chuyển khi player ra khỏi vùng này
        val cameraDeadZoneLeft = width * CAMERA_DEAD_ZONE_LEFT
        val cameraDeadZoneRight = width * CAMERA_DEAD_ZONE_RIGHT
        val heroScreenX = heroX - cameraX

        if (heroScreenX < cameraDeadZoneLeft) {
            // Player quá gần bên trái - di chuyển camera
            cameraX = heroX - cameraDeadZoneLeft
        } else if (heroScreenX > cameraDeadZoneRight) {
            // Player quá gần bên phải - di chuyển camera
            cameraX = heroX - cameraDeadZoneRight
        }

        cameraY = 0f  // Không scroll theo Y
    }

    // ==================== 8. VÒNG ĐỜI GAME ====================
    override fun surfaceCreated(holder: SurfaceHolder) {
        // Được gọi khi surface được tạo (khởi động game)
        val screenWidth = width.toFloat()
        val screenHeight = height.toFloat()

        // Khởi tạo maps
        initializeMaps(width, height)

        // Đặt vị trí các thành phần game
        val groundY = getCurrentGroundY(screenHeight * 0.75f)
        positionHero(groundY)
        positionEnemiesOnGround(groundY)
        positionUIControls(screenWidth, screenHeight)

        // Khởi động game thread
        gameThread = GameThread(holder, this)
        gameThread?.running = true
        gameThread?.start()

        // Bắt đầu đếm thời gian
        gameStartTime = System.currentTimeMillis()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // Được gọi khi surface thay đổi kích thước (không cần xử lý gì)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // Được gọi khi surface bị hủy (dừng game thread)
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

    // ==================== 9. INPUT HANDLING ====================
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> handleTouchDown(event)
            MotionEvent.ACTION_MOVE -> {} // Not needed for button-based joystick
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> handleTouchUp(event)
        }
        return true
    }

    private fun handleTouchDown(event: MotionEvent) {
        val index = event.actionIndex
        val x = event.getX(index)
        val y = event.getY(index)
        val pointerId = event.getPointerId(index)

        // Joystick (left side)
        if (x < width / 2) {
            joystick.onTouchDown(x, y, pointerId)
        }

        // Settings button
        if (settingsButton.isPressed(x, y)) {
            settingsButton.onTouch(pointerId)
            showPauseMenu()
            return
        }

        // Attack button
        if (attackButton.isPressed(x, y)) {
            attackButton.onTouch(pointerId)
            heroAttack()
        }

        // Bow button (Samurai_Archer only)
        if (characterType == "Samurai_Archer" && bowButton.isPressed(x, y)) {
            bowButton.onTouch(pointerId)
            samuraiArcher?.let { archer ->
                if (archer.getCombatMode() == SamuraiArcher.CombatMode.MELEE) {
                    archer.switchCombatMode()
                }
                archer.attack()
            }
        }

        // Shield button (Fighter only)
        if (characterType == "Fighter" && shieldButton.isPressed(x, y)) {
            shieldButton.onTouch(pointerId)
            fighter?.activateShield()
        }

        // Jump button
        if (jumpButton.isPressed(x, y)) {
            jumpButton.onTouch(pointerId)
            heroJump()
        }
    }

    private fun handleTouchUp(event: MotionEvent) {
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

    // ==================== 10. UPDATE LOGIC ====================
    fun update() {
        // Skip update if game is not running
        if (isGameOver || isPaused || isVictory) return

        // Update map
        updateCurrentMap(cameraX, cameraY)

        // Update hero
        val joystickX = joystick.getX()
        updateHero(joystickX, 0f)

        // Get hero state
        val heroState = getHeroPosition()

        // Check hero death
        if (heroState.isDead && !isGameOver) {
            isGameOver = true
            showGameOver()
            return
        }

        // Update camera
        updateCamera(heroState.x)

        // Keep enemies on ground
        val groundY = getCurrentGroundY(height * 0.75f)
        positionEnemiesOnGround(groundY)

        // Update enemies
        updateAllEnemies(heroState)

        // Remove dead enemies
        removeDeadEnemies()

        // Check victory
        if (!isVictory && checkAllEnemiesDead()) {
            isVictory = true
            showVictory()
            return
        }

        // Handle collisions
        handleHeroMeleeAttacks(heroState)
        handleHeroRangedAttacks()
    }

    // ==================== 11. COLLISION DETECTION ====================
    private fun handleHeroMeleeAttacks(heroState: HeroState) {
        if (!heroState.canDealDamage) return

        val attackRange = PLAYER_MELEE_RANGE
        val damage = heroState.attackDamage

        // Attack skeletons
        for (skeleton in skeletons) {
            if (!skeleton.isDead() && skeleton.isCollidingWith(heroState.x, heroState.y, attackRange)) {
                val dx = skeleton.getX() - heroState.x
                val isFacing = (dx > 0 && heroState.facingRight) || (dx < 0 && !heroState.facingRight)
                if (isFacing) {
                    skeleton.takeDamage(damage)
                    heroMarkDamageDealt()
                }
            }
        }

        // Attack demons
        for (demon in demons) {
            if (!demon.isDead() && demon.isCollidingWith(heroState.x, heroState.y, attackRange)) {
                val dx = demon.getX() - heroState.x
                val isFacing = (dx > 0 && heroState.facingRight) || (dx < 0 && !heroState.facingRight)
                if (isFacing) {
                    demon.takeDamage(damage)
                    heroMarkDamageDealt()
                    break
                }
            }
        }

        // Attack medusas
        for (medusa in medusas) {
            if (!medusa.isDead() && medusa.isCollidingWith(heroState.x, heroState.y, attackRange)) {
                val dx = medusa.getX() - heroState.x
                val isFacing = (dx > 0 && heroState.facingRight) || (dx < 0 && !heroState.facingRight)
                if (isFacing) {
                    medusa.takeDamage(damage)
                    heroMarkDamageDealt()
                    break
                }
            }
        }

        // Attack jinns
        for (jinn in jinns) {
            if (!jinn.isDead() && jinn.isCollidingWith(heroState.x, heroState.y, attackRange)) {
                val dx = jinn.getX() - heroState.x
                val isFacing = (dx > 0 && heroState.facingRight) || (dx < 0 && !heroState.facingRight)
                if (isFacing) {
                    jinn.takeDamage(damage)
                    heroMarkDamageDealt()
                    break
                }
            }
        }

        // Attack small dragons
        for (dragon in smallDragons) {
            if (!dragon.isDead() && dragon.isCollidingWith(heroState.x, heroState.y, attackRange)) {
                val dx = dragon.getX() - heroState.x
                val isFacing = (dx > 0 && heroState.facingRight) || (dx < 0 && !heroState.facingRight)
                if (isFacing) {
                    dragon.takeDamage(damage)
                    heroMarkDamageDealt()
                    break
                }
            }
        }

        // Attack dragons
        for (dragon in dragons) {
            if (!dragon.isDead() && dragon.isCollidingWith(heroState.x, heroState.y, attackRange)) {
                val dx = dragon.getX() - heroState.x
                val isFacing = (dx > 0 && heroState.facingRight) || (dx < 0 && !heroState.facingRight)
                if (isFacing) {
                    dragon.takeDamage(damage)
                    heroMarkDamageDealt()
                    break
                }
            }
        }
    }

    private fun handleHeroRangedAttacks() {
        handleArrowAttacks()
        handleSkillAttacks()
    }

    private fun handleArrowAttacks() {
        samuraiArcher?.getArrows()?.forEach { arrow ->
            if (!arrow.isActive()) return@forEach

            checkArrowHitEnemy(arrow, skeletons, 80f)
            checkArrowHitEnemy(arrow, demons, 80f)
            checkArrowHitEnemy(arrow, medusas, 80f)
            checkArrowHitEnemy(arrow, jinns, 80f)
            checkArrowHitEnemy(arrow, smallDragons, 120f)
            checkArrowHitEnemy(arrow, dragons, 120f)
        }
    }

    private fun <T : Any> checkArrowHitEnemy(arrow: Arrow, enemies: List<T>, range: Float) {
        for (enemy in enemies) {
            if (isEnemyDead(enemy) || !arrow.isActive()) continue

            val enemyX = getEnemyX(enemy)
            val enemyY = getEnemyY(enemy)

            if (arrow.checkCollision(enemyX, enemyY, range)) {
                damageEnemy(enemy, ARROW_DAMAGE)
                arrow.deactivate()
                break
            }
        }
    }

    private fun handleSkillAttacks() {
        samuraiArcher?.getSkillProjectiles()?.forEach { skill ->
            if (!skill.isActive()) return@forEach

            checkSkillHitEnemy(skill, skeletons, 80f)
            checkSkillHitEnemy(skill, demons, 80f)
            checkSkillHitEnemy(skill, medusas, 80f)
            checkSkillHitEnemy(skill, jinns, 80f)
            checkSkillHitEnemy(skill, smallDragons, 120f)
            checkSkillHitEnemy(skill, dragons, 120f)
        }
    }

    private fun <T : Any> checkSkillHitEnemy(skill: SkillProjectile, enemies: List<T>, range: Float) {
        for (enemy in enemies) {
            if (isEnemyDead(enemy) || !skill.isActive()) continue

            val enemyX = getEnemyX(enemy)
            val enemyY = getEnemyY(enemy)

            if (skill.checkCollision(enemyX, enemyY, range)) {
                damageEnemy(enemy, skill.getDamage())
                skill.deactivate()
                break
            }
        }
    }

    // Helper methods for generic enemy operations
    private fun <T : Any> isEnemyDead(enemy: T): Boolean {
        return when (enemy) {
            is Skeleton -> enemy.isDead()
            is Demon -> enemy.isDead()
            is Medusa -> enemy.isDead()
            is Jinn -> enemy.isDead()
            is SmallDragon -> enemy.isDead()
            is Dragon -> enemy.isDead()
            else -> false
        }
    }

    private fun <T : Any> getEnemyX(enemy: T): Float {
        return when (enemy) {
            is Skeleton -> enemy.getX()
            is Demon -> enemy.getX()
            is Medusa -> enemy.getX()
            is Jinn -> enemy.getX()
            is SmallDragon -> enemy.getX()
            is Dragon -> enemy.getX()
            else -> 0f
        }
    }

    private fun <T : Any> getEnemyY(enemy: T): Float {
        return when (enemy) {
            is Skeleton -> enemy.y
            is Demon -> enemy.y
            is Medusa -> enemy.y
            is Jinn -> enemy.y
            is SmallDragon -> enemy.y
            is Dragon -> enemy.y
            else -> 0f
        }
    }

    private fun <T : Any> damageEnemy(enemy: T, damage: Int) {
        when (enemy) {
            is Skeleton -> enemy.takeDamage(damage)
            is Demon -> enemy.takeDamage(damage)
            is Medusa -> enemy.takeDamage(damage)
            is Jinn -> enemy.takeDamage(damage)
            is SmallDragon -> enemy.takeDamage(damage)
            is Dragon -> enemy.takeDamage(damage)
        }
    }

    // ==================== 12. RENDERING ====================
    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        // Draw map background (not affected by camera)
        drawCurrentMap(canvas, cameraX, cameraY)

        // Apply camera transform for world objects
        canvas.save()
        canvas.translate(-cameraX, -cameraY)

        // Draw enemies
        drawAllEnemies(canvas)

        // Draw hero
        drawHero(canvas)

        canvas.restore()

        // Draw UI (not affected by camera)
        drawHeroUI(canvas)
        drawUIControls(canvas)
    }

    // ==================== 13. GAME FLOW ====================
    private fun showGameOver() {
        handler.postDelayed({
            gameOverDialog = GameOverDialog(
                context,
                onContinue = { resetGame() },
                onRestart = { /* Handled in GameOverDialog */ }
            )
            gameOverDialog?.show()
        }, 1000)
    }

    private fun showPauseMenu() {
        isPaused = true
        handler.post {
            pauseMenuDialog = PauseMenuDialog(
                context,
                onContinue = { isPaused = false },
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

        resetHero()
        resetAllEnemies()

        gameStartTime = System.currentTimeMillis()
    }

    // ==================== GAME THREAD ====================
    // Thread riêng để chạy vòng lặp game (60 FPS)
    private class GameThread(private val surfaceHolder: SurfaceHolder, private val gameView: GameView_v2) : Thread() {
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
}
