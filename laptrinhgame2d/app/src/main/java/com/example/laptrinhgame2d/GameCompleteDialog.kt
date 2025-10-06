package com.example.laptrinhgame2d

import android.app.Dialog
import android.content.Context
import android.content.Intent
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

class GameCompleteDialog(
    private val context: Context,
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
                    dpToPx(350),  // 350dp width như XML
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20))

                // Background giống XML nhưng đẹp hơn
                val bgDrawable = GradientDrawable().apply {
                    colors = intArrayOf(
                        Color.parseColor("#1A252F"),
                        Color.parseColor("#2C3E50"),
                        Color.parseColor("#34495E")
                    )
                    orientation = GradientDrawable.Orientation.TOP_BOTTOM
                    cornerRadius = dpToPx(20).toFloat()
                    setStroke(dpToPx(4), Color.parseColor("#FFD700"))
                }
                background = bgDrawable
                elevation = dpToPx(16).toFloat()
            }

            // BRAVO Title
            val titleText = TextView(context).apply {
                text = "🎉 BRAVO! 🎉"
                textSize = 32f
                setTextColor(Color.parseColor("#FFD700"))
                gravity = Gravity.CENTER
                typeface = Typeface.DEFAULT_BOLD
                setShadowLayer(5f, 3f, 3f, Color.BLACK)

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER
                    setMargins(0, 0, 0, dpToPx(16))
                }
            }
            mainContainer.addView(titleText)

            // Congratulations message
            val messageText = TextView(context).apply {
                text = "Congratulations!\nYou have completed the entire game!\nAll levels conquered!"
                textSize = 16f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                setShadowLayer(2f, 1f, 1f, Color.BLACK)

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER
                    setMargins(0, 0, 0, dpToPx(20))
                }
            }
            mainContainer.addView(messageText)

            // Play Again Button
            val playAgainBtn = createXMLStyleButton(
                "🎮 PLAY AGAIN",
                Color.parseColor("#27AE60"),
                16f,
                dpToPx(48)
            ) {
                dismiss()
                onPlayAgain()

                // Restart game từ Grassland
                val intent = Intent(context, CharacterSelectionActivity::class.java).apply {
                    putExtra("MAP_TYPE", 1) // Grassland
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                (context as? MainActivity)?.finish()
            }
            mainContainer.addView(playAgainBtn)

            // Main Menu Button
            val mainMenuBtn = createXMLStyleButton(
                "🏠 MAIN MENU",
                Color.parseColor("#E74C3C"),
                16f,
                dpToPx(48)
            ) {
                dismiss()
                onMainMenu()

                val intent = Intent(context, MainMenuActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                (context as? MainActivity)?.finish()
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

            // Create button background với gradient effect
            val buttonBg = GradientDrawable().apply {
                colors = intArrayOf(backgroundColor, darkenColor(backgroundColor))
                orientation = GradientDrawable.Orientation.TOP_BOTTOM
                cornerRadius = dpToPx(12).toFloat()
                setStroke(dpToPx(2), Color.parseColor("#FFD700"))
            }
            background = buttonBg

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                height
            ).apply {
                setMargins(0, 0, 0, dpToPx(12))
            }

            elevation = dpToPx(4).toFloat()
            setShadowLayer(4f, 0f, 2f, Color.BLACK)
            setOnClickListener { onClick() }
        }
    }

    // Utility functions
    private fun dpToPx(dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    private fun darkenColor(color: Int): Int {
        val factor = 0.7f
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