package com.example.laptrinhgame2d

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LevelSelectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_level_select)

        // Level previews
        val forestPreview = findViewById<ImageView>(R.id.forestPreview)
        val desertPreview = findViewById<ImageView>(R.id.desertPreview)
        val icePreview = findViewById<ImageView>(R.id.icePreview)

        // Difficulty info
        val levelInfo = findViewById<TextView>(R.id.levelInfo)

        // Buttons for selection
        val forestButton = findViewById<Button>(R.id.forestButton)
        val desertButton = findViewById<Button>(R.id.desertButton)
        val iceButton = findViewById<Button>(R.id.iceButton)

        // Set click listeners for level selection
        forestButton.setOnClickListener {
            levelInfo.text = "Selected: Forest - Difficulty: Easy"
            navigateToCharacterSelection("Forest")
        }

        desertButton.setOnClickListener {
            levelInfo.text = "Selected: Desert - Difficulty: Medium"
            navigateToCharacterSelection("Desert")
        }

        iceButton.setOnClickListener {
            levelInfo.text = "Selected: Ice - Difficulty: Hard"
            navigateToCharacterSelection("Ice")
        }
    }

    private fun navigateToCharacterSelection(level: String) {
        val intent = Intent(this, CharacterSelectionActivity::class.java)
        intent.putExtra("SELECTED_LEVEL", level)
        startActivity(intent)
    }
}