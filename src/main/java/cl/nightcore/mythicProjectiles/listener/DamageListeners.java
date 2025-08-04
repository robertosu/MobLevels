package cl.nightcore.mythicProjectiles.listener;

import cl.nightcore.mythicProjectiles.MythicProjectiles;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DamageListeners implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {

        if (event.getDamager() instanceof Creeper creeper) {
            // Obtener el nivel del mob
            int mobLevel = MythicProjectiles.getLevel(creeper);
            // Obtener el daño base
            double baseDamage = event.getDamage();
            // Aplicar la fórmula: damage * (1.05 ^ (mobLevel - 1))
            double multiplier = Math.pow(1.0425, mobLevel - 1);
            double newDamage = baseDamage * multiplier;
            // Establecer el nuevo daño
            event.setDamage(newDamage);
            return;
        }
        // Handle dragon breath
        if (event.getDamager().getType() == EntityType.AREA_EFFECT_CLOUD) {
            AreaEffectCloud aec = (AreaEffectCloud) event.getDamager();
            if (!(aec.getSource() instanceof EnderDragon)) {
                return;
            }
            processRangedDamage((LivingEntity) aec.getSource(), event);
            return;
        }
        // Handle projectiles
        if (event.getDamager() instanceof Projectile projectile) {
            if (!(projectile.getShooter() instanceof LivingEntity) || projectile.getShooter() instanceof Player) {
                return;
            }
            processRangedDamage((LivingEntity) projectile.getShooter(), event);
        }

    }

    private void processRangedDamage(LivingEntity shooter, EntityDamageByEntityEvent event) {
        if (!shooter.isValid()) {
            return;
        }
        int mobLevel = MythicProjectiles.getLevel(shooter);
        double baseDamage = event.getDamage();
        System.out.println("SHOOTER: " + shooter.getName());
        System.out.println("DAÑO ORIGINAL; " + event.getDamage());
        // Apply formula: baseDamage * (1.05 ^ (mobLevel - 1))
        double multiplier = Math.pow(1.0425, mobLevel - 1);
        double newDamage = baseDamage * multiplier;
        System.out.println("DAÑO FINAL; " + newDamage);
        event.setDamage(newDamage);
    }
}
