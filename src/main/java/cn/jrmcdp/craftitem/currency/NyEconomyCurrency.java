package cn.jrmcdp.craftitem.currency;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.config.ConfigMain;
import com.mc9y.nyeconomy.api.NyEconomyAPI;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

public class NyEconomyCurrency implements ICurrency {
    private final NyEconomyAPI economy;
    private final String type;
    public NyEconomyCurrency(NyEconomyAPI economy, String type) {
        this.economy = economy;
        this.type = type;
    }

    @ApiStatus.Internal
    public static void register(CraftItem plugin) {
        NyEconomyAPI economy = NyEconomyAPI.getInstance();
        Map<String, ICurrency> cache = new HashMap<>();
        plugin.registerCurrency(str -> {
            if (str.startsWith("NyEconomy:")) {
                String type = str.substring(10);
                ICurrency exists = cache.get(type);
                if (exists != null) return exists;
                NyEconomyCurrency currency = new NyEconomyCurrency(economy, type);
                cache.put(type, currency);
                return currency;
            }
            return null;
        });
        plugin.info("已挂钩 NyEconomy 经济");
    }

    @Override
    public String getName() {
        return ConfigMain.inst().getCurrencyName("NyEconomy", type);
    }

    @Override
    public String serialize() {
        return "NyEconomy:" + type;
    }

    @Override
    public double get(OfflinePlayer player) {
        return economy.getBalance(type, player.getUniqueId());
    }

    @Override
    public boolean has(OfflinePlayer player, double money) {
        return economy.getBalance(type, player.getUniqueId()) >= money;
    }

    @Override
    public boolean giveMoney(OfflinePlayer player, double money) {
        economy.withdraw(type, player.getUniqueId(), (int) money);
        return true;
    }

    @Override
    public boolean takeMoney(OfflinePlayer player, double money) {
        if (has(player, money)) {
            economy.deposit(type, player.getUniqueId(), (int) money);
            return true;
        }
        return false;
    }
}
