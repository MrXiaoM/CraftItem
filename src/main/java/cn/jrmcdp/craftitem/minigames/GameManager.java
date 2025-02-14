package cn.jrmcdp.craftitem.minigames;

import cn.jrmcdp.craftitem.minigames.game.BasicGameConfig;
import cn.jrmcdp.craftitem.minigames.game.GameInstance;
import cn.jrmcdp.craftitem.minigames.game.GameSettings;
import cn.jrmcdp.craftitem.minigames.game.GamingPlayer;
import cn.jrmcdp.craftitem.minigames.utils.AdventureManagerImpl;
import cn.jrmcdp.craftitem.minigames.utils.LogUtils;
import cn.jrmcdp.craftitem.minigames.utils.OffsetUtils;
import cn.jrmcdp.craftitem.utils.Pair;
import cn.jrmcdp.craftitem.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GameManager implements Listener {
    static GameManager inst;
    static JavaPlugin plugin;
    public static JavaPlugin getPlugin() {
        return plugin;
    }
    public static GameManager inst() {
        return inst;
    }
    public final MiniGames miniGames;
    protected final ConcurrentHashMap<UUID, GamingPlayer> gamingPlayerMap = new ConcurrentHashMap<>();
    public GameManager(JavaPlugin plugin) {
        if (GameManager.inst != null) throw new IllegalStateException("GameManager is already loaded");
        GameManager.plugin = plugin;
        GameManager.inst = this;

        AdventureManagerImpl.load(plugin);
        this.miniGames = new MiniGames(plugin);
        Bukkit.getPluginManager().registerEvents(this, plugin);
        if (Utils.isPresent("com.destroystokyo.paper.event.player.PlayerJumpEvent")) {
            Bukkit.getPluginManager().registerEvents(new OnPaper(this::onJump), plugin);
        } else {
            plugin.getLogger().warning("当前服务端非 Paper 服务端，困难锻造中 dance 类型的小游戏（默认配置不使用）将无法正常使用");
        }
    }

    public void reloadConfig() {
        this.miniGames.unload();
        this.miniGames.load();
        unload();
        FileConfiguration config = plugin.getConfig();
        OffsetUtils.loadConfig(config.getConfigurationSection("offset-characters"));
    }

    public void unload() {
        for (GamingPlayer gamingPlayer : gamingPlayerMap.values()) {
            gamingPlayer.cancel();
        }
        gamingPlayerMap.clear();
    }

    public void disable() {
        unload();
        miniGames.disable();
    }

    /**
     * Processes the game result for a gaming player
     *
     * @param gamingPlayer The gaming player whose game result should be processed.
     */
    public void processGameResult(GamingPlayer gamingPlayer) {
        final Player player = gamingPlayer.getPlayer();
        final UUID uuid = player.getUniqueId();

        gamingPlayer.cancel();
        gamingPlayerMap.remove(uuid);
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (gamingPlayer.isSuccessful()) {
                gamingPlayer.getGame().success(gamingPlayer);
            } else {
                gamingPlayer.getGame().fail(gamingPlayer);
            }
        });
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();
        GamingPlayer gamingPlayer = gamingPlayerMap.remove(uuid);
        if (gamingPlayer != null) {
            gamingPlayer.cancel();
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        GamingPlayer gamingPlayer = gamingPlayerMap.get(event.getPlayer().getUniqueId());
        if (gamingPlayer != null) {
            if ((event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK))
                    && gamingPlayer.onLeftClick()) {
                event.setCancelled(true);
            }
            if ((event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
                    && gamingPlayer.onRightClick()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
        GamingPlayer gamingPlayer = gamingPlayerMap.get(event.getPlayer().getUniqueId());
        if (gamingPlayer != null) {
            if (gamingPlayer.onSwapHand())
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (event.isCancelled()) return;
        if (!event.isSneaking()) return;
        GamingPlayer gamingPlayer = gamingPlayerMap.get(event.getPlayer().getUniqueId());
        if (gamingPlayer != null) {
            if (gamingPlayer.onSneak())
                event.setCancelled(true);
        }
    }

    private void onJump(Cancellable event, Player player) {
        if (event.isCancelled()) return;
        GamingPlayer gamingPlayer = gamingPlayerMap.get(player.getUniqueId());
        if (gamingPlayer != null) {
            if (gamingPlayer.onJump())
                event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;
        GamingPlayer gamingPlayer = gamingPlayerMap.get(event.getPlayer().getUniqueId());
        if (gamingPlayer != null) {
            if (gamingPlayer.onChat(event.getMessage()))
                event.setCancelled(true);
        }
    }

    public void startGame(GameData game, Player player, String key) {
        if (gamingPlayerMap.containsKey(player.getUniqueId())) return;
        Pair<BasicGameConfig, GameInstance> gamePair = miniGames.getGameInstance(key);
        if (key == null) {
            LogUtils.warn("No game is available for player:" + player.getName());
            return;
        }
        if (gamePair == null) {
            LogUtils.warn(String.format("Game %s doesn't exist.", key));
            return;
        }
        BasicGameConfig cfg = gamePair.left();
        GameInstance inst = gamePair.right();
        GameSettings settings = cfg.getGameSetting();
        GamingPlayer gamePlayer = inst.start(game, player, settings);
        gamingPlayerMap.put(player.getUniqueId(), gamePlayer);
    }

}
