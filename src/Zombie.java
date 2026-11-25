import java.awt.*;
import java.awt.image.BufferedImage;

public class Zombie {
    private double x, y;
    private final int row;
    private final int direction; // -1 left (normal), +1 right (mirrored)
    private final BufferedImage image;
    private int health = 100;
    private static final double SPEED = 20; // pixels per second (slow shamble)
    private boolean eating = false;
    private int eatingCol = -1;
    private long eatTimer = 0;
    private static final long EAT_INTERVAL = 1000; // damage every 1 second
    private static final int EAT_DAMAGE = 20;

    // Bobbing animation
    private double bobPhase = Math.random() * Math.PI * 2;

    public Zombie(int row, double x, double y, BufferedImage image) {
        this(row, x, y, -1, image);
    }

    public Zombie(int row, double x, double y, int direction, BufferedImage image) {
        this.row = row;
        this.x = x;
        this.y = y;
        this.direction = direction >= 0 ? 1 : -1;
        this.image = image;
    }

    public void update(GamePanel game, long deltaMs) {
        bobPhase += deltaMs / 250.0;

        if (eating) {
            eatTimer += deltaMs;
            if (eatTimer >= EAT_INTERVAL) {
                eatTimer -= EAT_INTERVAL;
                Plant plant = game.getPlantAt(row, eatingCol);
                if (plant != null) {
                    plant.takeDamage(EAT_DAMAGE);
                } else {
                    eating = false;
                    eatingCol = -1;
                }
            }
        } else {
            x += direction * SPEED * deltaMs / 1000.0;

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
        double bobOffset = eating ? 0 : Math.sin(bobPhase) * 3;
        int drawSize = getDrawSize(cellH);
        int drawX = (int) x;
        int drawY = (int) (y + (cellH - drawSize) / 2.0 + bobOffset);

        if (image != null) {
            if (direction < 0) {
                g.drawImage(image, drawX, drawY, drawSize, drawSize, null);
            } else {
                // Flip horizontally on mirrored lawn so zombie faces toward the plants
                g.drawImage(image, drawX + drawSize, drawY, drawX, drawY + drawSize,
                        0, 0, image.getWidth(), image.getHeight(), null);
            }
        } else {
            g.setColor(new Color(140, 60, 60, 210));
            g.fillRoundRect(drawX, drawY, drawSize, drawSize, 8, 8);
        }

        // Health bar above zombie
        int barWidth = drawSize;
        int barHeight = 4;
        int barX = drawX;
        int barY = drawY - 6;

        g.setColor(new Color(60, 0, 0, 180));
        g.fillRect(barX, barY, barWidth, barHeight);

        double healthPct = Math.max(0, health / 100.0);
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
    public int getHealth() { return health; }

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
