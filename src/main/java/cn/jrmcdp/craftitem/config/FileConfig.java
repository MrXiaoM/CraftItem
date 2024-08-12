package cn.jrmcdp.craftitem.config;

import cn.jrmcdp.craftitem.CraftItem;
import java.io.File;
import java.io.IOException;

import cn.jrmcdp.craftitem.minigames.utils.LogUtils;
import cn.jrmcdp.craftitem.utils.Utils;
import org.bukkit.configuration.file.YamlConfiguration;

public enum FileConfig {
    Message(new File(CraftItem.getPlugin().getDataFolder(), "Message.yml")),
    Craft(new File(CraftItem.getPlugin().getDataFolder(), "Craft.yml"), ' '),
    Gui(new File(CraftItem.getPlugin().getDataFolder(), "Gui.yml")),
    Category(new File(CraftItem.getPlugin().getDataFolder(), "Category.yml")),
    Material(new File(CraftItem.getPlugin().getDataFolder(), "Material.yml")),
    Custom(null);

    private File file;
    private final char separator;
    FileConfig(File file) {
        this.file = file;
        this.separator = '.';
    }

    FileConfig(File file, char separator) {
        this.file = file;
        this.separator = separator;
    }

    public YamlConfiguration loadConfig() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(this.file);
        if (separator != '.') {
            config.options().pathSeparator(separator);
        }
        return config;
    }

    public YamlConfiguration loadConfig(String path, String name) {
        File parent = CraftItem.getPlugin().getDataFolder();
        File file = new File(parent, path + File.separator + name + ".yml");
        Utils.createNewFile(file);
        return YamlConfiguration.loadConfiguration(file);
    }

    public boolean exists() {
        return file.exists();
    }

    public void saveConfig(YamlConfiguration config) {
        saveConfig(config, this.file);
    }

    public void saveConfig(YamlConfiguration config, File file) {
        try {
            config.save(file);
        } catch (IOException e) {
            LogUtils.warn("保存 " + file.getName() + " 时出现一个错误", e);
        }
    }

    public void saveConfig(String path, String name, YamlConfiguration config) {
        File parent = CraftItem.getPlugin().getDataFolder();
        File file = new File(parent, path + File.separator + name + ".yml");
        saveConfig(config, file);
    }
}
