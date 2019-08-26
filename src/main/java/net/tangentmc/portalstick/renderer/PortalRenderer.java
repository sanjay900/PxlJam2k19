package net.tangentmc.portalstick.renderer;

import net.tangentmc.portalstick.Portal;
import net.tangentmc.portalstick.TextureHandler;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.HashMap;

import static net.tangentmc.portalstick.Utils.from;

public class PortalRenderer extends MapRenderer {
    private static final int renderDistance = (16 * 16) ^ 2;
    private Portal portal;
    private int blockNum;

    public PortalRenderer(Portal portal, int blockNum) {
        this.portal = portal;
        this.blockNum = blockNum;
    }

    private Vector getPxVec(int x, int y, int res, Vector normal) {
        //TODO: floor portals
        //TODO: this seems stupidly overcomplicated
        double dx = normal.getZ() == 0 ? 0 : ((normal.getZ() == 1 ? res - x : x) / (double) res);
        double dy = y / (double) res;
        double dz = normal.getX() == 0 ? 0 : ((normal.getX() == 1 ? res - x : x) / (double) res);
        return new Vector(dx, dy, dz).add(normal.clone().setY(0));
    }

    @Override
    public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
        if (portal.getDestination() == null) return;
        HashMap<BlockVector, String> types = new HashMap<>();
        int res = 128;
        Location fromBlk = portal.getBlock(blockNum).getLocation();
        Location toBlk = portal.getDestination().getBlock(blockNum).getLocation();
        Vector normal = portal.getNormal();
        Vector dNormal = portal.getDestination().getNormal();
        for (int x = 0; x < res; x++) {
            for (int y = 0; y < res; y++) {

                Vector origPx = getPxVec(x, y, res, normal);
                Vector destPx = getPxVec(x, y, res, dNormal);
                Location orig = fromBlk.clone().add(origPx);
                Location dest = toBlk.clone().add(destPx);
                Vector originRay = orig.toVector().subtract(player.getEyeLocation().toVector()).normalize().multiply(0.1);
                Vector destRay = from(portal.getMatrixDirection().transform(from(originRay)));
                while (("air".equals(types.get(dest.toVector().toBlockVector())) || dest.getBlock().getType() == Material.AIR) && dest.distanceSquared(toBlk) < renderDistance) {
                    types.put(dest.toVector().toBlockVector(), "air");
                    dest.add(destRay);
                }
                String data = types.getOrDefault(dest.toVector().toBlockVector(), dest.getBlock().getBlockData().getAsString());
                ImageData image = TextureHandler.textureHandler.getImage(data);
                if (image == null) {
                    mapCanvas.setPixel(x, (res - y), MapPalette.TRANSPARENT);
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
                    mapCanvas.setPixel(x, (res - y), rgb);
                }

            }
        }
    }
}
