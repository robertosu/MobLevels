package cl.nightcore.mythicProjectiles.listener;

import cl.nightcore.mythicProjectiles.boss.BossDifficulty;
import cl.nightcore.mythicProjectiles.boss.WorldBoss;
import cl.nightcore.mythicProjectiles.config.ConfigManager;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import io.lumine.mythic.core.mobs.ActiveMob;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import static cl.nightcore.mythicProjectiles.util.MobUtil.getLevel;

public class BossListener implements Listener {

    private static final double BOSS_ANNOUNCE_RADIUS = 100.0;


    @EventHandler(priority = EventPriority.HIGH)
    public void onBossDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = event.getEntity().getKiller();
        // Verificar si la entidad que murió es un jefe
        if (WorldBoss.isBoss(entity) && killer != null) {
            BossDifficulty difficulty = WorldBoss.getBossDifficulty(entity);
            if (difficulty == null) return;
            // Anunciar la muerte del jefe
            announceBossDeath(entity, difficulty, killer);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMythicBossDeath(MythicMobDeathEvent event) {
        Entity entity = event.getEntity();
        Entity killer = event.getKiller();
        // Verificar si la entidad que murió es un jefe
        if (killer instanceof Player player && WorldBoss.isBoss(entity)) {
            BossDifficulty difficulty = WorldBoss.getBossDifficulty(entity);
            if (difficulty == null) return;
            // Anunciar la muerte del jefe
            announceBossDeathMythic(event.getMob(), difficulty, player);
        }
    }

    public static void announceBossSpawn(LivingEntity boss, BossDifficulty difficulty, int radiusMultiplier) {
        Component message = Component.text("⚔ ", NamedTextColor.RED)
                .append(Component.text("¡Un jefe", NamedTextColor.GRAY))
                .append(ConfigManager.formatLevel(String.valueOf(getLevel(boss))))
                .append(Component.translatable(boss.getType().translationKey(), NamedTextColor.WHITE))
                .append(Component.text(" "))
                .append(Component.text(difficulty.getDisplayName(), difficulty.getColor()))
                .append(Component.text(" ha aparecido cerca!", NamedTextColor.GRAY))
                .append(Component.text(" ⚔", NamedTextColor.RED));

        // Notificar a jugadores cercanos
        for (Player player : boss.getWorld().getPlayers()) {
            if (player.getLocation().distance(boss.getLocation()) <= BOSS_ANNOUNCE_RADIUS * radiusMultiplier) {
                player.sendMessage(message);
                player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.5f, 1.0f);
            }
        }
    }

    public static void announceBossSpawnMythic(ActiveMob boss, BossDifficulty difficulty, int radiusMultiplier) {
        String bossName = boss.getDisplayName();
        Entity entity = boss.getEntity().getBukkitEntity();

        Component bossNameComponent = MiniMessage.miniMessage().deserialize(bossName);

        Component message = Component.text("⚔ ", NamedTextColor.RED)
                .append(Component.text("¡Un jefe", NamedTextColor.GRAY))
                .append(ConfigManager.formatLevel(String.valueOf(getLevel(entity))))
                .append(bossNameComponent)
                .append(Component.text(" "))
                .append(Component.text(difficulty.getDisplayName(), difficulty.getColor()))
                .append(Component.text(" ha aparecido cerca!", NamedTextColor.GRAY))
                .append(Component.text(" ⚔", NamedTextColor.RED));

        // Notificar a jugadores cercanos
        for (Player player : entity.getWorld().getPlayers()) {
            if (player.getLocation().distance(entity.getLocation()) <= BOSS_ANNOUNCE_RADIUS * radiusMultiplier) {
                player.sendMessage(message);
                player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.5f, 1.0f);
            }
        }
    }



    private void announceBossDeath(Entity boss, BossDifficulty difficulty, Player killer) {
        Component message = Component.text("☠ ", NamedTextColor.DARK_RED)
                .append(Component.text("El jefe ", NamedTextColor.RED))
                .append(ConfigManager.formatLevel(String.valueOf(getLevel(boss))))
                .append(Component.translatable(boss.getType().translationKey(), NamedTextColor.WHITE))
                .append(Component.text(" "))
                .append(Component.text(difficulty.getDisplayName(), difficulty.getColor()))
                .append(Component.text(" ha sido derrotado", NamedTextColor.RED))
                .append(Component.text(" ☠", NamedTextColor.DARK_RED));



            // Anunciar a todos los jugadores en el mundo y reproducir el sonido al jugador
            Bukkit.broadcast(message);
            killer.playSound(killer.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);

    }

    private void announceBossDeathMythic(ActiveMob boss, BossDifficulty difficulty, Player killer) {
        String bossName = boss.getDisplayName();
        Entity entity = boss.getEntity().getBukkitEntity();

        Component bossNameComponent = MiniMessage.miniMessage().deserialize(bossName);


        Component message = Component.text("☠ ", NamedTextColor.DARK_RED)
                .append(Component.text("El jefe ", NamedTextColor.RED))
                .append(ConfigManager.formatLevel(String.valueOf(getLevel(entity))))
                .append(bossNameComponent)
                .append(Component.text(" "))
                .append(Component.text(difficulty.getDisplayName(), difficulty.getColor()))
                .append(Component.text(" ha sido derrotado", NamedTextColor.RED))
                .append(Component.text(" ☠", NamedTextColor.DARK_RED));

        // Anunciar a todos los jugadores en el mundo y reproducir el sonido al jugador
        Bukkit.broadcast(message);
        killer.playSound(killer.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
    }
}