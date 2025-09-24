package cl.nightcore.mythicProjectiles.listener;

import cl.nightcore.mythicProjectiles.boss.WorldBoss;
import cl.nightcore.mythicProjectiles.config.ConfigManager;
import io.lumine.mythic.bukkit.MythicBukkit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static cl.nightcore.mythicProjectiles.util.MobUtil.getLevel;

public class DeathMessageListeners implements Listener {

    @EventHandler()
    public void onPlayerDeath(PlayerDeathEvent event) {
        Entity killer = null;

        killer = getPlayersKiller(event);

        Component deathMessage = event.deathMessage();

        if (MythicBukkit.inst().getMobManager().isMythicMob(killer)) {
            return;
        }

        if (killer != null){
            System.out.println("killer is NOT null");
            var modifiedComponent = modifyDeathMessageDirect(deathMessage,killer);
            event.deathMessage(modifiedComponent);
            return;
        }
        if (deathMessage != null) {
            // Siempre intentar modificar el mensaje, con o sin killer directo
            Component modifiedDeathMessage = modifyDeathMessage(deathMessage, event.getEntity());
            event.deathMessage(modifiedDeathMessage);

        }
    }


    public Component modifyDeathMessage(Component deathMessage, Entity entity) {

        if (!(deathMessage instanceof TranslatableComponent translatableComponent)) {
            System.out.println("deathmessage not instanceof translateablecomponent");
            return deathMessage;
        }

        List<TranslationArgument> arguments = translatableComponent.arguments();
        if (arguments.isEmpty()) {
            System.out.println("translateablecomponent translation arguments empty");
            return deathMessage;
        }

        List<TranslationArgument> newArguments = new ArrayList<>();
        boolean modified = false;

        for (TranslationArgument argument : arguments) {
            // Buscar cualquier entidad en los argumentos del mensaje
            LivingEntity entityInMessage = findEntityFromComponent(argument);

            if (entityInMessage != null) {

                if (MythicBukkit.inst().getMobManager().isMythicMob(entityInMessage)){
                    return deathMessage;
                }
                // Reemplazar con versión mejorada
                Component enhancedComponent = createEnhancedMobComponent(entityInMessage);
                newArguments.add(TranslationArgument.component(enhancedComponent));
                modified = true;
            } else {
                newArguments.add(argument);
            }
        }

        if (modified) {
            return Component.translatable(translatableComponent.key(), newArguments);
        }

        return deathMessage;
    }

    /**
     * Busca una entidad desde un componente del mensaje
     */
    private LivingEntity findEntityFromComponent(TranslationArgument argument) {
        if (!(argument.value() instanceof Component component)) {
            return null;
        }

        // Buscar por UUID en hover event
        if (component.hoverEvent() != null &&
                component.hoverEvent().action() == HoverEvent.Action.SHOW_ENTITY) {

            HoverEvent.ShowEntity showEntity = (HoverEvent.ShowEntity) component.hoverEvent().value();
            UUID entityUUID = showEntity.id();

            // Buscar la entidad por UUID
            return org.bukkit.Bukkit.getWorlds().stream()
                    .flatMap(world -> world.getEntities().stream())
                    .filter(entity -> entity instanceof LivingEntity)
                    .map(entity -> (LivingEntity) entity)
                    .filter(entity -> entity.getUniqueId().equals(entityUUID))
                    .findFirst()
                    .orElse(null);
        }

        return null;
    }

    /**
     * Crea el componente mejorado del mob con nivel y dificultad
     */
    private Component createEnhancedMobComponent(Entity entity) {
        Component mobName = getMobName(entity);

        Component mobLevel = ConfigManager.formatLevel2(String.valueOf(getLevel(entity)));

        Component bossDifficulty = getBossDifficultyComponent(entity);

        return mobLevel.append(mobName).append(Component.space()).append(bossDifficulty);
    }

    private Component getMobName(Entity entity) {
        if (entity.customName() != null) {
            return entity.customName();
        } else if (entity instanceof Player player) {
            return player.name();
        } else {
            return Component.translatable(entity.getType().translationKey());
        }
    }

    private Component getBossDifficultyComponent(Entity entity) {
        if (WorldBoss.isBoss(entity)) {
            var difficulty = WorldBoss.getBossDifficulty(entity);
            return Component.text(difficulty.getDisplayName(), difficulty.getColor());
        }
        return Component.empty();
    }

    public Component modifyDeathMessageDirect(Component deathMessage, Entity entity) {

        if (!(deathMessage instanceof TranslatableComponent translatableComponent)) {
           // System.out.println("first return (not instanceof translatablecomponent)");
            return deathMessage;
        }

        List<TranslationArgument> arguments = translatableComponent.arguments();
        if (arguments.size() < 2) {
         //   System.out.println("second return (not <2 arguments size)");
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
        //System.out.println("mobname"+ mobName);

        // Combinar el nombre del mob con el nivel
        Component newMobComponent = createEnhancedMobComponent(entity);

        // Crear una nueva lista de argumentos reemplazando el componente del mob
        List<TranslationArgument> newArguments = new ArrayList<>(arguments);
        newArguments.set(1, TranslationArgument.component(newMobComponent));

        // Crear el nuevo mensaje de muerte con los argumentos actualizados
        return Component.translatable(translatableComponent.key(), newArguments);

    }

    public Entity getPlayersKiller(PlayerDeathEvent event) {
        // Obtener la causa del último daño recibido por el jugador

        // Obtener el dañador (entity que causó el daño)
        Entity killer = null;
        killer = event.getDamageSource().getCausingEntity();


        /*//Si el dañador es enderdragón
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
*/

        // Verificar si el killer es un jugador
        /*if (killer instanceof Player player) {
            killer = player; // Si el killer es un jugador, retornar null (opcional, dependiendo de tu caso de uso)
        }*/

        return killer; // Retornar la entidad que mató al jugador
    }




}