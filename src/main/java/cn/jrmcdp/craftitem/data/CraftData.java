package cn.jrmcdp.craftitem.data;

import cn.jrmcdp.craftitem.Utils;

import java.util.*;

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
    public CraftData() {
        this(new ArrayList<>(), 75, Arrays.asList(5, 10, 20), 188, new ItemStack(Material.COBBLESTONE), new ArrayList<>(), new ArrayList<>(), 0, 0, false);
    }

    public CraftData(List<ItemStack> material, int chance, List<Integer> multiple, int cost, ItemStack displayItem, List<ItemStack> items, List<String> commands, long time, int timeCost, boolean difficult) {
        this.material = material;
        this.chance = chance;
        this.multiple = multiple;
        this.cost = cost;
        this.displayItem = displayItem;
        this.items = items;
        this.commands = commands;
        this.time = time;
        this.timeCost = timeCost;
        this.difficult = difficult;
    }

    public String getTimeDisplay() {
        return getTimeDisplay(time);
    }
    public static String getTimeDisplay(long second) {
        int hour = 0, minute = 0;
        while (second >= 86400) {
            second -= 86400;
            hour++;
        }
        while (second >= 60) {
            second -= 60;
            minute++;
        }
        return (hour > 0 ? (hour + "时 ") : "")
                + (minute > 0 || hour > 0 ? (minute + "分 ") : "")
                + (second > 0 ? (second + "秒") : "0");
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
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

    public boolean hasMaterial(Inventory gui) {
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
        return map;
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public static CraftData deserialize(Map<String, Object> map) {
        return new CraftData(
                (map.get("Material") == null) ? new ArrayList<>() : (List<ItemStack>)map.get("Material"),
                (map.get("Chance") == null) ? 0 : (Integer) map.get("Chance"),
                (map.get("Multiple") == null) ? new ArrayList<>() : (List<Integer>)map.get("Multiple"),
                (map.get("Cost") == null) ? 0 : (Integer) map.get("Cost"),
                (map.get("DisplayItem") == null) ? new ItemStack(Material.BARRIER) : (ItemStack)map.get("DisplayItem"),
                (map.get("Items") == null) ? new ArrayList<>() : (List<ItemStack>)map.get("Items"),
                (map.get("Commands") == null) ? new ArrayList<>() : (List<String>)map.get("Commands"),
                (map.get("TimeSecond") == null) ? 0 : (Long) map.get("TimeSecond"),
                (map.get("TimeCost") == null) ? 0 : (Integer) map.get("TimeCost"),
                map.get("Difficult") != null && (Boolean) map.get("Difficult"));
    }
}
