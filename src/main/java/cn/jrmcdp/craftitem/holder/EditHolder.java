package cn.jrmcdp.craftitem.holder;

import cn.jrmcdp.craftitem.data.CraftData;
import cn.jrmcdp.craftitem.Utils;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

public class EditHolder implements InventoryHolder {
    private String id;

    private CraftData craftData;

    public EditHolder(String id, CraftData craftData) {
        this.id = id;
        this.craftData = craftData;
    }

    public String getId() {
        return this.id;
    }

    public CraftData getCraftData() {
        return this.craftData;
    }

    public Inventory buildGui() {
        Inventory inventory = Bukkit.createInventory(this, 9, "编辑 " + this.id);
        inventory.setContents(getItems());
        return inventory;
    }

    private ItemStack[] getItems() {
        return new ItemStack[] {
                getItemStack(Material.WHEAT, "§a材料",
                        Arrays.asList(
                                "§7点击 查看/编辑",
                                "",
                                "§7当前:"
                        ),
                        Utils.itemToListString(craftData.getMaterial())
                ),
                getItemStack(Material.COMPASS, "§a成功率",
                        Arrays.asList(
                                "§7点击 编辑",
                                "§7使用正整数",
                                "",
                                "§7当前: §e" + craftData.getChance()
                        )
                ),
                getItemStack(Material.HOPPER, "§a倍数",
                        Arrays.asList(
                                "§7点击 编辑",
                                "§7格式 \"5 10 20\"",
                                "§7对应 小/中/大 失败/成功 的 涨幅/跌幅",
                                "",
                                "§7当前: §e" + craftData.getMultiple()
                        )
                ),
                getItemStack(Material.GOLD_INGOT, "§a价格",
                        Arrays.asList(
                                "§7点击 查看/编辑",
                                "§7使用正整数",
                                "",
                                "§7当前: §e" + craftData.getCost()
                        )
                ),
                getItemStack(Material.PAINTING, "§a显示物品",
                        Arrays.asList(
                                "§7点击 查看/编辑",
                                "§7对外显示的物品外貌",
                                "",
                                "§7当前: §e" + cn.jrmcdp.craftitem.config.Material.getItemName(craftData.getDisplayItem())
                        )
                ),
                getItemStack(Material.CHEST, "§a奖励物品",
                        Arrays.asList(
                                "§7点击 查看/编辑",
                                "§7锻造成功后给予的物品",
                                "",
                                "§7当前:"
                        ),
                        Utils.itemToListString(craftData.getItems())
                ),
                getItemStack(Material.PAPER, "§a奖励命令",
                        Arrays.asList(
                                "§7点击 查看/编辑",
                                "§7格式 \"say 这个插件太棒了||服务器说这个插件太棒了\"",
                                "§7用 || 分割，左边是命令 右边是显示出来的介绍",
                                "§7此处支持 Papi 占位符 变量",
                                "§7锻造成功后执行的命令",
                                "",
                                "§7当前:"
                        ),
                        craftData.getCommands()
                )
        };
    }

    private ItemStack getItemStack(Material material, String name, List<String>... lores) {
        ItemStack itemStack = new ItemStack(XMaterial.matchXMaterial(material).parseItem());
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(name);
        itemMeta.setLore(Arrays.stream(lores).flatMap(Collection::stream).collect(Collectors.toList()));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public Inventory getInventory() {
        return null;
    }
}
