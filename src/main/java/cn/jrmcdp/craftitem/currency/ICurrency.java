package cn.jrmcdp.craftitem.currency;

import top.mrxiaom.pluginbase.economy.IEconomy;

public interface ICurrency extends IEconomy {
    String getName();
    String serialize();
}
