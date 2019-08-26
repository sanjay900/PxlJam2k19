package net.tangentmc.portalstick;

import net.tangentmc.portalstick.renderer.PortalRenderer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.bukkit.util.Vector;
import org.joml.Matrix3d;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

import static net.tangentmc.portalstick.Utils.from;
import static net.tangentmc.portalstick.Utils.getDirection;

public class Portal {
    private static final int portalSize = 2;
    private List<MapView> maps = new ArrayList<>();
    private List<Block> blocks = new ArrayList<>();
    private List<ItemFrame> entities = new ArrayList<>();
    private Matrix3d matrix = new Matrix3d();
    private Matrix3d matrixInv = new Matrix3d();
    private Portal destination;
    private Matrix3d destMatrix;

    public Portal(Block source, Vector normal, Vector rotation) {
        matrix.setLookAlong(from(normal), new Vector3d(0, 1, 0));
        matrix.setLookAlong(from(new Vector().subtract(normal)), new Vector3d(0, 1, 0));

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

    public void setDestination(Portal destination) {
        this.destination = destination;
        destMatrix = destination.matrix.mul(matrixInv);
    }

    public Portal getDestination() {
        return destination;
    }

    public Matrix3d getMatrix() {
        return destMatrix;
    }

    public Block getBlock(int i) {
        return blocks.get(i);
    }
}
