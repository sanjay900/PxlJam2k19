package net.tangentmc.portalstick;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class Main extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Location block1 = new Location(Bukkit.getWorlds().get(0), -171, 76, 191);
        Location block2 = new Location(Bukkit.getWorlds().get(0), -174, 76, 191);
        Portal first = new Portal(block1.getBlock(), new Vector(0,0,-1), new Vector(0,0,0));
        Portal second = new Portal(block2.getBlock(), new Vector(0,0,-1), new Vector(0,0,0));
        second.setDestination(first);
        first.setDestination(second);
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        Bukkit.getWorlds().forEach(world -> world.getEntities().forEach(entity -> {
            if (entity.getScoreboardTags().contains("deleteMe")) {
                entity.remove();
            }
        }));
    }

}
