package cn.jrmcdp.craftitem.utils;

import cn.jrmcdp.craftitem.CraftItem;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

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

    public static void saveResource(String fileName) {
        CraftItem plugin = CraftItem.getPlugin();
        File file = new File(plugin.getDataFolder(), fileName);
        if (file.exists()) return;
        createParentDir(file);
        try (InputStream resource = plugin.getResource(fileName);
             InputStreamReader reader = resource == null ? null : new InputStreamReader(resource, StandardCharsets.UTF_8)) {
            if (resource == null) throw new IOException("插件jar内不存在资源文件 " + fileName);
            try (OutputStream out = Files.newOutputStream(file.toPath());
                 Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
                char[] buffer = new char[1024];
                int len;
                while ((len = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, len);
                }
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "保存资源文件 " + fileName + " 时出现一个异常", e);
        }
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
            saveResource(fileName);
        }
        return load(file);
    }

    public static YamlConfiguration load(File file) {
        YamlConfiguration config = new YamlConfiguration();
        if (!file.exists()) return config;
        try {
            FileInputStream stream = new FileInputStream(file);
            config.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
        } catch (FileNotFoundException ignored) {
        } catch (IOException | InvalidConfigurationException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + file, e);
        }
        return config;
    }
}
