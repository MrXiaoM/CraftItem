package cn.jrmcdp.craftitem;

import cn.jrmcdp.craftitem.actions.ActionBack;
import cn.jrmcdp.craftitem.actions.ActionReopen;
import cn.jrmcdp.craftitem.config.ConfigMain;
import cn.jrmcdp.craftitem.config.ItemTranslation;
import cn.jrmcdp.craftitem.config.Message;
import cn.jrmcdp.craftitem.currency.*;
import cn.jrmcdp.craftitem.data.CraftData;
import cn.jrmcdp.craftitem.depend.mythic.IMythic;
import cn.jrmcdp.craftitem.depend.mythic.Mythic4;
import cn.jrmcdp.craftitem.depend.mythic.Mythic5;
import cn.jrmcdp.craftitem.gui.IHolder;
import cn.jrmcdp.craftitem.minigames.GameManager;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.actions.ActionProviders;
import top.mrxiaom.pluginbase.api.IRunTask;
import top.mrxiaom.pluginbase.economy.IEconomy;
import top.mrxiaom.pluginbase.func.GuiManager;
import top.mrxiaom.pluginbase.func.LanguageManager;
import top.mrxiaom.pluginbase.gui.IGuiHolder;
import top.mrxiaom.pluginbase.paper.PaperFactory;
import top.mrxiaom.pluginbase.resolver.DefaultLibraryResolver;
import top.mrxiaom.pluginbase.resolver.utils.ClassLoaderWrapper;
import top.mrxiaom.pluginbase.utils.AdventureUtil;
import top.mrxiaom.pluginbase.utils.ConfigUpdater;
import top.mrxiaom.pluginbase.utils.ConfigUtils;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.pluginbase.utils.inventory.InventoryFactory;
import top.mrxiaom.pluginbase.utils.item.ItemEditor;
import top.mrxiaom.pluginbase.utils.scheduler.FoliaLibScheduler;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CraftItem extends BukkitPlugin {
    private ConfigMain config;
    private boolean enableConfigUpdater = false;
    private IRunTask timer;
    private String langUtilsLanguage;
    private IMythic mythic;
    private final List<ICurrencyProvider> currencyRegistry = new ArrayList<>();
    public CraftItem() throws Exception {
        super(new OptionsBuilder()
                .bungee(true)
                .adventure(true)
                .database(true)
                .reconnectDatabaseWhenReloadConfig(false)
                .scanIgnore("cn.jrmcdp.craftitem.libs"));
        scheduler = new FoliaLibScheduler(this);

        info("正在检查依赖库状态");
        File librariesDir = ClassLoaderWrapper.isSupportLibraryLoader
                ? new File("libraries") // 防止出现依赖版本不同的问题
                : new File(this.getDataFolder(), "libraries");
        DefaultLibraryResolver resolver = new DefaultLibraryResolver(getLogger(), librariesDir);

        YamlConfiguration overrideLibraries = ConfigUtils.load(resolve("./.override-libraries.yml"));
        for (String key : overrideLibraries.getKeys(false)) {
            resolver.getStartsReplacer().put(key, overrideLibraries.getString(key));
        }
        resolver.addResolvedLibrary(BuildConstants.RESOLVED_LIBRARIES);

        List<URL> libraries = resolver.doResolve();
        info("正在添加 " + libraries.size() + " 个依赖库到类加载器");
        for (URL library : libraries) {
            classLoader.addURL(library);
        }
    }

    @Override
    public @NotNull ItemEditor initItemEditor() {
        return PaperFactory.createItemEditor();
    }

    @Override
    public @NotNull InventoryFactory initInventoryFactory() {
        return PaperFactory.createInventoryFactory();
    }

    public static CraftItem getPlugin() {
        return (CraftItem) getInstance();
    }

    public static GameManager getMiniGames() {
        return GameManager.inst();
    }

    /**
     * @see CraftItem#createInventory(InventoryHolder, int, String)
     */
    @Deprecated
    public static InventoryFactory getInventoryFactory() {
        return getPlugin().inventory;
    }

    public boolean isEnableConfigUpdater() {
        return enableConfigUpdater;
    }

    @Deprecated
    public IEconomy economy() {
        return parseCurrency("Vault");
    }

    public void registerCurrency(@NotNull ICurrencyProvider provider) {
        currencyRegistry.add(provider);
        currencyRegistry.sort(Comparator.comparingInt(ICurrencyProvider::priority));
    }

    public void unregisterCurrency(@NotNull ICurrencyProvider provider) {
        currencyRegistry.remove(provider);
        currencyRegistry.sort(Comparator.comparingInt(ICurrencyProvider::priority));
    }

    @Nullable
    public ICurrency parseCurrency(String str) {
        for (ICurrencyProvider provider : currencyRegistry) {
            ICurrency currency = provider.parse(str);
            if (currency != null) {
                return currency;
            }
        }
        return null;
    }

    public IMythic getMythic() {
        return mythic;
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
        if (Util.isPresent("io.lumine.mythic.bukkit.MythicBukkit")) {
            mythic = new Mythic5();
        }
        if (Util.isPresent("io.lumine.xikage.mythicmobs.MythicMobs")) {
            mythic = new Mythic4();
        }
        if (Util.isPresent("net.milkbowl.vault.economy.Economy")) {
            VaultCurrency.register(this);
        }
        if (Util.isPresent("org.black_ixx.playerpoints.PlayerPointsAPI")) {
            PlayerPointsCurrency.register(this);
        }
        if (Util.isPresent("me.yic.mpoints.MPointsAPI")) {
            MPointsCurrency.register(this);
        }
        if (Util.isPresent("su.nightexpress.coinsengine.api.CoinsEngineAPI")) {
            CoinsEngineCurrency.register(this);
        }
        if (Util.isPresent("com.mc9y.nyeconomy.api.NyEconomyAPI")) {
            NyEconomyCurrency.register(this);
        }

        saveDefaultConfig();
        ConfigurationSerialization.registerClass(CraftData.class);
        ActionProviders.registerActionProviders(ActionBack.PROVIDER, ActionReopen.PROVIDER);
        LanguageManager.inst()
                .setLangFile("messages.yml")
                .register(Message.class, Message::holder)
                .setProcessor((holder, key, value) -> {
                    if (value instanceof ItemStack) {
                        return ItemTranslation.get((ItemStack) value, langUtilsLanguage);
                    }
                    return value;
                });
        if (Util.isPresent("org.bukkit.event.server.ServerLoadEvent")) {
            Bukkit.getPluginManager().registerEvents(new Listener() {
                public void onServerLoad(ServerLoadEvent e) {
                    if (!e.getType().equals(ServerLoadEvent.LoadType.STARTUP)) return;
                    HandlerList.unregisterAll(this);
                    reloadConfig();
                }
            }, this);
        }
    }

    public void onSecond() {
        GuiManager manager = GuiManager.inst();
        for (Player player : Bukkit.getOnlinePlayers()) {
            IGuiHolder gui = manager.getOpeningGui(player);
            if (gui instanceof IHolder) {
                ((IHolder) gui).onSecond();
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
            boolean updated = checkUpdated(file, version);
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

    private static boolean checkUpdated(File file, String version) {
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
                updated = !last.equals(version);
            } catch (Throwable ignored) {
            }
        }
        return updated;
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
