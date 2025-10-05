package com.example.laptrinhgame2d.maps

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import java.io.File

class Ground(
    context: Context,
    imagePath: String,
    val y: Float, // Vị trí Y của mặt đất (top của ground)
    private val screenWidth: Int,
    private val screenHeight: Int
) {
    private var bitmap: Bitmap
    private var x1 = 0f
    private var x2 = 0f
    private val tileWidth: Int
    private val tileHeight: Int

    init {
        // Load hoặc generate bitmap
        val originalBitmap = try {
            // Thử load từ assets
            val inputStream = context.assets.open(imagePath)
            val bmp = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            bmp
        } catch (e: Exception) {
            // Nếu không tìm thấy trong assets, thử load từ internal storage
            val file = File(context.filesDir, imagePath)
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else {
                // Nếu vẫn không có, tạo mới
                val filename = imagePath.substringAfterLast("/")
                val generatedBitmap = BackgroundGenerator.generateGroundTexture(512, 512)
                // Lưu để lần sau dùng lại
                BackgroundGenerator.saveBitmapToInternalStorage(context, generatedBitmap, filename)
                generatedBitmap
            }
        }

        // Tính toán kích thước tile - chiếm từ groundY đến đáy màn hình (25% phía dưới)
        tileHeight = (screenHeight - y).toInt()
        val aspectRatio = originalBitmap.width.toFloat() / originalBitmap.height.toFloat()
        tileWidth = (tileHeight * aspectRatio).toInt()

        bitmap = Bitmap.createScaledBitmap(originalBitmap, tileWidth, tileHeight, true)

        // Chỉ recycle nếu originalBitmap khác bitmap (tức là đã scale)
        if (originalBitmap != bitmap) {
            originalBitmap.recycle()
        }

        // Khởi tạo vị trí 2 tile nối tiếp
        x2 = tileWidth.toFloat()
    }

    fun update(cameraX: Float) {
        // Ground cuộn theo camera với tốc độ 1:1
        x1 = -cameraX
        x2 = x1 + tileWidth

        // Loop vô hạn
        while (x1 + tileWidth < 0) {
            x1 += tileWidth * 2
        }

        while (x2 + tileWidth < 0) {
            x2 += tileWidth * 2
        }

        // Đảm bảo có đủ tiles để phủ màn hình
        if (x1 > screenWidth) {
            x1 = x2 - tileWidth
        }
        if (x2 > screenWidth) {
            x2 = x1 - tileWidth
        }
    }

    fun draw(canvas: Canvas, cameraX: Float, cameraY: Float) {
        // Vẽ nhiều tiles để phủ kín màn hình
        val startX = (cameraX / tileWidth).toInt() * tileWidth
        val numTiles = (screenWidth.toFloat() / tileWidth).toInt() + 3

        for (i in 0..numTiles) {
            val x = startX + (i * tileWidth) - cameraX
            canvas.drawBitmap(bitmap, x, y - cameraY, null)
        }
    }

    fun recycle() {
        bitmap.recycle()
    }
}