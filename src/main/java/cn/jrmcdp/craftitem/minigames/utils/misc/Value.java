package cn.jrmcdp.craftitem.minigames.utils.misc;

import org.bukkit.entity.Player;

import java.util.Map;

public interface Value {

    double get(Player player, Map<String, String> values);
}
