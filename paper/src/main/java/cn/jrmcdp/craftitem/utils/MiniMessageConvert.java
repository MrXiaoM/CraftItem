package cn.jrmcdp.craftitem.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import top.mrxiaom.pluginbase.utils.AdventureUtil;

public class MiniMessageConvert {
    public static String legacyToMiniMessage(String legacy) {
        return AdventureUtil.legacyToMiniMessage(legacy);
    }
}
