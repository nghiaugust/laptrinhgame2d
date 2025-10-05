package com.example.laptrinhgame2d.enemies

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import com.example.laptrinhgame2d.DamageText
import com.example.laptrinhgame2d.enemies.JinnProjectile
import java.io.IOException
import kotlin.math.sqrt

class Jinn(private val context: Context, private var x: Float, var y: Float) {

    private val idleFrames = mutableListOf<Bitmap>()
    private val flightFrames = mutableListOf<Bitmap>()
    private val attackFrames = mutableListOf<Bitmap>()
    private val skillFrames = mutableListOf<Bitmap>()
    private val hurtFrames = mutableListOf<Bitmap>()
    private val deadFrames = mutableListOf<Bitmap>()

    private var currentFrames = mutableListOf<Bitmap>()
    private var currentFrame = 0
    private var frameCounter = 0
    private val frameDelay = 4 // Giảm từ 5 xuống 4 để mượt hơn

    private var state = State.IDLE
    private var facingRight = true
    private val speed = 3f

    private var isAnimationLocked = false
    private var isDead = false

    private var health = 150
    private val maxHealth = 150

    // Dead timer
    private var deadTimer = 0
    private val deadDuration = 300

    // AI behavior
    private var targetX = x
    private var targetY = y
    private var attackCooldown = 0
    private val attackCooldownMax = 90 // Cooldown giữa các lần bắn
    private val attackRange = 500f // Tầm bắn
    private val stopDistance = 250f // Dừng lại ở khoảng cách này để bắn
    private val detectionRange = 600f // Phát hiện player từ xa hơn
    private val projectileDamage = 25

    // Projectiles
    val projectiles = mutableListOf<JinnProjectile>()

    // Idle movement
    private var idleMovementTimer = 0
    private var idleMovementDirection = 1f
    private val idleMovementDistance = 50f
    private var originalIdleX = x

    // Damage texts
    private val damageTexts = mutableListOf<DamageText>()

    enum class State {
        IDLE, FLIGHT, ATTACK, HURT, DEAD
    }

    init {
        loadFrames()
        currentFrames = idleFrames
    }

    private fun loadFrames() {
        try {
            // Load idle frames (3 frames: Idle1-3)
            for (i in 1..3) {
                val bitmap = loadBitmap("enemies/jinn/idle/Idle$i.png")
                idleFrames.add(bitmap)
            }

            // Load flight frames (4 frames: Flight1-4)
            for (i in 1..4) {
                val bitmap = loadBitmap("enemies/jinn/flight/Flight$i.png")
                flightFrames.add(bitmap)
            }

            // Load attack frames (4 frames: Attack1-4)
            for (i in 1..4) {
                val bitmap = loadBitmap("enemies/jinn/attack/Attack$i.png")
                attackFrames.add(bitmap)
            }

            // Load hurt frames (2 frames: Hurt1-2)
            for (i in 1..2) {
                val bitmap = loadBitmap("enemies/jinn/hurt/Hurt$i.png")
                hurtFrames.add(bitmap)
            }

            // Load dead frames (6 frames: Death1-6)
            for (i in 1..6) {
                val bitmap = loadBitmap("enemies/jinn/deadth/Death$i.png")
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
        // Scale to fit screen
        return Bitmap.createScaledBitmap(bitmap, bitmap.width * 3, bitmap.height * 3, false)
    }

    fun update(playerX: Float, playerY: Float) {
        // Update damage texts
        damageTexts.forEach { it.update() }
        damageTexts.removeAll { it.isFinished() }

        // Update projectiles
        projectiles.forEach { it.update(playerX, playerY) }
        projectiles.removeAll { it.isFinished() }

        if (isDead) {
            deadTimer++
            updateAnimation()
            return
        }

        // Giảm attack cooldown
        if (attackCooldown > 0) {
            attackCooldown--
        }

        // Nếu đang trong animation khóa
        if (isAnimationLocked) {
            updateAnimation()
            return
        }

        // Tính khoảng cách đến player
        val dx = playerX - x
        val dy = playerY - y
        val distance = sqrt(dx * dx + dy * dy)

        // AI logic
        when {
            // 1. Trong tầm bắn và cooldown hết - tấn công (attack animation + bắn projectile)
            distance < attackRange && attackCooldown == 0 -> {
                attack(playerX, playerY)
            }

            // 2. Trong phạm vi phát hiện nhưng đang cooldown - bay qua lại
            distance < detectionRange && attackCooldown > 0 -> {
                idleMovementTimer++
                if (idleMovementTimer >= 30) {
                    idleMovementDirection *= -1f
                    idleMovementTimer = 0
                }

                val moveSpeed = 1.0f
                x += idleMovementDirection * moveSpeed
                facingRight = dx > 0
                setState(State.FLIGHT)
            }

            // 3. Quá gần stopDistance - lùi lại để giữ khoảng cách bắn
            distance < stopDistance -> {
                // Lùi ra xa player
                val dirX = -dx / distance
                val dirY = -dy / distance

                facingRight = dx > 0

                x += dirX * speed * 0.5f
                y += dirY * speed * 0.5f

                setState(State.FLIGHT)
            }

            // 4. Phát hiện player - tiến lại gần đến khoảng cách tối ưu
            distance < detectionRange -> {
                // Nếu quá xa attackRange, tiến lại gần
                if (distance > attackRange) {
                    val dirX = dx / distance
                    val dirY = dy / distance

                    facingRight = dx > 0

                    x += dirX * speed
                    y += dirY * speed

                    setState(State.FLIGHT)
                } else {
                    // Trong tầm bắn, dừng lại và chờ cooldown
                    facingRight = dx > 0
                    setState(State.IDLE)
                }
            }

            // 5. Idle - bay qua lại
            else -> {
                idleMovementTimer++
                if (idleMovementTimer >= 60) {
                    idleMovementDirection *= -1f
                    idleMovementTimer = 0
                }

                val moveSpeed = 0.5f
                x += idleMovementDirection * moveSpeed
                facingRight = idleMovementDirection > 0

                if (originalIdleX == 0f) originalIdleX = x
                x = x.coerceIn(originalIdleX - idleMovementDistance, originalIdleX + idleMovementDistance)

                setState(State.FLIGHT)
            }
        }

        // Giới hạn trong màn hình
        x = x.coerceIn(0f, 2000f)
        y = y.coerceIn(0f, 1000f)

        updateAnimation()
    }

    private fun attack(targetX: Float, targetY: Float) {
        if (isAnimationLocked) return

        setState(State.ATTACK)
        isAnimationLocked = true
        currentFrame = 0
        attackCooldown = attackCooldownMax

        // Lưu vị trí target để spawn projectile sau
        this.targetX = targetX
        this.targetY = targetY
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
            State.FLIGHT -> flightFrames
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

            // Spawn projectile ở frame 3 của attack animation (4 frames total, spawn ở giữa-cuối)
            if (state == State.ATTACK && currentFrame == 3) {
                spawnProjectile()
            }

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

    private fun spawnProjectile() {
        // Spawn projectile cách xa Jinn hơn (120px) để khớp với tay Jinn trong animation
        // Và thấp hơn một chút (y + 30f) để khớp với vị trí tay
        val offsetX = if (facingRight) 120f else -120f
        val projectileX = x + offsetX
        val projectileY = y + 30f // Dịch xuống 30px thay vì lên -20px

        val projectile =
            JinnProjectile(context, projectileX, projectileY, targetX, targetY, projectileDamage)
        projectiles.add(projectile)
    }

    fun draw(canvas: Canvas) {
        if (currentFrames.isEmpty()) return

        val bitmap = currentFrames[currentFrame]

        // Fade out effect khi chết
        val paint = if (isDead && deadTimer > deadDuration - 60) {
            val fadeProgress = (deadTimer - (deadDuration - 60)) / 60f
            Paint().apply {
                alpha = ((1f - fadeProgress) * 255).toInt().coerceIn(0, 255)
            }
        } else {
            null
        }

        if (!facingRight) {
            val matrix = Matrix()
            matrix.preScale(-1f, 1f)
            val flippedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)
            canvas.drawBitmap(flippedBitmap, x - bitmap.width / 2, y - bitmap.height / 2, paint)
        } else {
            canvas.drawBitmap(bitmap, x - bitmap.width / 2, y - bitmap.height / 2, paint)
        }

        // Vẽ projectiles
        projectiles.forEach { it.draw(canvas) }

        // Vẽ damage texts
        damageTexts.forEach { it.draw(canvas) }

        // Vẽ health bar
        if (!isDead && health < maxHealth) {
            drawHealthBar(canvas)
        }
    }

    private fun drawHealthBar(canvas: Canvas) {
        val barWidth = 120f
        val barHeight = 15f
        val barX = x - barWidth / 2
        val barY = y - 150f

        // Background (đỏ)
        canvas.drawRect(barX, barY, barX + barWidth, barY + barHeight,
            Paint().apply {
                color = Color.RED
                style = Paint.Style.FILL
            })

        // Health hiện tại (xanh lá)
        val healthWidth = barWidth * (health.toFloat() / maxHealth)
        canvas.drawRect(barX, barY, barX + healthWidth, barY + barHeight,
            Paint().apply {
                color = Color.GREEN
                style = Paint.Style.FILL
            })

        // Viền
        canvas.drawRect(barX, barY, barX + barWidth, barY + barHeight,
            Paint().apply {
                color = Color.BLACK
                style = Paint.Style.STROKE
                strokeWidth = 2f
            })

        // Text HP
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
    // getY() tự động được tạo bởi Kotlin vì y là public var
    fun isDead() = isDead
    fun getHealth() = health
    // getProjectiles() tự động được tạo bởi Kotlin vì projectiles là public val

    fun shouldBeRemoved(): Boolean {
        return isDead && deadTimer >= deadDuration
    }

    // Va chạm (không cần cho Jinn vì chỉ tấn công tầm xa)
    fun isCollidingWith(otherX: Float, otherY: Float, range: Float): Boolean {
        val dx = otherX - x
        val dy = otherY - y
        val distance = sqrt(dx * dx + dy * dy)
        return distance < range
    }
}