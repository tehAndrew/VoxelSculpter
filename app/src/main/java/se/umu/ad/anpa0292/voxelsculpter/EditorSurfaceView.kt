package se.umu.ad.anpa0292.voxelsculpter

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent

class EditorSurfaceView(context: Context, attributeSet: AttributeSet) : GLSurfaceView(context, attributeSet) {
    private var renderer: EditorRenderer

    private var prevX = 0f
    private var prevY = 0f

    companion object {
        const val ROTATION_SENSITIVITY = 0.3f
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
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - prevX
                val dy = event.y - prevY
                renderer.camera.rotate(
                    -dx * ROTATION_SENSITIVITY,
                    dy * ROTATION_SENSITIVITY
                )

                prevX = event.x
                prevY = event.y
            }
        }

        return true
    }
}