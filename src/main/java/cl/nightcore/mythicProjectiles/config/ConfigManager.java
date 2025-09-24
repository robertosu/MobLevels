package cl.nightcore.mythicProjectiles.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigManager {

    private static final int MAX_CACHED_LEVEL = 100;
    public static boolean spigotMode = false;
    private static Map<String, Component> levelComponentCache = new ConcurrentHashMap<>();
    private static Map<String, Component> levelComponentCache2 = new ConcurrentHashMap<>();
    private static final Map<Range, String> gradientFormats = new HashMap<>();

    private static boolean useTieredGradients;
    private static boolean levelAsPrefix;
    private static String levelFormat;
    private static String levelFormat2;
    private static String defaultGradient;
    private static boolean showLevels;
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public FileConfiguration config;

    public ConfigManager(Plugin plugin) {
        config = plugin.getConfig();
        useTieredGradients = config.getBoolean("use-tiered-gradients", false);
        levelFormat = config.getString("format.level-format", "[Lvl.{level}]");
        levelFormat2 = config.getString("format.level-format2", "[Lvl.{level}]");
        showLevels = config.getBoolean("show-level", true);
        levelAsPrefix = config.getBoolean("level-as-prefix", false); // false means suffix by default

        loadTieredGradients();
        precacheLevelComponents();
        precacheLevelComponents2();
    }

    private static Component createLevelComponent(String level) {
        if (!useTieredGradients) {
            return miniMessage.deserialize(levelFormat,
                    Placeholder.unparsed("level", level)
            );
        }

        try {
            int levelInt = Integer.parseInt(level);
            String gradient = getGradientFormatForLevel(levelInt);
            // Aplicamos el gradiente al formato base
            String formattedText = String.format("<%s>%s</gradient>",
                    gradient,
                    levelFormat.replace("{level}", level)
            );
            return miniMessage.deserialize(formattedText);
        } catch (NumberFormatException e) {
            return miniMessage.deserialize(levelFormat,
                    Placeholder.unparsed("level", level)
            );
        }
    }

    private static Component createLevelComponent2(String level) {
        if (!useTieredGradients) {
            return miniMessage.deserialize(levelFormat2,
                    Placeholder.unparsed("level", level)
            );
        }

        try {
            int levelInt = Integer.parseInt(level);
            String gradient = getGradientFormatForLevel(levelInt);
            // Aplicamos el gradiente al formato base
            String formattedText = String.format("<%s>%s</gradient>",
                    gradient,
                    levelFormat2.replace("{level}", level)
            );
            return miniMessage.deserialize(formattedText);
        } catch (NumberFormatException e) {
            return miniMessage.deserialize(levelFormat2,
                    Placeholder.unparsed("level", level)
            );
        }
    }

    private static String getGradientFormatForLevel(int level) {
        for (Map.Entry<Range, String> entry : gradientFormats.entrySet()) {
            if (entry.getKey().contains(level)) {
                return entry.getValue();
            }
        }
        return defaultGradient != null ? defaultGradient : "gradient:#FFFFFF:#CCCCCC";
    }

    public static Component formatLevel(String level) {
        if (!showLevels || level == null || level.isEmpty()) {
            return Component.empty();
        }
        return levelComponentCache.computeIfAbsent(level, ConfigManager::createLevelComponent);
    }

    public static Component formatLevel2(String level) {
        if (!showLevels || level == null || level.isEmpty()) {
            return Component.empty();
        }
        return levelComponentCache2.computeIfAbsent(level, ConfigManager::createLevelComponent2);
    }

    private void loadTieredGradients() {
        ConfigurationSection gradientSection = config.getConfigurationSection("tiered-gradients");
        if (gradientSection == null) {
            Bukkit.getLogger().warning("No se encontró la sección de gradientes en la configuración");
            return;
        }

        gradientFormats.clear();

        for (String key : gradientSection.getKeys(false)) {
            if (key.equals("default")) {
                defaultGradient = gradientSection.getString(key);
                Bukkit.getLogger().info("Gradiente por defecto cargado: " + defaultGradient);
                continue;
            }

            String[] range = key.split("-");
            if (range.length == 2) {
                try {
                    int min = Integer.parseInt(range[0]);
                    int max = Integer.parseInt(range[1]);
                    String gradientFormat = gradientSection.getString(key);
                    gradientFormats.put(new Range(min, max), gradientFormat);
                    Bukkit.getLogger().info("Rango de gradiente cargado: " + min + "-" + max + " = " + gradientFormat);
                } catch (NumberFormatException e) {
                    Bukkit.getLogger().warning("Error al parsear el rango: " + key);
                }
            }
        }
        Bukkit.getLogger().info("Total de rangos de gradiente cargados: " + gradientFormats.size());
    }

    private void precacheLevelComponents() {
        levelComponentCache = new ConcurrentHashMap<>();
        for (int i = 0; i <= MAX_CACHED_LEVEL; i++) {
            String level = String.valueOf(i);
            levelComponentCache.put(level, createLevelComponent(level));
        }
        Bukkit.getLogger().info("Precached " + MAX_CACHED_LEVEL + " level components");
    }

    private void precacheLevelComponents2() {
        levelComponentCache2 = new ConcurrentHashMap<>();
        for (int i = 0; i <= MAX_CACHED_LEVEL; i++) {
            String level = String.valueOf(i);
            levelComponentCache2.put(level, createLevelComponent2(level));
        }
        Bukkit.getLogger().info("Precached " + MAX_CACHED_LEVEL + " level components");
    }

    private record Range(int min, int max) {
        boolean contains(int value) {
            return value >= min && value <= max;
        }
    }

    public static boolean isLevelAsPrefix() {
        return levelAsPrefix;
    }
}