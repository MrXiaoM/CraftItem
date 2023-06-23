package cn.jrmcdp.craftitem.listener;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.Utils;
import cn.jrmcdp.craftitem.config.Craft;
import cn.jrmcdp.craftitem.config.Gui;
import cn.jrmcdp.craftitem.config.Message;
import cn.jrmcdp.craftitem.data.CraftData;
import cn.jrmcdp.craftitem.data.PlayerData;
import cn.jrmcdp.craftitem.holder.CategoryHolder;
import cn.jrmcdp.craftitem.holder.EditHolder;
import cn.jrmcdp.craftitem.holder.ForgeHolder;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.messages.Titles;
import me.clip.placeholderapi.PlaceholderAPI;
import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class GuiListener implements Listener {

    private static Titles title = new Titles("§a敲敲打打", "§e锻造中...", 10, 20, 10);

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        final Inventory inventory = event.getInventory();
        if (inventory == null)
            return;
        InventoryHolder inventoryHolder = inventory.getHolder();
        if (inventoryHolder == null)
            return;
        if (inventoryHolder instanceof ForgeHolder) {
            ForgeHolder holder = (ForgeHolder) inventoryHolder;
            event.setCancelled(true);
            final PlayerData playerData = holder.getPlayerData();
            final Player player = playerData.getPlayer();
            player.playSound(player.getLocation(), XSound.matchXSound("UI_BUTTON_CLICK").get().parseSound(), 1.0F, 2.0F);
            if (!event.getClick().isRightClick() && !event.getClick().isLeftClick())
                return;
            if (event.getRawSlot() < 0 || event.getRawSlot() >= (Gui.getChest()).length)
                return;
            String key = Gui.getChest()[event.getRawSlot()];
            if ("锻".equals(key)) {
                final CraftData craftData = holder.getCraftData();
                int cost = craftData.getCost();
                if (!CraftItem.getEcon().has(player, cost)) {
                    player.sendMessage(Message.getPrefix() + "§e没有足够的金币来锻造");
                    return;
                }
                CraftItem.getEcon().withdrawPlayer(player, cost);
                if (!craftData.hasMaterial(player.getInventory())) {
                    player.sendMessage(Message.getPrefix() + "§e身上没有足够的材料");
                    return;
                }
                final boolean win = (RandomUtils.nextInt(100) + 1 <= craftData.getChance());
                final int multiple = RandomUtils.nextInt(3);
                final Integer score = craftData.getMultiple().get(multiple);
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

                    BukkitTask task = (new BukkitRunnable() {
                        int i = 1;

                        public void run() {
                            if (this.i >= 3) {
                                if (!craftData.hasMaterial(player.getInventory())) {
                                    player.sendMessage(Message.getPrefix() + "§e身上没有足够的材料");
                                    clear();
                                    return;
                                }
                                if (win) {
                                    player.playSound(player.getLocation(), XSound.matchXSound("BLOCK_ANVIL_USE").get().parseSound(), 1.0F, 1.0F);
                                    Integer value = playerData.addScore(holder.getId(), score);
                                    if (value == 100) {
                                        craftData.takeMaterial(player.getInventory());
                                        playerData.clearScore(holder.getId());
                                        player.sendMessage(Message.getPrefix() + "§a成功锻造出了 §e" + cn.jrmcdp.craftitem.config.Material.getItemName(craftData.getDisplayItem()));
                                        for (ItemStack item : craftData.getItems()) {
                                            for (ItemStack add : player.getInventory().addItem(new ItemStack[] { item }).values()) {
                                                player.getWorld().dropItem(player.getLocation(), add);
                                                player.sendMessage(Message.getPrefix() + "§c背包已满 §d" + cn.jrmcdp.craftitem.config.Material.getItemName(add) + "§ex" + add.getAmount() + " §c掉了出来");
                                            }
                                        }
                                        for (String str : craftData.getCommands()) {
                                            String cmd = str.split("\\|\\|")[0];
                                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderAPI.setPlaceholders(player, cmd));
                                        }
                                    } else {
                                        switch (multiple) {
                                            case 0 : {
                                                player.sendMessage(Message.getPrefix() + "§a锻造 小成功 ！！！ §f§l[ §a+ §e" + score + "% §f§l]");
                                                break;
                                            }
                                            case 1 : {
                                                player.sendMessage(Message.getPrefix() + "§a锻造 成功 ！！！ §f§l[ §a+ §e" + score + "% §f§l]");
                                                break;
                                            }
                                            case 2 : {
                                                player.sendMessage(Message.getPrefix() + "§a锻造 大成功 ！！！ §f§l[ §a+ §e" + score + "% §f§l]");
                                                break;
                                            }
                                        }
                                    }
                                } else {
                                    player.playSound(player.getLocation(), XSound.matchXSound("BLOCK_GLASS_BREAK").get().parseSound(), 1.0F, 1.0F);
                                    playerData.addScore(holder.getId(), -score);
                                    switch (multiple) {
                                        case 0 : {
                                            player.sendMessage(Message.getPrefix() + "§c锻造 小失败 ！！！ §f§l[ §c- §e" + score + "% §f§l]");
                                            break;
                                        }
                                        case 1 : {
                                            player.sendMessage(Message.getPrefix() + "§c锻造 失败 ！！！ §f§l[ §c- §e" + score + "% §f§l]");
                                            break;
                                        }
                                        case 2 : {
                                            player.sendMessage(Message.getPrefix() + "§c锻造 大失败 ！！！ §f§l[ §c- §e" + score + "% §f§l]");
                                            player.sendMessage(Message.getPrefix() + "§c并且还损坏了一个 §e" + cn.jrmcdp.craftitem.config.Material.getItemName(craftData.takeRandomMaterial(player.getInventory())));
                                            break;
                                        }
                                    }
                                }
                                playerData.save();
                                Bukkit.getScheduler().runTaskLater(CraftItem.getPlugin(), () -> {
                                    if (!player.isOnline()) {
                                        cancel();
                                        return;
                                    }
                                    Gui.openGui(playerData, holder.getId(), craftData);
                                }, 10);
                                clear();
                                return;
                            }
                            title.send(player);
                            player.playSound(player.getLocation(), XSound.matchXSound("BLOCK_ANVIL_LAND").get().parseSound(), 1.0F, 0.8F);
                            this.i++;
                        }
                    }).runTaskTimer(CraftItem.getPlugin(), 5L, 15L);
                }, CraftItem.getPlugin());
            }
        } else if (inventoryHolder instanceof CategoryHolder) {
            event.setCancelled(true);
            CategoryHolder holder = (CategoryHolder) inventoryHolder;
            final Player player = (Player)event.getWhoClicked();
            if (event.getRawSlot() < 0 || event.getRawSlot() >= holder.getChest().length) return;
            player.playSound(player.getLocation(), XSound.matchXSound("UI_BUTTON_CLICK").get().parseSound(), 1.0F, 2.0F);
            String key = holder.getChest()[event.getRawSlot()];
            switch (key) {
                case "上" : {
                    if (!holder.upPage()) player.sendMessage(Message.getPrefix() + "§e当前已是首页");
                    return;
                }
                case "下" : {
                    if (!holder.downPage()) player.sendMessage(Message.getPrefix() + "§e当前已是尾页");
                    return;
                }
                case "方" : {
                    String name = holder.getSlot()[event.getRawSlot()];
                    if (name == null) return;
                    Gui.openGui(holder.getPlayerData(), name, Craft.getCraftData(name));
                    return;
                }
            }
        } else if (inventoryHolder instanceof EditHolder) {
            EditHolder holder = (EditHolder) inventoryHolder;
            final Inventory gui, loreGui;
            event.setCancelled(true);
            final Player player = (Player)event.getWhoClicked();
            player.playSound(player.getLocation(), XSound.matchXSound("UI_BUTTON_CLICK").get().parseSound(), 1.0F, 2.0F);
            if (event.getRawSlot() < 0 || event.getRawSlot() >= 9) return;
            final CraftData craftData = holder.getCraftData();
            switch (event.getRawSlot()) {
                case 0 : {
                    gui = Bukkit.createInventory(null, 54, "材料");
                    gui.addItem((ItemStack[]) craftData.getMaterial().toArray((Object[]) new ItemStack[craftData.getMaterial().size()]));
                    player.openInventory(gui);
                    Bukkit.getPluginManager().registerEvents(new Listener() {
                        @EventHandler
                        public void onInventoryClose(InventoryCloseEvent eventB) {
                            if (eventB.getPlayer().equals(player)) {
                                List<ItemStack> list = new ArrayList<>();
                                for (ItemStack content : eventB.getInventory().getContents()) {
                                    if (content != null && !content.getType().equals(Material.AIR))
                                        list.add(content);
                                }
                                craftData.setMaterial(list);
                                Craft.save(holder.getId(), craftData);
                                HandlerList.unregisterAll(this);
                                Bukkit.getScheduler().runTask(CraftItem.getPlugin(), () -> player.openInventory(holder.buildGui()));
                            }
                        }
                    }, CraftItem.getPlugin());
                    break;
                }
                case 1 : {
                    player.closeInventory();
                    player.sendMessage(Message.getPrefix() + "§a请发送概率 正整数");
                    Bukkit.getPluginManager().registerEvents(new Listener() {
                        @EventHandler
                        public void onAsyncPlayerChat(AsyncPlayerChatEvent eventB) {
                            if (eventB.getPlayer().equals(player)) {
                                eventB.setCancelled(true);
                                Integer chance = Utils.tryParseInt(eventB.getMessage());
                                if (chance == null) {
                                    player.sendMessage(Message.getPrefix() + "§c请输入整数");
                                } else {
                                    craftData.setChance(chance);
                                    Craft.save(holder.getId(), craftData);
                                }
                                HandlerList.unregisterAll(this);
                                Bukkit.getScheduler().runTask(CraftItem.getPlugin(), () -> player.openInventory(holder.buildGui()));
                            }
                        }
                    }, CraftItem.getPlugin());
                    break;
                }
                case 2 : {
                    player.closeInventory();
                    player.sendMessage(Message.getPrefix() + "§a请按照格式填写倍率 \"5 10 20\" (小 中 大)");
                    Bukkit.getPluginManager().registerEvents(new Listener() {
                        @EventHandler
                        public void onAsyncPlayerChat(AsyncPlayerChatEvent eventB) {
                            if (eventB.getPlayer().equals(player)) {
                                eventB.setCancelled(true);
                                String[] split = eventB.getMessage().split(" ");
                                List<Integer> list = new ArrayList<>();
                                for (String str : split) {
                                    Integer chance = Utils.tryParseInt(str);
                                    if (chance == null) {
                                        player.sendMessage(Message.getPrefix() + "§c数字转换异常");
                                        HandlerList.unregisterAll(this);
                                        Bukkit.getScheduler().runTask(CraftItem.getPlugin(), () -> player.openInventory(holder.buildGui()));
                                        return;
                                    }
                                    list.add(chance);
                                }
                                craftData.setMultiple(list);
                                Craft.save(holder.getId(), craftData);
                                HandlerList.unregisterAll(this);
                                Bukkit.getScheduler().runTask(CraftItem.getPlugin(), () -> player.openInventory(holder.buildGui()));
                            }
                        }
                    }, CraftItem.getPlugin());
                    break;
                }
                case 3 : {
                    player.closeInventory();
                    player.sendMessage(Message.getPrefix() + "§a请发送金额 正整数");
                    Bukkit.getPluginManager().registerEvents(new Listener() {
                        @EventHandler
                        public void onAsyncPlayerChat(AsyncPlayerChatEvent eventB) {
                            if (eventB.getPlayer().equals(player)) {
                                eventB.setCancelled(true);
                                Integer chance = Utils.tryParseInt(eventB.getMessage());
                                if (chance == null) {
                                    player.sendMessage(Message.getPrefix() + "§c请输入整数");
                                } else {
                                    craftData.setCost(chance);
                                    Craft.save(holder.getId(), craftData);
                                }
                                HandlerList.unregisterAll(this);
                                Bukkit.getScheduler().runTask(CraftItem.getPlugin(), () -> player.openInventory(holder.buildGui()));
                            }
                        }
                    }, CraftItem.getPlugin());
                    break;
                }
                case 4 : {
                    gui = Bukkit.createInventory(null, 9, "将要展示的物品放在第一格");
                    gui.addItem(craftData.getDisplayItem());
                    player.openInventory(gui);
                    Bukkit.getPluginManager().registerEvents(new Listener() {
                        @EventHandler
                        public void onInventoryClose(InventoryCloseEvent eventB) {
                            if (eventB.getPlayer().equals(player)) {
                                ItemStack item = eventB.getInventory().getItem(0);
                                if (item == null || item.getType().equals(Material.AIR)) {
                                    player.sendMessage(Message.getPrefix() + "§c未找到第一格的物品");
                                    HandlerList.unregisterAll(this);
                                    Bukkit.getScheduler().runTask(CraftItem.getPlugin(), () -> player.openInventory(holder.buildGui()));
                                    return;
                                }
                                craftData.setDisplayItem(item);
                                Craft.save(holder.getId(), craftData);
                                HandlerList.unregisterAll(this);
                                Bukkit.getScheduler().runTask(CraftItem.getPlugin(), () -> player.openInventory(holder.buildGui()));
                            }
                        }
                    }, CraftItem.getPlugin());
                    break;
                }
                case 5 : {
                    gui = Bukkit.createInventory(null, 54, "奖励物品");
                    for (ItemStack item : craftData.getItems()) {
                        gui.addItem(item);
                    }
                    player.openInventory(gui);
                    Bukkit.getPluginManager().registerEvents(new Listener() {
                        @EventHandler
                        public void onInventoryClose(InventoryCloseEvent eventB) {
                            if (eventB.getPlayer().equals(player)) {
                                List<ItemStack> list = new ArrayList<>();
                                for (ItemStack content : eventB.getInventory().getContents()) {
                                    if (content != null && !content.getType().equals(Material.AIR))
                                        list.add(content);
                                }
                                craftData.setItems(list);
                                Craft.save(holder.getId(), craftData);
                                HandlerList.unregisterAll(this);
                                Bukkit.getScheduler().runTask(CraftItem.getPlugin(), () -> player.openInventory(holder.buildGui()));
                            }
                        }
                    }, CraftItem.getPlugin());
                    break;
                }
                case 6 : {
                    loreGui = Bukkit.createInventory(null, 54, "奖励命令");
                    for (String line : craftData.getCommands()) {
                        ItemStack itemStack = new ItemStack(Material.PAPER);
                        ItemMeta itemMeta = itemStack.getItemMeta();
                        itemMeta.setDisplayName(line);
                        itemStack.setItemMeta(itemMeta);
                        loreGui.addItem(itemStack);
                    }
                    player.openInventory(loreGui);
                    Bukkit.getPluginManager().registerEvents(new Listener() {
                        boolean isChat = false;

                        @EventHandler
                        public void onInventoryClick(InventoryClickEvent e) {
                            if (e.getWhoClicked().getName().equals(player.getName())) {
                                if (e.getCurrentItem() != null && !e.getCurrentItem().getType().equals(Material.AIR))
                                    return;
                                if (e.getCursor() != null && !e.getCursor().getType().equals(Material.AIR))
                                    return;
                                this.isChat = true;
                                player.closeInventory();
                                Bukkit.getPluginManager().registerEvents(new Listener() {
                                    @EventHandler
                                    public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
                                        if (e.getPlayer().equals(player)) {
                                            e.setCancelled(true);
                                            String id = e.getMessage();
                                            ItemStack itemStack = new ItemStack(Material.PAPER);
                                            ItemMeta itemMeta = itemStack.getItemMeta();
                                            itemMeta.setDisplayName(id.replace("&", "§"));
                                            itemStack.setItemMeta(itemMeta);
                                            loreGui.addItem(itemStack);
                                            Bukkit.getScheduler().runTask(CraftItem.getPlugin(), () -> player.openInventory(loreGui));
                                            isChat = false;
                                            HandlerList.unregisterAll(this);
                                        }
                                    }
                                }, CraftItem.getPlugin());
                            }
                        }

                        @EventHandler
                        public void onInventoryClose(InventoryCloseEvent e) {
                            if (e.getPlayer().getName().equals(player.getName()) && !this.isChat) {
                                List<String> lore = new ArrayList<>();
                                for (ListIterator<ItemStack> listIterator = e.getInventory().iterator(); listIterator.hasNext(); ) {
                                    ItemStack itemStack = listIterator.next();
                                    if (itemStack == null || itemStack.getType().equals(Material.AIR))
                                        continue;
                                    ItemMeta itemMeta = itemStack.getItemMeta();
                                    if (!itemMeta.hasDisplayName())
                                        continue;
                                    lore.add(itemMeta.getDisplayName());
                                }
                                craftData.setCommands(lore);
                                Craft.save(holder.getId(), craftData);
                                HandlerList.unregisterAll(this);
                                Bukkit.getScheduler().runTask(CraftItem.getPlugin(), () -> player.openInventory(holder.buildGui()));
                            }
                        }
                    }, CraftItem.getPlugin());
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Inventory inventory = event.getInventory();
        if (inventory == null) {
            return;
        }
        InventoryHolder inventoryHolder = inventory.getHolder();
        if (inventoryHolder instanceof ForgeHolder) {
            event.setCancelled(true);
        }
    }
}
