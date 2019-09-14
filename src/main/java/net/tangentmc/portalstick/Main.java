package net.tangentmc.portalstick;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class Main extends JavaPlugin implements Listener {
    private static Main instance;
    private BukkitTask rewinding = null;
    private List<Pushable> pushables = new ArrayList<>();
    BossBar bar;

    public static final double TIME_SCALE = 5;

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
                    if (bar.getProgress() <= 0) {
                        rewinding.cancel();
                        rewinding = null;
                        pushables.forEach(Pushable::stopRewind);
                        return;
                    };
                    pushables.forEach(Pushable::rewind);
                    bar.setProgress(Math.max(0,bar.getProgress() - 0.05));
                }, 1, 5);
            }
            return true;
        });
        Bukkit.getPluginManager().registerEvents(this, this);
        bar = Bukkit.createBossBar("Rewind Amount", BarColor.BLUE, BarStyle.SEGMENTED_20);
        bar.setProgress(0);
        Bukkit.getOnlinePlayers().forEach(bar::addPlayer);
    }

    @Override
    public void onLoad() {
    }

    @Override
    public void onDisable() {
        bar.removeAll();
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent evt) {
        bar.addPlayer(evt.getPlayer());
    }

    @EventHandler
    public void itemGet(EntityPickupItemEvent evt) {
        if (evt.getEntity() instanceof Player && evt.getItem().getItemStack().getType() == Material.GOLD_INGOT) {
            bar.setProgress(Math.min(1, bar.getProgress() + evt.getItem().getItemStack().getAmount() * 0.05));
            evt.setCancelled(true);
            evt.getItem().remove();
        }
    }

    public static Main getInstance() {
        return instance;
    }
}
