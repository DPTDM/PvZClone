import java.awt.*;
import java.awt.image.BufferedImage;

public class Zombie {
    private double x, y;
    private final int row;
    private final BufferedImage image;
    private int health = 100;
    private static final double SPEED = 20; // pixels per second (slow shamble)
    private boolean eating = false;
    private long eatTimer = 0;
    private static final long EAT_INTERVAL = 1000; // damage every 1 second
    private static final int EAT_DAMAGE = 20;

    // Bobbing animation
    private double bobPhase = Math.random() * Math.PI * 2;

    public Zombie(int row, double x, double y, BufferedImage image) {
        this.row = row;
        this.x = x;
        this.y = y;
        this.image = image;
    }

    public void update(GamePanel game, long deltaMs) {
        bobPhase += deltaMs / 250.0;

        if (eating) {
            // Damage the plant we're on
            eatTimer += deltaMs;
            if (eatTimer >= EAT_INTERVAL) {
                eatTimer -= EAT_INTERVAL;
                Plant plant = game.getPlantAt(row, getCurrentCol(game));
                if (plant != null) {
                    plant.takeDamage(EAT_DAMAGE);
                } else {
                    eating = false; // plant is gone, resume walking
                }
            }
        } else {
            x -= SPEED * deltaMs / 1000.0;

            // Check if we've reached a plant
            int col = getCurrentCol(game);
            if (col >= 0 && col < GamePanel.GRID_COLS) {
                Plant plant = game.getPlantAt(row, col);
                if (plant != null) {
                    // Snap to the plant's cell and start eating
                    int[] pos = game.getCellPosition(row, col);
                    // Stop at the right edge of the cell
                    if (x <= pos[0] + game.getCellWidth()) {
                        eating = true;
                        eatTimer = 0;
                    }
                }
            }
        }
    }

    private int getCurrentCol(GamePanel game) {
        int cellW = game.getCellWidth();
        if (cellW <= 0) return -1;
        return (int) ((x - GamePanel.GRID_X_OFFSET) / cellW);
    }

    public void draw(Graphics2D g, int cellH) {
        if (image == null) return;

        // Slight vertical bobbing while walking
        double bobOffset = eating ? 0 : Math.sin(bobPhase) * 3;
        int drawSize = (int) (cellH * 0.9);
        int drawX = (int) x;
        int drawY = (int) (y + (cellH - drawSize) / 2.0 + bobOffset);

        g.drawImage(image, drawX, drawY, drawSize, drawSize, null);

        // Health bar above zombie
        int barWidth = drawSize;
        int barHeight = 4;
        int barX = drawX;
        int barY = drawY - 6;

        // Background
        g.setColor(new Color(60, 0, 0, 180));
        g.fillRect(barX, barY, barWidth, barHeight);

        // Health fill
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
    public int getHealth() { return health; }

    public void takeDamage(int amount) {
        health -= amount;
    }

    public boolean isDead() { return health <= 0; }

    public boolean isOffScreen() { return x < -60; }

    public Rectangle getBounds(int cellH) {
        int drawSize = (int) (cellH * 0.9);
        return new Rectangle((int) x, (int) y, drawSize, drawSize);
    }
}
