package cn.jrmcdp.craftitem.config.data;

import cn.jrmcdp.craftitem.ColorHelper;
import cn.jrmcdp.craftitem.minigames.utils.AdventureManagerImpl;
import org.bukkit.entity.Player;

public class Title {
    public final String title;
    public final String subTitle;
    public final int fadeIn;
    public final int stay;
    public final int fadeOut;

    public Title(String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        this.title = ColorHelper.parseColor(title);
        this.subTitle = ColorHelper.parseColor(subTitle);
        this.fadeIn = fadeIn < 1 ? 10 : fadeIn;
        this.stay = stay < 1 ? 20 : stay;
        this.fadeOut = fadeOut < 1 ? 10 : fadeOut;
    }

    public void send(Player player) {
        AdventureManagerImpl.getInstance().sendTitle(player, title, subTitle, fadeIn, stay, fadeOut);
    }
}
