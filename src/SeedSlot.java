import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class SeedSlot extends JPanel {
    private final int slotIndex;
    private boolean selected = false;
    private boolean hovered = false;
    private boolean affordable = true;

    // Plant data (null if slot is empty)
    private PlantType plantType;
    private BufferedImage plantImage;

    public SeedSlot(int slotIndex, Runnable onClick) {
        this.slotIndex = slotIndex;
        setPreferredSize(new Dimension(55, 62));
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (onClick != null) {
                    onClick.run();
                }
            }
        });
    }

    public int getSlotIndex() { return slotIndex; }
    public boolean isSelected() { return selected; }

    public void setSelected(boolean selected) {
        this.selected = selected;
        repaint();
    }

    public PlantType getPlantType() { return plantType; }

    public void setPlantData(PlantType type, BufferedImage image) {
        this.plantType = type;
        this.plantImage = image;
        repaint();
    }

    public void setAffordable(boolean affordable) {
        this.affordable = affordable;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth();
        int h = getHeight();

        // Background
        if (plantType != null && !affordable) {
            g2d.setColor(new Color(60, 25, 25, 220)); // Dark red = can't afford
        } else if (selected) {
            g2d.setColor(new Color(40, 75, 40, 220));
        } else if (hovered) {
            g2d.setColor(new Color(55, 55, 55, 220));
        } else {
            g2d.setColor(new Color(30, 30, 30, 200));
        }
        g2d.fillRoundRect(2, 2, w - 4, h - 4, 8, 8);

        // Border
        if (selected) {
            g2d.setColor(new Color(124, 252, 0)); // Bright green
            g2d.setStroke(new BasicStroke(2.5f));
        } else if (hovered) {
            g2d.setColor(new Color(255, 215, 0)); // Gold
            g2d.setStroke(new BasicStroke(2f));
        } else {
            g2d.setColor(new Color(100, 100, 100));
            g2d.setStroke(new BasicStroke(1.5f));
        }
        g2d.drawRoundRect(2, 2, w - 4, h - 4, 8, 8);

        if (plantImage != null && plantType != null) {
            // Draw plant thumbnail (top part of slot)
            int imgSize = Math.min(w, h) - 22;
            int imgX = (w - imgSize) / 2;
            int imgY = 5;

            // Gray out if not affordable
            if (!affordable) {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
            }
            g2d.drawImage(plantImage, imgX, imgY, imgSize, imgSize, null);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

            // Draw sun cost at bottom
            g2d.setColor(affordable ? new Color(255, 215, 0) : new Color(200, 80, 80));
            g2d.setFont(new Font("Arial", Font.BOLD, 11));
            String costText = String.valueOf(plantType.getSunCost());
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(costText, (w - fm.stringWidth(costText)) / 2, h - 5);
        } else {
            // Empty slot: show slot number
            g2d.setColor(selected ? new Color(124, 252, 0) : new Color(100, 100, 100, 150));
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            String text = String.valueOf(slotIndex + 1);
            FontMetrics fm = g2d.getFontMetrics();
            int textX = (w - fm.stringWidth(text)) / 2;
            int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2d.drawString(text, textX, textY);
        }

        g2d.dispose();
    }
}
