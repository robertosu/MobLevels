package cl.nightcore.mythicProjectiles.listener;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CombustListener implements Listener {

    private final Map<LivingEntity, Double> sunBurningEntities = new HashMap<>();
    private final double percentage = 5.0;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCombust(EntityCombustEvent event) {
        if (event instanceof EntityCombustByBlockEvent || event instanceof EntityCombustByEntityEvent) {
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        if (entity.getWorld().getEnvironment() == World.Environment.NETHER ||
            entity.getWorld().getEnvironment() == World.Environment.THE_END) {
            return;
        }

        List<EntityType> entityTypesCanBurnInSunlight2 = Arrays.asList(
                EntityType.SKELETON, EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER,
        EntityType.STRAY, EntityType.DROWNED, EntityType.PHANTOM, EntityType.BOGGED
        );

        if (!entityTypesCanBurnInSunlight2.contains(entity.getType())) {
            return;
        }

        EntityEquipment equipment = entity.getEquipment();
        if (equipment != null) {
            ItemStack helmet = equipment.getHelmet();
            if (helmet != null && helmet.getType() != Material.AIR) {
                return;
            }
        }

        // Pre-calculamos el daño
        AttributeInstance maxHealthAttribute = entity.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealthAttribute == null) {
            return;
        }

        double maxHealth = maxHealthAttribute.getValue();
        double damageAmount = maxHealth * (percentage / 100);

        sunBurningEntities.put(entity, damageAmount);

        cleanInvalidEntities();
    }

    private void cleanInvalidEntities() {
        sunBurningEntities.entrySet().removeIf(entry -> {
        LivingEntity entity = entry.getKey();
        return !entity.isValid() || entity.isDead();
    });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        if (event.getCause() != EntityDamageEvent.DamageCause.FIRE_TICK) {
            return;
        }

        Double damageAmount = sunBurningEntities.get(entity);
        if (damageAmount == null) {
            return;
        }

        event.setDamage(damageAmount);
    }
}