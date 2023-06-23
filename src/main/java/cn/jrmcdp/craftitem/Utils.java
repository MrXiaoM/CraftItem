package cn.jrmcdp.craftitem;

import java.util.*;

import cn.jrmcdp.craftitem.config.Material;
import org.bukkit.inventory.ItemStack;

public class Utils {

    public static List<String> itemToListString(Collection<ItemStack> collection) {
        List<String> list = new ArrayList<>();
        for (ItemStack itemStack : collection) {
            list.add("§a" + Material.getItemName(itemStack) + "§fx" + itemStack.getAmount());
        }
        return list;
    }

    public static Map<ItemStack, Integer> getAmountMap(List<ItemStack> list) {
        Map<ItemStack, Integer> map = new HashMap<>();
        for (ItemStack itemStack : list) {
            ItemStack item = itemStack.clone();
            item.setAmount(1);
            if (map.containsKey(item)) {
                map.put(item, map.get(item) + itemStack.getAmount());
                continue;
            }
            map.put(item, itemStack.getAmount());
        }
        return map;
    }

    public static Integer tryParseInt(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
