package com.example.laptrinhgame2d

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

/**
 * 3 MAP WRAPPERS - Dựa trên GameMap hiện có, chỉ thêm màu sắc/decorations
 */

// ========== MAP 1: GRASSLAND (CỎ XANH - DỄ) - MẶC ĐỊNH ==========
class GrasslandMap(context: Context, screenWidth: Int, screenHeight: Int) {
    private val gameMap = GameMap(context, screenWidth, screenHeight)
    val groundY: Float get() = gameMap.groundY

    // Decorations
    private val grassPaint = Paint().apply {
        color = Color.rgb(34, 139, 34)
        style = Paint.Style.FILL
    }

    private val flowerPaint = Paint().apply {
        color = Color.rgb(255, 215, 0)
        style = Paint.Style.FILL
    }

    fun update(cameraX: Float, cameraY: Float) {
        gameMap.update(cameraX, cameraY)
    }

    fun draw(canvas: Canvas, cameraX: Float, cameraY: Float) {
        // 1. Vẽ GameMap gốc (sky, mountains, hills, ground)
        gameMap.draw(canvas, cameraX, cameraY)

        // 2. Vẽ thêm decorations (cỏ + hoa)
        val groundY = gameMap.groundY

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
        gameMap.cleanup()
    }
}

// ========== MAP 2: DESERT (SA MẠC - TRUNG BÌNH) ==========
class DesertMap(context: Context, screenWidth: Int, screenHeight: Int) {
    private val gameMap = GameMap(context, screenWidth, screenHeight)
    val groundY: Float get() = gameMap.groundY

    // Decorations
    private val cactusPaint = Paint().apply {
        color = Color.rgb(50, 150, 50)
        style = Paint.Style.FILL
    }

    private val sandPaint = Paint().apply {
        color = Color.rgb(238, 203, 173)
        alpha = 150
        style = Paint.Style.FILL
    }

    // Overlay vàng cam nhẹ
    private val desertOverlayPaint = Paint().apply {
        color = Color.rgb(255, 218, 185)
        alpha = 40
        style = Paint.Style.FILL
    }

    fun update(cameraX: Float, cameraY: Float) {
        gameMap.update(cameraX, cameraY)
    }

    fun draw(canvas: Canvas, cameraX: Float, cameraY: Float) {
        // 1. Vẽ GameMap gốc
        gameMap.draw(canvas, cameraX, cameraY)

        // 2. Thêm overlay vàng cam nhẹ để tạo cảm giác sa mạc
        canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), desertOverlayPaint)

        // 3. Vẽ xương rồng
        val groundY = gameMap.groundY

        for (i in 0..10) {
            val x = (i * 350f + 100f) - (cameraX * 0.95f)
            val y = groundY - cameraY

            if (x > -100 && x < canvas.width + 100) {
                // Thân xương rồng
                canvas.drawRect(x - 12f, y - 70f, x + 12f, y, cactusPaint)
                // Cành trái
                canvas.drawRect(x - 28f, y - 45f, x - 12f, y - 20f, cactusPaint)
                // Cành phải
                canvas.drawRect(x + 12f, y - 40f, x + 28f, y - 15f, cactusPaint)
            }
        }

        // 4. Cát bay (animated)
        val offset = (System.currentTimeMillis() / 30 % 300).toFloat()
        for (i in 0..6) {
            val x = (i * 300f + offset) - (cameraX * 0.3f)
            val y = groundY - 90f - cameraY

            if (x > -50 && x < canvas.width + 50) {
                canvas.drawCircle(x, y, 5f, sandPaint)
                canvas.drawCircle(x + 10f, y + 5f, 4f, sandPaint)
            }
        }
    }

    fun cleanup() {
        gameMap.cleanup()
    }
}

// ========== MAP 3: VOLCANO (NÚI LỬA - KHÓ) ==========
class VolcanoMap(context: Context, screenWidth: Int, screenHeight: Int) {
    private val gameMap = GameMap(context, screenWidth, screenHeight)
    val groundY: Float get() = gameMap.groundY

    // Decorations
    private val rockPaint = Paint().apply {
        color = Color.rgb(69, 69, 69)
        style = Paint.Style.FILL
    }

    private val lavaPaint = Paint().apply {
        color = Color.rgb(255, 69, 0)
        alpha = 200
        style = Paint.Style.FILL
    }

    private val smokePaint = Paint().apply {
        color = Color.rgb(80, 80, 80)
        alpha = 100
        style = Paint.Style.FILL
    }

    // Overlay đỏ cam
    private val volcanoOverlayPaint = Paint().apply {
        color = Color.rgb(139, 69, 19)
        alpha = 50
        style = Paint.Style.FILL
    }

    fun update(cameraX: Float, cameraY: Float) {
        gameMap.update(cameraX, cameraY)
    }

    fun draw(canvas: Canvas, cameraX: Float, cameraY: Float) {
        // 1. Vẽ GameMap gốc
        gameMap.draw(canvas, cameraX, cameraY)

        // 2. Thêm overlay đỏ cam để tạo cảm giác núi lửa
        canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), volcanoOverlayPaint)

        val groundY = gameMap.groundY

        // 3. Vẽ sông dung nham nhỏ dưới ground
        val lavaY = groundY + 50f - cameraY
        canvas.drawRect(0f, lavaY, canvas.width.toFloat(), canvas.height.toFloat(), lavaPaint)

        // 4. Vẽ đá núi lửa
        for (i in 0..12) {
            val x = (i * 280f + 80f) - (cameraX * 0.9f)
            val y = groundY - 35f - cameraY

            if (x > -80 && x < canvas.width + 80) {
                canvas.drawCircle(x, y, 30f, rockPaint)
                canvas.drawCircle(x - 12f, y - 8f, 18f, rockPaint)
                canvas.drawCircle(x + 12f, y - 5f, 20f, rockPaint)
            }
        }

        // 5. Khói bốc lên (animated)
        val offset = (System.currentTimeMillis() / 40 % 80).toFloat()
        for (i in 0..5) {
            val x = (i * 500f) - (cameraX * 0.4f)
            val y = groundY - 130f - offset - cameraY

            if (x > -100 && x < canvas.width + 100) {
                canvas.drawCircle(x, y, 35f, smokePaint)
                canvas.drawCircle(x + 20f, y - 25f, 28f, smokePaint)
                canvas.drawCircle(x - 20f, y - 25f, 28f, smokePaint)
            }
        }
    }

    fun cleanup() {
        gameMap.cleanup()
    }
}