package cl.nightcore.mythicProjectiles;

import cl.nightcore.mythicProjectiles.command.ReloadCommand;
import cl.nightcore.mythicProjectiles.config.ConfigManager;
import cl.nightcore.mythicProjectiles.listener.*;
import cl.nightcore.mythicProjectiles.nametag.NametagManager;
import cl.nightcore.mythicProjectiles.placeholderapi.AuraLevelExpansion;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class MythicProjectiles extends JavaPlugin implements Listener {

    public static NamespacedKey levelKey;
    private static MythicProjectiles instance;
    private ConfigManager configManager;
    private NametagManager nametagManager;

    public static MythicProjectiles getInstance() {
        return instance;
    }

    public static int getLevel(Entity entity) {
        return entity.getPersistentDataContainer().getOrDefault(levelKey, PersistentDataType.INTEGER, 0);
    }

    public static int getPlayerLevel(Player player) {
        return (int )Math.round(AuraSkillsApi.get().getUserManager().getUser(player.getUniqueId()).getSkillAverage());
    }



    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new SpawnListeners(), this);
        getServer().getPluginManager().registerEvents(new DamageListeners(), this);
        getServer().getPluginManager().registerEvents(new CombustListener(), this);
        getServer().getPluginManager().registerEvents(new DeathMessageListeners(), this);
        getServer().getPluginManager().registerEvents(new XpGainListener(), this);

        Objects.requireNonNull(getCommand("moblevels")).setExecutor(new ReloadCommand(this));
        levelKey = new NamespacedKey(this, "level");
        configManager = new ConfigManager(this);
        nametagManager = new NametagManager(this);
        instance = this;
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new AuraLevelExpansion(this).register();
        }
    }

    @Override
    public void onDisable() {
        nametagManager.onDisable();
    }

    public void reloadPlugin(){
        this.reloadConfig();
        configManager = new ConfigManager(this);
        nametagManager = new NametagManager(this);
    }


    public ConfigManager getConfigManager() {
        return configManager;
    }
}