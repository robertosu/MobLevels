package cl.nightcore.mythicProjectiles.boss;

import org.bukkit.entity.EntityType;

@SuppressWarnings("unused")
public enum HostileMob {
    ZOMBIE(EntityType.ZOMBIE),
    SKELETON(EntityType.SKELETON),
    CREEPER(EntityType.CREEPER),
    SPIDER(EntityType.SPIDER),
    ENDERMAN(EntityType.ENDERMAN),
    BLAZE(EntityType.BLAZE),
    CAVE_SPIDER(EntityType.CAVE_SPIDER),
    DROWNED(EntityType.DROWNED),
    ELDER_GUARDIAN(EntityType.ELDER_GUARDIAN),
    EVOKER(EntityType.EVOKER),
    VINDICATOR(EntityType.VINDICATOR),
    PILLAGER(EntityType.PILLAGER),
    WITCH(EntityType.WITCH),
    WITHER(EntityType.WITHER),
    WITHER_SKELETON(EntityType.WITHER_SKELETON),
    GHAST(EntityType.GHAST),
    MAGMA_CUBE(EntityType.MAGMA_CUBE),
    SLIME(EntityType.SLIME),
    HUSK(EntityType.HUSK),
    STRAY(EntityType.STRAY),
    PHANTOM(EntityType.PHANTOM),
    ZOMBIE_VILLAGER(EntityType.ZOMBIE_VILLAGER),
    ZOGLIN(EntityType.ZOGLIN),
    HOGLIN(EntityType.HOGLIN),
    WARDEN(EntityType.WARDEN),
    VEX(EntityType.VEX),
    ENDERMITE(EntityType.ENDERMITE),
    ILLUSIONER(EntityType.ILLUSIONER),
    RAVAGER(EntityType.RAVAGER),
    SKELETON_HORSE(EntityType.SKELETON_HORSE),
    ZOMBIE_HORSE(EntityType.ZOMBIE_HORSE),
    PIGLIN(EntityType.PIGLIN),
    PIGLIN_BRUTE(EntityType.PIGLIN_BRUTE),
    BOGGED(EntityType.BOGGED),
    BREEZE(EntityType.BREEZE),
    ZOMBIFIED_PIGLIN(EntityType.ZOMBIFIED_PIGLIN),
    SHULKER(EntityType.SHULKER),
    GUARDIAN(EntityType.GUARDIAN);

    private final EntityType entityType;

    // Constructor para asociar el EntityType con el enum
    HostileMob(EntityType entityType) {
        this.entityType = entityType;
    }

    /**
     * Verifica si un EntityType es considerado hostil
     */
    public static boolean isHostile(EntityType entityType) {
        for (HostileMob hostile : values()) {
            if (hostile.getEntityType() == entityType) {
                return true;
            }
        }
        return false;
    }

    /**
     * Obtiene el enum correspondiente a un EntityType
     */
    public static HostileMob fromEntityType(EntityType entityType) {
        for (HostileMob hostile : values()) {
            if (hostile.getEntityType() == entityType) {
                return hostile;
            }
        }
        return null;
    }

    public EntityType getEntityType() {
        return entityType;
    }

}
