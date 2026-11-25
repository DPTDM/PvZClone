import java.awt.*;
import java.awt.image.BufferedImage;

public class Zombie {
    private static final long VAULT_DURATION_MS = 420L;

    private double x, y;
    private final int row;
    private final int direction; // -1 left (normal), +1 right (mirrored)
    private final ZombieType type;
    private final BufferedImage image;
    private final int maxHealth;
    private int health;

    private boolean eating = false;
    private int eatingCol = -1;
    private long eatTimer = 0;

    // Vaulter state
    private boolean hasVaulted = false;
    private boolean vaulting = false;
    private long vaultTimer = 0;
    private double vaultStartX;
    private double vaultTargetX;

    // Bobbing animation
    private double bobPhase = Math.random() * Math.PI * 2;

    public Zombie(int row, double x, double y, BufferedImage image) {
        this(row, x, y, -1, ZombieType.NORMAL, image);
    }

    public Zombie(int row, double x, double y, int direction, BufferedImage image) {
        this(row, x, y, direction, ZombieType.NORMAL, image);
    }

    public Zombie(int row, double x, double y, int direction, ZombieType type, BufferedImage image) {
        this.row = row;
        this.x = x;
        this.y = y;
        this.direction = direction >= 0 ? 1 : -1;
        this.type = type != null ? type : ZombieType.NORMAL;
        this.image = image;
        this.maxHealth = this.type.getMaxHealth();
        this.health = this.maxHealth;
    }

    public void update(GamePanel game, long deltaMs) {
        bobPhase += deltaMs / 250.0;

        if (vaulting) {
            vaultTimer += deltaMs;
            double progress = Math.min(1.0, (double) vaultTimer / VAULT_DURATION_MS);
            x = vaultStartX + (vaultTargetX - vaultStartX) * progress;
            if (progress >= 1.0) {
                vaulting = false;
            }
            return;
        }

        if (eating) {
            eatTimer += deltaMs;
            if (eatTimer >= type.getEatIntervalMs()) {
                eatTimer -= type.getEatIntervalMs();
                Plant plant = game.getPlantAt(row, eatingCol);
                if (plant != null) {
                    plant.takeDamage(type.getEatDamage());
                } else {
                    eating = false;
                    eatingCol = -1;
                }
            }
            return;
        }

        double speedMult = (type == ZombieType.VAULTER && hasVaulted) ? 0.95 : type.getSpeedMultiplier();
        x += direction * game.getZombieSpeed() * speedMult * deltaMs / 1000.0;

        int drawSize = getDrawSize(game.getCellHeight());
        int col = getFrontCol(game, drawSize);
        if (col >= 0 && col < GamePanel.GRID_COLS) {
            Plant plant = game.getPlantAt(row, col);
            if (plant != null) {
                int[] pos = game.getCellPosition(row, col);
                boolean reachedPlant = direction < 0
                        ? x <= pos[0] + game.getCellWidth()
                        : x + drawSize >= pos[0];
                if (reachedPlant) {
                    if (type.canVault() && !hasVaulted) {
                        hasVaulted = true;
                        vaulting = true;
                        vaultTimer = 0;
                        vaultStartX = x;
                        vaultTargetX = direction < 0
                                ? (pos[0] - drawSize - 4)
                                : (pos[0] + game.getCellWidth() + 4);
                    } else {
                        eating = true;
                        eatingCol = col;
                        eatTimer = 0;
                    }
                }
            }
        }
    }

    private int getDrawSize(int cellH) {
        return (int) (cellH * 0.9);
    }

    private int getFrontCol(GamePanel game, int drawSize) {
        int cellW = game.getCellWidth();
        if (cellW <= 0) return -1;
        double frontX = direction < 0 ? x : (x + drawSize);
        return (int) Math.floor((frontX - game.getGridXOffset()) / (double) cellW);
    }

    public void draw(Graphics2D g, int cellH) {
        double bobOffset = eating || vaulting ? 0 : Math.sin(bobPhase) * 3;
        double vaultArcOffset = 0;
        if (vaulting) {
            double progress = Math.min(1.0, (double) vaultTimer / VAULT_DURATION_MS);
            vaultArcOffset = -32.0 * Math.sin(Math.PI * progress);
        }

        int drawSize = getDrawSize(cellH);
        int drawX = (int) x;
        int drawY = (int) (y + (cellH - drawSize) / 2.0 + bobOffset + vaultArcOffset);

        if (image != null) {
            if (direction < 0) {
                g.drawImage(image, drawX, drawY, drawSize, drawSize, null);
            } else {
                // Flip horizontally on mirrored lawn so zombie faces toward the plants
                g.drawImage(image, drawX + drawSize, drawY, drawX, drawY + drawSize,
                        0, 0, image.getWidth(), image.getHeight(), null);
            }
        } else {
            Color fallback = type.getTintColor() != null ? type.getTintColor() : new Color(140, 60, 60);
            g.setColor(new Color(fallback.getRed(), fallback.getGreen(), fallback.getBlue(), 215));
            g.fillRoundRect(drawX, drawY, drawSize, drawSize, 8, 8);
        }

        // Color-coded outline for quick variant identification
        if (type.getTintColor() != null) {
            Stroke prevStroke = g.getStroke();
            g.setColor(type.getTintColor());
            g.setStroke(new BasicStroke(2f));
            g.drawRect(drawX, drawY, drawSize, drawSize);
            g.setStroke(prevStroke);
        }

        // Health bar above zombie
        int barWidth = drawSize;
        int barHeight = 4;
        int barX = drawX;
        int barY = drawY - 6;

        g.setColor(new Color(60, 0, 0, 180));
        g.fillRect(barX, barY, barWidth, barHeight);

        double healthPct = Math.max(0, (double) health / maxHealth);
        Color hpColor = healthPct > 0.5 ? new Color(0, 200, 0) :
                         healthPct > 0.25 ? new Color(255, 165, 0) :
                         new Color(220, 0, 0);
        g.setColor(hpColor);
        g.fillRect(barX, barY, (int) (barWidth * healthPct), barHeight);
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public int getRow() { return row; }
    public int getDirection() { return direction; }
    public ZombieType getType() { return type; }
    public int getHealth() { return health; }
    public boolean isVaulting() { return vaulting; }
    public boolean hasVaulted() { return hasVaulted; }

    public void takeDamage(int amount) {
        health -= amount;
    }

    public boolean isDead() { return health <= 0; }

    public boolean isOffScreen(int screenWidth) {
        return direction < 0 ? x < -60 : x > screenWidth + 60;
    }

    public Rectangle getBounds(int cellH) {
        int drawSize = getDrawSize(cellH);
        return new Rectangle((int) x, (int) y, drawSize, drawSize);
    }
}
