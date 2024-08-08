package cn.jrmcdp.craftitem.minigames.utils;

import cn.jrmcdp.craftitem.minigames.GameManager;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

/**
 * Utility class for logging messages with various log levels.
 */
@SuppressWarnings({"unused"})
public final class LogUtils {

    /**
     * Log an informational message.
     *
     * @param message The message to log.
     */
    public static void info(@NotNull String message) {
        GameManager.getPlugin().getLogger().info(message);
    }

    /**
     * Log a warning message.
     *
     * @param message The message to log.
     */
    public static void warn(@NotNull String message) {
        GameManager.getPlugin().getLogger().warning(message);
    }

    /**
     * Log a severe error message.
     *
     * @param message The message to log.
     */
    public static void severe(@NotNull String message) {
        GameManager.getPlugin().getLogger().severe(message);
    }

    /**
     * Log a warning message with a throwable exception.
     *
     * @param message    The message to log.
     * @param throwable  The throwable exception to log.
     */
    public static void warn(@NotNull String message, Throwable throwable) {
        GameManager.getPlugin().getLogger().log(Level.WARNING, message, throwable);
    }

    /**
     * Log a severe error message with a throwable exception.
     *
     * @param message    The message to log.
     * @param throwable  The throwable exception to log.
     */
    public static void severe(@NotNull String message, Throwable throwable) {
        GameManager.getPlugin().getLogger().log(Level.SEVERE, message, throwable);
    }

    private LogUtils() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}