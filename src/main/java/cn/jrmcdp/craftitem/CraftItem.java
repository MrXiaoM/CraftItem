package cn.jrmcdp.craftitem;

import cn.jrmcdp.craftitem.actions.ActionBack;
import cn.jrmcdp.craftitem.config.*;
import cn.jrmcdp.craftitem.data.CraftData;
import cn.jrmcdp.craftitem.gui.IHolder;
import cn.jrmcdp.craftitem.minigames.GameManager;
import cn.jrmcdp.craftitem.utils.*;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.actions.ActionProviders;
import top.mrxiaom.pluginbase.api.IRunTask;
import top.mrxiaom.pluginbase.economy.EnumEconomy;
import top.mrxiaom.pluginbase.economy.IEconomy;
import top.mrxiaom.pluginbase.func.LanguageManager;
import top.mrxiaom.pluginbase.utils.AdventureUtil;
import top.mrxiaom.pluginbase.utils.ConfigUpdater;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.pluginbase.utils.scheduler.FoliaLibScheduler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;

public class CraftItem extends BukkitPlugin {
    private static InventoryFactory inventoryFactory;
    private ConfigMain config;
    private boolean enableConfigUpdater = false;
    private IRunTask timer;
    private String langUtilsLanguage;

    public CraftItem() {
        super(new OptionsBuilder()
                .bungee(true)
                .adventure(true)
                .database(true)
                .reconnectDatabaseWhenReloadConfig(false)
                .economy(EnumEconomy.VAULT)
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

    public boolean isEnableConfigUpdater() {
        return enableConfigUpdater;
    }

    public IEconomy economy() {
        return options.economy();
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
        try {
            if (PaperInventoryFactory.test()) {
                inventoryFactory = new PaperInventoryFactory();
            } else {
                inventoryFactory = new BukkitInventoryFactory();
            }
        } catch (Throwable ignored) {
            inventoryFactory = new BukkitInventoryFactory();
        }
        saveDefaultConfig();
        ConfigurationSerialization.registerClass(CraftData.class);
        ActionProviders.registerActionProvider(ActionBack.PROVIDER);
        LanguageManager.inst()
                .setLangFile("messages.yml")
                .register(Message.class, Message::holder)
                .setProcessor((holder, key, value) -> {
                    if (value instanceof ItemStack) {
                        return ItemTranslation.get((ItemStack) value, langUtilsLanguage);
                    }
                    return value;
                });
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
        ConsoleCommandSender sender = Bukkit.getConsoleSender();
        String version = getDescription().getVersion();
        AdventureUtil.sendMessage(sender, Message.prefix + "<green>插件成功启用 <yellow>v" + version);
        AdventureUtil.sendMessage(sender, Message.prefix + "<white>由 MrXiaoM 重置! 原作者为 ZhiBuMiao.");
        enableConfigUpdater = ConfigUpdater.supportComments;
        if (!enableConfigUpdater) {
            // 不支持注释的时候，通过 last-version 文件检查插件是否已更新。
            // 如果更新了，提醒用户应该手动更新配置文件。
            File file = resolve("./last-version");
            boolean updated = true;
            if (file.exists()) {
                try (FileInputStream fis = new FileInputStream(file);
                     InputStreamReader reader = new InputStreamReader(fis, StandardCharsets.UTF_8)) {
                    int len; char[] buffer = new char[1024];
                    StringBuilder sb = new StringBuilder();
                    while ((len = reader.read(buffer)) != -1) {
                        sb.append(buffer, 0, len);
                    }
                    String last = sb.toString().trim().replace("\r", "").replace("\n", "");
                    if (!last.equals(version)) {
                        updated = true;
                    } else {
                        updated = false;
                    }
                } catch (Throwable ignored) {
                }
            }
            if (updated) {
                try (FileOutputStream fos = new FileOutputStream(file);
                     OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
                    writer.write(version);
                } catch (Throwable ignored) {
                }
                AdventureUtil.sendMessage(sender, Message.prefix + "<yellow>你的服务端版本不支持设置配置文件注释，配置更新器已禁用。更新插件后，请手动更新不存在的配置项。");
                AdventureUtil.sendMessage(sender, Message.prefix + "<yellow><u>https://github.com/MrXiaoM/CraftItem/tree/main/src/main/resources");
            }
        }
    }

    @Override
    public void beforeDisable() {
        if (timer != null) timer.cancel();
        ConfigurationSerialization.unregisterClass(CraftData.class);
        HandlerList.unregisterAll(this);
        ConsoleCommandSender sender = Bukkit.getConsoleSender();
        AdventureUtil.sendMessage(sender, Message.prefix + "<green>插件成功卸载 <yellow>v" + getDescription().getVersion());
    }

    public void saveDefaultConfig() {
        if (!resolve("./config.yml").exists()) {
            saveResource("config.yml");
        }
        Util.mkdirs(resolve("./PlayerData"));
        for (String filename : new String[] {
                "Material.yml",
                "Gui.yml",
                "Category.yml",
        }) {
            if (!resolve("./" + filename).exists()) {
                saveResource(filename);
            }
        }
    }

    public ConfigMain config() {
        return config;
    }

    @Override
    protected void beforeReloadConfig(FileConfiguration config) {
        this.config = ConfigMain.inst();
        this.langUtilsLanguage = config.getString("LangUtils.Language", "zh_cn");
    }
}
