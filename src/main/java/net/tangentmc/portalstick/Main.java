package net.tangentmc.portalstick;

import com.google.common.collect.Lists;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Main extends JavaPlugin implements Listener {
    private Vector high = new Vector(-2, 60, 3);
    private Vector low = new Vector(-15,67,19);
    private Vector lowTarget = new Vector(100,65,100);
    private Vector start = new Vector(-8,63,18).subtract(low);
    private static Main instance;
    private BukkitTask rewinding = null;
    private List<Pushable> pushables = new ArrayList<>();
    private Vector lastCheckpoint = start.clone();
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
        Bukkit.getPluginCommand("start").setExecutor((commandSender, command, s, strings) -> {
            Player pl = (Player) commandSender;
            lastCheckpoint = start.clone().add(lowTarget);
            placeAndBegin(pl);
            return true;
        });
        Bukkit.getPluginCommand("retry").setExecutor((commandSender, command, s, strings) -> {
            Player pl = (Player) commandSender;
            Block b = pl.getLocation().getBlock();
            if (b.getType() == Material.BLACK_CARPET) {
                b.setType(Material.WHITE_CARPET);
            }
            if (b.getType() == Material.RED_CARPET) {
                b.setType(Material.LIME_CARPET);
            }
            placeAndBegin(pl);
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
        bar = Bukkit.createBossBar("Money", BarColor.BLUE, BarStyle.SEGMENTED_20);
        bar.setProgress(0);
        Bukkit.getOnlinePlayers().forEach(bar::addPlayer);
    }

    public void placeAndBegin(Player pl) {
        bar.setProgress(0);
        pl.getWorld().getEntitiesByClass(Item.class).forEach(Entity::remove);
        pushables.forEach(Pushable::remove);
        World w = BukkitAdapter.adapt(pl.getWorld());
        EditSession es = WorldEdit.getInstance().getEditSessionFactory().getEditSession(w, -1);
        Region region = new CuboidRegion(w, Utils.c(low), Utils.c(high));
        ForwardExtentCopy copy = new ForwardExtentCopy(es, region, Utils.c(low), es, Utils.c(lowTarget));
        copy.setCopyingEntities(false);
        copy.setCopyingBiomes(false);
        try {
            Operations.completeLegacy(copy);
        } catch (MaxChangedBlocksException e) {
            throw new RuntimeException(e);
        }
        es.commit();
        es.flushSession();
        pl.teleport(lastCheckpoint.toLocation(pl.getWorld()).setDirection(new Vector(0,0,-1)));
        BlockVector3 low = region.getMinimumPoint().subtract(Utils.c(this.low)).add(Utils.c(this.lowTarget));
        BlockVector3 high = region.getMaximumPoint().subtract(Utils.c(this.low)).add(Utils.c(this.lowTarget));
        for (int x = low.getX(); x <= high.getX(); x++) {
            for (int y = low.getY(); y <= high.getY(); y++) {
                for (int z = low.getZ(); z <= high.getZ(); z++) {
                    Block b = pl.getWorld().getBlockAt(x,y,z);
                    if (b.getType() == Material.DIAMOND_BLOCK) {
                        b.setType(Material.AIR);
                        pushables.add(new Pushable(b.getLocation().add(Utils.center().setY(0))));
                    }
                    if (b.getType() == Material.GOLD_BLOCK) {
                        b.setType(Material.AIR);
                        b.getWorld().dropItem(b.getLocation().add(Utils.center().setY(0)), new ItemStack(Material.GOLD_INGOT));
                    }
                }
            }
        }
    }

    @Override
    public void onLoad() {
    }

    @Override
    public void onDisable() {
        bar.removeAll();
        pushables.forEach(Pushable::remove);
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent evt) {
        bar.addPlayer(evt.getPlayer());
    }

    @EventHandler
    public void playerMove(PlayerMoveEvent evt) {
        Block b = evt.getTo().getBlock();
        if (b.getType() == Material.BLACK_CARPET) {
            b.setType(Material.WHITE_CARPET);
            lastCheckpoint = b.getLocation().toVector().add(Utils.center().setY(0));

        }
        if (b.getType() == Material.RED_CARPET) {
            b.setType(Material.LIME_CARPET);
            lastCheckpoint = b.getLocation().toVector().add(Utils.center().setY(0));
        }
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
