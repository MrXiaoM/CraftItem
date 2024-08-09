package cn.jrmcdp.craftitem.holder;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.config.*;
import cn.jrmcdp.craftitem.config.data.Icon;
import cn.jrmcdp.craftitem.data.CraftData;
import cn.jrmcdp.craftitem.data.PlayerData;
import cn.jrmcdp.craftitem.minigames.GameData;
import cn.jrmcdp.craftitem.utils.Pair;
import cn.jrmcdp.craftitem.utils.PlaceholderSupport;
import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
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
    private final Set<Integer> timeSlots = new HashSet<>();
    ForgeHolder(PlayerData playerData, String id, CraftData craftData) {
        this.playerData = playerData;
        this.id = id;
        this.craftData = craftData;
        this.endTime = playerData.getEndTime(id);
        this.done = endTime != null && System.currentTimeMillis() >= endTime;
        this.processing = endTime != null && (System.currentTimeMillis() >= endTime - craftData.getTime() * 1000L);
        this.chest = processing || done ? ForgeGui.getChestTime() : ForgeGui.getChest();
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
                    icon = ForgeGui.items.get("时_完成");
                } else if (processing) {
                    icon = ForgeGui.items.get("时_进行中");
                } else {
                    icon = ForgeGui.items.get("时");
                }
            } else {
                icon = ForgeGui.items.get("时_条件不足");
            }
        } else {
            icon = ForgeGui.items.get("时_未开启");
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
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Damageable) {
            Damageable damageable = (Damageable) meta;
            damageable.setDamage((short)((1.0d - progress) * item.getType().getMaxDurability()));
        }
        if (meta != null) meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE);
        item.setItemMeta(meta);
        return item;
    }

    public void putTimeSlot(int index) {
        timeSlots.add(index);
    }

    @Override
    public void onSecond() { // 每秒更新一次 时长锻造 的图标
        if (craftData.getTime() <= 0) return;
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

        if (!event.getClick().isRightClick() && !event.getClick().isLeftClick()) return;
        if (event.getRawSlot() < 0 || event.getRawSlot() >= chest.length) return;

        String key = String.valueOf(chest[event.getRawSlot()]);
        if ("锻".equals(key)) {
            if (!event.isShiftClick()) {
                if (event.isLeftClick()) { // 普通锻造
                    clickForgeOnce(player);
                } else if (event.isRightClick()) { // 锻造连击
                    clickForgeCombo(player);
                }
            }
            return;
        }
        if ("时".equals(key)) { // 时长锻造
            if (!event.isShiftClick() && event.isLeftClick()) {
                clickForgeTime(player);
            }
            return;
        }
        // 其它图标
        Icon icon = ForgeGui.items.get(key);
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

    /**
     * 点击进行 普通锻造
     */
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
                    Config.getRandomGame()
            );
            return;
        }
        Craft.playForgeAnimate(player, (clear, cancel) -> {
            if (Craft.doForgeResult(this, player, win, multiple, cancel)) {
                clear.run();
            }
        });
    }

    /**
     * 点击进行 锻造连击
     */
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
        Craft.playForgeAnimate(player, (clear, cancel) -> {
            Bukkit.getScheduler().runTask(CraftItem.getPlugin(), () -> {
                if (craftData.isNotEnoughMaterial(player)) return;

                for (int i = 0; i < combo; i++) {
                    CraftItem.getEcon().withdrawPlayer(player, costOneTime);
                    final boolean win = (RandomUtils.nextInt(100) + 1 <= craftData.getChance());
                    final int multiple = RandomUtils.nextInt(3);
                    if (!Craft.doForgeResult(this, player, win, multiple, null)) {
                        break;
                    }
                    if (craftData.isNotEnoughMaterial(player)) break;
                }
            });
            clear.run();
        });
    }

    /**
     * 点击进行 时长锻造
     */
    private void clickForgeTime(Player player) {
        // 检查是否满足时长锻造条件
        if (!Config.isMeetTimeForgeCondition(playerData.getPlayer())) return;
        CraftData craftData = getCraftData();
        // 检查是否已开启时长锻造
        if (craftData.getTime() <= 0) return;
        String key = getId();
        if (done) { // 如果时长锻造已完成
            player.closeInventory();
            long now = System.currentTimeMillis();
            Long endTime = playerData.getEndTime(key);
            if (endTime == null || now < endTime) {
                return;
            }
            playerData.clearScore(key);
            playerData.clearFailTimes(key);
            playerData.removeTime(key);
            playerData.save(); // 锻造结束，给予奖励
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
        if (!processing) { // 如果时长锻造未开始
            int cost = craftData.getTimeCost();
            if (!CraftItem.getEcon().has(player, cost)) {
                Message.craft__not_enough_money.msg(player);
                return;
            }
            if (craftData.isNotEnoughMaterial(player)) return;

            player.closeInventory();
            CraftItem.getEcon().withdrawPlayer(player, cost);
            craftData.takeAllMaterial(player.getInventory());
            long endTime = System.currentTimeMillis() + craftData.getTime() * 1000L;
            playerData.setTime(key, endTime);
            playerData.save(); // 开始时长锻造
            Message.craft__time_start.msg(player);
        }
    }
}
