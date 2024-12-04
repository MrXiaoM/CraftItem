package cn.jrmcdp.craftitem.config;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.config.data.Icon;
import cn.jrmcdp.craftitem.data.CraftData;
import cn.jrmcdp.craftitem.data.PlayerData;
import cn.jrmcdp.craftitem.event.CraftFailEvent;
import cn.jrmcdp.craftitem.event.CraftSuccessEvent;
import cn.jrmcdp.craftitem.holder.ForgeHolder;
import cn.jrmcdp.craftitem.utils.Pair;
import cn.jrmcdp.craftitem.utils.PlaceholderSupport;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

import static cn.jrmcdp.craftitem.utils.Pair.replace;

public class Craft {
    private static YamlConfiguration craftConfig;

    private static final Map<String, CraftData> craftDataMap = new HashMap<>();
    private static List<String> craftSuccessCommands;
    private static List<String> craftFailCommands;
    private static List<String> craftDoneCommands;

    public static void reload() {
        craftConfig = FileConfig.Craft.loadConfig();
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
        FileConfiguration pluginConfig = CraftItem.getPlugin().getConfig();
        craftSuccessCommands = pluginConfig.getStringList("Events.ForgeSuccess");
        craftFailCommands = pluginConfig.getStringList("Events.ForgeFail");
        craftDoneCommands = pluginConfig.getStringList("Events.ForgeDone");
    }

    public static Map<String, CraftData> getCraftDataMap() {
        return craftDataMap;
    }

    public static CraftData getCraftData(String key) {
        return craftDataMap.get(key);
    }

    public static void save(String id, CraftData craftData) {
        craftDataMap.put(id, craftData);
        craftConfig.set(id, craftData);
        FileConfig.Craft.saveConfig(craftConfig);
    }
    public static void delete(String id) {
        craftDataMap.remove(id);
        craftConfig.set(id, null);
        FileConfig.Craft.saveConfig(craftConfig);
    }

    public static boolean doForgeResult(ForgeHolder holder, Player player, boolean win, int multiple, Runnable cancel) {
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
            Config.playSoundForgeSuccess(player);
            int value = playerData.addScore(id, score);
            CraftSuccessEvent e = new CraftSuccessEvent(player, holder, oldValue, value, multiple);
            Bukkit.getPluginManager().callEvent(e);
            if (value != e.getNewValue()) {
                value = playerData.setScore(id, e.getNewValue());
                score = value - oldValue;
            }
            if (!craftSuccessCommands.isEmpty()) {
                List<String> list = replace(craftSuccessCommands,
                        Pair.of("%craft%", holder.getId()),
                        Pair.of("%modifier%", e.getMultiple() - 1),
                        Pair.of("%progress%", value),
                        Pair.of("%value%", score));
                Icon.runCommands(player, list);
            }
            if (value == 100) {
                craftData.takeAllMaterial(player);
                String failTimes = String.valueOf(playerData.getFailTimes(id));
                playerData.clearScore(id);
                playerData.clearFailTimes(id);
                playerData.addForgeCount(id, 1);
                Message.craft__success.msg(player, craftData.getDisplayItem());
                for (ItemStack item : craftData.getItems()) {
                    for (ItemStack add : player.getInventory().addItem(new ItemStack[] { item }).values()) {
                        player.getWorld().dropItem(player.getLocation(), add);
                        Message.full_inventory.msg(player, add, add.getAmount());
                    }
                }
                if (!craftDoneCommands.isEmpty()) {
                    List<String> list = replace(craftDoneCommands,
                            Pair.of("%craft%", holder.getId()));
                    Icon.runCommands(player, list);
                }
                for (String str : craftData.getCommands()) {
                    String cmd = str.split("\\|\\|")[0].replace("%fail_times%", failTimes);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderSupport.setPlaceholders(player, cmd));
                }
            } else {
                switch (e.getMultiple()) {
                    case 0 : {
                        Message.craft__process_success_small.msg(player, score);
                        break;
                    }
                    case 1 : {
                        Message.craft__process_success_medium.msg(player, score);
                        break;
                    }
                    case 2 : {
                        Message.craft__process_success_big.msg(player, score);
                        break;
                    }
                }
            }
        } else {
            Config.playSoundForgeFail(player);
            int value = playerData.addScore(id, -score);
            CraftFailEvent e = new CraftFailEvent(player, holder, oldValue, value, multiple);
            Bukkit.getPluginManager().callEvent(e);
            if (value != e.getNewValue()) {
                value = playerData.setScore(id, e.getNewValue());
                score = value - oldValue;
            }
            if (!craftFailCommands.isEmpty()) {
                List<String> list = replace(craftFailCommands,
                        Pair.of("%craft%", holder.getId()),
                        Pair.of("%modifier%", e.getMultiple() - 1),
                        Pair.of("%progress%", value),
                        Pair.of("%value%", score));
                Icon.runCommands(player, list);
            }
            switch (e.getMultiple()) {
                case 0 : {
                    Message.craft__process_fail_small.msg(player, score);
                    break;
                }
                case 1 : {
                    Message.craft__process_fail_medium.msg(player, score);
                    break;
                }
                case 2 : {
                    Message.craft__process_fail_big.msg(player, score);
                    ItemStack itemStack = craftData.takeRandomMaterial(player);
                    if (itemStack != null) {
                        Message.craft__process_fail_lost_item.msg(player, itemStack.getAmount(), itemStack);
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
            ForgeGui.openGui(playerData, id, craftData);
        }, 10);
        return true;
    }

    public static void playForgeAnimate(Player player, BiConsumer<Runnable, Runnable> consumer) {
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
                        Config.getForgeTitle().send(player);
                        Config.playSoundForgeTitle(player);
                        this.counter++;
                    }
                }).runTaskTimer(CraftItem.getPlugin(), 5L, 15L);
            }
        }, CraftItem.getPlugin());
    }
}
