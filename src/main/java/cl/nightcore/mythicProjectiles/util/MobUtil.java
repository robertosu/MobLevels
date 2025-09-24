package cl.nightcore.mythicProjectiles.util;

import cl.nightcore.mythicProjectiles.MythicProjectiles;
import cl.nightcore.mythicProjectiles.boss.BossDifficulty;
import cl.nightcore.mythicProjectiles.boss.BossUtil;
import cl.nightcore.mythicProjectiles.boss.WorldBoss;
import cl.nightcore.mythicProjectiles.config.ConfigManager;
import io.lumine.mythic.core.mobs.ActiveMob;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Spider;
import org.bukkit.persistence.PersistentDataType;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import static cl.nightcore.mythicProjectiles.listener.BossListener.announceBossSpawn;
import static cl.nightcore.mythicProjectiles.listener.BossListener.announceBossSpawnMythic;

public class MobUtil {

    static boolean debug = false;
    private static final int bufferZone = 500;
    private static final int levelIncreaseDistance = 250;
    // Sistema de pesos para la varianza de niveles
    private static final class LevelVariance {
        // Pesos para cada variación de nivel (de -3 a +3)
        // Índice 0 = -3, Índice 1 = -2, Índice 2 = -1, Índice 3 = 0 (base),
        // Índice 4 = +1, Índice 5 = +2, Índice 6 = +3
        private static final int[] LEVEL_WEIGHTS = {
                5,    // -3 niveles: 5% probabilidad
                10,   // -2 niveles: 10% probabilidad
                15,   // -1 nivel:   15% probabilidad
                40,   // 0 (base):   40% probabilidad
                15,   // +1 nivel:   15% probabilidad
                10,   // +2 niveles: 10% probabilidad
                5     // +3 niveles: 5% probabilidad
        };

        private static final int TOTAL_WEIGHT = 100; // Suma de todos los pesos

        /**
         * Aplica varianza al nivel base usando el sistema de pesos
         * @param baseLevel El nivel base calculado por distancia
         * @return El nivel final con varianza aplicada
         */
        public static int applyVariance(int baseLevel){
            int random = ThreadLocalRandom.current().nextInt(TOTAL_WEIGHT);
            int cumulativeWeight = 0;

            for (int i = 0; i < LEVEL_WEIGHTS.length; i++) {
                cumulativeWeight += LEVEL_WEIGHTS[i];
                if (random < cumulativeWeight) {
                    int variance = i - 3; // Convertir índice a varianza (-3 a +3)
                    int finalLevel = baseLevel + variance;

                    // Asegurar que el nivel nunca sea menor a 1
                    return Math.max(1, finalLevel);
                }
            }
           throw new IllegalStateException("level weights for variance empty");
        }

        /**
         * Versión alternativa con configuración personalizable
         */
        public static int applyCustomVariance(int baseLevel, VarianceConfig config) {
            int random = ThreadLocalRandom.current().nextInt(config.getTotalWeight());
            int cumulativeWeight = 0;

            int[] weights = config.getWeights();
            for (int i = 0; i < weights.length; i++) {
                cumulativeWeight += weights[i];
                if (random < cumulativeWeight) {
                    int variance = i - config.getMaxVariance();
                    int finalLevel = baseLevel + variance;
                    return Math.max(1, finalLevel);
                }
            }

            throw new IllegalStateException("level weights for variance empty");
        }
    }

    /**
     * Clase de configuración para varianza personalizable
     */
    public static class VarianceConfig {
        private final int maxVariance;
        private final int[] weights;
        private final int totalWeight;

        public VarianceConfig(int maxVariance, int[] weights) {
            this.maxVariance = maxVariance;
            this.weights = weights;
            this.totalWeight = java.util.Arrays.stream(weights).sum();
        }

        // Configuración por defecto (±3 niveles)
        public static VarianceConfig defaultConfig() {
            return new VarianceConfig(3, new int[]{5, 10, 20, 30, 20, 10, 5});
        }

        // Configuración conservadora (±2 niveles, más peso al centro)
        public static VarianceConfig conservativeConfig() {
            return new VarianceConfig(2, new int[]{10, 25, 30, 25, 10});
        }

        // Configuración agresiva (±3 niveles, más dispersión)
        public static VarianceConfig aggressiveConfig() {
            return new VarianceConfig(3, new int[]{15, 15, 15, 10, 15, 15, 15});
        }

        public int getMaxVariance() { return maxVariance; }
        public int[] getWeights() { return weights; }
        public int getTotalWeight() { return totalWeight; }
    }

    public static void setNewHealth(LivingEntity entity, int level) {
        double maxHealth = Objects.requireNonNull(entity.getType().getDefaultAttributes().getAttribute(Attribute.MAX_HEALTH)).getValue();
        double multiplier = Math.pow(1.0425, level - 1);
        double newHealth = maxHealth * multiplier;
        Objects.requireNonNull(entity.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(newHealth);
        entity.setHealth(newHealth);
    }

    public static void setNewHealthMythic(ActiveMob entity, int level){


        Bukkit.getScheduler().runTaskLater(MythicProjectiles.getInstance(),()->{
            double maxHealth = entity.getType().getHealth().get();
            double multiplier = Math.pow(1.0425, level - 1);
            double newHealth = maxHealth * multiplier;
            entity.getEntity().setHealthAndMax(newHealth);
        },5L);


    }

    public static void setNewHealthSpigot(LivingEntity entity, int level) {
        double maxHealth = entity.getMaxHealth();
        double multiplier = Math.pow(1.0425, level - 1);
        double newHealth = maxHealth * multiplier;
        entity.setMaxHealth(newHealth);
        entity.setHealth(newHealth);
    }


    private static void setNewDamage(LivingEntity entity, int level) {
        if (entity.getType().getDefaultAttributes().getAttribute(Attribute.ATTACK_DAMAGE) != null) {
            double damage = Objects.requireNonNull(entity.getType().getDefaultAttributes().getAttribute(Attribute.ATTACK_DAMAGE)).getValue();
            double multiplier = Math.pow(1.0425, level - 1);
            double newDamage = damage * multiplier;
            Objects.requireNonNull(entity.getAttribute(Attribute.ATTACK_DAMAGE)).setBaseValue(newDamage);
        }
    }

    private static void setNewDamageSpigot(LivingEntity entity, int level) {
        if (entity.getAttribute(Attribute.ATTACK_DAMAGE) != null) {
            double damage = Objects.requireNonNull(entity.getAttribute(Attribute.ATTACK_DAMAGE)).getValue();
            double multiplier = Math.pow(1.0425, level - 1);
            double newDamage = damage * multiplier;
            Objects.requireNonNull(entity.getAttribute(Attribute.ATTACK_DAMAGE)).setBaseValue(newDamage);
        }
    }

    private static void setScale(Spider entity) {
        double minScale = 0.4;
        double maxScale = 2.5;
        double scale = ThreadLocalRandom.current().nextDouble(minScale, maxScale);
        var attribute = entity.getAttribute(Attribute.SCALE);
        attribute.setBaseValue(scale);
    }

    public static void setMobLevel(LivingEntity entity, boolean canspawnboss) {
        // Aplicar varianza al nivel base
        int finalLevel = LevelVariance.applyVariance(determineLevel(entity));

        setLeveledKey(entity, finalLevel);

        // Verificar si la entidad puede ser un jefe (usar el nivel final para el cálculo)
        if (BossUtil.canBeBoss(entity) && canspawnboss) {
            double bossChance = WorldBoss.calculateBossChance(finalLevel);

            if (Math.random() < bossChance) {
                BossDifficulty difficulty = WorldBoss.selectRandomDifficulty();
                BossUtil.applyBossModifications(entity, finalLevel, difficulty);
                announceBossSpawn(entity, difficulty,difficulty.getTier());
                return;
            }
        }
        // Aplicar modificadores normales si no es jefe (usar el nivel final)
        if (!ConfigManager.spigotMode) {
            setNewDamage(entity, finalLevel);
            setNewHealth(entity, finalLevel);

        }else{
            setNewHealthSpigot(entity, finalLevel);
            setNewDamageSpigot(entity, finalLevel);
        }
        if (!ConfigManager.spigotMode) {
            if (entity instanceof Spider spider) {
                setScale(spider);
            }
        }
    }

    public static void setMythicMobLevel(ActiveMob activeMob, boolean canspawnboss) {
        var entity = activeMob.getEntity().getBukkitEntity();
        // Calcular nivel base por distancia
        int baseLevel = determineLevel(entity);
        // Aplicar varianza al nivel base
        int finalLevel = LevelVariance.applyVariance(baseLevel);

        setLeveledKey(entity, finalLevel);
        // Verificar si la entidad puede ser un jefe (usar el nivel final para el cálculo)
        if (canspawnboss) {
            double bossChance = WorldBoss.calculateBossChance(finalLevel);
            double roll = ThreadLocalRandom.current().nextDouble();
            System.out.println("bosschance: " + bossChance + "roll: " + roll);

            boolean isBoss = roll < bossChance;

            System.out.println(isBoss);

            if (isBoss) {
                BossDifficulty difficulty = WorldBoss.selectRandomDifficulty();
                BossUtil.applyBossModificationsMythic(activeMob, finalLevel, difficulty);
                var prevName = activeMob.getDisplayName();
                var levelComp = ConfigManager.formatLevel2(String.valueOf(finalLevel));
                var serializedDisplayName = MiniMessage.miniMessage().deserialize(prevName);
                var diffComponent = Component.text(difficulty.getDisplayName(),difficulty.getColor());
                var finalComponent = levelComp.append(serializedDisplayName).append(Component.space().append(diffComponent));
                MythicProjectiles.getInstance().getServer().broadcast(Component.text("Componente final:"));
                MythicProjectiles.getInstance().getServer().broadcast(finalComponent);
                activeMob.setDisplayName(MiniMessage.miniMessage().serialize(finalComponent));
                announceBossSpawnMythic(activeMob, difficulty,difficulty.getTier());
            }else {
                setNewHealthMythic(activeMob, finalLevel);
                //set name with level prefix
                var prevName = activeMob.getDisplayName();
                var levelComp = ConfigManager.formatLevel2(String.valueOf(finalLevel));
                var serializedDisplayName = MiniMessage.miniMessage().deserialize(prevName);
                var finalComponent = levelComp.append(serializedDisplayName);
                activeMob.setDisplayName(MiniMessage.miniMessage().serialize(finalComponent));
            }
        }



        if (!ConfigManager.spigotMode) {
            if (entity instanceof Spider spider) {
                setScale(spider);
            }
        }
    }

    public static int determineLevel(Entity entity) {
        Location spawnLoc = entity.getLocation();

        double distance = Math.sqrt(
                Math.pow(spawnLoc.getX(), 2) +
                        Math.pow(spawnLoc.getZ(), 2)
        );

        int level;
        if (distance <= bufferZone) {
            level = 1;
        } else {
            level = 1 + (int) ((distance - bufferZone) / levelIncreaseDistance);
        }
        return level;
    }

    public static int getLevel(Entity entity) {
        return entity.getPersistentDataContainer().getOrDefault(MythicProjectiles.levelKey, PersistentDataType.INTEGER, 0);
    }

    public static boolean isMobLeveled(Entity entity) {
        return entity.getPersistentDataContainer().has(MythicProjectiles.levelKey);
    }

    public static void setLeveledKey(Entity entity, int level) {
        entity.getPersistentDataContainer().set(
                MythicProjectiles.levelKey,
                PersistentDataType.INTEGER,
                level
        );
    }
}