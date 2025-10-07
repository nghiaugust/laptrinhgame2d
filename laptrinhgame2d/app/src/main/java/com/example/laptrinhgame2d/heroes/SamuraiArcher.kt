package com.example.laptrinhgame2d.heroes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import com.example.laptrinhgame2d.DamageText
import com.example.laptrinhgame2d.heroes.SkillProjectile
import java.io.IOException
import kotlin.math.abs
import kotlin.math.sqrt

class SamuraiArcher(private val context: Context, private var x: Float, var y: Float) {

    private val idleFrames = mutableListOf<Bitmap>()
    private val walkFrames = mutableListOf<Bitmap>()
    private val runFrames = mutableListOf<Bitmap>()
    private val jumpFrames = mutableListOf<Bitmap>()

    // Melee Attack (cận chiến)
    private val meleeAttackType1Frames = mutableListOf<Bitmap>()
    private val meleeAttackType2Frames = mutableListOf<Bitmap>()
    private val meleeAttackType3Frames = mutableListOf<Bitmap>()
    private val meleeSkillType1Frames = mutableListOf<Bitmap>()
    private val meleeSkillType2Frames = mutableListOf<Bitmap>()
    private val meleeSkillType3Frames = mutableListOf<Bitmap>()

    // Ranged Attack (bắn cung)
    private val shotFrames = mutableListOf<Bitmap>()
    private var arrowBitmap: Bitmap? = null
    private val arrows = mutableListOf<Arrow>()

    // Charged Attack (giữ nút attack)
    private val chargedAttackFrames = mutableListOf<Bitmap>() // Tổng hợp cả 3 bộ attack
    private val skillProjectiles = mutableListOf<SkillProjectile>()
    private var isChargingAttack = false
    private var chargeTimer = 0
    private val chargeTimeRequired = 10 // thời gian giữ để attack
    private var hasLaunchedSkills = false

    private val hurtFrames = mutableListOf<Bitmap>()
    private val deadFrames = mutableListOf<Bitmap>()

    private var currentFrames = mutableListOf<Bitmap>()
    private var currentFrame = 0
    private var frameCounter = 0
    private val frameDelay = 2 // Tăng tốc animation (giảm từ 5 xuống 2)

    private var state = State.IDLE
    private var facingRight = true
    private val speed = 13f

    // Jump physics
    private var velocityY = 0f
    private val jumpPower = -25f
    private val gravity = 1.2f
    private var isJumping = false
    private var groundY = 0f

    private var attackComboCounter = 0
    private var attackComboTimer = 0
    private val attackComboTimeout = 30

    private var isAnimationLocked = false
    private var isDead = false
    private var hasDealtDamageThisAttack = false
    private var hasShotArrow = false

    // Combat mode
    private var combatMode = CombatMode.MELEE // Mặc định là cận chiến

    // Health and Armor
    private var health = 100
    private val maxHealth = 100
    private var armor = 80
    private val maxArmor = 80
    private var armorRegenCounter = 0
    private val armorRegenDelay = 120
    private val armorRegenAmount = 10

    private val damageTexts = mutableListOf<DamageText>()

    enum class State {
        IDLE, WALK, RUN, JUMP,
        MELEE_ATTACK1, MELEE_ATTACK2, MELEE_ATTACK3,
        CHARGED_ATTACK, // Tấn công nạp đầy
        RANGED_SHOT,
        HURT, DEAD
    }

    enum class CombatMode {
        MELEE, RANGED
    }

    init {
        loadFrames()
        currentFrames = idleFrames
    }

    private fun loadFrames() {
        try {
            // Load idle frames
            for (i in 1..8) {
                val bitmap = loadBitmap("heroes/Samurai_Archer/idle/Idle$i.png")
                idleFrames.add(bitmap)
            }

            // Load walk frames
            for (i in 1..7) {
                val bitmap = loadBitmap("heroes/Samurai_Archer/walk/Walk$i.png")
                walkFrames.add(bitmap)
            }

            // Load run frames
            val runFiles = context.assets.list("heroes/Samurai_Archer/run") ?: emptyArray()
            for (file in runFiles.sorted()) {
                if (file.endsWith(".png") && file != "Run.png") {
                    val bitmap = loadBitmap("heroes/Samurai_Archer/run/$file")
                    runFrames.add(bitmap)
                }
            }

            // Load jump frames (nếu có)
            val jumpFiles = context.assets.list("heroes/Samurai_Archer/jump") ?: emptyArray()
            for (file in jumpFiles.sorted()) {
                if (file.endsWith(".png") && file != "Jump.png") {
                    val bitmap = loadBitmap("heroes/Samurai_Archer/jump/$file")
                    jumpFrames.add(bitmap)
                }
            }

            // Load melee attack frames
            val attackType1Files = context.assets.list("heroes/Samurai_Archer/attack/type1") ?: emptyArray()
            for (file in attackType1Files.sorted()) {
                if (file.endsWith(".png")) {
                    val bitmap = loadBitmap("heroes/Samurai_Archer/attack/type1/$file")
                    meleeAttackType1Frames.add(bitmap)
                }
            }

            val attackType2Files = context.assets.list("heroes/Samurai_Archer/attack/type2") ?: emptyArray()
            for (file in attackType2Files.sorted()) {
                if (file.endsWith(".png")) {
                    val bitmap = loadBitmap("heroes/Samurai_Archer/attack/type2/$file")
                    meleeAttackType2Frames.add(bitmap)
                }
            }

            val attackType3Files = context.assets.list("heroes/Samurai_Archer/attack/type3") ?: emptyArray()
            for (file in attackType3Files.sorted()) {
                if (file.endsWith(".png")) {
                    val bitmap = loadBitmap("heroes/Samurai_Archer/attack/type3/$file")
                    meleeAttackType3Frames.add(bitmap)
                }
            }

            // Load melee skill frames (hiệu ứng kiếm chém) - 496x496 -> scale to 3x = 1488x1488
            val skillType1Files = context.assets.list("heroes/Samurai_Archer/skills/type1") ?: emptyArray()
            for (file in skillType1Files.sorted()) {
                if (file.endsWith(".png")) {
                    val bitmap = loadSkillBitmap("heroes/Samurai_Archer/skills/type1/$file")
                    meleeSkillType1Frames.add(bitmap)
                }
            }

            val skillType2Files = context.assets.list("heroes/Samurai_Archer/skills/type2") ?: emptyArray()
            for (file in skillType2Files.sorted()) {
                if (file.endsWith(".png")) {
                    val bitmap = loadSkillBitmap("heroes/Samurai_Archer/skills/type2/$file")
                    meleeSkillType2Frames.add(bitmap)
                }
            }

            val skillType3Files = context.assets.list("heroes/Samurai_Archer/skills/type3") ?: emptyArray()
            for (file in skillType3Files.sorted()) {
                if (file.endsWith(".png")) {
                    val bitmap = loadSkillBitmap("heroes/Samurai_Archer/skills/type3/$file")
                    meleeSkillType3Frames.add(bitmap)
                }
            }

            // Load shot frames (bắn cung)
            for (i in 1..13) {
                val bitmap = loadBitmap("heroes/Samurai_Archer/shot/Shot$i.png")
                shotFrames.add(bitmap)
            }

            // Load charged attack frames (tất cả frames từ 3 bộ type)
            try {
                // Thêm tất cả frames của type1
                chargedAttackFrames.addAll(meleeAttackType1Frames)
                // Thêm tất cả frames của type2
                chargedAttackFrames.addAll(meleeAttackType2Frames)
                // Thêm tất cả frames của type3
                chargedAttackFrames.addAll(meleeAttackType3Frames)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Load arrow bitmap
            arrowBitmap = loadBitmap("heroes/Samurai_Archer/arrow/Arrow.png")

            // Load hurt frames
            val hurtFiles = context.assets.list("heroes/Samurai_Archer/hurt") ?: emptyArray()
            for (file in hurtFiles.sorted()) {
                if (file.endsWith(".png") && file != "Hurt.png") {
                    val bitmap = loadBitmap("heroes/Samurai_Archer/hurt/$file")
                    hurtFrames.add(bitmap)
                }
            }

            // Load dead frames
            val deadFiles = context.assets.list("heroes/Samurai_Archer/dead") ?: emptyArray()
            for (file in deadFiles.sorted()) {
                if (file.endsWith(".png") && file != "Dead.png") {
                    val bitmap = loadBitmap("heroes/Samurai_Archer/dead/$file")
                    deadFrames.add(bitmap)
                }
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun loadBitmap(path: String): Bitmap {
        val inputStream = context.assets.open(path)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        return Bitmap.createScaledBitmap(bitmap, bitmap.width * 3, bitmap.height * 3, false)
    }

    private fun loadSkillBitmap(path: String): Bitmap {
        val inputStream = context.assets.open(path)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        // Skill frames 496x496 -> scale xuống để phù hợp với nhân vật 128x128
        // 496 / 128 ≈ 3.875, nên scale skill về 128*3 = 384 để khớp với nhân vật
        val targetSize = 128 * 3 // 384x384 - cùng kích thước với nhân vật sau khi scale
        return Bitmap.createScaledBitmap(bitmap, targetSize, targetSize, false)
    }

    fun update(joystickX: Float, joystickY: Float) {
        if (isDead) return

        // Update damage texts
        damageTexts.forEach { it.update() }
        damageTexts.removeAll { it.isFinished() }

        // Update arrows
        arrows.forEach { it.update() }
        arrows.removeAll { !it.isActive() }

        // Update skill projectiles
        skillProjectiles.forEach { it.update() }
        skillProjectiles.removeAll { !it.isActive() }

        // Armor regeneration
        armorRegenCounter++
        if (armorRegenCounter >= armorRegenDelay) {
            armorRegenCounter = 0
            if (armor < maxArmor) {
                armor = (armor + armorRegenAmount).coerceAtMost(maxArmor)
            }
        }

        // Giảm combo timer
        if (attackComboTimer > 0) {
            attackComboTimer--
            if (attackComboTimer == 0) {
                attackComboCounter = 0
            }
        }

        // Update charge timer khi đang charge
        if (isChargingAttack && !isAnimationLocked) {
            chargeTimer++
            if (chargeTimer >= chargeTimeRequired) {
                // Đủ thời gian charge -> thực hiện charged attack
                executeChargedAttack()
            }
        }

        // Jump physics - áp dụng trước khi kiểm tra animation lock
        if (isJumping) {
            velocityY += gravity
            y += velocityY

            // Kiểm tra chạm đất
            if (y >= groundY) {
                y = groundY
                velocityY = 0f
                isJumping = false
                // Không tự động setState về IDLE nếu đang tấn công
                if (!isAnimationLocked) {
                    setState(State.IDLE)
                }
            }
        }

        // Nếu đang trong animation khóa, không thể di chuyển (trừ khi đang nhảy)
        if (isAnimationLocked && !isJumping) {
            updateAnimation()
            return
        }

        // Di chuyển nhân vật (chỉ ngang)
        val moveDistance = abs(joystickX)

        if (moveDistance > 0.1f) {
            if (joystickX > 0.1f) {
                facingRight = true
            } else if (joystickX < -0.1f) {
                facingRight = false
            }

            x += joystickX * speed
            x = x.coerceIn(0f, 3000f)

            if (!isJumping) {
                if (moveDistance > 0.7f) {
                    setState(State.RUN)
                } else {
                    setState(State.WALK)
                }
            }
        } else if (!isJumping) {
            setState(State.IDLE)
        }

        updateAnimation()
    }

    fun switchCombatMode() {
        if (isAnimationLocked) return

        combatMode = if (combatMode == CombatMode.MELEE) {
            CombatMode.RANGED
        } else {
            CombatMode.MELEE
        }
    }

    /**
     * Kiểm tra xem có thể tấn công không
     */
    fun canAttack(): Boolean {
        return !((isAnimationLocked && !isJumping) || isDead)
    }

    fun attack() {
        // Cho phép tấn công kể cả khi đang nhảy
        if (!canAttack()) return

        if (combatMode == CombatMode.MELEE) {
            // Bắt đầu charge attack
            isChargingAttack = true
            chargeTimer = 0
        } else {
            // Ranged shot - bắn cung kể cả khi đang nhảy
            setState(State.RANGED_SHOT)
            isAnimationLocked = true
            hasShotArrow = false
            currentFrame = 0
        }
    }

    fun releaseAttack() {
        if (isDead) return

        // Nhả nút attack
        if (isChargingAttack) {
            isChargingAttack = false

            if (chargeTimer < chargeTimeRequired && combatMode == CombatMode.MELEE) {
                // Chưa đủ thời gian -> attack thường (cho phép khi đang nhảy)
                attackComboTimer = attackComboTimeout
                attackComboCounter++
                hasDealtDamageThisAttack = false

                when {
                    attackComboCounter == 1 -> {
                        setState(State.MELEE_ATTACK1)
                        isAnimationLocked = true
                    }
                    attackComboCounter == 2 -> {
                        setState(State.MELEE_ATTACK2)
                        isAnimationLocked = true
                    }
                    attackComboCounter >= 3 -> {
                        setState(State.MELEE_ATTACK3)
                        isAnimationLocked = true
                        attackComboCounter = 0
                    }
                }
                currentFrame = 0
            }

            chargeTimer = 0
        }
    }

    private fun executeChargedAttack() {
        if (chargedAttackFrames.size < 3) return

        setState(State.CHARGED_ATTACK)
        isAnimationLocked = true
        isChargingAttack = false
        hasLaunchedSkills = false
        currentFrame = 0
        chargeTimer = 0
    }

    fun jump() {
        if (isJumping || isDead || jumpFrames.isEmpty()) return

        setState(State.JUMP)
        isAnimationLocked = true
        isJumping = true
        velocityY = jumpPower
        currentFrame = 0
    }

    fun setGroundY(ground: Float) {
        groundY = ground
    }

    fun takeDamage(damage: Int) {
        if (isDead) return // Bỏ check state == State.HURT

        damageTexts.add(DamageText(x, y - 50f, damage))

        if (armor > 0) {
            armor -= damage
            if (armor < 0) {
                health += armor
                armor = 0
            }
        } else {
            health -= damage
        }

        if (health <= 0) {
            health = 0
            die()
        }
        // Bỏ HURT state để không bị gián đoạn
    }

    private fun die() {
        setState(State.DEAD)
        isDead = true
        isAnimationLocked = true
        currentFrame = 0
    }

    fun getAttackDamage(): Int {
        return when (state) {
            State.MELEE_ATTACK1 -> 15  // Attack + skill overlay = stronger
            State.MELEE_ATTACK2 -> 25
            State.MELEE_ATTACK3 -> 40
            else -> 0
        }
    }

    fun isAttacking(): Boolean {
        return state == State.MELEE_ATTACK1 || state == State.MELEE_ATTACK2 || state == State.MELEE_ATTACK3
    }

    fun canDealDamage(): Boolean {
        if (hasDealtDamageThisAttack) return false

        return when (state) {
            State.MELEE_ATTACK1 -> currentFrame in 3..4
            State.MELEE_ATTACK2 -> currentFrame in 3..4
            State.MELEE_ATTACK3 -> currentFrame in 4..5
            else -> false
        }
    }

    fun markDamageDealt() {
        hasDealtDamageThisAttack = true
    }

    private fun setState(newState: State) {
        if (state == newState) return

        state = newState
        currentFrame = 0

        currentFrames = when (state) {
            State.IDLE -> idleFrames
            State.WALK -> walkFrames
            State.RUN -> if (runFrames.isEmpty()) walkFrames else runFrames
            State.JUMP -> if (jumpFrames.isEmpty()) idleFrames else jumpFrames
            State.MELEE_ATTACK1 -> meleeAttackType1Frames
            State.MELEE_ATTACK2 -> meleeAttackType2Frames
            State.MELEE_ATTACK3 -> meleeAttackType3Frames
            State.CHARGED_ATTACK -> chargedAttackFrames
            State.RANGED_SHOT -> shotFrames
            State.HURT -> hurtFrames
            State.DEAD -> deadFrames
        }
    }

    private fun updateAnimation() {
        frameCounter++

        if (frameCounter >= frameDelay) {
            frameCounter = 0
            currentFrame++

            // Bắn mũi tên ở frame giữa của animation shot
            if (state == State.RANGED_SHOT && currentFrame == 7 && !hasShotArrow && arrowBitmap != null) {
                shootArrow()
                hasShotArrow = true
            }

            // Bắn skill projectiles trong charged attack
            if (state == State.CHARGED_ATTACK && !hasLaunchedSkills) {
                // Bắn skills ở frame cuối cùng của animation (sau khi chạy hết 3 bộ type)
                val totalFrames = chargedAttackFrames.size
                if (currentFrame >= totalFrames - 2) { // Frame gần cuối
                    launchSkillProjectiles()
                    hasLaunchedSkills = true
                }
            }

            if (currentFrame >= currentFrames.size) {
                if (state == State.DEAD) {
                    currentFrame = currentFrames.size - 1
                } else {
                    currentFrame = 0

                    if (isAnimationLocked) {
                        isAnimationLocked = false

                        // Tự động chuyển về MELEE mode sau khi bắn xong
                        if (state == State.RANGED_SHOT) {
                            combatMode = CombatMode.MELEE
                        }

                        setState(State.IDLE)
                    }
                }
            }
        }
    }

    private fun launchSkillProjectiles() {
        val direction = if (facingRight) 1f else -1f
        val startX = x + (if (facingRight) 100f else -100f)
        val startY = y - 20f

        // Bắn 3 skill projectiles lần lượt
        // Skill Type 1 (trên - với yOffset để thấp xuống)
        if (meleeSkillType1Frames.isNotEmpty()) {
            val skill1 = SkillProjectile(
                startX,
                startY - 60f,
                direction,
                meleeSkillType1Frames,
                30,
                yOffset = 150f
            )
            skillProjectiles.add(skill1)
        }

        // Skill Type 2 (giữa)
        if (meleeSkillType2Frames.isNotEmpty()) {
            val skill2 = SkillProjectile(startX, startY, direction, meleeSkillType2Frames, 40)
            skillProjectiles.add(skill2)
        }

        // Skill Type 3 (dưới)
        if (meleeSkillType3Frames.isNotEmpty()) {
            val skill3 = SkillProjectile(startX, startY + 60f, direction, meleeSkillType3Frames, 50)
            skillProjectiles.add(skill3)
        }
    }

    private fun shootArrow() {
        arrowBitmap?.let { bitmap ->
            val direction = if (facingRight) 1f else -1f
            val arrowX = x + (if (facingRight) 80f else -80f)
            val arrowY = y - 20f

            val arrow = Arrow(arrowX, arrowY, direction, bitmap, 15)
            arrows.add(arrow)
        }
    }

    fun draw(canvas: Canvas) {
        if (currentFrames.isEmpty()) return

        val bitmap = currentFrames[currentFrame]

        // Vẽ nhân vật
        if (!facingRight) {
            val matrix = Matrix()
            matrix.preScale(-1f, 1f)
            val flippedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)
            canvas.drawBitmap(flippedBitmap, x - bitmap.width / 2, y - bitmap.height / 2, null)
        } else {
            canvas.drawBitmap(bitmap, x - bitmap.width / 2, y - bitmap.height / 2, null)
        }

        // Vẽ skill effect overlay khi đang melee attack
        when (state) {
            State.MELEE_ATTACK1 -> {
                if (currentFrame < meleeSkillType1Frames.size) {
                    drawSkillOverlay(canvas, meleeSkillType1Frames[currentFrame], yOffset = 150f)// giảm chiều cao của skill1
                }
            }
            State.MELEE_ATTACK2 -> {
                if (currentFrame < meleeSkillType2Frames.size) {
                    drawSkillOverlay(canvas, meleeSkillType2Frames[currentFrame])
                }
            }
            State.MELEE_ATTACK3 -> {
                if (currentFrame < meleeSkillType3Frames.size) {
                    drawSkillOverlay(canvas, meleeSkillType3Frames[currentFrame])
                }
            }
            State.CHARGED_ATTACK -> {
                // Vẽ skill overlay tương ứng với từng bộ animation
                val type1Size = meleeAttackType1Frames.size
                val type2Size = meleeAttackType2Frames.size
                val type3Size = meleeAttackType3Frames.size

                when {
                    // Đang chạy type1
                    currentFrame < type1Size -> {
                        if (currentFrame < meleeSkillType1Frames.size) {
                            drawSkillOverlay(canvas, meleeSkillType1Frames[currentFrame], yOffset = 150f)
                        }
                    }
                    // Đang chạy type2
                    currentFrame < type1Size + type2Size -> {
                        val type2Frame = currentFrame - type1Size
                        if (type2Frame < meleeSkillType2Frames.size) {
                            drawSkillOverlay(canvas, meleeSkillType2Frames[type2Frame])
                        }
                    }
                    // Đang chạy type3
                    else -> {
                        val type3Frame = currentFrame - type1Size - type2Size
                        if (type3Frame < meleeSkillType3Frames.size) {
                            drawSkillOverlay(canvas, meleeSkillType3Frames[type3Frame])
                        }
                    }
                }
            }
            else -> {}
        }

        // Vẽ damage texts
        damageTexts.forEach { it.draw(canvas) }

        // Vẽ arrows
        arrows.forEach { it.draw(canvas) }

        // Vẽ skill projectiles
        skillProjectiles.forEach { it.draw(canvas) }
    }

    private fun drawSkillOverlay(canvas: Canvas, skillBitmap: Bitmap, yOffset: Float = 0f) {
        if (!facingRight) {
            val matrix = Matrix()
            matrix.preScale(-1f, 1f)
            val flippedBitmap = Bitmap.createBitmap(skillBitmap, 0, 0, skillBitmap.width, skillBitmap.height, matrix, false)
            canvas.drawBitmap(flippedBitmap, x - skillBitmap.width / 2, y - skillBitmap.height / 2 + yOffset, null)
        } else {
            canvas.drawBitmap(skillBitmap, x - skillBitmap.width / 2, y - skillBitmap.height / 2 + yOffset, null)
        }
    }

    fun drawUI(canvas: Canvas) {
        val barWidth = 250f
        val barHeight = 30f
        val padding = 20f
        val spacing = 10f

        // Health bar
        canvas.drawRect(padding, padding, padding + barWidth, padding + barHeight,
            Paint().apply {
                color = Color.RED
                style = Paint.Style.FILL
            })

        val healthWidth = barWidth * (health.toFloat() / maxHealth)
        canvas.drawRect(padding, padding, padding + healthWidth, padding + barHeight,
            Paint().apply {
                color = Color.GREEN
                style = Paint.Style.FILL
            })

        canvas.drawRect(padding, padding, padding + barWidth, padding + barHeight,
            Paint().apply {
                color = Color.BLACK
                style = Paint.Style.STROKE
                strokeWidth = 3f
            })

        canvas.drawText("HP: $health/$maxHealth", padding + 8, padding + 21,
            Paint().apply {
                color = Color.WHITE
                textSize = 20f
                isFakeBoldText = true
                setShadowLayer(2f, 1f, 1f, Color.BLACK)
            })

        // Armor bar
        val armorY = padding + barHeight + spacing
        canvas.drawRect(padding, armorY, padding + barWidth, armorY + barHeight,
            Paint().apply {
                color = Color.DKGRAY
                style = Paint.Style.FILL
            })

        val armorWidth = barWidth * (armor.toFloat() / maxArmor)
        canvas.drawRect(padding, armorY, padding + armorWidth, armorY + barHeight,
            Paint().apply {
                color = Color.CYAN
                style = Paint.Style.FILL
            })

        canvas.drawRect(padding, armorY, padding + barWidth, armorY + barHeight,
            Paint().apply {
                color = Color.BLACK
                style = Paint.Style.STROKE
                strokeWidth = 3f
            })

        canvas.drawText("ARMOR: $armor/$maxArmor", padding + 8, armorY + 21,
            Paint().apply {
                color = Color.WHITE
                textSize = 20f
                isFakeBoldText = true
                setShadowLayer(2f, 1f, 1f, Color.BLACK)
            })

        // Combat mode indicator
        val modeY = armorY + barHeight + spacing
        val modeText = if (combatMode == CombatMode.MELEE) "⚔️ MELEE" else "🏹 RANGED"
        canvas.drawText("Mode: $modeText", padding, modeY + 20,
            Paint().apply {
                color = Color.YELLOW
                textSize = 18f
                isFakeBoldText = true
                setShadowLayer(2f, 1f, 1f, Color.BLACK)
            })
    }

    fun reset() {
        health = maxHealth
        armor = maxArmor
        isDead = false
        isAnimationLocked = false
        setState(State.IDLE)
        currentFrame = 0
        damageTexts.clear()
        arrows.clear()
        skillProjectiles.clear()
        combatMode = CombatMode.MELEE
        isChargingAttack = false
        chargeTimer = 0
    }

    // Getters
    fun getX() = x
    // getY() is auto-generated by Kotlin because y is now public var
    fun isDead() = isDead
    fun getHealth() = health
    fun setHealth(newHealth: Int) {
        health = newHealth.coerceIn(0, maxHealth)
    }
    fun getArmor() = armor
    fun getFacingRight() = facingRight
    fun getArrows() = arrows
    fun getSkillProjectiles() = skillProjectiles
    fun getCombatMode() = combatMode

    fun isCollidingWith(otherX: Float, otherY: Float, range: Float): Boolean {
        val dx = otherX - x
        val dy = otherY - y
        val distance = sqrt(dx * dx + dy * dy)
        return distance < range
    }
}