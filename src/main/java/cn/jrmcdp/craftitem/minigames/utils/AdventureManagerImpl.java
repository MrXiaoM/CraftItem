package cn.jrmcdp.craftitem.minigames.utils;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;

public class AdventureManagerImpl {
    private final BukkitAudiences adventure;
    private final MiniMessage miniMessage;
    private static AdventureManagerImpl instance;
    public static final boolean legacyColorSupport = false;

    private AdventureManagerImpl(JavaPlugin plugin) {
        this.adventure = BukkitAudiences.create(plugin);
        this.miniMessage = MiniMessage.builder().build();
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

    public Component miniMessage(String text) {
        if (text == null) {
            return Component.empty();
        }
        if (legacyColorSupport) {
            return miniMessage.deserialize(legacyToMiniMessage(text));
        } else {
            return miniMessage.deserialize(text);
        }
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
        audience.showTitle(Title.title(title,subtitle, times));
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

    public String legacyToMiniMessage(String legacy) {
        StringBuilder stringBuilder = new StringBuilder();
        char[] chars = legacy.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (!isColorCode(chars[i])) {
                stringBuilder.append(chars[i]);
                continue;
            }
            if (i + 1 >= chars.length) {
                stringBuilder.append(chars[i]);
                continue;
            }
            switch (chars[i+1]) {
                case '0': stringBuilder.append("<black>"); break;
                case '1': stringBuilder.append("<dark_blue>"); break;
                case '2': stringBuilder.append("<dark_green>"); break;
                case '3': stringBuilder.append("<dark_aqua>"); break;
                case '4': stringBuilder.append("<dark_red>"); break;
                case '5': stringBuilder.append("<dark_purple>"); break;
                case '6': stringBuilder.append("<gold>"); break;
                case '7': stringBuilder.append("<gray>"); break;
                case '8': stringBuilder.append("<dark_gray>"); break;
                case '9': stringBuilder.append("<blue>"); break;
                case 'a': stringBuilder.append("<green>"); break;
                case 'b': stringBuilder.append("<aqua>"); break;
                case 'c': stringBuilder.append("<red>"); break;
                case 'd': stringBuilder.append("<light_purple>"); break;
                case 'e': stringBuilder.append("<yellow>"); break;
                case 'f': stringBuilder.append("<white>"); break;
                case 'r': stringBuilder.append("<r><!i>"); break;
                case 'l': stringBuilder.append("<b>"); break;
                case 'm': stringBuilder.append("<st>"); break;
                case 'o': stringBuilder.append("<i>"); break;
                case 'n': stringBuilder.append("<u>"); break;
                case 'k': stringBuilder.append("<o>"); break;
                case 'x': {
                    if (i + 13 >= chars.length
                            || !isColorCode(chars[i+2])
                            || !isColorCode(chars[i+4])
                            || !isColorCode(chars[i+6])
                            || !isColorCode(chars[i+8])
                            || !isColorCode(chars[i+10])
                            || !isColorCode(chars[i+12])) {
                        stringBuilder.append(chars[i]);
                        continue;
                    }
                    stringBuilder
                            .append("<#")
                            .append(chars[i+3])
                            .append(chars[i+5])
                            .append(chars[i+7])
                            .append(chars[i+9])
                            .append(chars[i+11])
                            .append(chars[i+13])
                            .append(">");
                    i += 12;
                    break;
                }
                default: {
                    stringBuilder.append(chars[i]);
                    continue;
                }
            }
            i++;
        }
        return stringBuilder.toString();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isColorCode(char c) {
        return c == '§' || c == '&';
    }

}