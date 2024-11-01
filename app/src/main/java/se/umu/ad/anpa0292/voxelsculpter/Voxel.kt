package se.umu.ad.anpa0292.voxelsculpter

import android.opengl.Matrix
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Voxel(val pos: Vector3D): Parcelable {
    val transform = FloatArray(16);
    val color = floatArrayOf(1.0f, 0.5f, 0.0f, 1.0f)

    companion object {
        const val HALF_SIDE = 0.5f

        val vertices = floatArrayOf(
            // Front face
            -HALF_SIDE, -HALF_SIDE, HALF_SIDE,
            HALF_SIDE, -HALF_SIDE, HALF_SIDE,
            HALF_SIDE, HALF_SIDE, HALF_SIDE,
            -HALF_SIDE, HALF_SIDE, HALF_SIDE,

            // Back face
            -HALF_SIDE, -HALF_SIDE, -HALF_SIDE,
            HALF_SIDE, -HALF_SIDE, -HALF_SIDE,
            HALF_SIDE, HALF_SIDE, -HALF_SIDE,
            -HALF_SIDE, HALF_SIDE, -HALF_SIDE,

            // Left face
            -HALF_SIDE, HALF_SIDE, HALF_SIDE,
            -HALF_SIDE, HALF_SIDE, -HALF_SIDE,
            -HALF_SIDE, -HALF_SIDE, -HALF_SIDE,
            -HALF_SIDE, -HALF_SIDE, HALF_SIDE,

            // Right face
            HALF_SIDE, HALF_SIDE, HALF_SIDE,
            HALF_SIDE, HALF_SIDE, -HALF_SIDE,
            HALF_SIDE, -HALF_SIDE, -HALF_SIDE,
            HALF_SIDE, -HALF_SIDE, HALF_SIDE,

            // Top face
            -HALF_SIDE, HALF_SIDE, HALF_SIDE,
            HALF_SIDE, HALF_SIDE, HALF_SIDE,
            HALF_SIDE, HALF_SIDE, -HALF_SIDE,
            -HALF_SIDE, HALF_SIDE, -HALF_SIDE,

            // Bottom face
            -HALF_SIDE, -HALF_SIDE, HALF_SIDE,
            HALF_SIDE, -HALF_SIDE, HALF_SIDE,
            HALF_SIDE, -HALF_SIDE, -HALF_SIDE,
            -HALF_SIDE, -HALF_SIDE, -HALF_SIDE
        )

        val normals = floatArrayOf(
            // Front face
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,

            // Back face
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,

            // Left face
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,

            // Right face
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,

            // Top face
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,

            // Bottom face
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f
        )

        val solidIndices = shortArrayOf(
            // Front face
            0, 1, 2, 0, 2, 3,

            // Back face
            4, 5, 6, 4, 6, 7,

            // Left face
            8, 9, 10, 8, 10, 11,

            // Right face
            12, 13, 14, 12, 14, 15,

            // Top face
            16, 17, 18, 16, 18, 19,

            // Bottom face
            20, 21, 22, 20, 22, 23
        )

        val wireframeIndices = shortArrayOf(
            // Front face edges
            0, 1, 1, 2, 2, 3, 3, 0,

            // Back face edges
            4, 5, 5, 6, 6, 7, 7, 4,

            // Connecting edges between front and back faces
            0, 4, 1, 5, 2, 6, 3, 7
        )
    }

    init {
        Matrix.setIdentityM(transform, 0)
        Matrix.translateM(transform, 0, pos.x, pos.y, pos.z)
    }
}