package cn.jrmcdp.craftitem.currency;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.config.ConfigMain;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.ApiStatus;

public class PlayerPointsCurrency implements ICurrency {
    private final PlayerPointsAPI economy;
    public PlayerPointsCurrency(PlayerPointsAPI economy) {
        this.economy = economy;
    }

    @ApiStatus.Internal
    public static void register(CraftItem plugin) {
        PlayerPointsAPI economy = PlayerPoints.getInstance().getAPI();
        ICurrency currency = new PlayerPointsCurrency(economy);
        plugin.registerCurrency(str -> {
            if (str.equalsIgnoreCase("PlayerPoints")) {
                return currency;
            }
            return null;
        });
        plugin.info("已挂钩 PlayerPoints 经济");
    }

    @Override
    public String getName() {
        return ConfigMain.inst().getCurrencyName("PlayerPoints");
    }

    @Override
    public String serialize() {
        return "PlayerPoints";
    }

    @Override
    public double get(OfflinePlayer player) {
        return economy.look(player.getUniqueId());
    }

    @Override
    public boolean has(OfflinePlayer player, double money) {
        return get(player) >= money;
    }

    @Override
    public boolean giveMoney(OfflinePlayer player, double money) {
        return economy.give(player.getUniqueId(), (int) money);
    }

    @Override
    public boolean takeMoney(OfflinePlayer player, double money) {
        if (has(player, money)) {
            return economy.take(player.getUniqueId(), (int) money);
        }
        return false;
    }
}
