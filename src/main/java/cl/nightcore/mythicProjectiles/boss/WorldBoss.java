package cl.nightcore.mythicProjectiles.boss;
import cl.nightcore.mythicProjectiles.MythicProjectiles;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataType;
public class WorldBoss {

    private static NamespacedKey bossKey;
    private static NamespacedKey difficultyKey;

    public static void initialize(MythicProjectiles plugin) {
        bossKey = new NamespacedKey(plugin, "is_boss");
        difficultyKey = new NamespacedKey(plugin, "boss_difficulty");
    }

    public static boolean isBoss(LivingEntity entity) {
        return entity.getPersistentDataContainer().getOrDefault(bossKey, PersistentDataType.BOOLEAN, false);
    }

    public static void setBoss(LivingEntity entity, boolean isBoss) {
        entity.getPersistentDataContainer().set(bossKey, PersistentDataType.BOOLEAN, isBoss);
    }

    public static BossDifficulty getBossDifficulty(LivingEntity entity) {
        String difficultyName = entity.getPersistentDataContainer().get(difficultyKey, PersistentDataType.STRING);
        if (difficultyName == null) {
            return null;
        }
        try {
            return BossDifficulty.valueOf(difficultyName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static void setBossDifficulty(LivingEntity entity, BossDifficulty difficulty) {
        entity.getPersistentDataContainer().set(difficultyKey, PersistentDataType.STRING, difficulty.name());
    }

    /**
     * Calcula la probabilidad de que un mob sea jefe basado en su nivel
     * Nivel 1: 0.05% (0.0005)
     * Nivel 120: 0.005% (0.00005)
     * Curva exponencial decreciente
     */
    public static double calculateBossChance(int level) {
        // Fórmula: chance = 0.0005 * e^(-0.0194 * (level - 1))
        // Esto da aproximadamente 0.05% en nivel 1 y 0.005% en nivel 120
        return 0.0005 * Math.exp(-0.0194 * (level - 1));
    }

    /**
     * Selecciona una dificultad aleatoria para el jefe
     * Las probabilidades son:
     * Fácil: 55%
     * Normal: 30%
     * Difícil: 10%
     * Extremo: 5%
     */
    public static BossDifficulty selectRandomDifficulty() {
        double random = Math.random();

        if (random < 0.55) {
            return BossDifficulty.EASY;
        } else if (random < 0.85) {
            return BossDifficulty.NORMAL;
        } else if (random < 0.95) {
            return BossDifficulty.HARD;
        } else {
            return BossDifficulty.EXTREME;
        }
    }
}