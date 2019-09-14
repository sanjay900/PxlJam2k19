package net.tangentmc.portalstick;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Piston;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Shulker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;


public class Pushable implements Listener, Runnable {
    private Map<BlockVector, Long> lastUsed = new HashMap<>();
    private Stack<Location> locations = new Stack<>();
    private Vector last;
    private long lastTime;
    private Minecart minecart;
    private BlockData data;
    private BukkitTask task;
    private boolean isRewinding = false;

    public Pushable(Location location) {
        data = Bukkit.createBlockData(Material.DIAMOND_BLOCK);
        spawn(location);
    }

    private void spawn(Location location) {
        location.setYaw(0);
        location.setPitch(0);
        FallingBlock fallingBlock = (FallingBlock) Utils.spawnFallingBlock(location, data);
        minecart = location.getWorld().spawn(location, Minecart.class);
        Shulker s = location.getWorld().spawn(location, Shulker.class);
        s.setAI(false);
        s.setInvulnerable(true);
        s.setSilent(true);
        fallingBlock.setInvulnerable(true);
        fallingBlock.setSilent(true);
        minecart.setSilent(true);
        minecart.setInvulnerable(true);
        minecart.addPassenger(s);
        minecart.addPassenger(fallingBlock);
        Bukkit.getPluginManager().registerEvents(this, Main.getInstance());
        task = Bukkit.getScheduler().runTaskTimer(Main.getInstance(), this, 0, 1);
    }

    @EventHandler
    public void pl(PlayerMoveEvent evt) {
        Vector v = minecart.getLocation().subtract(evt.getTo()).toVector();
        if (v.lengthSquared() < 2 && Math.abs(v.getY()) < 1) {
            minecart.setVelocity(v);
        }
    }

    public void rewind() {
        minecart.setGravity(false);
        isRewinding = true;
        if (task.isCancelled()) {
            minecart.getLocation().getBlock().setType(Material.AIR);
            spawn(locations.pop());
            return;
        }
        if (locations.isEmpty()) {
            minecart.setVelocity(new Vector(0, 0, 0));
        } else {
            Location l = locations.pop();
            Vector v = l.subtract(minecart.getLocation()).toVector().multiply(1.0f / Main.TIME_SCALE);
            if (v.lengthSquared() > 2) {
                if (!locations.empty()) {
                    l = locations.pop();
                }
                teleport(l);
            } else {
                minecart.setVelocity(v);
            }
        }
    }

    @Override
    public void run() {
        if (isRewinding) return;
        if (locations.isEmpty() || locations.peek().distanceSquared(minecart.getLocation()) > 0.5) {
            locations.push(minecart.getLocation());
        }
        if (System.currentTimeMillis() - 1000 < lastTime) {
            minecart.setVelocity(last.setY(last.getY() - 0.0098));
        }
        for (BlockFace bf : BlockFace.values()) {
            Block b = minecart.getLocation().getBlock().getRelative(bf);
            if (b.getType() == Material.PISTON && lastUsed.getOrDefault(new BlockVector(b.getX(), b.getY(), b.getZ()), System.currentTimeMillis()) + 1000 > System.currentTimeMillis()) {
                Piston p = (Piston) b.getBlockData();
                if (p.getFacing() == bf.getOppositeFace()) {
                    minecart.setVelocity(last = p.getFacing().getDirection().multiply(5).setY(p.getFacing().getModY()));
                    if (last.getX() != 0 || last.getZ() != 0) {
                        lastTime = System.currentTimeMillis();
                        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                            p.setExtended(true);
                            b.setBlockData(p, false);
                            b.getRelative(p.getFacing()).setBlockData(Material.PISTON_HEAD.createBlockData());
                        }, 1);
                    }
                }
            }
            if (b.getType() == Material.CARVED_PUMPKIN) {
                Directional d = (Directional) b.getBlockData();
                if (d.getFacing() == bf.getOppositeFace()) {
                    minecart.getLocation().getBlock().setBlockData(data);
                    minecart.getPassengers().forEach(Entity::remove);
                    minecart.remove();
                    task.cancel();
                }
            }
            if (b.getType() == Material.OBSERVER) {
                Directional d = (Directional) b.getBlockData();
                if (d.getFacing() == bf) {
                    Block signB = b.getRelative(d.getFacing());
                    Sign sign = (Sign) signB.getState();
                    String[] split = sign.getLine(0).split(",");
                    Block dest = signB.getWorld().getBlockAt(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
                    d = (Directional) dest.getBlockData();
                    teleport(dest.getLocation().add(d.getFacing().getDirection()));
                }
            }
        }
    }

    public void teleport(Location to) {
        List<Entity> entities = minecart.getPassengers();
        entities.forEach(minecart::removePassenger);
        minecart.teleport(to);
        entities.forEach(minecart::addPassenger);
    }

    public void stopRewind() {
        minecart.setGravity(true);
        minecart.setVelocity(new Vector(0, 0, 0));
        isRewinding = false;
    }
}
