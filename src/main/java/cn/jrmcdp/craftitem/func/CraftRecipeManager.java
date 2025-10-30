package cn.jrmcdp.craftitem.func;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.config.ConfigForgeGui;
import cn.jrmcdp.craftitem.config.Message;
import cn.jrmcdp.craftitem.data.CraftData;
import cn.jrmcdp.craftitem.data.MaterialInstance;
import cn.jrmcdp.craftitem.data.PlayerData;
import cn.jrmcdp.craftitem.event.CraftFailEvent;
import cn.jrmcdp.craftitem.event.CraftSuccessEvent;
import cn.jrmcdp.craftitem.gui.GuiForge;
import cn.jrmcdp.craftitem.utils.ConfigUtils;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import top.mrxiaom.pluginbase.actions.ActionProviders;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.ListPair;
import top.mrxiaom.pluginbase.utils.depend.PAPI;
import top.mrxiaom.pluginbase.utils.Pair;

import java.io.File;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

import static cn.jrmcdp.craftitem.utils.Utils.replace;
import static top.mrxiaom.pluginbase.actions.ActionProviders.loadActions;

@AutoRegister
public class CraftRecipeManager extends AbstractModule {
    private YamlConfiguration craftConfig;
    private final Map<String, CraftData> craftDataMap = new HashMap<>();
    private List<IAction> craftSuccessCommands;
    private List<IAction> craftFailCommands;
    private List<IAction> craftDoneCommands;
    private List<IAction> craftReopenGuiCommands;
    private File craftConfigFile;
    private boolean inCurrentServer;
    private boolean requirePermission;

    public CraftRecipeManager(CraftItem plugin) {
        super(plugin);
    }

    public boolean isInCurrentServer() {
        return inCurrentServer;
    }

    public boolean isRequirePermission() {
        return requirePermission;
    }

    @Override
    public void reloadConfig(MemoryConfiguration pluginConfig) {
        String configPath = pluginConfig.getString("Setting.CraftRecipeFile", "./Craft.yml");
        inCurrentServer = configPath.startsWith("./");
        craftConfigFile = plugin.resolve(configPath);
        if (!craftConfigFile.exists()) {
            plugin.saveResource("Craft.yml", craftConfigFile);
        }
        craftConfig = new YamlConfiguration();
        craftConfig.options().pathSeparator(' ');
        ConfigUtils.load(craftConfig, craftConfigFile);
        craftDataMap.clear();
        for (String key : craftConfig.getKeys(false)) {
            Object object = craftConfig.get(key, null);
            if (object instanceof CraftData) {
                craftDataMap.put(key, (CraftData) object);
            } else {
                Logger logger = CraftItem.getPlugin().getLogger();
                logger.warning("无法读取 Craft.yml 的项目 " + key + ": " + object);
            }
        }
        info("加载了 " + craftDataMap.size() + " 个锻造配方");
        craftSuccessCommands = loadActions(pluginConfig, "Events.ForgeSuccess");
        craftFailCommands = loadActions(pluginConfig, "Events.ForgeFail");
        craftDoneCommands = loadActions(pluginConfig, "Events.ForgeDone");
        craftReopenGuiCommands = loadActions(pluginConfig, "Events.ReopenGui");
        requirePermission = pluginConfig.getBoolean("Setting.RequirePermission", true);
    }

    public Map<String, CraftData> getCraftDataMap() {
        return craftDataMap;
    }

    public CraftData getCraftData(String key) {
        return craftDataMap.get(key);
    }

    public String getPermission(String craftKey) {
        return "craftitem.open." + craftKey;
    }

    public boolean hasPermission(Permissible p, String craftKey) {
        return !requirePermission || p.hasPermission(getPermission(craftKey));
    }

    public List<String> getCraftKeys(Permissible p) {
        return filter(p, getCraftDataMap().keySet());
    }

    public List<String> filter(Permissible p, Collection<String> craftKeys) {
        if (requirePermission) {
            List<String> list = new ArrayList<>();
            for (String s : craftKeys) {
                if (hasPermission(p, s)) {
                    list.add(s);
                }
            }
            return list;
        }
        return Lists.newArrayList(craftKeys);
    }

    public void save(String id, CraftData craftData) {
        craftDataMap.put(id, craftData);
        craftConfig.set(id, craftData);
        ConfigUtils.save(craftConfig, craftConfigFile);
    }

    public void delete(String id) {
        craftDataMap.remove(id);
        craftConfig.set(id, null);
        ConfigUtils.save(craftConfig, craftConfigFile);
    }

    public void doReopenForgeGui(GuiForge holder) {
        if (craftReopenGuiCommands.isEmpty()) {
            holder.parent.openGui(holder.getPlayerData(), holder.getId(), holder.getCraftData(), holder.getCategory());
        } else {
            ActionProviders.run(plugin, holder.getPlayer(), craftReopenGuiCommands);
        }
    }

    public boolean doForgeResult(GuiForge holder, Player player, boolean win, int multiple, Runnable cancel) {
        String id = holder.getId();
        CraftData craftData = holder.getCraftData();
        PlayerData playerData = holder.getPlayerData();
        if (craftData.isNotEnoughMaterial(player)) {
            cancel.run();
            return false;
        }

        if (!win) {
            int failTimes = playerData.addFailTimes(id, 1);
            if (craftData.getGuaranteeFailTimes() > 0){
                if (failTimes > craftData.getGuaranteeFailTimes()) {
                    win = true;
                    multiple = 0;
                }
            }
        } else {
            playerData.setFailTimes(id, 0);
        }

        int score = craftData.getMultiple().get(multiple);
        int oldValue = playerData.getScore(id);
        if (win) {
            plugin.config().getSoundForgeSuccess().play(player);
            int value = playerData.addScore(id, score);
            CraftSuccessEvent e = new CraftSuccessEvent(player, holder, oldValue, value, multiple);
            Bukkit.getPluginManager().callEvent(e);
            if (value != e.getNewValue()) {
                value = playerData.setScore(id, e.getNewValue());
                score = value - oldValue;
            }
            if (!craftSuccessCommands.isEmpty()) {
                ListPair<String, Object> r = new ListPair<>();
                r.add("%craft%", holder.getId());
                r.add("%modifier%", e.getMultiple() - 1);
                r.add("%progress%", value);
                r.add("%value%", score);
                ActionProviders.run(plugin, player, craftSuccessCommands, r);
            }
            if (value == 100) {
                craftData.takeAllMaterial(player);
                String failTimes = String.valueOf(playerData.getFailTimes(id));
                playerData.clearScore(id);
                playerData.clearFailTimes(id);
                playerData.addForgeCount(id, 1);
                Message.craft__success.tm(player, craftData.getDisplayItem());
                for (ItemStack item : craftData.getItems()) {
                    for (ItemStack add : player.getInventory().addItem(new ItemStack[] { item }).values()) {
                        player.getWorld().dropItem(player.getLocation(), add);
                        Message.full_inventory.tm(player, add, add.getAmount());
                    }
                }
                if (!craftDoneCommands.isEmpty()) {
                    ListPair<String, Object> r = new ListPair<>();
                    r.add("%craft%", holder.getId());
                    ActionProviders.run(plugin, player, craftDoneCommands, r);
                }
                for (String str : craftData.getCommands()) {
                    String cmd = str.split("\\|\\|")[0].replace("%fail_times%", failTimes);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PAPI.setPlaceholders(player, cmd));
                }
            } else {
                switch (e.getMultiple()) {
                    case 0 : {
                        Message.craft__process_success_small.tm(player, score);
                        break;
                    }
                    case 1 : {
                        Message.craft__process_success_medium.tm(player, score);
                        break;
                    }
                    case 2 : {
                        Message.craft__process_success_big.tm(player, score);
                        break;
                    }
                }
            }
        } else {
            plugin.config().getSoundForgeFail().play(player);
            int value = playerData.addScore(id, -score);
            CraftFailEvent e = new CraftFailEvent(player, holder, oldValue, value, multiple);
            Bukkit.getPluginManager().callEvent(e);
            if (value != e.getNewValue()) {
                value = playerData.setScore(id, e.getNewValue());
                score = value - oldValue;
            }
            if (!craftFailCommands.isEmpty()) {
                ListPair<String, Object> r = new ListPair<>();
                r.add("%craft%", holder.getId());
                r.add("%modifier%", e.getMultiple() - 1);
                r.add("%progress%", value);
                r.add("%value%", score);
                ActionProviders.run(plugin, player, craftFailCommands, r);
            }
            switch (e.getMultiple()) {
                case 0 : {
                    Message.craft__process_fail_small.tm(player, score);
                    break;
                }
                case 1 : {
                    Message.craft__process_fail_medium.tm(player, score);
                    break;
                }
                case 2 : {
                    Message.craft__process_fail_big.tm(player, score);
                    MaterialInstance instance = craftData.takeRandomMaterial(player);
                    if (instance != null) {
                        Message.craft__process_fail_lost_item.tm(player, instance.getAmount(), instance.getSample());
                    }
                    break;
                }
            }
        }
        playerData.save();
        Bukkit.getScheduler().runTaskLater(CraftItem.getPlugin(), () -> {
            if (!player.isOnline()) {
                if (cancel != null) cancel.run();
                return;
            }
            doReopenForgeGui(holder);
        }, 10);
        return true;
    }

    public void playForgeAnimate(Player player, BiConsumer<Runnable, Runnable> consumer) {
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onPlayerQuit(PlayerQuitEvent eventB) {
                if (eventB.getPlayer().equals(player))
                    clear();
            }

            public void clear() {
                task.cancel();
                HandlerList.unregisterAll(this);
            }

            final BukkitTask task;
            {
                Runnable clear = this::clear;
                task = (new BukkitRunnable() {
                    int counter = 1;
                    // 因为事件执行可能会阻塞，加个完成标志避免定时器重复执行
                    boolean doneFlag = false;
                    public void run() {
                        if (doneFlag) return;
                        if (this.counter >= 3) {
                            doneFlag = true;
                            consumer.accept(clear, this::cancel);
                            return;
                        }
                        plugin.config().sendForgeTitle(player);
                        plugin.config().getSoundForgeTitle().play(player);
                        this.counter++;
                    }
                }).runTaskTimer(CraftItem.getPlugin(), 5L, 15L);
            }
        }, CraftItem.getPlugin());
    }

    public static CraftRecipeManager inst() {
        return instanceOf(CraftRecipeManager.class);
    }
}
