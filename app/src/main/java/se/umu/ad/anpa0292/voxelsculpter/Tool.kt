import se.umu.ad.anpa0292.voxelsculpter.Vector3D
import se.umu.ad.anpa0292.voxelsculpter.World

enum class Tool {
    ADD {
        override fun use(world: World, at: Vector3D) {
            world.addVoxelAtScreenPos(at)
        }
    },
    REMOVE {
        override fun use(world: World, at: Vector3D) {
            world.removeVoxelAtScreenPos(at)
        }
    },
    PAINT {
        override fun use(world: World, at: Vector3D) {
            println("Using the PAINT tool.")
        }
    };
    abstract fun use(world: World, at: Vector3D)
}