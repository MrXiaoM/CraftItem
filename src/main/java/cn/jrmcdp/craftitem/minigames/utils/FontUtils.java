package cn.jrmcdp.craftitem.minigames.utils;

/**
 * Utility class for working with fonts in text.
 */
public class FontUtils {

    private FontUtils() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    /**
     * Surrounds the given text with a specified font tag.
     *
     * @param text The text to be surrounded with the font tag.
     * @param font The font to use in the font tag.
     * @return The input text surrounded by the font tag.
     */
    public static String surroundWithFont(String text, String font) {
        return "<font:" + font + ">" + text + "</font>";
    }
}