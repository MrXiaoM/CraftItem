package cn.jrmcdp.craftitem.minigames.utils.game;

import org.bukkit.configuration.ConfigurationSection;

public interface GameFactory {

    GameInstance setArgs(ConfigurationSection section);

}