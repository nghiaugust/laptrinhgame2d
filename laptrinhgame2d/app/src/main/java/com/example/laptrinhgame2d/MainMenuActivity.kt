package com.example.laptrinhgame2d

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainMenuActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)
        
        // Ẩn action bar
        supportActionBar?.hide()
        
        // Ẩn thanh trạng thái và thanh điều hướng để có trải nghiệm toàn màn hình
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )
        
        // Giữ màn hình luôn sáng
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        // Load background từ assets và set vào layout
        try {
            val layout = findViewById<View>(R.id.mainMenuLayout)
            val inputStream = assets.open("common/background.jpg")
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val drawable = BitmapDrawable(resources, bitmap)
            layout.background = drawable
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
            // Nếu load thất bại, dùng màu đen làm backup
            findViewById<View>(R.id.mainMenuLayout).setBackgroundColor(android.graphics.Color.BLACK)
        }
        
        // Nút Start Game
        val startButton = findViewById<Button>(R.id.startGameButton)
        startButton.setOnClickListener {
            // Chuyển sang màn hình chọn nhân vật
            val intent = Intent(this, CharacterSelectionActivity::class.java)
            startActivity(intent)
        }
    }
    
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // Giữ chế độ toàn màn hình khi quay lại activity
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
        }
    }
}
