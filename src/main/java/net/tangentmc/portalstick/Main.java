package net.tangentmc.portalstick;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class Main extends JavaPlugin implements Listener {
    private static Main instance;
    private BukkitTask rewinding = null;
    private List<Pushable> pushables = new ArrayList<>();

    @Override
    public void onEnable() {
        Main.instance = this;
        Bukkit.getPluginCommand("spawnPushable").setExecutor((commandSender, command, s, strings) -> {
            Player pl = (Player) commandSender;
            pushables.add(new Pushable(pl.getLocation()));
            return true;
        });
        Bukkit.getPluginCommand("rewind").setExecutor((commandSender, command, s, strings) -> {
            Player pl = (Player) commandSender;
            if (rewinding != null) {
                rewinding.cancel();
                rewinding = null;
                pushables.forEach(Pushable::stopRewind);

            } else {
                rewinding = Bukkit.getScheduler().runTaskTimer(this, () -> {
                    pushables.forEach(Pushable::rewind);
                }, 1l, 1l);
            }
            return true;
        });

    }

    @Override
    public void onLoad() {
    }

    public static Main getInstance() {
        return instance;
    }
}
