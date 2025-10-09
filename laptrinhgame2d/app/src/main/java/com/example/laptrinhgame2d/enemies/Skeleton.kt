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

class Skeleton(private val context: Context, private var x: Float, var y: Float) {

    private val idleFrames = mutableListOf<Bitmap>()
    private val walkFrames = mutableListOf<Bitmap>()
    private val attackFrames = mutableListOf<Bitmap>()
    private val hurtFrames = mutableListOf<Bitmap>()
    private val deadFrames = mutableListOf<Bitmap>()

    private var currentFrames = mutableListOf<Bitmap>()
    private var currentFrame = 0
    private var frameCounter = 0
    private val frameDelay = 5 // Số frame trước khi chuyển sang sprite tiếp theo

    private var state = State.IDLE
    private var facingRight = true
    private val speed = 4f

    private var isAnimationLocked = false
    private var isDead = false

    private var health = 100
    private val maxHealth = 100

    // Dead timer - biến mất sau 5 giây (300 frames)
    private var deadTimer = 0
    private val deadDuration = 120 // 5 seconds at 60 FPS

    // AI behavior
    private var targetX = x
    private var targetY = y
    private var attackCooldown = 0
    private val attackCooldownMax = 60 // frames
    private val attackRange = 220f // Tăng từ 150f lên 220f - skeleton bắt đầu attack sớm hơn
    private val stopDistance = 180f // Dừng lại cách player 180 pixels (không đi đè lên)
    private val detectionRange = 400f
    private val attackDamage = 20
    private var hasDealtDamageThisAttack = false

    // Idle movement (di chuyển qua lại tại chỗ)
    private var idleMovementTimer = 0
    private var idleMovementDirection = 1f // 1 = phải, -1 = trái
    private val idleMovementDistance = 30f // Di chuyển 30 pixels qua lại
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
            // Load idle frame
            val idleBitmap = loadBitmap("enemies/skeleton/idle/idle1.png")
            idleFrames.add(idleBitmap)

            // Load walk frames
            for (i in 1..7) {
                val bitmap = loadBitmap("enemies/skeleton/walk/walk$i.png")
                walkFrames.add(bitmap)
            }

            // Load attack frames
            for (i in 1..6) {
                val bitmap = loadBitmap("enemies/skeleton/attack/attack$i.png")
                attackFrames.add(bitmap)
            }

            // Load hurt frames
            for (i in 1..2) {
                val bitmap = loadBitmap("enemies/skeleton/hurt/hurt$i.png")
                hurtFrames.add(bitmap)
            }

            // Load dead frame
            val deadBitmap = loadBitmap("enemies/skeleton/dead/dead.png")
            deadFrames.add(deadBitmap)

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

    fun update(playerX: Float, playerY: Float) {
        // Update damage texts
        damageTexts.forEach { it.update() }
        damageTexts.removeAll { it.isFinished() }

        if (isDead) {
            deadTimer++
            updateAnimation() // Vẫn cần update animation khi chết
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

        // AI logic với 4 trạng thái
        when {
            // 1. Đủ gần để tấn công và cooldown hết
            distance < attackRange && attackCooldown == 0 -> {
                attack()
            }

            // 2. Trong phạm vi attack nhưng đang cooldown - di chuyển qua lại nhẹ với animation
            distance < attackRange && attackCooldown > 0 -> {
                // Di chuyển qua lại nhẹ
                idleMovementTimer++
                if (idleMovementTimer >= 30) { // Đổi hướng mỗi 0.5 giây
                    idleMovementDirection *= -1f
                    idleMovementTimer = 0
                }

                // Di chuyển nhẹ qua lại
                val moveSpeed = 0.8f
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
                x = x.coerceIn(0f, 2000f)
                y = y.coerceIn(0f, 1000f)

                setState(State.WALK)
            }

            // 5. Không phát hiện player - idle và di chuyển qua lại tại chỗ với animation
            else -> {
                idleMovementTimer++
                if (idleMovementTimer >= 60) { // Đổi hướng mỗi 1 giây
                    idleMovementDirection *= -1f
                    idleMovementTimer = 0
                }

                // Di chuyển nhẹ qua lại
                val moveSpeed = 0.5f
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

        // Tạo damage text
        damageTexts.add(DamageText(x, y - 50f, damage))

        health -= damage

        if (health <= 0) {
            health = 0
            die()
        } else {
            setState(State.HURT)
            isAnimationLocked = true
            currentFrame = 0
        }
    }

    private fun die() {
        setState(State.DEAD)
        isDead = true
        isAnimationLocked = true
        currentFrame = 0
    }

    private fun setState(newState: State) {
        if (state == newState) return

        state = newState
        currentFrame = 0

        currentFrames = when (state) {
            State.IDLE -> idleFrames
            State.WALK -> walkFrames
            State.ATTACK -> attackFrames
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

        // Tính alpha cho fade out effect khi chết
        val paint = if (isDead && deadTimer > deadDuration - 60) {
            // Fade out trong 1 giây cuối (60 frames)
            val fadeProgress = (deadTimer - (deadDuration - 60)) / 60f
            Paint().apply {
                alpha = ((1f - fadeProgress) * 255).toInt().coerceIn(0, 255)
            }
        } else {
            null
        }

        if (!facingRight) {
            // Lật bitmap
            val matrix = Matrix()
            matrix.preScale(-1f, 1f)
            val flippedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)
            canvas.drawBitmap(flippedBitmap, x - bitmap.width / 2, y - bitmap.height / 2, paint)
        } else {
            canvas.drawBitmap(bitmap, x - bitmap.width / 2, y - bitmap.height / 2, paint)
        }

        // Vẽ damage texts
        damageTexts.forEach { it.draw(canvas) }

        // Vẽ health bar nếu chưa chết
        if (!isDead && health < maxHealth) {
            drawHealthBar(canvas)
        }
    }

    private fun drawHealthBar(canvas: Canvas) {
        val barWidth = 120f
        val barHeight = 15f
        val barX = x - barWidth / 2
        val barY = y - 150f

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
    // getY() is auto-generated by Kotlin because y is now public var
    fun isDead() = isDead
    fun getHealth() = health
    fun getAttackRange() = attackRange
    fun getAttackDamage() = attackDamage
    fun isAttacking() = state == State.ATTACK

    // Kiểm tra nếu skeleton đã chết và hết thời gian (sẽ bị xóa)
    fun shouldBeRemoved(): Boolean {
        return isDead && deadTimer >= deadDuration
    }

    // Kiểm tra nếu đang ở frame gây damage (frame 3-4 của attack animation)
    fun canDealDamage(): Boolean {
        return state == State.ATTACK && currentFrame in 3..4 && !hasDealtDamageThisAttack
    }

    fun markDamageDealt() {
        hasDealtDamageThisAttack = true
    }

    // Kiểm tra va chạm
    fun isCollidingWith(otherX: Float, otherY: Float, range: Float): Boolean {
        val dx = otherX - x
        val dy = otherY - y
        val distance = sqrt(dx * dx + dy * dy)
        return distance < range
    }
}