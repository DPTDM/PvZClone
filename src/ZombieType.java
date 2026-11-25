import java.awt.Color;

/**
 * Zombie variants differentiated by speed, bite behavior, vaulting, and color tint
 * (keeping standard health pools without HP inflation).
 */
public enum ZombieType {
    NORMAL("Normal", 100, 1.0, 20, 1000L, false, null),
    RUNNER("Runner", 75, 1.85, 15, 900L, false, new Color(225, 50, 50)),
    CHOMPER("Chomper", 100, 0.95, 35, 600L, false, new Color(245, 140, 25)),
    VAULTER("Vaulter", 100, 1.4, 20, 1000L, true, new Color(175, 65, 235));

    private final String displayName;
    private final int maxHealth;
    private final double speedMultiplier;
    private final int eatDamage;
    private final long eatIntervalMs;
    private final boolean canVault;
    private final Color tintColor;

    ZombieType(String displayName, int maxHealth, double speedMultiplier,
               int eatDamage, long eatIntervalMs, boolean canVault, Color tintColor) {
        this.displayName = displayName;
        this.maxHealth = maxHealth;
        this.speedMultiplier = speedMultiplier;
        this.eatDamage = eatDamage;
        this.eatIntervalMs = eatIntervalMs;
        this.canVault = canVault;
        this.tintColor = tintColor;
    }

    public String getDisplayName() { return displayName; }
    public int getMaxHealth() { return maxHealth; }
    public double getSpeedMultiplier() { return speedMultiplier; }
    public int getEatDamage() { return eatDamage; }
    public long getEatIntervalMs() { return eatIntervalMs; }
    public boolean canVault() { return canVault; }
    public Color getTintColor() { return tintColor; }
}
