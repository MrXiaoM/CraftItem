package cn.jrmcdp.craftitem.config;

import java.util.List;
import org.bukkit.configuration.file.YamlConfiguration;

public class Message {
    private static YamlConfiguration config;

    private static String prefix;

    private static String reload;

    private static String noPermission;

    private static String noPlayer;

    private static List<String> help;

    public static void reload() {
        config = FileConfig.Message.getConfig();
        prefix = config.getString("前缀", "§c缺少信息 前缀");
        reload = config.getString("重载", "§c缺少信息 重载");
        noPermission = config.getString("无权限", "§c缺少信息 无权限");
        noPlayer = config.getString("非玩家", "§c缺少信息 非玩家");
        help = config.getStringList("帮助");
    }

    public static YamlConfiguration getConfig() {
        return config;
    }

    public static String getPrefix() {
        return prefix;
    }

    public static String getReload() {
        return reload;
    }

    public static String getNoPermission() {
        return noPermission;
    }

    public static String getNoPlayer() {
        return noPlayer;
    }

    public static List<String> getHelp() {
        return help;
    }
}
