package cn.jrmcdp.craftitem;

import cn.jrmcdp.craftitem.config.*;
import cn.jrmcdp.craftitem.data.CraftData;
import cn.jrmcdp.craftitem.listener.GuiListener;
import cn.jrmcdp.craftitem.listener.PlayerListener;

import java.io.File;

import cn.jrmcdp.craftitem.minigames.GameManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class CraftItem extends JavaPlugin {
    private static CraftItem plugin;

    private static Economy econ;

    private static GameManager miniGames;
    private GuiListener guiListener;
    private PlayerListener playerListener;

    public static CraftItem getPlugin() {
        return plugin;
    }

    public static Economy getEcon() {
        return econ;
    }

    public static GameManager getMiniGames() {
        return miniGames;
    }

    public void onEnable() {
        super.onEnable();
        if (!setupEconomy()) {
            Bukkit.getConsoleSender().sendMessage(Message.prefix + "§c未安装 Vault，自动关闭插件");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        miniGames = new GameManager(this);
        saveDefaultConfig();
        plugin = this;
        ConfigurationSerialization.registerClass(CraftData.class);
        Message.reload();
        Material.reload();
        Config.reload();
        Craft.reload();
        Gui.reload();
        Category.reload();
        regListener(
                guiListener = new GuiListener(this),
                playerListener = new PlayerListener()
        );
        Cmd.register(this, "CraftItem");
        Bukkit.getConsoleSender().sendMessage(Message.prefix + "§a插件成功启用 By.ZhiBuMiao & MrXiaoM");
    }

    public void onDisable() {
        super.onDisable();
        if (miniGames != null) miniGames.disable();
        if (guiListener != null) guiListener.onDisable();
        ConfigurationSerialization.unregisterClass(CraftData.class);
        unRegListener(guiListener, playerListener);
        HandlerList.unregisterAll(this);
        Bukkit.getConsoleSender().sendMessage(Message.prefix + "§2插件成功卸载 By.ZhiBuMiao & MrXiaoM");
    }

    public void saveDefaultConfig() {
        super.saveDefaultConfig();
        File folder = new File(getDataFolder(), "PlayerData");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String[] files = {
                "Material.yml",
                "Craft.yml",
                "Gui.yml",
                "Category.yml",
        };
        for (String filename : files) {
            File file = new File(getDataFolder(), filename);
            if (!file.exists())
                saveResource(filename, false);
        }
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        FileConfiguration config = getConfig();
        config.addDefault("TimeForgeConditions", null);
        config.addDefault("offset-characters", null);
        miniGames.reloadConfig();
    }

    public void regListener(Listener... list) {
        for (Listener listener : list) {
            if (listener != null) Bukkit.getPluginManager().registerEvents(listener, this);
        }
    }

    public void unRegListener(Listener... list) {
        for (Listener listener : list) {
            if (listener != null) HandlerList.unregisterAll(listener);
        }
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null)
            return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null)
            return false;
        econ = rsp.getProvider();
        return true;
    }
}
