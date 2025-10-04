package com.game.maps

/**
 * Map1 - Forest Level (Màn rừng)
 * Bối cảnh: Rừng cây xanh tươi với đường mòn
 */
class Map1 {
    val levelName = "Forest Adventure"
    val background = "forest"
    val difficulty = 1
    
    // Layout đơn giản: T = Tree, P = Player spawn, . = walkable path, # = obstacle
    val layout = arrayOf(
        "T T T T T T T T T T",
        "T . . . . . . . . T",
        "T . # . . . . # . T",
        "T . . . P . . . . T",
        "T . # . . . . # . T",
        "T . . . . . . . . T",
        "T T T T T T T T T T"
    )
    
    val enemies = listOf("Goblin", "Wolf")
    val items = listOf("HealthPotion", "Coin")
}