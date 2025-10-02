package com.example.laptrinhgame2d

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CharacterSelectionActivity : AppCompatActivity() {
    
    private var selectedCharacter = "Fighter"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_character_selection)
        
        // Ẩn action bar
        supportActionBar?.hide()
        
        // KHÔNG load background cho màn hình này
        
        val characterImage = findViewById<ImageView>(R.id.characterImage)
        val characterName = findViewById<TextView>(R.id.characterName)
        val selectButton = findViewById<Button>(R.id.selectButton)
        val prevButton = findViewById<Button>(R.id.prevButton)
        val nextButton = findViewById<Button>(R.id.nextButton)
        
        // Hiển thị Fighter mặc định
        updateCharacterDisplay(characterImage, characterName)
        
        // Nút Previous
        prevButton.setOnClickListener {
            selectedCharacter = when (selectedCharacter) {
                "Fighter" -> "Samurai_Commander"
                "Samurai_Archer" -> "Fighter"
                "Samurai_Commander" -> "Samurai_Archer"
                else -> "Fighter"
            }
            updateCharacterDisplay(characterImage, characterName)
        }
        
        // Nút Next
        nextButton.setOnClickListener {
            selectedCharacter = when (selectedCharacter) {
                "Fighter" -> "Samurai_Archer"
                "Samurai_Archer" -> "Samurai_Commander"
                "Samurai_Commander" -> "Fighter"
                else -> "Fighter"
            }
            updateCharacterDisplay(characterImage, characterName)
        }
        
        // Xử lý sự kiện click button Select
        selectButton.setOnClickListener {
            // Chuyển sang MainActivity (game) với nhân vật đã chọn
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("CHARACTER_TYPE", selectedCharacter)
            startActivity(intent)
            finish()
        }
    }
    
    private fun updateCharacterDisplay(imageView: ImageView, nameView: TextView) {
        try {
            when (selectedCharacter) {
                "Fighter" -> {
                    val inputStream = assets.open("heroes/Fighter/idle/Idle1.png")
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    imageView.setImageBitmap(bitmap)
                    inputStream.close()
                    
                    nameView.text = "FIGHTER"
                }
                "Samurai_Archer" -> {
                    val inputStream = assets.open("heroes/Samurai_Archer/idle/Idle1.png")
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    imageView.setImageBitmap(bitmap)
                    inputStream.close()
                    
                    nameView.text = "SAMURAI ARCHER"
                }
                "Samurai_Commander" -> {
                    val inputStream = assets.open("heroes/Samurai_Commander/idle/Idle1.png")
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    imageView.setImageBitmap(bitmap)
                    inputStream.close()
                    
                    nameView.text = "SAMURAI COMMANDER"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
