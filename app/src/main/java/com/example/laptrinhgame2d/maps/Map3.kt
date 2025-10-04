package com.example.laptrinhgame2d.maps;

class Map3 {
    val levelName = "Frozen Kingdom";
    val background = "ice";
    val difficulty = 3;
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
    );
    val enemies = listOf("IceGolem", "FrostSpider");
    val items = listOf("FirePotion", "Crystal");
}