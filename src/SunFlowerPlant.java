import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Moonlamp sun-generating plant.
 * Starts in idle form while charging, switches to bloom form when sun is ready,
 * and waits until clicked before granting sun and restarting its charge cycle.
 */
public class SunFlowerPlant extends Plant {
    public static final long CHARGE_INTERVAL = 8000; // 8 seconds to bloom
    public static final int SUN_VALUE = 25;

    private final BufferedImage bloomImage;
    private long chargeTimer = 0;
    private boolean bloomed = false;
    private double pulsePhase = 0;

    public SunFlowerPlant(int row, int col, BufferedImage idleImage) {
        this(row, col, idleImage, idleImage);
    }

    public SunFlowerPlant(int row, int col, BufferedImage idleImage, BufferedImage bloomImage) {
        super(row, col, idleImage);
        this.bloomImage = bloomImage;
    }

    @Override
    public void update(GamePanel game, long deltaMs) {
        if (bloomed) {
            pulsePhase += deltaMs / 300.0;
            return; // Stays bloomed and does not charge until clicked
        }

        chargeTimer += deltaMs;
        if (chargeTimer >= CHARGE_INTERVAL) {
            chargeTimer = CHARGE_INTERVAL;
            bloomed = true;
            pulsePhase = 0;
        }
    }

    public boolean isBloomed() {
        return bloomed;
    }

    /**
     * Harvests ready sun from this moonlamp and returns it to normal charging form.
     *
     * @return amount of sun collected (0 if not bloomed)
     */
    public int collectSun() {
        if (!bloomed) {
            return 0;
        }
        bloomed = false;
        chargeTimer = 0;
        return SUN_VALUE;
    }

    @Override
    public void draw(Graphics2D g, int x, int y, int cellW, int cellH, boolean mirrored) {
        if (bloomed) {
            int glowAlpha = (int) (35 + 20 * Math.sin(pulsePhase));
            int glowPad = 6;
            g.setColor(new Color(80, 210, 255, glowAlpha));
            g.fillOval(x + glowPad, y + glowPad, cellW - glowPad * 2, cellH - glowPad * 2);
        }

        BufferedImage activeImage = (bloomed && bloomImage != null) ? bloomImage : image;
        if (activeImage != null) {
            drawCellImage(g, activeImage, x, y, cellW, cellH, mirrored);
        } else {
            g.setColor(bloomed ? new Color(80, 200, 255, 200) : new Color(30, 80, 100, 200));
            g.fillRoundRect(x + 6, y + 6, cellW - 12, cellH - 12, 8, 8);
        }
    }
}
