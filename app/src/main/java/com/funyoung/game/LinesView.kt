package com.funyoung.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.hypot
import kotlin.random.Random

class LinesView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val lines = mutableListOf<MutableList<Ball>>()
    private val balls = mutableListOf<Ball>()
    private var selectedBalls = mutableListOf<Ball>()
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
        val random = Random(System.currentTimeMillis())
        val numLines = random.nextInt(4, 7)
        val numBallsPerLine = random.nextInt(5, 8)
        val spacingX = 300f // width.toFloat() / (numLines + 1)

        for (i in 1..numLines) {
            val lineBalls = mutableListOf<Ball>()
            val startX = i * spacingX
            val spacingY = 100f  //height.toFloat() / (numBallsPerLine + 1)
            for (j in 1..numBallsPerLine) {
                val x = startX //+ random.nextInt(-50, 50)
                val y = j * spacingY
                val color = colors.random(random)
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

                // 检查是否连接到其他直线底部的球上
                lines.forEach { line ->
                    if (line !== selectedBalls && line.isNotEmpty()) {
                        val bottomBall = line.last()
                        if (hypot(bottomBall.x - x, bottomBall.y - y) <= bottomBall.radius) {
                            // 断开原来的直线
                            selectedBalls.forEach { ball ->
                                lines.forEach { oldLine ->
                                    oldLine.remove(ball)
                                }
                            }
                            // 连接到新的直线
                            line.addAll(selectedBalls)
                            selectedBalls.clear()
                            generateBallsAndLines()
                            return true
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
