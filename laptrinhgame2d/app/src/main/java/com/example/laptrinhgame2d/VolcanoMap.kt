//package com.example.laptrinhgame2d
//
//import android.content.Context
//import android.graphics.Canvas
//import android.graphics.Color
//import android.graphics.LinearGradient
//import android.graphics.Paint
//import android.graphics.Shader
//import kotlin.random.Random
//
///**
// * Map 3: Volcano - Núi lửa nguy hiểm
// * Kế thừa GameMap để tương thích với code hiện tại
// */
//class VolcanoMap(
//    context: Context,
//    private val screenWidth: Int,
//    private val screenHeight: Int
//) : GameMap(context, screenWidth, screenHeight) {
//
//    private val mapName = "Volcano"
//    private val mapDifficulty = "Hard"
//
//    // Hiệu ứng dung nham
//    private val lavaParticles = mutableListOf<LavaParticle>()
//    private var particleSpawnTimer = 0
//
//    fun updateCustom() {
//        // Update lava particles
//        particleSpawnTimer++
//        if (particleSpawnTimer >= 10) {
//            particleSpawnTimer = 0
//            // Spawn new particle
//            val x = Random.nextFloat() * 3000f
//            lavaParticles.add(LavaParticle(x, groundY + 10f))
//        }
//
//        lavaParticles.forEach { it.update() }
//        lavaParticles.removeAll { it.isDead() }
//    }
//
//    fun drawCustomElements(canvas: Canvas, cameraX: Float, cameraY: Float) {
//        // Vẽ background đỏ cam (núi lửa)
//        val gradientPaint = Paint().apply {
//            shader = LinearGradient(
//                0f, 0f, 0f, screenHeight.toFloat(),
//                Color.rgb(80, 20, 10),
//                Color.rgb(180, 60, 20),
//                Shader.TileMode.CLAMP
//            )
//        }
//        canvas.drawRect(0f, 0f, screenWidth.toFloat(), screenHeight.toFloat(), gradientPaint)
//
//        // Vẽ dung nham effect
//        drawLavaEffect(canvas, cameraX, cameraY)
//
//        // Vẽ elements đặc trưng
//        drawVolcanoElements(canvas, cameraX, cameraY)
//    }
//
//    private fun drawLavaEffect(canvas: Canvas, cameraX: Float, cameraY: Float) {
//        // Vẽ sông dung nham dưới ground
//        val lavaPaint = Paint().apply {
//            color = Color.rgb(255, 100, 0)
//            style = Paint.Style.FILL
//        }
//
//        val lavaY = groundY + 50f - cameraY
//        canvas.drawRect(0f, lavaY, screenWidth.toFloat(), screenHeight.toFloat(), lavaPaint)
//
//        // Vẽ lava particles
//        canvas.save()
//        canvas.translate(-cameraX, -cameraY)
//        lavaParticles.forEach { it.draw(canvas) }
//        canvas.restore()
//    }
//
//    private fun drawVolcanoElements(canvas: Canvas, cameraX: Float, cameraY: Float) {
//        // Vẽ các tảng đá núi lửa
//        val rockPaint = Paint().apply {
//            color = Color.rgb(60, 30, 20)
//            style = Paint.Style.FILL
//        }
//
//        for (i in 0..8) {
//            val rockX = (i * 400f + 150f) - (cameraX * 0.85f)
//            val rockY = groundY - 40f - cameraY
//
//            if (rockX > -80 && rockX < screenWidth + 80) {
//                canvas.drawCircle(rockX, rockY, 40f, rockPaint)
//                canvas.drawCircle(rockX - 20f, rockY - 15f, 25f, rockPaint)
//                canvas.drawCircle(rockX + 20f, rockY - 10f, 30f, rockPaint)
//            }
//        }
//
//        // Vẽ khói đen bốc lên
//        val smokePaint = Paint().apply {
//            color = Color.rgb(40, 40, 40)
//            alpha = 100
//            style = Paint.Style.FILL
//        }
//
//        val smokeOffset = (System.currentTimeMillis() / 50 % 100).toFloat()
//        for (i in 0..3) {
//            val smokeX = (i * 700f) - (cameraX * 0.4f)
//            val smokeY = groundY - 200f - smokeOffset - cameraY
//
//            if (smokeX > -100 && smokeX < screenWidth + 100) {
//                canvas.drawCircle(smokeX, smokeY, 50f, smokePaint)
//                canvas.drawCircle(smokeX + 30f, smokeY - 40f, 40f, smokePaint)
//                canvas.drawCircle(smokeX - 30f, smokeY - 40f, 40f, smokePaint)
//            }
//        }
//    }
//
//    fun getMapName() = mapName
//    fun getMapDifficulty() = mapDifficulty
//
//    // Inner class cho hiệu ứng dung nham
//    private class LavaParticle(var x: Float, var y: Float) {
//        private var velocityY = -Random.nextFloat() * 3f - 2f
//        private var life = 60
//
//        fun update() {
//            y += velocityY
//            velocityY += 0.1f // gravity
//            life--
//        }
//
//        fun isDead() = life <= 0
//
//        fun draw(canvas: Canvas) {
//            val alpha = (life / 60f * 255).toInt().coerceIn(0, 255)
//            val paint = Paint().apply {
//                color = Color.rgb(255, 150, 0)
//                this.alpha = alpha
//                style = Paint.Style.FILL
//            }
//            canvas.drawCircle(x, y, 5f, paint)
//        }
//    }
//}