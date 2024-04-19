package cn.jrmcdp.craftitem.minigames.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class NMSHelper {
    static String serverVersion;
    static Class<?> cMinecraftKey;
    static Method namespacedKeyFromMinecraft;
    public static void init() {
        serverVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            Class<?> cNamespacedKey = Class.forName("org.bukkit.craftbukkit." + serverVersion + ".util.CraftNamespacedKey");
            for (Method method : cNamespacedKey.getDeclaredMethods()) {
                if (method.getName().equalsIgnoreCase("fromMinecraft")) {
                    namespacedKeyFromMinecraft = method;
                    cMinecraftKey = method.getParameterTypes()[0];
                    break;
                }
            }
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    static Object FIELD_BIOME_KEY = null;
    static Method mGetRegistryCustom = null;
    static Method mGetRegistryFromKey = null;
    static Method mGetValue = null;
    static Method mGetFromRegistry = null;
    public static NamespacedKey getRealBiomeNMS(Location loc) {
        return getRealBiomeNMS(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }
    public static NamespacedKey getRealBiomeNMS(World world, int x, int y, int z) {
        try {
            Method mGetHandle = world.getClass().getDeclaredMethod("getHandle");
            Object handle = mGetHandle.invoke(world);
            if (mGetRegistryCustom == null) for (Method method : handle.getClass().getDeclaredMethods()) {
                if (method.getParameterCount() != 0) continue;
                if (method.getReturnType().getName().contains("Registry")) {
                    mGetRegistryCustom = method;
                    break;
                }
            }
            if (mGetRegistryCustom == null) throw new IllegalAccessException("Can't find method that return type contains `Registry` from world handle");
            Object registryCustom = mGetRegistryCustom.invoke(handle);
            if (mGetRegistryFromKey == null) for (Method method : registryCustom.getClass().getDeclaredMethods()) {
                if (method.getParameterCount() != 1) continue;
                if (method.getReturnType().getName().contains("Registry")) {
                    mGetRegistryFromKey = method;
                    break;
                }
            }
            if (mGetRegistryFromKey == null) throw new IllegalAccessException("Can't find method that return type contains `Registry` from RegistryCustom");
            if (FIELD_BIOME_KEY == null) {
                String registryCustomClassName = registryCustom.getClass().getName();
                for (Field field : Class.forName(registryCustomClassName.substring(registryCustomClassName.lastIndexOf(".")) + ".Registries").getDeclaredFields()) {
                    if (!Modifier.isStatic(field.getModifiers())) continue;
                    Object inst = field.get(null);
                    if (inst.toString().contains("worldgen/biome]")) {
                        FIELD_BIOME_KEY = inst;
                        break;
                    }
                }
            }
            if (FIELD_BIOME_KEY == null) throw new IllegalAccessException("Can't find worldgen biome registry");
            Object registry = mGetRegistryFromKey.invoke(registryCustom, FIELD_BIOME_KEY);
            Method mGetNoiseBiome = handle.getClass().getDeclaredMethod("getNoiseBiome", int.class, int.class, int.class);
            Object biomeHolder = mGetNoiseBiome.invoke(handle, x >> 2, y >> 2, z >> 2);
            if (mGetValue == null) for (Method method : biomeHolder.getClass().getDeclaredMethods()) {
                if (method.getParameterTypes().length == 1) {
                    mGetValue = method;
                    break;
                }
            }
            if (mGetValue == null) throw new IllegalAccessException("Can't find get value method in Holder");
            Object biome = mGetValue.invoke(biomeHolder);
            if (biome == null) return null;
            if (mGetFromRegistry == null) for (Method method : registry.getClass().getDeclaredMethods()) {
                if (method.getParameterCount() != 1) continue;
                if (method.getReturnType().getName().equals(cMinecraftKey.getName())) {
                    mGetFromRegistry = method;
                    break;
                }
            }
            if (mGetFromRegistry == null) throw new IllegalAccessException("Can't find get value method in registry");
            Object key = mGetFromRegistry.invoke(registry, biome);
            if (key == null) return null;
            return (NamespacedKey) namespacedKeyFromMinecraft.invoke(null, key);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        return null;
    }
}
