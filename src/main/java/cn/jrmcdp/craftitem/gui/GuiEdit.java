package cn.jrmcdp.craftitem.gui;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.config.ConfigMain;
import cn.jrmcdp.craftitem.manager.CraftDataManager;
import cn.jrmcdp.craftitem.config.ConfigForgeGui;
import cn.jrmcdp.craftitem.config.Message;
import cn.jrmcdp.craftitem.data.CraftData;
import cn.jrmcdp.craftitem.utils.Prompter;
import cn.jrmcdp.craftitem.utils.Utils;
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
import top.mrxiaom.pluginbase.utils.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static cn.jrmcdp.craftitem.utils.Utils.*;

public class GuiEdit implements IHolder {
    private final String id;

    private final CraftData craftData;
    private Inventory inventory;
    private int invSize = 0;

    private final Player player;
    private final CraftDataManager manager = CraftDataManager.inst();

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

    @Override
    public Inventory newInventory() {
        ItemStack[] items = new ItemStack[invSize = 18];
        inventory = CraftItem.getInventoryFactory().create(this, items.length, Message.gui__edit_title.str(this.id));
        items[0] = item0();
        items[1] = item1();
        items[2] = item2();
        items[3] = item3();
        items[4] = item4();
        items[5] = item5();
        items[6] = item6();
        items[7] = item7();
        items[8] = item8();
        items[9] = item9();
        items[10] = item10();
        items[11] = item11();
        inventory.setContents(items);
        return inventory;
    }

    private ItemStack item0() {
        return getItemStack(getMaterial("WHEAT"), Message.gui__edit__item__material__name.str(),
                Message.gui__edit__item__material__lore.list(
                        String.join("\n§7", Utils.itemToListString(craftData.getMaterial()))
                ));
    }

    private ItemStack item1() {
        return getItemStack(getMaterial("COMPASS"), Message.gui__edit__item__successful_rate__name.str(),
                Message.gui__edit__item__successful_rate__lore.list(
                        craftData.getChance()
                ));
    }

    private ItemStack item2() {
        return getItemStack(getMaterial("HOPPER"), Message.gui__edit__item__multiple__name.str(),
                Message.gui__edit__item__multiple__lore.list(
                        craftData.getMultiple().stream().map(String::valueOf).collect(Collectors.joining(" "))
                ));
    }

    private ItemStack item3() {
        return getItemStack(getMaterial("GOLD_INGOT"), Message.gui__edit__item__cost__name.str(),
                Message.gui__edit__item__cost__lore.list(
                        craftData.getCost()
                ));
    }

    private ItemStack item4() {
        return getItemStack(getMaterial("PAINTING"), Message.gui__edit__item__display__name.str(),
                Message.gui__edit__item__display__lore.list(
                        craftData.getDisplayItem()
                ));
    }

    private ItemStack item5() {
        return getItemStack(getMaterial("CHEST"), Message.gui__edit__item__item__name.str(),
                Message.gui__edit__item__item__lore.list(
                        String.join("\n§7", Utils.itemToListString(craftData.getItems()))
                ));
    }

    private ItemStack item6() {
        return getItemStack(getMaterial("PAPER"), Message.gui__edit__item__command__name.str(),
                Message.gui__edit__item__command__lore.list(
                        String.join("\n§7", craftData.getCommands())
                ));
    }

    private ItemStack item7() {
        return getItemStack(getMaterial("CLOCK", "WATCH"), Message.gui__edit__item__time__name.str(),
                Message.gui__edit__item__time__lore.list(
                        craftData.getTimeDisplay(), craftData.getTimeCost()
                ));
    }

    private ItemStack item8() {
        String groupTime = craftData.getTimeCountLimit();
        if (groupTime.trim().isEmpty()) groupTime = Message.gui__edit__unset.str();
        String groupNormal = craftData.getCountLimit();
        if (groupNormal.trim().isEmpty()) groupNormal = Message.gui__edit__unset.str();
        return getItemStack(getMaterial("BUCKET"), Message.gui__edit__item__time_count_limit__name.str(),
                Message.gui__edit__item__time_count_limit__lore.list(groupNormal, groupTime));
    }

    private ItemStack item9() {
        return getItemStack(getMaterial("FISHING_ROD"), Message.gui__edit__item__difficult__name.str(),
                Message.gui__edit__item__difficult__lore.list(
                        (craftData.isDifficult() ? Message.gui__edit__status__on : Message.gui__edit__status__off).str()
                ));
    }
    private ItemStack item10() {
        return getItemStack(getMaterial("BOWL"), Message.gui__edit__item__fail_times__name.str(),
                Message.gui__edit__item__fail_times__lore.list(
                        craftData.getGuaranteeFailTimes() > 0 ? String.valueOf(craftData.getGuaranteeFailTimes()) : Message.gui__edit__unset.str()
                ));
    }

    private ItemStack item11() {
        return getItemStack(getMaterial("MAGMA_CREAM"), Message.gui__edit__item__combo__name.str(),
                Message.gui__edit__item__combo__lore.list(
                        craftData.getCombo() > 0 ? String.valueOf(craftData.getCombo()) : Message.gui__edit__unset.str()
                ));
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

    @Override
    public void onClick(
            InventoryAction action, ClickType click,
            InventoryType.SlotType slotType, int slot,
            ItemStack currentItem, ItemStack cursor,
            InventoryView view, InventoryClickEvent event
    ) {
        manager.plugin.config().playSoundClickInventory(player);
        if (event.getRawSlot() < 0 || event.getRawSlot() >= invSize) return;
        final CraftData craftData = getCraftData();
        switch (event.getRawSlot()) {
            case 0: { // 材料
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
                    open();
                });
                break;
            }
            case 1: { // 成功率
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
                    open();
                });
                break;
            }
            case 2: { // 倍数
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
                            open();
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
                    open();
                });
                Prompter.onChat(player, consumeChat);
                break;
            }
            case 3: { // 价格
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
                    open();
                });
                break;
            }
            case 4: { // 显示物品
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
                    open();
                });
                break;
            }
            case 5: { // 奖励物品
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
                    open();
                });
                break;
            }
            case 6: { // 奖励命令
                AtomicBoolean isChat = new AtomicBoolean(false);
                Message title = Message.gui__edit_command_title;
                Prompter.gui(player, 54, title, inv -> { // init
                    for (String line : craftData.getCommands()) {
                        inv.addItem(Utils.getItemStack(Material.PAPER, line, Message.gui__edit_command_lore.list()));
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
                                message.replace("&", "§"),
                                Message.gui__edit_command_lore.list()
                        );
                        inv.addItem(itemStack);
                        open();
                        isChat.set(false);
                    });
                }, inv -> { // onClose
                    if (isChat.get()) return false;
                    List<String> commands = new ArrayList<>();
                    for (ItemStack itemStack : inv) {
                        if (itemStack == null || itemStack.getType().equals(Material.AIR)) continue;
                        ItemMeta meta = itemStack.getItemMeta();
                        if (meta == null || !meta.hasDisplayName()) continue;
                        commands.add(meta.getDisplayName());
                    }
                    craftData.setCommands(commands);
                    manager.save(getId(), craftData);
                    open();
                    return true;
                });

                break;
            }
            case 7: { // 锻造时长
                if (event.isLeftClick()) {
                    craftData.setTime(craftData.getTime() + (event.isShiftClick() ? 600 : 60));
                    manager.save(getId(), craftData);
                } else if (event.isRightClick()) {
                    craftData.setTime(Math.max(0, craftData.getTime() - (event.isShiftClick() ? 600 : 60)));
                    manager.save(getId(), craftData);
                } else if (event.getClick().equals(ClickType.DROP)) {
                    player.closeInventory();
                    Message.gui__edit_time_cost.tm(player);
                    Prompter.onChat(player, message -> {
                        Integer cost = Util.parseInt(message).orElse(null);
                        if (cost == null || cost < 0) {
                            Message.not_integer.tm(player);
                        } else {
                            craftData.setTimeCost(cost);
                            manager.save(getId(), craftData);
                        }
                        open();
                    });
                    break;
                }
                event.getView().getTopInventory().setItem(7, item7());
                Util.submitInvUpdate(player);
                break;
            }
            case 8: { // 锻造次数限制
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
                        open();
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
                    event.getView().getTopInventory().setItem(8, item8());
                    Util.submitInvUpdate(player);
                }
                break;
            }
            case 9: { // 困难锻造
                craftData.setDifficult(!craftData.isDifficult());
                event.getView().getTopInventory().setItem(9, item9());
                Util.submitInvUpdate(player);
                manager.save(getId(), craftData);
                break;
            }
            case 10: { // 保底次数
                if (event.isLeftClick()) {
                    craftData.setGuaranteeFailTimes(craftData.getGuaranteeFailTimes() + (event.isShiftClick() ? 10 : 1));
                    manager.save(getId(), craftData);
                } else if (event.isRightClick()) {
                    craftData.setGuaranteeFailTimes(Math.max(0, craftData.getGuaranteeFailTimes() - (event.isShiftClick() ? 10 : 1)));
                    manager.save(getId(), craftData);
                }
                event.getView().getTopInventory().setItem(10, item10());
                Util.submitInvUpdate(player);
                break;
            }
            case 11: { // 连击次数
                if (event.isLeftClick()) {
                    craftData.setCombo(craftData.getCombo() + (event.isShiftClick() ? 10 : 1));
                    manager.save(getId(), craftData);
                } else if (event.isRightClick()) {
                    craftData.setCombo(Math.max(0, craftData.getCombo() - (event.isShiftClick() ? 10 : 1)));
                    manager.save(getId(), craftData);
                }
                event.getView().getTopInventory().setItem(11, item11());
                Util.submitInvUpdate(player);
                break;
            }
        }
    }
}
