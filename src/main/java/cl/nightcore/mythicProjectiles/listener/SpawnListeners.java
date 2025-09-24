package cl.nightcore.mythicProjectiles.listener;

import cl.nightcore.mythicProjectiles.MythicProjectiles;
import cl.nightcore.mythicProjectiles.boss.BossDifficulty;
import cl.nightcore.mythicProjectiles.config.ConfigManager;
import cl.nightcore.mythicProjectiles.util.MobUtil;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.compatibility.AbstractModelEngineSupport;
import io.lumine.mythic.bukkit.compatibility.ModelEngineSupport;
import io.lumine.mythic.bukkit.events.MythicMobSpawnEvent;
import io.lumine.mythic.core.skills.projectiles.ProjectileBullet;
import io.lumine.mythic.core.skills.projectiles.bullet.MEGBullet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTameEvent;

import static cl.nightcore.mythicProjectiles.listener.BossListener.announceBossSpawnMythic;


public class SpawnListeners implements Listener {


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        MobUtil.setMobLevel(event.getEntity(),true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMythicCreatureSpawn(MythicMobSpawnEvent event) {
        System.out.println("MythicmobspawnEvent called");
        MobUtil.setMythicMobLevel(event.getMob(),true);

    }

    @EventHandler(ignoreCancelled = true)
    private void onWolfTame(EntityTameEvent e) {
        var entity = e.getEntity();
        var owner = (Player) e.getOwner();
        int level = MobUtil.getLevel(entity);

        if (entity instanceof Wolf wolf) {
            if (MobUtil.getLevel(wolf) > MythicProjectiles.getPlayerLevel(owner)) {
                owner.sendMessage(Component.text("No puedes domesticar un animal de nivel superior al tuyo").color(NamedTextColor.RED));
                e.setCancelled(true);
            }
        }

        System.out.println("Setted new health for wolf:" + entity.getName() + entity.getAttribute(Attribute.MAX_HEALTH));
        Bukkit.getScheduler().runTaskLater(MythicProjectiles.getInstance(), () -> {
            MobUtil.setNewHealth(entity, level);
        }, 2L);

        System.out.println("New health for wolf:" + entity.getName() + entity.getAttribute(Attribute.MAX_HEALTH));
    }



}