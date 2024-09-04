package se.umu.ad.anpa0292.voxelsculpter

import android.util.Log
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * This utility object provides a set of functions for 3D vector math needed within the scoop of
 * this project.
 */
object VectorMath3D {
    /**
     * Converts the spherical coordinates ([r], [theta], [phi]) to the cartesian coordinates
     * (x, y, z). [theta] and [phi] are angles in degrees.
     *
     * Preconditions:
     * - [r] must be non-negative.
     * - [theta] must be in range [-90, 90] degrees.
     * - [destVec] must have at least 3 elements.
     *
     * Post conditions:
     * - The first three entries of [destVec] will be overwritten with the coordinates (x, y, z)
     *
     * @param destVec The destination vector (represented as an array), where the cartesian
     *     coordinates will be stored. Must be of at least size 3.
     * @param r The radial distance to the origin, must be non-negative.
     * @param theta The polar angle in degrees, must be in range [-90, 90] degrees.
     * @param phi The azimuthal angle in degrees.
     * @throws IllegalArgumentException if any precondition is not met.
     */
    fun sphericalToCartesianCoords(
        destVec: FloatArray,
        r: Float, theta: Float, phi: Float
    ) {
        require(destVec.size >= 3) {"destVec must be of at least size 3"}
        require(r >= 0) {"r must be non-negative."}
        require(theta in -90f..90f) {"theta must be in range [-90, 90]."}

        val thetaRad = toRadian(theta)
        val phiRad = toRadian(phi)

        destVec[0] = r * cos(thetaRad) * sin(phiRad)
        destVec[1] = r * sin(thetaRad)
        destVec[2] = r * cos(thetaRad) * cos(phiRad)
    }

    /**
     * Adds the two vectors [lhsVec] and [rhsVec] together and stores the result in [destVec].
     *
     * Preconditions:
     * - [destVec] must have at least 3 elements.
     * - [lhsVec] must have at least 3 elements.
     * - [rhsVec] must have at least 3 elements.
     *
     * Post conditions:
     * - The first three entries of [destVec] will be overwritten with the result of the vector
     *   addition.
     *
     * @param destVec The destination vector (represented as an array), where the result of the
     *     vector addition will be stored. Must be of at least size 3.
     * @param lhsVec The left-hand side vector in the vector addition. Must be of at least size 3.
     * @param rhsVec The right-hand side vector in the vector addition. Must be of at least size 3.
     * @throws IllegalArgumentException if any precondition is not met.
     */
    fun add(destVec: FloatArray, lhsVec: FloatArray, rhsVec: FloatArray) {
        require(destVec.size >= 3) {"destVec must be of at least size 3"}
        require(lhsVec.size >= 3) {"lhsVec must be of at least size 3"}
        require(rhsVec.size >= 3) {"rhsVec must be of at least size 3"}

        destVec[0] = lhsVec[0] + rhsVec[0]
        destVec[1] = lhsVec[1] + rhsVec[1]
        destVec[2] = lhsVec[2] + rhsVec[2]
    }

    fun sub(destVec: FloatArray, lhsVec: FloatArray, rhsVec: FloatArray) {
        require(destVec.size >= 3) {"destVec must be of at least size 3"}
        require(lhsVec.size >= 3) {"lhsVec must be of at least size 3"}
        require(rhsVec.size >= 3) {"rhsVec must be of at least size 3"}

        destVec[0] = lhsVec[0] - rhsVec[0]
        destVec[1] = lhsVec[1] - rhsVec[1]
        destVec[2] = lhsVec[2] - rhsVec[2]
    }

    fun cross(destVec: FloatArray, lhsVec: FloatArray, rhsVec: FloatArray) {
        require(destVec.size >= 3) {"destVec must be of at least size 3"}
        require(lhsVec.size >= 3) {"lhsVec must be of at least size 3"}
        require(rhsVec.size >= 3) {"rhsVec must be of at least size 3"}

        destVec[0] = lhsVec[1] * rhsVec[2] - lhsVec[2] * rhsVec[1]
        destVec[1] = lhsVec[2] * rhsVec[0] - lhsVec[0] * rhsVec[2]
        destVec[2] = lhsVec[0] * rhsVec[1] - lhsVec[1] * rhsVec[0]
    }

    /**
     * Scale vector [vec] with scalar [scalar] and stores the result in [destVec].
     *
     * Preconditions:
     * - [destVec] must have at least 3 elements.
     * - [vec] must have at least 3 elements.
     *
     * Post conditions:
     * - The first three entries of [destVec] will be overwritten with the result of the vector
     *   addition.
     *
     * @param destVec The destination vector (represented as an array), where the result of the
     *     vector addition will be stored. Must be of at least size 3.
     * @param scalar The value to scale [vec] with.
     * @param vec The vector to scale. Must be of at least size 3.
     * @throws IllegalArgumentException if any precondition is not met.
     */
    fun scale(destVec: FloatArray, scalar: Float, vec: FloatArray) {
        require(destVec.size >= 3) {"destVec must be of at least size 3"}
        require(vec.size >= 3) {"vec must be of at least size 3"}

        destVec[0] = scalar * vec[0]
        destVec[1] = scalar * vec[1]
        destVec[2] = scalar * vec[2]
    }

    fun normalize(destVec: FloatArray, vec: FloatArray) {
        require(destVec.size >= 3) {"destVec must be of at least size 3"}
        require(vec.size >= 3) {"vec must be of at least size 3"}

        val norm = norm(vec)

        Log.d("kuk", vec.toString())
        Log.d("kuk", norm.toString())

        require(norm != 0f)

        vec.forEachIndexed { i, value ->
            destVec[i] = value / norm
        }
    }

    fun norm(vec: FloatArray): Float {
        require(vec.size >= 3) {"vec must be of at least size 3"}

        return sqrt(vec[0] * vec[0] + vec[1] * vec[1] + vec[2] * vec[2])
    }

    private fun toRadian(degree: Float): Float {
        return (degree / 180 * Math.PI).toFloat()
    }
}