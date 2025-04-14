package cn.jrmcdp.craftitem.gui;

import top.mrxiaom.pluginbase.gui.IGui;

public interface IHolder extends IGui, IAutoCloseHolder {
    default void onSecond() {

    }
}
