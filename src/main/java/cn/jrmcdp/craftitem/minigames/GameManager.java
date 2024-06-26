package cn.jrmcdp.craftitem.minigames;

import cn.jrmcdp.craftitem.utils.Utils;
import cn.jrmcdp.craftitem.minigames.utils.*;
import cn.jrmcdp.craftitem.minigames.utils.effect.Effect;
import cn.jrmcdp.craftitem.minigames.utils.game.BasicGameConfig;
import cn.jrmcdp.craftitem.minigames.utils.game.GameInstance;
import cn.jrmcdp.craftitem.minigames.utils.game.GamingPlayer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

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
    private final MiniGames miniGames;
    private final VersionManager versionManager;
    protected final ConcurrentHashMap<UUID, GamingPlayer> gamingPlayerMap = new ConcurrentHashMap<>();
    public GameManager(JavaPlugin plugin) {
        if (GameManager.inst != null) throw new IllegalStateException("GameManager is already loaded");
        GameManager.plugin = plugin;
        GameManager.inst = this;
        NMSHelper.init();

        this.versionManager = new VersionManager(plugin);
        ReflectionUtils.load();
        AdventureManagerImpl.load(plugin);
        this.miniGames = new MiniGames(plugin);
        Bukkit.getPluginManager().registerEvents(this, plugin);
        if (Utils.isPresent("com.destroystokyo.paper.event.player.PlayerJumpEvent")) {
            Bukkit.getPluginManager().registerEvents(new OnPaper(this), plugin);
        } else {
            plugin.getLogger().warning("当前服务端非 Paper 服务端，困难锻造中 dance 类型的小游戏（默认配置不使用）将无法正常使用");
        }
    }

    public MiniGames getMiniGames() {
        return miniGames;
    }

    public VersionManager getVersionManager() {
        return versionManager;
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

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;
        GamingPlayer gamingPlayer = gamingPlayerMap.get(event.getPlayer().getUniqueId());
        if (gamingPlayer != null) {
            if (gamingPlayer.onChat(event.getMessage()))
                event.setCancelled(true);
        }
    }

    @Nullable
    public GamingPlayer startGame(GameData game, Player player, String key, Effect effect) {
        if (gamingPlayerMap.containsKey(player.getUniqueId())) return null;
        Pair<BasicGameConfig, GameInstance> gamePair = miniGames.getGameInstance(key);
        if (key == null) {
            LogUtils.warn("No game is available for player:" + player.getName());
            return null;
        }
        if (gamePair == null) {
            LogUtils.warn(String.format("Game %s doesn't exist.", key));
            return null;
        }
        GamingPlayer gamingPlayer;
        gamingPlayerMap.put(player.getUniqueId(), gamingPlayer = gamePair.right().start(game, player, gamePair.left().getGameSetting(effect)));
        return gamingPlayer;
    }

}
