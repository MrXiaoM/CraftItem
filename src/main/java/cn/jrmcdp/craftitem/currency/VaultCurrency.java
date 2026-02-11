package cn.jrmcdp.craftitem.currency;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.config.ConfigMain;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.ApiStatus;

public class VaultCurrency implements ICurrency {
    private final Economy economy;
    public VaultCurrency(Economy economy) {
        this.economy = economy;
    }

    @ApiStatus.Internal
    public static void register(CraftItem plugin) {
        RegisteredServiceProvider<Economy> service = Bukkit.getServicesManager().getRegistration(Economy.class);
        Economy economy = service == null ? null : service.getProvider();
        if (economy != null) {
            ICurrency currency = new VaultCurrency(economy);
            plugin.registerCurrency(str -> {
                if (str.equalsIgnoreCase("Vault")) {
                    return currency;
                }
                return null;
            });
            plugin.info("已挂钩 Vault 经济 (" + economy.getName() + ")");
        } else {
            plugin.warn("已发现 Vault 经济接口，但没有可用的经济服务，你可能未安装经济插件");
        }
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
