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
        draw(g, x, y, cellW, cellH, false);
    }

    public void draw(Graphics2D g, int x, int y, int cellW, int cellH, boolean mirrored) {
        if (image != null) {
            drawCellImage(g, image, x, y, cellW, cellH, mirrored);
        } else {
            // Fallback colored rectangle
            g.setColor(new Color(0, 200, 0, 180));
            g.fillRoundRect(x + 6, y + 6, cellW - 12, cellH - 12, 8, 8);
        }
    }

    protected void drawCellImage(Graphics2D g, BufferedImage img, int x, int y,
                                 int cellW, int cellH, boolean mirrored) {
        int padding = 4;
        int dx1 = x + padding;
        int dy1 = y + padding;
        int dx2 = x + cellW - padding;
        int dy2 = y + cellH - padding;
        if (!mirrored) {
            g.drawImage(img, dx1, dy1, dx2 - dx1, dy2 - dy1, null);
        } else {
            g.drawImage(img, dx2, dy1, dx1, dy2, 0, 0, img.getWidth(), img.getHeight(), null);
        }
    }

    public int getRow() { return row; }
    public int getCol() { return col; }
    public int getHealth() { return health; }
    public void takeDamage(int amount) { health -= amount; }
    public boolean isDead() { return health <= 0; }
}
