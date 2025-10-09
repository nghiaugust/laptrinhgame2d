package com.example.laptrinhgame2d.enemies

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import com.example.laptrinhgame2d.DamageText
import com.example.laptrinhgame2d.enemies.DragonFire
import java.io.IOException
import kotlin.math.abs
import kotlin.math.sqrt

class Dragon(private val context: Context, private var x: Float, var y: Float) {

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
    private val speed = 3f

    private var isAnimationLocked = false
    private var isDead = false

    private var health = 180
    private val maxHealth = 180

    // Dead timer
    private var deadTimer = 0
    private val deadDuration = 120

    // AI behavior
    private var targetX = x
    private var targetY = y
    private var attackCooldown = 0
    private val attackCooldownMax = 70 // Cooldown giữa các lần phun lửa
    private val attackRange = 280f // Tầm phun lửa (cận chiến)
    private val stopDistance = 200f // Dừng lại ở khoảng cách này để tấn công
    private val detectionRange = 450f // Phát hiện player
    private val fireDamage = 30 // Damage của lửa

    // Fire projectiles (phun lửa tầm ngắn)
    val fireProjectiles = mutableListOf<DragonFire>()

    // Idle movement
    private var idleMovementTimer = 0
    private var idleMovementDirection = 1f
    private val idleMovementDistance = 40f
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
                val bitmap = loadBitmap("enemies/dragon/idle/Idle$i.png")
                idleFrames.add(bitmap)
            }

            // Load walk frames (5 frames)
            for (i in 1..5) {
                val bitmap = loadBitmap("enemies/dragon/walk/Walk$i.png")
                walkFrames.add(bitmap)
            }

            // Load attack frames (4 frames) - phun lửa
            for (i in 1..4) {
                val bitmap = loadBitmap("enemies/dragon/attack/Attack$i.png")
                attackFrames.add(bitmap)
            }

            // Load hurt frames - sử dụng idle frames vì không có hurt animation
            hurtFrames.addAll(idleFrames)

            // Load dead frames (5 frames)
            for (i in 1..5) {
                val bitmap = loadBitmap("enemies/dragon/deadth/Death$i.png")
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

    fun update(playerX: Float, playerY: Float) {
        if (isDead) {
            deadTimer++
            return
        }

        // Update cooldowns
        if (attackCooldown > 0) attackCooldown--

        // Update fire projectiles
        fireProjectiles.removeAll { it.isDead() }
        fireProjectiles.forEach { it.update() }

        // Update damage texts
        damageTexts.forEach { it.update() }
        damageTexts.removeAll { it.isFinished() }

        // Lưu vị trí player để phun lửa
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

            // Tấn công (phun lửa) nếu trong tầm
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

                    // Spawn fire projectile at frame 2 (attack animation có 4 frames: 0-3)
                    if (state == State.ATTACK && currentFrame == 2) {
                        spawnFire()
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
                            isDead = true
                        }
                        else -> {
                            currentFrame = 0
                        }
                    }
                }
            }
        }
    }

    private fun spawnFire() {
        // Spawn lửa từ miệng rồng (gần hơn vì là cận chiến)
        val offsetX = if (facingRight) 80f else -80f
        val fireX = x + offsetX
        val fireY = y - 10f // Hơi cao lên một chút (miệng rồng)

        val fire = DragonFire(context, fireX, fireY, targetX, targetY, fireDamage, facingRight)
        fireProjectiles.add(fire)
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

        // Draw fire projectiles
        fireProjectiles.forEach { it.draw(canvas) }

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
        } else {
            setState(State.HURT)
        }
    }

    fun getX(): Float = x

    fun isDead(): Boolean = isDead
    fun shouldBeRemoved(): Boolean = isDead && deadTimer >= deadDuration
    fun getAttackRange(): Float = attackRange

    fun isCollidingWith(targetX: Float, targetY: Float, range: Float): Boolean {
        val dx = targetX - x
        val dy = targetY - y
        val distance = sqrt(dx * dx + dy * dy)
        return distance < range
    }
}