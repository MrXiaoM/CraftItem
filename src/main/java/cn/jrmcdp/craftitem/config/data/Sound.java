package cn.jrmcdp.craftitem.config.data;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import static cn.jrmcdp.craftitem.utils.Utils.valueOfOrNull;

public class Sound {
    private @Nullable org.bukkit.Sound sound;
    private float volume = 1.0f, pitch = 1.0f;
    public Sound(ConfigurationSection section, String key, String... def) {
        this(section.getString(key), def);
    }
    public Sound(@Nullable String key, String... def) {
        this.sound = valueOfOrNull(org.bukkit.Sound.class, key, def);
    }

    public float getVolume() {
        return volume;
    }

    public float getPitch() {
        return pitch;
    }

    public Sound setPitch(float pitch) {
        this.pitch = pitch;
        return this;
    }

    public Sound setVolume(float volume) {
        this.volume = volume;
        return this;
    }

    public void play(Player player) {
        if (sound == null) return;
        Location loc = player.getLocation();
        player.playSound(loc, sound, volume, pitch);
    }
}
