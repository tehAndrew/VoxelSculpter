package se.umu.ad.anpa0292.voxelsculpter

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet

class EditorSurfaceView(context: Context, attributeSet: AttributeSet)
    : GLSurfaceView(context, attributeSet) {
        private var renderer: EditorRenderer

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
}