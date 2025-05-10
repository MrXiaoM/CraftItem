package cn.jrmcdp.craftitem.gui;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.manager.CraftDataManager;
import cn.jrmcdp.craftitem.config.ConfigForgeGui;
import cn.jrmcdp.craftitem.config.Message;
import cn.jrmcdp.craftitem.config.data.Icon;
import cn.jrmcdp.craftitem.data.CraftData;
import cn.jrmcdp.craftitem.data.PlayerData;
import cn.jrmcdp.craftitem.minigames.GameData;
import cn.jrmcdp.craftitem.utils.Utils;
import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.AdventureItemStack;
import top.mrxiaom.pluginbase.utils.PAPI;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.*;

public class GuiForge implements IHolder {
    private final PlayerData playerData;
    private final String id;
    private final CraftData craftData;
    private final String category;
    private final String title;
    private final Map<String, Icon> items;

    private Inventory inventory;
    public final Long endTime;
    public final char[] chest;
    public boolean done;
    public boolean processing;
    private final Set<Integer> timeSlots = new HashSet<>();
    public final CraftDataManager manager = CraftDataManager.inst();
    public final ConfigForgeGui parent;
    public GuiForge(ConfigForgeGui parent, String title, @Nullable String category, Map<String, Icon> items, PlayerData playerData, String id, CraftData craftData) {
        this.parent = parent;
        this.title = title;
        this.category = category;
        this.items = items;
        this.playerData = playerData;
        this.id = id;
        this.craftData = craftData;
        this.endTime = playerData.getEndTime(id);
        this.done = endTime != null && System.currentTimeMillis() >= endTime;
        this.processing = endTime != null && (System.currentTimeMillis() >= endTime - craftData.getTime() * 1000L);
        this.chest = processing || done ? parent.getChestTime() : parent.getChest();
    }

    public @Nullable String getCategory() {
        return category;
    }

    public ItemStack getTimeIcon() {
        Icon icon;
        CraftData craftData = getCraftData();
        if (craftData.getTime() > 0) {
            if (parent.plugin.config().isMeetTimeForgeCondition(playerData.getPlayer())) {
                if (done) {
                    icon = parent.getIcon("时_完成");
                } else if (processing) {
                    icon = parent.getIcon("时_进行中");
                } else {
                    icon = parent.getIcon("时");
                }
            } else {
                icon = parent.getIcon("时_条件不足");
            }
        } else {
            icon = parent.getIcon("时_未开启");
        }
        long startTime = endTime == null ? 0 : (endTime - (craftData.getTime() * 1000));
        double progress = endTime == null ? 0.0d : Math.min(1.0d, (System.currentTimeMillis() - startTime) / (craftData.getTime() * 1000.0d));
        String remainTime = endTime == null ? "" : parent.plugin.config().getTimeDisplay(Math.max(0, (endTime - System.currentTimeMillis()) / 1000L), "0秒");

        int count = playerData.getTimeForgeCount(getId());
        int limit = craftData.getTimeForgeCountLimit(playerData.getPlayer());

        ItemStack item = icon.getItem(
                playerData.getPlayer(),
                Pair.of("<Progress>", String.format("%.2f%%", progress * 100)),
                Pair.of("<RemainTime>", remainTime),
                Pair.of("<Time>", craftData.getTimeDisplay()),
                Pair.of("<Cost>", craftData.getTimeCost()),
                Pair.of("<LimitCountCurrent>", count),
                Pair.of("<LimitCountMax>", limit != 0 ? Math.max(limit, 0) : Message.craft__unlimited.str()),
                Pair.of("<LimitCount>", limit != 0 ? Message.craft__limited.str(count, limit) : Message.craft__unlimited.str())
        );

        if (item.getType().getMaxDurability() > 1) {
            short damage = (short) ((1.0d - progress) * item.getType().getMaxDurability());
            item.setDurability(damage); // 不用 Damageable，兼容 1.12.2 或以下
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            Utils.valueOf(ItemFlag.class, "HIDE_DYE").ifPresent(meta::addItemFlags);
            Utils.valueOf(ItemFlag.class, "HIDE_ATTRIBUTES").ifPresent(meta::addItemFlags);
            item.setItemMeta(meta);
        }
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

    @Override
    public Player getPlayer() {
        return playerData.getPlayer();
    }

    @Override
    public Inventory newInventory() {
        inventory = CraftItem.getInventoryFactory().create(this, chest.length, PAPI.setPlaceholders(playerData.getPlayer(), title));
        Player player = getPlayer();
        ItemStack[] is = new ItemStack[chest.length];
        Iterator<ItemStack> iterator = craftData.getMaterial().iterator();
        for (int i = 0; i < chest.length; i++) {
            String key = String.valueOf(chest[i]);
            switch (key) {
                case "材": {
                    if (iterator.hasNext()) {
                        is[i] = iterator.next();
                        break;
                    }
                    Icon icon = items.get(key);
                    if (icon != null) {
                        is[i] = icon.getItem(player);
                    }
                    break;
                }
                case "物": {
                    ItemStack item = craftData.getDisplayItem().clone();
                    ItemMeta meta = item.getItemMeta();
                    List<String> lore = meta == null ? null : meta.getLore();
                    if (lore == null) lore = new ArrayList<>();
                    lore.addAll(Message.gui__craft_info__lore__header.list());
                    for (ItemStack itemStack : craftData.getItems())
                        lore.add(Message.gui__craft_info__lore__item.str(Utils.getItemName(itemStack), itemStack.getAmount()));
                    for (String command : craftData.getCommands()) {
                        String[] split = command.split("\\|\\|");
                        if (split.length > 1) lore.add(Message.gui__craft_info__lore__command.str(split[1]));
                    }
                    AdventureItemStack.setItemLoreMiniMessage(item, lore);
                    is[i] = item;
                    break;
                }
                case "锻": {
                    Icon icon = items.get(craftData.isDifficult()
                            ? "锻_困难"
                            : (craftData.getCombo() > 0 ? "锻_连击" : "锻"));
                    if (icon != null) {
                        int count = playerData.getForgeCount(id);
                        int limit = craftData.getForgeCountLimit(player);
                        is[i] = icon.getItem(
                                player,
                                Pair.of("<ChanceName>", parent.plugin.config().getChanceName(craftData.getChance())),
                                Pair.of("<Score>", playerData.getScore(id)),
                                Pair.of("<Cost>", craftData.getCost()),
                                Pair.of("<Combo>", craftData.getCombo()),
                                Pair.of("<LimitCountCurrent>", count),
                                Pair.of("<LimitCountMax>", limit != 0 ? Math.max(limit, 0) : Message.craft__unlimited.str()),
                                Pair.of("<LimitCount>", limit != 0 ? Message.craft__limited.str(count, limit) : Message.craft__unlimited.str())
                        );
                    }
                    break;
                }
                case "时": {
                    putTimeSlot(i);
                    is[i] = getTimeIcon();
                    break;
                }
                case "返": {
                    Icon icon = items.get(key);
                    if (icon != null) {
                        if (category == null) {
                            Icon redirect = items.get(icon.redirect);
                            if (redirect != null) {
                                is[i] = icon.getItem(player);
                            }
                            break;
                        }
                        is[i] = icon.getItem(player);
                    }
                    break;
                }
                default : {
                    Icon icon = items.get(key);
                    if (icon != null) {
                        is[i] = icon.getItem(player);
                    }
                    break;
                }
            }
        }
        inventory.setContents(is);
        return null;
    }

    @Override
    @SuppressWarnings("IfCanBeSwitch")
    public void onClick(
            InventoryAction action, ClickType click,
            InventoryType.SlotType slotType, int slot,
            ItemStack currentItem, ItemStack cursor,
            InventoryView view, InventoryClickEvent event
    ) {
        event.setCancelled(true);
        Player player = playerData.getPlayer();
        parent.plugin.config().playSoundClickInventory(player);

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
        if ("返".equals(key)) { // 特殊图标
            Icon icon = parent.getIcon(key);
            if (icon == null) return;
            if (category == null) {
                Icon redirect = parent.getIcon(icon.redirect);
                if (redirect != null) {
                    handleClick(redirect, player, event);
                }
                return;
            }
            handleClick(icon, player, event);
            return;
        }
        // 其它图标
        Icon icon = parent.getIcon(key);
        if (icon != null) {
            handleClick(icon, player, event);
        }
    }

    private void handleClick(Icon icon, Player player, InventoryClickEvent event) {
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

    private boolean checkForgeData(Player player, CraftData craftData) {
        if (craftData.getMultiple().size() != 3) {
            Message.not_expected.tm(player, "multiple");
            return true;
        }
        return false;
    }

    /**
     * 点击进行 普通锻造
     */
    private void clickForgeOnce(Player player) {
        CraftData craftData = getCraftData();
        if (checkForgeData(player, craftData)) {
            return;
        }
        if (craftData.isDifficult() && CraftItem.getMiniGames() == null) {
            Message.no_protocollib.tm(player);
            return;
        }
        int cost = craftData.getCost();
        if (!parent.plugin.economy().has(player, cost)) {
            Message.craft__not_enough_money.tm(player);
            return;
        }
        if (craftData.isNotEnoughMaterial(player)) return;
        String key = getId();
        int limit = craftData.getForgeCountLimit(player);
        if (limit < 0 || (limit > 0 && playerData.getForgeCount(key) >= limit)) {
            Message.craft__forge_limit.tm(player, Math.max(limit, 0));
            return;
        }
        parent.plugin.economy().takeMoney(player, cost);
        final boolean win = (RandomUtils.nextInt(100) + 1 <= craftData.getChance());
        final int multiple = RandomUtils.nextInt(3);
        player.closeInventory();
        if (craftData.isDifficult()) {
            String randomGame = parent.plugin.config().getRandomGame();
            if (randomGame == null) {
                Message.no_minigames.tm(player);
                return;
            }
            CraftItem.getMiniGames().startGame(
                    new GameData(this, player, win, multiple),
                    player,
                    randomGame
            );
            return;
        }
        manager.playForgeAnimate(player, (clear, cancel) -> {
            if (manager.doForgeResult(this, player, win, multiple, cancel)) {
                clear.run();
            }
        });
    }

    /**
     * 点击进行 锻造连击
     */
    private void clickForgeCombo(Player player) {
        CraftData craftData = getCraftData();
        if (checkForgeData(player, craftData)) {
            return;
        }
        int combo = craftData.getCombo();
        if (craftData.isDifficult() || combo <= 0) return;

        int costOneTime = craftData.getCost();
        if (!parent.plugin.economy().has(player, costOneTime * combo)) {
            Message.craft__not_enough_money.tm(player);
            return;
        }
        if (craftData.isNotEnoughMaterial(player)) return;
        String key = getId();
        int limit = craftData.getForgeCountLimit(player);
        if (limit < 0 || (limit > 0 && playerData.getForgeCount(key) + combo - 1 >= limit)) {
            Message.craft__forge_limit.tm(player, Math.max(limit, 0));
            return;
        }

        player.closeInventory();
        manager.playForgeAnimate(player, (clear, cancel) -> {
            parent.plugin.getScheduler().runTask(() -> {
                if (craftData.isNotEnoughMaterial(player)) return;

                for (int i = 0; i < combo; i++) {
                    parent.plugin.economy().takeMoney(player, costOneTime);
                    final boolean win = (RandomUtils.nextInt(100) + 1 <= craftData.getChance());
                    final int multiple = RandomUtils.nextInt(3);
                    if (!manager.doForgeResult(this, player, win, multiple, null)) {
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
        if (!parent.plugin.config().isMeetTimeForgeCondition(playerData.getPlayer())) return;
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
            playerData.addTimeForgeCount(key, 1);
            playerData.save(); // 锻造结束，给予奖励
            Message.craft__success.tm(player, craftData.getDisplayItem());
            for (ItemStack item : craftData.getItems()) {
                for (ItemStack add : player.getInventory().addItem(new ItemStack[] { item }).values()) {
                    player.getWorld().dropItem(player.getLocation(), add);
                    Message.full_inventory.tm(player, add, add.getAmount());
                }
            }
            for (String str : craftData.getCommands()) {
                String cmd = str.split("\\|\\|")[0];
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PAPI.setPlaceholders(player, cmd));
            }
            return;
        }
        if (!processing) { // 如果时长锻造未开始
            int cost = craftData.getTimeCost();
            if (!parent.plugin.economy().has(player, cost)) {
                Message.craft__not_enough_money.tm(player);
                return;
            }
            if (craftData.isNotEnoughMaterial(player)) return;
            int limit = craftData.getTimeForgeCountLimit(player);
            if (limit < 0 || (limit > 0 && playerData.getTimeForgeCount(key) >= limit)) {
                Message.craft__time_forge_limit.tm(player, Math.max(limit, 0));
                return;
            }

            player.closeInventory();
            parent.plugin.economy().takeMoney(player, cost);
            craftData.takeAllMaterial(player);
            long endTime = System.currentTimeMillis() + craftData.getTime() * 1000L;
            playerData.setTime(key, endTime);
            playerData.save(); // 开始时长锻造
            Message.craft__time_start.tm(player);
        }
    }
}
