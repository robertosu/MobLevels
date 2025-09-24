package cl.nightcore.mythicProjectiles.mythicmobs;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.entity.Entity;

import java.util.Optional;

public class MythicMobsHook {
    Entity bukkitEntity;


    Optional<ActiveMob> optActiveMob = MythicBukkit.inst().getMobManager().getActiveMob(bukkitEntity.getUniqueId());

    public ActiveMob getOptActiveMob() {
        if (optActiveMob.isPresent()){
            return optActiveMob.get();
        }
        return null;
    }

    public static void setMythicMobLevel(ActiveMob mythicMob){

    }
}
