package com.example.laptrinhgame2d

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class CharacterSelectionActivity : AppCompatActivity() {

    private lateinit var characterName: TextView
    private lateinit var characterImage: ImageView
    private lateinit var characterHealth: TextView
    private lateinit var characterAttack: TextView
    private lateinit var characterSkill: TextView

    private var currentCharacterIndex = 0

    // ===== THÊM: Nhận MAP_TYPE từ MapSelectionActivity =====
    private var selectedMapType = 1 // Mặc định là Grassland

    private val characters = listOf(
        CharacterInfo(
            id = 1,
            name = "FIGHTER",
            type = "Fighter",
            health = "❤️ 150 HP",
            attack = "⚔️ 25 DMG",
            skill = "🛡️ Shield Defense"
        ),
        CharacterInfo(
            id = 2,
            name = "SAMURAI ARCHER",
            type = "Samurai_Archer",
            health = "❤️ 120 HP",
            attack = "⚔️ 20 DMG / 🏹 15 DMG",
            skill = "🏹 Ranged Combat + Charged Attack"
        ),
        CharacterInfo(
            id = 3,
            name = "SAMURAI COMMANDER",
            type = "Samurai_Commander",
            health = "❤️ 140 HP",
            attack = "⚔️ 30 DMG",
            skill = "⚡ Heavy Attacks"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_character_selection)

        supportActionBar?.hide()

        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // ===== THÊM: Lấy MAP_TYPE từ intent =====
        selectedMapType = intent.getIntExtra("MAP_TYPE", 1)

        initViews()
        updateCharacterDisplay()
        setupButtons()
    }

    private fun initViews() {
        characterName = findViewById(R.id.characterName)
        characterImage = findViewById(R.id.characterImage)
        characterHealth = findViewById(R.id.characterHealth)
        characterAttack = findViewById(R.id.characterAttack)
        characterSkill = findViewById(R.id.characterSkill)
    }

    private fun updateCharacterDisplay() {
        val character = characters[currentCharacterIndex]

        characterName.text = character.name
        characterHealth.text = character.health
        characterAttack.text = character.attack
        characterSkill.text = character.skill

        // ===== THAY ĐỔI: Load hình ảnh từ assets như code cũ =====
        loadCharacterImage(character.type)
    }

    private fun loadCharacterImage(characterType: String) {
        try {
            when (characterType) {
                "Fighter" -> {
                    val inputStream = assets.open("heroes/Fighter/idle/Idle1.png")
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    characterImage.setImageBitmap(bitmap)
                    inputStream.close()
                }
                "Samurai_Archer" -> {
                    val inputStream = assets.open("heroes/Samurai_Archer/idle/Idle1.png")
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    characterImage.setImageBitmap(bitmap)
                    inputStream.close()
                }
                "Samurai_Commander" -> {
                    val inputStream = assets.open("heroes/Samurai_Commander/idle/Idle1.png")
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    characterImage.setImageBitmap(bitmap)
                    inputStream.close()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Nếu không load được hình, sử dụng màu background như backup
            when (characterType) {
                "Fighter" -> characterImage.setBackgroundColor(android.graphics.Color.rgb(200, 100, 100))
                "Samurai_Archer" -> characterImage.setBackgroundColor(android.graphics.Color.rgb(100, 150, 200))
                "Samurai_Commander" -> characterImage.setBackgroundColor(android.graphics.Color.rgb(150, 100, 200))
            }
        }
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.prevCharButton).setOnClickListener {
            currentCharacterIndex = if (currentCharacterIndex > 0) {
                currentCharacterIndex - 1
            } else {
                characters.size - 1
            }
            updateCharacterDisplay()
        }

        findViewById<Button>(R.id.nextCharButton).setOnClickListener {
            currentCharacterIndex = if (currentCharacterIndex < characters.size - 1) {
                currentCharacterIndex + 1
            } else {
                0
            }
            updateCharacterDisplay()
        }

        // ===== THAY ĐỔI: Truyền cả CHARACTER_TYPE và MAP_TYPE sang MainActivity =====
        findViewById<Button>(R.id.selectCharButton).setOnClickListener {
            val selectedCharacterType = characters[currentCharacterIndex].type
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("CHARACTER_TYPE", selectedCharacterType)
            intent.putExtra("MAP_TYPE", selectedMapType) // Truyền MAP_TYPE
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

    data class CharacterInfo(
        val id: Int,
        val name: String,
        val type: String,
        val health: String,
        val attack: String,
        val skill: String
    )
}