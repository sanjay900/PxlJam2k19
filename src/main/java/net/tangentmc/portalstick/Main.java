package net.tangentmc.portalstick;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
    private static Main instance;

    @Override
    public void onEnable() {
        Main.instance = this;

    }

    public static Main getInstance() {
        return instance;
    }
}
