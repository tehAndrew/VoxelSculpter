package se.umu.ad.anpa0292.voxelsculpter

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent

class EditorSurfaceView(context: Context, attributeSet: AttributeSet) : GLSurfaceView(context, attributeSet) {
    private var renderer: EditorRenderer

    private val world = World()

    private var prevPos = Vector3D(0f, 0f, 0f)
    private var prevDistance = 0f

    var currentTool = Tool.ADD

    enum class GestureType {
        NONE,
        SINGLE_POINTER_CLICK,
        SINGLE_POINTER_MOVE,
        TWO_POINTER_GESTURE,
        MULTI_POINTER_GESTURE
    }

    private var currentGesture = GestureType.NONE

    companion object {
        const val ROTATION_SENSITIVITY = 0.3f
        const val ZOOM_SENSITIVITY = 0.1f
        const val PAN_SENSITIVITY = 0.01f
    }

    init {
        // Set openGL ES 2.0
        setEGLContextClientVersion(2)

        // Set renderer
        renderer = EditorRenderer(context, world)
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
                // Single pointer down
                currentGesture = GestureType.SINGLE_POINTER_CLICK
                prevPos = Vector3D(event.x, event.y, 0f)
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                // Multi-touch begins
                val currentPos = if (event.pointerCount > 1) {
                    val pointer1 = Vector3D(event.getX(0), event.getY(0), 0f)
                    val pointer2 = Vector3D(event.getX(1), event.getY(1), 0f)
                    (pointer1 + pointer2) * 0.5f
                } else Vector3D(event.x, event.y, 0f)

                val currentDistance = if (event.pointerCount > 1) {
                    val pointer1 = Vector3D(event.getX(0), event.getY(0), 0f)
                    val pointer2 = Vector3D(event.getX(1), event.getY(1), 0f)
                    (pointer1 - pointer2).norm()
                } else 0f

                currentGesture = if (event.pointerCount > 2) {
                    GestureType.MULTI_POINTER_GESTURE
                } else {
                    GestureType.TWO_POINTER_GESTURE
                }

                prevPos = currentPos
                prevDistance = currentDistance
            }

            MotionEvent.ACTION_MOVE -> {
                val currentPos = if (event.pointerCount > 1) {
                    val pointer1 = Vector3D(event.getX(0), event.getY(0), 0f)
                    val pointer2 = Vector3D(event.getX(1), event.getY(1), 0f)
                    (pointer1 + pointer2) * 0.5f
                } else Vector3D(event.x, event.y, 0f)

                val currentDistance = if (event.pointerCount > 1) {
                    val pointer1 = Vector3D(event.getX(0), event.getY(0), 0f)
                    val pointer2 = Vector3D(event.getX(1), event.getY(1), 0f)
                    (pointer1 - pointer2).norm()
                } else 0f

                val delta = currentPos - prevPos

                if (currentGesture == GestureType.SINGLE_POINTER_CLICK && delta.norm() > 1f) {
                    currentGesture = GestureType.SINGLE_POINTER_MOVE
                }

                if (currentGesture == GestureType.SINGLE_POINTER_MOVE) {
                    // Handle rotation
                    world.camera.rotate(
                        -delta.x * ROTATION_SENSITIVITY,
                        delta.y * ROTATION_SENSITIVITY
                    )
                } else if (currentGesture == GestureType.TWO_POINTER_GESTURE) {
                    // Handle panning and zooming
                    world.camera.pan(
                        -delta.x * PAN_SENSITIVITY,
                        delta.y * PAN_SENSITIVITY
                    )
                    world.selectVoxelAtCenter()
                    world.camera.zoom((prevDistance - currentDistance) * ZOOM_SENSITIVITY)
                }

                prevPos = currentPos
                prevDistance = currentDistance
            }

            MotionEvent.ACTION_POINTER_UP -> {
                if (event.pointerCount == 3) {
                    currentGesture = GestureType.TWO_POINTER_GESTURE

                    val pointer1 = Vector3D(event.getX(0), event.getY(0), 0f)
                    val pointer2 = Vector3D(event.getX(1), event.getY(1), 0f)
                    prevPos = (pointer1 + pointer2) * 0.5f
                    prevDistance = (pointer1 - pointer2).norm()

                } else if (event.pointerCount == 2) {
                    currentGesture = GestureType.SINGLE_POINTER_MOVE

                    world.centerCamera() // Move from pan to single touch will center camera
                                         // around selected voxel

                    val remainingPointerIndex = if (event.actionIndex == 0) 1 else 0
                    prevPos = Vector3D(event.getX(remainingPointerIndex), event.getY(remainingPointerIndex), 0f)
                    prevDistance = 0f
                }
            }

            MotionEvent.ACTION_UP -> {
                // End of gesture
                if (currentGesture == GestureType.SINGLE_POINTER_CLICK) {
                    currentTool.use(world, Vector3D(event.x, event.y, 0f))
                }
                currentGesture = GestureType.NONE
            }
        }

        return true
    }
}