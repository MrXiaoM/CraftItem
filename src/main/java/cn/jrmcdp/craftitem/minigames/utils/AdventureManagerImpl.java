package cn.jrmcdp.craftitem.minigames.utils;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLib;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;

public class AdventureManagerImpl {
    private final ProtocolManager protocolManager;
    private final BukkitAudiences adventure;
    private static AdventureManagerImpl instance;

    private AdventureManagerImpl(JavaPlugin plugin) {
        this.adventure = BukkitAudiences.create(plugin);
        this.protocolManager = ProtocolLibrary.getProtocolManager();
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

    public void close() {
        if (adventure != null)
            adventure.close();
    }

    
    public Component getComponentFromMiniMessage(String text) {
        if (text == null) {
            return Component.empty();
        }
        if (CFConfig.legacyColorSupport) {
            return MiniMessage.miniMessage().deserialize(legacyToMiniMessage(text));
        } else {
            return MiniMessage.miniMessage().deserialize(text);
        }
    }

    
    public void sendMessage(CommandSender sender, String s) {
        if (s == null) return;
        if (sender instanceof Player) sendPlayerMessage((Player) sender, s);
        else if (sender instanceof ConsoleCommandSender) sendConsoleMessage(s);
    }

    
    public void sendMessageWithPrefix(CommandSender sender, String s) {
        if (s == null) return;
        if (sender instanceof Player) sendPlayerMessage((Player) sender, CFLocale.MSG_Prefix + s);
        else if (sender instanceof ConsoleCommandSender) sendConsoleMessage(CFLocale.MSG_Prefix + s);
    }

    
    public void sendConsoleMessage(String s) {
        if (s == null) return;
        Audience au = adventure.sender(Bukkit.getConsoleSender());
        au.sendMessage(getComponentFromMiniMessage(s));
    }

    
    public void sendPlayerMessage(Player player, String s) {
        if (s == null) return;
        Audience au = adventure.player(player);
        au.sendMessage(getComponentFromMiniMessage(s));
    }

    
    public void sendTitle(Player player, String title, String subtitle, int in, int duration, int out) {
        sendTitle(player, getComponentFromMiniMessage(title), getComponentFromMiniMessage(subtitle), in, duration, out);
    }

    
    public void sendTitle(Player player, Component title, Component subtitle, int in, int duration, int out) {
        try {
            PacketContainer titlePacket = new PacketContainer(PacketType.Play.Server.SET_TITLE_TEXT);
            titlePacket.getModifier().write(0, getIChatComponent(componentToJson(title)));
            PacketContainer subTitlePacket = new PacketContainer(PacketType.Play.Server.SET_SUBTITLE_TEXT);
            subTitlePacket.getModifier().write(0, getIChatComponent(componentToJson(subtitle)));
            PacketContainer timePacket = new PacketContainer(PacketType.Play.Server.SET_TITLES_ANIMATION);
            timePacket.getIntegers().write(0, in);
            timePacket.getIntegers().write(1, duration);
            timePacket.getIntegers().write(2, out);
            protocolManager.sendServerPacket(player, titlePacket);
            protocolManager.sendServerPacket(player, subTitlePacket);
            protocolManager.sendServerPacket(player, timePacket);
        } catch (InvocationTargetException | IllegalAccessException e) {
            LogUtils.warn("Error occurred when sending title");
        }
    }

    
    public void sendActionbar(Player player, String s) {
        try {
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.SET_ACTION_BAR_TEXT);
            packet.getModifier().write(0, getIChatComponent(componentToJson(getComponentFromMiniMessage(s))));
            protocolManager.sendServerPacket(player, packet);
        } catch (InvocationTargetException | IllegalAccessException e) {
            LogUtils.warn("Error occurred when sending actionbar");
        }
    }

    
    public void sendSound(Player player, Sound.Source source, Key key, float volume, float pitch) {
        Sound sound = Sound.sound(key, source, volume, pitch);
        Audience au = adventure.player(player);
        au.playSound(sound);
    }

    
    public void sendSound(Player player, Sound sound) {
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

    
    public String componentToLegacy(Component component) {
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    
    public String componentToJson(Component component) {
        return GsonComponentSerializer.gson().serialize(component);
    }

    
    public Object shadedComponentToOriginalComponent(Component component) {
        Object cp;
        try {
            cp = ReflectionUtils.gsonDeserializeMethod.invoke(ReflectionUtils.gsonInstance, GsonComponentSerializer.gson().serialize(component));
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
        return cp;
    }

    public Object getIChatComponent(String json) throws InvocationTargetException, IllegalAccessException {
        return ReflectionUtils.iChatComponentMethod.invoke(null, json);
    }
}