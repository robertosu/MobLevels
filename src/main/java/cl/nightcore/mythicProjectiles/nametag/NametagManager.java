package cl.nightcore.mythicProjectiles.nametag;

import cl.nightcore.mythicProjectiles.MythicProjectiles;
import cl.nightcore.mythicProjectiles.config.ConfigManager;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

import static cl.nightcore.mythicProjectiles.MythicProjectiles.getLevel;

public class NametagManager implements Listener {
    private static final double NAMETAG_RADIUS = 20.0;
    private final MythicProjectiles plugin;
    private final NametagPacketSender nametagSender;
    private final ProtocolManager protocolManager;
    // Nuevo mapa para rastrear entidades etiquetadas por cada jugador
    private final Map<Player, Set<Integer>> playerNametaggedEntities = new HashMap<>();
    private BukkitTask globalNametagTask;

    public NametagManager(MythicProjectiles plugin) {
        this.plugin = plugin;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        this.nametagSender = new NametagPacketSender(plugin);
        startGlobalNametagUpdates();
    }

    private void startGlobalNametagUpdates() {
        globalNametagTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                updateNametagsForPlayer(viewer);
            }
        }, 0L, 20L);
    }

    private void updateNametagsForPlayer(Player viewer) {
        List<Entity> nearbyEntities = viewer.getNearbyEntities(NAMETAG_RADIUS, NAMETAG_RADIUS, NAMETAG_RADIUS)
                .stream()
                .filter(entity -> entity instanceof LivingEntity && !(entity instanceof Player) && !(entity instanceof ArmorStand))
                .toList();

        Set<Integer> currentNametaggedEntities = playerNametaggedEntities.computeIfAbsent(viewer, k -> new HashSet<>());
        Set<Integer> updatedEntities = new HashSet<>();

        for (Entity entity : nearbyEntities) {
            int entityId = entity.getEntityId();
            updatedEntities.add(entityId);

            if (viewer.hasLineOfSight(entity)) {
                Component levelComponent = ConfigManager.formatLevel(String.valueOf(getLevel(entity)));

                // Use the config value to determine prefix/suffix placement
                Component prefix = ConfigManager.isLevelAsPrefix() ? levelComponent : null;
                Component suffix = ConfigManager.isLevelAsPrefix() ? null : levelComponent;

                nametagSender.sendNametagWithTranslationKey(
                        viewer,
                        entity,
                        prefix,
                        suffix,
                        Component.text(entity.getType().name())
                );
            } else {
                // Si no tiene línea de visión, ocultar la nametag
                PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
                packet.getIntegers().write(0, entityId);

                List<WrappedDataValue> dataValues = new ArrayList<>();
                WrappedDataWatcher.Serializer booleanSerializer = WrappedDataWatcher.Registry.get(Boolean.class);

                // Establecer la visibilidad del nametag como falsa
                dataValues.add(new WrappedDataValue(3, booleanSerializer, false));

                packet.getDataValueCollectionModifier().write(0, dataValues);
                protocolManager.sendServerPacket(viewer, packet);
            }
        }

        // Eliminar nametags para entidades que ya no están en rango
        Set<Integer> entitiesToRemove = new HashSet<>(currentNametaggedEntities);
        entitiesToRemove.removeAll(updatedEntities);

        for (Integer entityId : entitiesToRemove) {
            // Aquí podrías enviar un paquete para eliminar/ocultar el nametag
            // Por ejemplo, usando un paquete de metadatos para establecer el nametag como invisible
            PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
            packet.getIntegers().write(0, entityId);

            List<WrappedDataValue> dataValues = new ArrayList<>();
            WrappedDataWatcher.Serializer booleanSerializer = WrappedDataWatcher.Registry.get(Boolean.class);

            // Establecer la visibilidad del nametag como falsa
            dataValues.add(new WrappedDataValue(3, booleanSerializer, false));

            packet.getDataValueCollectionModifier().write(0, dataValues);
            protocolManager.sendServerPacket(viewer, packet);
        }

        // Actualizar el conjunto de entidades etiquetadas para este jugador
        currentNametaggedEntities.clear();
        currentNametaggedEntities.addAll(updatedEntities);
    }

    public void onDisable() {
        if (globalNametagTask != null) {
            globalNametagTask.cancel();
        }
        // Limpiar el mapa de entidades etiquetadas
        playerNametaggedEntities.clear();
    }

}
