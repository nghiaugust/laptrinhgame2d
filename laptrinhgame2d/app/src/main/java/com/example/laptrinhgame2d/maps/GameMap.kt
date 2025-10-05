package com.example.laptrinhgame2d.maps

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.laptrinhgame2d.maps.BackgroundLayer
import com.example.laptrinhgame2d.maps.Ground

class GameMap(
    private val context: Context,
    private val screenWidth: Int,
    private val screenHeight: Int
) {
    // Background layers (parallax scrolling)
    private val skyLayer: BackgroundLayer
    private val mountainLayer: BackgroundLayer
    private val hillLayer: BackgroundLayer

    // Ground platform
    private val ground: Ground
    val groundY: Float // Vị trí Y của mặt đất để nhân vật và quái đứng trên

    // Paint cho debugging (optional)
    private val debugPaint = Paint().apply {
        color = Color.RED
        strokeWidth = 3f
        style = Paint.Style.STROKE
    }

    init {
        // Tính toán vị trí mặt đất (75% chiều cao màn hình)
        // Ground sẽ chiếm 25% phía dưới (từ 75% đến 100%)
        groundY = screenHeight * 0.75f

        // Khởi tạo các layer background với tốc độ parallax khác nhau
        // Layer càng xa càng chậm (tạo hiệu ứng chiều sâu)
        skyLayer = BackgroundLayer(
            context,
            "backgrounds/sky.png", // Ảnh bầu trời
            scrollSpeed = 0.1f, // Cuộn rất chậm
            screenHeight = screenHeight
        )

        mountainLayer = BackgroundLayer(
            context,
            "backgrounds/mountains.png", // Ảnh núi xa
            scrollSpeed = 0.3f, // Cuộn chậm
            screenHeight = screenHeight
        )

        hillLayer = BackgroundLayer(
            context,
            "backgrounds/hills.png", // Ảnh đồi gần
            scrollSpeed = 0.6f, // Cuộn nhanh hơn
            screenHeight = screenHeight
        )

        // Khởi tạo ground
        ground = Ground(
            context,
            "backgrounds/ground.png", // Ảnh mặt đất
            y = groundY,
            screenWidth = screenWidth,
            screenHeight = screenHeight
        )
    }

    fun update(cameraX: Float, cameraY: Float) {
        // Update tất cả layers với parallax effect
        skyLayer.update(cameraX)
        mountainLayer.update(cameraX)
        hillLayer.update(cameraX)
        ground.update(cameraX)
    }

    fun draw(canvas: Canvas, cameraX: Float, cameraY: Float) {
        // Vẽ theo thứ tự từ xa đến gần
        skyLayer.draw(canvas)
        mountainLayer.draw(canvas)
        hillLayer.draw(canvas)
        ground.draw(canvas, cameraX, cameraY)

        // Vẽ đường ground line để debug (optional - có thể tắt)
        // canvas.drawLine(0f, groundY - cameraY, screenWidth.toFloat(), groundY - cameraY, debugPaint)
    }

    fun cleanup() {
        skyLayer.recycle()
        mountainLayer.recycle()
        hillLayer.recycle()
        ground.recycle()
    }
}