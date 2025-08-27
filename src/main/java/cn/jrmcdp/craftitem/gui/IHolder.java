package cn.jrmcdp.craftitem.gui;

import top.mrxiaom.pluginbase.gui.IGuiHolder;

public interface IHolder extends IGuiHolder, IAutoCloseHolder {
    default void onSecond() {

    }
}
