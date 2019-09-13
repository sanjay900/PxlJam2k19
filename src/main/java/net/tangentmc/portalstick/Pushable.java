package net.tangentmc.portalstick;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;


public class Pushable implements Listener {
    private FallingBlock fallingBlock;

    private Minecart minecart;
    public Pushable(Location location) {
        location.setYaw(0);
        location.setPitch(0);
        fallingBlock = (FallingBlock) Utils.spawnEntity(Main.CUSTOM_FALLING_BLOCK, location);
        minecart = location.getWorld().spawn(location, Minecart.class);
        Shulker s = location.getWorld().spawn(location, Shulker.class);
        s.setAI(false);
        minecart.addPassenger(s);
        minecart.addPassenger(fallingBlock);

        Bukkit.getPluginManager().registerEvents(this, Main.getInstance());
    }

    @EventHandler
    public void pl(PlayerMoveEvent evt) {
        Vector v = minecart.getLocation().subtract(evt.getTo()).toVector();
        if (v.lengthSquared() < 2 && Math.abs(v.getY()) < 1) {
            minecart.setVelocity(v);
        }
    }
}
