package cl.nightcore.mythicProjectiles.placeholderapi;

import cl.nightcore.mythicProjectiles.MythicProjectiles;
import cl.nightcore.mythicProjectiles.config.ConfigManager;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class AuraLevelExpansion extends PlaceholderExpansion {

    private final MythicProjectiles plugin;
    private final LegacyComponentSerializer serializer;

    public AuraLevelExpansion(MythicProjectiles plugin) {
        this.plugin = plugin;
        this.serializer = LegacyComponentSerializer.builder()
                .character('&')
                .hexColors()
                .build();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "moblevels"; // Este es el identificador de tu expansión (usado como %miplugin_placeholder%)
    }

    @Override
    public @NotNull String getAuthor() {
        return "Haste66";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true; // Esto asegura que la expansión se mantenga cargada incluso si PlaceholderAPI se recarga
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        if (params.equalsIgnoreCase("coloredLevel")) {
            Component component = getAverageColored(player);
            return serializer.serialize(component) + "&r";
        }

        return null;
    }

    private Component getAverageColored(Player player) {

        int level = (int) Math.round(AuraSkillsApi.get().getUser(player.getUniqueId()).getSkillAverage());
        return ConfigManager.formatLevel2(String.valueOf(level));
    }


}