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
import kotlin.math.sqrt

class Fighter(private val context: Context, private var x: Float, var y: Float) {

    private val idleFrames = mutableListOf<Bitmap>()
    private val walkFrames = mutableListOf<Bitmap>()
    private val runFrames = mutableListOf<Bitmap>()
    private val jumpFrames = mutableListOf<Bitmap>()
    private val attackType1Frames = mutableListOf<Bitmap>()
    private val attackType2Frames = mutableListOf<Bitmap>()
    private val attackType3Frames = mutableListOf<Bitmap>()
    private val shieldFrames = mutableListOf<Bitmap>()
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
    private val jumpPower = -25f // Tăng từ -15f lên -25f (nhảy cao hơn)
    private val gravity = 1.2f
    private var isJumping = false
    private var groundY = 0f

    private var attackComboCounter = 0
    private var attackComboTimer = 0
    private val attackComboTimeout = 30 // frames

    private var isAnimationLocked = false
    private var isDead = false
    private var hasDealtDamageThisAttack = false
    private var isShieldActive = false

    // Health and Armor system
    private var health = 100
    private val maxHealth = 100
    private var armor = 100
    private val maxArmor = 100
    private var armorRegenCounter = 0
    private val armorRegenDelay = 120 // 2 seconds at 60 FPS
    private val armorRegenAmount = 10

    // Damage texts
    private val damageTexts = mutableListOf<DamageText>()

    enum class State {
        IDLE, WALK, RUN, JUMP, ATTACK1, ATTACK2, ATTACK3, SHIELD, HURT, DEAD
    }

    init {
        loadFrames()
        currentFrames = idleFrames
    }

    private fun loadFrames() {
        try {
            // Load idle frames
            for (i in 1..5) {
                val bitmap = loadBitmap("heroes/Fighter/idle/Idle$i.png")
                idleFrames.add(bitmap)
            }

            // Load walk frames
            for (i in 1..7) {
                val bitmap = loadBitmap("heroes/Fighter/walk/Walk$i.png")
                walkFrames.add(bitmap)
            }

            // Load run frames
            for (i in 1..7) {
                val bitmap = loadBitmap("heroes/Fighter/run/Run$i.png")
                runFrames.add(bitmap)
            }

            // Load jump frames
            for (i in 1..9) {
                val bitmap = loadBitmap("heroes/Fighter/jump/Jump$i.png")
                jumpFrames.add(bitmap)
            }

            // Load attack type1 frames
            val attackType1Files = context.assets.list("heroes/Fighter/attack/type1") ?: emptyArray()
            for (file in attackType1Files.sorted()) {
                if (file.endsWith(".png")) {
                    val bitmap = loadBitmap("heroes/Fighter/attack/type1/$file")
                    attackType1Frames.add(bitmap)
                }
            }

            // Load attack type2 frames
            val attackType2Files = context.assets.list("heroes/Fighter/attack/type2") ?: emptyArray()
            for (file in attackType2Files.sorted()) {
                if (file.endsWith(".png")) {
                    val bitmap = loadBitmap("heroes/Fighter/attack/type2/$file")
                    attackType2Frames.add(bitmap)
                }
            }

            // Load attack type3 frames
            val attackType3Files = context.assets.list("heroes/Fighter/attack/type3") ?: emptyArray()
            for (file in attackType3Files.sorted()) {
                if (file.endsWith(".png")) {
                    val bitmap = loadBitmap("heroes/Fighter/attack/type3/$file")
                    attackType3Frames.add(bitmap)
                }
            }

            // Load shield frames
            for (i in 1..1) {
                val bitmap = loadBitmap("heroes/Fighter/shield/Shield$i.png")
                shieldFrames.add(bitmap)
            }

            // Load hurt frames
            for (i in 1..2) {
                val bitmap = loadBitmap("heroes/Fighter/hurt/Hurt$i.png")
                hurtFrames.add(bitmap)
            }

            // Load dead frames
            for (i in 1..2) {
                val bitmap = loadBitmap("heroes/Fighter/dead/Dead$i.png")
                deadFrames.add(bitmap)
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun loadBitmap(path: String): Bitmap {
        val inputStream = context.assets.open(path)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        // Scale bitmap để phù hợp với màn hình
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
                // Không tự động setState về IDLE, để animation tấn công tiếp tục nếu đang tấn công
                if (!isAnimationLocked) {
                    setState(State.IDLE)
                }
            }
        }

        // Nếu đang trong animation khóa (tấn công, shield, hurt), không thể di chuyển
        // Trừ khi đang nhảy thì cho phép di chuyển
        if (isAnimationLocked && state != State.SHIELD && !isJumping) {
            updateAnimation()
            return
        }

        // Nếu đang shield, chỉ update animation không di chuyển
        if (isShieldActive) {
            updateAnimation()
            return
        }

        // Di chuyển nhân vật (chỉ di chuyển ngang, không di chuyển dọc)
        val moveDistance = Math.abs(joystickX)

        if (moveDistance > 0.1f) {
            // Xác định hướng
            if (joystickX > 0.1f) {
                facingRight = true
            } else if (joystickX < -0.1f) {
                facingRight = false
            }

            // Di chuyển ngang (kể cả khi đang nhảy)
            x += joystickX * speed

            // Giới hạn trong thế giới game (world bounds)
            x = x.coerceIn(0f, 3000f)

            // Chọn animation (không thay đổi nếu đang nhảy)
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
        return !((isAnimationLocked && !isJumping) || isDead || isShieldActive)
    }

    fun attack() {
        // Cho phép tấn công kể cả khi đang nhảy, chỉ cần không bị khóa animation khác
        if (!canAttack()) return

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

    fun activateShield() {
        // Không cho phép shield khi đang nhảy
        if (isAnimationLocked || isDead || isJumping) return

        setState(State.SHIELD)
        isShieldActive = true
        currentFrame = 0
    }

    fun deactivateShield() {
        if (state == State.SHIELD) {
            isShieldActive = false
            setState(State.IDLE)
        }
    }

    fun jump() {
        if (isJumping || isDead || isShieldActive) return

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
        if (isDead || isShieldActive) return // Bỏ check state == State.HURT để không gián đoạn

        // Tạo damage text
        damageTexts.add(DamageText(x, y - 50f, damage))

        // Armor absorbs damage first
        if (armor > 0) {
            armor -= damage
            if (armor < 0) {
                health += armor // Add the overflow damage to health
                armor = 0
            }
        } else {
            health -= damage
        }

        if (health <= 0) {
            health = 0
            die()
        }
        // Bỏ HURT state để không bị gián đoạn khi bị đánh
    }

    private fun die() {
        setState(State.DEAD)
        isDead = true
        isAnimationLocked = true
        currentFrame = 0
    }

    fun getAttackDamage(): Int {
        return when (state) {
            State.ATTACK1 -> 10
            State.ATTACK2 -> 15
            State.ATTACK3 -> 25
            else -> 0
        }
    }

    fun isAttacking(): Boolean {
        return state == State.ATTACK1 || state == State.ATTACK2 || state == State.ATTACK3
    }

    // Kiểm tra nếu đang ở frame gây damage của attack animation
    fun canDealDamage(): Boolean {
        if (hasDealtDamageThisAttack) return false

        return when (state) {
            State.ATTACK1 -> currentFrame in 2..3
            State.ATTACK2 -> currentFrame in 2..3
            State.ATTACK3 -> currentFrame in 3..4
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
            State.RUN -> runFrames
            State.JUMP -> jumpFrames
            State.ATTACK1 -> attackType1Frames
            State.ATTACK2 -> attackType2Frames
            State.ATTACK3 -> attackType3Frames
            State.SHIELD -> shieldFrames
            State.HURT -> hurtFrames
            State.DEAD -> deadFrames
        }
    }

    private fun updateAnimation() {
        frameCounter++

        if (frameCounter >= frameDelay) {
            frameCounter = 0

            // Nếu đang shield, giữ ở frame 0
            if (isShieldActive) {
                currentFrame = 0
                return
            }

            currentFrame++

            if (currentFrame >= currentFrames.size) {
                // Nếu đang dead, giữ ở frame cuối
                if (state == State.DEAD) {
                    currentFrame = currentFrames.size - 1
                } else {
                    currentFrame = 0

                    // Kết thúc animation khóa
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

        if (!facingRight) {
            // Lật bitmap
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
        // Draw health bar - Tăng kích thước
        val barWidth = 250f
        val barHeight = 30f
        val padding = 20f
        val spacing = 10f

        // Health bar background (red)
        canvas.drawRect(padding, padding, padding + barWidth, padding + barHeight,
            Paint().apply {
                color = Color.RED
                style = Paint.Style.FILL
            })

        // Health bar (green)
        val healthWidth = barWidth * (health.toFloat() / maxHealth)
        canvas.drawRect(padding, padding, padding + healthWidth, padding + barHeight,
            Paint().apply {
                color = Color.GREEN
                style = Paint.Style.FILL
            })

        // Health bar border
        canvas.drawRect(padding, padding, padding + barWidth, padding + barHeight,
            Paint().apply {
                color = Color.BLACK
                style = Paint.Style.STROKE
                strokeWidth = 3f
            })

        // Health text - Tăng font size
        canvas.drawText("HP: $health/$maxHealth", padding + 8, padding + 21,
            Paint().apply {
                color = Color.WHITE
                textSize = 20f
                isFakeBoldText = true
                setShadowLayer(2f, 1f, 1f, Color.BLACK)
            })

        // Armor bar background (dark gray)
        val armorY = padding + barHeight + spacing
        canvas.drawRect(padding, armorY, padding + barWidth, armorY + barHeight,
            Paint().apply {
                color = Color.DKGRAY
                style = Paint.Style.FILL
            })

        // Armor bar (blue)
        val armorWidth = barWidth * (armor.toFloat() / maxArmor)
        canvas.drawRect(padding, armorY, padding + armorWidth, armorY + barHeight,
            Paint().apply {
                color = Color.CYAN
                style = Paint.Style.FILL
            })

        // Armor bar border
        canvas.drawRect(padding, armorY, padding + barWidth, armorY + barHeight,
            Paint().apply {
                color = Color.BLACK
                style = Paint.Style.STROKE
                strokeWidth = 3f
            })

        // Armor text - Tăng font size
        canvas.drawText("ARMOR: $armor/$maxArmor", padding + 8, armorY + 21,
            Paint().apply {
                color = Color.WHITE
                textSize = 20f
                isFakeBoldText = true
                setShadowLayer(2f, 1f, 1f, Color.BLACK)
            })
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

    // Reset fighter khi continue
    fun reset() {
        health = maxHealth
        armor = maxArmor
        isDead = false
        isAnimationLocked = false
        isShieldActive = false
        setState(State.IDLE)
        currentFrame = 0
        damageTexts.clear()
    }

    // Kiểm tra va chạm
    fun isCollidingWith(otherX: Float, otherY: Float, range: Float): Boolean {
        val dx = otherX - x
        val dy = otherY - y
        val distance = sqrt(dx * dx + dy * dy)
        return distance < range
    }
}