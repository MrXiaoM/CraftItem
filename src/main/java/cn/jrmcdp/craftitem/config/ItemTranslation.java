package cn.jrmcdp.craftitem.config;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.func.AbstractModule;
import cn.jrmcdp.craftitem.utils.ConfigUtils;
import com.meowj.langutils.lang.LanguageHelper;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTType;
import de.tr7zw.changeme.nbtapi.iface.ReadableNBT;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import org.bukkit.Material;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.mrxiaom.pluginbase.func.AutoRegister;

import java.util.HashMap;
import java.util.Map;

import static top.mrxiaom.pluginbase.utils.Util.isPresent;

@AutoRegister
public class ItemTranslation extends AbstractModule {
    private static final Map<String, String> material = new HashMap<>();
    private static boolean supportTranslationKey;
    private static boolean supportLangUtils;
    public ItemTranslation(CraftItem plugin) {
        super(plugin);
        supportTranslationKey = isPresent("org.bukkit.Translatable");
        supportLangUtils = isPresent("com.meowj.langutils.lang.LanguageHelper");
    }

    private static void doItemTest() {
    }

    @Override
    public void reloadConfig(MemoryConfiguration cfg) {
        YamlConfiguration config = ConfigUtils.loadPluginConfig(plugin, "Material.yml");
        material.clear();
        for (String key : config.getKeys(false)) {
            material.put(key, config.getString(key, "读取异常"));
        }
    }

    public static Map<String, String> getMaterial() {
        return material;
    }

    public static String get(String name, String def) {
        return getMaterial().getOrDefault(name, def);
    }

    public static String get(String name) {
        return get(name, name);
    }

    public static String get(ItemStack item, Player player) {
        if (supportTranslationKey) {
            return "<translate:" + item.getTranslationKey() + ">";
        }
        if (supportLangUtils) {
            return LanguageHelper.getItemName(item, player);
        }
        return get(item.getType().name());
    }

    public static String get(ItemStack item, String locale) {
        ItemMeta meta = item.hasItemMeta() ? item.getItemMeta() : null;
        String displayName = meta != null && meta.hasDisplayName() ? meta.getDisplayName() : null;
        if (displayName != null) {
            return displayName;
        }
        if (supportTranslationKey) {
            return "<translate:" + item.getTranslationKey() + ">";
        }
        if (supportLangUtils) {
            return LanguageHelper.getItemName(item, locale);
        }
        return get(item.getType().name());
    }
}
