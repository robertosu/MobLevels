package cl.nightcore.mythicProjectiles.nametag;

import cl.nightcore.mythicProjectiles.MythicProjectiles;
import cl.nightcore.mythicProjectiles.boss.BossDifficulty;
import cl.nightcore.mythicProjectiles.boss.WorldBoss;
import cl.nightcore.mythicProjectiles.config.ConfigManager;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import io.lumine.mythic.bukkit.MythicBukkit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static cl.nightcore.mythicProjectiles.util.MobUtil.getLevel;
import static cl.nightcore.mythicProjectiles.config.ConfigManager.spigotMode;

public class NametagManager implements Listener {
    // Rangos diferentes para mobs normales y bosses
    private static final double NORMAL_NAMETAG_RADIUS = 20.0;
    private static final double BOSS_NAMETAG_RADIUS = 50.0; // Más lejos para bosses

    private final MythicProjectiles plugin;
    private final NametagPacketSender nametagSender;
    private final ProtocolManager protocolManager;
    private final Map<Player, Set<Integer>> playerNametaggedEntities = new HashMap<>();
    private BukkitTask globalNametagTask;

    public NametagManager(MythicProjectiles plugin) {
        this.plugin = plugin;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        this.nametagSender = new NametagPacketSender(plugin);
        startGlobalNametagUpdates();
    }

    private void startGlobalNametagUpdates() {
        if (!spigotMode) {
            globalNametagTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                for (Player viewer : Bukkit.getOnlinePlayers()) {
                    updateNametagsForPlayer(viewer);
                }
            }, 0L, 20L);
        }
    }

    private void updateNametagsForPlayer(Player viewer) {
        // Usar el rango máximo para obtener todas las entidades posibles
        double maxRadius = Math.max(BOSS_NAMETAG_RADIUS, NORMAL_NAMETAG_RADIUS);

        List<Entity> nearbyEntities = viewer.getNearbyEntities(maxRadius, maxRadius, maxRadius)
                .stream()
                .filter(entity -> entity instanceof LivingEntity && !(entity instanceof Player) /*&& !(entity instanceof ArmorStand stand)*/)
                .toList();

        Set<Integer> currentNametaggedEntities = playerNametaggedEntities.computeIfAbsent(viewer, k -> new HashSet<>());
        Set<Integer> updatedEntities = new HashSet<>();

        for (Entity entity : nearbyEntities) {
          /*  if (entity instanceof  ArmorStand stand){
                if  (!stand.hasMetadata("MythicMobs")){
                    continue;
                }
            }*/
            LivingEntity livingEntity = (LivingEntity) entity;
            int entityId = entity.getEntityId();

            // Determinar el rango apropiado para esta entidad
            double entityRadius = getEntityNametagRadius(livingEntity);
            double distanceToEntity = viewer.getLocation().distance(entity.getLocation());

            // Solo procesar si está dentro del rango apropiado
            if (distanceToEntity <= entityRadius) {
                updatedEntities.add(entityId);

                if (viewer.hasLineOfSight(entity)) {
                    // Verificar si es un jefe
                    if (WorldBoss.isBoss(livingEntity)) {
                        handleBossNametag(viewer, livingEntity);
                    } else {
                        handleNormalMobNametag(viewer, livingEntity);
                    }
                } else {
                    // Si no tiene línea de visión, ocultar la nametag
                    hideNametag(viewer, entityId);
                }
            }
        }

        // Eliminar nametags para entidades que ya no están en rango
        Set<Integer> entitiesToRemove = new HashSet<>(currentNametaggedEntities);
        entitiesToRemove.removeAll(updatedEntities);

        for (Integer entityId : entitiesToRemove) {
            hideNametag(viewer, entityId);
        }

        // Actualizar el conjunto de entidades etiquetadas para este jugador
        currentNametaggedEntities.clear();
        currentNametaggedEntities.addAll(updatedEntities);
    }

    /**
     * Determina el rango de nametag apropiado para una entidad
     */
    private double getEntityNametagRadius(LivingEntity entity) {
        return WorldBoss.isBoss(entity) ? BOSS_NAMETAG_RADIUS : NORMAL_NAMETAG_RADIUS;
    }

    private void handleBossNametag(Player viewer, LivingEntity entity) {


        BossDifficulty difficulty = WorldBoss.getBossDifficulty(entity);
        if (difficulty == null) return;

        int level = getLevel(entity);

        Component levelComponent = ConfigManager.formatLevel(String.valueOf(level));
        Component difficultyComponent = getBossDifficultyComponent(difficulty);

        Component prefix, suffix;

        if (ConfigManager.isLevelAsPrefix()) {
            // Formato: <nivel> <nombredelmob> <dificultadcoloreada>
            prefix = levelComponent;
            suffix = Component.text(" ").append(difficultyComponent);
        } else {
            // Formato: <dificultadcoloreada> <nombredelmob> <nivel>
            prefix = difficultyComponent;
            suffix = Component.text(" ").append(levelComponent);
        }

        nametagSender.sendNametagWithTranslationKey(
                viewer,
                entity,
                prefix,
                suffix,
                Component.text(entity.getType().name())
        );
    }

    @NotNull
    private static Component getBossDifficultyComponent(BossDifficulty difficulty) {
        // Obtener el color de la dificultad desde el enum
        TextColor difficultyColor = difficulty.getColor();

        // Si el color no está definido en el enum, usar colores por defecto
        if (difficultyColor == null) {
            difficultyColor = switch (difficulty) {
                case EASY -> NamedTextColor.GREEN;
                case NORMAL -> NamedTextColor.YELLOW;
                case HARD -> NamedTextColor.RED;
                case EXTREME -> NamedTextColor.DARK_PURPLE;
            };
        }

        // Crear solo el componente de dificultad coloreada
        return Component.text(difficulty.getDisplayName(), difficultyColor)
                .decorate(TextDecoration.BOLD);
    }

    private void handleNormalMobNametag(Player viewer, LivingEntity entity) {
        Component levelComponent = ConfigManager.formatLevel(String.valueOf(getLevel(entity)));

        // Para mobs normales, mantener la configuración original
        Component prefix = ConfigManager.isLevelAsPrefix() ? levelComponent : null;
        Component suffix = ConfigManager.isLevelAsPrefix() ? null : levelComponent;

        nametagSender.sendNametagWithTranslationKey(
                viewer,
                entity,
                prefix,
                suffix,
                Component.text(entity.getType().name())
        );
    }

    private void hideNametag(Player viewer, int entityId) {
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        packet.getIntegers().write(0, entityId);

        List<WrappedDataValue> dataValues = new ArrayList<>();
        WrappedDataWatcher.Serializer booleanSerializer = WrappedDataWatcher.Registry.get(Boolean.class);

        // Establecer la visibilidad del nametag como falsa
        dataValues.add(new WrappedDataValue(3, booleanSerializer, false));

        packet.getDataValueCollectionModifier().write(0, dataValues);
        protocolManager.sendServerPacket(viewer, packet);
    }

    public void onDisable() {
        if (globalNametagTask != null) {
            globalNametagTask.cancel();
        }
        // Limpiar el mapa de entidades etiquetadas
        playerNametaggedEntities.clear();
    }
}