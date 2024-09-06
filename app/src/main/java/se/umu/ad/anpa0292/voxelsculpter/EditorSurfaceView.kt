package se.umu.ad.anpa0292.voxelsculpter

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent

class EditorSurfaceView(context: Context, attributeSet: AttributeSet) : GLSurfaceView(context, attributeSet) {
    private var renderer: EditorRenderer

    private var prevPos = Vector3D(0f, 0f, 0f)
    private var prevDistance = 0f

    companion object {
        const val ROTATION_SENSITIVITY = 0.3f
        const val ZOOM_SENSITIVITY = 0.1f
        const val PAN_SENSITIVITY = 0.01f
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
            MotionEvent.ACTION_POINTER_UP -> {
                if (event.pointerCount == 2) {
                    // Transition smoothly when lifting one finger
                    val remainingPointerIndex = if (event.actionIndex == 0) 1 else 0
                    prevPos = Vector3D(event.getX(remainingPointerIndex), event.getY(remainingPointerIndex), 0f)
                } else if (event.pointerCount == 1) {
                    prevPos = Vector3D(event.x, event.y, 0f)
                }
            }
        }

        return true
    }

    private fun handleDown(event: MotionEvent) {
        when (event.pointerCount) {
            1 -> {
                prevPos = Vector3D(event.x, event.y, 0f)
            }
            2 -> {
                val pointer1 = Vector3D(event.getX(0), event.getY(0), 0f)
                val pointer2 = Vector3D(event.getX(1), event.getY(1), 0f)
                prevDistance = (pointer1 - pointer2).norm()
                prevPos = 0.5f * (pointer1 + pointer2)
            }
        }
    }

    private fun handleMove(event: MotionEvent) {
        when (event.pointerCount) {
            1 -> handleSingleDrag(event)
            2 -> handleTwoFingerTouch(event)
        }
    }

    private fun handleSingleDrag(event: MotionEvent) {
        val pointer = Vector3D(event.x, event.y, 0f)
        val dPos = pointer - prevPos

        renderer.camera.rotate(
            -dPos.x * ROTATION_SENSITIVITY,
            dPos.y * ROTATION_SENSITIVITY
        )
        prevPos = pointer
    }

    private fun handleTwoFingerTouch(event: MotionEvent) {
        val pointer1 = Vector3D(event.getX(0), event.getY(0), 0f)
        val pointer2 = Vector3D(event.getX(1), event.getY(1), 0f)

        // Handle zoom
        val distance = (pointer1 - pointer2).norm()
        val zoomDelta = prevDistance - distance

        renderer.camera.zoom(zoomDelta * ZOOM_SENSITIVITY)
        prevDistance = distance

        // Handle pan
        val middlePoint = 0.5f * (pointer1 + pointer2)
        val dPos = middlePoint - prevPos

        renderer.camera.pan(
            -dPos.x * PAN_SENSITIVITY,
            dPos.y * PAN_SENSITIVITY
        )

        prevPos = middlePoint
    }
}