package net.tangentmc.portalstick;

import net.tangentmc.portalstick.renderer.PortalRenderer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.bukkit.util.Vector;
import org.joml.Matrix3d;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static net.tangentmc.portalstick.Utils.from;
import static net.tangentmc.portalstick.Utils.getDirection;

public class Portal {
    private static final BlockData FENCE = Bukkit.createBlockData(Material.OAK_FENCE);
    private static final int portalSize = 2;
    private List<MapView> maps = new ArrayList<>();
    private List<Block> blocks = new ArrayList<>();
    private List<ItemFrame> entities = new ArrayList<>();
    private Matrix3d matrix = new Matrix3d();
    private Matrix3d matrixOpp = new Matrix3d();
    private Portal destination;
    private Matrix3d destMatrix;
    private Matrix3d destMatrixSame;
    private HashSet<UUID> players = new HashSet<>();
    private HashSet<UUID> cooldown = new HashSet<>();
    private Vector normal;
    private Vector rotation;

    public Portal(Block source, Vector normal, Vector rotation) {
        matrix.setLookAlong(from(normal), new Vector3d(0, 1, 0));
        matrixOpp.setLookAlong(from(new Vector().subtract(normal)), new Vector3d(0, 1, 0));
        this.normal = normal;
        this.rotation = rotation;
        System.out.println(normal);
        System.out.println(getDirection(normal, rotation));
        BlockFace relation = getDirection(normal, rotation);
        if (normal.getY() == 0) {
            relation = BlockFace.DOWN;
        }

        for (int i = 0; i < portalSize; i++) {
            MapView mv = Bukkit.createMap(source.getWorld());
            maps.add(mv);
            mv.getRenderers().forEach(mv::removeRenderer);
            mv.addRenderer(new PortalRenderer(this, i));
            ItemFrame ifr = source.getWorld().spawn(source.getLocation().add(normal), ItemFrame.class);
            entities.add(ifr);
            ifr.addScoreboardTag("deleteMe");
            ItemStack it = new ItemStack(Material.FILLED_MAP);
            MapMeta meta = (MapMeta) it.getItemMeta();
            meta.setMapView(mv);
            it.setItemMeta(meta);
            ifr.setItem(it);
            blocks.add(source);
            source = source.getRelative(relation);
        }

    }

    public Vector getNormal() {
        return normal.clone();
    }

    public Vector getRotation() {
        return rotation.clone();
    }

    public void setDestination(Portal destination) {
        this.destination = destination;
        destMatrix = new Matrix3d(destination.matrix).invert().mul(matrixOpp);
        destMatrixSame = new Matrix3d(destination.matrix).invert().mul(matrix);
    }

    public Portal getDestination() {
        return destination;
    }

    public Matrix3d getMatrixDirection() {
        return destMatrix;
    }

    public Matrix3d getMatrixPosition() {
        return destMatrixSame;
    }

    public Block getBlock(int i) {
        return blocks.get(i);
    }


    public void handlePlayerMove(Player player, Vector to) {
        double dist = Double.MAX_VALUE;
        for (Block b : blocks) {
            dist = Math.min(to.distanceSquared(b.getLocation().toVector().add(Utils.center())), dist);
        }
        if (dist < 0.8 && !cooldown.contains(player.getUniqueId()) && !destination.cooldown.contains(player.getUniqueId())) {
            destination.cooldown.add(player.getUniqueId());
            Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                Vector fromV = blocks.get(0).getLocation().toVector().add(Utils.center()).subtract(player.getLocation().toVector());
                Vector toV = from(getMatrixPosition().transform(from(fromV)));
                Location dest = destination.blocks.get(0).getLocation().add(Utils.center()).subtract(toV);
                dest.setDirection(from(getMatrixDirection().transform(from(player.getLocation().getDirection()))));
                player.teleport(dest);
            });
            System.out.println("TP");
        } else if (dist > 4){
            destination.cooldown.remove(player.getUniqueId());
        }
        BlockData bd = null;
        if (dist <= 4 && players.add(player.getUniqueId())) {
            bd = FENCE;
        }
        boolean removed = dist > 4 && players.remove(player.getUniqueId());
        for (Block b : blocks) {
            if (removed) {
                bd = b.getBlockData();
            }
            if (bd != null) {
                player.sendBlockChange(b.getLocation(), bd);
            }
        }
    }

    public void remove() {
        entities.forEach(Entity::remove);
    }
}
