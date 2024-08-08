package cn.jrmcdp.craftitem.config;

import cn.jrmcdp.craftitem.CraftItem;
import java.io.File;
import java.io.IOException;

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
        this.file = new File(CraftItem.getPlugin().getDataFolder(), path + File.separator + name + ".yml");
        if (!this.file.exists())
            try {
                this.file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        return YamlConfiguration.loadConfiguration(this.file);
    }

    public boolean exists() {
        return file.exists();
    }

    public void saveConfig(YamlConfiguration config) {
        try {
            config.save(this.file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveConfig(String path, String name, YamlConfiguration config) {
        this.file = new File(CraftItem.getPlugin().getDataFolder(), path + File.separator + name + ".yml");
        saveConfig(config);
    }
}
