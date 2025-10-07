package com.example.laptrinhgame2d

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import com.example.laptrinhgame2d.victory.VictoryRecord

class LevelVictoryDialog(
    private val context: Context,
    private val victoryRecord: VictoryRecord,
    private val currentLevel: LevelManager.Level,
    private val onNextLevel: () -> Unit,
    private val onViewHistory: () -> Unit,
    private val onPlayAgain: () -> Unit,
    private val onMainMenu: () -> Unit
) {

    private var dialog: Dialog? = null

    fun show() {
        dialog = Dialog(context).apply {
            setCancelable(false)

            // Root container - RelativeLayout như XML
            val rootLayout = RelativeLayout(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(Color.parseColor("#CC000000"))
            }

            // ScrollView để có thể cuộn
            val scrollView = ScrollView(context).apply {
                layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    addRule(RelativeLayout.CENTER_IN_PARENT)
                }
                isFillViewport = true
            }

            // Main content container
            val mainContainer = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = ViewGroup.LayoutParams(
                    dpToPx(320),  // 320dp width như XML
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))

                // Background giống XML
                val bgDrawable = GradientDrawable().apply {
                    colors = intArrayOf(
                        Color.parseColor("#2C3E50"),
                        Color.parseColor("#34495E")
                    )
                    orientation = GradientDrawable.Orientation.TOP_BOTTOM
                    cornerRadius = dpToPx(16).toFloat()
                    setStroke(dpToPx(3), Color.parseColor("#FFD700"))
                }
                background = bgDrawable
                elevation = dpToPx(16).toFloat()
            }

            // Title TextView
            val titleText = TextView(context).apply {
                text = "${currentLevel.displayName} Complete!"
                textSize = 24f
                setTextColor(Color.parseColor("#FFD700"))
                gravity = Gravity.CENTER
                typeface = Typeface.DEFAULT_BOLD
                setShadowLayer(4f, 2f, 2f, Color.BLACK)

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER
                    setMargins(0, 0, 0, dpToPx(12))
                }
            }
            mainContainer.addView(titleText)

            // Stats container
            val statsContainer = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, dpToPx(12))
                }
                setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10))
                setBackgroundColor(Color.parseColor("#34495E"))
            }

            // Time stat
            val timeText = TextView(context).apply {
                text = "⏱ Time: ${victoryRecord.getFormattedTime()}"
                textSize = 16f
                setTextColor(Color.parseColor("#2ECC71"))
                gravity = Gravity.CENTER
                typeface = Typeface.DEFAULT_BOLD
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER
                    setMargins(0, 0, 0, dpToPx(6))
                }
            }
            statsContainer.addView(timeText)
            
            // Score stat (ĐIỂM CHÍNH)
            val scoreText = TextView(context).apply {
                text = "⭐ Score: ${victoryRecord.totalScore}"
                textSize = 20f
                setTextColor(Color.parseColor("#F39C12"))
                gravity = Gravity.CENTER
                typeface = Typeface.DEFAULT_BOLD
                setShadowLayer(3f, 1f, 1f, Color.BLACK)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER
                    setMargins(0, 0, 0, dpToPx(8))
                }
            }
            statsContainer.addView(scoreText)
            
            // Bonus details (nếu có bonus)
            if (victoryRecord.bonusScore > 0) {
                val bonusDetailsContainer = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(dpToPx(10), 0, dpToPx(10), dpToPx(8))
                    }
                    setPadding(dpToPx(8), dpToPx(6), dpToPx(8), dpToPx(6))
                    
                    val bgDrawable = GradientDrawable().apply {
                        setColor(Color.parseColor("#27AE60"))
                        cornerRadius = dpToPx(8).toFloat()
                    }
                    background = bgDrawable
                }
                
                // Bonus header
                val bonusHeader = TextView(context).apply {
                    text = "🎁 BONUS: +${victoryRecord.bonusScore}"
                    textSize = 14f
                    setTextColor(Color.parseColor("#FFD700"))
                    gravity = Gravity.CENTER
                    typeface = Typeface.DEFAULT_BOLD
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.CENTER
                        setMargins(0, 0, 0, dpToPx(4))
                    }
                }
                bonusDetailsContainer.addView(bonusHeader)
                
                // Individual bonus achievements
                if (victoryRecord.achievedTimeBonus) {
                    val timeBonusText = TextView(context).apply {
                        text = "✓ Speed Bonus: +1000"
                        textSize = 11f
                        setTextColor(Color.WHITE)
                        gravity = Gravity.CENTER
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            gravity = Gravity.CENTER
                            setMargins(0, 0, 0, dpToPx(2))
                        }
                    }
                    bonusDetailsContainer.addView(timeBonusText)
                }
                
                if (victoryRecord.achievedNoHitBonus) {
                    val flawlessPoints = victoryRecord.flawlessScore
                    val noHitBonusText = TextView(context).apply {
                        text = "✓ Flawless Bonus: +$flawlessPoints"
                        textSize = 11f
                        setTextColor(Color.WHITE)
                        gravity = Gravity.CENTER
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            gravity = Gravity.CENTER
                            setMargins(0, 0, 0, dpToPx(2))
                        }
                    }
                    bonusDetailsContainer.addView(noHitBonusText)
                }
                
                if (victoryRecord.achievedComboBonus) {
                    val comboBonusText = TextView(context).apply {
                        text = "✓ Combo Master: +500"
                        textSize = 11f
                        setTextColor(Color.WHITE)
                        gravity = Gravity.CENTER
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            gravity = Gravity.CENTER
                        }
                    }
                    bonusDetailsContainer.addView(comboBonusText)
                }
                
                statsContainer.addView(bonusDetailsContainer)
            }

            // Hero stat
            val heroText = TextView(context).apply {
                text = "⚔️ Hero: ${victoryRecord.characterType}"
                textSize = 13f
                setTextColor(Color.parseColor("#ECF0F1"))
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER
                    setMargins(0, 0, 0, dpToPx(6))
                }
            }
            statsContainer.addView(heroText)

            // Enemies stat
            val enemiesText = TextView(context).apply {
                text = "💀 Enemies Defeated: ${victoryRecord.enemiesKilled}"
                textSize = 13f
                setTextColor(Color.parseColor("#ECF0F1"))
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER
                }
            }
            statsContainer.addView(enemiesText)

            mainContainer.addView(statsContainer)

            // Next Level Button
            val nextLevelBtn = createXMLStyleButton(
                "➤ NEXT LEVEL",
                Color.parseColor("#E74C3C"),
                14f,
                dpToPx(42)
            ) {
                dismiss()
                onNextLevel()
            }
            mainContainer.addView(nextLevelBtn)

            // View History Button
            val historyBtn = createXMLStyleButton(
                "VIEW HISTORY",
                Color.parseColor("#3498DB"),
                12f,
                dpToPx(36)
            ) {
                dismiss()
                onViewHistory()
            }
            mainContainer.addView(historyBtn)

            // Play Again Button
            val playAgainBtn = createXMLStyleButton(
                "REPLAY LEVEL",
                Color.parseColor("#F39C12"),
                12f,
                dpToPx(36)
            ) {
                dismiss()
                onPlayAgain()
            }
            mainContainer.addView(playAgainBtn)

            // Main Menu Button
            val mainMenuBtn = createXMLStyleButton(
                "MAIN MENU",
                Color.parseColor("#95A5A6"),
                12f,
                dpToPx(36)
            ) {
                dismiss()
                onMainMenu()
            }
            // Remove bottom margin for last button
            val params = mainMenuBtn.layoutParams as LinearLayout.LayoutParams
            params.setMargins(0, 0, 0, 0)
            mainMenuBtn.layoutParams = params
            mainContainer.addView(mainMenuBtn)

            scrollView.addView(mainContainer)
            rootLayout.addView(scrollView)

            setContentView(rootLayout)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            // Set dialog size
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            show()
        }
    }

    private fun createXMLStyleButton(
        text: String,
        backgroundColor: Int,
        textSize: Float,
        height: Int,
        onClick: () -> Unit
    ): Button {
        return Button(context).apply {
            this.text = text
            this.textSize = textSize
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD

            // Create button background với rounded corners
            val buttonBg = GradientDrawable().apply {
                setColor(backgroundColor)
                cornerRadius = dpToPx(8).toFloat()
                setStroke(dpToPx(1), darkenColor(backgroundColor))
            }
            background = buttonBg

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                height
            ).apply {
                setMargins(0, 0, 0, dpToPx(8))
            }

            elevation = dpToPx(4).toFloat()
            setOnClickListener { onClick() }
        }
    }

    // Utility functions
    private fun dpToPx(dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    private fun darkenColor(color: Int): Int {
        val factor = 0.8f
        val a = Color.alpha(color)
        val r = (Color.red(color) * factor).toInt()
        val g = (Color.green(color) * factor).toInt()
        val b = (Color.blue(color) * factor).toInt()
        return Color.argb(a, r, g, b)
    }

    fun dismiss() {
        dialog?.dismiss()
        dialog = null
    }

    fun isShowing(): Boolean {
        return dialog?.isShowing == true
    }
}