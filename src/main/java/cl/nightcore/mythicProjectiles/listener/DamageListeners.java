package cl.nightcore.mythicProjectiles.listener;

import cl.nightcore.mythicProjectiles.boss.BossUtil;
import cl.nightcore.mythicProjectiles.boss.WorldBoss;
import cl.nightcore.mythicProjectiles.util.MobUtil;
import io.lumine.mythic.bukkit.events.MythicDamageEvent;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DamageListeners implements Listener {



    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onMythicDamage(MythicDamageEvent event) {
        double baseDamage = event.getDamage();
        double finalDamage = baseDamage;

        // Aplicar multiplicador normal de nivel
        int mobLevel = MobUtil.getLevel(event.getCaster().getEntity().getBukkitEntity());
        double multiplier = Math.pow(1.0425, mobLevel - 1);
        finalDamage *= multiplier;

        if (event.getCaster().getEntity().getBukkitEntity() instanceof LivingEntity livingEntity){
            if (WorldBoss.isBoss(livingEntity)) {
                double bossMultiplier = BossUtil.calculateBossDamageMultiplier(livingEntity, mobLevel);
                finalDamage = baseDamage * bossMultiplier;
            }
        }
        event.setDamage(finalDamage);
    }


        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {

        if (event.getDamager() instanceof Creeper creeper) {
            // Obtener el daño base
            double baseDamage = event.getDamage();
            double finalDamage = baseDamage;

            // Aplicar multiplicador normal de nivel
            int mobLevel = MobUtil.getLevel(creeper);
            double multiplier = Math.pow(1.0425, mobLevel - 1);
            finalDamage *= multiplier;

            // Si es jefe, aplicar multiplicador adicional
            if (WorldBoss.isBoss(creeper)) {
                double bossMultiplier = BossUtil.calculateBossDamageMultiplier(creeper, mobLevel);
                finalDamage = baseDamage * bossMultiplier;
            }

            event.setDamage(finalDamage);
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

        int mobLevel = MobUtil.getLevel(shooter);
        double baseDamage = event.getDamage();
        double finalDamage = baseDamage;

        /*System.out.println("SHOOTER: " + shooter.getName());
        System.out.println("DAÑO ORIGINAL: " + event.getDamage());*/

        // Si es jefe, usar el multiplicador de jefe
        if (WorldBoss.isBoss(shooter)) {
            double bossMultiplier = BossUtil.calculateBossDamageMultiplier(shooter, mobLevel);
            finalDamage = baseDamage * bossMultiplier;
        } else {
            // Aplicar multiplicador normal de nivel
            double multiplier = Math.pow(1.0425, mobLevel - 1);
            finalDamage *= multiplier;
        }

        /*System.out.println("DAÑO FINAL: " + finalDamage);*/
        event.setDamage(finalDamage);
    }
}