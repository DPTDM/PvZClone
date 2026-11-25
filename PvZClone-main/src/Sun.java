import java.awt.*;
import java.awt.image.BufferedImage;

public class Sun {
    private double x, y;
    private final double targetY;
    private final BufferedImage image;
    public static final int SIZE = 45;
    private static final double FALL_SPEED = 30; // pixels per second
    private long lifetime = 0;
    private static final long MAX_LIFETIME = 12000; // 12 seconds
    private boolean collected = false;
    private static final int SUN_VALUE = 50;

    // Pulsing animation
    private double pulsePhase = 0;

    public Sun(double x, double y, BufferedImage image) {
        this.x = x;
        this.y = y;
        this.targetY = y + 50; // fall a bit then stop
        this.image = image;
    }

    public void update(long deltaMs) {
        lifetime += deltaMs;
        pulsePhase += deltaMs / 300.0;

        if (y < targetY) {
            y += FALL_SPEED * deltaMs / 1000.0;
            if (y > targetY) y = targetY;
        }
    }

    public void draw(Graphics2D g) {
        if (collected) return;

        // Subtle pulsing glow effect
        int glowAlpha = (int) (40 + 25 * Math.sin(pulsePhase));
        int glowPad = 6;
        g.setColor(new Color(255, 255, 0, glowAlpha));
        g.fillOval((int) x - glowPad, (int) y - glowPad,
                SIZE + glowPad * 2, SIZE + glowPad * 2);

        if (image != null) {
            g.drawImage(image, (int) x, (int) y, SIZE, SIZE, null);
        } else {
            g.setColor(new Color(255, 215, 0));
            g.fillOval((int) x, (int) y, SIZE, SIZE);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString("☀", (int) x + 12, (int) y + 28);
        }

        // Fade out in last 3 seconds
        if (lifetime > MAX_LIFETIME - 3000) {
            float fadeAlpha = (MAX_LIFETIME - lifetime) / 3000f;
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, fadeAlpha)));
        }
    }

    public boolean contains(int px, int py) {
        return px >= x && px <= x + SIZE && py >= y && py <= y + SIZE;
    }

    public boolean isExpired() {
        return lifetime >= MAX_LIFETIME || collected;
    }

    public void collect() {
        collected = true;
    }

    public boolean isCollected() {
        return collected;
    }

    public static int getSunValue() {
        return SUN_VALUE;
    }
}
