package se.umu.ad.anpa0292.voxelsculpter

import android.util.Log
import kotlin.math.max
import kotlin.math.min

data class Ray(val origin: Vector3D, val direction: Vector3D) {
    val invDirection = Vector3D(1 / direction.x, 1 / direction.y, 1 / direction.z)
}
data class AABB(val min: Vector3D, val max: Vector3D)
class World(viewportWidth: Int, viewportHeight: Int) {
    val camera: PerspectiveCamera = PerspectiveCamera(
        Vector3D(0f, 0f, 0f),
        60f,
        viewportWidth, viewportHeight
    )
    val voxels = arrayOf(
        Voxel(Vector3D(0f, 0f, 0f)),
        Voxel(Vector3D(1f, 0f, 0f)),
        Voxel(Vector3D(-1f, 0f, 0f)),
        Voxel(Vector3D(0f, -1f, 0f)),
        Voxel(Vector3D(0f, 1f, 0f)),
    )
    var selectedVoxel = voxels[1]

    fun selectVoxelAtScreenPos(screenPos: Vector3D) {
        val ray = camera.screenPosToWorldRay(screenPos)
        val tVals = mutableListOf<Pair<Float, Voxel>>()
        for (voxel in voxels) {
            val aabb = calculateVoxelAABB(voxel)
            Log.d("selectVoxelAtScreenPos", ray.origin.toString());
            intersectRayAABB(ray, aabb)?.let {
                tVals.add(it to voxel)
            }
        }

        if (tVals.isNotEmpty()) {
            val (value, voxel) = tVals.minBy { it.first }
            selectedVoxel = voxel
            camera.setTarget(voxel.pos)
        }
    }

    // Uses the fast branchless check here: https://tavianator.com/2015/ray_box_nan.html
    private fun intersectRayAABB(ray: Ray, aabb: AABB): Float? {
        var tmin = Float.NEGATIVE_INFINITY
        var tmax = Float.POSITIVE_INFINITY

        for (i in 0 until 3)  {
            val t1 = (aabb.min[i] - ray.origin[i]) * ray.invDirection[i]
            val t2 = (aabb.max[i] - ray.origin[i]) * ray.invDirection[i]

            tmin = max(tmin, min(t1, t2))
            tmax = min(tmax, max(t1, t2))
        }

        val isIntersecting = tmax > maxOf(tmin, 0f)
        return if (isIntersecting) tmin else null
    }

    private fun calculateVoxelAABB(voxel: Voxel): AABB {
        val center = voxel.pos
        val halfSideVec =  Vector3D(Voxel.HALF_SIDE, Voxel.HALF_SIDE, Voxel.HALF_SIDE)
        return AABB(
            center - halfSideVec,
            center + halfSideVec
        )
    }
}