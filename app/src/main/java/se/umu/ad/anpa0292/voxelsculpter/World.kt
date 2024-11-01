package se.umu.ad.anpa0292.voxelsculpter

import android.util.Log
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.max
import kotlin.math.min

data class Ray(val origin: Vector3D, val direction: Vector3D) {
    val invDirection = Vector3D(1 / direction.x, 1 / direction.y, 1 / direction.z)
}
data class AABB(val min: Vector3D, val max: Vector3D)

data class IntersectionRes(val voxel: Voxel, val t: Float, val normal: Vector3D);

class World() {
    val camera: PerspectiveCamera = PerspectiveCamera(
        Vector3D(0f, 0f, 0f),
        60f,
        0f, 0f
    )

    val voxels = CopyOnWriteArrayList<Voxel>(listOf(Voxel(Vector3D(0.0f, 0.0f, 0.0f))))
    var selectedVoxel = voxels[0]

    private fun addVoxel(voxel: Voxel) {
        voxels.add(voxel)
    }

    private fun removeVoxel(voxel: Voxel) {
        voxels.remove(voxel)
    }

    fun selectVoxelAtCenter() {
        val (viewportWidth, viewportHeight) = camera.getViewport()
        val result = getVoxelAtScreenPos(
            Vector3D(viewportWidth / 2f, viewportHeight / 2f, 0f)
        )

        result?.let { (voxel, _, _) -> selectedVoxel = voxel }
    }

    fun addVoxelAtScreenPos(screenPos: Vector3D) {
        val result = getVoxelAtScreenPos(screenPos)
        result?.let { (voxel, _, normal) ->
            val newVoxel = Voxel(voxel.pos + normal)
            addVoxel(newVoxel)
        }
    }

    fun removeVoxelAtScreenPos(screenPos: Vector3D) {
        val result = getVoxelAtScreenPos(screenPos)
        result?.let { (voxel, _, _) ->
            if (voxels.size == 1) return@let

            removeVoxel(voxel)
            if (voxel == selectedVoxel) {
                selectedVoxel = getClosestVoxel(voxel.pos)
            }
        }
    }

    fun getClosestVoxel(pos: Vector3D): Voxel {
        return voxels.minBy { (pos - it.pos).norm() }
    }

    fun getVoxelAtScreenPos(screenPos: Vector3D): IntersectionRes? {
        val ray = camera.screenPosToWorldRay(screenPos)
        val intersectionResList = mutableListOf<IntersectionRes>()
        for (voxel in voxels) {
            val aabb = calculateVoxelAABB(voxel)
            intersectRayAABB(ray, aabb)?.let {
                val (t, normal) = it
                intersectionResList.add(
                    IntersectionRes(voxel, t, normal)
                )
            }
        }

        return if (intersectionResList.isNotEmpty()) intersectionResList.minBy { it.t } else null
    }

    fun centerCamera() {
        camera.setTarget(selectedVoxel.pos)
    }

    private fun intersectRayAABB(ray: Ray, aabb: AABB): Pair<Float, Vector3D>? {
        var tmin = Float.NEGATIVE_INFINITY
        var tmax = Float.POSITIVE_INFINITY
        var normal: Vector3D? = null

        for (i in 0 until 3) {
            val t1 = (aabb.min[i] - ray.origin[i]) * ray.invDirection[i]
            val t2 = (aabb.max[i] - ray.origin[i]) * ray.invDirection[i]

            val (near, far) = if (t1 < t2) t1 to t2 else t2 to t1

            if (near > tmin) {
                tmin = near
                // Update normal for the near side
                normal = when (i) {
                    0 -> if (t1 < t2) Vector3D(-1f, 0f, 0f) else Vector3D(1f, 0f, 0f) // X axis
                    1 -> if (t1 < t2) Vector3D(0f, -1f, 0f) else Vector3D(0f, 1f, 0f) // Y axis
                    2 -> if (t1 < t2) Vector3D(0f, 0f, -1f) else Vector3D(0f, 0f, 1f) // Z axis
                    else -> null
                }
            }

            if (far < tmax) {
                tmax = far
            }

            if (tmin > tmax || tmax < 0) {
                return null // No intersection
            }
        }

        return Pair(tmin, normal!!)
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