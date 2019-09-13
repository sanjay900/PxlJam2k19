package net.tangentmc.portalstick;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.types.Type;
import net.minecraft.server.v1_14_R1.*;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.util.Vector;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.bukkit.entity.Entity;

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
        return new Vector(0.5, 0.5, 0.5);
    }

    @SuppressWarnings("unchecked")
    public static EntityTypes injectNewEntity(String name, String extend_from, EntityTypes.b function) {
        // get the server's datatypes (also referred to as "data fixers")
        Map<String, Type<?>> types = (Map<String, Type<?>>) DataConverterRegistry.a().getSchema(DataFixUtils.makeKey(SharedConstants.a().getWorldVersion())).findChoiceType(DataConverterTypes.ENTITY).types();
        // inject the new custom entity (this registers the
        // name/id with the server so you can use it in things
        // like the vanilla /summon command)
        types.put("minecraft:" + name, types.get("minecraft:" + extend_from));
        // create and return an EntityTypes for the custom entity
        // store this somewhere so you can reference it later (like for spawning)
        EntityTypes.a<net.minecraft.server.v1_14_R1.Entity> a = EntityTypes.a.a(function, EnumCreatureType.MONSTER);
        return IRegistry.a(IRegistry.ENTITY_TYPE, name, a.a(name));
    }
    public static Entity spawnEntity(EntityTypes entityType, Location loc) {
        net.minecraft.server.v1_14_R1.World world = ((CraftWorld) loc.getWorld()).getHandle();
        net.minecraft.server.v1_14_R1.Entity nmsEntity = entityType.b( // NMS method to spawn an entity from an EntityTypes
                world, // reference to the NMS world
                null, // EntityTag NBT compound
                null, // custom name of entity
                null, // player reference. used to know if player is OP to apply EntityTag NBT compound
                new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), // the BlockPosition to spawn at
                null,
                true, // center entity on BlockPosition and correct Y position for Entity's height
                false); // not sure. alters the Y position. this is only ever true when using spawn egg and clicked face is UP
        nmsEntity.activatedTick = 1;
        world.addEntity(nmsEntity);
        return nmsEntity.getBukkitEntity(); // convert to a Bukkit entity
    }
}
