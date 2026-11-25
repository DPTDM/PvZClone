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
            int dir = game.getLawnSide().getProjectileDirection();
            int[] pos = game.getCellPosition(row, col);
            int cellW = game.getCellWidth();
            int cellH = game.getCellHeight();
            double projX = dir >= 0 ? (pos[0] + cellW) : (pos[0] - Projectile.SIZE);
            double projY = pos[1] + cellH / 2.0 - Projectile.SIZE / 2.0;
            game.addProjectile(new Projectile(projX, projY, row, dir, image));
        }
    }
}
