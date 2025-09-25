package cn.jrmcdp.craftitem.minigames.utils;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import top.mrxiaom.pluginbase.utils.AdventureUtil;

import java.time.Duration;

import static top.mrxiaom.pluginbase.utils.AdventureUtil.miniMessage;

public class AdventureManagerImpl {
    public static void sendMessage(CommandSender sender, String message) {
        AdventureUtil.sendMessage(sender, message);
    }

    public static void sendTitle(Player player, String title, String subtitle, int in, int duration, int out) {
        sendTitle(player, miniMessage(title), miniMessage(subtitle), in, duration, out);
    }
    
    public static void sendTitle(Player player, Component title, Component subtitle, int in, int duration, int out) {
        Audience audience = AdventureUtil.of(player);
        Title.Times times = Title.Times.times(
                Duration.ofMillis(in * 50L),
                Duration.ofMillis(duration * 50L),
                Duration.ofMillis(out * 50L)
        );
        audience.showTitle(Title.title(title, subtitle, times));
    }
    
    public static void sendActionbar(Player player, String s) {
        AdventureUtil.sendActionBar(player, s);
    }
    
    public static void sendSound(Player player, Sound.Source source, Key key, float volume, float pitch) {
        Sound sound = Sound.sound(key, source, volume, pitch);
        Audience au = AdventureUtil.of(player);
        au.playSound(sound);
    }
}
