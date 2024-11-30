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

import java.time.Duration;

import static cn.jrmcdp.craftitem.utils.MiniMessageConvert.miniMessage;

public class AdventureManagerImpl {
    private final BukkitAudiences adventure;
    private static AdventureManagerImpl instance;

    private AdventureManagerImpl(JavaPlugin plugin) {
        this.adventure = BukkitAudiences.create(plugin);
        instance = this;
    }

    public static void load(JavaPlugin plugin) {
        if (instance == null) {
            new AdventureManagerImpl(plugin);
        }
    }

    public static AdventureManagerImpl getInstance() {
        return instance;
    }

    public void sendMessage(CommandSender sender, String message) {
        adventure.sender(sender).sendMessage(miniMessage(message));
    }

    public void sendTitle(Player player, String title, String subtitle, int in, int duration, int out) {
        sendTitle(player, miniMessage(title), miniMessage(subtitle), in, duration, out);
    }
    
    public void sendTitle(Player player, Component title, Component subtitle, int in, int duration, int out) {
        Audience audience = adventure.player(player);
        Title.Times times = Title.Times.times(
                Duration.ofMillis(in * 50L),
                Duration.ofMillis(duration * 50L),
                Duration.ofMillis(out * 50L)
        );
        audience.showTitle(Title.title(title, subtitle, times));
    }
    
    public void sendActionbar(Player player, String s) {
        Audience audience = adventure.player(player);
        audience.sendActionBar(miniMessage(s));
    }
    
    public void sendSound(Player player, Sound.Source source, Key key, float volume, float pitch) {
        Sound sound = Sound.sound(key, source, volume, pitch);
        Audience au = adventure.player(player);
        au.playSound(sound);
    }

}