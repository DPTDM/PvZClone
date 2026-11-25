import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Supplier;

/**
 * Interactive bottom-corner widget showing a live scaled view of the opposite lawn.
 * Clicking it transfers the player to that lawn.
 */
public class LawnPreviewPanel extends JPanel {
    private static final int PREVIEW_WIDTH = 192;
    private static final int PREVIEW_HEIGHT = 108;
    private static final int CORNER_RADIUS = 12;
    private static final int REFRESH_DELAY_MS = 50;

    private final Supplier<GamePanel> targetPanelSupplier;
    private final Timer refreshTimer;
    private boolean hovered = false;

    public LawnPreviewPanel(Supplier<GamePanel> targetPanelSupplier, Runnable onTransfer) {
        this.targetPanelSupplier = targetPanelSupplier;
        setPreferredSize(new Dimension(PREVIEW_WIDTH, PREVIEW_HEIGHT));
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        refreshTimer = new Timer(REFRESH_DELAY_MS, e -> {
            if (isShowing()) {
                repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                GamePanel target = targetPanelSupplier.get();
                if (target != null) {
                    setToolTipText("Switch to " + target.getLawnSide().getDisplayName() + " Lawn");
                }
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (onTransfer != null) {
                    onTransfer.run();
                }
            }
        });
    }

    @Override
    public void addNotify() {
        super.addNotify();
        refreshTimer.start();
    }

    @Override
    public void removeNotify() {
        refreshTimer.stop();
        super.removeNotify();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        Shape clipShape = new RoundRectangle2D.Float(2, 2, w - 4, h - 4, CORNER_RADIUS, CORNER_RADIUS);

        // Render live scene of the opposite lawn inside the rounded clip
        Graphics2D contentG = (Graphics2D) g2d.create();
        contentG.clip(clipShape);
        contentG.translate(2, 2);

        GamePanel targetPanel = targetPanelSupplier.get();
        if (targetPanel != null) {
            targetPanel.renderLivePreview(contentG, w - 4, h - 4);
        } else {
            contentG.setColor(Color.DARK_GRAY);
            contentG.fillRect(0, 0, w - 4, h - 4);
        }

        if (hovered) {
            contentG.setColor(new Color(255, 255, 255, 30));
            contentG.fillRect(0, 0, w - 4, h - 4);
        }

        // Bottom caption strip
        int stripHeight = 22;
        contentG.setColor(new Color(15, 15, 15, 185));
        contentG.fillRect(0, (h - 4) - stripHeight, w - 4, stripHeight);

        String labelText = targetPanel != null
                ? "LIVE • " + targetPanel.getLawnSide().getDisplayName().toUpperCase()
                : "SWITCH LAWN";
        contentG.setFont(new Font("Arial", Font.BOLD, 11));
        contentG.setColor(hovered ? new Color(124, 252, 0) : Color.WHITE);
        FontMetrics fm = contentG.getFontMetrics();
        int textX = ((w - 4) - fm.stringWidth(labelText)) / 2;
        int textY = (h - 4) - stripHeight + ((stripHeight - fm.getHeight()) / 2) + fm.getAscent();
        contentG.drawString(labelText, textX, textY);
        contentG.dispose();

        // Outer border
        if (hovered) {
            g2d.setColor(new Color(124, 252, 0));
            g2d.setStroke(new BasicStroke(2.5f));
        } else {
            g2d.setColor(new Color(180, 180, 180, 210));
            g2d.setStroke(new BasicStroke(2f));
        }
        g2d.draw(clipShape);

        g2d.dispose();
    }
}
