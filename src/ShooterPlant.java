import java.awt.image.BufferedImage;

public class ShooterPlant extends Plant {
    private static final long DEFAULT_SHOOT_INTERVAL = 2000L; // 2 seconds

    private final long shootIntervalMs;
    private final int damage;
    private final int projectileSize;
    private long shootTimer = 0;

    public ShooterPlant(int row, int col, BufferedImage image) {
        this(row, col, 100, DEFAULT_SHOOT_INTERVAL, Projectile.DEFAULT_DAMAGE, Projectile.SIZE, image);
    }

    public ShooterPlant(int row, int col, int maxHealth, long shootIntervalMs,
                        int damage, int projectileSize, BufferedImage image) {
        super(row, col, image);
        this.health = maxHealth;
        this.shootIntervalMs = shootIntervalMs;
        this.damage = damage;
        this.projectileSize = projectileSize;
    }

    public long getShootIntervalMs() { return shootIntervalMs; }
    public int getDamage() { return damage; }

    @Override
    public void update(GamePanel game, long deltaMs) {
        shootTimer += deltaMs;
        if (shootTimer >= shootIntervalMs) {
            shootTimer -= shootIntervalMs;
            int dir = game.getLawnSide().getProjectileDirection();
            int[] pos = game.getCellPosition(row, col);
            int cellW = game.getCellWidth();
            int cellH = game.getCellHeight();
            double projX = dir >= 0 ? (pos[0] + cellW) : (pos[0] - projectileSize);
            double projY = pos[1] + cellH / 2.0 - projectileSize / 2.0;
            game.addProjectile(new Projectile(projX, projY, row, dir, damage, projectileSize, image));
        }
    }
}
