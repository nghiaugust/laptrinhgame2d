package com.example.laptrinhgame2d

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MapSelectionActivity : AppCompatActivity() {

    private var selectedMap = 1 // 1, 2, hoặc 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_selection)

        // Ẩn action bar
        supportActionBar?.hide()

        // Toàn màn hình
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Load background
        try {
            val layout = findViewById<View>(R.id.mapSelectionLayout)
            val inputStream = assets.open("common/background.jpg")
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val drawable = BitmapDrawable(resources, bitmap)
            layout.background = drawable
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
            findViewById<View>(R.id.mapSelectionLayout).setBackgroundColor(
                android.graphics.Color.BLACK
            )
        }

        val mapImage = findViewById<ImageView>(R.id.mapImage)
        val mapName = findViewById<TextView>(R.id.mapName)
        val mapDifficulty = findViewById<TextView>(R.id.mapDifficulty)
        val mapDescription = findViewById<TextView>(R.id.mapDescription)
        val selectButton = findViewById<Button>(R.id.selectMapButton)
        val prevButton = findViewById<Button>(R.id.prevMapButton)
        val nextButton = findViewById<Button>(R.id.nextMapButton)
        val backButton = findViewById<Button>(R.id.backButton)

        // Hiển thị Map 1 mặc định
        updateMapDisplay(mapImage, mapName, mapDifficulty, mapDescription)

        // Nút Previous
        prevButton.setOnClickListener {
            selectedMap = when (selectedMap) {
                1 -> 3
                2 -> 1
                3 -> 2
                else -> 1
            }
            updateMapDisplay(mapImage, mapName, mapDifficulty, mapDescription)
        }

        // Nút Next
        nextButton.setOnClickListener {
            selectedMap = when (selectedMap) {
                1 -> 2
                2 -> 3
                3 -> 1
                else -> 1
            }
            updateMapDisplay(mapImage, mapName, mapDifficulty, mapDescription)
        }

        // Nút Select - chuyển sang màn hình chọn nhân vật (CODE CŨ)
        selectButton.setOnClickListener {
            val intent = Intent(this, CharacterSelectionActivity::class.java)
            intent.putExtra("SELECTED_MAP", selectedMap) // Truyền map đã chọn
            startActivity(intent)
        }

        // Nút Back - quay về main menu
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun updateMapDisplay(
        imageView: ImageView,
        nameView: TextView,
        difficultyView: TextView,
        descriptionView: TextView
    ) {
        when (selectedMap) {
            1 -> {
                nameView.text = "GRASSLAND"
                difficultyView.text = "Difficulty: Easy ⭐"
                difficultyView.setTextColor(android.graphics.Color.GREEN)
                descriptionView.text = "A peaceful meadow with green hills and clear skies. Perfect for beginners!"

                // Dùng ảnh có sẵn để preview
                try {
                    val inputStream = assets.open("backgrounds/hills.png")
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    imageView.setImageBitmap(bitmap)
                    inputStream.close()
                } catch (e: Exception) {
                    imageView.setBackgroundColor(android.graphics.Color.rgb(100, 200, 100))
                }
            }
            2 -> {
                nameView.text = "DESERT"
                difficultyView.text = "Difficulty: Medium ⭐⭐"
                difficultyView.setTextColor(android.graphics.Color.YELLOW)
                descriptionView.text = "A harsh desert with scorching heat. More enemies!"

                try {
                    val inputStream = assets.open("backgrounds/mountains.png")
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    imageView.setImageBitmap(bitmap)
                    inputStream.close()
                } catch (e: Exception) {
                    imageView.setBackgroundColor(android.graphics.Color.rgb(255, 220, 150))
                }
            }
            3 -> {
                nameView.text = "VOLCANO"
                difficultyView.text = "Difficulty: Hard ⭐⭐⭐"
                difficultyView.setTextColor(android.graphics.Color.RED)
                descriptionView.text = "A treacherous volcano. Most enemies! Only for brave warriors!"

                try {
                    val inputStream = assets.open("backgrounds/sky.png")
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    imageView.setImageBitmap(bitmap)
                    inputStream.close()
                } catch (e: Exception) {
                    imageView.setBackgroundColor(android.graphics.Color.rgb(180, 60, 20))
                }
            }
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
}