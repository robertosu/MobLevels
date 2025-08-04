package cl.nightcore.mythicProjectiles.listener;

import cl.nightcore.mythicProjectiles.MythicProjectiles;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import static cl.nightcore.mythicProjectiles.MythicProjectiles.levelKey;

public class SpawnListeners implements Listener {

    private static void setNewHealth(LivingEntity entity, int level) {
        double maxHealth = Objects.requireNonNull(entity.getType().getDefaultAttributes().getAttribute(Attribute.MAX_HEALTH)).getValue();
        double multiplier = Math.pow(1.0425, level - 1);
        double newHealth = maxHealth * multiplier;
        Objects.requireNonNull(entity.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(newHealth);
        entity.setHealth(newHealth);
    }

    private static void setNewDamage(LivingEntity entity, int level) {
        // System.out.println(entity.getType().name());
        // Check if the entity type has an ATTACK_DAMAGE attribute
        if (entity.getType().getDefaultAttributes().getAttribute(Attribute.ATTACK_DAMAGE) != null) {

            double damage = Objects.requireNonNull(entity.getType().getDefaultAttributes().getAttribute(Attribute.ATTACK_DAMAGE)).getValue();
            double multiplier = Math.pow(1.0425, level - 1);
            double newDamage = damage * multiplier;
            Objects.requireNonNull(entity.getAttribute(Attribute.ATTACK_DAMAGE)).setBaseValue(newDamage);
        }
    }

    private static void setScale(Spider entity) {
        // Define los límites mínimo y máximo
        double minScale = 0.4; // Escala mínima
        double maxScale = 2.5; // Escala máxima

        // Genera un número aleatorio entre minScale y maxScale
        double scale = ThreadLocalRandom.current().nextDouble(minScale, maxScale);

        var attribute = entity.getAttribute(Attribute.SCALE);
        attribute.setBaseValue(scale);
    }

    private static void setLeveledKey(LivingEntity entity, int level) {
        entity.getPersistentDataContainer().set(
                levelKey,
                PersistentDataType.INTEGER,
                level
        );
    }

    private static void applyModifiers(LivingEntity entity) {
        int level = determineLevel(entity);
        // Asignar el nivel a la entidad
        setLeveledKey(entity, level);
        // Asignar la nueva HP
        setNewHealth(entity, level);
        setNewDamage(entity, level);
        if (entity instanceof Spider spider){
            setScale(spider);
        }
    }

    private static int determineLevel(LivingEntity entity) {
        int bufferZone = 500;
        int levelIncreaseDistance = 250;
        Location spawnLoc = entity.getLocation();
        // Calcular distancia desde el spawn (0,0)
        double distance = Math.sqrt(
                Math.pow(spawnLoc.getX(), 2) +
                        Math.pow(spawnLoc.getZ(), 2)
        );
        // Calcular nivel basado en la distancia
        int level;
        // Radio de la zona inicial (nivel 1)
        if (distance <= bufferZone) {
            level = 1;
        } else {
            // Distancia para aumentar de nivel
            level = 1 + (int) ((distance - bufferZone) / levelIncreaseDistance);
        }
        return level;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onChunkPopulate(ChunkPopulateEvent event) {
        for (@NotNull Entity entity : event.getChunk().getEntities()) {
            if (entity instanceof LivingEntity livingEntity) {
                applyModifiers(livingEntity);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        applyModifiers(event.getEntity());
    }


    @EventHandler(ignoreCancelled = true)
    private void onWolfTame(EntityTameEvent e) {
        var entity = e.getEntity();
        var owner = (Player) e.getOwner();
        int level = MythicProjectiles.getLevel(entity);

        if (entity instanceof Wolf wolf) {
            if (MythicProjectiles.getLevel(wolf) > MythicProjectiles.getPlayerLevel(owner)) {
                owner.sendMessage(Component.text("No puedes domesticar un animal de nivel superior al tuyo").color(NamedTextColor.RED));
                e.setCancelled(true);

            }
        }
        System.out.println("Setted new helt for wolf:" + entity.getName() + entity.getAttribute(Attribute.MAX_HEALTH));
        Bukkit.getScheduler().runTaskLater(MythicProjectiles.getInstance(), () -> {
            setNewHealth(entity,level);
        }, 2L);


        System.out.println("New health for wolf:" + entity.getName() + entity.getAttribute(Attribute.MAX_HEALTH));

    }

}
