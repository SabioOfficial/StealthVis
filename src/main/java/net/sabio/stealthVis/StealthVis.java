package net.sabio.stealthVis;

import org.bukkit.plugin.java.JavaPlugin;

public final class StealthVis extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new StealthVisListener(this), this);
        getLogger().info("[StealthVis] StealthVis v1.0 activated!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
