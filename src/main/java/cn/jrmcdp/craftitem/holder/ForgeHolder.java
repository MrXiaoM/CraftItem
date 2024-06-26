package cn.jrmcdp.craftitem.holder;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.config.Config;
import cn.jrmcdp.craftitem.config.Gui;
import cn.jrmcdp.craftitem.config.Icon;
import cn.jrmcdp.craftitem.config.Message;
import cn.jrmcdp.craftitem.data.CraftData;
import cn.jrmcdp.craftitem.data.PlayerData;
import cn.jrmcdp.craftitem.event.CraftFailEvent;
import cn.jrmcdp.craftitem.event.CraftSuccessEvent;
import cn.jrmcdp.craftitem.minigames.GameData;
import cn.jrmcdp.craftitem.minigames.utils.Pair;
import cn.jrmcdp.craftitem.minigames.utils.effect.FishingEffect;
import cn.jrmcdp.craftitem.utils.PlaceholderSupport;
import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class ForgeHolder implements IHolder {
    private final PlayerData playerData;
    private final String id;
    private final CraftData craftData;
    private Inventory inventory;
    public final Long endTime;
    public final char[] chest;
    public boolean done;
    public boolean processing;
    Set<Integer> timeSlots = new HashSet<>();
    ForgeHolder(PlayerData playerData, String id, CraftData craftData) {
        this.playerData = playerData;
        this.id = id;
        this.craftData = craftData;
        this.endTime = playerData.getEndTime(id);
        this.done = endTime != null && System.currentTimeMillis() >= endTime;
        this.processing = endTime != null && (System.currentTimeMillis() >= endTime - craftData.getTime() * 1000L);
        this.chest = processing || done ? Gui.getChestTime() : Gui.getChest();
    }

    public static Inventory buildGui(PlayerData playerData, String id, CraftData craftData, int size, String title) {
        ForgeHolder holder = new ForgeHolder(playerData, id, craftData);
        Inventory inventory = Bukkit.createInventory(holder, size, PlaceholderSupport.setPlaceholders(playerData.getPlayer(), title));
        holder.inventory = inventory;
        return inventory;
    }

    public ItemStack getTimeIcon() {
        Icon icon;
        CraftData craftData = getCraftData();
        if (craftData.getTime() > 0) {
            if (Config.isMeetTimeForgeCondition(playerData.getPlayer())) {
                if (done) {
                    icon = Gui.items.get("时_完成");
                } else if (processing) {
                    icon = Gui.items.get("时_进行中");
                } else {
                    icon = Gui.items.get("时");
                }
            } else {
                icon = Gui.items.get("时_条件不足");
            }
        } else {
            icon = Gui.items.get("时_未开启");
        }
        long startTime = endTime == null ? 0 : (endTime - (craftData.getTime() * 1000));
        double progress = endTime == null ? 0.0d : Math.min(1.0d, (System.currentTimeMillis() - startTime) / (craftData.getTime() * 1000.0d));
        String remainTime = endTime == null ? "" : CraftData.getTimeDisplay(Math.max(0, (endTime - System.currentTimeMillis()) / 1000L), "0秒");
        ItemStack item = icon.getItem(
                playerData.getPlayer(),
                Pair.of("<Progress>", String.format("%.2f%%", progress * 100)),
                Pair.of("<RemainTime>", remainTime),
                Pair.of("<Time>", craftData.getTimeDisplay()),
                Pair.of("<Cost>", craftData.getTimeCost())
        );
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta instanceof Damageable) {
            Damageable damageable = (Damageable) itemMeta;
            damageable.setDamage((short)((1.0d - progress) * item.getType().getMaxDurability()));
        }
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE);
        item.setItemMeta(itemMeta);
        return item;
    }

    public void putTimeSlot(int i) {
        timeSlots.add(i);
    }

    @Override
    public void onSecond() {
        this.done = endTime != null && System.currentTimeMillis() >= endTime;
        this.processing = endTime != null && (System.currentTimeMillis() >= endTime - getCraftData().getTime() * 1000L);
        if (processing || done) {
            ItemStack item = getTimeIcon();
            for (int slot : timeSlots) {
                getInventory().setItem(slot, item);
            }
        }
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

    @NotNull
    public Inventory getInventory() {
        return inventory;
    }

    public void onClick(InventoryClickEvent event) {
        final PlayerData playerData = getPlayerData();
        final Player player = playerData.getPlayer();
        player.playSound(player.getLocation(), Config.getSoundClickInventory(), 1.0F, 2.0F);
        if (!event.getClick().isRightClick() && !event.getClick().isLeftClick())
            return;
        if (event.getRawSlot() < 0 || event.getRawSlot() >= chest.length)
            return;
        String key = String.valueOf(chest[event.getRawSlot()]);
        if ("锻".equals(key)) {
            if (!event.isShiftClick()) {
                if (event.isLeftClick()) {
                    clickForgeOnce(player);
                } else if (event.isRightClick()) {
                    clickForgeCombo(player);
                }
            }
            return;
        }
        if ("时".equals(key)) {
            if (!Config.isMeetTimeForgeCondition(playerData.getPlayer())) return;
            CraftData craftData = getCraftData();
            if (craftData.getTime() <= 0) return;
            if (done) {
                long now = System.currentTimeMillis();
                Long endTime = playerData.getEndTime(getId());
                if (endTime == null || now < endTime) {
                    player.closeInventory();
                    return;
                }
                player.closeInventory();
                playerData.clearScore(getId());
                playerData.clearFailTimes(getId());
                playerData.removeTime(getId());
                playerData.save();
                Message.craft__success.msg(player, craftData.getDisplayItem());
                for (ItemStack item : craftData.getItems()) {
                    for (ItemStack add : player.getInventory().addItem(new ItemStack[] { item }).values()) {
                        player.getWorld().dropItem(player.getLocation(), add);
                        Message.full_inventory.msg(player, add, add.getAmount());
                    }
                }
                for (String str : craftData.getCommands()) {
                    String cmd = str.split("\\|\\|")[0];
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderSupport.setPlaceholders(player, cmd));
                }
                return;
            }
            if (!processing) {
                int cost = craftData.getTimeCost();
                if (!CraftItem.getEcon().has(player, cost)) {
                    Message.craft__not_enough_money.msg(player);
                    return;
                }
                if (craftData.isNotEnoughMaterial(player)) return;

                player.closeInventory();
                CraftItem.getEcon().withdrawPlayer(player, cost);
                craftData.takeAllMaterial(player.getInventory());
                playerData.setTime(getId(), System.currentTimeMillis() + craftData.getTime() * 1000L);
                playerData.save();
                Message.craft__time_start.msg(player);
            }
            return;
        }
        Icon icon = Gui.items.get(key);
        if (icon == null) return;
        if (!event.isShiftClick()) {
            if (event.isLeftClick()) {
                icon.leftClick(player);
            }
            else if (event.isRightClick()) {
                icon.rightClick(player);
            }
        } else {
            if (event.isLeftClick()) {
                icon.shiftLeftClick(player);
            }
            else if (event.isRightClick()) {
                icon.shiftRightClick(player);
            }
        }
    }

    private void clickForgeOnce(Player player) {
        CraftData craftData = getCraftData();
        if (craftData.isDifficult() && CraftItem.getMiniGames() == null) {
            Message.no_protocollib.msg(player);
            return;
        }
        int cost = craftData.getCost();
        if (!CraftItem.getEcon().has(player, cost)) {
            Message.craft__not_enough_money.msg(player);
            return;
        }
        if (craftData.isNotEnoughMaterial(player)) return;

        CraftItem.getEcon().withdrawPlayer(player, cost);
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

    private void clickForgeCombo(Player player) {
        CraftData craftData = getCraftData();
        int combo = craftData.getCombo();
        if (craftData.isDifficult() || combo <= 0) return;

        int costOneTime = craftData.getCost();
        if (!CraftItem.getEcon().has(player, costOneTime * combo)) {
            Message.craft__not_enough_money.msg(player);
            return;
        }
        if (craftData.isNotEnoughMaterial(player)) return;

        player.closeInventory();

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
                        Bukkit.getScheduler().runTask(CraftItem.getPlugin(), () -> doCombo(player));
                        clear();
                        return;
                    }
                    Config.getForgeTitle().send(player);
                    player.playSound(player.getLocation(), Config.getSoundForgeTitle(), 1.0F, 0.8F);
                    this.i++;
                }
            }).runTaskTimer(CraftItem.getPlugin(), 5L, 15L);
        }, CraftItem.getPlugin());
    }

    private void doCombo(Player player) {
        CraftData craftData = getCraftData();
        if (craftData.isNotEnoughMaterial(player)) return;

        int combo = craftData.getCombo();
        int costOneTime = craftData.getCost();
        for (int i = 0; i < combo; i++) {
            CraftItem.getEcon().withdrawPlayer(player, costOneTime);
            final boolean win = (RandomUtils.nextInt(100) + 1 <= craftData.getChance());
            final int multiple = RandomUtils.nextInt(3);
            if (!doForgeResult(player, win, multiple, null)) {
                break;
            }
            if (craftData.isNotEnoughMaterial(player)) break;
        }
    }

    public boolean doForgeResult(Player player, boolean win, int multiple, Runnable cancel) {
        CraftData craftData = getCraftData();
        if (craftData.isNotEnoughMaterial(player)) {
            cancel.run();
            return false;
        }

        if (!win) {
            int failTimes = playerData.addFailTimes(getId(), 1);
            if (craftData.getGuaranteeFailTimes() > 0){
                if (failTimes > craftData.getGuaranteeFailTimes()) {
                    win = true;
                    multiple = 0;
                }
            }
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
                String failTimes = String.valueOf(playerData.getFailTimes(getId()));
                playerData.clearScore(getId());
                playerData.clearFailTimes(getId());
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
            Gui.openGui(playerData, getId(), craftData);
        }, 10);
        return true;
    }
}
