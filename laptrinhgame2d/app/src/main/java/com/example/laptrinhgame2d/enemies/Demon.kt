package com.example.laptrinhgame2d.enemies

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

class Demon(private val context: Context, private var x: Float, var y: Float) {

    private val idleFrames = mutableListOf<Bitmap>()
    private val walkFrames = mutableListOf<Bitmap>()
    private val attackFrames = mutableListOf<Bitmap>()
    private val hurtFrames = mutableListOf<Bitmap>()
    private val deadFrames = mutableListOf<Bitmap>()

    private var currentFrames = mutableListOf<Bitmap>()
    private var currentFrame = 0
    private var frameCounter = 0
    private val frameDelay = 5

    private var state = State.IDLE
    private var facingRight = true
    private val speed = 3f // Chậm hơn skeleton (skeleton = 4f)

    private var isAnimationLocked = false
    private var isDead = false

    // Health system
    private var health = 80 // Ít máu hơn skeleton (skeleton = 100)
    private val maxHealth = 80

    // Dead timer - biến mất sau 5 giây (300 frames)
    private var deadTimer = 0
    private val deadDuration = 300 // 5 seconds at 60 FPS

    // AI behavior
    private var targetX = x
    private var targetY = y
    private var attackCooldown = 0
    private val attackCooldownMax = 90 // Chậm hơn skeleton (skeleton = 60) - 1.5 giây
    private val attackRange = 250f // Xa hơn skeleton (skeleton = 220f)
    private val stopDistance = 200f // Dừng xa hơn skeleton
    private val detectionRange = 500f // Phát hiện xa hơn skeleton (skeleton = 400f)
    private val attackDamage = 30 // Mạnh hơn skeleton (skeleton = 20)
    private var hasDealtDamageThisAttack = false

    // Idle movement (di chuyển qua lại tại chỗ)
    private var idleMovementTimer = 0
    private var idleMovementDirection = 1f // 1 = phải, -1 = trái
    private val idleMovementDistance = 40f // Di chuyển xa hơn skeleton (skeleton = 30f)
    private var originalIdleX = x

    // Damage texts
    private val damageTexts = mutableListOf<DamageText>()

    enum class State {
        IDLE, WALK, ATTACK, HURT, DEAD
    }

    init {
        loadFrames()
        currentFrames = idleFrames
    }

    private fun loadFrames() {
        try {
            // Load idle frames (3 frames thực tế)
            for (i in 1..3) {
                val bitmap = loadBitmap("enemies/demon/idle/Idle$i.png")
                idleFrames.add(bitmap)
            }

            // Load walk frames (6 frames thực tế)
            for (i in 1..6) {
                val bitmap = loadBitmap("enemies/demon/walk/Walk$i.png")
                walkFrames.add(bitmap)
            }

            // Load attack frames (4 frames thực tế)
            for (i in 1..4) {
                val bitmap = loadBitmap("enemies/demon/attack/Attack$i.png")
                attackFrames.add(bitmap)
            }

            // Load hurt frames (2 frames thực tế)
            for (i in 1..2) {
                val bitmap = loadBitmap("enemies/demon/hurt/Hurt$i.png")
                hurtFrames.add(bitmap)
            }

            // Load dead frames (5 frames thực tế, trong folder "deadth")
            for (i in 1..5) {
                val bitmap = loadBitmap("enemies/demon/deadth/Death$i.png")
                deadFrames.add(bitmap)
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun loadBitmap(path: String): Bitmap {
        val inputStream = context.assets.open(path)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        // Scale bitmap 3 lần
        val scaledWidth = originalBitmap.width * 3
        val scaledHeight = originalBitmap.height * 3
        val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, scaledWidth, scaledHeight, false)
        originalBitmap.recycle()

        return scaledBitmap
    }

    fun update(playerX: Float, playerY: Float) {
        // Update damage texts
        damageTexts.forEach { it.update() }
        damageTexts.removeAll { it.isFinished() }

        if (isDead) {
            deadTimer++
            updateAnimation()
            return
        }

        // Giảm attack cooldown
        if (attackCooldown > 0) {
            attackCooldown--
        }

        // Nếu đang trong animation khóa (tấn công, hurt), không thể di chuyển
        if (isAnimationLocked) {
            updateAnimation()
            return
        }

        // Tính khoảng cách đến player
        val dx = playerX - x
        val dy = playerY - y
        val distance = sqrt(dx * dx + dy * dy)

        // AI logic với 5 trạng thái
        when {
            // 1. Đủ gần để tấn công và cooldown hết
            distance < attackRange && attackCooldown == 0 -> {
                attack()
            }

            // 2. Trong phạm vi attack nhưng đang cooldown - di chuyển qua lại nhẹ với animation
            distance < attackRange && attackCooldown > 0 -> {
                // Di chuyển qua lại nhẹ
                idleMovementTimer++
                if (idleMovementTimer >= 40) { // Đổi hướng mỗi 0.67 giây
                    idleMovementDirection *= -1f
                    idleMovementTimer = 0
                }

                // Di chuyển nhẹ qua lại
                val moveSpeed = 0.6f
                x += idleMovementDirection * moveSpeed

                // Quay mặt về hướng di chuyển
                facingRight = idleMovementDirection > 0

                // Dùng WALK animation khi di chuyển
                setState(State.WALK)
            }

            // 3. Đủ gần (trong stopDistance) - dừng lại không tiến gần hơn
            distance < stopDistance -> {
                // Quay mặt về player nhưng không di chuyển
                facingRight = dx > 0
                setState(State.IDLE)
            }

            // 4. Phát hiện player (trong detectionRange) - đuổi theo
            distance < detectionRange -> {
                // Di chuyển về phía player
                val dirX = dx / distance
                val dirY = dy / distance

                // Xác định hướng
                facingRight = dx > 0

                // Di chuyển
                x += dirX * speed
                y += dirY * speed

                // Giới hạn trong màn hình
                x = x.coerceIn(0f, 5000f)
                y = y.coerceIn(0f, 2000f)

                setState(State.WALK)
            }

            // 5. Không phát hiện player - idle và di chuyển qua lại tại chỗ với animation
            else -> {
                idleMovementTimer++
                if (idleMovementTimer >= 80) { // Đổi hướng mỗi 1.33 giây (chậm hơn skeleton)
                    idleMovementDirection *= -1f
                    idleMovementTimer = 0
                }

                // Di chuyển nhẹ qua lại
                val moveSpeed = 0.4f
                x += idleMovementDirection * moveSpeed

                // Quay mặt về hướng di chuyển
                facingRight = idleMovementDirection > 0

                // Giới hạn trong phạm vi idle movement
                if (originalIdleX == 0f) originalIdleX = x
                x = x.coerceIn(originalIdleX - idleMovementDistance, originalIdleX + idleMovementDistance)

                // Dùng WALK animation khi di chuyển qua lại
                setState(State.WALK)
            }
        }

        updateAnimation()
    }

    private fun attack() {
        if (isAnimationLocked) return

        setState(State.ATTACK)
        isAnimationLocked = true
        currentFrame = 0
        attackCooldown = attackCooldownMax
        hasDealtDamageThisAttack = false
    }

    fun takeDamage(damage: Int) {
        if (isDead || state == State.HURT) return

        health -= damage

        // Hiển thị damage text
        damageTexts.add(DamageText(x, y - 50f, damage))

        if (health <= 0) {
            health = 0
            die()
        } else {
            // Hurt animation
            setState(State.HURT)
            isAnimationLocked = true
            currentFrame = 0
        }
    }

    private fun die() {
        isDead = true
        setState(State.DEAD)
        currentFrame = 0
    }

    private fun setState(newState: State) {
        if (state == newState) return

        state = newState
        currentFrame = 0
        frameCounter = 0

        currentFrames = when (state) {
            State.IDLE -> idleFrames
            State.WALK -> walkFrames
            State.ATTACK -> attackFrames
            State.HURT -> hurtFrames
            State.DEAD -> deadFrames
        }

        if (currentFrames.isEmpty()) {
            currentFrames = idleFrames
        }
    }

    private fun updateAnimation() {
        if (currentFrames.isEmpty()) return

        frameCounter++
        if (frameCounter >= frameDelay) {
            frameCounter = 0
            currentFrame++

            // Xử lý animation loop
            when (state) {
                State.ATTACK, State.HURT, State.DEAD -> {
                    if (currentFrame >= currentFrames.size) {
                        currentFrame = currentFrames.size - 1

                        if (state == State.ATTACK || state == State.HURT) {
                            isAnimationLocked = false
                            setState(State.IDLE)
                        }
                    }
                }
                else -> {
                    if (currentFrame >= currentFrames.size) {
                        currentFrame = 0
                    }
                }
            }
        }
    }

    fun draw(canvas: Canvas) {
        if (currentFrames.isEmpty()) return

        val bitmap = currentFrames[currentFrame]

        val matrix = Matrix()
        if (!facingRight) {
            matrix.setScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
        }
        matrix.postTranslate(x - bitmap.width / 2f, y - bitmap.height / 2f)

        canvas.drawBitmap(bitmap, matrix, null)

        // Vẽ health bar nếu chưa chết
        if (!isDead) {
            drawHealthBar(canvas)
        }

        // Vẽ damage texts
        damageTexts.forEach { it.draw(canvas) }
    }

    private fun drawHealthBar(canvas: Canvas) {
        val barWidth = 120f
        val barHeight = 15f
        val barX = x - barWidth / 2
        val barY = y - 150f // Cao hơn đầu Demon

        // Vẽ background (đỏ)
        canvas.drawRect(barX, barY, barX + barWidth, barY + barHeight,
            Paint().apply {
                color = Color.RED
                style = Paint.Style.FILL
            })

        // Vẽ health hiện tại (xanh lá)
        val healthWidth = barWidth * (health.toFloat() / maxHealth)
        canvas.drawRect(barX, barY, barX + healthWidth, barY + barHeight,
            Paint().apply {
                color = Color.GREEN
                style = Paint.Style.FILL
            })

        // Vẽ viền
        canvas.drawRect(barX, barY, barX + barWidth, barY + barHeight,
            Paint().apply {
                color = Color.BLACK
                style = Paint.Style.STROKE
                strokeWidth = 2f
            })

        // Vẽ text HP
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 14f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            setShadowLayer(2f, 1f, 1f, Color.BLACK)
        }
        canvas.drawText("$health/$maxHealth", x, barY + 11, textPaint)
    }

    // Getters
    fun getX() = x
    fun isDead() = isDead
    fun getHealth() = health
    fun getAttackRange() = attackRange
    fun getAttackDamage() = attackDamage
    fun isAttacking() = state == State.ATTACK

    // Kiểm tra nếu demon đã chết và hết thời gian (sẽ bị xóa)
    fun shouldBeRemoved() = isDead && deadTimer >= deadDuration

    fun canDealDamage(): Boolean {
        if (state != State.ATTACK || hasDealtDamageThisAttack) return false

        // Frame 2-3 của attack animation là frame gây damage (attack có 4 frames)
        return currentFrame in 2..3
    }

    fun markDamageDealt() {
        hasDealtDamageThisAttack = true
    }

    fun isCollidingWith(otherX: Float, otherY: Float, range: Float): Boolean {
        val dx = otherX - x
        val dy = otherY - y
        val distance = sqrt(dx * dx + dy * dy)
        return distance < range
    }
}