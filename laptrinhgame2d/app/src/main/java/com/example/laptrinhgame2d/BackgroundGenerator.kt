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

    // ========== GRASSLAND BACKGROUNDS (GIỮ NGUYÊN) ==========
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

    // ========== DESERT BACKGROUNDS (MỚI HOÀN TOÀN) ==========
    fun generateDesertSkyBackground(width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Gradient từ vàng cam (trên) đến cam đậm (dưới) - bầu trời sa mạc
        val paint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                Color.rgb(255, 218, 85), // Golden yellow
                Color.rgb(255, 165, 0), // Orange
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Mặt trời sa mạc
        val sunPaint = Paint().apply {
            color = Color.rgb(255, 255, 0) // Bright yellow
            alpha = 200
            style = Paint.Style.FILL
        }
        canvas.drawCircle(width * 0.8f, height * 0.2f, 60f, sunPaint)

        // Hiệu ứng nhiệt - heat waves
        val heatPaint = Paint().apply {
            color = Color.rgb(255, 255, 255)
            alpha = 50
            style = Paint.Style.FILL
        }

        for (i in 0..5) {
            val x = width * (0.1f + i * 0.15f)
            canvas.drawOval(x - 30f, height * 0.6f, x + 30f, height * 0.8f, heatPaint)
        }

        return bitmap
    }

    fun generateDesertMountainsBackground(width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        canvas.drawColor(Color.TRANSPARENT)

        // Núi sa mạc xa (màu nâu nhạt)
        val mountainPaint1 = Paint().apply {
            color = Color.rgb(205, 133, 63) // Peru color
            style = Paint.Style.FILL
            alpha = 120
        }

        val path1 = Path().apply {
            moveTo(0f, height.toFloat())
            lineTo(width * 0.25f, height * 0.3f)
            lineTo(width * 0.5f, height * 0.4f)
            lineTo(width * 0.75f, height * 0.25f)
            lineTo(width.toFloat(), height * 0.35f)
            lineTo(width.toFloat(), height.toFloat())
            close()
        }
        canvas.drawPath(path1, mountainPaint1)

        // Núi sa mạc gần (màu nâu đậm hơn)
        val mountainPaint2 = Paint().apply {
            color = Color.rgb(160, 82, 45) // Saddle brown
            style = Paint.Style.FILL
            alpha = 160
        }

        val path2 = Path().apply {
            moveTo(0f, height.toFloat())
            lineTo(width * 0.2f, height * 0.5f)
            lineTo(width * 0.4f, height * 0.45f)
            lineTo(width * 0.6f, height * 0.55f)
            lineTo(width * 0.8f, height * 0.5f)
            lineTo(width.toFloat(), height * 0.6f)
            lineTo(width.toFloat(), height.toFloat())
            close()
        }
        canvas.drawPath(path2, mountainPaint2)

        return bitmap
    }

    fun generateDesertHillsBackground(width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        canvas.drawColor(Color.TRANSPARENT)

        // Đồi cát sa mạc
        val sandHillPaint = Paint().apply {
            color = Color.rgb(238, 203, 173) // Sandy brown
            style = Paint.Style.FILL
            alpha = 180
        }

        val path = Path().apply {
            moveTo(0f, height.toFloat())
            lineTo(width * 0.15f, height * 0.7f)
            lineTo(width * 0.3f, height * 0.75f)
            lineTo(width * 0.5f, height * 0.65f)
            lineTo(width * 0.7f, height * 0.72f)
            lineTo(width * 0.85f, height * 0.68f)
            lineTo(width.toFloat(), height * 0.75f)
            lineTo(width.toFloat(), height.toFloat())
            close()
        }
        canvas.drawPath(path, sandHillPaint)

        return bitmap
    }

    // ========== VOLCANO BACKGROUNDS (MỚI HOÀN TOÀN) ==========
    fun generateVolcanoSkyBackground(width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Gradient từ đỏ tối (trên) đến cam đậm (dưới) - bầu trời núi lửa
        val paint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                Color.rgb(139, 69, 19), // Saddle brown
                Color.rgb(255, 69, 0), // Red orange
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Khói đen từ núi lửa
        val smokePaint = Paint().apply {
            color = Color.rgb(64, 64, 64)
            alpha = 150
            style = Paint.Style.FILL
        }

        // Đám khói lớn
        for (i in 0..4) {
            val x = width * (0.2f + i * 0.2f)
            val y = height * 0.1f
            canvas.drawCircle(x, y, 80f + i * 20f, smokePaint)
            canvas.drawCircle(x + 30f, y - 20f, 60f + i * 15f, smokePaint)
        }

        // Tia lửa trong không khí
        val emberPaint = Paint().apply {
            color = Color.rgb(255, 140, 0)
            alpha = 200
            style = Paint.Style.FILL
        }

        for (i in 0..10) {
            val x = kotlin.random.Random.nextFloat() * width
            val y = kotlin.random.Random.nextFloat() * height * 0.6f
            canvas.drawCircle(x, y, 3f, emberPaint)
        }

        return bitmap
    }

    fun generateVolcanoMountainsBackground(width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        canvas.drawColor(Color.TRANSPARENT)

        // Núi lửa xa (màu đỏ tối)
        val volcanoMountainPaint1 = Paint().apply {
            color = Color.rgb(128, 0, 0) // Dark red
            style = Paint.Style.FILL
            alpha = 140
        }

        val path1 = Path().apply {
            moveTo(0f, height.toFloat())
            // Tạo hình núi lửa có miệng núi lửa
            lineTo(width * 0.2f, height * 0.4f)
            lineTo(width * 0.25f, height * 0.35f) // Miệng núi lửa
            lineTo(width * 0.3f, height * 0.4f)
            lineTo(width * 0.6f, height * 0.5f)
            lineTo(width * 0.65f, height * 0.45f) // Miệng núi lửa
            lineTo(width * 0.7f, height * 0.5f)
            lineTo(width.toFloat(), height * 0.3f)
            lineTo(width.toFloat(), height.toFloat())
            close()
        }
        canvas.drawPath(path1, volcanoMountainPaint1)

        // Núi lửa gần (màu đỏ đậm hơn)
        val volcanoMountainPaint2 = Paint().apply {
            color = Color.rgb(178, 34, 34) // Fire brick
            style = Paint.Style.FILL
            alpha = 180
        }

        val path2 = Path().apply {
            moveTo(0f, height.toFloat())
            lineTo(width * 0.15f, height * 0.6f)
            lineTo(width * 0.2f, height * 0.55f) // Miệng núi lửa
            lineTo(width * 0.25f, height * 0.6f)
            lineTo(width * 0.5f, height * 0.65f)
            lineTo(width * 0.55f, height * 0.6f) // Miệng núi lửa
            lineTo(width * 0.6f, height * 0.65f)
            lineTo(width * 0.8f, height * 0.7f)
            lineTo(width.toFloat(), height * 0.6f)
            lineTo(width.toFloat(), height.toFloat())
            close()
        }
        canvas.drawPath(path2, volcanoMountainPaint2)

        // Dung nham chảy từ miệng núi lửa
        val lavaPaint = Paint().apply {
            color = Color.rgb(255, 69, 0)
            alpha = 200
            style = Paint.Style.FILL
        }

        // Dòng dung nham 1
        val lavaPath1 = Path().apply {
            moveTo(width * 0.25f, height * 0.35f)
            lineTo(width * 0.23f, height * 0.5f)
            lineTo(width * 0.27f, height * 0.5f)
            close()
        }
        canvas.drawPath(lavaPath1, lavaPaint)

        // Dòng dung nham 2
        val lavaPath2 = Path().apply {
            moveTo(width * 0.65f, height * 0.45f)
            lineTo(width * 0.63f, height * 0.65f)
            lineTo(width * 0.67f, height * 0.65f)
            close()
        }
        canvas.drawPath(lavaPath2, lavaPaint)

        return bitmap
    }

    fun generateVolcanoHillsBackground(width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        canvas.drawColor(Color.TRANSPARENT)

        // Đồi đá núi lửa
        val volcanicHillPaint = Paint().apply {
            color = Color.rgb(105, 105, 105) // Dim gray
            style = Paint.Style.FILL
            alpha = 160
        }

        val path = Path().apply {
            moveTo(0f, height.toFloat())
            lineTo(width * 0.15f, height * 0.75f)
            lineTo(width * 0.3f, height * 0.8f)
            lineTo(width * 0.45f, height * 0.7f)
            lineTo(width * 0.6f, height * 0.78f)
            lineTo(width * 0.75f, height * 0.72f)
            lineTo(width * 0.9f, height * 0.8f)
            lineTo(width.toFloat(), height * 0.75f)
            lineTo(width.toFloat(), height.toFloat())
            close()
        }
        canvas.drawPath(path, volcanicHillPaint)

        // Đá núi lửa đen hơn
        val darkRockPaint = Paint().apply {
            color = Color.rgb(64, 64, 64)
            style = Paint.Style.FILL
        }

        val darkPath = Path().apply {
            moveTo(0f, height.toFloat())
            lineTo(width * 0.15f, height * 0.75f)
            lineTo(width * 0.3f, height * 0.8f)
            lineTo(width * 0.45f, height * 0.7f)
            lineTo(width * 0.6f, height * 0.78f)
            lineTo(width * 0.75f, height * 0.72f)
            lineTo(width * 0.9f, height * 0.8f)
            lineTo(width.toFloat(), height * 0.75f)
            lineTo(width.toFloat(), height * 0.83f)
            lineTo(width * 0.9f, height * 0.83f)
            lineTo(width * 0.75f, height * 0.75f)
            lineTo(width * 0.6f, height * 0.81f)
            lineTo(width * 0.45f, height * 0.73f)
            lineTo(width * 0.3f, height * 0.83f)
            lineTo(width * 0.15f, height * 0.78f)
            lineTo(0f, height.toFloat())
            close()
        }
        canvas.drawPath(darkPath, darkRockPaint)

        return bitmap
    }

    // ========== GROUND TEXTURES ==========
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

    fun generateDesertGroundTexture(width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Nền cát sa mạc
        val sandPaint = Paint().apply {
            color = Color.rgb(238, 203, 173) // Sandy brown
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), sandPaint)

        // Cát vàng ở trên
        val goldSandPaint = Paint().apply {
            color = Color.rgb(255, 218, 185) // Peach puff
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height * 0.3f, goldSandPaint)

        // Texture cát (các gợn sóng)
        val sandWavePaint = Paint().apply {
            color = Color.rgb(218, 165, 132)
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }

        for (i in 0 until width step 40) {
            for (j in 0 until height step 20) {
                canvas.drawLine(
                    i.toFloat(), j.toFloat(),
                    (i + 30).toFloat(), j.toFloat(),
                    sandWavePaint
                )
            }
        }

        // Các hạt cát
        val sandGrainPaint = Paint().apply {
            color = Color.rgb(205, 133, 63)
            style = Paint.Style.FILL
        }

        for (i in 0 until width step 25) {
            for (j in 0 until height step 35) {
                canvas.drawCircle(i.toFloat(), j.toFloat(), 3f, sandGrainPaint)
            }
        }

        return bitmap
    }

    fun generateVolcanoGroundTexture(width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Nền đá núi lửa
        val volcanicRockPaint = Paint().apply {
            color = Color.rgb(64, 64, 64) // Dark gray
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), volcanicRockPaint)

        // Đá đen ở trên
        val blackRockPaint = Paint().apply {
            color = Color.rgb(32, 32, 32) // Very dark gray
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height * 0.4f, blackRockPaint)

        // Vết nứt dung nham
        val lavaCrackPaint = Paint().apply {
            color = Color.rgb(255, 69, 0) // Red orange
            strokeWidth = 4f
            style = Paint.Style.STROKE
        }

        for (i in 0 until width step 60) {
            canvas.drawLine(
                i.toFloat(), 0f,
                i.toFloat(), height * 0.6f,
                lavaCrackPaint
            )
        }

        // Các đá nhỏ
        val smallRockPaint = Paint().apply {
            color = Color.rgb(105, 105, 105)
            style = Paint.Style.FILL
        }

        for (i in 0 until width step 35) {
            for (j in 0 until height step 45) {
                canvas.drawCircle(i.toFloat(), j.toFloat(), 8f, smallRockPaint)
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
     * Tạo tất cả các background cần thiết cho GRASSLAND
     */
    fun generateAllGrasslandBackgrounds(context: Context, screenWidth: Int, screenHeight: Int) {
        // Tạo và lưu sky
        val sky = generateSkyBackground(screenWidth * 2, screenHeight)
        saveBitmapToInternalStorage(context, sky, "grassland_sky.png")

        // Tạo và lưu mountains
        val mountains = generateMountainsBackground(screenWidth * 2, screenHeight)
        saveBitmapToInternalStorage(context, mountains, "grassland_mountains.png")

        // Tạo và lưu hills
        val hills = generateHillsBackground(screenWidth * 2, screenHeight)
        saveBitmapToInternalStorage(context, hills, "grassland_hills.png")

        // Tạo và lưu ground
        val ground = generateGroundTexture(512, 512)
        saveBitmapToInternalStorage(context, ground, "grassland_ground.png")
    }

    /**
     * Tạo tất cả các background cần thiết cho DESERT
     */
    fun generateAllDesertBackgrounds(context: Context, screenWidth: Int, screenHeight: Int) {
        // Tạo và lưu sky sa mạc
        val sky = generateDesertSkyBackground(screenWidth * 2, screenHeight)
        saveBitmapToInternalStorage(context, sky, "desert_sky.png")

        // Tạo và lưu mountains sa mạc
        val mountains = generateDesertMountainsBackground(screenWidth * 2, screenHeight)
        saveBitmapToInternalStorage(context, mountains, "desert_mountains.png")

        // Tạo và lưu hills sa mạc
        val hills = generateDesertHillsBackground(screenWidth * 2, screenHeight)
        saveBitmapToInternalStorage(context, hills, "desert_hills.png")

        // Tạo và lưu ground sa mạc
        val ground = generateDesertGroundTexture(512, 512)
        saveBitmapToInternalStorage(context, ground, "desert_ground.png")
    }

    /**
     * Tạo tất cả các background cần thiết cho VOLCANO
     */
    fun generateAllVolcanoBackgrounds(context: Context, screenWidth: Int, screenHeight: Int) {
        // Tạo và lưu sky núi lửa
        val sky = generateVolcanoSkyBackground(screenWidth * 2, screenHeight)
        saveBitmapToInternalStorage(context, sky, "volcano_sky.png")

        // Tạo và lưu mountains núi lửa
        val mountains = generateVolcanoMountainsBackground(screenWidth * 2, screenHeight)
        saveBitmapToInternalStorage(context, mountains, "volcano_mountains.png")

        // Tạo và lưu hills núi lửa
        val hills = generateVolcanoHillsBackground(screenWidth * 2, screenHeight)
        saveBitmapToInternalStorage(context, hills, "volcano_hills.png")

        // Tạo và lưu ground núi lửa
        val ground = generateVolcanoGroundTexture(512, 512)
        saveBitmapToInternalStorage(context, ground, "volcano_ground.png")
    }
}