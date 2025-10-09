package com.example.laptrinhgame2d.maps

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.laptrinhgame2d.CharacterSelectionActivity
import com.example.laptrinhgame2d.R

class MapSelectionActivity : AppCompatActivity() {

    private lateinit var mapName: TextView
    private lateinit var mapImage: ImageView
    private lateinit var mapDifficulty: TextView
    private lateinit var mapDescription: TextView

    private var currentMapIndex = 0

    // ===== THÊM 2 MAP MỚI VÀO DANH SÁCH =====
    private val maps = listOf(
        MapInfo(
            id = 1,
            name = "GRASSLAND",
            difficulty = "Easy ⭐",
            description = "A peaceful meadow with green hills and clear skies. Perfect for beginners to learn the ropes!"
        ),
        MapInfo(
            id = 2,
            name = "DESERT",
            difficulty = "Medium ⭐⭐",
            description = "A scorching desert with cacti and sandstorms. Watch out for mirages and burning heat!"
        ),
        MapInfo(
            id = 3,
            name = "VOLCANO",
            difficulty = "Hard ⭐⭐⭐",
            description = "A dangerous volcanic wasteland with flowing lava and toxic smoke. Only for the bravest warriors!"
        ),
        // ===== 2 MAP MỚI =====
        MapInfo(
            id = 4,
            name = "ICE WORLD",
            difficulty = "Very Hard ⭐⭐⭐⭐",
            description = "A frozen wasteland with snowstorms and ice crystals. Enemies are stronger and more dangerous!"
        ),
        MapInfo(
            id = 5,
            name = "SPACE STATION",
            difficulty = "Extreme ⭐⭐⭐⭐⭐",
            description = "A cosmic station among the stars. The ultimate challenge with the most powerful enemies!"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_selection)

        supportActionBar?.hide()
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        initViews()
        updateMapDisplay()
        setupButtons()
    }

    private fun initViews() {
        mapName = findViewById(R.id.mapName)
        mapImage = findViewById(R.id.mapImage)
        mapDifficulty = findViewById(R.id.mapDifficulty)
        mapDescription = findViewById(R.id.mapDescription)
    }

    private fun updateMapDisplay() {
        val map = maps[currentMapIndex]

        mapName.text = map.name
        mapDifficulty.text = "Difficulty: ${map.difficulty}"
        mapDescription.text = map.description

        // ===== CẬP NHẬT MÀU CHO 5 MAPS =====
        when (map.id) {
            1 -> mapImage.setBackgroundColor(Color.rgb(34, 139, 34))      // Green cho Grassland
            2 -> mapImage.setBackgroundColor(Color.rgb(238, 203, 173))    // Sandy brown cho Desert
            3 -> mapImage.setBackgroundColor(Color.rgb(139, 69, 19))      // Dark red-brown cho Volcano
            4 -> mapImage.setBackgroundColor(Color.rgb(135, 206, 235))    // Sky blue cho Ice
            5 -> mapImage.setBackgroundColor(Color.rgb(25, 25, 112))      // Midnight blue cho Space
        }
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.prevMapButton).setOnClickListener {
            currentMapIndex = if (currentMapIndex > 0) {
                currentMapIndex - 1
            } else {
                maps.size - 1  // Quay về map cuối (Space)
            }
            updateMapDisplay()
        }

        findViewById<Button>(R.id.nextMapButton).setOnClickListener {
            currentMapIndex = if (currentMapIndex < maps.size - 1) {
                currentMapIndex + 1
            } else {
                0  // Quay về map đầu (Grassland)
            }
            updateMapDisplay()
        }

        findViewById<Button>(R.id.selectMapButton).setOnClickListener {
            val selectedMapId = maps[currentMapIndex].id
            val intent = Intent(this, CharacterSelectionActivity::class.java)
            intent.putExtra("MAP_TYPE", selectedMapId)
            startActivity(intent)
        }

        findViewById<Button>(R.id.backButton).setOnClickListener {
            finish()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }
    }

    data class MapInfo(
        val id: Int,
        val name: String,
        val difficulty: String,
        val description: String
    )
}