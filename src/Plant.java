import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class Plant {
    protected int row, col;
    protected BufferedImage image;
    protected int health = 100;

    public Plant(int row, int col, BufferedImage image) {
        this.row = row;
        this.col = col;
        this.image = image;
    }

    public abstract void update(GamePanel game, long deltaMs);

    public void draw(Graphics2D g, int x, int y, int cellW, int cellH) {
        if (image != null) {
            int padding = 4;
            g.drawImage(image, x + padding, y + padding,
                    cellW - padding * 2, cellH - padding * 2, null);
        } else {
            // Fallback colored rectangle
            g.setColor(new Color(0, 200, 0, 180));
            g.fillRoundRect(x + 6, y + 6, cellW - 12, cellH - 12, 8, 8);
        }
    }

    public int getRow() { return row; }
    public int getCol() { return col; }
    public int getHealth() { return health; }
    public void takeDamage(int amount) { health -= amount; }
    public boolean isDead() { return health <= 0; }
}
