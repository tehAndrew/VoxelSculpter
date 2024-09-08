package se.umu.ad.anpa0292.voxelsculpter

import android.opengl.Matrix
import android.util.Log

class PerspectiveCamera(
    private var target: Vector3D,
    private var distance: Float,
    var viewportWidth: Int,
    var viewportHeight: Int
) {
    private var verticalAngle = 0f
    private var horizontalAngle = 0f

    // Perspective projection parameters
    private val fovY = 45f
    private val near = 1f
    private val far = 200f
    private var aspectRatio = viewportWidth.toFloat() / viewportHeight.toFloat()

    // Transforms
    private val viewMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewProjectionMatrix = FloatArray(16)
    private val inverseViewProjectionMatrix = FloatArray(16)

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
            position.x, position.y, position.z,
            target.x, target.y, target.z,
            0f, 1f, 0f
        )
    }

    private fun updateProjectionMatrix() {
        Matrix.perspectiveM(
            projectionMatrix, 0,
            fovY,
            aspectRatio,
            near,
            far
        )
    }

    private fun updateViewProjectionMatrix() {
        Matrix.multiplyMM(
            viewProjectionMatrix, 0,
            projectionMatrix, 0,
            viewMatrix, 0
        )
        Matrix.invertM(inverseViewProjectionMatrix, 0, viewProjectionMatrix, 0)
    }

    fun rotate(horDelta: Float, verDelta: Float) {
        Matrix.setIdentityM(viewMatrix, 0)

        horizontalAngle += horDelta
        if (horizontalAngle >= 360f) horizontalAngle -= 360f
        if (horizontalAngle < 0f) horizontalAngle += 360f

        verticalAngle = (verticalAngle + verDelta).coerceIn(-89f, 89f)

        updateViewMatrix()
        updateViewProjectionMatrix()
    }

    fun zoom(zoomFactor: Float) {
        distance = (distance + zoomFactor).coerceIn(5f, 80f)

        updateViewMatrix()
        updateViewProjectionMatrix()
    }

    fun pan(horDelta: Float, verDelta: Float) {
        val forward = (target - getPosition()).normalize()
        val right = (forward cross Vector3D(0f, 1f, 0f)).normalize()
        val up = (right cross forward).normalize()

        target += right * horDelta + up * verDelta

        updateViewMatrix()
        updateViewProjectionMatrix()
    }

    fun screenPosToWorldRay(screenPos: Vector3D): Ray {
        val ndcX = (2f * screenPos.x / viewportWidth) - 1f
        val ndcY = 1f - (2f * screenPos.y / viewportWidth)

        val nearPoint = floatArrayOf(ndcX, ndcY, -1.0f, 1.0f) // Near plane in NDC
        val farPoint = floatArrayOf(ndcX, ndcY, 1.0f, 1.0f) // Far plane in NDC

        val worldNear = FloatArray(4)
        val worldFar = FloatArray(4)

        // Unproject near point
        Matrix.multiplyMV(
            worldNear, 0,
            inverseViewProjectionMatrix, 0,
            nearPoint, 0
        )

        // Unproject far point
        Matrix.multiplyMV(
            worldFar, 0,
            inverseViewProjectionMatrix, 0,
            farPoint, 0
        )

        // Normalize world points
        for (i in 0..2) {
            worldNear[i] /= worldNear[3]
            worldFar[i] /= worldFar[3]
        }

        val rayOrigin = Vector3D.fromGLVector(worldNear)
        return Ray(
            rayOrigin,
            (Vector3D.fromGLVector(worldFar) - rayOrigin).normalize()
        )
    }

    fun setViewport(width: Int, height: Int) {
        viewportWidth = width
        viewportHeight = height
        aspectRatio = width.toFloat() / height.toFloat()
        updateProjectionMatrix()
        updateViewProjectionMatrix()
    }

    fun getPosition(): Vector3D {
        val position =
            Vector3D.fromSphericalCoords(distance, verticalAngle, horizontalAngle) + target
        return position
    }

    fun getTransform(): FloatArray {
        return viewProjectionMatrix
    }

    fun getInverseTransform(): FloatArray {
        return inverseViewProjectionMatrix
    }
}