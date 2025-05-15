package cn.jrmcdp.craftitem.utils;

import cn.jrmcdp.craftitem.CraftItem;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import top.mrxiaom.pluginbase.BukkitPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.logging.Level;

/**
 * 强制使用 UTF-8 编码的配置工具类
 */
public class ConfigUtils {

    @CanIgnoreReturnValue
    private static boolean createParentDir(File file) {
        File parent = file.getParentFile();
        return parent != null && parent.mkdirs();
    }
    public static YamlConfiguration loadPluginConfig(BukkitPlugin plugin, String fileName, char separator) {
        File file = plugin.resolve("./" + fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName);
        }
        YamlConfiguration config = new YamlConfiguration();
        if (separator != '.') {
            config.options().pathSeparator(separator);
        }
        load(config, file);
        return config;
    }
    public static YamlConfiguration loadPluginConfig(BukkitPlugin plugin, String file) {
        return loadPluginConfig(plugin, file, '.');
    }
    public static YamlConfiguration loadConfig(String path, String name) {
        File file = CraftItem.getPlugin().resolve(path + File.separator + name + ".yml");
        return ConfigUtils.load(file);
    }

    public static void savePluginConfig(BukkitPlugin plugin, String fileName, YamlConfiguration config) {
        File file = plugin.resolve(fileName);
        save(config, file);
    }

    public static void saveConfig(String path, String name, YamlConfiguration config) {
        File file = CraftItem.getPlugin().resolve(path + File.separator + name + ".yml");
        save(config, file);
    }

    public static void save(FileConfiguration config, File file) {
        createParentDir(file);
        String data = config.saveToString();
        try (OutputStream out = Files.newOutputStream(file.toPath());
             Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
            writer.write(data);
        } catch (IOException e) {
            CraftItem.getPlugin().getLogger().log(Level.WARNING, "保存文件 " + file.getName() + " 时出现一个异常", e);
        }
    }

    public static YamlConfiguration loadOrSaveResource(String fileName) {
        File file = new File(CraftItem.getPlugin().getDataFolder(), fileName);
        if (!file.exists()) {
            CraftItem.getPlugin().saveResource(fileName);
        }
        return load(file);
    }

    public static YamlConfiguration load(File file) {
        YamlConfiguration config = new YamlConfiguration();
        load(config, file);
        return config;
    }

    public static void load(YamlConfiguration config, File file) {
        if (!file.exists()) return;
        try (FileInputStream stream = new FileInputStream(file);
             Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)
        ) {
            config.load(reader);
        } catch (FileNotFoundException ignored) {
        } catch (IOException | InvalidConfigurationException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + file, e);
        }
    }
}
