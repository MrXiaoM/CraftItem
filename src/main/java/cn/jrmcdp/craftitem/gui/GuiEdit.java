package cn.jrmcdp.craftitem.gui;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.config.ConfigForgeGui;
import cn.jrmcdp.craftitem.config.Message;
import cn.jrmcdp.craftitem.data.CraftData;
import cn.jrmcdp.craftitem.func.CraftRecipeManager;
import cn.jrmcdp.craftitem.utils.Prompter;
import cn.jrmcdp.craftitem.utils.Utils;
import de.tr7zw.changeme.nbtapi.NBT;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.jrmcdp.craftitem.utils.Utils.*;

public class GuiEdit implements IHolder {
    private final String id;

    private final CraftData craftData;
    private Inventory inventory;
    private int invSize = 0;

    private final Player player;
    private final CraftRecipeManager manager = CraftRecipeManager.inst();

    public GuiEdit(Player player, String id, CraftData craftData) {
        this.player = player;
        this.id = id;
        this.craftData = craftData;
    }

    public static void openGui(Player player, String id, CraftData craftData) {
        new GuiEdit(player, id, craftData).open();
    }

    public String getId() {
        return this.id;
    }

    public CraftData getCraftData() {
        return this.craftData;
    }

    enum Slot {
        MATERIAL(0, gui -> {
            return getItemStack(getMaterial("WHEAT"), Message.gui__edit__item__material__name.str(),
                    Message.gui__edit__item__material__lore.list(
                            String.join("\n§7", Utils.itemToListString(gui.craftData.getMaterial()))
                    ));
        }),
        SUCCESSFUL_RATE(1, gui -> {
            return getItemStack(getMaterial("COMPASS"), Message.gui__edit__item__successful_rate__name.str(),
                    Message.gui__edit__item__successful_rate__lore.list(
                            gui.craftData.getChance()
                    ));
        }),
        MULTIPLE(2, gui -> {
            return getItemStack(getMaterial("HOPPER"), Message.gui__edit__item__multiple__name.str(),
                    Message.gui__edit__item__multiple__lore.list(
                            gui.craftData.getMultiple().stream().map(String::valueOf).collect(Collectors.joining(" "))
                    ));
        }),
        COST_MONEY(3, gui -> {
            return getItemStack(getMaterial("GOLD_INGOT"), Message.gui__edit__item__cost__name.str(),
                    Message.gui__edit__item__cost__lore.list(
                            gui.craftData.getCost()
                    ));
        }),
        COST_LEVEL(4, gui -> {
            return getItemStack(getMaterial("EXPERIENCE_BOTTLE"), Message.gui__edit__item__cost_level__name.str(),
                    Message.gui__edit__item__cost_level__lore.list(
                            gui.craftData.getCostLevel()
                    ));
        }),
        DISPLAY(5, gui -> {
            return getItemStack(getMaterial("PAINTING"), Message.gui__edit__item__display__name.str(),
                    Message.gui__edit__item__display__lore.list(
                            gui.craftData.getDisplayItem()
                    ));
        }),
        REWARD_ITEMS(6, gui -> {
            return getItemStack(getMaterial("CHEST"), Message.gui__edit__item__item__name.str(),
                    Message.gui__edit__item__item__lore.list(
                            String.join("\n§7", Utils.itemToListString(gui.craftData.getItems()))
                    ));
        }),
        REWARD_COMMANDS(7, gui -> {
            return getItemStack(getMaterial("PAPER"), Message.gui__edit__item__command__name.str(),
                    Message.gui__edit__item__command__lore.list(
                            String.join("\n§7", gui.craftData.getCommands())
                    ));
        }),
        TIME(8, gui -> {
            return getItemStack(getMaterial("CLOCK", "WATCH"), Message.gui__edit__item__time__name.str(),
                    Message.gui__edit__item__time__lore.list(
                            gui.craftData.getTimeDisplay(), gui.craftData.getTimeCost()
                    ));
        }),
        TIME_LIMIT(9, gui -> {
            String groupTime = gui.craftData.getTimeCountLimit();
            if (groupTime.trim().isEmpty()) groupTime = Message.gui__edit__unset.str();
            String groupNormal = gui.craftData.getCountLimit();
            if (groupNormal.trim().isEmpty()) groupNormal = Message.gui__edit__unset.str();
            return getItemStack(getMaterial("BUCKET"), Message.gui__edit__item__time_count_limit__name.str(),
                    Message.gui__edit__item__time_count_limit__lore.list(groupNormal, groupTime));
        }),
        DIFFICULT(10, gui -> {
            return getItemStack(getMaterial("FISHING_ROD"), Message.gui__edit__item__difficult__name.str(),
                    Message.gui__edit__item__difficult__lore.list(
                            (gui.craftData.isDifficult() ? Message.gui__edit__status__on : Message.gui__edit__status__off).str()
                    ));
        }),
        FAIL_TIMES(11, gui -> {
            return getItemStack(getMaterial("BOWL"), Message.gui__edit__item__fail_times__name.str(),
                    Message.gui__edit__item__fail_times__lore.list(
                            gui.craftData.getGuaranteeFailTimes() > 0 ? String.valueOf(gui.craftData.getGuaranteeFailTimes()) : Message.gui__edit__unset.str()
                    ));
        }),
        COMBO(12, gui -> {
            return getItemStack(getMaterial("MAGMA_CREAM"), Message.gui__edit__item__combo__name.str(),
                    Message.gui__edit__item__combo__lore.list(
                            gui.craftData.getCombo() > 0 ? String.valueOf(gui.craftData.getCombo()) : Message.gui__edit__unset.str()
                    ));
        }),

        ;
        final int index;
        final Function<GuiEdit, ItemStack> icon;

        Slot(int index, Function<GuiEdit, ItemStack> icon) {
            this.index = index;
            this.icon = icon;
        }

        void setItem(Inventory inv, GuiEdit gui) {
            ItemStack item = icon.apply(gui);
            inv.setItem(index, item);
        }

        @Nullable
        static Slot valueOf(int index) {
            for (Slot slot : values()) {
                if (slot.index == index) return slot;
            }
            return null;
        }
    }

    @Override
    public Inventory newInventory() {
        ItemStack[] items = new ItemStack[invSize = 18];
        inventory = CraftItem.getInventoryFactory().create(this, items.length, Message.gui__edit_title.str(this.id));
        for (Slot slot : Slot.values()) {
            items[slot.index] = slot.icon.apply(this);
        }
        inventory.setContents(items);
        return inventory;
    }


    @NotNull
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    private void checkMaterialSlots(Player player, int size) {
        ConfigForgeGui forgeGui = ConfigForgeGui.inst();
        int count1 = 0, count2 = 0;
        for (char c : forgeGui.getChest()) if (c == '材') count1++;
        for (char c : forgeGui.getChestTime()) if (c == '材') count2++;
        if (size > Math.min(count1, count2)) {
            Message.gui__edit__item__material__too_much.tm(player);
        }
    }

    private void reopen() {
        manager.plugin.getScheduler().runTask(this::open);
    }

    @Override
    public void onClick(
            InventoryAction action, ClickType click,
            InventoryType.SlotType slotType, int slotIndex,
            ItemStack currentItem, ItemStack cursor,
            InventoryView view, InventoryClickEvent event
    ) {
        event.setCancelled(true);
        manager.plugin.config().getSoundClickInventory().play(player);
        if (event.getRawSlot() < 0 || event.getRawSlot() >= invSize) return;
        final CraftData craftData = getCraftData();
        Slot slot = Slot.valueOf(slotIndex);
        if (slot == null) return;
        switch (slot) {
            case MATERIAL: { // 材料
                Message title = Message.gui__edit_material_title;
                ItemStack[] items = craftData.getMaterialArray();
                Prompter.gui(player, 54, title, items, inv -> { // onClose
                    List<ItemStack> list = new ArrayList<>();
                    for (ItemStack content : inv.getContents()) {
                        if (content != null && !content.getType().equals(Material.AIR)) {
                            list.add(content);
                        }
                    }
                    craftData.setMaterial(list);
                    manager.save(getId(), craftData);
                    checkMaterialSlots(player, list.size());
                    reopen();
                });
                break;
            }
            case SUCCESSFUL_RATE: { // 成功率
                player.closeInventory();
                Message.gui__edit_input_chance.tm(player);
                Prompter.onChat(player, message -> {
                    Integer chance = Util.parseInt(message).orElse(null);
                    if (chance == null || chance < 0) {
                        Message.not_integer.tm(player);
                    } else {
                        craftData.setChance(chance);
                        manager.save(getId(), craftData);
                    }
                    reopen();
                });
                break;
            }
            case MULTIPLE: { // 倍数
                player.closeInventory();
                Message.gui__edit_input_multiple.tm(player);
                AtomicReference<Consumer<List<Integer>>> save = new AtomicReference<>(null);
                Consumer<String> consumeChat = message -> {
                    String[] split = message.split(" ");
                    List<Integer> list = new ArrayList<>();
                    for (String str : split) {
                        Integer chance = Util.parseInt(str).orElse(null);
                        if (chance == null) {
                            Message.not_integer.tm(player);
                            reopen();
                            return;
                        }
                        list.add(chance);
                    }
                    Consumer<List<Integer>> consumer = save.get();
                    if (consumer != null) consumer.accept(list);
                };
                save.set(list -> {
                    if (list.size() != 3) {
                        Message.gui__edit_input_multiple.tm(player);
                        Prompter.onChat(player, consumeChat);
                    } else {
                        craftData.setMultiple(list);
                        manager.save(getId(), craftData);
                    }
                    reopen();
                });
                Prompter.onChat(player, consumeChat);
                break;
            }
            case COST_MONEY: { // 价格
                player.closeInventory();
                Message.gui__edit_input_cost.tm(player);
                Prompter.onChat(player, message -> {
                    Integer cost = Util.parseInt(message).orElse(null);
                    if (cost == null || cost < 0) {
                        Message.not_integer.tm(player);
                    } else {
                        craftData.setCost(cost);
                        manager.save(getId(), craftData);
                    }
                    reopen();
                });
                break;
            }
            case COST_LEVEL: { // 花费经验等级
                player.closeInventory();
                Message.gui__edit_input_cost_level.tm(player);
                Prompter.onChat(player, message -> {
                    Integer costLevel = Util.parseInt(message).orElse(null);
                    if (costLevel == null || costLevel < 0) {
                        Message.not_integer.tm(player);
                    } else {
                        craftData.setCostLevel(costLevel);
                        manager.save(getId(), craftData);
                    }
                    reopen();
                });
                break;
            }
            case DISPLAY: { // 显示物品
                Message title = Message.gui__edit_display_title;
                ItemStack[] items = new ItemStack[] { craftData.getDisplayItem() };
                Prompter.gui(player, 9, title, items, inv -> { // onClose
                    ItemStack item = inv.getItem(0);
                    if (item == null || item.getType().equals(Material.AIR)) {
                        Message.gui__edit_display_not_found.tm(player);
                    } else {
                        craftData.setDisplayItem(item);
                        manager.save(getId(), craftData);
                    }
                    reopen();
                });
                break;
            }
            case REWARD_ITEMS: { // 奖励物品
                Message title = Message.gui__edit_item_title;
                List<ItemStack> items = craftData.getItems();
                Prompter.gui(player, 54, title, items, inv -> { // onClose
                    List<ItemStack> list = new ArrayList<>();
                    for (ItemStack content : inv.getContents()) {
                        if (content != null && !content.getType().equals(Material.AIR))
                            list.add(content);
                    }
                    craftData.setItems(list);
                    manager.save(getId(), craftData);
                    reopen();
                });
                break;
            }
            case REWARD_COMMANDS: { // 奖励命令
                AtomicBoolean isChat = new AtomicBoolean(false);
                Message title = Message.gui__edit_command_title;
                Prompter.gui(player, 54, title, inv -> { // init
                    for (String line : craftData.getCommands()) {
                        ItemStack itemStack = getItemStack(Material.PAPER, line, Message.gui__edit_command_lore.list());
                        NBT.modify(itemStack, nbt -> {
                            nbt.setString("CRAFTITEM_COMMAND", line);
                        });
                        inv.addItem(itemStack);
                    }
                }, e -> { // onClick
                    e.setCancelled(true);
                    Inventory inv = e.getClickedInventory();
                    if (inv == null || !(inv.getHolder() instanceof Prompter)) return;
                    if (!isAir(e.getCursor())) return;
                    if (!isAir(e.getCurrentItem())) {
                        inv.setItem(e.getSlot(), null);
                        return;
                    }
                    isChat.set(true);
                    player.closeInventory();
                    Message.gui__edit_command_tips.tm(player);

                    Prompter.onChat(player, message -> {
                        ItemStack itemStack = Utils.getItemStack(
                                Material.PAPER,
                                message,
                                Message.gui__edit_command_lore.list()
                        );
                        NBT.modify(itemStack, nbt -> {
                            nbt.setString("CRAFTITEM_COMMAND", message);
                        });
                        inv.addItem(itemStack);
                        manager.plugin.getScheduler().runTask(() -> {
                            player.closeInventory();
                            player.openInventory(inv);
                            isChat.set(false);
                        });
                    });
                }, inv -> { // onClose: return true -> unregister handlers
                    if (isChat.get()) return false;
                    List<String> commands = new ArrayList<>();
                    for (ItemStack itemStack : inv) {
                        if (itemStack == null || itemStack.getType().equals(Material.AIR)) continue;
                        String command = NBT.get(itemStack, nbt -> {
                            return nbt.getString("CRAFTITEM_COMMAND");
                        });
                        if (command != null && !command.isEmpty()) {
                            commands.add(command);
                        }
                    }
                    craftData.setCommands(commands);
                    manager.save(getId(), craftData);
                    reopen();
                    return true;
                });

                break;
            }
            case TIME: { // 锻造时长
                if (event.isLeftClick()) {
                    craftData.setTime(craftData.getTime() + (event.isShiftClick() ? 600 : 60));
                    manager.save(getId(), craftData);
                } else if (event.isRightClick()) {
                    craftData.setTime(Math.max(0, craftData.getTime() - (event.isShiftClick() ? 600 : 60)));
                    manager.save(getId(), craftData);
                } else if (event.getClick().equals(ClickType.DROP)) {
                    player.closeInventory();
                    Message.gui__edit_time_cost_sum__tips.tm(player);
                    Prompter.onChat(player, message -> {
                        char type = Character.toUpperCase(message.charAt(0));
                        String args = message.substring(1);
                        switch (type) {
                            case 'M': {
                                int cost = Util.parseInt(args).orElse(-1);
                                if (cost < 0) {
                                    Message.not_integer.tm(player);
                                } else {
                                    craftData.setTimeCost(cost);
                                    manager.save(getId(), craftData);
                                }
                                break;
                            }
                            case 'L': {
                                int cost = Util.parseInt(args).orElse(-1);
                                if (cost < 0) {
                                    Message.not_integer.tm(player);
                                } else {
                                    craftData.setTimeCostLevel(cost);
                                    manager.save(getId(), craftData);
                                }
                                break;
                            }
                            default: {
                                Message.gui__edit_time_cost_sum__wrong_type.tm(player);
                                break;
                            }
                        }
                        reopen();
                    });
                    break;
                }
                Slot.TIME.setItem(event.getView().getTopInventory(), this);
                Util.submitInvUpdate(player);
                break;
            }
            case TIME_LIMIT: { // 锻造次数限制
                if (event.isLeftClick()) {
                    int size = 9;
                    List<ItemStack> items = new ArrayList<>();
                    Map<String, Map<String, Integer>> groups = manager.plugin.config().getCountLimitGroups();
                    for (Map.Entry<String, Map<String, Integer>> entry : groups.entrySet()) {
                        String group = entry.getKey();
                        Map<String, Integer> map = entry.getValue();
                        List<String> lore = new ArrayList<>();
                        lore.add("");
                        for (Map.Entry<String, Integer> e : map.entrySet()) {
                            lore.add("§f" + e.getKey() + "§7 : §e" + e.getValue());
                        }
                        items.add(Utils.getItemStack(Material.PAPER, group, lore));
                    }
                    while (size < items.size()) {
                        size += 9;
                        if (size >= 54) break;
                    }
                    Message title = event.isShiftClick()
                            ? Message.gui__edit_time_limit_count_title_time
                            : Message.gui__edit_time_limit_count_title_normal;
                    Prompter.gui(player, size, title, inv -> {
                        int invSize = inv.getSize();
                        for (int i = 0; i < invSize && i < items.size(); i++) {
                            inv.setItem(i, items.get(i));
                        }
                    }, e -> {
                        Inventory inv = e.getClickedInventory();
                        if (inv == null || !(inv.getHolder() instanceof Prompter)) return;
                        ItemStack item = e.getCurrentItem();
                        if (item == null || !item.getType().equals(Material.PAPER)) return;
                        ItemMeta meta = item.getItemMeta();
                        if (meta == null) return;
                        String group = meta.getDisplayName();
                        if (!groups.containsKey(group)) return;

                        if (event.isShiftClick()) {
                            craftData.setTimeCountLimit(group);
                        } else {
                            craftData.setCountLimit(group);
                        }
                        manager.save(getId(), craftData);
                        player.closeInventory();
                    }, inv -> {
                        reopen();
                    });
                    return;
                }
                if (event.isRightClick()) {
                    if (event.isShiftClick()) {
                        craftData.setTimeCountLimit("");
                    } else {
                        craftData.setCountLimit("");
                    }
                    manager.save(getId(), craftData);
                    Slot.TIME_LIMIT.setItem(event.getView().getTopInventory(), this);
                    Util.submitInvUpdate(player);
                }
                break;
            }
            case DIFFICULT: { // 困难锻造
                craftData.setDifficult(!craftData.isDifficult());
                Slot.DIFFICULT.setItem(event.getView().getTopInventory(), this);
                Util.submitInvUpdate(player);
                manager.save(getId(), craftData);
                break;
            }
            case FAIL_TIMES: { // 保底次数
                if (event.isLeftClick()) {
                    craftData.setGuaranteeFailTimes(craftData.getGuaranteeFailTimes() + (event.isShiftClick() ? 10 : 1));
                    manager.save(getId(), craftData);
                } else if (event.isRightClick()) {
                    craftData.setGuaranteeFailTimes(Math.max(0, craftData.getGuaranteeFailTimes() - (event.isShiftClick() ? 10 : 1)));
                    manager.save(getId(), craftData);
                }
                Slot.FAIL_TIMES.setItem(event.getView().getTopInventory(), this);
                Util.submitInvUpdate(player);
                break;
            }
            case COMBO: { // 连击次数
                if (event.isLeftClick()) {
                    craftData.setCombo(craftData.getCombo() + (event.isShiftClick() ? 10 : 1));
                    manager.save(getId(), craftData);
                } else if (event.isRightClick()) {
                    craftData.setCombo(Math.max(0, craftData.getCombo() - (event.isShiftClick() ? 10 : 1)));
                    manager.save(getId(), craftData);
                }
                Slot.COMBO.setItem(event.getView().getTopInventory(), this);
                Util.submitInvUpdate(player);
                break;
            }
        }
    }
}
