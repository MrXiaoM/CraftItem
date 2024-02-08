package cn.jrmcdp.craftitem.listener;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.Utils;
import cn.jrmcdp.craftitem.config.Config;
import cn.jrmcdp.craftitem.config.Craft;
import cn.jrmcdp.craftitem.config.Gui;
import cn.jrmcdp.craftitem.config.Message;
import cn.jrmcdp.craftitem.data.CraftData;
import cn.jrmcdp.craftitem.data.PlayerData;
import cn.jrmcdp.craftitem.event.CraftFailEvent;
import cn.jrmcdp.craftitem.event.CraftSuccessEvent;
import cn.jrmcdp.craftitem.holder.CategoryHolder;
import cn.jrmcdp.craftitem.holder.EditHolder;
import cn.jrmcdp.craftitem.holder.ForgeHolder;
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

public class GuiListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        final Inventory inventory = event.getInventory();
        InventoryHolder inventoryHolder = inventory.getHolder();
        if (inventoryHolder == null)
            return;
        if (inventoryHolder instanceof ForgeHolder) {
            ForgeHolder holder = (ForgeHolder) inventoryHolder;
            event.setCancelled(true);
            final PlayerData playerData = holder.getPlayerData();
            final Player player = playerData.getPlayer();
            player.playSound(player.getLocation(), Config.getSoundClickInventory(), 1.0F, 2.0F);
            if (!event.getClick().isRightClick() && !event.getClick().isLeftClick())
                return;
            if (event.getRawSlot() < 0 || event.getRawSlot() >= (Gui.getChest()).length)
                return;
            String key = Gui.getChest()[event.getRawSlot()];
            if ("锻".equals(key)) {
                final CraftData craftData = holder.getCraftData();
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
                                if (!craftData.hasMaterial(player.getInventory())) {
                                    Message.craft__not_enough_material.msg(player);
                                    clear();
                                    return;
                                }

                                int score = craftData.getMultiple().get(multiple);
                                int oldValue = playerData.getScore(holder.getId());
                                if (win) {
                                    player.playSound(player.getLocation(), Config.getSoundForgeSuccess(), 1.0F, 1.0F);
                                    int value = playerData.addScore(holder.getId(), score);
                                    CraftSuccessEvent e = new CraftSuccessEvent(player, holder, oldValue, value, multiple);
                                    Bukkit.getPluginManager().callEvent(e);
                                    if (value != e.getNewValue()) {
                                        value = playerData.setScore(holder.getId(), e.getNewValue());
                                        score = value - oldValue;
                                    }
                                    if (value == 100) {
                                        craftData.takeMaterial(player.getInventory());
                                        playerData.clearScore(holder.getId());
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
                                    int value = playerData.addScore(holder.getId(), -score);
                                    CraftFailEvent e = new CraftFailEvent(player, holder, oldValue, value, multiple);
                                    Bukkit.getPluginManager().callEvent(e);
                                    if (value != e.getNewValue()) {
                                        value = playerData.setScore(holder.getId(), e.getNewValue());
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
                                        cancel();
                                        return;
                                    }
                                    Gui.openGui(playerData, holder.getId(), craftData);
                                }, 10);
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
        } else if (inventoryHolder instanceof CategoryHolder) {
            event.setCancelled(true);
            CategoryHolder holder = (CategoryHolder) inventoryHolder;
            final Player player = (Player)event.getWhoClicked();
            if (event.getRawSlot() < 0 || event.getRawSlot() >= holder.getChest().length) return;
            player.playSound(player.getLocation(), Config.getSoundClickInventory(), 1.0F, 2.0F);
            String key = holder.getChest()[event.getRawSlot()];
            switch (key) {
                case "上" : {

                    if (!holder.upPage()) Message.page__already_first.msg(player);
                    return;
                }
                case "下" : {
                    if (!holder.downPage()) Message.page__already_last.msg(player);
                    return;
                }
                case "方" : {
                    String name = holder.getSlot()[event.getRawSlot()];
                    if (name == null) return;
                    Gui.openGui(holder.getPlayerData(), name, Craft.getCraftData(name));
                }
            }
        } else if (inventoryHolder instanceof EditHolder) {
            EditHolder holder = (EditHolder) inventoryHolder;
            final Inventory gui, loreGui;
            event.setCancelled(true);
            final Player player = (Player)event.getWhoClicked();
            player.playSound(player.getLocation(), Config.getSoundClickInventory(), 1.0F, 2.0F);
            if (event.getRawSlot() < 0 || event.getRawSlot() >= 9) return;
            final CraftData craftData = holder.getCraftData();
            switch (event.getRawSlot()) {
                case 0 : {
                    gui = Bukkit.createInventory(null, 54, Message.gui__edit_material_title.get());
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
                    Message.gui__edit_input_chance.msg(player);
                    Bukkit.getPluginManager().registerEvents(new Listener() {
                        @EventHandler
                        public void onAsyncPlayerChat(AsyncPlayerChatEvent eventB) {
                            if (eventB.getPlayer().equals(player)) {
                                eventB.setCancelled(true);
                                Integer chance = Utils.tryParseInt(eventB.getMessage());
                                if (chance == null || chance < 0) {
                                    Message.not_integer.msg(player);
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
                    Message.gui__edit_input_multiple.msg(player);
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
                                        Message.not_integer.msg(player);
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
                    Message.gui__edit_input_cost.msg(player);
                    Bukkit.getPluginManager().registerEvents(new Listener() {
                        @EventHandler
                        public void onAsyncPlayerChat(AsyncPlayerChatEvent eventB) {
                            if (eventB.getPlayer().equals(player)) {
                                eventB.setCancelled(true);
                                Integer chance = Utils.tryParseInt(eventB.getMessage());
                                if (chance == null) {
                                    Message.not_integer.msg(player);
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
                    gui = Bukkit.createInventory(null, 9, Message.gui__edit_display_title.get());
                    gui.addItem(craftData.getDisplayItem());
                    player.openInventory(gui);
                    Bukkit.getPluginManager().registerEvents(new Listener() {
                        @EventHandler
                        public void onInventoryClose(InventoryCloseEvent eventB) {
                            if (eventB.getPlayer().equals(player)) {
                                ItemStack item = eventB.getInventory().getItem(0);
                                if (item == null || item.getType().equals(Material.AIR)) {
                                    Message.gui__edit_display_not_found.msg(player);
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
                    gui = Bukkit.createInventory(null, 54, Message.gui__edit_item_title.get());
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
                    loreGui = Bukkit.createInventory(null, 54, Message.gui__edit_command_title.get());
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
                                for (ItemStack itemStack : e.getInventory()) {
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
        InventoryHolder inventoryHolder = inventory.getHolder();
        if (inventoryHolder instanceof ForgeHolder) {
            event.setCancelled(true);
        }
    }
}
