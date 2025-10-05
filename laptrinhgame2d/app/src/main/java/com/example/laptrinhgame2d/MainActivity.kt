package com.example.laptrinhgame2d

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var gameView: GameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ẩn thanh trạng thái và thanh điều hướng
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )

        // Giữ màn hình luôn sáng
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Lấy loại nhân vật từ intent (mặc định là Fighter)
        val characterType = intent.getStringExtra("CHARACTER_TYPE") ?: "Fighter"

        // Tạo GameView chỉ với characterType (KHÔNG CẦN mapType)
        gameView = GameView(this, characterType)
        setContentView(gameView)
    }
}