package cn.jrmcdp.craftitem.holder;

import cn.jrmcdp.craftitem.Utils;
import cn.jrmcdp.craftitem.config.Message;
import cn.jrmcdp.craftitem.data.CraftData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
}
