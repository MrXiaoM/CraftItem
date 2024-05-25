package cn.jrmcdp.craftitem.config;

import cn.jrmcdp.craftitem.ColorHelper;
import cn.jrmcdp.craftitem.minigames.utils.Pair;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Icon {
    private final String key;
    private final org.bukkit.Material material;
    private final int amount;
    private final String name;
    private final List<String> lore;
    private final Integer customModelData;
    private final List<String> leftClick;
    private final List<String> rightClick;
    private final List<String> shiftLeftClick;
    private final List<String> shiftRightClick;

    public Icon(String key, org.bukkit.Material material, int amount, String name, List<String> lore, Integer customModelData, List<String> leftClick, List<String> rightClick, List<String> shiftLeftClick, List<String> shiftRightClick) {
        this.key = key;
        this.material = material;
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
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        if (name != null) meta.setDisplayName(name);
        if (!lore.isEmpty()) {
            List<String> lore = new ArrayList<>();
            for (String s : PlaceholderAPI.setPlaceholders(player, this.lore)) {
                for (Pair<String, Object> pair : replacements) {
                    if (s.contains(pair.getKey())) {
                        s = s.replace(pair.getKey(), pair.getValue().toString());
                    }
                }
                lore.add(s);
            }
            meta.setLore(lore);
        }
        if (customModelData != null) {
            meta.setCustomModelData(customModelData);
        }
        item.setItemMeta(meta);
        return item;
    }

    public void leftClick(Player player) {
        runCommands(player, getLeftClick());
    }

    public void rightClick(Player player) {
        runCommands(player, getRightClick());
    }

    public void shiftLeftClick(Player player) {
        runCommands(player, getShiftLeftClick());
    }

    public void shiftRightClick(Player player) {
        runCommands(player, getShiftRightClick());
    }

    public String getKey() {
        return key;
    }

    public List<String> getLeftClick() {
        return leftClick;
    }

    public List<String> getRightClick() {
        return rightClick;
    }

    public List<String> getShiftLeftClick() {
        return shiftLeftClick;
    }

    public List<String> getShiftRightClick() {
        return shiftRightClick;
    }


    public static void runCommands(Player player, List<String> commands) {
        if (commands == null || commands.isEmpty()) return;
        commands = PlaceholderAPI.setPlaceholders(player, commands);
        for (String s : commands) {
            if (s.startsWith("[player]")) {
                Bukkit.dispatchCommand(player, s.substring(8).trim());
            } else if (s.startsWith("[console]")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.substring(9).trim());
            } else if (s.startsWith("[message]")) {
                player.sendMessage(ColorHelper.parseColor(s.substring(9).trim()));
            } else if (s.startsWith("[close]")) {
                player.closeInventory();
            }
        }
    }

}
