package com.funyoung.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.hypot

class BallsView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val balls = mutableListOf<Ball>()
    private var selectedBall: Ball? = null

    init {
        // 初始化一些小球
        balls.add(Ball(200f, 200f, 50f, Color.RED))
        balls.add(Ball(400f, 400f, 50f, Color.BLUE))
        balls.add(Ball(600f, 600f, 50f, Color.GREEN))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        balls.forEach { ball ->
            val paint = Paint().apply {
                color = ball.color
            }
            canvas.drawCircle(ball.x, ball.y, ball.radius, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                selectedBall = balls.find { ball ->
                    hypot(ball.x - event.x, ball.y - event.y) <= ball.radius
                }
            }
            MotionEvent.ACTION_MOVE -> {
                selectedBall?.let { ball ->
                    ball.x = event.x
                    ball.y = event.y
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                selectedBall = null
            }
        }
        return true
    }

    data class Ball(var x: Float, var y: Float, val radius: Float, val color: Int)
}
