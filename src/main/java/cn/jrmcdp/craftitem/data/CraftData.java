package cn.jrmcdp.craftitem.data;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.config.ConfigMain;
import cn.jrmcdp.craftitem.config.Message;
import cn.jrmcdp.craftitem.currency.ICurrency;
import cn.jrmcdp.craftitem.event.MaterialDisappearEvent;
import cn.jrmcdp.craftitem.func.MaterialAdapterManager;
import cn.jrmcdp.craftitem.utils.RandomUtils;
import cn.jrmcdp.craftitem.utils.Utils;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.Pair;

import java.util.*;
import java.util.function.Supplier;

import static cn.jrmcdp.craftitem.utils.Utils.takeItem;

public class CraftData implements ConfigurationSerializable {
    public static class MaterialState {
        /**
         * 材料物品
         */
        public final MaterialInstance item;
        /**
         * 玩家当前拥有的材料数量
         */
        public final int amount;
        /**
         * 玩家所需的材料数量
         */
        public final int target;

        public MaterialState(MaterialInstance item, int amount, int target) {
            this.item = item;
            this.amount = amount;
            this.target = target;
        }

        public boolean isEnough() {
            return amount >= target;
        }
    }
    private final CraftItem plugin;
    private final ConfigMain config;
    /**
     * 原始材料物品
     */
    private List<ItemStack> material;
    /**
     * 已加载、经过处理的材料匹配器
     */
    private List<MaterialInstance> loadedMaterial;
    /**
     * 单次锻造成功几率，0-100
     */
    private int chance;
    /**
     * 锻造程度，在成功/失败时，对应增加/减少的进度。这个列表的长度必须为3
     * <ul>
     *   <li><code>index=0</code> 程度为小，如 小成功、小失败</li>
     *   <li><code>index=1</code> 程度为中等，如 成功、失败</li>
     *   <li><code>index=2</code> 程度为大，如 大成功、大失败</li>
     * </ul>
     */
    private List<Integer> multiple;
    /**
     * 单次锻造需要花费的货币类型
     */
    private ICurrency costCurrency;
    /**
     * 单次锻造需要花费的金钱数量
     */
    private int cost;
    /**
     * 单次锻造需要花费的经验等级
     */
    private int costLevel;
    /**
     * 锻造界面显示的图标物品
     */
    private ItemStack displayItem;
    /**
     * 锻造成功给予的物品
     */
    private List<ItemStack> items;
    /**
     * 锻造成功执行的命令列表
     */
    private List<String> commands;
    /**
     * 时长锻造所需的时间 (秒)
     */
    private long time;
    /**
     * 时长锻造需要花费的货币类型
     */
    private ICurrency timeCostCurrency;
    /**
     * 时长锻造需要花费的金钱数量
     */
    private int timeCost;
    /**
     * 时长锻造需要花费的经验等级
     */
    private int timeCostLevel;
    /**
     * 是否开启困难锻造
     */
    private boolean difficult;
    /**
     * 锻造保底次数
     */
    private int guaranteeFailTimes;
    /**
     * 锻造连击次数
     */
    private int combo;
    /**
     * 普通锻造/困难锻造 的可用次数限制组
     */
    private String countLimit;
    /**
     * 时长锻造 的可用次数限制组
     */
    private String timeCountLimit;
    public CraftData() {
        this(new ArrayList<>(), 75, Arrays.asList(5, 10, 20), vault(), 188, 0, new ItemStack(Material.COBBLESTONE), new ArrayList<>(), new ArrayList<>(), 0, vault(), 0, 0, false, 0, 0, "", "");
    }

    @ApiStatus.Internal
    public CraftData(List<ItemStack> material, int chance, List<Integer> multiple, ICurrency costCurrency, int cost, int costLevel, ItemStack displayItem, List<ItemStack> items, List<String> commands, long time, ICurrency timeCostCurrency, int timeCost, int timeCostLevel, boolean difficult, int guaranteeFailTimes, int combo, String countLimit, String timeCountLimit) {
        this.config = ConfigMain.inst();
        this.plugin = config.plugin;
        this.setMaterial(material);
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
        this.costCurrency = costCurrency;
        this.cost = cost;
        this.costLevel = costLevel;
        this.displayItem = displayItem;
        this.items = items;
        this.commands = commands;
        this.time = time;
        this.timeCostCurrency = timeCostCurrency;
        this.timeCost = timeCost;
        this.timeCostLevel = timeCostLevel;
        this.difficult = difficult;
        this.guaranteeFailTimes = guaranteeFailTimes;
        this.combo = combo;
        this.countLimit = countLimit;
        this.timeCountLimit = timeCountLimit;
    }

    public CraftItem plugin() {
        return plugin;
    }

    public String getTimeDisplay() {
        return getTimeDisplay("无");
    }
    public String getTimeDisplay(String noneTips) {
        return config.getTimeDisplay(time, noneTips);
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

    @Nullable
    public ICurrency getTimeCostCurrency() {
        return timeCostCurrency;
    }

    @NotNull
    public String getTimeCostCurrencyName() {
        return timeCostCurrency == null ? "" : timeCostCurrency.getName();
    }

    public void setTimeCostCurrency(@Nullable ICurrency timeCostCurrency) {
        this.timeCostCurrency = timeCostCurrency;
    }

    public int getTimeCostLevel() {
        return timeCostLevel;
    }

    public void setTimeCostLevel(int timeCostLevel) {
        this.timeCostLevel = timeCostLevel;
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

    public String getCountLimit() {
        return countLimit;
    }

    public void setCountLimit(String countLimit) {
        this.countLimit = countLimit;
    }

    public String getTimeCountLimit() {
        return timeCountLimit;
    }

    public void setTimeCountLimit(String timeCountLimit) {
        this.timeCountLimit = timeCountLimit;
    }

    public List<ItemStack> getMaterial() {
        return this.material;
    }

    public ItemStack[] getMaterialArray() {
        ItemStack[] array = new ItemStack[material.size()];
        for (int i = 0; i < material.size(); i++) {
            array[i] = material.get(i);
        }
        return array;
    }

    public void setMaterial(List<ItemStack> material) {
        this.material = material;
        this.loadedMaterial = MaterialAdapterManager.inst().fromMaterials(material);
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

    @Nullable
    public ICurrency getCostCurrency() {
        return costCurrency;
    }

    @NotNull
    public String getCostCurrencyName() {
        return costCurrency == null ? "" : costCurrency.getName();
    }

    public void setCostCurrency(@Nullable ICurrency costCurrency) {
        this.costCurrency = costCurrency;
    }

    public int getCostLevel() {
        return this.costLevel;
    }

    public void setCostLevel(int costLevel) {
        this.costLevel = costLevel;
    }

    public ItemStack getDisplayItem() {
        return this.displayItem;
    }

    public List<ItemStack> getItems() {
        List<ItemStack> list = new ArrayList<>();
        for (ItemStack item : items) {
            list.add(item.clone());
        }
        return list;
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

    public int getTimeForgeCountLimit(Player player) {
        return config.getCountLimit(player, getTimeCountLimit());
    }

    public int getForgeCountLimit(Player player) {
        return config.getCountLimit(player, getCountLimit());
    }

    public boolean isNotEnoughMaterial(Player player) {
        List<MaterialState> state = getMaterialState(player, player.getInventory());
        state.removeIf(MaterialState::isEnough);
        if (!state.isEmpty()) {
            Message.craft__not_enough_material.tm(player);
            for (MaterialState entry : state) {
                Message.craft__not_enough_material_details.tmf(player, Utils.getItemName(entry.item.getSample(), player), entry.amount, entry.target);
            }
            return true;
        }
        return false;
    }

    public List<MaterialInstance> getLoadedMaterial() {
        return Collections.unmodifiableList(this.loadedMaterial);
    }

    @Deprecated
    public List<MaterialState> getMaterialState(Inventory gui) {
        return getMaterialState(null, gui);
    }
    public List<MaterialState> getMaterialState(Player player, Inventory gui) {
        List<MaterialState> list = new ArrayList<>();
        Map<MaterialInstance, Integer> amountMap = Utils.getAmountMap(this.loadedMaterial);
        for (Map.Entry<MaterialInstance, Integer> entry : amountMap.entrySet()) {
            int amount = 0;
            for (ItemStack i : gui.getContents()) {
                if (entry.getKey().getAdapter().match(player, i)) {
                    amount += i.getAmount();
                }
            }
            list.add(new MaterialState(entry.getKey(), amount, entry.getValue()));
        }
        list.sort(Comparator.comparingInt(it -> it.amount));
        Collections.reverse(list);
        return list;
    }

    public MaterialInstance takeRandomMaterial(Player player) {
        List<MaterialInstance> list = config.filterMaterials(loadedMaterial);
        if (list.isEmpty()) return null;
        MaterialInstance item = list.get(RandomUtils.nextInt(list.size()));
        MaterialInstance.Mutable clone = item.toMutable();
        clone.setAmount(1); // 只拿走一个

        MaterialDisappearEvent event = new MaterialDisappearEvent(player, this, item, clone);
        Bukkit.getPluginManager().callEvent(event);
        MaterialInstance toDisappear = event.getMaterialToDisappear();
        if (event.isCancelled() || toDisappear == null || toDisappear.getAmount() == 0) return null;

        takeItem(player, Lists.newArrayList(toDisappear));
        return toDisappear;
    }

    public void takeAllMaterial(Player player) {
        takeItem(player, loadedMaterial);
    }

    public boolean checkCost(Player player) {
        return checkCost(player, 1);
    }

    public boolean checkCost(Player player, int times) {
        if (costCurrency == null) {
            Message.craft__currency_not_available.tm(player);
            return true;
        }
        if (!costCurrency.has(player, cost * times)) {
            String currencyName = costCurrency.getName();
            Message.craft__not_enough_currency.tm(player, Pair.of("%currency%", currencyName));
            return true;
        }
        if (player.getLevel() < costLevel * times) {
            Message.craft__not_enough_level.tm(player);
            return true;
        }
        return false;
    }

    public boolean doCost(Player player) {
        if (costCurrency == null) {
            Message.craft__currency_not_available.tm(player);
            return true;
        }
        if (!costCurrency.takeMoney(player, cost)) {
            String currencyName = costCurrency.getName();
            Message.craft__not_enough_currency.tm(player, Pair.of("%currency%", currencyName));
            return true;
        }
        int level = player.getLevel();
        if (level < costLevel) {
            Message.craft__not_enough_level.tm(player);
            return true;
        }
        player.setLevel(Math.max(0, level - costLevel));
        return false;
    }

    public boolean checkCostTime(Player player) {
        if (timeCostCurrency == null) {
            Message.craft__currency_not_available.tm(player);
            return true;
        }
        if (!timeCostCurrency.has(player, timeCost)) {
            String currencyName = timeCostCurrency.getName();
            Message.craft__not_enough_currency.tm(player, Pair.of("%currency%", currencyName));
            return true;
        }
        if (player.getLevel() < timeCostLevel) {
            Message.craft__not_enough_level.tm(player);
            return true;
        }
        return false;
    }

    public boolean doCostTime(Player player) {
        if (timeCostCurrency == null) {
            Message.craft__currency_not_available.tm(player);
            return true;
        }
        if (!timeCostCurrency.takeMoney(player, timeCost)) {
            String currencyName = timeCostCurrency.getName();
            Message.craft__not_enough_currency.tm(player, Pair.of("%currency%", currencyName));
            return true;
        }
        int level = player.getLevel();
        if (level < timeCostLevel) {
            Message.craft__not_enough_level.tm(player);
            return true;
        }
        player.setLevel(Math.max(0, level - timeCostLevel));
        return false;
    }

    @NotNull
    public Map<String, Object> serialize() {
        // 注: 配置路径分隔符是 ' '(空格) 不是 '.'(点)
        Map<String, Object> map = new HashMap<>();
        map.put("Material", this.material);
        map.put("Chance", this.chance);
        map.put("Multiple", this.multiple);
        if (this.costCurrency != null) {
            map.put("CostCurrency", this.costCurrency.serialize());
        }
        map.put("Cost", this.cost);
        map.put("CostLevel", this.costLevel);
        map.put("DisplayItem", this.displayItem);
        map.put("Items", this.items);
        map.put("Commands", this.commands);
        map.put("TimeSecond", this.time);
        if (this.timeCostCurrency != null) {
            map.put("TimeCostCurrency", this.timeCostCurrency.serialize());
        }
        map.put("TimeCost", this.timeCost);
        map.put("TimeCostLevel", this.timeCostLevel);
        map.put("Difficult", this.difficult);
        map.put("GuaranteeFailTimes", this.guaranteeFailTimes);
        map.put("Combo", this.combo);
        map.put("CountLimit", this.countLimit);
        map.put("TimeCountLimit", this.timeCountLimit);
        return map;
    }

    @NotNull
    @SuppressWarnings("unused")
    public static CraftData deserialize(Map<String, Object> map) {
        // 注: 配置路径分隔符是 ' '(空格) 不是 '.'(点)
        return new CraftData(
                get(map, "Material", ArrayList::new), // List<ItemStack>
                get(map, "Chance", 0),
                get(map, "Multiple", ArrayList::new), // List<Integer>
                currency(map, "CostCurrency"),
                get(map, "Cost", 0),
                get(map, "CostLevel", 0),
                get(map, "DisplayItem", () -> new ItemStack(Material.BARRIER)),
                get(map, "Items", ArrayList::new), // List<ItemStack>
                get(map, "Commands", ArrayList::new), // List<String>
                Long.parseLong(String.valueOf(get(map, "TimeSecond", "0"))),
                currency(map, "TimeCostCurrency"),
                get(map, "TimeCost", 0),
                get(map, "TimeCostLevel", 0),
                get(map, "Difficult", false),
                get(map, "GuaranteeFailTimes", 0),
                get(map, "Combo", 0),
                get(map, "CountLimit", ""),
                get(map, "TimeCountLimit", "")
        );
    }

    private static <T> T get(Map<String, Object> map, String key, T def) {
        return get(map, key, () -> def);
    }
    @SuppressWarnings("unchecked")
    private static <T> T get(Map<String, Object> map, String key, Supplier<T> def) {
        return map.get(key) == null ? def.get() : (T) map.get(key);
    }
    private static ICurrency currency(Map<String, Object> map, String key) {
        String currencyStr = (String) map.get(key);
        if (currencyStr == null) {
            // 如果没有这个配置，返回 Vault 货币作为缺省值
            return vault();
        }
        // 解析货币类型
        return CraftItem.getPlugin().parseCurrency(currencyStr);
    }
    private static ICurrency vault() {
        return CraftItem.getPlugin().parseCurrency("Vault");
    }
}
