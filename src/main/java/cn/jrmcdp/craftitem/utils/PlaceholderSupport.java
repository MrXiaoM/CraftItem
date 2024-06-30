package cn.jrmcdp.craftitem.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PlaceholderSupport {
    private static boolean isSupport;
    public static void init() {
        isSupport = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }
    public static String setPlaceholders(Player player, String s) {
        if (!isSupport) {
            return s.replace("%player_name%", player.getName());
        }
        return PlaceholderAPI.setPlaceholders(player, s);
    }
    public static List<String> setPlaceholders(Player player, List<String> s) {
        if (!isSupport) {
            List<String> list = new ArrayList<>(s);
            list.replaceAll(it -> it.replace("%player_name%", player.getName()));
            return list;
        }
        return PlaceholderAPI.setPlaceholders(player, s);
    }
}
