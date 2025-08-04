        package cl.nightcore.mythicProjectiles.listener;

import cl.nightcore.mythicProjectiles.MythicProjectiles;
import dev.aurelium.auraskills.api.event.skill.EntityXpGainEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class XpGainListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    private void onXpGain(EntityXpGainEvent event) {

        var source = event.getAttacked();
        var level = MythicProjectiles.getLevel(source);
        event.setAmount(calculateMobXP(level, event.getAmount()));
    }


    public double calculateMobXP(int mobLevel, double baseXP) {
        double xpMultiplier = Math.pow(1.025, mobLevel - 1);
        return baseXP * xpMultiplier;
    }


}
