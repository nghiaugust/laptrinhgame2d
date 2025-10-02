package com.example.laptrinhgame2d

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class Joystick(
    var centerX: Float,
    var centerY: Float,
    private val buttonWidth: Float,
    private val buttonHeight: Float
) {
    private var leftPressed = false
    private var rightPressed = false
    var leftPointerId = -1
    var rightPointerId = -1
    
    private val buttonPaint = Paint().apply {
        color = Color.argb(120, 150, 150, 150)
        style = Paint.Style.FILL
    }
    
    private val buttonPressedPaint = Paint().apply {
        color = Color.argb(200, 100, 150, 255)
        style = Paint.Style.FILL
    }
    
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 50f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    
    // Tính toán vị trí nút dựa trên centerX và centerY hiện tại
    private fun getLeftButtonBounds(): FloatArray {
        return floatArrayOf(
            centerX - buttonWidth - 10f,  // left
            centerY - buttonHeight / 2,    // top
            centerX - 10f,                 // right
            centerY + buttonHeight / 2     // bottom
        )
    }
    
    private fun getRightButtonBounds(): FloatArray {
        return floatArrayOf(
            centerX + 10f,                     // left
            centerY - buttonHeight / 2,        // top
            centerX + buttonWidth + 10f,       // right
            centerY + buttonHeight / 2         // bottom
        )
    }
    
    fun onTouchDown(x: Float, y: Float, pId: Int) {
        val leftBounds = getLeftButtonBounds()
        val rightBounds = getRightButtonBounds()
        
        // Kiểm tra nút trái
        if (x >= leftBounds[0] && x <= leftBounds[2] && 
            y >= leftBounds[1] && y <= leftBounds[3]) {
            leftPressed = true
            leftPointerId = pId
        }
        
        // Kiểm tra nút phải
        if (x >= rightBounds[0] && x <= rightBounds[2] && 
            y >= rightBounds[1] && y <= rightBounds[3]) {
            rightPressed = true
            rightPointerId = pId
        }
    }
    
    fun onTouchUp(pId: Int) {
        if (pId == leftPointerId) {
            leftPressed = false
            leftPointerId = -1
        }
        if (pId == rightPointerId) {
            rightPressed = false
            rightPointerId = -1
        }
    }
    
    fun reset() {
        leftPressed = false
        rightPressed = false
        leftPointerId = -1
        rightPointerId = -1
    }
    
    fun getX(): Float {
        return when {
            leftPressed && rightPressed -> 0f // Cả 2 nút = không di chuyển
            leftPressed -> -1f
            rightPressed -> 1f
            else -> 0f
        }
    }
    
    fun getY(): Float {
        return 0f // Không có di chuyển lên xuống
    }
    
    fun draw(canvas: Canvas) {
        val leftBounds = getLeftButtonBounds()
        val rightBounds = getRightButtonBounds()
        
        // Vẽ nút trái
        canvas.drawRect(
            leftBounds[0], leftBounds[1], leftBounds[2], leftBounds[3],
            if (leftPressed) buttonPressedPaint else buttonPaint
        )
        canvas.drawText("◄", 
            (leftBounds[0] + leftBounds[2]) / 2, 
            centerY + 20f, 
            textPaint
        )
        
        // Vẽ nút phải
        canvas.drawRect(
            rightBounds[0], rightBounds[1], rightBounds[2], rightBounds[3],
            if (rightPressed) buttonPressedPaint else buttonPaint
        )
        canvas.drawText("►", 
            (rightBounds[0] + rightBounds[2]) / 2, 
            centerY + 20f, 
            textPaint
        )
    }
}
