package cn.jrmcdp.craftitem.currency;

import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ICurrencyProvider {
    default int priority() {
        return 1000;
    }
    @Nullable ICurrency parse(String str);
}
