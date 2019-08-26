package net.tangentmc.portalstick.renderer;

import net.tangentmc.portalstick.Portal;
import net.tangentmc.portalstick.TextureHandler;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

import static net.tangentmc.portalstick.Utils.from;

public class PortalRenderer extends MapRenderer {
    private static final int renderDistance = (16 * 16) ^ 2;
    private Portal portal;
    private int blockNum;

    public PortalRenderer(Portal portal, int blockNum) {
        this.portal = portal;
        this.blockNum = blockNum;
    }

    @Override
    public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
        if (portal.getDestination() == null) return;
        Set<BlockVector> air = new HashSet<>();
        int res = 128;
        Location block1 = portal.getBlock(blockNum).getLocation();
        Location block2 = portal.getDestination().getBlock(blockNum).getLocation();
        for (int x = 0; x < res; x++) {
            for (int y = 0; y < res; y++) {
                Vector origPx = new Vector((res - x) / (double) res, (res - y) / (double) res, 0);
                Vector destPx = from(portal.getMatrix().transform(from(origPx)));
                Location orig = block1.clone().add(origPx);
                Location dest = block2.clone().add(destPx);
                Vector originRay = orig.subtract(player.getEyeLocation().subtract(player.getLocation().getDirection().normalize())).toVector().normalize().multiply(0.1);
                Vector destRay = from(portal.getMatrix().transform(from(originRay)));
                while ((air.contains(dest.toVector().toBlockVector()) || dest.getBlock().getType() == Material.AIR) && dest.distanceSquared(block2) < renderDistance) {
                    air.add(dest.toVector().toBlockVector());
                    dest.add(destRay);
                }
                Block b = dest.getBlock();
                ImageData image = TextureHandler.textureHandler.getImage(b.getBlockData().getAsString());
                if (image == null) {
                    mapCanvas.setPixel(x, y, MapPalette.TRANSPARENT);
                } else {
                    int width = image.width;
                    int height = image.height;
                    int px, py;
                    double dx = dest.getX() - dest.getBlockX();
                    double dy = dest.getY() - dest.getBlockY();
                    double dz = dest.getZ() - dest.getBlockZ();
                    double abx = Math.abs(destRay.getX());
                    double aby = Math.abs(destRay.getY());
                    double abz = Math.abs(destRay.getZ());
                    if (abx < aby && abx < abz) {
                        px = (int) (dy * width);
                        py = (int) (dz * height);
                    } else if (aby < abx && aby < abz) {
                        px = (int) (dx * width);
                        py = (int) (dz * height);
                    } else {
                        px = (int) (dx * width);
                        py = (int) (dy * height);
                    }

                    byte rgb = image.bytes[py * width + px];
                    mapCanvas.setPixel(x, y, rgb);
                }

            }
        }
    }
}
