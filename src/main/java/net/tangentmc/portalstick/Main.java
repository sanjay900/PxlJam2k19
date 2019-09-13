package net.tangentmc.portalstick;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.types.Type;
import net.minecraft.server.v1_14_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Function;

import static net.tangentmc.portalstick.Utils.injectNewEntity;

public class Main extends JavaPlugin implements Listener {
    private static Main instance;
    public static EntityTypes CUSTOM_FALLING_BLOCK;
    public static EntityTypes CUSTOM_SLIME;
    @Override
    public void onEnable() {
        Main.instance = this;
        Bukkit.getPluginCommand("spawnPushable").setExecutor((commandSender, command, s, strings) -> {
            Player pl = (Player)commandSender;
            new Pushable(pl.getLocation());
            return true;
        });

    }

    @Override
    public void onLoad() {
        // register the custom entity in the server
        // it is recommended to do this when the server is loading
        // but since we're not replacing vanilla entities it can be
        // done later if wanted
        CUSTOM_FALLING_BLOCK = injectNewEntity("custom_falling_block", "falling_block", (EntityTypes entitytypes, World world) -> new CustomFallingBlock(world, 0,0,0, Blocks.SAND.getBlockData()));
        CUSTOM_SLIME = injectNewEntity("custom_slime", "slime", CustomSlime::new);
    }

    public static Main getInstance() {
        return instance;
    }
}
