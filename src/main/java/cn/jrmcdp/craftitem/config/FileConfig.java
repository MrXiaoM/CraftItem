package cn.jrmcdp.craftitem.config;

import cn.jrmcdp.craftitem.CraftItem;
import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.YamlConfiguration;

public enum FileConfig {
    Message(new File(CraftItem.getPlugin().getDataFolder(), "Message.yml")),
    Craft(new File(CraftItem.getPlugin().getDataFolder(), "Craft.yml")),
    Gui(new File(CraftItem.getPlugin().getDataFolder(), "Gui.yml")),
    Category(new File(CraftItem.getPlugin().getDataFolder(), "Category.yml")),
    Material(new File(CraftItem.getPlugin().getDataFolder(), "Material.yml")),
    Custom(null);

    private File file;

    FileConfig(File file) {
        this.file = file;
    }

    public YamlConfiguration getConfig() {
        return YamlConfiguration.loadConfiguration(this.file);
    }

    public YamlConfiguration getConfig(String path, String name) {
        this.file = new File(CraftItem.getPlugin().getDataFolder(), path + File.separator + name + ".yml");
        if (!this.file.exists())
            try {
                this.file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        return YamlConfiguration.loadConfiguration(this.file);
    }

    public YamlConfiguration getOrCreateConfig(String path, String name) {
        this.file = new File(CraftItem.getPlugin().getDataFolder(), path + File.separator + name + ".yml");
        if (!this.file.exists())
            try {
                this.file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        return YamlConfiguration.loadConfiguration(this.file);
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
        try {
            config.save(this.file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
