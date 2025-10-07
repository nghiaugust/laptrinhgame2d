package com.example.laptrinhgame2d.enemies

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import com.example.laptrinhgame2d.DamageText
import com.example.laptrinhgame2d.enemies.MedusaProjectile
import java.io.IOException
import kotlin.math.abs
import kotlin.math.sqrt

class Medusa(private val context: Context, private var x: Float, var y: Float) {

    private val idleFrames = mutableListOf<Bitmap>()
    private val walkFrames = mutableListOf<Bitmap>()
    private val attackFrames = mutableListOf<Bitmap>()
    private val hurtFrames = mutableListOf<Bitmap>()
    private val deadFrames = mutableListOf<Bitmap>()

    private var currentFrames = mutableListOf<Bitmap>()
    private var currentFrame = 0
    private var frameCounter = 0
    private val frameDelay = 4

    private var state = State.IDLE
    private var facingRight = true
    private val speed = 3.5f

    private var isAnimationLocked = false
    private var isDead = false

    private var health = 100
    private val maxHealth = 100

    // Dead timer
    private var deadTimer = 0
    private val deadDuration = 300

    // AI behavior
    private var targetX = x
    private var targetY = y
    private var attackCooldown = 0
    private val attackCooldownMax = 80 // Cooldown giữa các lần bắn đá
    private val attackRange = 480f // Tầm bắn
    private val stopDistance = 220f // Dừng lại ở khoảng cách này để bắn
    private val detectionRange = 580f // Phát hiện player
    private val projectileDamage = 22

    // Stone Projectiles
    val projectiles = mutableListOf<MedusaProjectile>()

    // Idle movement
    private var idleMovementTimer = 0
    private var idleMovementDirection = 1f
    private val idleMovementDistance = 50f
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
            // Load idle frames (3 frames)
            for (i in 1..3) {
                val bitmap = loadBitmap("enemies/medusa/idle/Idle$i.png")
                idleFrames.add(bitmap)
            }

            // Load walk frames (4 frames)
            for (i in 1..4) {
                val bitmap = loadBitmap("enemies/medusa/walk/Walk$i.png")
                walkFrames.add(bitmap)
            }

            // Load attack frames (6 frames)
            for (i in 1..6) {
                val bitmap = loadBitmap("enemies/medusa/attack/Attack$i.png")
                attackFrames.add(bitmap)
            }

            // Load hurt frames (2 frames)
            for (i in 1..2) {
                val bitmap = loadBitmap("enemies/medusa/hurt/Hurt$i.png")
                hurtFrames.add(bitmap)
            }

            // Load dead frames (6 frames, trong folder "deadth")
            for (i in 1..6) {
                val bitmap = loadBitmap("enemies/medusa/deadth/Death$i.png")
                deadFrames.add(bitmap)
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun loadBitmap(path: String): Bitmap {
        return try {
            val inputStream = context.assets.open(path)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: IOException) {
            e.printStackTrace()
            Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        }
    }

    fun update(playerX: Float, playerY: Float, playerIsDead: Boolean) {
        if (isDead) {
            deadTimer++
            return
        }

        // Update cooldowns
        if (attackCooldown > 0) attackCooldown--

        // Update projectiles
        projectiles.removeAll { it.isDead() }
        projectiles.forEach { it.update() }

        // Update damage texts
        damageTexts.forEach { it.update() }
        damageTexts.removeAll { it.isFinished() }

        if (playerIsDead) {
            setState(State.IDLE)
            return
        }

        // Lưu vị trí player để bắn đá
        targetX = playerX
        targetY = playerY

        val distance = calculateDistance(x, y, playerX, playerY)

        // AI behavior
        when {
            // Chết
            health <= 0 -> {
                if (state != State.DEAD) {
                    setState(State.DEAD)
                }
            }

            // Tấn công nếu trong tầm
            distance <= attackRange && attackCooldown == 0 && state != State.ATTACK && state != State.HURT -> {
                facingRight = playerX > x
                setState(State.ATTACK)
            }

            // Đuổi theo player nếu ở xa
            distance > stopDistance && distance <= detectionRange && state != State.ATTACK && state != State.HURT -> {
                // Di chuyển về phía player
                val dirX = playerX - x
                val dirY = playerY - y
                val length = sqrt(dirX * dirX + dirY * dirY)
                if (length > 0) {
                    x += (dirX / length) * speed
                    facingRight = dirX > 0
                }
                setState(State.WALK)
            }

            // Giữ khoảng cách
            distance <= stopDistance && state != State.ATTACK && state != State.HURT -> {
                // Đứng yên chờ cooldown
                setState(State.IDLE)
            }

            // Idle wandering
            distance > detectionRange && state != State.ATTACK && state != State.HURT -> {
                setState(State.IDLE)

                // Idle movement
                idleMovementTimer++
                if (idleMovementTimer > 30) {
                    x += idleMovementDirection * 1f
                    if (abs(x - originalIdleX) > idleMovementDistance) {
                        idleMovementDirection *= -1f
                        facingRight = !facingRight
                    }
                }
            }
        }

        // Update animation
        frameCounter++
        if (frameCounter >= frameDelay) {
            frameCounter = 0

            if (!isAnimationLocked) {
                currentFrame = (currentFrame + 1) % currentFrames.size
            } else {
                // Animation locked - tiến đến cuối
                if (currentFrame < currentFrames.size - 1) {
                    currentFrame++

                    // Spawn stone projectile at frame 3 (attack animation có 6 frames: 0-5)
                    if (state == State.ATTACK && currentFrame == 3) {
                        spawnProjectile()
                    }
                } else {
                    // Animation kết thúc
                    when (state) {
                        State.ATTACK -> {
                            currentFrame = 0
                            isAnimationLocked = false
                            attackCooldown = attackCooldownMax
                            setState(State.IDLE)
                        }
                        State.HURT -> {
                            currentFrame = 0
                            isAnimationLocked = false
                            setState(State.IDLE)
                        }
                        State.DEAD -> {
                            currentFrame = currentFrames.size - 1
                            // isDead đã được set = true trong takeDamage()
                        }
                        else -> {
                            currentFrame = 0
                        }
                    }
                }
            }
        }
    }

    private fun spawnProjectile() {
        // Spawn stone cách xa medusa và ở vị trí tay
        val offsetX = if (facingRight) 130f else -130f
        val projectileX = x + offsetX
        val projectileY = y + 10f // Hơi thấp xuống một chút

        val projectile =
            MedusaProjectile(context, projectileX, projectileY, targetX, targetY, projectileDamage)
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

        // Draw projectiles
        projectiles.forEach { it.draw(canvas) }

        // Draw damage texts
        damageTexts.forEach { it.draw(canvas) }

        // Draw health bar (chỉ khi bị thương)
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

        // Text hiển thị số máu
        val healthText = "$health/$maxHealth"
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 20f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
            setShadowLayer(2f, 1f, 1f, Color.BLACK)
        }
        canvas.drawText(healthText, x, barY + barHeight - 2f, textPaint)
    }

    private fun calculateDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x2 - x1
        val dy = y2 - y1
        return sqrt(dx * dx + dy * dy)
    }

    private fun setState(newState: State) {
        if (state == newState) return
        if (isAnimationLocked && newState != State.HURT && newState != State.DEAD) return

        state = newState
        currentFrame = 0

        when (state) {
            State.IDLE -> {
                currentFrames = idleFrames
                isAnimationLocked = false
            }
            State.WALK -> {
                currentFrames = walkFrames
                isAnimationLocked = false
            }
            State.ATTACK -> {
                currentFrames = attackFrames
                isAnimationLocked = true
            }
            State.HURT -> {
                currentFrames = hurtFrames
                isAnimationLocked = true
            }
            State.DEAD -> {
                currentFrames = deadFrames
                isAnimationLocked = true
            }
        }
    }

    fun takeDamage(damage: Int) {
        if (isDead || state == State.DEAD) return

        health -= damage
        damageTexts.add(DamageText(x + 100f, y - 50f, damage))

        if (health <= 0) {
            health = 0
            setState(State.DEAD)
            isDead = true  // Set isDead ngay lập tức khi chết
        } else {
            setState(State.HURT)
        }
    }

    fun getX(): Float = x

    fun isDead(): Boolean = isDead
    fun shouldRemove(): Boolean = isDead && deadTimer >= deadDuration
    fun getAttackRange(): Float = attackRange

    fun isCollidingWith(targetX: Float, targetY: Float, range: Float): Boolean {
        val dx = targetX - x
        val dy = targetY - y
        val distance = sqrt(dx * dx + dy * dy)
        return distance < range
    }
}