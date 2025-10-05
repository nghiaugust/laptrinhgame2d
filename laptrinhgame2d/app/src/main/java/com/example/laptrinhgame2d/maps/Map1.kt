package com.example.laptrinhgame2d.maps

class Map1 {
    val name: String = "Forest Level"
    val description: String = "A lush forest filled with obstacles and hidden treasures."
    val width: Int = 20
    val height: Int = 15

    // Add further map elements here
    val obstacles: List<String> = listOf("Tree", "Rock", "River")
    val treasures: List<String> = listOf("Gold Coin", "Magic Potion")

    fun displayMapInfo() {
        println("Map Name: $name")
        println("Description: $description")
        println("Dimensions: ${width}x$height")
        println("Obstacles: $obstacles")
        println("Treasures: $treasures")
    }
}