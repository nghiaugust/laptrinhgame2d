package com.example.laptrinhgame2d.maps

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.laptrinhgame2d.maps.BackgroundGenerator
import com.example.laptrinhgame2d.maps.BackgroundLayer
import com.example.laptrinhgame2d.maps.Ground
import kotlin.math.sin
import kotlin.random.Random

/**
 * MAP 4: ICE WORLD - Thế giới băng tuyết
 * Sử dụng hệ thống BackgroundLayer + Ground như maps khác
 */
class IceMap(context: Context, screenWidth: Int, screenHeight: Int) {
    private val gameContext = context
    private val screenW = screenWidth
    private val screenH = screenHeight

    // Background layers cho Ice World
    private val skyLayer: BackgroundLayer
    private val mountainLayer: BackgroundLayer
    private val hillLayer: BackgroundLayer
    private val ground: Ground
    val groundY: Float

    // Decorations cho Ice World
    private val snowflakeList = mutableListOf<Snowflake>()
    private var animationFrame = 0

    private val snowflakePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val iceDecorationPaint = Paint().apply {
        color = Color.parseColor("#A5F3FC")
        style = Paint.Style.FILL
    }

    private val treePaint = Paint().apply {
        color = Color.parseColor("#065F46")
        style = Paint.Style.FILL
    }

    private val trunkPaint = Paint().apply {
        color = Color.parseColor("#92400E")
        style = Paint.Style.FILL
    }

    data class Snowflake(
        var x: Float,
        var y: Float,
        val speed: Float,
        val size: Float
    )

    init {
        groundY = screenHeight * 0.75f

        // Tạo backgrounds cho Ice World
        BackgroundGenerator.generateAllIceBackgrounds(context, screenWidth, screenHeight)

        // Khởi tạo layers với backgrounds băng tuyết
        skyLayer = BackgroundLayer(
            context,
            "backgrounds/ice_sky.png",
            scrollSpeed = 0.1f,
            screenHeight = screenHeight
        )

        mountainLayer = BackgroundLayer(
            context,
            "backgrounds/ice_mountains.png",
            scrollSpeed = 0.3f,
            screenHeight = screenHeight
        )

        hillLayer = BackgroundLayer(
            context,
            "backgrounds/ice_hills.png",
            scrollSpeed = 0.6f,
            screenHeight = screenHeight
        )

        ground = Ground(
            context,
            "backgrounds/ice_ground.png",
            y = groundY,
            screenWidth = screenWidth,
            screenHeight = screenHeight
        )

        // Tạo snowflakes cho hiệu ứng tuyết rơi
        repeat(60) {
            snowflakeList.add(
                Snowflake(
                    x = Random.nextFloat() * screenWidth * 3,
                    y = Random.nextFloat() * screenHeight,
                    speed = Random.nextFloat() * 3f + 1f,
                    size = Random.nextFloat() * 5f + 2f
                )
            )
        }
    }

    fun update(cameraX: Float, cameraY: Float) {
        skyLayer.update(cameraX)
        mountainLayer.update(cameraX)
        hillLayer.update(cameraX)
        ground.update(cameraX)

        animationFrame++

        // Update snowflakes
        snowflakeList.forEach { flake ->
            flake.y += flake.speed
            flake.x += sin(animationFrame * 0.01f + flake.x * 0.001f) * 0.8f

            // Reset khi rơi xuống dưới
            if (flake.y > screenH + 100) {
                flake.y = -100f
                flake.x = Random.nextFloat() * screenW * 3
            }
        }
    }

    fun draw(canvas: Canvas, cameraX: Float, cameraY: Float) {
        // 1. Vẽ background layers
        skyLayer.draw(canvas)
        mountainLayer.draw(canvas)
        hillLayer.draw(canvas)
        ground.draw(canvas, cameraX, cameraY)

        // 2. Vẽ tuyết rơi
        snowflakeList.forEach { flake ->
            val screenX = flake.x - cameraX * 0.2f // Parallax nhẹ
            if (screenX > -50 && screenX < screenW + 50) {
                canvas.drawCircle(screenX, flake.y, flake.size, snowflakePaint)
            }
        }

        // 3. Vẽ decorations (khối băng, cây thông)
        drawIceDecorations(canvas, cameraX, cameraY)
    }

    private fun drawIceDecorations(canvas: Canvas, cameraX: Float, cameraY: Float) {
        // Vẽ khối băng trang trí
        for (i in 0..25) {
            val crystalX = (i * 200f + 80f) - (cameraX * 0.9f)
            val crystalY = groundY - cameraY

            if (crystalX > -120 && crystalX < screenW + 120) {
                // Khối băng kim cương
                val size = if (i % 3 == 0) 40f else 25f

                canvas.drawCircle(crystalX, crystalY - size, size, iceDecorationPaint)

                // Highlight
                val highlightPaint = Paint().apply {
                    color = Color.WHITE
                    alpha = 180
                    style = Paint.Style.FILL
                }
                canvas.drawCircle(crystalX - size/3, crystalY - size - size/3, size/2, highlightPaint)
            }
        }

        // Vẽ cây thông phủ tuyết
        for (i in 0..20) {
            val treeX = (i * 300f + 150f) - (cameraX * 0.85f)
            val treeY = groundY - cameraY

            if (treeX > -100 && treeX < screenW + 100) {
                // Thân cây
                canvas.drawRect(treeX - 12f, treeY - 50f, treeX + 12f, treeY, trunkPaint)

                // 3 tầng lá cây
                for (layer in 0..2) {
                    val layerY = treeY - 50f - layer * 35f
                    val layerSize = 50f - layer * 10f

                    // Lá cây xanh
                    canvas.drawCircle(treeX, layerY - 20f, layerSize, treePaint)

                    // Tuyết phủ trên lá
                    val snowPaint = Paint().apply {
                        color = Color.WHITE
                        alpha = 220
                        style = Paint.Style.FILL
                    }
                    canvas.drawCircle(treeX, layerY - 25f, layerSize * 0.7f, snowPaint)
                }
            }
        }

        // Vẽ sương khói lạnh
        val mistPaint = Paint().apply {
            color = Color.parseColor("#E0F2FE")
            alpha = 80
            style = Paint.Style.FILL
        }

        val mistOffset = (System.currentTimeMillis() / 80 % 200).toFloat()
        for (i in 0..8) {
            val mistX = (i * 400f + mistOffset) - (cameraX * 0.3f)
            val mistY = groundY - 80f - cameraY + sin((System.currentTimeMillis() / 1500f + i) * 1.5f) * 15f

            if (mistX > -150 && mistX < screenW + 150) {
                canvas.drawCircle(mistX, mistY, 35f, mistPaint)
                canvas.drawCircle(mistX + 50f, mistY + 10f, 25f, mistPaint)
                canvas.drawCircle(mistX - 40f, mistY - 5f, 30f, mistPaint)
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