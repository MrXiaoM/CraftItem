package cn.jrmcdp.craftitem.holder;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.Utils;
import cn.jrmcdp.craftitem.config.Config;
import cn.jrmcdp.craftitem.config.Gui;
import cn.jrmcdp.craftitem.config.Message;
import cn.jrmcdp.craftitem.data.CraftData;
import cn.jrmcdp.craftitem.data.PlayerData;
import cn.jrmcdp.craftitem.event.CraftFailEvent;
import cn.jrmcdp.craftitem.event.CraftSuccessEvent;
import cn.jrmcdp.craftitem.minigames.GameData;
import cn.jrmcdp.craftitem.minigames.utils.effect.FishingEffect;
import me.clip.placeholderapi.PlaceholderAPI;
import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public class ForgeHolder implements InventoryHolder {
    private final PlayerData playerData;
    private final String id;
    private final CraftData craftData;
    private Inventory inventory;

    ForgeHolder(PlayerData playerData, String id, CraftData craftData) {
        this.playerData = playerData;
        this.id = id;
        this.craftData = craftData;
    }

    public static Inventory buildGui(PlayerData playerData, String id, CraftData craftData, int size, String title) {
        ForgeHolder holder = new ForgeHolder(playerData, id, craftData);
        Inventory inventory = Bukkit.createInventory(holder, size, title);
        holder.inventory = inventory;
        return inventory;
    }

    public PlayerData getPlayerData() {
        return this.playerData;
    }

    public String getId() {
        return this.id;
    }

    public CraftData getCraftData() {
        return this.craftData;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void onClick(InventoryClickEvent event) {
        final PlayerData playerData = getPlayerData();
        final Player player = playerData.getPlayer();
        player.playSound(player.getLocation(), Config.getSoundClickInventory(), 1.0F, 2.0F);
        if (!event.getClick().isRightClick() && !event.getClick().isLeftClick())
            return;
        if (event.getRawSlot() < 0 || event.getRawSlot() >= (Gui.getChest()).length)
            return;
        String key = Gui.getChest()[event.getRawSlot()];
        if ("锻".equals(key)) {
            final CraftData craftData = getCraftData();
            int cost = craftData.getCost();
            if (!CraftItem.getEcon().has(player, cost)) {
                Message.craft__not_enough_money.msg(player);
                return;
            }
            CraftItem.getEcon().withdrawPlayer(player, cost);
            if (!craftData.hasMaterial(player.getInventory())) {
                Message.craft__not_enough_material.msg(player);
                return;
            }
            final boolean win = (RandomUtils.nextInt(100) + 1 <= craftData.getChance());
            final int multiple = RandomUtils.nextInt(3);
            player.closeInventory();
            if (craftData.isDifficult()) {
                CraftItem.getMiniGames().startGame(
                        new GameData(this, player, win, multiple),
                        player,
                        Config.getRandomGame(),
                        new FishingEffect()
                );
                return;
            }
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

                final BukkitTask task = (new BukkitRunnable() {
                    int i = 1;
                    // 因为事件执行可能会阻塞，加个完成标志避免定时器重复执行
                    boolean doneFlag = false;
                    public void run() {
                        if (doneFlag) return;
                        if (this.i >= 3) {
                            doneFlag = true;
                            if (doForgeResult(player, win, multiple, this::cancel)) {
                                clear();
                            }
                            return;
                        }
                        Config.getForgeTitle().send(player);
                        player.playSound(player.getLocation(), Config.getSoundForgeTitle(), 1.0F, 0.8F);
                        this.i++;
                    }
                }).runTaskTimer(CraftItem.getPlugin(), 5L, 15L);
            }, CraftItem.getPlugin());
        }
    }

    public boolean doForgeResult(Player player, boolean win, int multiple, Runnable cancel) {
        if (!craftData.hasMaterial(player.getInventory())) {
            Message.craft__not_enough_material.msg(player);
            cancel.run();
            return false;
        }

        int score = craftData.getMultiple().get(multiple);
        int oldValue = playerData.getScore(getId());
        if (win) {
            player.playSound(player.getLocation(), Config.getSoundForgeSuccess(), 1.0F, 1.0F);
            int value = playerData.addScore(getId(), score);
            CraftSuccessEvent e = new CraftSuccessEvent(player, ForgeHolder.this, oldValue, value, multiple);
            Bukkit.getPluginManager().callEvent(e);
            if (value != e.getNewValue()) {
                value = playerData.setScore(getId(), e.getNewValue());
                score = value - oldValue;
            }
            if (value == 100) {
                craftData.takeAllMaterial(player.getInventory());
                playerData.clearScore(getId());
                Message.craft__success.msg(player, Utils.getItemName(craftData.getDisplayItem()));
                for (ItemStack item : craftData.getItems()) {
                    for (ItemStack add : player.getInventory().addItem(new ItemStack[] { item }).values()) {
                        player.getWorld().dropItem(player.getLocation(), add);
                        Message.full_inventory.msg(player, Utils.getItemName(add), add.getAmount());
                    }
                }
                for (String str : craftData.getCommands()) {
                    String cmd = str.split("\\|\\|")[0];
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderAPI.setPlaceholders(player, cmd));
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
            int value = playerData.addScore(getId(), -score);
            CraftFailEvent e = new CraftFailEvent(player, ForgeHolder.this, oldValue, value, multiple);
            Bukkit.getPluginManager().callEvent(e);
            if (value != e.getNewValue()) {
                value = playerData.setScore(getId(), e.getNewValue());
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
                        Message.craft__process_fail_lost_item.msg(player, itemStack.getAmount(), Utils.getItemName(itemStack));
                    }
                    break;
                }
            }
        }
        playerData.save();
        Bukkit.getScheduler().runTaskLater(CraftItem.getPlugin(), () -> {
            if (!player.isOnline()) {
                cancel.run();
                return;
            }
            Gui.openGui(playerData, getId(), craftData);
        }, 10);
        return true;
    }
}
