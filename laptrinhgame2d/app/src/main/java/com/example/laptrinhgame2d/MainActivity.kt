package com.example.laptrinhgame2d

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var gameView: GameView
    //private lateinit var gameView: GameView_v2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ẩn thanh trạng thái và thanh điều hướng để có trải nghiệm toàn màn hình
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )

        // Giữ màn hình luôn sáng
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Lấy loại nhân vật từ intent (mặc định là Fighter)
        val characterType = intent.getStringExtra("CHARACTER_TYPE") ?: "Fighter"

        // ===== THÊM: Lấy map type từ intent (mặc định là 1 - Grassland) =====
        val mapType = intent.getIntExtra("MAP_TYPE", 1)

        // Tạo và set GameView với loại nhân vật và map type
        gameView = GameView(this, characterType, mapType)
        //gameView = GameView_v2(this, characterType, mapType)
        setContentView(gameView)
    }
}