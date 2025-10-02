package com.example.laptrinhgame2d

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import java.io.File
import java.io.FileOutputStream

/**
 * Helper class để tạo các ảnh background đơn giản cho game
 * Nếu chưa có ảnh trong assets, sẽ tạo ảnh mặc định
 */
object BackgroundGenerator {
    
    fun generateSkyBackground(width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Gradient từ xanh nhạt (trên) đến xanh đậm (dưới)
        val paint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                Color.rgb(135, 206, 235), // Sky blue
                Color.rgb(176, 224, 230), // Powder blue
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        
        // Vẽ một vài đám mây đơn giản
        val cloudPaint = Paint().apply {
            color = Color.WHITE
            alpha = 180
            style = Paint.Style.FILL
        }
        
        // Đám mây 1
        canvas.drawCircle(width * 0.2f, height * 0.2f, 80f, cloudPaint)
        canvas.drawCircle(width * 0.25f, height * 0.2f, 100f, cloudPaint)
        canvas.drawCircle(width * 0.3f, height * 0.2f, 80f, cloudPaint)
        
        // Đám mây 2
        canvas.drawCircle(width * 0.6f, height * 0.3f, 90f, cloudPaint)
        canvas.drawCircle(width * 0.65f, height * 0.3f, 110f, cloudPaint)
        canvas.drawCircle(width * 0.7f, height * 0.3f, 85f, cloudPaint)
        
        return bitmap
    }
    
    fun generateMountainsBackground(width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Nền trong suốt
        canvas.drawColor(Color.TRANSPARENT)
        
        // Vẽ núi xa (màu xanh nhạt)
        val mountainPaint1 = Paint().apply {
            color = Color.rgb(100, 149, 237) // Cornflower blue
            style = Paint.Style.FILL
            alpha = 150
        }
        
        val path1 = Path().apply {
            moveTo(0f, height.toFloat())
            lineTo(width * 0.3f, height * 0.4f)
            lineTo(width * 0.6f, height * 0.5f)
            lineTo(width.toFloat(), height * 0.3f)
            lineTo(width.toFloat(), height.toFloat())
            close()
        }
        canvas.drawPath(path1, mountainPaint1)
        
        // Vẽ núi gần (màu xanh đậm hơn)
        val mountainPaint2 = Paint().apply {
            color = Color.rgb(70, 130, 180) // Steel blue
            style = Paint.Style.FILL
            alpha = 180
        }
        
        val path2 = Path().apply {
            moveTo(0f, height.toFloat())
            lineTo(width * 0.2f, height * 0.6f)
            lineTo(width * 0.5f, height * 0.5f)
            lineTo(width * 0.8f, height * 0.65f)
            lineTo(width.toFloat(), height * 0.55f)
            lineTo(width.toFloat(), height.toFloat())
            close()
        }
        canvas.drawPath(path2, mountainPaint2)
        
        return bitmap
    }
    
    fun generateHillsBackground(width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Nền trong suốt
        canvas.drawColor(Color.TRANSPARENT)
        
        // Vẽ đồi (màu xanh lá)
        val hillPaint = Paint().apply {
            color = Color.rgb(34, 139, 34) // Forest green
            style = Paint.Style.FILL
            alpha = 200
        }
        
        val path = Path().apply {
            moveTo(0f, height.toFloat())
            
            // Vẽ các đồi nhấp nhô
            lineTo(width * 0.15f, height * 0.75f)
            lineTo(width * 0.3f, height * 0.8f)
            lineTo(width * 0.45f, height * 0.7f)
            lineTo(width * 0.6f, height * 0.75f)
            lineTo(width * 0.75f, height * 0.72f)
            lineTo(width * 0.9f, height * 0.78f)
            lineTo(width.toFloat(), height * 0.75f)
            lineTo(width.toFloat(), height.toFloat())
            close()
        }
        canvas.drawPath(path, hillPaint)
        
        // Vẽ cỏ đậm hơn ở trên đồi
        val grassPaint = Paint().apply {
            color = Color.rgb(0, 128, 0) // Green
            style = Paint.Style.FILL
        }
        
        val grassPath = Path().apply {
            moveTo(0f, height.toFloat())
            lineTo(width * 0.15f, height * 0.75f)
            lineTo(width * 0.3f, height * 0.8f)
            lineTo(width * 0.45f, height * 0.7f)
            lineTo(width * 0.6f, height * 0.75f)
            lineTo(width * 0.75f, height * 0.72f)
            lineTo(width * 0.9f, height * 0.78f)
            lineTo(width.toFloat(), height * 0.75f)
            lineTo(width.toFloat(), height * 0.78f)
            lineTo(width * 0.9f, height * 0.81f)
            lineTo(width * 0.75f, height * 0.75f)
            lineTo(width * 0.6f, height * 0.78f)
            lineTo(width * 0.45f, height * 0.73f)
            lineTo(width * 0.3f, height * 0.83f)
            lineTo(width * 0.15f, height * 0.78f)
            lineTo(0f, height.toFloat())
            close()
        }
        canvas.drawPath(grassPath, grassPaint)
        
        return bitmap
    }
    
    fun generateGroundTexture(width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Nền đất màu nâu
        val groundPaint = Paint().apply {
            color = Color.rgb(139, 90, 43) // Saddle brown
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), groundPaint)
        
        // Vẽ cỏ xanh ở trên
        val grassPaint = Paint().apply {
            color = Color.rgb(0, 128, 0) // Green
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height * 0.2f, grassPaint)
        
        // Vẽ các đường cỏ nhỏ
        val grassStrokePaint = Paint().apply {
            color = Color.rgb(34, 139, 34)
            strokeWidth = 3f
            style = Paint.Style.STROKE
        }
        
        for (i in 0 until width step 20) {
            canvas.drawLine(
                i.toFloat(), height * 0.15f,
                i.toFloat(), height * 0.25f,
                grassStrokePaint
            )
        }
        
        // Vẽ texture đất (các chấm nhỏ)
        val dirtPaint = Paint().apply {
            color = Color.rgb(101, 67, 33)
            style = Paint.Style.FILL
        }
        
        for (i in 0 until width step 30) {
            for (j in (height * 0.3f).toInt() until height step 40) {
                canvas.drawCircle(i.toFloat(), j.toFloat(), 5f, dirtPaint)
            }
        }
        
        return bitmap
    }
    
    /**
     * Lưu bitmap vào internal storage để BackgroundLayer có thể load
     */
    fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap, filename: String) {
        try {
            val directory = File(context.filesDir, "backgrounds")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            
            val file = File(directory, filename)
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Tạo tất cả các background cần thiết
     */
    fun generateAllBackgrounds(context: Context, screenWidth: Int, screenHeight: Int) {
        // Tạo và lưu sky
        val sky = generateSkyBackground(screenWidth * 2, screenHeight)
        saveBitmapToInternalStorage(context, sky, "sky.png")
        
        // Tạo và lưu mountains
        val mountains = generateMountainsBackground(screenWidth * 2, screenHeight)
        saveBitmapToInternalStorage(context, mountains, "mountains.png")
        
        // Tạo và lưu hills
        val hills = generateHillsBackground(screenWidth * 2, screenHeight)
        saveBitmapToInternalStorage(context, hills, "hills.png")
        
        // Tạo và lưu ground (texture nhỏ để tile)
        val ground = generateGroundTexture(512, 512)
        saveBitmapToInternalStorage(context, ground, "ground.png")
    }
}
