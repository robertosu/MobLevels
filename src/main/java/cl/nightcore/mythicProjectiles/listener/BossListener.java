package cl.nightcore.mythicProjectiles.listener;

import cl.nightcore.mythicProjectiles.boss.BossDifficulty;
import cl.nightcore.mythicProjectiles.boss.WorldBoss;
import cl.nightcore.mythicProjectiles.config.ConfigManager;
import cl.nightcore.mythicProjectiles.nametag.NametagManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import static cl.nightcore.mythicProjectiles.MythicProjectiles.getLevel;

public class BossListener implements Listener {

    private static final double BOSS_ANNOUNCE_RADIUS = 100.0;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();

        // Verificar si la entidad spawneada es un jefe
        if (WorldBoss.isBoss(entity)) {
            BossDifficulty difficulty = WorldBoss.getBossDifficulty(entity);
            if (difficulty == null) return;

            // Anunciar el spawn del jefe a jugadores cercanos
            announceBossSpawn(entity, difficulty);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBossDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        // Verificar si la entidad que murió es un jefe
        if (WorldBoss.isBoss(entity)) {
            BossDifficulty difficulty = WorldBoss.getBossDifficulty(entity);
            if (difficulty == null) return;

            // Anunciar la muerte del jefe
            announceBossDeath(entity, difficulty);
            
            // Bonificar XP adicional
            bonifyBossKill(event, difficulty);
        }
    }

    private void announceBossSpawn(LivingEntity boss, BossDifficulty difficulty) {
        Component message = Component.text("⚔ ", NamedTextColor.RED)
                .append(Component.text("¡Un jefe ", NamedTextColor.YELLOW))
                .append(ConfigManager.formatLevel(String.valueOf(getLevel(boss))))
                .append(Component.text(" "))
                .append(Component.translatable(boss.getType().translationKey(), NamedTextColor.WHITE))
                .append(Component.text(difficulty.getDisplayName(), difficulty.getColor()))
                .append(Component.text(" ha aparecido cerca!", NamedTextColor.YELLOW))
                .append(Component.text(" ⚔", NamedTextColor.RED));

        // Notificar a jugadores cercanos
        for (Player player : boss.getWorld().getPlayers()) {
            if (player.getLocation().distance(boss.getLocation()) <= BOSS_ANNOUNCE_RADIUS) {
                player.sendMessage(message);
                player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.5f, 1.0f);
            }
        }
    }



    private void announceBossDeath(LivingEntity boss, BossDifficulty difficulty) {
        Component message = Component.text("☠ ", NamedTextColor.DARK_RED)
                .append(Component.text("El jefe ", NamedTextColor.RED))
                .append(ConfigManager.formatLevel(String.valueOf(getLevel(boss))))
                .append(Component.text(" "))
                .append(Component.translatable(boss.getType().translationKey(), NamedTextColor.WHITE))
                .append(Component.text(difficulty.getDisplayName(), difficulty.getColor()))
                .append(Component.text(" ha sido derrotado", NamedTextColor.RED))
                .append(Component.text(" ☠", NamedTextColor.DARK_RED));

        // Anunciar a todos los jugadores en el mundo
        for (Player player : boss.getWorld().getPlayers()) {
            player.sendMessage(message);
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }
    }



    private void bonifyBossKill(EntityDeathEvent event, BossDifficulty difficulty) {
        // Multiplicar la experiencia basado en la dificultad del jefe
        int bonusXp = switch (difficulty) {
            case EASY -> 50;
            case NORMAL -> 100;
            case HARD -> 200;
            case EXTREME -> 400;
        };
        
        event.setDroppedExp(event.getDroppedExp() + bonusXp);
    }

}