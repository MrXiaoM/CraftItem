package cn.jrmcdp.craftitem.holder;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.Utils;
import cn.jrmcdp.craftitem.config.Config;
import cn.jrmcdp.craftitem.config.Craft;
import cn.jrmcdp.craftitem.config.Message;
import cn.jrmcdp.craftitem.data.CraftData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EditHolder implements InventoryHolder {
    private final String id;

    private final CraftData craftData;
    private Inventory inventory;

    EditHolder(String id, CraftData craftData) {
        this.id = id;
        this.craftData = craftData;
    }

    public String getId() {
        return this.id;
    }

    public CraftData getCraftData() {
        return this.craftData;
    }

    public static Inventory buildGui(String id, CraftData craftData) {
        EditHolder holder = new EditHolder(id, craftData);
        Inventory inventory = Bukkit.createInventory(holder, 9, Message.gui__edit_title.get(holder.id));
        inventory.setContents(holder.getItems());
        holder.inventory = inventory;
        return inventory;
    }
    public Inventory buildGui() {
        Inventory inventory = Bukkit.createInventory(this, 9, Message.gui__edit_title.get(this.id));
        inventory.setContents(getItems());
        this.inventory = inventory;
        return inventory;
    }

    private ItemStack[] getItems() {
        return new ItemStack[] {
                getItemStack(Material.WHEAT, Message.gui__edit__item__material__name.get(),
                        Message.gui__edit__item__material__lore.list(
                                String.join("\n§7", Utils.itemToListString(craftData.getMaterial()))
                        )
                ),
                getItemStack(Material.COMPASS, Message.gui__edit__item__successful_rate__name.get(),
                        Message.gui__edit__item__successful_rate__lore.list(
                                craftData.getChance()
                        )
                ),
                getItemStack(Material.HOPPER, Message.gui__edit__item__multiple__name.get(),
                        Message.gui__edit__item__multiple__lore.list(
                                craftData.getMultiple().stream().map(String::valueOf).collect(Collectors.joining(" "))
                        )
                ),
                getItemStack(Material.GOLD_INGOT, Message.gui__edit__item__cost__name.get(),
                        Message.gui__edit__item__cost__lore.list(
                                craftData.getCost()
                        )
                ),
                getItemStack(Material.PAINTING, Message.gui__edit__item__display__name.get(),
                        Message.gui__edit__item__display__lore.list(
                                Utils.getItemName(craftData.getDisplayItem())
                        )
                ),
                getItemStack(Material.CHEST, Message.gui__edit__item__item__name.get(),
                        Message.gui__edit__item__item__lore.list(
                            String.join("\n§7", Utils.itemToListString(craftData.getItems()))
                        )
                ),
                getItemStack(Material.PAPER, Message.gui__edit__item__command__name.get(),
                        Message.gui__edit__item__command__lore.list(
                            String.join("\n§7", craftData.getCommands())
                        )
                )
        };
    }

    private ItemStack getItemStack(Material material, String name, List<String> lores) {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(name);
        itemMeta.setLore(lores);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void onClick(InventoryClickEvent event) {
        final Inventory gui, loreGui;
        final Player player = (Player)event.getWhoClicked();
        player.playSound(player.getLocation(), Config.getSoundClickInventory(), 1.0F, 2.0F);
        if (event.getRawSlot() < 0 || event.getRawSlot() >= 9) return;
        final CraftData craftData = getCraftData();
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
                            Craft.save(getId(), craftData);
                            HandlerList.unregisterAll(this);
                            Bukkit.getScheduler().runTask(CraftItem.getPlugin(), () -> player.openInventory(buildGui()));
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
                                Craft.save(getId(), craftData);
                            }
                            HandlerList.unregisterAll(this);
                            Bukkit.getScheduler().runTask(CraftItem.getPlugin(), () -> player.openInventory(buildGui()));
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
                                    Bukkit.getScheduler().runTask(CraftItem.getPlugin(), () -> player.openInventory(buildGui()));
                                    return;
                                }
                                list.add(chance);
                            }
                            craftData.setMultiple(list);
                            Craft.save(getId(), craftData);
                            HandlerList.unregisterAll(this);
                            Bukkit.getScheduler().runTask(CraftItem.getPlugin(), () -> player.openInventory(buildGui()));
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
                                Craft.save(getId(), craftData);
                            }
                            HandlerList.unregisterAll(this);
                            Bukkit.getScheduler().runTask(CraftItem.getPlugin(), () -> player.openInventory(buildGui()));
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
                                Bukkit.getScheduler().runTask(CraftItem.getPlugin(), () -> player.openInventory(buildGui()));
                                return;
                            }
                            craftData.setDisplayItem(item);
                            Craft.save(getId(), craftData);
                            HandlerList.unregisterAll(this);
                            Bukkit.getScheduler().runTask(CraftItem.getPlugin(), () -> player.openInventory(buildGui()));
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
                            Craft.save(getId(), craftData);
                            HandlerList.unregisterAll(this);
                            Bukkit.getScheduler().runTask(CraftItem.getPlugin(), () -> player.openInventory(buildGui()));
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
                            Craft.save(getId(), craftData);
                            HandlerList.unregisterAll(this);
                            Bukkit.getScheduler().runTask(CraftItem.getPlugin(), () -> player.openInventory(buildGui()));
                        }
                    }
                }, CraftItem.getPlugin());
                break;
            }
        }
    }
}
