package cn.jrmcdp.craftitem.func;

import cn.jrmcdp.craftitem.CraftItem;

@SuppressWarnings({"unused"})
public abstract class AbstractPluginHolder extends top.mrxiaom.pluginbase.func.AbstractPluginHolder<CraftItem> {
    public AbstractPluginHolder(CraftItem plugin) {
        super(plugin);
    }

    public AbstractPluginHolder(CraftItem plugin, boolean register) {
        super(plugin, register);
    }
}
