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
    private static boolean itemNbtUseComponentsFormat;
    private static boolean textUseComponent;
    public ItemTranslation(CraftItem plugin) {
        super(plugin);
        supportTranslationKey = isPresent("org.bukkit.Translatable");
        supportLangUtils = isPresent("com.meowj.langutils.lang.LanguageHelper");
    }

    private static void doItemTest() {
        itemNbtUseComponentsFormat = MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4);
        ItemStack item = new ItemStack(Material.STONE);
        ItemMeta meta = item.getItemMeta();
        String testDisplayName = "§a§l测试§e§l文本";
        if (meta == null) { // 预料之外的情况
            textUseComponent = false;
        } else {
            meta.setDisplayName(testDisplayName);
            item.setItemMeta(meta);
            if (itemNbtUseComponentsFormat) {
                textUseComponent = true;
            } else {
                // 测试物品是否支持使用 component
                NBT.get(item, nbt -> {
                    ReadableNBT display = nbt.getCompound("display");
                    if (display == null) {
                        textUseComponent = false;
                        return;
                    }
                    String name = display.getString("Name");
                    // 旧版本文本组件不支持 JSON 字符串，设置旧版颜色符之后，物品名会跟之前一样
                    textUseComponent = !name.equals(testDisplayName);
                });
            }
        }
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
        if (supportTranslationKey && textUseComponent) {
            return "<translate:" + item.getTranslationKey() + ">";
        }
        if (supportLangUtils) {
            return LanguageHelper.getItemName(item, player);
        }
        return get(item.getType().name());
    }

    public static String get(ItemStack item, String locale) {
        ItemMeta meta = item.hasItemMeta() ? item.getItemMeta() : null;
        String displayName = meta.hasDisplayName() ? meta.getDisplayName() : null;
        if (displayName != null) {
            return displayName;
        }
        if (supportTranslationKey && textUseComponent) {
            return "<translate:" + item.getTranslationKey() + ">";
        }
        if (supportLangUtils) {
            return LanguageHelper.getItemName(item, locale);
        }
        return get(item.getType().name());
    }
}
