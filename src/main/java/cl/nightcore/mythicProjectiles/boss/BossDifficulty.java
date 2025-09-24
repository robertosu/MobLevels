package cl.nightcore.mythicProjectiles.boss;


import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public enum BossDifficulty {
    EASY(1, 1.15, "[Fácil]", NamedTextColor.GREEN),
    NORMAL(1, 1.30, "[Normal]", NamedTextColor.BLUE),
    HARD(2, 1.45, "[Difícil]", NamedTextColor.DARK_PURPLE),
    EXTREME(2, 1.60, "[Extremo]", NamedTextColor.DARK_RED);
    private final int tier;
    private final double damageMultiplier; // Porcentaje de bonificación de daño
    private final String displayName;
    private final NamedTextColor color;
    BossDifficulty(int tier, double damageBonus, String displayName,NamedTextColor color) {
        this.tier = tier;
        this.damageMultiplier = damageBonus;
        this.displayName = displayName;
        this.color = color;
    }
    public int getTier() {
        return tier;
    }


    public double getDamageMultiplier() {
        return damageMultiplier;
    }
    public String getDisplayName() {
        return displayName;
    }
    public TextColor getColor() { return color;}
}