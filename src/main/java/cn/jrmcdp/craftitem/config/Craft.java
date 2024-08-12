package cn.jrmcdp.craftitem.config;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.data.CraftData;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

import cn.jrmcdp.craftitem.data.PlayerData;
import cn.jrmcdp.craftitem.event.CraftFailEvent;
import cn.jrmcdp.craftitem.event.CraftSuccessEvent;
import cn.jrmcdp.craftitem.holder.ForgeHolder;
import cn.jrmcdp.craftitem.utils.PlaceholderSupport;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Craft {
    private static YamlConfiguration config;

    private static final Map<String, CraftData> craftDataMap = new HashMap<>();

    public static void reload() {
        config = FileConfig.Craft.loadConfig();
        craftDataMap.clear();
        for (String key : config.getKeys(false)) {
            Object object = config.get(key, null);
            if (object instanceof CraftData) {
                craftDataMap.put(key, (CraftData) object);
            } else {
                Logger logger = CraftItem.getPlugin().getLogger();
                logger.warning("无法读取 Craft.yml 的项目 " + key + ": " + object);
            }
        }
    }

    public static Map<String, CraftData> getCraftDataMap() {
        return craftDataMap;
    }

    public static CraftData getCraftData(String key) {
        return craftDataMap.get(key);
    }

    public static void save(String id, CraftData craftData) {
        craftDataMap.put(id, craftData);
        config.set(id, craftData);
        FileConfig.Craft.saveConfig(config);
    }
    public static void delete(String id) {
        craftDataMap.remove(id);
        config.set(id, null);
        FileConfig.Craft.saveConfig(config);
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
            player.playSound(player.getLocation(), Config.getSoundForgeSuccess(), 1.0F, 1.0F);
            int value = playerData.addScore(id, score);
            CraftSuccessEvent e = new CraftSuccessEvent(player, holder, oldValue, value, multiple);
            Bukkit.getPluginManager().callEvent(e);
            if (value != e.getNewValue()) {
                value = playerData.setScore(id, e.getNewValue());
                score = value - oldValue;
            }
            if (value == 100) {
                craftData.takeAllMaterial(player.getInventory());
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
            player.playSound(player.getLocation(), Config.getSoundForgeFail(), 1.0F, 1.0F);
            int value = playerData.addScore(id, -score);
            CraftFailEvent e = new CraftFailEvent(player, holder, oldValue, value, multiple);
            Bukkit.getPluginManager().callEvent(e);
            if (value != e.getNewValue()) {
                value = playerData.setScore(id, e.getNewValue());
                score = value - oldValue;
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
                    ItemStack itemStack = craftData.takeRandomMaterial(player, player.getInventory());
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
                        player.playSound(player.getLocation(), Config.getSoundForgeTitle(), 1.0F, 0.8F);
                        this.counter++;
                    }
                }).runTaskTimer(CraftItem.getPlugin(), 5L, 15L);
            }
        }, CraftItem.getPlugin());
    }
}
