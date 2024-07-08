package com.funyoung.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.hypot

class LinesView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val lines = mutableListOf<List<Ball>>()
    private val balls = mutableListOf<Ball>()
    private val selectedBalls = mutableListOf<Ball>()
    private var offsetX = 0f
    private var offsetY = 0f

    init {
        // 初始化小球和连线
        generateBallsAndLines()
    }

    private fun generateBallsAndLines() {
        balls.clear()
        lines.clear()

        // 生成随机小球
        val colors = listOf(Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.CYAN)
        val random = ThreadLocalRandom.current()
        val numLines = random.nextInt(2, 4)
        val numBallsPerLine = random.nextInt(3, 6)
        val spacingX = 300f // width.toFloat() / (numLines + 1)

        for (i in 1..numLines) {
            val lineBalls = mutableListOf<Ball>()
            val startX = i * spacingX
            val spacingY = 50f  //height.toFloat() / (numBallsPerLine + 1)
            for (j in 1..numBallsPerLine) {
                val x = startX + random.nextInt(-50, 50)
                val y = j * spacingY
                val color = colors.random()
                val ball = Ball(x, y, 50f, color)
                balls.add(ball)
                lineBalls.add(ball)
            }
            lines.add(lineBalls)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        lines.forEach { line ->
            val path = Path()
            line.forEachIndexed { index, ball ->
                val paint = Paint().apply {
                    color = ball.color
                    style = Paint.Style.FILL
                }
                if (index == 0) {
                    path.moveTo(ball.x, ball.y)
                } else {
                    path.lineTo(ball.x, ball.y)
                }
                canvas.drawCircle(ball.x, ball.y, ball.radius, paint)
            }
            canvas.drawPath(path, Paint().apply {
                color = Color.BLACK
                style = Paint.Style.STROKE
                strokeWidth = 5f
            })
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                selectedBalls.clear()
                lines.forEach { line ->
                    line.forEach { ball ->
                        if (hypot(ball.x - x, ball.y - y) <= ball.radius) {
                            selectedBalls.add(ball)
                        }
                    }
                }
                offsetX = x - (selectedBalls.firstOrNull()?.x ?: 0f)
                offsetY = y - (selectedBalls.firstOrNull()?.y ?: 0f)
            }
            MotionEvent.ACTION_MOVE -> {
                selectedBalls.forEach { ball ->
                    ball.x = x - offsetX
                    ball.y = y - offsetY
                }

                // 检查是否吸附
                balls.forEach { ball ->
                    if (ball !in selectedBalls) {
                        selectedBalls.forEach { selectedBall ->
                            val distance = hypot(ball.x - selectedBall.x, ball.y - selectedBall.y)
                            val threshold = ball.radius + selectedBall.radius + 20
                            if (distance < threshold) {
                                ball.x = selectedBall.x
                                ball.y = selectedBall.y
                            }
                        }
                    }
                }

                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                selectedBalls.clear()
            }
        }
        return true
    }

    data class Ball(var x: Float, var y: Float, val radius: Float, val color: Int)
}
