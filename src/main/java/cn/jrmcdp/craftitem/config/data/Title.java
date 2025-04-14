package cn.jrmcdp.craftitem.config.data;

import cn.jrmcdp.craftitem.minigames.utils.AdventureManagerImpl;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import top.mrxiaom.pluginbase.utils.AdventureUtil;

public class Title {
    public final Component title;
    public final Component subTitle;
    public final int fadeIn;
    public final int stay;
    public final int fadeOut;

    public Title(String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        this.title = AdventureUtil.miniMessage(title);
        this.subTitle = AdventureUtil.miniMessage(subTitle);
        this.fadeIn = fadeIn < 1 ? 10 : fadeIn;
        this.stay = stay < 1 ? 20 : stay;
        this.fadeOut = fadeOut < 1 ? 10 : fadeOut;
    }

    public void send(Player player) {
        AdventureManagerImpl.sendTitle(player, title, subTitle, fadeIn, stay, fadeOut);
    }
}
