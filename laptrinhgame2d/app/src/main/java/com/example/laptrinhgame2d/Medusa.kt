package com.example.laptrinhgame2d

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import java.io.IOException
import kotlin.math.sqrt
import kotlin.math.abs

class Medusa(private val context: Context, private var x: Float, private var y: Float) {
    
    private val idleFrames = mutableListOf<Bitmap>()
    private val walkFrames = mutableListOf<Bitmap>()
    private val attackFrames = mutableListOf<Bitmap>()
    private val skillFrames = mutableListOf<Bitmap>()
    private val hurtFrames = mutableListOf<Bitmap>()
    private val deadFrames = mutableListOf<Bitmap>()
    
    private var currentFrames = mutableListOf<Bitmap>()
    private var currentFrame = 0
    private var frameCounter = 0
    private val frameDelay = 5
    
    private var state = State.IDLE
    private var facingRight = true
    private val speed = 5f // Nhanh hơn để chạy trốn
    
    private var isAnimationLocked = false
    private var isDead = false
    
    // Health system
    private var health = 60 // Ít máu vì là ranged enemy
    private val maxHealth = 60
    
    // Dead timer
    private var deadTimer = 0
    private val deadDuration = 300 // 5 seconds
    
    // AI behavior - kiting style (chạy trốn và ném đá)
    private var targetX = x
    private var targetY = y
    private var attackCooldown = 0
    private val attackCooldownMax = 50 // 0.83 giây - tấn công nhanh
    private var skillCooldown = 0
    private val skillCooldownMax = 120 // 2 giây - dùng skill
    
    private val attackRange = 300f // Tầm đánh xa
    private val skillRange = 400f // Tầm skill xa hơn
    private val retreatDistance = 200f // Khoảng cách bắt đầu chạy trốn
    private val safeDistance = 350f // Khoảng cách an toàn
    private val detectionRange = 600f // Phát hiện rất xa
    
    private val attackDamage = 15 // Damage attack thường
    private val skillDamage = 25 // Damage skill mạnh hơn
    
    private var hasDealtDamageThisAttack = false
    
    // Projectiles
    val stones = mutableListOf<Stone>()
    
    // Target position (để bắn đá về phía player)
    private var targetPlayerX = 0f
    private var targetPlayerY = 0f
    
    // Idle movement
    private var idleMovementTimer = 0
    private var idleMovementDirection = 1f
    private val idleMovementDistance = 30f
    private var originalIdleX = x
    
    // Damage texts
    private val damageTexts = mutableListOf<DamageText>()
    
    enum class State {
        IDLE, WALK, ATTACK, SKILL, HURT, DEAD
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
            
            // Skill sử dụng attack animation (không có animation riêng)
            // Stone1-8 trong folder skill là animation của viên đá bay, không phải của Medusa
            skillFrames.addAll(attackFrames)
            
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
        if (skillCooldown > 0) skillCooldown--
        
        // Update stones
        stones.removeAll { it.isDead() }
        stones.forEach { it.update() }
        
        // Update damage texts
        damageTexts.forEach { it.update() }
        damageTexts.removeAll { it.isFinished() }
        
        if (playerIsDead) {
            setState(State.IDLE)
            return
        }
        
        // Lưu vị trí player để bắn đá
        targetPlayerX = playerX
        targetPlayerY = playerY
        
        val distance = calculateDistance(x, y, playerX, playerY)
        
        // AI behavior: Kiting style
        when {
            // Chết
            health <= 0 -> {
                if (state != State.DEAD) {
                    setState(State.DEAD)
                }
            }
            
            // Dùng skill nếu có thể
            distance <= skillRange && skillCooldown == 0 && state != State.SKILL && state != State.HURT -> {
                facingRight = playerX > x
                setState(State.SKILL)
                hasDealtDamageThisAttack = false
            }
            
            // Tấn công thường nếu có thể
            distance <= attackRange && attackCooldown == 0 && skillCooldown > 60 && state != State.ATTACK && state != State.HURT -> {
                facingRight = playerX > x
                setState(State.ATTACK)
                hasDealtDamageThisAttack = false
            }
            
            // Chạy trốn nếu player đến quá gần
            distance < retreatDistance && state != State.ATTACK && state != State.SKILL && state != State.HURT -> {
                // Chạy ra xa player
                val dirX = x - playerX
                val dirY = y - playerY
                val length = sqrt(dirX * dirX + dirY * dirY)
                if (length > 0) {
                    x += (dirX / length) * speed
                    facingRight = dirX < 0 // Quay lưng với player khi chạy trốn
                }
                setState(State.WALK)
            }
            
            // Giữ khoảng cách an toàn
            distance < safeDistance && distance >= retreatDistance && state != State.ATTACK && state != State.SKILL && state != State.HURT -> {
                setState(State.IDLE)
                facingRight = playerX > x
            }
            
            // Di chuyển lại gần player để tấn công
            distance >= safeDistance && distance < detectionRange && state != State.ATTACK && state != State.SKILL && state != State.HURT -> {
                val dirX = playerX - x
                val dirY = playerY - y
                val length = sqrt(dirX * dirX + dirY * dirY)
                if (length > 0) {
                    x += (dirX / length) * speed
                    facingRight = dirX > 0
                }
                setState(State.WALK)
            }
            
            // Idle patrol khi player xa
            distance >= detectionRange && state != State.ATTACK && state != State.SKILL && state != State.HURT -> {
                idleMovementTimer++
                if (idleMovementTimer >= 120) {
                    idleMovementTimer = 0
                    idleMovementDirection *= -1
                    originalIdleX = x
                }
                
                val targetIdleX = originalIdleX + (idleMovementDirection * idleMovementDistance)
                if (abs(x - targetIdleX) > 2f) {
                    x += idleMovementDirection * (speed * 0.5f)
                    facingRight = idleMovementDirection > 0
                    setState(State.WALK)
                } else {
                    setState(State.IDLE)
                }
            }
        }
        
        // Update animation
        updateAnimation()
    }
    
    private fun updateAnimation() {
        if (isAnimationLocked) {
            frameCounter++
            if (frameCounter >= frameDelay) {
                frameCounter = 0
                currentFrame++
                
                if (currentFrame >= currentFrames.size) {
                    when (state) {
                        State.ATTACK -> {
                            currentFrame = 0
                            isAnimationLocked = false
                            attackCooldown = attackCooldownMax
                            setState(State.IDLE)
                        }
                        State.SKILL -> {
                            currentFrame = 0
                            isAnimationLocked = false
                            skillCooldown = skillCooldownMax
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
                } else {
                    // Spawn projectile at specific frames
                    if (state == State.ATTACK && currentFrame == 3 && !hasDealtDamageThisAttack) {
                        spawnStone(false) // Attack stone
                        hasDealtDamageThisAttack = true
                    } else if (state == State.SKILL && currentFrame == 4 && !hasDealtDamageThisAttack) {
                        spawnStone(true) // Skill stone
                        hasDealtDamageThisAttack = true
                    }
                }
            }
        } else {
            frameCounter++
            if (frameCounter >= frameDelay) {
                frameCounter = 0
                currentFrame = (currentFrame + 1) % currentFrames.size
            }
        }
    }
    
    private fun spawnStone(isSkill: Boolean) {
        // Spawn đá từ vị trí tâm sprite (x, y)
        val stoneX = if (facingRight) x + 50f else x - 50f
        val stoneY = y
        
        // Tính hướng bắn về phía player
        val dirX = targetPlayerX - stoneX
        val dirY = targetPlayerY - stoneY
        val length = sqrt(dirX * dirX + dirY * dirY)
        
        val stoneSpeed = 15f
        val velocityX = if (length > 0) (dirX / length) * stoneSpeed else (if (facingRight) stoneSpeed else -stoneSpeed)
        val velocityY = if (length > 0) (dirY / length) * stoneSpeed else 0f
        
        val damage = if (isSkill) skillDamage else attackDamage
        
        stones.add(Stone(context, stoneX, stoneY, velocityX, velocityY, damage))
    }
    
    private fun setState(newState: State) {
        // Không reset animation nếu đang ở cùng state IDLE hoặc WALK
        if (state == newState && (state == State.IDLE || state == State.WALK)) return
        
        // Không thể chuyển state khi đang trong animation bị khóa
        if (isAnimationLocked && newState != State.DEAD) return
        
        state = newState
        currentFrame = 0
        frameCounter = 0
        
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
            State.SKILL -> {
                currentFrames = skillFrames
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
        } else if (state != State.ATTACK && state != State.SKILL) {
            setState(State.HURT)
        }
    }
    
    fun draw(canvas: Canvas, cameraX: Float, cameraY: Float) {
        if (currentFrames.isEmpty()) return
        
        val frame = currentFrames[currentFrame]
        
        // Vẽ sprite với tâm tại (x, y) - giống như Skeleton
        val matrix = Matrix()
        
        val scaledWidth = 200f
        val scaledHeight = 200f
        
        // Scale sprite
        matrix.postScale(
            if (facingRight) scaledWidth / frame.width else -scaledWidth / frame.width,
            scaledHeight / frame.height
        )
        
        // Translate để tâm sprite nằm tại (x, y)
        matrix.postTranslate(
            if (facingRight) x - scaledWidth / 2 - cameraX else x + scaledWidth / 2 - cameraX,
            y - scaledHeight / 2 - cameraY
        )
        
        canvas.drawBitmap(frame, matrix, null)
        
        // Draw health bar nếu chưa chết
        if (!isDead) {
            drawHealthBar(canvas, cameraX, cameraY)
        }
        
        // Draw stones
        stones.forEach { it.draw(canvas, cameraX, cameraY) }
        
        // Draw damage texts (in world space, so need to adjust for camera)
        canvas.save()
        canvas.translate(-cameraX, -cameraY)
        damageTexts.forEach { it.draw(canvas) }
        canvas.restore()
    }
    
    private fun drawHealthBar(canvas: Canvas, cameraX: Float, cameraY: Float) {
        val barWidth = 120f
        val barHeight = 15f
        val barX = x - barWidth / 2 - cameraX
        val barY = y - 150f - cameraY // Vị trí cố định như Skeleton
        
        // Vẽ background (đỏ)
        canvas.drawRect(barX, barY, barX + barWidth, barY + barHeight,
            android.graphics.Paint().apply {
                color = android.graphics.Color.RED
                style = android.graphics.Paint.Style.FILL
            })
        
        // Vẽ health hiện tại (xanh lá)
        val healthWidth = barWidth * (health.toFloat() / maxHealth)
        canvas.drawRect(barX, barY, barX + healthWidth, barY + barHeight,
            android.graphics.Paint().apply {
                color = android.graphics.Color.GREEN
                style = android.graphics.Paint.Style.FILL
            })
        
        // Vẽ viền
        canvas.drawRect(barX, barY, barX + barWidth, barY + barHeight,
            android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 2f
            })
        
        // Vẽ text HP
        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 14f
            isFakeBoldText = true
            textAlign = android.graphics.Paint.Align.CENTER
            setShadowLayer(2f, 1f, 1f, android.graphics.Color.BLACK)
        }
        canvas.drawText("$health/$maxHealth", x - cameraX, barY + 11, textPaint)
    }
    
    private fun calculateDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x2 - x1
        val dy = y2 - y1
        return sqrt(dx * dx + dy * dy)
    }
    
    fun getX(): Float = x // Tâm sprite tại x, y - giống Skeleton
    fun getY(): Float = y
    fun setY(newY: Float) { y = newY }
    fun isDead(): Boolean = isDead
    fun shouldRemove(): Boolean = isDead && deadTimer >= deadDuration
    fun getAttackRange(): Float = attackRange
    
    fun isCollidingWith(targetX: Float, targetY: Float, range: Float): Boolean {
        // Collision box ở tâm (x, y) - giống Skeleton
        val dx = targetX - x
        val dy = targetY - y
        val distance = sqrt(dx * dx + dy * dy)
        return distance < range
    }
}
