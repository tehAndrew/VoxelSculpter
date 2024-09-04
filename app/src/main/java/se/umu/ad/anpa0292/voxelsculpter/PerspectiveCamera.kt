package se.umu.ad.anpa0292.voxelsculpter

import android.opengl.Matrix
import android.util.Log

class PerspectiveCamera(
    private val target: FloatArray,
    private var distance: Float
) {
    private var verticalAngle = 0f
    private var horizontalAngle = 0f

    // Perspective projection parameters
    private val fovY = 45f // Field of view in the Y direction
    private var aspectRatio = 16f / 9f // Aspect ratio (width / height)
    private val near = 1f
    private val far = 200f

    // Transforms
    private val viewMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewProjectionMatrix = FloatArray(16)

    init {
        updateViewMatrix()
        updateProjectionMatrix()
        updateViewProjectionMatrix()
    }

    private fun updateViewMatrix() {
        Matrix.setIdentityM(viewMatrix, 0)

        val position = getPosition()

        Matrix.setLookAtM(
            viewMatrix, 0,
            position[0], position[1], position[2],
            target[0], target[1], target[2],
            0f, 1f, 0f
        )
    }

    private fun updateProjectionMatrix() {
        Matrix.perspectiveM(
            projectionMatrix, 0,
            fovY,             // Field of view in Y direction
            aspectRatio,      // Aspect ratio
            near,             // Near clipping plane
            far               // Far clipping plane
        )
    }

    private fun updateViewProjectionMatrix() {
        Matrix.multiplyMM(
            viewProjectionMatrix, 0,
            projectionMatrix, 0,
            viewMatrix, 0
        )
    }

    fun rotate(horDelta: Float, verDelta: Float) {
        Matrix.setIdentityM(viewMatrix, 0)

        horizontalAngle += horDelta
        verticalAngle += verDelta

        if (verticalAngle < -89f) verticalAngle = -89f
        if (verticalAngle > 89f) verticalAngle = 89f

        updateViewMatrix()
        updateViewProjectionMatrix()
    }

    fun zoom(zoomFactor: Float) {
        distance = (distance + zoomFactor).coerceIn(5f, 80f)

        updateViewMatrix()
        updateViewProjectionMatrix()
    }

    fun pan(horDelta: Float, verDelta: Float) {
        // Calculate forward vector
        val forward = FloatArray(3)
        VectorMath3D.sub(forward, target, getPosition())
        VectorMath3D.normalize(forward, forward)

        // Calculate right vector
        val right = FloatArray(3)
        VectorMath3D.cross(right, forward, floatArrayOf(0f, 1f, 0f))
        VectorMath3D.normalize(right, right)

        val up = FloatArray(3)
        VectorMath3D.cross(up, right, forward)
        VectorMath3D.normalize(up, up)

        VectorMath3D.scale(right, horDelta, right)
        VectorMath3D.add(target, target, right)

        VectorMath3D.scale(up, verDelta, up)
        VectorMath3D.add(target, target, up)

        updateViewMatrix()
        updateViewProjectionMatrix()
    }

    fun setAspectRatio(width: Int, height: Int) {
        // Calculate the aspect ratio based on screen dimensions
        aspectRatio = width.toFloat() / height.toFloat()
        updateProjectionMatrix()
        updateViewProjectionMatrix()
    }

    fun getPosition(): FloatArray {
        val position = FloatArray(3)
        VectorMath3D.sphericalToCartesianCoords(position, distance, verticalAngle, horizontalAngle)
        VectorMath3D.add(position, target, position)
        return position
    }

    fun getTransform(): FloatArray {
        return viewProjectionMatrix
    }
}