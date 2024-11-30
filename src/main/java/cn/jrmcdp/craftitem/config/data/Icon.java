package cn.jrmcdp.craftitem.config.data;

import cn.jrmcdp.craftitem.minigames.utils.AdventureManagerImpl;
import cn.jrmcdp.craftitem.utils.AdventureItemStack;
import cn.jrmcdp.craftitem.utils.Pair;
import cn.jrmcdp.craftitem.utils.PlaceholderSupport;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Icon {
    public final Material material;
    public final int data;
    public final int amount;
    public final String name;
    public final List<String> lore;
    public final Integer customModelData;
    public final List<String> leftClick;
    public final List<String> rightClick;
    public final List<String> shiftLeftClick;
    public final List<String> shiftRightClick;

    public Icon(Material material, int data, int amount, String name, List<String> lore, Integer customModelData, List<String> leftClick, List<String> rightClick, List<String> shiftLeftClick, List<String> shiftRightClick) {
        this.material = material;
        this.data = data;
        this.amount = amount;
        this.name = name;
        this.lore = lore;
        this.customModelData = customModelData;
        this.leftClick = leftClick;
        this.rightClick = rightClick;
        this.shiftLeftClick = shiftLeftClick;
        this.shiftRightClick = shiftRightClick;
    }

    @SafeVarargs
    public final ItemStack getItem(Player player, Pair<String, Object>... replacements) {
        ItemStack item = data > 0 ? new ItemStack(material, amount, (short) data) : new ItemStack(material, amount);
        if (name != null) AdventureItemStack.setItemDisplayName(item, name);
        if (!lore.isEmpty()) {
            List<String> lore = new ArrayList<>();
            for (String s : PlaceholderSupport.setPlaceholders(player, this.lore)) {
                for (Pair<String, Object> pair : replacements) {
                    if (s.contains(pair.getKey())) {
                        s = s.replace(pair.getKey(), pair.getValue().toString());
                    }
                }
                lore.add(s);
            }
            AdventureItemStack.setItemLore(item, lore);
        }
        if (customModelData != null) AdventureItemStack.setCustomModelData(item, customModelData);
        return item;
    }

    public void leftClick(Player player) {
        runCommands(player, leftClick);
    }

    public void rightClick(Player player) {
        runCommands(player, rightClick);
    }

    public void shiftLeftClick(Player player) {
        runCommands(player, shiftLeftClick);
    }

    public void shiftRightClick(Player player) {
        runCommands(player, shiftRightClick);
    }

    public static void runCommands(Player player, List<String> commands) {
        if (commands == null || commands.isEmpty()) return;
        commands = PlaceholderSupport.setPlaceholders(player, commands);
        for (String s : commands) {
            if (s.startsWith("[player]")) {
                Bukkit.dispatchCommand(player, s.substring(8).trim());
            } else if (s.startsWith("[console]")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.substring(9).trim());
            } else if (s.startsWith("[message]")) {
                AdventureManagerImpl.getInstance().sendMessage(player, s.substring(9).trim());
            } else if (s.startsWith("[close]")) {
                player.closeInventory();
            }
        }
    }
}
