import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class TopBar extends JPanel {
    private final GameWindow gameWindow;
    private final JLabel sunLabel;
    private final List<SeedSlot> seedSlots = new ArrayList<>();
    private int selectedSlotIndex = -1;
    private int sunCount = 50;

    public TopBar(GameWindow gameWindow, int numSlots) {
        this.gameWindow = gameWindow;
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Background container panel with semi-transparent rounded background
        JPanel seedBankPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(20, 20, 20, 190));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2d.setColor(new Color(90, 80, 70));
                g2d.setStroke(new BasicStroke(2f));
                g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 12, 12);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        seedBankPanel.setOpaque(false);
        seedBankPanel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        // Sun Counter Box
        JPanel sunBox = new JPanel(new GridLayout(2, 1));
        sunBox.setPreferredSize(new Dimension(60, 58));
        sunBox.setOpaque(false);

        JLabel sunIconLabel = new JLabel("☀️", SwingConstants.CENTER);
        sunIconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        sunIconLabel.setForeground(new Color(255, 215, 0));

        sunLabel = new JLabel(String.valueOf(sunCount), SwingConstants.CENTER);
        sunLabel.setFont(new Font("Arial", Font.BOLD, 15));
        sunLabel.setForeground(Color.WHITE);

        sunBox.add(sunIconLabel);
        sunBox.add(sunLabel);
        seedBankPanel.add(sunBox);

        // Separator between sun and slots
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(2, 50));
        sep.setForeground(new Color(80, 80, 80));
        seedBankPanel.add(sep);

        // Seed Packet Slots
        for (int i = 0; i < numSlots; i++) {
            final int index = i;
            SeedSlot slot = new SeedSlot(index, () -> onSlotClicked(index));
            seedSlots.add(slot);
            seedBankPanel.add(slot);
        }

        add(seedBankPanel, BorderLayout.WEST);

        // Right side: Shovel & Menu Button
        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightControls.setOpaque(false);

        // Shovel placeholder square
        JPanel shovelBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(30, 30, 30, 200));
                g2d.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 8, 8);
                g2d.setColor(new Color(100, 100, 100));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 8, 8);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        shovelBox.setLayout(new BorderLayout());
        shovelBox.setPreferredSize(new Dimension(52, 52));
        shovelBox.setOpaque(false);
        shovelBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JLabel shovelIcon = new JLabel("⛏", SwingConstants.CENTER);
        shovelIcon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 20));
        shovelIcon.setForeground(Color.LIGHT_GRAY);
        shovelBox.add(shovelIcon, BorderLayout.CENTER);
        rightControls.add(shovelBox);

        // Menu button
        JButton menuButton = createMenuButton("MENU");
        menuButton.addActionListener(e -> gameWindow.showMenu());
        rightControls.add(menuButton);

        add(rightControls, BorderLayout.EAST);
    }

    /**
     * Assign a plant type and its image to a specific seed slot.
     */
    public void assignPlantType(int index, PlantType type, BufferedImage image) {
        if (index >= 0 && index < seedSlots.size()) {
            seedSlots.get(index).setPlantData(type, image);
        }
    }

    /**
     * Get the plant type assigned to a slot (null if empty).
     */
    public PlantType getPlantType(int index) {
        if (index >= 0 && index < seedSlots.size()) {
            return seedSlots.get(index).getPlantType();
        }
        return null;
    }

    /**
     * Update each slot's affordability based on current sun count.
     */
    public void updateAffordability() {
        for (SeedSlot slot : seedSlots) {
            PlantType type = slot.getPlantType();
            if (type != null) {
                slot.setAffordable(sunCount >= type.getSunCost());
            }
        }
    }

    private void onSlotClicked(int index) {
        SeedSlot slot = seedSlots.get(index);
        // Can't select an empty slot
        if (slot.getPlantType() == null) return;

        if (selectedSlotIndex == index) {
            // Deselect
            slot.setSelected(false);
            selectedSlotIndex = -1;
        } else {
            // Deselect previous
            if (selectedSlotIndex >= 0 && selectedSlotIndex < seedSlots.size()) {
                seedSlots.get(selectedSlotIndex).setSelected(false);
            }
            // Select new
            selectedSlotIndex = index;
            slot.setSelected(true);
        }
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(100, 42));
        button.setFont(new Font("Arial", Font.BOLD, 15));
        button.setFocusPainted(false);
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(25, 25, 25, 220));
        button.setBorder(BorderFactory.createLineBorder(new Color(124, 252, 0), 2));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(50, 50, 50, 240));
                button.setBorder(BorderFactory.createLineBorder(new Color(255, 215, 0), 2));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(25, 25, 25, 220));
                button.setBorder(BorderFactory.createLineBorder(new Color(124, 252, 0), 2));
            }
        });

        return button;
    }

    public int getSunCount() { return sunCount; }

    public void setSunCount(int sunCount) {
        applySunCount(sunCount);
        gameWindow.syncSunCount(sunCount, this);
    }

    /**
     * Updates displayed sun without re-broadcasting to other panels.
     */
    public void applySunCount(int sunCount) {
        this.sunCount = sunCount;
        sunLabel.setText(String.valueOf(sunCount));
        updateAffordability();
    }

    public int getSelectedSlotIndex() { return selectedSlotIndex; }

    public List<SeedSlot> getSeedSlots() { return seedSlots; }

    /**
     * Select a slot by index via keyboard shortcut.
     */
    public void selectSlot(int index) {
        if (index < 0 || index >= seedSlots.size()) return;
        if (seedSlots.get(index).getPlantType() == null) return;
        onSlotClicked(index);
    }
}
