package cn.jrmcdp.craftitem.currency;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.config.ConfigMain;
import me.yic.mpoints.MPointsAPI;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.ApiStatus;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class MPointsCurrency implements ICurrency {
    private final MPointsAPI economy;
    private final String sign;
    public MPointsCurrency(MPointsAPI economy, String sign) {
        this.economy = economy;
        this.sign = sign;
    }

    @ApiStatus.Internal
    public static void register(CraftItem plugin) {
        MPointsAPI economy = new MPointsAPI();
        Map<String, ICurrency> cache = new HashMap<>();
        plugin.registerCurrency(str -> {
            if (str.startsWith("MPoints:")) {
                String sign = str.substring(8);
                ICurrency exists = cache.get(sign);
                if (exists != null) return exists;
                MPointsCurrency currency = new MPointsCurrency(economy, sign);
                cache.put(sign, currency);
                return currency;
            }
            return null;
        });
        plugin.info("已挂钩 MPoints 经济");
    }

    @Override
    public String getName() {
        return ConfigMain.inst().getCurrencyName("MPoints", sign);
    }

    @Override
    public String serialize() {
        return "MPoints:" + sign;
    }

    @Override
    public double get(OfflinePlayer player) {
        return economy.getbalance(sign, player.getUniqueId()).doubleValue();
    }

    @Override
    public boolean has(OfflinePlayer player, double money) {
        return get(player) >= money;
    }

    @Override
    public boolean giveMoney(OfflinePlayer player, double money) {
        return economy.changebalance(sign, player.getUniqueId(), player.getName(), BigDecimal.valueOf(money), true) == 0;
    }

    @Override
    public boolean takeMoney(OfflinePlayer player, double money) {
        if (has(player, money)) {
            return economy.changebalance(sign, player.getUniqueId(), player.getName(), BigDecimal.valueOf(money), false) == 0;
        }
        return false;
    }
}
