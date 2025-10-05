//package com.example.laptrinhgame2d
//
//import android.content.Context
//import android.graphics.Canvas
//import android.graphics.Color
//import android.graphics.Paint
//
///**
// * Map 1: Grassland - Đồng cỏ xanh mát
// * Background: Bầu trời xanh, mây trắng, đồi cỏ xanh
// * Theme: Dễ nhất, môi trường thân thiện
// */
//class Map1(
//    context: Context,
//    private val screenWidth: Int,
//    private val screenHeight: Int
//) : GameMap(context, screenWidth, screenHeight) {
//
//    private val mapName = "Grassland"
//    private val mapDifficulty = "Easy"
//
//    // Custom draw để thêm elements đặc trưng
//    fun drawCustomElements(canvas: Canvas, cameraX: Float, cameraY: Float) {
//        drawGrasslandElements(canvas, cameraX, cameraY)
//    }
//
//    private fun drawGrasslandElements(canvas: Canvas, cameraX: Float, cameraY: Float) {
//        // Vẽ một số bụi cỏ nhỏ trên ground
//        val grassPaint = Paint().apply {
//            color = Color.rgb(50, 180, 50)
//            strokeWidth = 8f
//            style = Paint.Style.STROKE
//        }
//
//        // Vẽ cỏ ở nhiều vị trí khác nhau (parallax với camera)
//        for (i in 0..10) {
//            val grassX = (i * 300f) - (cameraX * 0.8f)
//            val grassY = groundY - cameraY
//
//            // Chỉ vẽ nếu trong màn hình
//            if (grassX > -100 && grassX < screenWidth + 100) {
//                // Vẽ 3 ngọn cỏ
//                canvas.drawLine(grassX, grassY, grassX - 10f, grassY - 30f, grassPaint)
//                canvas.drawLine(grassX, grassY, grassX, grassY - 35f, grassPaint)
//                canvas.drawLine(grassX, grassY, grassX + 10f, grassY - 28f, grassPaint)
//            }
//        }
//
//        // Vẽ một vài hoa nhỏ
//        val flowerPaint = Paint().apply {
//            color = Color.rgb(255, 200, 100)
//            style = Paint.Style.FILL
//        }
//
//        for (i in 0..8) {
//            val flowerX = (i * 350f + 100f) - (cameraX * 0.8f)
//            val flowerY = groundY - 15f - cameraY
//
//            if (flowerX > -50 && flowerX < screenWidth + 50) {
//                canvas.drawCircle(flowerX, flowerY, 12f, flowerPaint)
//            }
//        }
//    }
//
//    fun getMapName() = mapName
//    fun getMapDifficulty() = mapDifficulty
//}