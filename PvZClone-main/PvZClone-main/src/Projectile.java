import java.awt.*;
import java.awt.image.BufferedImage;

public class Projectile {
    private double x, y;
    private final int row; // which lane this projectile is in
    private final BufferedImage image;
    private static final double SPEED = 200; // pixels per second
    public static final int SIZE = 30;

    public Projectile(double x, double y, BufferedImage image) {
        this.x = x;
        this.y = y;
        this.image = image;
        this.row = -1; // unset
    }

    public Projectile(double x, double y, int row, BufferedImage image) {
        this.x = x;
        this.y = y;
        this.row = row;
        this.image = image;
    }

    public void update(long deltaMs) {
        x += SPEED * deltaMs / 1000.0;
    }

    public void draw(Graphics2D g) {
        if (image != null) {
            g.drawImage(image, (int) x, (int) y, SIZE, SIZE, null);
        } else {
            g.setColor(Color.YELLOW);
            g.fillOval((int) x, (int) y, SIZE, SIZE);
        }
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public int getRow() { return row; }

    public boolean isOffScreen(int screenWidth) {
        return x > screenWidth;
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, SIZE, SIZE);
    }
}
