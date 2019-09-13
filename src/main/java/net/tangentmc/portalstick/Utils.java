package net.tangentmc.portalstick;

import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;
public class Utils {
    private static final BlockFace[] RADIAL = {BlockFace.WEST, BlockFace.NORTH_WEST, BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST};

    public static BlockFace getDirection(Vector facing, Vector rotation) {
        if (facing.getX() == 0 && facing.getZ() == 0) {
            return BlockFace.DOWN;
        } else {
            int index = (int) Math.round(Math.atan2(-facing.getX(), facing.getZ()) / (Math.PI / 4));
            return RADIAL[index & 0x7];
        }
    }

    public static Vector center() {
        return new Vector(0.5,0.5,0.5);
    }
}
