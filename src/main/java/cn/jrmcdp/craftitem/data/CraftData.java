package cn.jrmcdp.craftitem.data;

import cn.jrmcdp.craftitem.Utils;

import java.util.*;
import java.util.function.Supplier;

import cn.jrmcdp.craftitem.event.MaterialDisappearEvent;
import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CraftData implements ConfigurationSerializable {
    private List<ItemStack> material;

    private int chance;

    private List<Integer> multiple;

    private int cost;

    private ItemStack displayItem;

    private List<ItemStack> items;

    private List<String> commands;
    /**
     * 锻造时长 (秒)
     */
    private long time;
    private int timeCost;
    private boolean difficult;
    private int guaranteeFailTimes;
    private int combo;
    public CraftData() {
        this(new ArrayList<>(), 75, Arrays.asList(5, 10, 20), 188, new ItemStack(Material.COBBLESTONE), new ArrayList<>(), new ArrayList<>(), 0, 0, false, 0, 0);
    }

    public CraftData(List<ItemStack> material, int chance, List<Integer> multiple, int cost, ItemStack displayItem, List<ItemStack> items, List<String> commands, long time, int timeCost, boolean difficult, int guaranteeFailTimes, int combo) {
        this.material = material;
        this.chance = chance;
        this.multiple = multiple;
        if (multiple.isEmpty()) throw new IllegalArgumentException("multiple can not be empty!");
        Integer i = null;
        while (multiple.size() < 3) {
            if (i == null) {
                i = multiple.get(multiple.size() - 1);
            }
            multiple.add(i);
        }
        this.cost = cost;
        this.displayItem = displayItem;
        this.items = items;
        this.commands = commands;
        this.time = time;
        this.timeCost = timeCost;
        this.difficult = difficult;
        this.guaranteeFailTimes = guaranteeFailTimes;
        this.combo = combo;
    }

    public String getTimeDisplay() {
        return getTimeDisplay(time, "无");
    }
    public String getTimeDisplay(String noneTips) {
        return getTimeDisplay(time, noneTips);
    }
    public static String getTimeDisplay(long second, String noneTips) {
        int hour = 0, minute = 0;
        while (second >= 3600) {
            second -= 3600;
            hour++;
        }
        while (second >= 60) {
            second -= 60;
            minute++;
        }
        return (hour > 0 ? (hour + "时 ") : "")
                + (minute > 0 ? (minute + "分 ") : "")
                + (second > 0 ? (second + "秒") : (minute > 0 || hour > 0 ? "" : noneTips));
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time < 0 ? 0 : time;
    }

    public int getTimeCost() {
        return timeCost;
    }

    public void setTimeCost(int timeCost) {
        this.timeCost = timeCost;
    }

    public boolean isDifficult() {
        return difficult;
    }

    public void setDifficult(boolean difficult) {
        this.difficult = difficult;
    }

    public int getGuaranteeFailTimes() {
        return guaranteeFailTimes;
    }

    public void setGuaranteeFailTimes(int guaranteeFailTimes) {
        this.guaranteeFailTimes = guaranteeFailTimes;
    }

    public int getCombo() {
        return combo;
    }

    public void setCombo(int combo) {
        this.combo = combo;
    }

    public List<ItemStack> getMaterial() {
        return this.material;
    }

    public void setMaterial(List<ItemStack> material) {
        this.material = material;
    }

    public int getChance() {
        return this.chance;
    }

    public void setChance(int chance) {
        this.chance = chance;
    }

    public List<Integer> getMultiple() {
        return this.multiple;
    }

    public void setMultiple(List<Integer> multiple) {
        this.multiple = multiple;
    }

    public int getCost() {
        return this.cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public ItemStack getDisplayItem() {
        return this.displayItem;
    }

    public List<ItemStack> getItems() {
        return this.items;
    }

    public List<String> getCommands() {
        return this.commands;
    }

    public void setItems(List<ItemStack> items) {
        this.items = items;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    public void setDisplayItem(ItemStack displayItem) {
        this.displayItem = displayItem;
    }

    public boolean hasAllMaterial(Inventory gui) {
        Map<ItemStack, Integer> amountMap = Utils.getAmountMap(this.material);
        for (Map.Entry<ItemStack, Integer> entry : amountMap.entrySet()) {
            if (!gui.containsAtLeast(entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    public ItemStack takeRandomMaterial(Player player, Inventory gui) {
        ItemStack item = this.material.get(RandomUtils.nextInt(this.material.size()));
        ItemStack clone = item.clone();
        clone.setAmount(1);

        MaterialDisappearEvent event = new MaterialDisappearEvent(player, this, item, clone);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return null;
        clone = event.getItemToDisappear();

        gui.removeItem(clone);
        return clone;
    }

    public void takeAllMaterial(Inventory gui) {
        gui.removeItem(this.material.toArray(new ItemStack[0]));
    }
    @NotNull
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("Material", this.material);
        map.put("Chance", this.chance);
        map.put("Multiple", this.multiple);
        map.put("Cost", this.cost);
        map.put("DisplayItem", this.displayItem);
        map.put("Items", this.items);
        map.put("Commands", this.commands);
        map.put("TimeSecond", this.time);
        map.put("TimeCost", this.timeCost);
        map.put("Difficult", this.difficult);
        map.put("GuaranteeFailTimes", this.guaranteeFailTimes);
        map.put("Combo", this.combo);
        return map;
    }

    @NotNull
    @SuppressWarnings("unused")
    public static CraftData deserialize(Map<String, Object> map) {
        return new CraftData(
                get(map, "Material", ArrayList::new), // List<ItemStack>
                get(map, "Chance", () -> 0),
                get(map, "Multiple", ArrayList::new), // List<Integer>
                get(map, "Cost", () -> 0),
                get(map, "DisplayItem", () -> new ItemStack(Material.BARRIER)),
                get(map, "Items", ArrayList::new), // List<ItemStack>
                get(map, "Commands", ArrayList::new), // List<String>
                Long.parseLong(get(map, "TimeSecond", () -> "0")),
                get(map, "TimeCost", () -> 0),
                get(map, "Difficult", () -> false),
                get(map, "GuaranteeFailTimes", () -> 0),
                get(map, "Combo", () -> 0)
        );
    }
    
    @SuppressWarnings("unchecked")
    private static <T> T get(Map<String, Object> map, String key, Supplier<T> def) {
        return map.get(key) == null ? def.get() : (T) map.get(key);
    }
}
