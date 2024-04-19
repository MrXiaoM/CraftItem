package cn.jrmcdp.craftitem.minigames.utils;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * This class implements the VersionManager interface and is responsible for managing version-related information.
 */
public class VersionManager {

    private final boolean isNewerThan1_19_R2;
    private final boolean isNewerThan1_19_R3;
    private final boolean isNewerThan1_20;
    private final boolean isNewerThan1_19;
    private final String serverVersion;
    private final boolean isSpigot;
    private boolean hasRegionScheduler;
    private boolean isMojmap;
    private final String pluginVersion;

    public VersionManager(JavaPlugin plugin) {

        // Get the server version
        serverVersion = plugin.getServer().getClass().getPackage().getName().split("\\.")[3];
        String[] split = serverVersion.split("_");
        int main_ver = Integer.parseInt(split[1]);

        if (main_ver >= 20) {
            isNewerThan1_19_R2 = true;
            isNewerThan1_19_R3 = true;
            isNewerThan1_20 = true;
            isNewerThan1_19 = true;
        } else if (main_ver == 19) {
            isNewerThan1_19_R2 = Integer.parseInt(split[2].substring(1)) >= 2;
            isNewerThan1_19_R3 = Integer.parseInt(split[2].substring(1)) >= 3;
            isNewerThan1_20 = false;
            isNewerThan1_19 = true;
        } else {
            isNewerThan1_19_R2 = false;
            isNewerThan1_19_R3 = false;
            isNewerThan1_20 = false;
            isNewerThan1_19 = false;
        }

        // Check if the server is Spigot
        String server_name = plugin.getServer().getName();
        this.isSpigot = server_name.equals("CraftBukkit");

        // Check if the server is Folia
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.AsyncScheduler");
            this.hasRegionScheduler = true;
        } catch (ClassNotFoundException ignored) {

        }

        // Check if the server is Mojmap
        try {
            Class.forName("net.minecraft.network.protocol.game.ClientboundBossEventPacket");
            this.isMojmap = true;
        } catch (ClassNotFoundException ignored) {

        }

        // Get the plugin version
        this.pluginVersion = plugin.getDescription().getVersion();
    }

   
    public boolean isVersionNewerThan1_19() {
        return isNewerThan1_19;
    }

   
    public boolean isVersionNewerThan1_19_R3() {
        return isNewerThan1_19_R3;
    }


   
    public boolean isVersionNewerThan1_19_R2() {
        return isNewerThan1_19_R2;
    }

   
    public boolean isVersionNewerThan1_20() {
        return isNewerThan1_20;
    }

   
    public boolean isSpigot() {
        return isSpigot;
    }

   
    public String getPluginVersion() {
        return pluginVersion;
    }

   
    public boolean hasRegionScheduler() {
        return hasRegionScheduler;
    }

   
    public boolean isMojmap() {
        return isMojmap;
    }

   
    public String getServerVersion() {
        return serverVersion;
    }

    // Method to compare two version strings
    private boolean compareVer(String newV, String currentV) {
        if (newV == null || currentV == null || newV.isEmpty() || currentV.isEmpty()) {
            return false;
        }
        String[] newVS = newV.split("\\.");
        String[] currentVS = currentV.split("\\.");
        int maxL = Math.min(newVS.length, currentVS.length);
        for (int i = 0; i < maxL; i++) {
            try {
                String[] newPart = newVS[i].split("-");
                String[] currentPart = currentVS[i].split("-");
                int newNum = Integer.parseInt(newPart[0]);
                int currentNum = Integer.parseInt(currentPart[0]);
                if (newNum > currentNum) {
                    return true;
                } else if (newNum < currentNum) {
                    return false;
                } else if (newPart.length > 1 && currentPart.length > 1) {
                    String[] newHotfix = newPart[1].split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
                    String[] currentHotfix = currentPart[1].split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
                    if (newHotfix.length == 2 && currentHotfix.length == 1) return true;
                    else if (newHotfix.length > 1 && currentHotfix.length > 1) {
                        int newHotfixNum = Integer.parseInt(newHotfix[1]);
                        int currentHotfixNum = Integer.parseInt(currentHotfix[1]);
                        if (newHotfixNum > currentHotfixNum) {
                            return true;
                        } else if (newHotfixNum < currentHotfixNum) {
                            return false;
                        } else {
                            return newHotfix[0].compareTo(currentHotfix[0]) > 0;
                        }
                    }
                } else if (newPart.length > 1) {
                    return true;
                } else if (currentPart.length > 1) {
                    return false;
                }
            }
            catch (NumberFormatException ignored) {
                return false;
            }
        }
        return newVS.length > currentVS.length;
    }
}
