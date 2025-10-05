package com.example.laptrinhgame2d.maps

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import com.example.laptrinhgame2d.maps.BackgroundGenerator
import java.io.File

class BackgroundLayer(
    context: Context,
    imagePath: String,
    private val scrollSpeed: Float, // Tốc độ cuộn (parallax effect)
    private val screenHeight: Int
) {
    private var bitmap: Bitmap
    private var x1 = 0f
    private var x2 = 0f
    private val scaledHeight: Int
    private val scaledWidth: Int

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
                val generatedBitmap = when {
                    filename.contains("sky") -> {
                        BackgroundGenerator.generateSkyBackground(1920, screenHeight)
                    }
                    filename.contains("mountains") -> {
                        BackgroundGenerator.generateMountainsBackground(1920, screenHeight)
                    }
                    filename.contains("hills") -> {
                        BackgroundGenerator.generateHillsBackground(1920, screenHeight)
                    }
                    else -> {
                        // Default: sky nếu không khớp
                        BackgroundGenerator.generateSkyBackground(1920, screenHeight)
                    }
                }
                // Lưu để lần sau dùng lại
                BackgroundGenerator.saveBitmapToInternalStorage(context, generatedBitmap, filename)
                generatedBitmap
            }
        }

        // Scale bitmap theo chiều cao màn hình
        scaledHeight = screenHeight
        val aspectRatio = originalBitmap.width.toFloat() / originalBitmap.height.toFloat()
        scaledWidth = (scaledHeight * aspectRatio).toInt()

        bitmap = Bitmap.createScaledBitmap(originalBitmap, scaledWidth, scaledHeight, true)

        // Chỉ recycle nếu originalBitmap khác bitmap (tức là đã scale)
        if (originalBitmap != bitmap) {
            originalBitmap.recycle()
        }

        // Khởi tạo vị trí 2 ảnh nối tiếp để cuộn liên tục
        x2 = scaledWidth.toFloat()
    }

    fun update(cameraX: Float) {
        // Di chuyển layer theo camera với tốc độ parallax
        val offset = cameraX * scrollSpeed

        x1 = -offset
        x2 = x1 + scaledWidth

        // Nếu ảnh đầu tiên ra khỏi màn hình bên trái, chuyển nó sang bên phải
        if (x1 + scaledWidth < 0) {
            x1 = x2 + scaledWidth
        }

        // Nếu ảnh thứ hai ra khỏi màn hình bên trái, chuyển nó sang bên phải
        if (x2 + scaledWidth < 0) {
            x2 = x1 + scaledWidth
        }

        // Sắp xếp lại để ảnh nào bên trái hơn được vẽ trước
        if (x1 > x2) {
            val temp = x1
            x1 = x2
            x2 = temp
        }
    }

    fun draw(canvas: Canvas) {
        // Vẽ 2 ảnh nối tiếp
        canvas.drawBitmap(bitmap, x1, 0f, null)
        canvas.drawBitmap(bitmap, x2, 0f, null)
    }

    fun recycle() {
        bitmap.recycle()
    }
}