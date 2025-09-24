package cl.nightcore.mythicProjectiles.command;

import cl.nightcore.mythicProjectiles.MythicProjectiles;
import cl.nightcore.mythicProjectiles.boss.BossDifficulty;
import cl.nightcore.mythicProjectiles.boss.BossUtil;
import cl.nightcore.mythicProjectiles.boss.HostileMob;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.*;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.executors.CommandExecutor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.Objects;

public class SummonCommand {

    private final MythicProjectiles plugin;

    public SummonCommand(MythicProjectiles plugin) {
        this.plugin = plugin;
        registerCommand();
    }

    private void registerCommand() {
        new CommandAPICommand("summonmob")
                .withPermission("moblevels.summon")
                .withSubcommand(
                        new CommandAPICommand("normal")
                                .withArguments(
                                        new StringArgument("entityType").replaceSuggestions(ArgumentSuggestions.strings(
                                                Arrays.stream(EntityType.values())
                                                        .filter(type -> type.isSpawnable() && type.isAlive())
                                                        .map(entityType -> entityType.name().toLowerCase())
                                                        .toArray(String[]::new)
                                        )),
                                        new IntegerArgument("level", 1, 120),
                                        new IntegerArgument("quantity")
                                )
                                .executesPlayer(this::summonNormalMob)
                )
                .withSubcommand(
                        new CommandAPICommand("boss")
                                .withArguments(
                                        new StringArgument("entityType").replaceSuggestions(ArgumentSuggestions.strings(
                                                Arrays.stream(HostileMob.values())
                                                        .map(hostileMob -> hostileMob.getEntityType().name().toLowerCase())
                                                        .toArray(String[]::new)
                                        )),
                                        new IntegerArgument("level", 1, 120),
                                        new StringArgument("difficulty").replaceSuggestions(ArgumentSuggestions.strings(
                                                Arrays.stream(BossDifficulty.values())
                                                        .map(diff -> diff.name().toLowerCase())
                                                        .toArray(String[]::new)
                                        )),
                                        new IntegerArgument("quantity")
                                )
                                .executesPlayer(this::summonBossMob)
                )
                .withSubcommand(
                        new CommandAPICommand("help")
                                .executesPlayer(this::showHelp)
                )
                .register();
    }

    private void summonNormalMob(Player player, CommandArguments args) {
        String entityTypeStr = (String) args.get("entityType");
        int level = (int) args.get("level");
        int quantity = (int) args.get("quantity");

        EntityType entityType;
        try {
            entityType = EntityType.valueOf(entityTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(Component.text("Tipo de entidad inválido: " + entityTypeStr, NamedTextColor.RED));
            return;
        }

        if (!entityType.isSpawnable() || !entityType.isAlive()) {
            player.sendMessage(Component.text("Esta entidad no se puede spawnear o no está viva.", NamedTextColor.RED));
            return;
        }

        Location spawnLocation = player.getLocation();
        while(quantity>0){
            LivingEntity entity = (LivingEntity) player.getWorld().spawnEntity(spawnLocation, entityType);
            quantity = quantity-1;
        }


        LivingEntity entity = (LivingEntity) player.getWorld().spawnEntity(spawnLocation, entityType);

        // Aplicar modificadores de nivel
        applyLevelModifiers(entity, level);

        player.sendMessage(Component.text("✓ Spawneado ", NamedTextColor.GREEN)
                .append(Component.translatable(entityType.translationKey(), NamedTextColor.YELLOW))
                .append(Component.text(" de nivel " + level, NamedTextColor.GREEN)));
    }

    private void summonBossMob(Player player, CommandArguments args) {
        String entityTypeStr = (String) args.get("entityType"); // Cambiar a String
        int level = (int) args.get("level");
        int quantity = (int) args.get("quantity");
        String difficultyStr = (String) args.get("difficulty");

        // Convertir string a EntityType
        EntityType entityType;
        try {
            entityType = EntityType.valueOf(entityTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(Component.text("Tipo de entidad inválido: " + entityTypeStr, NamedTextColor.RED));
            return;
        }

        if (!entityType.isSpawnable() || !entityType.isAlive()) {
            player.sendMessage(Component.text("Tipo de entidad inválido o no spawnable.", NamedTextColor.RED));
            return;
        }

        // Verificar si la entidad puede ser jefe
        boolean canBeBoss = Arrays.stream(HostileMob.values())
                .anyMatch(hostileMob -> hostileMob.getEntityType() == entityType);

        if (!canBeBoss) {
            player.sendMessage(Component.text("Esta entidad no puede ser un jefe.", NamedTextColor.RED));
            return;
        }

        BossDifficulty difficulty;
        try {
            difficulty = BossDifficulty.valueOf(difficultyStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(Component.text("Dificultad inválida. Usa: EASY, NORMAL, HARD, EXTREME", NamedTextColor.RED));
            return;
        }

        Location spawnLocation = player.getLocation();
        LivingEntity entity = null;
        while (quantity > 0) {
            entity = (LivingEntity) player.getWorld().spawnEntity(spawnLocation, entityType);
            quantity = quantity - 1;
        }

        // Aplicar modificadores de jefe
        BossUtil.applyBossModifications(entity, level, difficulty);

        player.sendMessage(Component.text("⚔ Spawneado jefe ", NamedTextColor.GOLD)
                .append(Component.translatable(entityType.translationKey(), NamedTextColor.YELLOW))
                .append(Component.text(" de nivel " + level + " ", NamedTextColor.GOLD))
                .append(Component.text(difficulty.getDisplayName(), difficulty.getColor()))
                .append(Component.text(" ⚔", NamedTextColor.GOLD)));
    }

    private void showHelp(Player player, CommandArguments args) {
        player.sendMessage(Component.text("=== MobLevels Summon Help ===", NamedTextColor.GOLD));
        player.sendMessage(Component.text(""));
        player.sendMessage(Component.text("/summonmob normal <entityType> <level>", NamedTextColor.YELLOW)
                .append(Component.text(" - Spawnea un mob normal con el nivel especificado", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/summonmob boss <entityType> <level> <difficulty>", NamedTextColor.YELLOW)
                .append(Component.text(" - Spawnea un jefe con nivel y dificultad", NamedTextColor.GRAY)));
        player.sendMessage(Component.text(""));
        player.sendMessage(Component.text("Dificultades de jefe:", NamedTextColor.AQUA));
        player.sendMessage(Component.text("• EASY", BossDifficulty.EASY.getColor())
                .append(Component.text(" - Fácil (+15% daño)", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("• NORMAL", BossDifficulty.NORMAL.getColor())
                .append(Component.text(" - Normal (+30% daño)", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("• HARD", BossDifficulty.HARD.getColor())
                .append(Component.text(" - Difícil (+45% daño)", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("• EXTREME", BossDifficulty.EXTREME.getColor())
                .append(Component.text(" - Extremo (+60% daño)", NamedTextColor.GRAY)));
        player.sendMessage(Component.text(""));
        player.sendMessage(Component.text("Ejemplos:", NamedTextColor.GREEN));
        player.sendMessage(Component.text("• /summonmob normal ZOMBIE 25", NamedTextColor.WHITE));
        player.sendMessage(Component.text("• /summonmob boss SKELETON 50 HARD", NamedTextColor.WHITE));
    }

    private void applyLevelModifiers(LivingEntity entity, int level) {
        // Establecer el nivel en el persistent data
        entity.getPersistentDataContainer().set(
                MythicProjectiles.levelKey,
                PersistentDataType.INTEGER,
                level
        );

        // Aplicar modificadores de HP
        var healthAttribute = entity.getAttribute(Attribute.MAX_HEALTH);
        if (healthAttribute != null) {
            double baseHealth = entity.getType().getDefaultAttributes()
                    .getAttribute(Attribute.MAX_HEALTH).getValue();
            double multiplier = Math.pow(1.0425, level - 1);
            double newHealth = baseHealth * multiplier;
            healthAttribute.setBaseValue(newHealth);
            entity.setHealth(newHealth);
        }

        // Aplicar modificadores de daño
        var damageAttribute = entity.getAttribute(Attribute.ATTACK_DAMAGE);
        if (damageAttribute != null) {
            double baseDamage = entity.getType().getDefaultAttributes()
                    .getAttribute(Attribute.ATTACK_DAMAGE).getValue();
            double multiplier = Math.pow(1.0425, level - 1);
            double newDamage = baseDamage * multiplier;
            damageAttribute.setBaseValue(newDamage);
        }
    }
}