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
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> handleDown(event)
            MotionEvent.ACTION_MOVE -> handleMove(event)
        }

        return true
    }

    private fun handleDown(event: MotionEvent) {
        when (event.pointerCount) {
            1 -> {
                prevX = event.x
                prevY = event.y
            }
            2 -> {
                prevDistance = calculateDistance(event)
            }
        }
    }

    private fun handleMove(event: MotionEvent) {
        when (event.pointerCount) {
            1 -> handleSingleDrag(event)
            2 -> handlePinchZoom(event)
        }
    }

    private fun handleSingleDrag(event: MotionEvent) {
        val dx = event.x - prevX
        val dy = event.y - prevY
        renderer.camera.rotate(
            -dx * ROTATION_SENSITIVITY,
            dy * ROTATION_SENSITIVITY
        )
        prevX = event.x
        prevY = event.y
    }

    private fun handlePinchZoom(event: MotionEvent) {
        val distance = calculateDistance(event)
        val zoomDelta = prevDistance - distance
        renderer.camera.zoom(zoomDelta * ZOOM_SENSITIVITY)
        prevDistance = distance
    }

    private fun calculateDistance(event: MotionEvent): Float {
        val x1 = event.getX(0)
        val y1 = event.getY(0)
        val x2 = event.getX(1)
        val y2 = event.getY(1)
        return sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1))
    }
}