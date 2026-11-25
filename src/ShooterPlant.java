import java.awt.image.BufferedImage;

public class ShooterPlant extends Plant {
    private long shootTimer = 0;
    private static final long SHOOT_INTERVAL = 2000; // 2 seconds

    public ShooterPlant(int row, int col, BufferedImage image) {
        super(row, col, image);
    }

    @Override
    public void update(GamePanel game, long deltaMs) {
        shootTimer += deltaMs;
        if (shootTimer >= SHOOT_INTERVAL) {
            shootTimer -= SHOOT_INTERVAL;
            // Fire a projectile from the right edge of this cell, centered vertically
            int[] pos = game.getCellPosition(row, col);
            int cellW = game.getCellWidth();
            int cellH = game.getCellHeight();
            double projX = pos[0] + cellW;
            double projY = pos[1] + cellH / 2.0 - Projectile.SIZE / 2.0;
            game.addProjectile(new Projectile(projX, projY, row, image));
        }
    }
}
