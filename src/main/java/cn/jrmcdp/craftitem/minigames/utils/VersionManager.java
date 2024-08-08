package cn.jrmcdp.craftitem.minigames.utils;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * This class implements the VersionManager interface and is responsible for managing version-related information.
 */
public class VersionManager {
    private final boolean isSpigot;

    public VersionManager(JavaPlugin plugin) {
        // Check if the server is Spigot
        String server_name = plugin.getServer().getName();
        this.isSpigot = server_name.equals("CraftBukkit");
    }

    public boolean isSpigot() {
        return isSpigot;
    }
}
