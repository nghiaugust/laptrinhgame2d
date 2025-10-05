//package com.example.laptrinhgame2d
//
//import android.content.Context
//import android.graphics.Canvas
//import android.graphics.Color
//import android.graphics.Paint
//
///**
// * Map 2: Desert - Sa mạc khô hạn
// * Kế thừa GameMap để tương thích với code hiện tại
// */
//class DesertMap(
//    context: Context,
//    private val screenWidth: Int,
//    private val screenHeight: Int
//) : GameMap(context, screenWidth, screenHeight) {
//
//    private val mapName = "Desert"
//    private val mapDifficulty = "Medium"
//
//    // Custom draw để thêm elements đặc trưng
//    fun drawCustomElements(canvas: Canvas, cameraX: Float, cameraY: Float) {
//        // Vẽ background màu vàng cam (sa mạc)
//        canvas.drawColor(Color.rgb(255, 220, 160))
//
//        drawDesertElements(canvas, cameraX, cameraY)
//    }
//
//    private fun drawDesertElements(canvas: Canvas, cameraX: Float, cameraY: Float) {
//        // Vẽ xương rồng
//        val cactusPaint = Paint().apply {
//            color = Color.rgb(50, 130, 50)
//            style = Paint.Style.FILL
//        }
//
//        for (i in 0..6) {
//            val cactusX = (i * 500f + 200f) - (cameraX * 0.9f)
//            val cactusY = groundY - cameraY
//
//            if (cactusX > -100 && cactusX < screenWidth + 100) {
//                // Thân xương rồng
//                canvas.drawRect(
//                    cactusX - 20f, cactusY - 80f,
//                    cactusX + 20f, cactusY,
//                    cactusPaint
//                )
//
//                // Cành trái
//                canvas.drawRect(
//                    cactusX - 40f, cactusY - 60f,
//                    cactusX - 20f, cactusY - 30f,
//                    cactusPaint
//                )
//
//                // Cành phải
//                canvas.drawRect(
//                    cactusX + 20f, cactusY - 55f,
//                    cactusX + 40f, cactusY - 25f,
//                    cactusPaint
//                )
//            }
//        }
//
//        // Vẽ đám cát bay nhẹ (hiệu ứng gió)
//        val sandPaint = Paint().apply {
//            color = Color.rgb(255, 230, 180)
//            alpha = 150
//            style = Paint.Style.FILL
//        }
//
//        for (i in 0..4) {
//            val sandX = (i * 400f + System.currentTimeMillis() / 20 % 400) - (cameraX * 0.3f)
//            val sandY = groundY - 100f - cameraY
//
//            if (sandX > -50 && sandX < screenWidth + 50) {
//                canvas.drawCircle(sandX, sandY, 8f, sandPaint)
//            }
//        }
//    }
//
//    fun getMapName() = mapName
//    fun getMapDifficulty() = mapDifficulty
//}