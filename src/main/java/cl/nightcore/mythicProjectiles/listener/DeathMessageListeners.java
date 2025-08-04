package cl.nightcore.mythicProjectiles.listener;

import cl.nightcore.mythicProjectiles.config.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static cl.nightcore.mythicProjectiles.MythicProjectiles.getLevel;

public class DeathMessageListeners implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {

        LivingEntity killer;
        if (!(event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent damageByEntityEvent)) {
            return;
        } else {
            killer = getPlayersKiller(damageByEntityEvent);
        }

        if (killer != null) {
            Component deathMessage = event.deathMessage();
            if (deathMessage != null) {
                // Modificar el mensaje de muerte
                Component modifiedDeathMessage = modifyDeathMessage(deathMessage, killer);
                event.deathMessage(modifiedDeathMessage);
            }
        }
    }

    public Component modifyDeathMessage(Component deathMessage, LivingEntity entity) {
        if (!(deathMessage instanceof TranslatableComponent translatableComponent)) {
            return deathMessage;
        }

        List<TranslationArgument> arguments = translatableComponent.arguments();
        if (arguments.size() < 2) {
            return deathMessage;
        }

        // El segundo argumento es el mob
        TranslationArgument mobArgument = arguments.get(1);
        Component mobComponent = (Component) mobArgument.value();
        Component mobName = null;

        if(entity.customName() != null){
            mobName = entity.customName();
        } else if (mobComponent instanceof TranslatableComponent mobTranslatable) {
            mobName = mobTranslatable;
        }else if (entity instanceof Player player){
            mobName = player.name();
        }

        // Combinar el nombre del mob con el nivel
        Component newMobComponent = Objects.requireNonNull(mobName).append(Component.text(" ")).append(ConfigManager.formatLevel2(String.valueOf(getLevel(entity))));

        // Crear una nueva lista de argumentos reemplazando el componente del mob
        List<TranslationArgument> newArguments = new ArrayList<>(arguments);
        newArguments.set(1, TranslationArgument.component(newMobComponent));

        // Crear el nuevo mensaje de muerte con los argumentos actualizados
        return Component.translatable(translatableComponent.key(), newArguments);
    }


    public LivingEntity getPlayersKiller(EntityDamageByEntityEvent event) {
        // Obtener la causa del último daño recibido por el jugador

        // Obtener el dañador (entity que causó el daño)
        LivingEntity killer = null;
        Entity damager = event.getDamager();


        //Si el dañador es enderdragón
        if (event.getDamager().getType() == EntityType.AREA_EFFECT_CLOUD) {
            AreaEffectCloud aec = (AreaEffectCloud) event.getDamager();
            if (aec.getSource() instanceof EnderDragon dragon) {
                killer = dragon;
            }
        }

        // Si el dañador es un proyectil, obtener el tirador (shooter)
        if (damager instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof LivingEntity shooter) {
                killer = shooter;
            }
        }
        
        // Si el dañador es una LivingEntity, asignarlo directamente
        else if (damager instanceof LivingEntity livingEntity) {
            killer = livingEntity;
        }


        // Verificar si el killer es un jugador
        /*if (killer instanceof Player player) {
            killer = player; // Si el killer es un jugador, retornar null (opcional, dependiendo de tu caso de uso)
        }*/

        return killer; // Retornar la entidad que mató al jugador
    }

}