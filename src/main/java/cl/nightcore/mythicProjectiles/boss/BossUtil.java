package cl.nightcore.mythicProjectiles.boss;

import cl.nightcore.mythicProjectiles.MythicProjectiles;
import cl.nightcore.mythicProjectiles.config.ConfigManager;
import cl.nightcore.mythicProjectiles.util.MobUtil;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.EnchantingInventory;

import static cl.nightcore.mythicProjectiles.config.ConfigManager.spigotMode;
import static cl.nightcore.mythicProjectiles.util.MobUtil.getLevel;

public class BossUtil {

    private static final NamespacedKey bloodNightIsSpecialMob = new NamespacedKey("bloodnight","isspecialmob");


    /**
     * Verifica si una entidad puede ser un jefe
     */
    public static boolean canBeBoss(Entity entity) {


        if (spigotMode){
            return false;
        }


        if (entity.getEntitySpawnReason().equals(CreatureSpawnEvent.SpawnReason.SPAWNER)){
            return false;
        }


        if (entity.getPersistentDataContainer().has(bloodNightIsSpecialMob)) {
            return false;
        }

        // Verificar si el tipo de entidad está en el enum HostileMob
        return HostileMob.isHostile(entity.getType());
    }

    public static void applyBossModificationsMythic(ActiveMob activeMob, int level, BossDifficulty difficulty) {
        var entity = activeMob.getEntity().getBukkitEntity();

        // Marcar como jefe
        WorldBoss.setBoss(entity, true);
        WorldBoss.setBossDifficulty(entity, difficulty);

        applyBossHealthMythic(activeMob, level, difficulty);

        // No establecemos nombre personalizado aquí - eso lo maneja NametagManager
    }


    /**
     * Aplica las modificaciones de jefe a una entidad
     */


    public static void applyBossModifications(LivingEntity entity, int level, BossDifficulty difficulty) {

        MobUtil.setLeveledKey(entity, level);
        // Marcar como jefe
        WorldBoss.setBoss(entity, true);
        WorldBoss.setBossDifficulty(entity, difficulty);

        // Aplicar modificaciones de HP
        applyBossHealth(entity, level, difficulty);
        // Aplicar modificaciones de daño
        applyBossDamage(entity, level, difficulty);

        // No establecemos nombre personalizado aquí - eso lo maneja NametagManager
    }

    /**
     * Aplica la HP de jefe: HP normal * nivel * (1 + 0.15 * tier de dificultad)
     */
    private static void applyBossHealth(LivingEntity entity, int level, BossDifficulty difficulty) {
        var healthAttribute = entity.getAttribute(Attribute.MAX_HEALTH);
        if (healthAttribute != null) {
            // Obtener HP base del tipo de entidad
            double baseHealth = entity.getType().getDefaultAttributes().getAttribute(Attribute.MAX_HEALTH).getValue();

            // Aplicar multiplicador de nivel normal
            double levelMultiplier = Math.pow(1.0425, level - 1);
            double leveledHealth = baseHealth * levelMultiplier;

            // Aplicar multiplicador de jefe: nivel * (1 + 15% por tier)
            double bossHealthMultiplier = level * difficulty.getDamageMultiplier();
            double finalHealth = leveledHealth * bossHealthMultiplier;

            healthAttribute.setBaseValue(finalHealth);
            entity.setHealth(finalHealth);


        }
    }

    private static void applyBossHealthMythic(ActiveMob entity, int level, BossDifficulty difficulty) {
        Bukkit.getScheduler().runTaskLater(MythicProjectiles.getInstance(),()->{
            double baseHealth = entity.getType().getHealth().get();
            double levelMultiplier = Math.pow(1.0425, level - 1);
            double leveledHealth = baseHealth * levelMultiplier;

            // Aplicar multiplicador de jefe: nivel * (1 + 15% por tier)
            double bossHealthMultiplier = level * difficulty.getDamageMultiplier();
            double finalHealth = leveledHealth * bossHealthMultiplier;
            entity.getEntity().setHealthAndMax(finalHealth);
        },5L);
    }


    /**
     * Aplica el daño de jefe: daño_normal * (1 + nivel * 0.05) * multiplicador_dificultad
     */
    private static void applyBossDamage(LivingEntity entity, int level, BossDifficulty difficulty) {
        var damageAttribute = entity.getAttribute(Attribute.ATTACK_DAMAGE);
        if (damageAttribute != null) {
            // Obtener daño base del tipo de entidad
            double baseDamage = entity.getType().getDefaultAttributes().getAttribute(Attribute.ATTACK_DAMAGE).getValue();

            // Aplicar multiplicador de nivel normal primero
            double levelMultiplier = Math.pow(1.0425, level - 1);
            double normalDamage = baseDamage * levelMultiplier;

            // Aplicar multiplicador de jefe: daño_normal * (1 + nivel * 0.05) * multiplicador_dificultad
            double bossLevelMultiplier = 1.0 + (level * 0.05) * difficulty.getDamageMultiplier();
            double finalDamage = normalDamage * bossLevelMultiplier;

            damageAttribute.setBaseValue(finalDamage);
        }
    }

    /**
     * Calcula el multiplicador de daño total para un jefe
     * (usado en DamageListeners para daños que no usan ATTACK_DAMAGE)
     * DEBE SER IGUAL AL CÁLCULO DE applyBossDamage
     */
    public static double calculateBossDamageMultiplier(LivingEntity entity, int level) {
        BossDifficulty difficulty = WorldBoss.getBossDifficulty(entity);

        if (difficulty == null) {
            return 1.0;
        }

        // MISMA LÓGICA QUE applyBossDamage:
        // 1. Primero multiplicador de nivel normal
        double levelMultiplier = Math.pow(1.0425, level - 1);

        // 2. Luego multiplicador de jefe: (1 + nivel * 0.05)
        double bossLevelMultiplier = 1.0 + (level * 0.05);

        // 3. Finalmente multiplicador de dificultad
        double difficultyMultiplier = difficulty.getDamageMultiplier();

        // El resultado final debe ser: multiplicador_nivel * multiplicador_jefe * multiplicador_dificultad
        return levelMultiplier * bossLevelMultiplier * difficultyMultiplier;
    }

}