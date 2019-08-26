package net.tangentmc.portalstick;

import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;
import org.joml.Vector3d;

public class Utils {
    private static final BlockFace[] RADIAL = {BlockFace.WEST, BlockFace.NORTH_WEST, BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST};

    public static Vector3d from(Vector vector) {
        return new Vector3d(vector.getX(), vector.getY(), vector.getZ());
    }

    public static Vector from(Vector3d vector) {
        return new Vector(vector.x, vector.y, vector.z);
    }

    public static BlockFace getDirection(Vector facing, Vector rotation) {
        if (facing.getX() == 0 && facing.getZ() == 0) {
            return BlockFace.DOWN;
        } else {
            int index = (int) Math.round(Math.atan2(-rotation.getX(), rotation.getZ()) / (Math.PI / 4));
            return RADIAL[index & 0x7];
        }
    }
}
