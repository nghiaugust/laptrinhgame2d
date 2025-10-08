package com.example.laptrinhgame2d

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.laptrinhgame2d.victory.VictoryRecord

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_VICTORY_HISTORY = 1234
    }

    private lateinit var gameView: GameView
    private var lastVictoryRecord: VictoryRecord? = null
    private var lastLevel: LevelManager.Level? = null
    private var victoryDialog: LevelVictoryDialog? = null
    private var characterType: String = "Fighter"
    private var mapType: Int = 1
    private var levelManager: LevelManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        characterType = intent.getStringExtra("CHARACTER_TYPE") ?: "Fighter"
        mapType = intent.getIntExtra("MAP_TYPE", 1)
        if (mapType == 1) {
            GameView.resetAllHP()
        }

        levelManager = LevelManager(this)
        val level = levelManager!!.getLevelFromMapType(mapType)
        gameView = GameView(this, characterType, mapType)
        setContentView(gameView)
    }

    // Hàm này được GameView gọi khi thắng màn, hoặc khi quay về từ History
    fun showLevelVictoryDialog(victoryRecord: VictoryRecord, level: LevelManager.Level) {
        lastVictoryRecord = victoryRecord
        lastLevel = level
        val dialog = LevelVictoryDialog(
            this,
            victoryRecord,
            level,
            onNextLevel = { startNextLevel() },
            onViewHistory = { openVictoryHistory() },
            onPlayAgain = { replayLevel() },
            onMainMenu = { goToMainMenu() }
        )
        dialog.show()
    }

    private fun openVictoryHistory() {
        val intent = Intent(this, com.example.laptrinhgame2d.victory.VictoryHistoryActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_VICTORY_HISTORY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_VICTORY_HISTORY && resultCode == Activity.RESULT_OK) {
            // Show lại dialog Victory với dữ liệu cũ khi quay về từ màn lịch sử
            lastVictoryRecord?.let { record ->
                lastLevel?.let { level ->
                    showLevelVictoryDialog(record, level)
                }
            }
        }
    }

    // Chuyển sang màn tiếp theo khi bấm Next Level
    private fun startNextLevel() {
        victoryDialog?.dismiss()
        // Dừng gameView cũ triệt để
        if (::gameView.isInitialized) {
            gameView.pauseGame()
            gameView.releaseResources() // Nếu có
        }
        Handler(Looper.getMainLooper()).post {
            val nextLevel = levelManager?.getNextLevel(lastLevel!!)
            if (nextLevel != null) {
                gameView = GameView(this, characterType, nextLevel.mapType)
                setContentView(gameView)
            }
        }
    }

    // Chơi lại màn hiện tại
    private fun replayLevel() {
        if (lastLevel == null) return
        mapType = lastLevel!!.mapType
        gameView = GameView(this, characterType, mapType)
        setContentView(gameView)
    }

    // Về menu chính
    private fun goToMainMenu() {
        val intent = Intent(this, MainMenuActivity::class.java)
        startActivity(intent)
        finish()
    }
}