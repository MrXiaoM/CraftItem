package cn.jrmcdp.craftitem.minigames;

import cn.jrmcdp.craftitem.minigames.game.AbstractGamingPlayer;
import cn.jrmcdp.craftitem.minigames.game.BasicGameConfig;
import cn.jrmcdp.craftitem.minigames.game.GameFactory;
import cn.jrmcdp.craftitem.minigames.game.GameInstance;
import cn.jrmcdp.craftitem.minigames.utils.*;
import cn.jrmcdp.craftitem.utils.Pair;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("DuplicatedCode")
public class MiniGames {

    private final JavaPlugin plugin;
    private final HashMap<String, GameFactory> gameCreatorMap;
    private final HashMap<String, Pair<BasicGameConfig, GameInstance>> gameInstanceMap;
    public MiniGames(JavaPlugin plugin) {
        this.plugin = plugin;
        this.gameCreatorMap = new HashMap<>();
        this.gameInstanceMap = new HashMap<>();
        this.registerInbuiltGames();
    }

    private void registerInbuiltGames() {
        this.registerHoldGame();
        this.registerHoldV2Game();
        this.registerTensionGame();
        this.registerClickGame();
        this.registerDanceGame();
        this.registerAccurateClickGame();
        this.registerAccurateClickV2Game();
        this.registerAccurateClickV3Game();
    }

    public void load() {
        this.loadGamesFromPluginFolder();
    }

    public void unload() {
        this.gameInstanceMap.clear();
    }

    public void disable() {
        unload();
        this.gameCreatorMap.clear();
    }

    /**
     * Registers a new game type with the specified type identifier.
     *
     * @param type        The type identifier for the game.
     * @param gameFactory The {@link GameFactory} that creates instances of the game.
     */
    public void registerGameType(String type, GameFactory gameFactory) {
        if (!gameCreatorMap.containsKey(type)) {
            gameCreatorMap.put(type, gameFactory);
        }
    }

    /**
     * Retrieves the game factory associated with the specified game type.
     *
     * @param type The type identifier of the game.
     * @return The {@code GameFactory} for the specified game type, or {@code null} if not found.
     */
    
    @Nullable
    public GameFactory getGameFactory(String type) {
        return gameCreatorMap.get(type);
    }

    /**
     * Retrieves a game instance and its basic configuration associated with the specified key.
     *
     * @param key The key identifying the game instance.
     * @return An {@code Optional} containing a {@code Pair} of the basic game configuration and the game instance
     *         if found, or an empty {@code Optional} if not found.
     */
    
    public Pair<BasicGameConfig, GameInstance> getGameInstance(String key) {
        return gameInstanceMap.get(key);
    }

    /**
     * Loads minigames from the plugin folder.
     * This method searches for minigame configuration files in the plugin's data folder and loads them.
     */
    public void loadGamesFromPluginFolder() {
        Deque<File> fileDeque = new ArrayDeque<>();
        File typeFolder = new File(plugin.getDataFolder() + File.separator + "contents" + File.separator + "minigame");
        if (!typeFolder.exists()) {
            if (!typeFolder.mkdirs()) return;
            cn.jrmcdp.craftitem.utils.ConfigUtils.saveResource("contents" + File.separator + "minigame" + File.separator + "default.yml");
        }
        fileDeque.push(typeFolder);
        while (!fileDeque.isEmpty()) {
            File file = fileDeque.pop();
            File[] files = file.listFiles();
            if (files == null) continue;
            for (File subFile : files) {
                if (subFile.isDirectory()) {
                    fileDeque.push(subFile);
                } else if (subFile.isFile() && subFile.getName().endsWith(".yml")) {
                    loadSingleFile(subFile);
                }
            }
        }
    }

    /**
     * Loads a minigame configuration from a YAML file.
     * This method parses the YAML file and extracts minigame configurations to be used in the plugin.
     *
     * @param file The YAML file to load.
     */
    private void loadSingleFile(File file) {
        YamlConfiguration config = cn.jrmcdp.craftitem.utils.ConfigUtils.load(file);
        for (Map.Entry<String, Object> entry : config.getValues(false).entrySet()) {
            if (entry.getValue() instanceof ConfigurationSection) {
                ConfigurationSection section = (ConfigurationSection) entry.getValue();
                GameFactory creator = this.getGameFactory(section.getString("game-type"));
                if (creator == null) {
                    LogUtils.warn("Game type:" + section.getString("game-type") + " doesn't exist.");
                    continue;
                }

                BasicGameConfig.Builder basicGameBuilder = BasicGameConfig.builder();
                Object time = section.get("time", 15);
                if (time instanceof String) {
                    String[] split = ((String) time).split("~");
                    basicGameBuilder.time(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
                } else if (time instanceof Integer) {
                    basicGameBuilder.time((int) time);
                }
                Object difficulty = section.get("difficulty", "20~80");
                if (difficulty instanceof String) {
                    String[] split = ((String) difficulty).split("~");
                    basicGameBuilder.difficulty(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
                } else if (difficulty instanceof Integer) {
                    basicGameBuilder.difficulty((int) difficulty);
                }
                gameInstanceMap.put(entry.getKey(), Pair.of(basicGameBuilder.build(), creator.setArgs(section)));
            }
        }
    }

    private void registerAccurateClickGame() {
        this.registerGameType("accurate_click", (section -> {

            Set<String> chances = Objects.requireNonNull(section.getConfigurationSection("success-rate-sections")).getKeys(false);
            int widthPerSection = section.getInt("arguments.width-per-section", 16);
            double[] successRate = new double[chances.size()];
            for(int i = 0; i < chances.size(); i++)
                successRate[i] = section.getDouble("success-rate-sections." + (i + 1));
            int totalWidth = chances.size() * widthPerSection - 1;
            int pointerOffset = section.getInt("arguments.pointer-offset");
            int pointerWidth = section.getInt("arguments.pointer-width");
            List<String> title = ConfigUtils.stringListArgs(section.get("title"));
            String font = section.getString("subtitle.font");
            String barImage = section.getString("subtitle.bar");
            String pointerImage = section.getString("subtitle.pointer");

            return (game, player, settings) -> new AbstractGamingPlayer(game, player, settings) {

                private int progress = -1;
                private boolean face = true;
                private final String sendTitle = title.get(ThreadLocalRandom.current().nextInt(title.size()));

                
                public void arrangeTask() {
                    double period = ((double) 10*(200-settings.getDifficulty()))/((double) (1+4*settings.getDifficulty()));
                    this.task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this, 1L, (long) period / 50L);
                }

                
                public void onTick() {
                    if (face) progress++;
                    else progress--;
                    if (progress > totalWidth) {
                        face = !face;
                        progress = 2 * totalWidth - progress;
                    } else if (progress < 0) {
                        face = !face;
                        progress = -progress;
                    }
                    showUI();
                }

                public void showUI() {
                    String bar = FontUtils.surroundWithFont(barImage, font)
                            + OffsetUtils.getOffsetChars(pointerOffset + progress)
                            + FontUtils.surroundWithFont(pointerImage, font)
                            + OffsetUtils.getOffsetChars(totalWidth - progress - pointerWidth);
                    AdventureManagerImpl.getInstance().sendTitle(player, sendTitle, bar,0,10,0);
                }

                @Override
                public boolean onLeftClick() {
                    if (task != null) task.cancel();
                    setGameResult(isSuccessful());
                    endGame();
                    return true;
                }

                public boolean isSuccessful() {
                    if (isTimeOut) return false;
                    int last = progress / widthPerSection;
                    return (Math.random() < successRate[last]);
                }
            };
        }));
    }

    private void registerHoldGame() {
        this.registerGameType("hold", (section -> {

            int[] timeRequirements = section.getIntegerList("hold-time-requirements").stream().mapToInt(Integer::intValue).toArray();
            String judgementAreaImage = section.getString("subtitle.judgment-area");
            String pointerImage = section.getString("subtitle.pointer");
            int barEffectiveWidth = section.getInt("arguments.bar-effective-area-width");
            int judgementAreaOffset = section.getInt("arguments.judgment-area-offset");
            int judgementAreaWidth = section.getInt("arguments.judgment-area-width");
            int pointerIconWidth = section.getInt("arguments.pointer-icon-width");
            double punishment = section.getDouble("arguments.punishment");
            String[] progress = section.getStringList("progress").toArray(new String[0]);
            double waterResistance = section.getDouble("arguments.water-resistance", 0.15);
            double pullingStrength = section.getDouble("arguments.pulling-strength", 0.45);
            double looseningLoss = section.getDouble("arguments.loosening-strength-loss", 0.3);

            String title = section.getString("title","{progress}");
            String font = section.getString("subtitle.font");
            String barImage = section.getString("subtitle.bar");
            String tip = section.getString("tip");

            return (game, player, settings) -> new AbstractGamingPlayer(game, player, settings) {
                private double hold_time;
                private double judgement_position;
                private double fish_position;
                private double judgement_velocity;
                private double fish_velocity;
                private int timer;
                private final int time_requirement = timeRequirements[ThreadLocalRandom.current().nextInt(timeRequirements.length)] * 1000;
                private boolean played;

                
                public void arrangeTask() {
                    this.judgement_position = (double) (barEffectiveWidth - judgementAreaWidth) / 2;
                    this.task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this, 1L, 1L);
                }

                
                public void onTick() {
                    if (player.isSneaking()) addV();
                    else reduceV();
                    if (timer < 40 - (settings.getDifficulty() / 10)) {
                        timer++;
                    } else {
                        timer = 0;
                        if (Math.random() > ((double) 25 / (settings.getDifficulty() + 100))) {
                            burst();
                        }
                    }
                    judgement_position += judgement_velocity;
                    fish_position += fish_velocity;
                    fraction();
                    calibrate();
                    if (fish_position >= judgement_position && fish_position + pointerIconWidth <= judgement_position + judgementAreaWidth) {
                        hold_time += 33;
                    } else {
                        hold_time -= punishment * 33;
                    }
                    if (hold_time >= time_requirement) {
                        setGameResult(true);
                        endGame();
                        return;
                    }
                    hold_time = Math.max(0, Math.min(hold_time, time_requirement));
                    showUI();
                }

                private void burst() {
                    if (Math.random() < (judgement_position / barEffectiveWidth)) {
                        judgement_velocity = -1 - 0.8 * Math.random() * ((double) settings.getDifficulty() / 15);
                    } else {
                        judgement_velocity = 1 + 0.8 * Math.random() * ((double) settings.getDifficulty() / 15);
                    }
                }

                private void fraction() {
                    if (judgement_velocity > 0) {
                        judgement_velocity -= waterResistance;
                        if (judgement_velocity < 0) judgement_velocity = 0;
                    } else {
                        judgement_velocity += waterResistance;
                        if (judgement_velocity > 0) judgement_velocity = 0;
                    }
                }

                private void reduceV() {
                    fish_velocity -= looseningLoss;
                }

                private void addV() {
                    played = true;
                    fish_velocity += pullingStrength;
                }

                private void calibrate() {
                    if (fish_position < 0) {
                        fish_position = 0;
                        fish_velocity = 0;
                    }
                    if (fish_position + pointerIconWidth > barEffectiveWidth) {
                        fish_position = barEffectiveWidth - pointerIconWidth;
                        fish_velocity = 0;
                    }
                    if (judgement_position < 0) {
                        judgement_position = 0;
                        judgement_velocity = 0;
                    }
                    if (judgement_position + judgementAreaWidth > barEffectiveWidth) {
                        judgement_position = barEffectiveWidth - judgementAreaWidth;
                        judgement_velocity = 0;
                    }
                }

                public void showUI() {
                    String bar = FontUtils.surroundWithFont(barImage, font)
                            + OffsetUtils.getOffsetChars((int) (judgementAreaOffset + judgement_position))
                            + FontUtils.surroundWithFont(judgementAreaImage, font)
                            + OffsetUtils.getOffsetChars((int) (barEffectiveWidth - judgement_position - judgementAreaWidth))
                            + OffsetUtils.getOffsetChars((int) (-barEffectiveWidth - 1 + fish_position))
                            + FontUtils.surroundWithFont(pointerImage, font)
                            + OffsetUtils.getOffsetChars((int) (barEffectiveWidth - fish_position - pointerIconWidth + 1));
                    AdventureManagerImpl.getInstance().sendTitle(
                            player,
                            tip != null && !played ? tip : title.replace("{progress}", progress[(int) ((hold_time / time_requirement) * progress.length)]),
                            bar,
                            0,
                            10,
                            0
                    );
                }
            };
        }));
    }

    private void registerTensionGame() {
        this.registerGameType("tension", (section -> {

            int fishIconWidth = section.getInt("arguments.fish-icon-width");
            String fishImage = section.getString("subtitle.fish");
            String[] tension = section.getStringList("tension").toArray(new String[0]);
            String[] strugglingFishImage = section.getStringList("subtitle.struggling-fish").toArray(new String[0]);
            int barEffectiveWidth = section.getInt("arguments.bar-effective-area-width");
            int fishOffset = section.getInt("arguments.fish-offset");
            int fishStartPosition = section.getInt("arguments.fish-start-position");
            int successPosition = section.getInt("arguments.success-position");
            double ultimateTension = section.getDouble("arguments.ultimate-tension", 50);
            double normalIncrease = section.getDouble("arguments.normal-pull-tension-increase", 1);
            double strugglingIncrease = section.getDouble("arguments.struggling-tension-increase", 2);
            double tensionLoss = section.getDouble("arguments.loosening-tension-loss", 2);

            String title = section.getString("title","{progress}");
            String font = section.getString("subtitle.font");
            String barImage = section.getString("subtitle.bar");
            String tip = section.getString("tip");

            return (game, player, settings) -> new AbstractGamingPlayer(game, player, settings) {

                private int fish_position = fishStartPosition;
                private double strain;
                private int struggling_time;
                private boolean played;

                
                public void arrangeTask() {
                    this.task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this, 1L, 1L);
                }

                
                public void onTick() {
                    if (struggling_time <= 0) {
                        if (Math.random() < ((double) settings.getDifficulty() / 4000)) {
                            struggling_time = (int) (10 + Math.random() * ((double) settings.getDifficulty() / 4));
                        }
                    } else {
                        struggling_time--;
                    }
                    if (player.isSneaking()) pull();
                    else loosen();
                    if (fish_position < successPosition - fishIconWidth - 1) {
                        setGameResult(true);
                        endGame();
                        return;
                    }
                    if (fish_position + fishIconWidth > barEffectiveWidth || strain >= ultimateTension) {
                        setGameResult(false);
                        endGame();
                        return;
                    }
                    showUI();
                }

                public void pull() {
                    played = true;
                    if (struggling_time > 0) {
                        strain += (strugglingIncrease + ((double) settings.getDifficulty() / 50));
                        fish_position -= 1;
                    } else {
                        strain += normalIncrease;
                        fish_position -= 2;
                    }
                }

                public void loosen() {
                    fish_position++;
                    strain -= tensionLoss;
                }

                public void showUI() {
                    String bar = FontUtils.surroundWithFont(barImage, font)
                            + OffsetUtils.getOffsetChars(fishOffset + fish_position)
                            + FontUtils.surroundWithFont((struggling_time > 0 ? strugglingFishImage[struggling_time % strugglingFishImage.length] : fishImage), font)
                            + OffsetUtils.getOffsetChars(barEffectiveWidth - fish_position - fishIconWidth);
                    strain = Math.max(0, Math.min(strain, ultimateTension));
                    AdventureManagerImpl.getInstance().sendTitle(
                            player,
                            tip != null && !played ? tip : title.replace("{tension}", tension[(int) ((strain / ultimateTension) * tension.length)]),
                            bar,
                            0,
                            10,
                            0
                    );
                }
            };
        }));
    }

    private void registerDanceGame() {
        this.registerGameType("dance", (section -> {

            String subtitle = section.getString("subtitle", "<gray>Dance to win. Time left <white>{time}s");
            String leftNot = section.getString("title.left-button");
            String leftCorrect = section.getString("title.left-button-correct");
            String leftWrong = section.getString("title.left-button-wrong");
            String leftCurrent = section.getString("title.left-button-current");
            String rightNot = section.getString("title.right-button");
            String rightCorrect = section.getString("title.right-button-correct");
            String rightWrong = section.getString("title.right-button-wrong");
            String rightCurrent = section.getString("title.right-button-current");

            String upNot = section.getString("title.up-button");
            String upCorrect = section.getString("title.up-button-correct");
            String upWrong = section.getString("title.up-button-wrong");
            String upCurrent = section.getString("title.up-button-current");
            String downNot = section.getString("title.down-button");
            String downCorrect = section.getString("title.down-button-correct");
            String downWrong = section.getString("title.down-button-wrong");
            String downCurrent = section.getString("title.down-button-current");

            int maxShown = section.getInt("title.display-amount", 7);
            String tip = section.getString("tip");
            boolean easy = section.getBoolean("easy", false);

            Key correctSound = key(section.getString("sound.correct", "minecraft:block.amethyst_block.hit"));
            Key wrongSound = key(section.getString("sound.wrong", "minecraft:block.anvil.land"));

            return (game, player, settings) -> new AbstractGamingPlayer(game, player, settings) {

                private int clickedTimes;
                private int requiredTimes;
                private boolean preventFirst = true;
                // 0 = left / 1 = right / 2 = up / 3 = down
                private int[] order;
                boolean fail = false;

                
                public void arrangeTask() {
                    requiredTimes = settings.getDifficulty() / 4;
                    order = new int[requiredTimes];
                    for (int i = 0; i < requiredTimes; i++) {
                        order[i] = ThreadLocalRandom.current().nextInt(0, easy ? 2 : 4);
                    }
                    this.task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this, 1L, 1L);
                }

                
                public void onTick() {
                    showUI();
                    if (tip != null) {
                        AdventureManagerImpl.getInstance().sendActionbar(player, tip);
                    }
                }

                
                public boolean onRightClick() {
                    preventFirst = true;
                    if (order[clickedTimes] != 1) {
                        setGameResult(false);
                        fail = true;
                        showUI();
                        AdventureManagerImpl.getInstance().sendSound(
                                player,
                                Sound.Source.PLAYER,
                                wrongSound,
                                1,
                                1
                        );
                        endGame();
                        return true;
                    }

                    AdventureManagerImpl.getInstance().sendSound(
                            player,
                            Sound.Source.PLAYER,
                            correctSound,
                            1,
                            1
                    );
                    clickedTimes++;
                    if (clickedTimes >= requiredTimes) {
                        setGameResult(true);
                        showUI();
                        endGame();
                    }
                    return true;
                }

                
                public boolean onJump() {
                    if (order[clickedTimes] != 2) {
                        setGameResult(false);
                        fail = true;
                        showUI();
                        AdventureManagerImpl.getInstance().sendSound(
                                player,
                                Sound.Source.PLAYER,
                                wrongSound,
                                1,
                                1
                        );
                        endGame();
                        return false;
                    }

                    AdventureManagerImpl.getInstance().sendSound(
                            player,
                            Sound.Source.PLAYER,
                            correctSound,
                            1,
                            1
                    );
                    clickedTimes++;
                    if (clickedTimes >= requiredTimes) {
                        setGameResult(true);
                        showUI();
                        endGame();
                    }
                    return false;
                }

                
                public boolean onSneak() {
                    if (order[clickedTimes] != 3) {
                        setGameResult(false);
                        fail = true;
                        showUI();
                        AdventureManagerImpl.getInstance().sendSound(
                                player,
                                Sound.Source.PLAYER,
                                wrongSound,
                                1,
                                1
                        );
                        endGame();
                        return false;
                    }

                    AdventureManagerImpl.getInstance().sendSound(
                            player,
                            Sound.Source.PLAYER,
                            correctSound,
                            1,
                            1
                    );
                    clickedTimes++;
                    if (clickedTimes >= requiredTimes) {
                        setGameResult(true);
                        showUI();
                        endGame();
                    }
                    return false;
                }

                
                public boolean onLeftClick() {
                    if (preventFirst) {
                        preventFirst = false;
                        return false;
                    }

                    if (order[clickedTimes] != 0) {
                        setGameResult(false);
                        fail = true;
                        showUI();
                        AdventureManagerImpl.getInstance().sendSound(
                                player,
                                Sound.Source.PLAYER,
                                wrongSound,
                                1,
                                1
                        );
                        endGame();
                        return true;
                    }

                    AdventureManagerImpl.getInstance().sendSound(
                            player,
                            Sound.Source.PLAYER,
                            correctSound,
                            1,
                            1
                    );
                    clickedTimes++;
                    if (clickedTimes >= requiredTimes) {
                        setGameResult(true);
                        showUI();
                        endGame();
                    }
                    return false;
                }

                public void showUI() {
                    try {
                        if (requiredTimes <= maxShown) {
                            StringBuilder sb = new StringBuilder();
                            for (int x = 0; x < requiredTimes; x++) {
                                if (x < clickedTimes) {
                                    switch (order[x]) {
                                        case 0: sb.append(leftCorrect); break;
                                        case 1: sb.append(rightCorrect); break;
                                        case 2: sb.append(upCorrect); break;
                                        case 3: sb.append(downCorrect); break;
                                    }
                                } else if (clickedTimes == x) {
                                    switch (order[x]) {
                                        case 0: sb.append(fail ? leftWrong : leftCurrent); break;
                                        case 1: sb.append(fail ? rightWrong : rightCurrent); break;
                                        case 2: sb.append(fail ? upWrong : upCurrent); break;
                                        case 3: sb.append(fail ? downWrong : downCurrent); break;
                                    }
                                } else {
                                    switch (order[x]) {
                                        case 0: sb.append(leftNot); break;
                                        case 1: sb.append(rightNot); break;
                                        case 2: sb.append(upNot); break;
                                        case 3: sb.append(downNot); break;
                                    }
                                }
                            }
                            AdventureManagerImpl.getInstance().sendTitle(
                                    player,
                                    sb.toString(),
                                    subtitle.replace("{time}", String.format("%.1f", ((double) deadline - System.currentTimeMillis())/1000)),
                                    0,
                                    10,
                                    0
                            );
                        } else {
                            int half = (maxShown - 1) / 2;
                            int low = clickedTimes - half;
                            int high = clickedTimes + half;
                            if (low < 0) {
                                high += (-low);
                                low = 0;
                            } else if (high >= requiredTimes) {
                                low -= (high - requiredTimes + 1);
                                high = requiredTimes - 1;
                            }
                            StringBuilder sb = new StringBuilder();
                            for (int x = low; x < high + 1; x++) {
                                if (x < clickedTimes) {
                                    switch (order[x]) {
                                        case 0: sb.append(leftCorrect); break;
                                        case 1: sb.append(rightCorrect); break;
                                        case 2: sb.append(upCorrect); break;
                                        case 3: sb.append(downCorrect); break;
                                    }
                                } else if (clickedTimes == x) {
                                    switch (order[x]) {
                                        case 0: sb.append(fail ? leftWrong : leftCurrent); break;
                                        case 1: sb.append(fail ? rightWrong : rightCurrent); break;
                                        case 2: sb.append(fail ? upWrong : upCurrent); break;
                                        case 3: sb.append(fail ? downWrong : downCurrent); break;
                                    }
                                } else {
                                    switch (order[x]) {
                                        case 0: sb.append(leftNot); break;
                                        case 1: sb.append(rightNot); break;
                                        case 2: sb.append(upNot); break;
                                        case 3: sb.append(downNot); break;
                                    }
                                }
                            }
                            AdventureManagerImpl.getInstance().sendTitle(
                                    player,
                                    sb.toString(),
                                    subtitle.replace("{time}", String.format("%.1f", ((double) deadline - System.currentTimeMillis())/1000)),
                                    0,
                                    10,
                                    0
                            );
                        }
                    } catch (Exception e) {
                        LogUtils.warn("显示 dance 小游戏的 UI 时出现错误", e);
                    }
                }
            };
        }));
    }

    private void registerClickGame() {
        this.registerGameType("click", (section -> {

            String title = section.getString("title","<red>{click}");
            String subtitle = section.getString("subtitle", "<gray>Click <white>{clicks} <gray>times to win. Time left <white>{time}s");
            boolean left = section.getBoolean("left-click", true);

            return (game, player, settings) -> new AbstractGamingPlayer(game, player, settings) {

                private int clickedTimes;
                private final int requiredTimes = settings.getDifficulty();
                private boolean preventFirst = true;

                
                public void arrangeTask() {
                    this.task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this, 1L, 1L);
                }

                
                public void onTick() {
                    showUI();
                }

                
                public boolean onRightClick() {
                    if (left) {
                        setGameResult(false);
                        endGame();
                        return true;
                    }
                    clickedTimes++;
                    if (clickedTimes >= requiredTimes) {
                        showUI();
                        setGameResult(true);
                        endGame();
                    }
                    return true;
                }

                
                public boolean onLeftClick() {
                    if (!left) {
                        return false;
                    }
                    if (preventFirst) {
                        preventFirst = false;
                        return false;
                    }
                    clickedTimes++;
                    if (clickedTimes >= requiredTimes) {
                        showUI();
                        setGameResult(true);
                        endGame();
                    }
                    return false;
                }

                public void showUI() {
                    AdventureManagerImpl.getInstance().sendTitle(
                            player,
                            title.replace("{click}", String.valueOf(clickedTimes)),
                            subtitle.replace("{clicks}", String.valueOf(requiredTimes)).replace("{time}", String.format("%.1f", ((double) deadline - System.currentTimeMillis())/1000)),
                            0,
                            10,
                            0
                    );
                }
            };
        }));
    }

    private void registerAccurateClickV2Game() {

        this.registerGameType("accurate_click_v2", (section -> {

            Pair<Integer, Integer> barWidth = ConfigUtils.getIntegerPair(section.getString("title.total-width", "15~20"));
            Pair<Integer, Integer> barSuccess = ConfigUtils.getIntegerPair(section.getString("title.success-width","3~4"));
            String barBody = section.getString("title.body","");
            String barPointer = section.getString("title.pointer", "");
            String barTarget = section.getString("title.target","");

            String subtitle = section.getString("subtitle", "<gray>Reel in at the most critical moment");

            return (game, player, settings) -> new AbstractGamingPlayer(game, player, settings) {

                private final int totalWidth = ThreadLocalRandom.current().nextInt(barWidth.right() - barWidth.left() + 1) + barWidth.left();
                private final int successWidth = ThreadLocalRandom.current().nextInt(barSuccess.right() - barSuccess.left() + 1) + barSuccess.left();
                private final int successPosition = ThreadLocalRandom.current().nextInt((totalWidth - successWidth + 1)) + 1;
                private int currentIndex = 0;
                private int timer = 0;
                private boolean face = true;

                
                public void arrangeTask() {
                    this.task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this, 1L, 1L);
                }

                
                public void onTick() {
                    timer++;
                    if (timer % (21 - settings.getDifficulty() / 5) == 0) {
                        movePointer();
                    }
                    showUI();
                }

                private void movePointer() {
                    if (face) {
                        currentIndex++;
                        if (currentIndex >= totalWidth - 1) {
                            face = false;
                        }
                    } else {
                        currentIndex--;
                        if (currentIndex <= 0) {
                            face = true;
                        }
                    }
                }

                public void showUI() {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 1; i <= totalWidth; i++) {
                        if (i == currentIndex + 1) {
                            stringBuilder.append(barPointer);
                            continue;
                        }
                        if (i >= successPosition && i <= successPosition + successWidth - 1) {
                            stringBuilder.append(barTarget);
                            continue;
                        }
                        stringBuilder.append(barBody);
                    }

                    AdventureManagerImpl.getInstance().sendTitle(
                            player,
                            stringBuilder.toString(),
                            subtitle,
                            0,
                            10,
                            0
                    );
                }


                @Override
                public boolean onLeftClick() {
                    if (task != null) task.cancel();
                    setGameResult(isSuccessful());
                    endGame();
                    return true;
                }
                
                public boolean isSuccessful() {
                    return currentIndex + 1 <= successPosition + successWidth - 1 && currentIndex + 1 >= successPosition;
                }
            };
        }));
    }

    private void registerAccurateClickV3Game() {

        this.registerGameType("accurate_click_v3", (section -> {

            String font = section.getString("subtitle.font");
            String pointerImage = section.getString("subtitle.pointer");
            String barImage = section.getString("subtitle.bar");
            String judgementAreaImage = section.getString("subtitle.judgment-area");
            List<String> titles = ConfigUtils.stringListArgs(section.get("title"));

            int barEffectiveWidth = section.getInt("arguments.bar-effective-area-width");
            int judgementAreaWidth = section.getInt("arguments.judgment-area-width");
            int judgementAreaOffset = section.getInt("arguments.judgment-area-offset");
            int pointerIconWidth = section.getInt("arguments.pointer-icon-width");
            int pointerOffset = section.getInt("arguments.pointer-offset");

            return (game, player, settings) -> new AbstractGamingPlayer(game, player, settings) {

                private int progress = -1;
                private boolean face = true;
                private final int judgement_position = ThreadLocalRandom.current().nextInt(barEffectiveWidth - judgementAreaWidth + 1);
                private final String title = titles.get(ThreadLocalRandom.current().nextInt(titles.size()));

                
                public void arrangeTask() {
                    double period = ((double) 10*(200-settings.getDifficulty()))/((double) (1+4*settings.getDifficulty()));
                    this.task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this, 1L, (long) period / 10L);
                }

                
                public void onTick() {
                    if (face) {
                        progress++;
                        if (progress >= barEffectiveWidth - 1) {
                            face = false;
                        }
                    } else {
                        progress--;
                        if (progress <= 0) {
                            face = true;
                        }
                    }
                    showUI();
                }

                public void showUI() {
                    String bar = FontUtils.surroundWithFont(barImage, font)
                            + OffsetUtils.getOffsetChars(judgementAreaOffset + judgement_position)
                            + FontUtils.surroundWithFont(judgementAreaImage, font)
                            + OffsetUtils.getOffsetChars(barEffectiveWidth - judgement_position - judgementAreaWidth)
                            + OffsetUtils.getOffsetChars(progress + pointerOffset)
                            + FontUtils.surroundWithFont(pointerImage, font)
                            + OffsetUtils.getOffsetChars(barEffectiveWidth - progress - pointerIconWidth + 1);
                    AdventureManagerImpl.getInstance().sendTitle(
                            player,
                            title,
                            bar,
                            0,
                            10,
                            0
                    );
                }


                @Override
                public boolean onLeftClick() {
                    if (task != null) task.cancel();
                    setGameResult(isSuccessful());
                    endGame();
                    return true;
                }
                
                public boolean isSuccessful() {
                    return progress < judgement_position + judgementAreaWidth && progress >= judgement_position;
                }
            };
        }));
    }

    private void registerHoldV2Game() {
        this.registerGameType("hold_v2", (section -> {

            int[] timeRequirements = section.getIntegerList("hold-time-requirements").stream().mapToInt(Integer::intValue).toArray();
            String judgementAreaImage = section.getString("subtitle.judgment-area");
            String pointerImage = section.getString("subtitle.pointer");
            int barEffectiveWidth = section.getInt("arguments.bar-effective-area-width");
            int judgementAreaOffset = section.getInt("arguments.judgment-area-offset");
            int judgementAreaWidth = section.getInt("arguments.judgment-area-width");
            int pointerIconWidth = section.getInt("arguments.pointer-icon-width");
            double punishment = section.getDouble("arguments.punishment");
            String[] progress = section.getStringList("progress").toArray(new String[0]);
            double waterResistance = section.getDouble("arguments.water-resistance", 0.15);
            double pullingStrength = section.getDouble("arguments.pulling-strength", 3);
            double looseningLoss = section.getDouble("arguments.loosening-strength-loss", 0.5);

            String title = section.getString("title", "{progress}");
            String font = section.getString("subtitle.font");
            String barImage = section.getString("subtitle.bar");
            String tip = section.getString("tip");

            boolean left = section.getBoolean("left-click", true);

            return (game, player, settings) -> new AbstractGamingPlayer(game, player, settings) {
                private double hold_time;
                private double judgement_position;
                private double fish_position;
                private double judgement_velocity;
                private double fish_velocity;
                private int timer;
                private final int time_requirement = timeRequirements[ThreadLocalRandom.current().nextInt(timeRequirements.length)] * 1000;
                private boolean played;
                private boolean preventFirst = true;

                
                public void arrangeTask() {
                    this.judgement_position = (double) (barEffectiveWidth - judgementAreaWidth) / 2;
                    this.task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this, 1L, 1L);
                }

                
                public void onTick() {
                    if (timer < 40 - (settings.getDifficulty() / 10)) {
                        timer++;
                    } else {
                        timer = 0;
                        if (Math.random() > ((double) 25 / (settings.getDifficulty() + 100))) {
                            burst();
                        }
                    }
                    judgement_position += judgement_velocity;
                    fish_position += fish_velocity;
                    fraction();
                    calibrate();
                    if (fish_position >= judgement_position && fish_position + pointerIconWidth <= judgement_position + judgementAreaWidth) {
                        hold_time += 33;
                    } else {
                        hold_time -= punishment * 33;
                    }
                    if (hold_time >= time_requirement) {
                        setGameResult(true);
                        endGame();
                        return;
                    }
                    hold_time = Math.max(0, Math.min(hold_time, time_requirement));
                    showUI();
                }

                private void burst() {
                    if (Math.random() < (judgement_position / barEffectiveWidth)) {
                        judgement_velocity = -1 - 0.8 * Math.random() * ((double) settings.getDifficulty() / 15);
                    } else {
                        judgement_velocity = 1 + 0.8 * Math.random() * ((double) settings.getDifficulty() / 15);
                    }
                }

                private void fraction() {
                    if (judgement_velocity > 0) {
                        judgement_velocity -= waterResistance;
                        if (judgement_velocity < 0) judgement_velocity = 0;
                    } else {
                        judgement_velocity += waterResistance;
                        if (judgement_velocity > 0) judgement_velocity = 0;
                    }
                    fish_velocity -= looseningLoss;
                    if (fish_velocity < -10 * looseningLoss) {
                        fish_velocity = -10 * looseningLoss;
                    }
                }

                private void calibrate() {
                    if (fish_position < 0) {
                        fish_position = 0;
                        fish_velocity = 0;
                    }
                    if (fish_position + pointerIconWidth > barEffectiveWidth) {
                        fish_position = barEffectiveWidth - pointerIconWidth;
                        fish_velocity = 0;
                    }
                    if (judgement_position < 0) {
                        judgement_position = 0;
                        judgement_velocity = 0;
                    }
                    if (judgement_position + judgementAreaWidth > barEffectiveWidth) {
                        judgement_position = barEffectiveWidth - judgementAreaWidth;
                        judgement_velocity = 0;
                    }
                }

                
                public boolean onRightClick() {
                    if (left) {
                        setGameResult(false);
                        endGame();
                        return true;
                    }
                    played = true;
                    fish_velocity = pullingStrength;
                    return true;
                }

                
                public boolean onLeftClick() {
                    if (preventFirst) {
                        preventFirst = false;
                        return false;
                    }
                    if (left) {
                        played = true;
                        fish_velocity = pullingStrength;
                    }
                    return false;
                }

                public void showUI() {
                    String bar = FontUtils.surroundWithFont(barImage, font)
                            + OffsetUtils.getOffsetChars((int) (judgementAreaOffset + judgement_position))
                            + FontUtils.surroundWithFont(judgementAreaImage, font)
                            + OffsetUtils.getOffsetChars((int) (barEffectiveWidth - judgement_position - judgementAreaWidth))
                            + OffsetUtils.getOffsetChars((int) (-barEffectiveWidth - 1 + fish_position))
                            + FontUtils.surroundWithFont(pointerImage, font)
                            + OffsetUtils.getOffsetChars((int) (barEffectiveWidth - fish_position - pointerIconWidth + 1));
                    AdventureManagerImpl.getInstance().sendTitle(
                            player,
                            tip != null && !played ? tip : title.replace("{progress}", progress[(int) ((hold_time / time_requirement) * progress.length)]),
                            bar,
                            0,
                            10,
                            0
                    );
                }
            };
        }));
    }

    private static Key key(String string) {
        return Key.key(string, ':');
    }
}
