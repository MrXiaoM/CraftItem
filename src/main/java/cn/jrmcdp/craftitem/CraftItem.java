package cn.jrmcdp.craftitem;

import cn.jrmcdp.craftitem.config.*;
import cn.jrmcdp.craftitem.data.CraftData;
import cn.jrmcdp.craftitem.listener.GuiListener;
import cn.jrmcdp.craftitem.listener.PlayerListener;

import java.io.File;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class CraftItem extends JavaPlugin {
    private static CraftItem plugin;

    private static Economy econ;

    public static CraftItem getPlugin() {
        return plugin;
    }

    public static Economy getEcon() {
        return econ;
    }

    public void onEnable() {
        super.onEnable();
        if (!setupEconomy()) {
            Bukkit.getConsoleSender().sendMessage(Message.prefix + "§c未安装 Vault，自动关闭插件");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
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
                new GuiListener(),
                new PlayerListener()
        );
        getCommand("CraftItem").setExecutor(new Cmd());
        Bukkit.getConsoleSender().sendMessage(Message.prefix + "§a插件成功启用 By.ZhiBuMiao (Q630580569)");
    }

    public void onDisable() {
        super.onDisable();
        ConfigurationSerialization.unregisterClass(CraftData.class);
        unRegListener(
                new GuiListener(),
                new PlayerListener()
        );
        HandlerList.unregisterAll(this);
        Bukkit.getConsoleSender().sendMessage(Message.prefix + "§c插件成功卸载 By.ZhiBuMiao (Q630580569)");
    }

    public void saveDefaultConfig() {
        super.saveDefaultConfig();
        File folder = new File(getDataFolder(), "PlayerData");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String[] files = {
                "Message.yml",
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

    public void regListener(Listener... list) {
        for (Listener listener : list)
            Bukkit.getPluginManager().registerEvents(listener, this);
    }

    public void unRegListener(Listener... list) {
        for (Listener listener : list) {
            HandlerList.unregisterAll(listener);
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
