package cn.jrmcdp.craftitem.minigames.utils;

import cn.jrmcdp.craftitem.minigames.GameManager;
import com.comphenix.protocol.utility.MinecraftReflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class ReflectionUtils {
    public static Constructor<?> progressConstructor;
    public static Constructor<?> updateConstructor;
    public static Method iChatComponentMethod;
    public static Method gsonDeserializeMethod;

    public static void load() {
        // spigot map
        try {
            Class<?> packetBossClassF = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutBoss$f");
            progressConstructor = packetBossClassF.getDeclaredConstructor(float.class);
            progressConstructor.setAccessible(true);
            Class<?> packetBossClassE = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutBoss$e");
            updateConstructor = packetBossClassE.getDeclaredConstructor(MinecraftReflection.getIChatBaseComponentClass());
            updateConstructor.setAccessible(true);
            iChatComponentMethod = MinecraftReflection.getChatSerializerClass().getMethod("a", String.class);
            iChatComponentMethod.setAccessible(true);
        } catch (ReflectiveOperationException e1) {
            // mojang mapping
            try {
                Class<?> packetBossClassF = Class.forName("net.minecraft.network.protocol.game.ClientboundBossEventPacket$UpdateProgressOperation");
                progressConstructor = packetBossClassF.getDeclaredConstructor(float.class);
                progressConstructor.setAccessible(true);
                Class<?> packetBossClassE = Class.forName("net.minecraft.network.protocol.game.ClientboundBossEventPacket$UpdateNameOperation");
                updateConstructor = packetBossClassE.getDeclaredConstructor(MinecraftReflection.getIChatBaseComponentClass());
                updateConstructor.setAccessible(true);
                iChatComponentMethod = MinecraftReflection.getChatSerializerClass().getMethod("fromJson", String.class);
                iChatComponentMethod.setAccessible(true);
            } catch (ReflectiveOperationException e2) {
                LogUtils.severe("Error occurred when loading reflections", e2);
            }
            return;
        }
        if (GameManager.inst().getVersionManager().isSpigot()) return;
        try {
            Class<?> gsonComponentSerializerImplClass = Class.forName("net;kyori;adventure;text;serializer;gson;GsonComponentSerializerImpl".replace(";", "."));
            gsonDeserializeMethod = gsonComponentSerializerImplClass.getMethod("deserialize", String.class);
            gsonDeserializeMethod.setAccessible(true);
        } catch (ClassNotFoundException exception) {
            LogUtils.severe("Error occurred when loading reflections", exception);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}