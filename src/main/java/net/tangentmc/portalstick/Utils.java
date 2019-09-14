package net.tangentmc.portalstick;

import com.sk89q.worldedit.math.BlockVector3;
import net.minecraft.server.v1_14_R1.PlayerChunkMap;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_14_R1.block.data.CraftBlockData;
import org.bukkit.entity.Entity;
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

    public static BlockVector3 c(Vector v) {
        return BlockVector3.at(v.getBlockX(),v.getBlockY(),v.getBlockZ());
    }

    public static Vector center() {
        return new Vector(0.5, 0.5, 0.5);
    }

    public static Entity spawnFallingBlock(Location loc, BlockData data) {
        net.minecraft.server.v1_14_R1.World world = ((CraftWorld) loc.getWorld()).getHandle();
        net.minecraft.server.v1_14_R1.Entity nmsEntity = new CustomFallingBlock(world, loc.getX(), loc.getY(), loc.getZ(), ((CraftBlockData) data).getState());
        nmsEntity.activatedTick = 1;
        world.addEntity(nmsEntity);
        return nmsEntity.getBukkitEntity();
    }
}
