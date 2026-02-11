package cn.jrmcdp.craftitem.currency;

import cn.jrmcdp.craftitem.CraftItem;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.ApiStatus;
import su.nightexpress.coinsengine.api.CoinsEngineAPI;
import su.nightexpress.coinsengine.api.currency.Currency;

import java.util.HashMap;
import java.util.Map;

public class CoinsEngineCurrency implements ICurrency {
    private final Currency economy;
    public CoinsEngineCurrency(Currency economy) {
        this.economy = economy;
    }

    @ApiStatus.Internal
    public static void register(CraftItem plugin) {
        Map<String, ICurrency> cache = new HashMap<>();
        plugin.registerCurrency(str -> {
            if (str.startsWith("CoinsEngine:")) {
                String currencyId = str.substring(12);
                ICurrency exists = cache.get(currencyId);
                if (exists != null) return exists;
                Currency economy = CoinsEngineAPI.getCurrency(currencyId);
                if (economy == null) return null;
                CoinsEngineCurrency currency = new CoinsEngineCurrency(economy);
                cache.put(currencyId, currency);
                return currency;
            }
            return null;
        });
        plugin.info("已挂钩 CoinsEngine 经济");
    }

    @Override
    public String getName() {
        return economy.getName();
    }

    @Override
    public String serialize() {
        return "CoinsEngine:" + economy.getId();
    }

    @Override
    public double get(OfflinePlayer player) {
        return CoinsEngineAPI.getBalance(player.getUniqueId(), economy);
    }

    @Override
    public boolean has(OfflinePlayer player, double money) {
        return get(player) >= money;
    }

    @Override
    public boolean giveMoney(OfflinePlayer player, double money) {
        return CoinsEngineAPI.addBalance(player.getUniqueId(), economy, money);
    }

    @Override
    public boolean takeMoney(OfflinePlayer player, double money) {
        return CoinsEngineAPI.removeBalance(player.getUniqueId(), economy, money);
    }
}
