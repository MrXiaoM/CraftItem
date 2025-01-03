package cn.jrmcdp.craftitem;

import cn.jrmcdp.craftitem.config.*;
import cn.jrmcdp.craftitem.data.CraftData;
import cn.jrmcdp.craftitem.listener.GuiListener;
import cn.jrmcdp.craftitem.listener.PlayerListener;
import cn.jrmcdp.craftitem.minigames.GameManager;
import cn.jrmcdp.craftitem.utils.*;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class CraftItem extends JavaPlugin {
    public static final String netKyori;
    static {
        netKyori = new String(new char[] { 'n', 'e', 't', '.', 'k', 'y', 'o', 'r', 'i' });
    }
    private static CraftItem plugin;

    private static Economy econ;

    private static GameManager miniGames = null;
    private static InventoryFactory inventoryFactory;
    private GuiListener guiListener;
    private PlayerListener playerListener;
    YamlConfiguration config;

    public static CraftItem getPlugin() {
        return plugin;
    }

    public static Economy getEcon() {
        return econ;
    }

    public static GameManager getMiniGames() {
        return miniGames;
    }

    public static InventoryFactory getInventoryFactory() {
        return inventoryFactory;
    }

    @Override
    public void onLoad() {
        MinecraftVersion.replaceLogger(getLogger());
        MinecraftVersion.disableUpdateCheck();
        MinecraftVersion.disableBStats();
        MinecraftVersion.getVersion();
    }

    public void onEnable() {
        super.onEnable();
        if (!setupEconomy()) {
            Bukkit.getConsoleSender().sendMessage(Message.prefix + "§c未安装 Vault 或无法找到已与 Vault 挂钩的经济插件，自动关闭本插件");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        PlaceholderSupport.init();
        MiniMessageConvert.init();
        if (Utils.isPresent("com.destroystokyo.paper.utils.PaperPluginLogger")
        && Utils.isPresent(netKyori + ".adventure.text.Component")) try {
            inventoryFactory = new PaperInventoryFactory();
        } catch (Throwable ignored) {
            inventoryFactory = new BukkitInventoryFactory();
        } else {
            inventoryFactory = new BukkitInventoryFactory();
        }
        miniGames = new GameManager(this);
        plugin = this;
        saveDefaultConfig();
        ConfigurationSerialization.registerClass(CraftData.class);
        Message.reload();
        CraftMaterial.reload();
        Config.reload();
        Craft.reload();
        ForgeGui.reload();
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
        ConfigUtils.saveResource("config.yml");

        File folder = new File(getDataFolder(), "PlayerData");
        Utils.createDirectory(folder);

        for (String filename : new String[] {
                "Material.yml",
                "Craft.yml",
                "Gui.yml",
                "Category.yml",
        }) {
            ConfigUtils.saveResource(filename);
        }
    }

    public @NotNull FileConfiguration getConfig() {
        if (config == null) {
            this.reloadConfig();
        }
        return config;
    }
    @Override
    public void reloadConfig() {
        config = ConfigUtils.loadOrSaveResource("config.yml");
        if (miniGames != null) miniGames.reloadConfig();
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
