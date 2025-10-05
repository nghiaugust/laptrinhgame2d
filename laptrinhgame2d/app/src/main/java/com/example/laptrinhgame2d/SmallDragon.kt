package com.example.laptrinhgame2d

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import java.io.IOException
import kotlin.math.sqrt

class SmallDragon(private val context: Context, private var x: Float, var y: Float) {
    
    private val idleFrames = mutableListOf<Bitmap>()
    private val walkFrames = mutableListOf<Bitmap>()
    private val attackFrames = mutableListOf<Bitmap>()
    private val hurtFrames = mutableListOf<Bitmap>()
    private val deadFrames = mutableListOf<Bitmap>()
    
    private var currentFrames = mutableListOf<Bitmap>()
    private var currentFrame = 0
    private var frameCounter = 0
    private val frameDelay = 3
    
    private var state = State.IDLE
    private var facingRight = true
    private val speed = 2.5f
    
    private var isAnimationLocked = false
    private var isDead = false
    
    private var health = 120
    private val maxHealth = 120
    
    // Dead timer
    private var deadTimer = 0
    private val deadDuration = 300
    
    // AI behavior
    private var targetX = x
    private var targetY = y
    private var attackCooldown = 0
    private val attackCooldownMax = 80 // Cooldown giữa các lần bắn
    private val attackRange = 450f // Tầm bắn
    private val stopDistance = 200f // Dừng lại ở khoảng cách này để bắn
    private val detectionRange = 550f // Phát hiện player từ xa
    private val projectileDamage = 20
    
    // Projectiles (fireball)
    val projectiles = mutableListOf<SmallDragonProjectile>()
    
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
            // Load idle frames (3 frames: Idle1-3)
            for (i in 1..3) {
                val bitmap = loadBitmap("enemies/small_dragon/idle/Idle$i.png")
                idleFrames.add(bitmap)
            }
            
            // Load walk frames (4 frames: Walk1-4)
            for (i in 1..4) {
                val bitmap = loadBitmap("enemies/small_dragon/walk/Walk$i.png")
                walkFrames.add(bitmap)
            }
            
            // Load attack frames (3 frames: Attack1-3)
            for (i in 1..3) {
                val bitmap = loadBitmap("enemies/small_dragon/attack/Attack$i.png")
                attackFrames.add(bitmap)
            }
            
            // Load hurt frames (2 frames: Hurt1-2)
            for (i in 1..2) {
                val bitmap = loadBitmap("enemies/small_dragon/hurt/Hurt$i.png")
                hurtFrames.add(bitmap)
            }
            
            // Load dead frames (4 frames: Death1-4)
            for (i in 1..4) {
                val bitmap = loadBitmap("enemies/small_dragon/deadth/Death$i.png")
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
            // 1. Trong tầm bắn và cooldown hết - tấn công (attack animation + bắn fireball)
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
                
                val moveSpeed = 0.8f
                x += idleMovementDirection * moveSpeed
                facingRight = dx > 0
                setState(State.WALK)
            }
            
            // 3. Quá gần stopDistance - lùi lại để giữ khoảng cách bắn
            distance < stopDistance -> {
                // Lùi ra xa player
                val dirX = -dx / distance
                val dirY = -dy / distance
                
                facingRight = dx > 0
                
                x += dirX * speed * 0.5f
                y += dirY * speed * 0.5f
                
                setState(State.WALK)
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
                    
                    setState(State.WALK)
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
                
                val moveSpeed = 0.4f
                x += idleMovementDirection * moveSpeed
                facingRight = idleMovementDirection > 0
                
                if (originalIdleX == 0f) originalIdleX = x
                x = x.coerceIn(originalIdleX - idleMovementDistance, originalIdleX + idleMovementDistance)
                
                setState(State.WALK)
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
            
            // Spawn fireball ở frame 2 của attack animation (3 frames total)
            if (state == State.ATTACK && currentFrame == 2) {
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
        // Spawn fireball cách xa small dragon (150px) và thấp hơn để đồng bộ
        val offsetX = if (facingRight) 150f else -150f
        val projectileX = x + offsetX
        val projectileY = y + 20f // Điều chỉnh để đồng bộ với attack animation
        
        val projectile = SmallDragonProjectile(context, projectileX, projectileY, targetX, targetY, projectileDamage)
        projectiles.add(projectile)
    }
    
    fun draw(canvas: Canvas) {
        if (currentFrames.isEmpty()) return
        
        val bitmap = currentFrames[currentFrame]
        
        // Fade out effect khi chết
        val paint = if (isDead && deadTimer > deadDuration - 60) {
            val fadeProgress = (deadTimer - (deadDuration - 60)) / 60f
            android.graphics.Paint().apply {
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
            android.graphics.Paint().apply {
                color = android.graphics.Color.RED
                style = android.graphics.Paint.Style.FILL
            })
        
        // Health hiện tại (xanh lá)
        val healthWidth = barWidth * (health.toFloat() / maxHealth)
        canvas.drawRect(barX, barY, barX + healthWidth, barY + barHeight,
            android.graphics.Paint().apply {
                color = android.graphics.Color.GREEN
                style = android.graphics.Paint.Style.FILL
            })
        
        // Viền
        canvas.drawRect(barX, barY, barX + barWidth, barY + barHeight,
            android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 2f
            })
        
        // Text HP
        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 14f
            isFakeBoldText = true
            textAlign = android.graphics.Paint.Align.CENTER
            setShadowLayer(2f, 1f, 1f, android.graphics.Color.BLACK)
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
    
    // Va chạm (không cần cho SmallDragon vì chỉ tấn công tầm xa)
    fun isCollidingWith(otherX: Float, otherY: Float, range: Float): Boolean {
        val dx = otherX - x
        val dy = otherY - y
        val distance = sqrt(dx * dx + dy * dy)
        return distance < range
    }
}
