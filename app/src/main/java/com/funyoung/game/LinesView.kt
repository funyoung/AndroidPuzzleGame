package com.funyoung.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.hypot
import kotlin.random.Random

class LinesView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val lines = mutableListOf<MutableList<Ball>>()
    private val balls = mutableListOf<Ball>()

    private var selectedBalls = mutableListOf<Ball>()
    private var selectedLine = -1

    private var startX = 0f
    private var startY = 0f
    private var currentX = 0f
    private var currentY = 0f

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
            val x = i * spacingX
            val spacingY = 100f
            for (j in 1..numBallsPerLine) {
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

        lines.forEach { line -> drawLine(canvas, line)}
        drawLine(canvas, selectedBalls, currentX - startX, currentY - startY)
    }

    private fun drawLine(canvas: Canvas, line: List<Ball>, dx: Float = 0f, dy: Float = 0f) {
        line.forEach {ball ->
            val paint = Paint().apply {
                color = ball.color
                style = Paint.Style.FILL
            }

            canvas.drawCircle(ball.x + dx, ball.y + dy, ball.radius, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        currentX = event.x
        currentY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> cutLine()
            MotionEvent.ACTION_MOVE -> dragLine()
            MotionEvent.ACTION_UP -> dropLine()
        }
        return true
    }

    private fun dropLine() {
        if (selectedBalls.isNotEmpty()) {
            // 检查是否连接到其他直线底部的球上
            lines.forEach { line ->
                if (line !== selectedBalls && line.isNotEmpty()) {
                    val bottomBall = line.last()
                    if (hypot(bottomBall.x - currentX, bottomBall.radius + bottomBall.y - currentY) <= bottomBall.radius) {
                        val deltaX = bottomBall.x - selectedBalls.first().x
                        val deltaY = bottomBall.y - selectedBalls.first().y + bottomBall.radius + selectedBalls.first().radius
                        selectedBalls.forEach {
                            it.x += deltaX
                            it.y += deltaY
                        }
                        line.addAll(selectedBalls)
                        selectedBalls.clear()
                        return@forEach
                    }
                }
            }

            if (selectedBalls.isNotEmpty()) {
                lines[selectedLine].addAll(selectedBalls)
                selectedBalls.clear()
            }

            invalidate()
        }
    }

    private fun dragLine() {
        invalidate()
    }

    // 遍历检查按下出，点中小球的所在栏(停止拖拽时可能回弹回来)
    // 该小球和该栏它后面的小球都移进选择球的列表中
    private fun cutLine() {
        startX = currentX
        startY = currentY

        var pos = -1
        selectedLine = -1

        selectedBalls.clear()
        lines.forEachIndexed { index, line ->
            line.forEachIndexed { current, ball ->
                if (hypot(ball.x - startX, ball.y - startY) <= ball.radius) {
                    selectedLine = index
                    pos = current
                    return@forEachIndexed // 退出当前的 forEachIndexed 循环
                }
            }
            if (pos >= 0 && selectedLine >= 0) {
                return@forEachIndexed // 退出当前的 forEachIndexed 循环
            }
        }

        if (pos >= 0 && selectedLine >= 0) {
            selectedBalls.addAll(lines[selectedLine].removeFrom(pos))
        }
    }

    private fun <T> MutableList<T>.removeFrom(index: Int): MutableList<T> {
        if (index < 0 || index >= size) {
            throw IndexOutOfBoundsException("Index: $index, Size: $size")
        }
        val removedElements = this.subList(index, this.size).toMutableList()
        this.subList(index, this.size).clear()
        return removedElements
    }


    data class Ball(var x: Float, var y: Float, val radius: Float, val color: Int)
}
