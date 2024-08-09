package cn.jrmcdp.craftitem.minigames.game;

import org.bukkit.configuration.ConfigurationSection;

public interface GameFactory {

    GameInstance setArgs(ConfigurationSection section);

}