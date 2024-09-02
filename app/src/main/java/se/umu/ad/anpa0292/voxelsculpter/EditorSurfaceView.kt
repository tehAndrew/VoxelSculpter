package se.umu.ad.anpa0292.voxelsculpter

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent
import kotlin.math.abs
import kotlin.math.sqrt

class EditorSurfaceView(context: Context, attributeSet: AttributeSet) : GLSurfaceView(context, attributeSet) {
    private var renderer: EditorRenderer

    private var prevX = 0f
    private var prevY = 0f

    private var prevDistance = 0f

    private enum class TouchMode {
        IDLE,
        SINGLE_TAP,
        SINGLE_DRAG,
        TWO_POINTER_TOUCH
    }

    private var touchMode = TouchMode.IDLE

    companion object {
        const val ROTATION_SENSITIVITY = 0.3f
        const val ZOOM_SENSITIVITY = 0.1f
    }

    init {
        // Set openGL ES 2.0
        setEGLContextClientVersion(2)

        // Set renderer
        renderer = EditorRenderer(context)
        setRenderer(renderer)

        // Render continuously
        renderMode = RENDERMODE_CONTINUOUSLY

        // Keep context on pause
        preserveEGLContextOnPause = true
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                prevX = event.x
                prevY = event.y

                touchMode = if (event.pointerCount == 1) TouchMode.SINGLE_TAP else TouchMode.IDLE
            }

            MotionEvent.ACTION_POINTER_DOWN ->
                touchMode =
                    if (event.pointerCount == 2) {
                        val x1 = event.getX(0)
                        val y1 = event.getY(0)
                        val x2 = event.getX(1)
                        val y2 = event.getY(1)
                        prevDistance = sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1))

                        TouchMode.TWO_POINTER_TOUCH
                    }
                    else
                        TouchMode.IDLE

            MotionEvent.ACTION_MOVE -> {
                when (touchMode) {
                    TouchMode.IDLE -> {} // Do nothing when more than 2 pointers are used

                    TouchMode.SINGLE_TAP -> touchMode = TouchMode.SINGLE_DRAG

                    TouchMode.SINGLE_DRAG -> {
                        val dx = event.x - prevX
                        val dy = event.y - prevY
                        renderer.camera.rotate(
                            -dx * ROTATION_SENSITIVITY,
                            dy * ROTATION_SENSITIVITY
                        )

                        prevX = event.x
                        prevY = event.y
                    }

                    TouchMode.TWO_POINTER_TOUCH -> {
                        if (event.pointerCount == 1) {
                            prevX = event.x
                            prevY = event.y
                            touchMode = TouchMode.SINGLE_DRAG
                        }
                        else if (event.pointerCount == 2) {
                            val x1 = event.getX(0)
                            val y1 = event.getY(0)
                            val x2 = event.getX(1)
                            val y2 = event.getY(1)
                            val distance = sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1))

                            val zoomDelta = prevDistance - distance
                            renderer.camera.zoom(zoomDelta * ZOOM_SENSITIVITY)
                            prevDistance = distance
                        }
                    }
                }
            }
        }

        return true
    }
}