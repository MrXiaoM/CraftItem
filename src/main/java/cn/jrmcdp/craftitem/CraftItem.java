package cn.jrmcdp.craftitem;

import cn.jrmcdp.craftitem.config.*;
import cn.jrmcdp.craftitem.data.CraftData;
import cn.jrmcdp.craftitem.gui.IHolder;
import cn.jrmcdp.craftitem.minigames.GameManager;
import cn.jrmcdp.craftitem.utils.*;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.api.IRunTask;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.pluginbase.utils.scheduler.FoliaLibScheduler;

import java.io.File;

public class CraftItem extends BukkitPlugin {
    public static final String netKyori;
    static {
        netKyori = new String(new char[] { 'n', 'e', 't', '.', 'k', 'y', 'o', 'r', 'i' });
    }
    private static CraftItem plugin;
    private static InventoryFactory inventoryFactory;
    YamlConfiguration config;
    private IRunTask timer;

    public CraftItem() {
        super(new OptionsBuilder()
                .bungee(false)
                .adventure(true)
                .database(false)
                .reconnectDatabaseWhenReloadConfig(false)
                .vaultEconomy(true)
                .scanIgnore("cn.jrmcdp.craftitem.libs"));
        scheduler = new FoliaLibScheduler(this);
    }

    public static CraftItem getPlugin() {
        return (CraftItem) getInstance();
    }

    public static GameManager getMiniGames() {
        return GameManager.inst();
    }

    public static InventoryFactory getInventoryFactory() {
        return inventoryFactory;
    }

    @Override
    public void beforeLoad() {
        MinecraftVersion.replaceLogger(getLogger());
        MinecraftVersion.disableUpdateCheck();
        MinecraftVersion.disableBStats();
        MinecraftVersion.getVersion();
    }

    @Override
    public void beforeEnable() {
        if (Util.isPresent("com.destroystokyo.paper.utils.PaperPluginLogger")
        && Util.isPresent(netKyori + ".adventure.text.Component")) try {
            inventoryFactory = new PaperInventoryFactory();
        } catch (Throwable ignored) {
            inventoryFactory = new BukkitInventoryFactory();
        } else {
            inventoryFactory = new BukkitInventoryFactory();
        }
        saveDefaultConfig();
        ConfigurationSerialization.registerClass(CraftData.class);
        Message.reload();
        CraftMaterial.reload();
        Config.reload();
    }

    public void onSecond() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            InventoryHolder holder = player.getOpenInventory().getTopInventory().getHolder();
            if (holder instanceof IHolder) {
                ((IHolder) holder).onSecond();
            }
        }
    }

    @Override
    protected void afterEnable() {
        this.timer = getScheduler().runTaskTimer(this::onSecond, 20L, 20L);
        Bukkit.getConsoleSender().sendMessage(Message.prefix + "§a插件成功启用 By.ZhiBuMiao & MrXiaoM");
    }

    @Override
    public void beforeDisable() {
        if (timer != null) timer.cancel();
        ConfigurationSerialization.unregisterClass(CraftData.class);
        HandlerList.unregisterAll(this);
        Bukkit.getConsoleSender().sendMessage(Message.prefix + "§2插件成功卸载 By.ZhiBuMiao & MrXiaoM");
    }

    public void saveDefaultConfig() {
        ConfigUtils.saveResource("config.yml");

        File folder = new File(getDataFolder(), "PlayerData");
        Util.mkdirs(folder);

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
        super.reloadConfig();
        config = ConfigUtils.loadOrSaveResource("config.yml");
    }
}
