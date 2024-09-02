package se.umu.ad.anpa0292.voxelsculpter

import kotlin.math.cos
import kotlin.math.sin

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
    fun additionVV(destVec: FloatArray, lhsVec: FloatArray, rhsVec: FloatArray) {
        require(destVec.size >= 3) {"destVec must be of at least size 3"}
        require(lhsVec.size >= 3) {"lhsVec must be of at least size 3"}
        require(rhsVec.size >= 3) {"rhsVec must be of at least size 3"}

        destVec[0] = lhsVec[0] + rhsVec[0]
        destVec[1] = lhsVec[1] + rhsVec[1]
        destVec[2] = lhsVec[2] + rhsVec[2]
    }

    private fun toRadian(degree: Float): Float {
        return (degree / 180 * Math.PI).toFloat()
    }
}