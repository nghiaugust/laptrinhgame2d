package com.example.laptrinhgame2d

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.random.Random

/**
 * 3 MAP SYSTEMS - Mỗi map có background system riêng hoàn toàn
 */

// ========== MAP 1: GRASSLAND (CỎ XANH - DỄ) - MAP GỐC ==========
class GrasslandMap(context: Context, screenWidth: Int, screenHeight: Int) {
    private val gameContext = context
    private val screenW = screenWidth
    private val screenH = screenHeight

    // Background layers cho Grassland
    private val skyLayer: BackgroundLayer
    private val mountainLayer: BackgroundLayer
    private val hillLayer: BackgroundLayer
    private val ground: Ground
    val groundY: Float

    // Decorations cho Grassland
    private val grassPaint = Paint().apply {
        color = Color.rgb(34, 139, 34)
        style = Paint.Style.FILL
    }

    private val flowerPaint = Paint().apply {
        color = Color.rgb(255, 215, 0)
        style = Paint.Style.FILL
    }

    init {
        groundY = screenHeight * 0.75f

        // Tạo backgrounds cho Grassland
        BackgroundGenerator.generateAllGrasslandBackgrounds(context, screenWidth, screenHeight)

        // Khởi tạo layers
        skyLayer = BackgroundLayer(
            context,
            "backgrounds/grassland_sky.png",
            scrollSpeed = 0.1f,
            screenHeight = screenHeight
        )

        mountainLayer = BackgroundLayer(
            context,
            "backgrounds/grassland_mountains.png",
            scrollSpeed = 0.3f,
            screenHeight = screenHeight
        )

        hillLayer = BackgroundLayer(
            context,
            "backgrounds/grassland_hills.png",
            scrollSpeed = 0.6f,
            screenHeight = screenHeight
        )

        ground = Ground(
            context,
            "backgrounds/grassland_ground.png",
            y = groundY,
            screenWidth = screenWidth,
            screenHeight = screenHeight
        )
    }

    fun update(cameraX: Float, cameraY: Float) {
        skyLayer.update(cameraX)
        mountainLayer.update(cameraX)
        hillLayer.update(cameraX)
        ground.update(cameraX)
    }

    fun draw(canvas: Canvas, cameraX: Float, cameraY: Float) {
        // 1. Vẽ background layers
        skyLayer.draw(canvas)
        mountainLayer.draw(canvas)
        hillLayer.draw(canvas)
        ground.draw(canvas, cameraX, cameraY)

        // 2. Vẽ decorations (cỏ + hoa)
        for (i in 0..20) {
            val x = (i * 150f) - (cameraX * 0.9f)
            val y = groundY - cameraY

            if (x > -100 && x < canvas.width + 100) {
                // Bụi cỏ xanh
                canvas.drawCircle(x, y - 8f, 12f, grassPaint)
                canvas.drawCircle(x - 8f, y - 5f, 10f, grassPaint)
                canvas.drawCircle(x + 8f, y - 5f, 10f, grassPaint)

                // Hoa vàng (mỗi 3 bụi cỏ có 1 hoa)
                if (i % 3 == 0) {
                    canvas.drawCircle(x + 15f, y - 3f, 6f, flowerPaint)
                }
            }
        }
    }

    fun cleanup() {
        skyLayer.recycle()
        mountainLayer.recycle()
        hillLayer.recycle()
        ground.recycle()
    }
}

// ========== MAP 2: DESERT (SA MẠC - TRUNG BÌNH) - HOÀN TOÀN MỚI ==========
class DesertMap(context: Context, screenWidth: Int, screenHeight: Int) {
    private val gameContext = context
    private val screenW = screenWidth
    private val screenH = screenHeight

    // Background layers cho Desert
    private val skyLayer: BackgroundLayer
    private val mountainLayer: BackgroundLayer
    private val hillLayer: BackgroundLayer
    private val ground: Ground
    val groundY: Float

    // Decorations cho Desert
    private val cactusPaint = Paint().apply {
        color = Color.rgb(34, 139, 34)
        style = Paint.Style.FILL
    }

    private val cactusFlowerPaint = Paint().apply {
        color = Color.rgb(255, 105, 180)
        style = Paint.Style.FILL
    }

    private val sandPaint = Paint().apply {
        color = Color.rgb(238, 203, 173)
        alpha = 120
        style = Paint.Style.FILL
    }

    private val rockPaint = Paint().apply {
        color = Color.rgb(139, 90, 43)
        style = Paint.Style.FILL
    }

    private val miragePaint = Paint().apply {
        color = Color.rgb(135, 206, 235)
        alpha = 40
        style = Paint.Style.FILL
    }

    init {
        groundY = screenHeight * 0.75f

        // Tạo backgrounds cho Desert
        BackgroundGenerator.generateAllDesertBackgrounds(context, screenWidth, screenHeight)

        // Khởi tạo layers với backgrounds sa mạc
        skyLayer = BackgroundLayer(
            context,
            "backgrounds/desert_sky.png",
            scrollSpeed = 0.1f,
            screenHeight = screenHeight
        )

        mountainLayer = BackgroundLayer(
            context,
            "backgrounds/desert_mountains.png",
            scrollSpeed = 0.3f,
            screenHeight = screenHeight
        )

        hillLayer = BackgroundLayer(
            context,
            "backgrounds/desert_hills.png",
            scrollSpeed = 0.6f,
            screenHeight = screenHeight
        )

        ground = Ground(
            context,
            "backgrounds/desert_ground.png",
            y = groundY,
            screenWidth = screenWidth,
            screenHeight = screenHeight
        )
    }

    fun update(cameraX: Float, cameraY: Float) {
        skyLayer.update(cameraX)
        mountainLayer.update(cameraX)
        hillLayer.update(cameraX)
        ground.update(cameraX)
    }

    fun draw(canvas: Canvas, cameraX: Float, cameraY: Float) {
        // 1. Vẽ background layers sa mạc
        skyLayer.draw(canvas)
        mountainLayer.draw(canvas)
        hillLayer.draw(canvas)
        ground.draw(canvas, cameraX, cameraY)

        // 2. Vẽ decorations sa mạc
        // Xương rồng
        for (i in 0..15) {
            val x = (i * 200f + 50f) - (cameraX * 0.9f)
            val y = groundY - cameraY

            if (x > -150 && x < canvas.width + 150) {
                // Thân xương rồng chính
                canvas.drawRect(x - 15f, y - 80f, x + 15f, y, cactusPaint)

                // Cành (ngẫu nhiên)
                if (i % 3 == 0) {
                    canvas.drawRect(x - 35f, y - 60f, x - 15f, y - 25f, cactusPaint)
                    canvas.drawCircle(x - 25f, y - 65f, 4f, cactusFlowerPaint)
                }

                if (i % 4 == 1) {
                    canvas.drawRect(x + 15f, y - 55f, x + 35f, y - 20f, cactusPaint)
                    canvas.drawCircle(x + 25f, y - 60f, 4f, cactusFlowerPaint)
                }

                if (i % 5 == 2) {
                    canvas.drawCircle(x, y - 85f, 5f, cactusFlowerPaint)
                }
            }
        }

        // Đá sa mạc
        for (i in 0..12) {
            val x = (i * 250f + 120f) - (cameraX * 0.85f)
            val y = groundY - 25f - cameraY

            if (x > -80 && x < canvas.width + 80) {
                canvas.drawCircle(x, y, 20f, rockPaint)
                canvas.drawCircle(x - 15f, y + 8f, 12f, rockPaint)
                canvas.drawCircle(x + 18f, y + 5f, 15f, rockPaint)
            }
        }

        // Cát bay (animated)
        val windOffset = (System.currentTimeMillis() / 50 % 400).toFloat()
        for (i in 0..8) {
            val x = (i * 300f + windOffset) - (cameraX * 0.4f)
            val y = groundY - 120f - cameraY + (kotlin.math.sin((System.currentTimeMillis() / 1000f + i) * 2f) * 10f)

            if (x > -100 && x < canvas.width + 100) {
                canvas.drawCircle(x, y, 6f, sandPaint)
                canvas.drawCircle(x + 20f, y + 8f, 4f, sandPaint)
                canvas.drawCircle(x - 15f, y - 5f, 5f, sandPaint)
            }
        }

        // Mirages (ảo ảnh)
        val mirageOffset = (System.currentTimeMillis() / 100 % 800).toFloat()
        for (i in 0..3) {
            val x = (i * 600f + mirageOffset) - (cameraX * 0.2f)
            val y = groundY - 30f - cameraY

            if (x > -200 && x < canvas.width + 200) {
                canvas.drawOval(x - 80f, y - 15f, x + 80f, y + 15f, miragePaint)
            }
        }
    }

    fun cleanup() {
        skyLayer.recycle()
        mountainLayer.recycle()
        hillLayer.recycle()
        ground.recycle()
    }
}

// ========== MAP 3: VOLCANO (NÚI LỬA - KHÓ) - HOÀN TOÀN MỚI ==========
class VolcanoMap(context: Context, screenWidth: Int, screenHeight: Int) {
    private val gameContext = context
    private val screenW = screenWidth
    private val screenH = screenHeight

    // Background layers cho Volcano
    private val skyLayer: BackgroundLayer
    private val mountainLayer: BackgroundLayer
    private val hillLayer: BackgroundLayer
    private val ground: Ground
    val groundY: Float

    // Decorations cho Volcano
    private val lavaPaint = Paint().apply {
        color = Color.rgb(255, 69, 0)
        style = Paint.Style.FILL
    }

    private val lavaGlowPaint = Paint().apply {
        color = Color.rgb(255, 140, 0)
        alpha = 150
        style = Paint.Style.FILL
    }

    private val volcanicRockPaint = Paint().apply {
        color = Color.rgb(64, 64, 64)
        style = Paint.Style.FILL
    }

    private val darkRockPaint = Paint().apply {
        color = Color.rgb(32, 32, 32)
        style = Paint.Style.FILL
    }

    private val smokePaint = Paint().apply {
        color = Color.rgb(80, 80, 80)
        alpha = 120
        style = Paint.Style.FILL
    }

    private val emberPaint = Paint().apply {
        color = Color.rgb(255, 165, 0)
        style = Paint.Style.FILL
    }

    // Particles
    private val lavaParticles = mutableListOf<LavaParticle>()
    private var particleTimer = 0

    init {
        groundY = screenHeight * 0.75f

        // Tạo backgrounds cho Volcano
        BackgroundGenerator.generateAllVolcanoBackgrounds(context, screenWidth, screenHeight)

        // Khởi tạo layers với backgrounds núi lửa
        skyLayer = BackgroundLayer(
            context,
            "backgrounds/volcano_sky.png",
            scrollSpeed = 0.1f,
            screenHeight = screenHeight
        )

        mountainLayer = BackgroundLayer(
            context,
            "backgrounds/volcano_mountains.png",
            scrollSpeed = 0.3f,
            screenHeight = screenHeight
        )

        hillLayer = BackgroundLayer(
            context,
            "backgrounds/volcano_hills.png",
            scrollSpeed = 0.6f,
            screenHeight = screenHeight
        )

        ground = Ground(
            context,
            "backgrounds/volcano_ground.png",
            y = groundY,
            screenWidth = screenWidth,
            screenHeight = screenHeight
        )
    }

    fun update(cameraX: Float, cameraY: Float) {
        skyLayer.update(cameraX)
        mountainLayer.update(cameraX)
        hillLayer.update(cameraX)
        ground.update(cameraX)

        // Update lava particles
        particleTimer++
        if (particleTimer >= 15) {
            particleTimer = 0
            val x = Random.nextFloat() * 2000f
            val y = groundY - Random.nextFloat() * 100f
            lavaParticles.add(LavaParticle(x, y))
        }

        lavaParticles.forEach { it.update() }
        lavaParticles.removeAll { it.isDead() }
    }

    fun draw(canvas: Canvas, cameraX: Float, cameraY: Float) {
        // 1. Vẽ background layers núi lửa
        skyLayer.draw(canvas)
        mountainLayer.draw(canvas)
        hillLayer.draw(canvas)
        ground.draw(canvas, cameraX, cameraY)

        // 2. Vẽ sông dung nham dưới ground
        val lavaY = groundY + 60f - cameraY
        canvas.drawRect(0f, lavaY, canvas.width.toFloat(), canvas.height.toFloat(), lavaPaint)

        // Hiệu ứng sóng dung nham
        for (i in 0..10) {
            val x = i * 100f - (cameraX * 1.1f)
            val waveY = lavaY + kotlin.math.sin((System.currentTimeMillis() / 300f + i) * 2f) * 8f
            canvas.drawOval(x - 25f, waveY - 10f, x + 25f, waveY + 10f, lavaGlowPaint)
        }

        // 3. Vẽ đá núi lửa
        for (i in 0..18) {
            val x = (i * 180f + 40f) - (cameraX * 0.9f)
            val y = groundY - 35f - cameraY

            if (x > -120 && x < canvas.width + 120) {
                canvas.drawCircle(x, y, 25f, volcanicRockPaint)
                canvas.drawCircle(x - 8f, y - 5f, 18f, darkRockPaint)
                canvas.drawCircle(x + 8f, y + 3f, 20f, volcanicRockPaint)

                if (i % 4 == 0) {
                    val emberY = y - 35f + kotlin.math.sin((System.currentTimeMillis() / 200f + i) * 3f) * 5f
                    canvas.drawCircle(x, emberY, 3f, emberPaint)
                }
            }
        }

        // 4. Vẽ núi lửa nhỏ
        for (i in 0..8) {
            val x = (i * 400f + 200f) - (cameraX * 0.8f)
            val y = groundY - 80f - cameraY

            if (x > -150 && x < canvas.width + 150) {
                canvas.drawCircle(x, y, 30f, darkRockPaint)
                canvas.drawCircle(x, y, 20f, lavaPaint)
                canvas.drawCircle(x, y, 15f, lavaGlowPaint)
            }
        }

        // 5. Vẽ khói bốc lên
        val smokeOffset = (System.currentTimeMillis() / 60 % 100).toFloat()
        for (i in 0..6) {
            val x = (i * 350f + 150f) - (cameraX * 0.3f)
            val baseY = groundY - 180f - cameraY

            if (x > -150 && x < canvas.width + 150) {
                for (j in 0..3) {
                    val smokeY = baseY - (j * 40f) - smokeOffset
                    val smokeSize = 40f - (j * 5f)
                    canvas.drawCircle(x + Random.nextFloat() * 20f - 10f, smokeY, smokeSize, smokePaint)
                }
            }
        }

        // 6. Vẽ lava particles
        canvas.save()
        canvas.translate(-cameraX, -cameraY)
        lavaParticles.forEach { it.draw(canvas) }
        canvas.restore()

        // 7. Vẽ tia lửa bay
        val emberOffset = (System.currentTimeMillis() / 100 % 300).toFloat()
        for (i in 0..5) {
            val x = (i * 400f + emberOffset) - (cameraX * 0.6f)
            val y = groundY - 200f - cameraY + kotlin.math.sin((System.currentTimeMillis() / 400f + i) * 2f) * 20f

            if (x > -100 && x < canvas.width + 100) {
                canvas.drawCircle(x, y, 4f, emberPaint)
                canvas.drawCircle(x - 8f, y + 3f, 2f, lavaGlowPaint)
            }
        }
    }

    fun cleanup() {
        skyLayer.recycle()
        mountainLayer.recycle()
        hillLayer.recycle()
        ground.recycle()
    }

    // Inner class cho lava particles
    private class LavaParticle(var x: Float, var y: Float) {
        private var velocityX = (Random.nextFloat() - 0.5f) * 2f
        private var velocityY = -Random.nextFloat() * 4f - 2f
        private var life = 80
        private val maxLife = 80

        fun update() {
            x += velocityX
            y += velocityY
            velocityY += 0.15f // gravity
            life--
        }

        fun isDead() = life <= 0

        fun draw(canvas: Canvas) {
            val alpha = (life.toFloat() / maxLife * 255).toInt().coerceIn(0, 255)
            val paint = Paint().apply {
                color = if (life > maxLife / 2) {
                    Color.rgb(255, 165, 0) // Vàng cam
                } else {
                    Color.rgb(255, 69, 0) // Đỏ cam
                }
                this.alpha = alpha
                style = Paint.Style.FILL
            }
            val size = (life.toFloat() / maxLife * 6f + 2f)
            canvas.drawCircle(x, y, size, paint)
        }
    }
}