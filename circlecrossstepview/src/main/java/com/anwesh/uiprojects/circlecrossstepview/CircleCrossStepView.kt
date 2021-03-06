package com.anwesh.uiprojects.circlecrossstepview

/**
 * Created by anweshmishra on 03/11/18.
 */

import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.app.Activity
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.graphics.RectF
import android.util.Log

val nodes : Int = 5

fun Float.divideScale(i : Int, n : Int) : Float = Math.min((1f/n), Math.max(this - i * (1f/n), 0f)) * n

fun Canvas.drawCCSNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = w / (nodes + 1)
    val r : Float = gap / 3
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = Math.min(w, h) / 60
    paint.strokeCap = Paint.Cap.ROUND
    paint.color = Color.parseColor("#0D47A1")
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    save()
    translate(gap * i + gap, h/2)
    drawArc(RectF(-r, -r, r, r), -90f, 360f * sc1, false, paint)
    for (j in 0..1) {
        val sc : Float = sc2.divideScale(j, 2)
        save()
        rotate(90f * j + 45f)
        drawLine(0f, -r/2, 0f, -r/2 + r * sc, paint)
        restore()
    }
    restore()
}

class CircleCrossStepView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    var onAnimationListener : OnAnimationListener? = null

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    fun addOnAnimationListener(onComplete : (Int) -> Unit, onReset : (Int) -> Unit) {
        onAnimationListener = OnAnimationListener(onComplete, onReset)
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            val k : Float = (0.05f/ (1 + Math.floor(scale / 0.5))).toFloat()
            Log.d("value of k", "$k")
            scale += k * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }

            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class CCSNode(var i : Int, val state : State = State()) {

        var prev : CCSNode? = null

        var next : CCSNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = CCSNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawCCSNode(i, state.scale, paint)
            prev?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : CCSNode {
            var curr : CCSNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class CircleCrossStep(var i : Int) {

        private var curr : CCSNode = CCSNode(0)

        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : CircleCrossStepView) {

        private val animator : Animator = Animator(view)

        private val ccs : CircleCrossStep = CircleCrossStep(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#BDBDBD"))
            ccs.draw(canvas, paint)
            animator.animate {
                ccs.update {i, scl ->
                    animator.stop()
                    when (scl) {
                        1f -> view.onAnimationListener?.onComplete?.invoke(i)
                        0f -> view.onAnimationListener?.onReset?.invoke(i)
                    }
                }
            }
        }

        fun handleTap() {
            ccs.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity : Activity) : CircleCrossStepView {
            val view : CircleCrossStepView = CircleCrossStepView(activity)
            activity.setContentView(view)
            return view
        }
    }

    data class OnAnimationListener(var onComplete : (Int) -> Unit, var onReset : (Int) -> Unit)
}