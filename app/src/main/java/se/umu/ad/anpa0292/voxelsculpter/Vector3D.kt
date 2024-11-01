package se.umu.ad.anpa0292.voxelsculpter

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

operator fun Float.times(other: Vector3D): Vector3D {
    return Vector3D(this * other.x, this * other.y, this * other.z)
}

@Parcelize
data class Vector3D(val x: Float, val y: Float, val z: Float): Parcelable {
    companion object {
        fun fromSphericalCoords(r : Float, theta: Float, phi: Float): Vector3D {
            require(r >= 0) {"r must be non-negative."}
            require(theta in -90f..90f) {"theta must be in range [-90, 90]."}

            val thetaRad = (theta / 180f * PI.toFloat())
            val phiRad = (phi / 180f * PI.toFloat())

            return Vector3D(
                r * cos(thetaRad) * sin(phiRad),
                r * sin(thetaRad),
                r * cos(thetaRad) * cos(phiRad)
            )
        }

        fun fromGLVector(vec: FloatArray): Vector3D {
            return Vector3D(vec[0], vec[1], vec[2])
        }
    }

    operator fun get(index: Int): Float {
        return when (index) {
            0 -> x
            1 -> y
            2 -> z
            else -> throw IndexOutOfBoundsException("Index out of range: $index")
        }
    }

    operator fun plus(other: Vector3D): Vector3D {
        return Vector3D(x + other.x, y + other.y, z + other.z)
    }

    operator fun minus(other: Vector3D): Vector3D {
        return Vector3D(x - other.x, y - other.y, z - other.z)
    }

    operator fun unaryMinus(): Vector3D {
        return Vector3D(-x, -y, -z)
    }

    operator fun times(scalar: Float): Vector3D {
        return Vector3D(x * scalar, y * scalar, z * scalar)
    }

    infix fun cross(other: Vector3D): Vector3D {
        return Vector3D(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x
        )
    }

    fun norm(): Float {
        return sqrt(x * x + y * y + z * z)
    }

    fun normalize(): Vector3D {
        val norm = norm()

        require(norm > 0)

        return Vector3D(x / norm, y / norm, z / norm)
    }

    fun toGLVector(): FloatArray {
        return floatArrayOf(x, y, z, 1f)
    }
}