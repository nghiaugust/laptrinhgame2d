//package com.example.laptrinhgame2d
//
//import android.content.Context
//import android.graphics.Canvas
//
//class MapManager(private val context: Context) {
//
//    enum class MapType {
//        FOREST,    // Map 1 - Grassland
//        DESERT,    // Map 2 - Desert
//        VOLCANO    // Map 3 - Volcano
//    }
//
//    // Wrapper class để chứa map và xử lý custom drawing
//    inner class CustomGameMap(
//        val context: Context,
//        val screenWidth: Int,
//        val screenHeight: Int,
//        private val baseMap: GameMap,
//        private val customMap: Any? = null
//    ) : GameMap(context, screenWidth, screenHeight) {
//
//        override fun update(cameraX: Float, cameraY: Float) {
//            baseMap.update(cameraX, cameraY)
//            // Update custom cho Volcano
//            if (customMap is VolcanoMap) {
//                customMap.updateCustom()
//            }
//        }
//
//        override fun draw(canvas: Canvas, cameraX: Float, cameraY: Float) {
//            // Vẽ custom elements trước (background)
//            when (customMap) {
//                is DesertMap -> customMap.drawCustomElements(canvas, cameraX, cameraY)
//                is VolcanoMap -> customMap.drawCustomElements(canvas, cameraX, cameraY)
//            }
//
//            // Vẽ base GameMap (parallax layers, ground)
//            baseMap.draw(canvas, cameraX, cameraY)
//
//            // Vẽ custom elements sau (foreground)
//            when (customMap) {
//                is Map1 -> customMap.drawCustomElements(canvas, cameraX, cameraY)
//            }
//        }
//
//        override fun cleanup() {
//            baseMap.cleanup()
//        }
//    }
//
//    fun createGameMap(mapType: MapType, screenWidth: Int, screenHeight: Int): GameMap {
//        return when (mapType) {
//            MapType.FOREST -> {
//                val map1 = Map1(context, screenWidth, screenHeight)
//                CustomGameMap(context, screenWidth, screenHeight, map1, map1)
//            }
//            MapType.DESERT -> {
//                val desertMap = DesertMap(context, screenWidth, screenHeight)
//                CustomGameMap(context, screenWidth, screenHeight, desertMap, desertMap)
//            }
//            MapType.VOLCANO -> {
//                val volcanoMap = VolcanoMap(context, screenWidth, screenHeight)
//                CustomGameMap(context, screenWidth, screenHeight, volcanoMap, volcanoMap)
//            }
//        }
//    }
//
//    fun getMapName(mapType: MapType): String {
//        return when (mapType) {
//            MapType.FOREST -> "Grassland"
//            MapType.DESERT -> "Desert"
//            MapType.VOLCANO -> "Volcano"
//        }
//    }
//
//    fun getMapDifficulty(mapType: MapType): String {
//        return when (mapType) {
//            MapType.FOREST -> "Easy"
//            MapType.DESERT -> "Medium"
//            MapType.VOLCANO -> "Hard"
//        }
//    }
//}
//DESERT -> {
//    // Tạo GameMap với Desert theme
//    DesertMap(context, screenWidth, screenHeight)
//}
//MapType.VOLCANO -> {
//    // Tạo GameMap với Volcano theme
//    VolcanoMap(context, screenWidth, screenHeight)
//}
//}
//}
//
//fun getMapName(mapType: MapType): String {
//    return when (mapType) {
//        MapType.FOREST -> "Grassland"
//        MapType.DESERT -> "Desert"
//        MapType.VOLCANO -> "Volcano"
//    }
//}
//
//fun getMapDifficulty(mapType: MapType): String {
//    return when (mapType) {
//        MapType.FOREST -> "Easy"
//        MapType.DESERT -> "Medium"
//        MapType.VOLCANO -> "Hard"
//    }
//}
//}