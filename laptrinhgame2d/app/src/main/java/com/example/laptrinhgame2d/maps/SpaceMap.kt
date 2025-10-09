package com.example.laptrinhgame2d.maps

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.laptrinhgame2d.maps.BackgroundGenerator
import com.example.laptrinhgame2d.maps.BackgroundLayer
import com.example.laptrinhgame2d.maps.Ground
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * MAP 5: SPACE STATION - Trạm vũ trụ
 * Sử dụng hệ thống BackgroundLayer + Ground như maps khác
 */
class SpaceMap(context: Context, screenWidth: Int, screenHeight: Int) {
    private val gameContext = context
    private val screenW = screenWidth
    private val screenH = screenHeight

    // Background layers cho Space Station
    private val skyLayer: BackgroundLayer
    private val mountainLayer: BackgroundLayer
    private val hillLayer: BackgroundLayer
    private val ground: Ground
    val groundY: Float

    // Decorations cho Space Station
    private val starList = mutableListOf<Star>()
    private val planetList = mutableListOf<Planet>()
    private var animationFrame = 0

    private val starPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val structurePaint = Paint().apply {
        color = Color.parseColor("#4A5568")
        style = Paint.Style.FILL
    }

    private val techPaint = Paint().apply {
        color = Color.parseColor("#00D9FF")
        style = Paint.Style.FILL
    }

    private val energyPaint = Paint().apply {
        color = Color.parseColor("#FF0080")
        style = Paint.Style.FILL
    }

    data class Star(
        val x: Float,
        val y: Float,
        val brightness: Float,
        val twinkleSpeed: Float
    )

    data class Planet(
        val x: Float,
        val y: Float,
        val size: Float,
        val color: Int
    )

    init {
        groundY = screenHeight * 0.75f

        // Tạo backgrounds cho Space Station
        BackgroundGenerator.generateAllSpaceBackgrounds(context, screenWidth, screenHeight)

        // Khởi tạo layers với backgrounds vũ trụ
        skyLayer = BackgroundLayer(
            context,
            "backgrounds/space_sky.png",
            scrollSpeed = 0.1f,
            screenHeight = screenHeight
        )

        mountainLayer = BackgroundLayer(
            context,
            "backgrounds/space_mountains.png",
            scrollSpeed = 0.3f,
            screenHeight = screenHeight
        )

        hillLayer = BackgroundLayer(
            context,
            "backgrounds/space_hills.png",
            scrollSpeed = 0.6f,
            screenHeight = screenHeight
        )

        ground = Ground(
            context,
            "backgrounds/space_ground.png",
            y = groundY,
            screenWidth = screenWidth,
            screenHeight = screenHeight
        )

        // Tạo stars cho hiệu ứng lấp lánh
        repeat(120) {
            starList.add(
                Star(
                    x = Random.nextFloat() * screenWidth * 4,
                    y = Random.nextFloat() * screenHeight * 0.7f,
                    brightness = Random.nextFloat() * 0.8f + 0.2f,
                    twinkleSpeed = Random.nextFloat() * 0.08f + 0.02f
                )
            )
        }

        // Tạo planets ở xa
        planetList.add(Planet(600f, 120f, 70f, Color.parseColor("#FF6B6B")))   // Red planet
        planetList.add(Planet(1400f, 180f, 100f, Color.parseColor("#4ECDC4"))) // Cyan planet
        planetList.add(Planet(2200f, 90f, 55f, Color.parseColor("#FFE66D")))   // Yellow planet
        planetList.add(Planet(3000f, 150f, 85f, Color.parseColor("#95E1D3")))  // Green planet
    }

    fun update(cameraX: Float, cameraY: Float) {
        skyLayer.update(cameraX)
        mountainLayer.update(cameraX)
        hillLayer.update(cameraX)
        ground.update(cameraX)

        animationFrame++
    }

    fun draw(canvas: Canvas, cameraX: Float, cameraY: Float) {
        // 1. Vẽ background layers
        skyLayer.draw(canvas)
        mountainLayer.draw(canvas)
        hillLayer.draw(canvas)
        ground.draw(canvas, cameraX, cameraY)

        // 2. Vẽ stars lấp lánh
        starList.forEach { star ->
            val screenX = star.x - cameraX * 0.2f // Parallax nhẹ
            if (screenX > -10 && screenX < screenW + 10) {
                val twinkle = sin(animationFrame * star.twinkleSpeed) * 0.4f + 0.6f
                val alpha = (star.brightness * twinkle * 255).toInt()

                starPaint.alpha = alpha
                canvas.drawCircle(screenX, star.y, 2.5f, starPaint)
            }
        }

        // 3. Vẽ planets ở xa
        planetList.forEach { planet ->
            val screenX = planet.x - cameraX * 0.1f // Parallax chậm cho objects xa
            if (screenX > -planet.size && screenX < screenW + planet.size) {
                val planetPaint = Paint().apply {
                    color = planet.color
                    style = Paint.Style.FILL
                }
                canvas.drawCircle(screenX, planet.y, planet.size, planetPaint)

                // Highlight
                val highlightPaint = Paint().apply {
                    color = Color.WHITE
                    alpha = 120
                    style = Paint.Style.FILL
                }
                canvas.drawCircle(screenX - planet.size/3, planet.y - planet.size/3, planet.size/2, highlightPaint)
            }
        }

        // 4. Vẽ decorations (space structures)
        drawSpaceDecorations(canvas, cameraX, cameraY)
    }

    private fun drawSpaceDecorations(canvas: Canvas, cameraX: Float, cameraY: Float) {
        // Vẽ space towers/antennas
        for (i in 0..15) {
            val towerX = (i * 350f + 200f) - (cameraX * 0.9f)
            val towerY = groundY - cameraY

            if (towerX > -150 && towerX < screenW + 150) {
                // Tower base
                canvas.drawRect(towerX - 18f, towerY - 100f, towerX + 18f, towerY, structurePaint)

                // Tower top
                val topPaint = Paint().apply {
                    color = Color.parseColor("#2D3748")
                    style = Paint.Style.FILL
                }
                canvas.drawRect(towerX - 28f, towerY - 150f, towerX + 28f, towerY - 100f, topPaint)

                // Antenna
                val antennaPaint = Paint().apply {
                    color = techPaint.color
                    strokeWidth = 5f
                    style = Paint.Style.STROKE
                }
                canvas.drawLine(towerX, towerY - 150f, towerX, towerY - 220f, antennaPaint)

                // Blinking energy orb
                val blinkPhase = (animationFrame + i * 40) % 150
                if (blinkPhase < 75) {
                    val energySize = 8f + sin(animationFrame * 0.1f + i) * 3f
                    canvas.drawCircle(towerX, towerY - 225f, energySize, energyPaint)
                }
            }
        }

        // Vẽ floating platforms
        for (i in 0..12) {
            val platformX = (i * 400f + 300f) - (cameraX * 0.8f)
            val platformY = groundY - 180f - cameraY + sin((System.currentTimeMillis() / 2000f + i) * 2f) * 20f

            if (platformX > -120 && platformX < screenW + 120) {
                // Platform base
                val platformPaint = Paint().apply {
                    color = Color.parseColor("#34495E")
                    style = Paint.Style.FILL
                }
                canvas.drawRect(platformX - 60f, platformY - 15f, platformX + 60f, platformY + 15f, platformPaint)

                // Energy glow underneath
                val glowPaint = Paint().apply {
                    color = techPaint.color
                    alpha = 150
                    style = Paint.Style.FILL
                }
                canvas.drawCircle(platformX, platformY + 25f, 25f, glowPaint)
            }
        }

        // Vẽ energy streams
        val streamPaint = Paint().apply {
            color = techPaint.color
            alpha = 120
            strokeWidth = 4f
            style = Paint.Style.STROKE
        }

        val streamOffset = (System.currentTimeMillis() / 100 % 300).toFloat()
        for (i in 0..6) {
            val streamX = (i * 500f + streamOffset) - (cameraX * 0.4f)
            val streamStartY = groundY - 300f - cameraY
            val streamEndY = groundY - 50f - cameraY

            if (streamX > -50 && streamX < screenW + 50) {
                // Vertical energy stream
                canvas.drawLine(streamX, streamStartY, streamX, streamEndY, streamPaint)

                // Energy particles
                for (j in 0..5) {
                    val particleY = streamStartY + (streamEndY - streamStartY) * (j / 5f) +
                            sin((System.currentTimeMillis() / 200f + i + j) * 3f) * 10f
                    canvas.drawCircle(streamX, particleY, 3f, energyPaint)
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