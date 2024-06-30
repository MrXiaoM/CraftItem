package cn.jrmcdp.craftitem.config;

import cn.jrmcdp.craftitem.ColorHelper;
import org.bukkit.entity.Player;

public class Title {
    String title;
    String subTitle;
    int fadeIn;
    int stay;
    int fadeOut;

    public Title(String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        this.title = ColorHelper.parseColor(title);
        this.subTitle = ColorHelper.parseColor(subTitle);
        this.fadeIn = fadeIn < 1 ? 10 : fadeIn;
        this.stay = stay < 1 ? 20 : stay;
        this.fadeOut = fadeOut < 1 ? 10 : fadeOut;
    }

    public void send(Player player) {
        player.sendTitle(title, subTitle, fadeIn, stay, fadeOut);
    }
}
