package com.example.laptrinhgame2d.heroes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import com.example.laptrinhgame2d.DamageText
import java.io.IOException
import kotlin.math.abs
import kotlin.math.sqrt

class SamuraiCommander(private val context: Context, private var x: Float, var y: Float) {

    private val idleFrames = mutableListOf<Bitmap>()
    private val walkFrames = mutableListOf<Bitmap>()
    private val runFrames = mutableListOf<Bitmap>()
    private val jumpFrames = mutableListOf<Bitmap>()

    // Melee Attack (chỉ 3 type)
    private val attackType1Frames = mutableListOf<Bitmap>()
    private val attackType2Frames = mutableListOf<Bitmap>()
    private val attackType3Frames = mutableListOf<Bitmap>()

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

    // Health and Armor
    private var health = 120
    private val maxHealth = 120
    private var armor = 100
    private val maxArmor = 100
    private var armorRegenCounter = 0
    private val armorRegenDelay = 120
    private val armorRegenAmount = 10

    private val damageTexts = mutableListOf<DamageText>()

    enum class State {
        IDLE, WALK, RUN, JUMP,
        ATTACK1, ATTACK2, ATTACK3,
        HURT, DEAD
    }

    init {
        loadFrames()
        currentFrames = idleFrames
    }

    private fun loadFrames() {
        try {
            // Load idle frames
            for (i in 1..4) {
                val bitmap = loadBitmap("heroes/Samurai_Commander/idle/Idle$i.png")
                idleFrames.add(bitmap)
            }

            // Load walk frames
            for (i in 1..8) {
                val bitmap = loadBitmap("heroes/Samurai_Commander/walk/Walk$i.png")
                walkFrames.add(bitmap)
            }

            // Load run frames
            val runFiles = context.assets.list("heroes/Samurai_Commander/run") ?: emptyArray()
            for (file in runFiles.sorted()) {
                if (file.endsWith(".png") && file != "Run.png") {
                    val bitmap = loadBitmap("heroes/Samurai_Commander/run/$file")
                    runFrames.add(bitmap)
                }
            }

            // Load jump frames
            val jumpFiles = context.assets.list("heroes/Samurai_Commander/jump") ?: emptyArray()
            for (file in jumpFiles.sorted()) {
                if (file.endsWith(".png") && file != "Jump.png") {
                    val bitmap = loadBitmap("heroes/Samurai_Commander/jump/$file")
                    jumpFrames.add(bitmap)
                }
            }

            // Load attack frames
            val attackType1Files = context.assets.list("heroes/Samurai_Commander/attack/type1") ?: emptyArray()
            for (file in attackType1Files.sorted()) {
                if (file.endsWith(".png")) {
                    val bitmap = loadBitmap("heroes/Samurai_Commander/attack/type1/$file")
                    attackType1Frames.add(bitmap)
                }
            }

            val attackType2Files = context.assets.list("heroes/Samurai_Commander/attack/type2") ?: emptyArray()
            for (file in attackType2Files.sorted()) {
                if (file.endsWith(".png")) {
                    val bitmap = loadBitmap("heroes/Samurai_Commander/attack/type2/$file")
                    attackType2Frames.add(bitmap)
                }
            }

            val attackType3Files = context.assets.list("heroes/Samurai_Commander/attack/type3") ?: emptyArray()
            for (file in attackType3Files.sorted()) {
                if (file.endsWith(".png")) {
                    val bitmap = loadBitmap("heroes/Samurai_Commander/attack/type3/$file")
                    attackType3Frames.add(bitmap)
                }
            }

            // Load hurt frames
            val hurtFiles = context.assets.list("heroes/Samurai_Commander/hurt") ?: emptyArray()
            for (file in hurtFiles.sorted()) {
                if (file.endsWith(".png") && file != "Hurt.png") {
                    val bitmap = loadBitmap("heroes/Samurai_Commander/hurt/$file")
                    hurtFrames.add(bitmap)
                }
            }

            // Load dead frames
            val deadFiles = context.assets.list("heroes/Samurai_Commander/dead") ?: emptyArray()
            for (file in deadFiles.sorted()) {
                if (file.endsWith(".png") && file != "Dead.png") {
                    val bitmap = loadBitmap("heroes/Samurai_Commander/dead/$file")
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

    fun update(joystickX: Float, joystickY: Float) {
        if (isDead) return

        // Update damage texts
        damageTexts.forEach { it.update() }
        damageTexts.removeAll { it.isFinished() }

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

    /**
     * Kiểm tra xem có thể tấn công không
     */
    fun canAttack(): Boolean {
        return !((isAnimationLocked && !isJumping) || isDead)
    }

    fun attack() {
        // Cho phép tấn công kể cả khi đang nhảy
        if (!canAttack()) return

        // Combo attack 3 type
        attackComboTimer = attackComboTimeout
        attackComboCounter++
        hasDealtDamageThisAttack = false

        when {
            attackComboCounter == 1 -> {
                setState(State.ATTACK1)
                isAnimationLocked = true
            }
            attackComboCounter == 2 -> {
                setState(State.ATTACK2)
                isAnimationLocked = true
            }
            attackComboCounter >= 3 -> {
                setState(State.ATTACK3)
                isAnimationLocked = true
                attackComboCounter = 0
            }
        }

        currentFrame = 0
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
            State.ATTACK1 -> 20
            State.ATTACK2 -> 30
            State.ATTACK3 -> 45
            else -> 0
        }
    }

    fun isAttacking(): Boolean {
        return state == State.ATTACK1 || state == State.ATTACK2 || state == State.ATTACK3
    }

    fun canDealDamage(): Boolean {
        if (hasDealtDamageThisAttack) return false

        return when (state) {
            State.ATTACK1 -> currentFrame in 3..4
            State.ATTACK2 -> currentFrame in 3..4
            State.ATTACK3 -> currentFrame in 4..5
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
            State.ATTACK1 -> attackType1Frames
            State.ATTACK2 -> attackType2Frames
            State.ATTACK3 -> attackType3Frames
            State.HURT -> hurtFrames
            State.DEAD -> deadFrames
        }
    }

    private fun updateAnimation() {
        frameCounter++

        if (frameCounter >= frameDelay) {
            frameCounter = 0
            currentFrame++

            if (currentFrame >= currentFrames.size) {
                if (state == State.DEAD) {
                    currentFrame = currentFrames.size - 1
                } else {
                    currentFrame = 0

                    if (isAnimationLocked) {
                        isAnimationLocked = false
                        setState(State.IDLE)
                    }
                }
            }
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

        // Vẽ damage texts
        damageTexts.forEach { it.draw(canvas) }
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
    }

    fun reset() {
        health = maxHealth
        armor = maxArmor
        isDead = false
        isAnimationLocked = false
        setState(State.IDLE)
        currentFrame = 0
        damageTexts.clear()
    }

    // Getters
    fun getX() = x
    // getY() is auto-generated by Kotlin because y is now public var
    fun isDead() = isDead
    fun getHealth() = health
    fun getArmor() = armor
    fun getFacingRight() = facingRight

    fun isCollidingWith(otherX: Float, otherY: Float, range: Float): Boolean {
        val dx = otherX - x
        val dy = otherY - y
        val distance = sqrt(dx * dx + dy * dy)
        return distance < range
    }
}