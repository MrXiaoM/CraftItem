package cn.jrmcdp.craftitem.config;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.utils.ConfigUtils;
import cn.jrmcdp.craftitem.utils.Utils;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public enum FileConfig {
    Message("Message.yml", false),
    Craft("Craft.yml", true, ' '),
    Gui("Gui.yml", true),
    Category("Category.yml", true),
    Material("Material.yml", true),
    Custom(null, false);

    private final boolean saveIfNotExists;
    private final File file;
    private final char separator;
    FileConfig(String fileName, boolean saveIfNotExists) {
        this(fileName, saveIfNotExists, '.');
    }

    FileConfig(String fileName, boolean saveIfNotExists, char separator) {
        this.file = fileName == null ? null : new File(CraftItem.getPlugin().getDataFolder(), fileName);
        this.saveIfNotExists = saveIfNotExists;
        this.separator = separator;
    }

    public YamlConfiguration loadConfig() {
        YamlConfiguration config = saveIfNotExists ? ConfigUtils.loadOrSaveResource(file.getName()) : ConfigUtils.load(file);
        if (separator != '.') {
            config.options().pathSeparator(separator);
        }
        return config;
    }

    public YamlConfiguration loadConfig(String path, String name) {
        File parent = CraftItem.getPlugin().getDataFolder();
        File file = new File(parent, path + File.separator + name + ".yml");
        return ConfigUtils.load(file);
    }

    public boolean exists() {
        return file.exists();
    }

    public void saveConfig(YamlConfiguration config) {
        saveConfig(config, this.file);
    }

    public void saveConfig(YamlConfiguration config, File file) {
        ConfigUtils.save(config, file);
    }

    public void saveConfig(String path, String name, YamlConfiguration config) {
        File parent = CraftItem.getPlugin().getDataFolder();
        File file = new File(parent, path + File.separator + name + ".yml");
        saveConfig(config, file);
    }
}
