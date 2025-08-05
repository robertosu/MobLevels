package cl.nightcore.mythicProjectiles.boss;

import net.kyori.adventure.text.format.NamedTextColor;

public enum BossDifficulty {
    EASY(1, 15, "[Fácil]", NamedTextColor.GREEN),
    NORMAL(2, 30, "[Normal]", NamedTextColor.BLUE),
    HARD(3, 45, "[Difícil]", NamedTextColor.DARK_PURPLE),
    EXTREME(4, 60, "[Extremo]", NamedTextColor.DARK_RED);
    private final int tier;
    private final int damageBonus; // Porcentaje de bonificación de daño
    private final String displayName;
    private final NamedTextColor color;
    BossDifficulty(int tier, int damageBonus, String displayName,NamedTextColor color) {
        this.tier = tier;
        this.damageBonus = damageBonus;
        this.displayName = displayName;
        this.color = color;
    }
    public int getTier() {
        return tier;
    }
    public int getDamageBonus() {
        return damageBonus;
    }
    public String getDisplayName() {
        return displayName;
    }
    public double getDamageMultiplier() {
        return 1.0 + (damageBonus / 100.0);
    }
    public NamedTextColor getColor() { return color;}
}