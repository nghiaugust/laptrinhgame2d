package com.game.maps

/**
 * Map3 - Ice Level (Màn băng tuyết)
 * Bối cảnh: Vùng đất băng giá với khối băng và tuyết
 */
class Map3 {
    val levelName = "Frozen Kingdom"
    val background = "ice"
    val difficulty = 3
    
    // Layout đơn giản: I = Ice block, P = Player spawn, . = ice path, S = Snow pile
    val layout = arrayOf(
        "I I I I I I I I I I",
        "I . . S . . S . . I",
        "I . I . . . . I . I",
        "I S . . . . . . S I",
        "I . . . P . . . . I",
        "I S . . . . . . S I",
        "I . I . . . . I . I",
        "I . . S . . S . . I",
        "I I I I I I I I I I"
    )
    
    val enemies = listOf("IceGolem", "FrostSpider")
    val items = listOf("FirePotion", "Crystal")
}