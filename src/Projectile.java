import java.awt.*;
import java.awt.image.BufferedImage;

public class Projectile {
    public static final int DEFAULT_DAMAGE = 25;
    public static final int SIZE = 30;

    private double x, y;
    private final int row; // which lane this projectile is in
    private final int direction; // +1 right, -1 left
    private final int damage;
    private final int size;
    private final BufferedImage image;
    private static final double SPEED = 200; // pixels per second

    public Projectile(double x, double y, BufferedImage image) {
        this(x, y, -1, 1, DEFAULT_DAMAGE, SIZE, image);
    }

    public Projectile(double x, double y, int row, BufferedImage image) {
        this(x, y, row, 1, DEFAULT_DAMAGE, SIZE, image);
    }

    public Projectile(double x, double y, int row, int direction, BufferedImage image) {
        this(x, y, row, direction, DEFAULT_DAMAGE, SIZE, image);
    }

    public Projectile(double x, double y, int row, int direction, int damage, int size, BufferedImage image) {
        this.x = x;
        this.y = y;
        this.row = row;
        this.direction = direction >= 0 ? 1 : -1;
        this.damage = damage;
        this.size = size;
        this.image = image;
    }

    public void update(long deltaMs) {
        x += direction * SPEED * deltaMs / 1000.0;
    }

    public void draw(Graphics2D g) {
        if (image != null) {
            if (direction >= 0) {
                g.drawImage(image, (int) x, (int) y, size, size, null);
            } else {
                int drawX = (int) x;
                int drawY = (int) y;
                g.drawImage(image, drawX + size, drawY, drawX, drawY + size,
                        0, 0, image.getWidth(), image.getHeight(), null);
            }
        } else {
            g.setColor(Color.YELLOW);
            g.fillOval((int) x, (int) y, size, size);
        }
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public int getRow() { return row; }
    public int getDirection() { return direction; }
    public int getDamage() { return damage; }
    public int getSize() { return size; }

    public boolean isOffScreen(int screenWidth) {
        return direction >= 0 ? x > screenWidth : x + size < 0;
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, size, size);
    }
}
