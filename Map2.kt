package com.game.maps

/**
 * Map2 - Desert Level (Màn sa mạc)
 * Bối cảnh: Sa mạc khô cằn với xương rồng và cát
 */
class Map2 {
    val levelName = "Desert Storm"
    val background = "desert"
    val difficulty = 2
    
    // Layout đơn giản: C = Cactus, P = Player spawn, . = sand path, # = rock
    val layout = arrayOf(
        "C C C C C C C C C C",
        "C . . . . . . . . C",
        "C . # . C . C . # C",
        "C . . . . . . . . C",
        "C . C . P . . C . C",
        "C . . . . . . . . C",
        "C . # . C . C . # C",
        "C . . . . . . . . C",
        "C C C C C C C C C C"
    )
    
    val enemies = listOf("Scorpion", "SandWorm")
    val items = listOf("WaterBottle", "Gem")
}