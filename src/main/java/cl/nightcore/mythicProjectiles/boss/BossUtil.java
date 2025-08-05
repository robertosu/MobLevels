package cl.nightcore.mythicProjectiles.boss;

import cl.nightcore.mythicProjectiles.MythicProjectiles;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.Arrays;

public class BossUtil {

    /**
     * Verifica si una entidad puede ser un jefe
     */
    public static boolean canBeBoss(LivingEntity entity) {
        // Verificar si el tipo de entidad está en el enum HostileMob
        EntityType entityType = entity.getType();
        return Arrays.stream(HostileMob.values())
                .anyMatch(hostileMob -> hostileMob.getEntityType() == entityType);
    }

    /**
     * Aplica las modificaciones de jefe a una entidad
     */
    public static void applyBossModifications(LivingEntity entity, int level, BossDifficulty difficulty) {
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
            double bossHealthMultiplier = level * (1.0 + (0.15 * difficulty.getTier()));
            double finalHealth = leveledHealth * bossHealthMultiplier;

            healthAttribute.setBaseValue(finalHealth);
            entity.setHealth(finalHealth);
        }
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
            double bossLevelMultiplier = 1.0 + (level * 0.05);
            double finalDamage = normalDamage * bossLevelMultiplier * difficulty.getDamageMultiplier();

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