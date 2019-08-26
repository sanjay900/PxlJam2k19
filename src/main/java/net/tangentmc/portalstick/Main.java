package net.tangentmc.portalstick;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Main extends JavaPlugin implements Listener {
    List<Portal> portals = new ArrayList<>();
    private static Main instance;

    @Override
    public void onEnable() {
        Main.instance = this;
        Location block1 = new Location(Bukkit.getWorlds().get(0), -171, 76, 191);
        Location block2 = new Location(Bukkit.getWorlds().get(0), -174, 76, 191);
        Portal first = new Portal(block1.getBlock(), new Vector(0, 0, 1), new Vector(0, 0, 0));
        Portal second = new Portal(block2.getBlock(), new Vector(1, 0, 0), new Vector(0, 0, 0));
        second.setDestination(first);
        first.setDestination(second);
        Bukkit.getPluginManager().registerEvents(this, this);
        portals.add(first);
        portals.add(second);

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this, PacketType.Play.Client.POSITION) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                List<Double> doubles = event.getPacket().getDoubles().getValues();
                portals.forEach(p -> p.handlePlayerMove(event.getPlayer(), new Vector(doubles.get(0), doubles.get(1), doubles.get(2))));
            }
        });
    }

    public static Main getInstance() {
        return instance;
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
