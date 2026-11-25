import java.awt.image.BufferedImage;

public class SunFlowerPlant extends Plant {
    private long sunTimer = 0;
    private static final long SUN_INTERVAL = 8000; // 8 seconds

    public SunFlowerPlant(int row, int col, BufferedImage image) {
        super(row, col, image);
    }

    @Override
    public void update(GamePanel game, long deltaMs) {
        sunTimer += deltaMs;
        if (sunTimer >= SUN_INTERVAL) {
            sunTimer -= SUN_INTERVAL;
            // Create sun at plant position, slightly offset
            int[] pos = game.getCellPosition(row, col);
            int cellW = game.getCellWidth();
            game.addSun(new Sun(pos[0] + cellW / 2.0 - Sun.SIZE / 2.0,
                    pos[1], image));
        }
    }
}
