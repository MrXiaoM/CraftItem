package cn.jrmcdp.craftitem.currency;

import cn.jrmcdp.craftitem.config.ConfigMain;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;

public class VaultCurrency implements ICurrency {
    private final Economy economy;
    public VaultCurrency(Economy economy) {
        this.economy = economy;
    }

    @Override
    public String getName() {
        return ConfigMain.inst().getCurrencyName("Vault");
    }

    @Override
    public String serialize() {
        return "Vault";
    }

    @Override
    public double get(OfflinePlayer player) {
        return economy.getBalance(player);
    }

    @Override
    public boolean has(OfflinePlayer player, double money) {
        return economy.has(player, money);
    }

    @Override
    public boolean giveMoney(OfflinePlayer player, double money) {
        return economy.depositPlayer(player, money).transactionSuccess();
    }

    @Override
    public boolean takeMoney(OfflinePlayer player, double money) {
        if (has(player, money)) {
            return economy.withdrawPlayer(player, money).transactionSuccess();
        }
        return false;
    }
}
