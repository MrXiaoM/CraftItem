package cn.jrmcdp.craftitem.config;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.utils.ConfigUtils;
import cn.jrmcdp.craftitem.utils.Utils;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public enum FileConfig {
    Message("Message.yml"),
    Craft("Craft.yml", ' '),
    Gui("Gui.yml"),
    Category("Category.yml"),
    Material("Material.yml"),
    Custom(null);

    private final String fileName;
    private final File file;
    private final char separator;
    FileConfig(String fileName) {
        this(fileName, '.');
    }

    FileConfig(String fileName, char separator) {
        this.fileName = fileName;
        this.file = fileName == null ? null : new File(CraftItem.getPlugin().getDataFolder(), fileName);
        this.separator = separator;
    }

    public YamlConfiguration loadConfig() {
        YamlConfiguration config = ConfigUtils.loadOrSaveResource(fileName);
        if (separator != '.') {
            config.options().pathSeparator(separator);
        }
        return config;
    }

    public YamlConfiguration loadConfig(String path, String name) {
        File parent = CraftItem.getPlugin().getDataFolder();
        File file = new File(parent, path + File.separator + name + ".yml");
        Utils.createNewFile(file);
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
