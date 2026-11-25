import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Settings screen allowing players to customize keybindings and gameplay options.
 */
public class SettingsPanel extends JPanel {
    private static final Color LAWN_GREEN = new Color(124, 252, 0);
    private static final Color GOLD = new Color(255, 215, 0);

    private final GameWindow gameWindow;
    private final GameSettings settings;
    private final Map<String, JButton> keybindButtons = new LinkedHashMap<>();
    private final JButton gridToggleButton;
    private final JButton difficultyButton;
    private final JButton startingSunButton;
    private final JLabel statusLabel;

    private String listeningActionId = null;

    public SettingsPanel(GameWindow gameWindow, GameSettings settings) {
        this.gameWindow = gameWindow;
        this.settings = settings;

        setLayout(new BorderLayout(0, 14));
        setBackground(Color.BLACK);
        setBorder(BorderFactory.createEmptyBorder(24, 48, 24, 48));

        // Header
        JLabel titleLabel = new JLabel("SETTINGS", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 38));
        titleLabel.setForeground(LAWN_GREEN);
        add(titleLabel, BorderLayout.NORTH);

        // Two-column body: Keybinds (left) & Gameplay Options (right)
        JPanel columnsPanel = new JPanel(new GridLayout(1, 2, 32, 0));
        columnsPanel.setOpaque(false);

        JPanel keybindsCard = createSectionPanel("KEYBINDINGS");
        addKeybindRow(keybindsCard, "Switch Lawn", "SWITCH_LAWN");
        addKeybindRow(keybindsCard, "Restart Game", "RESTART");
        addKeybindRow(keybindsCard, "Back to Menu", "MENU");
        for (int i = 0; i < GameSettings.SLOT_COUNT; i++) {
            addKeybindRow(keybindsCard, "Seed Slot " + (i + 1), "SLOT_" + i);
        }

        JPanel optionsCard = createSectionPanel("GAMEPLAY & DISPLAY");
        gridToggleButton = createOptionButton();
        gridToggleButton.addActionListener(e -> {
            cancelListening();
            settings.setShowGridOverlay(!settings.isShowGridOverlay());
            refreshLabels();
            gameWindow.repaintActiveScreens();
        });
        addOptionRow(optionsCard, "Show Grid Overlay", gridToggleButton);

        difficultyButton = createOptionButton();
        difficultyButton.addActionListener(e -> {
            cancelListening();
            settings.cycleDifficulty();
            refreshLabels();
            setStatus("Difficulty applies to zombie spawns and speed.", Color.LIGHT_GRAY);
        });
        addOptionRow(optionsCard, "Difficulty", difficultyButton);

        startingSunButton = createOptionButton();
        startingSunButton.addActionListener(e -> {
            cancelListening();
            settings.cycleStartingSun();
            refreshLabels();
            setStatus("Starting sun applies on next game or restart.", Color.LIGHT_GRAY);
        });
        addOptionRow(optionsCard, "Starting Sun", startingSunButton);

        optionsCard.add(Box.createVerticalGlue());

        columnsPanel.add(keybindsCard);
        columnsPanel.add(optionsCard);
        add(columnsPanel, BorderLayout.CENTER);

        // Footer: status hint + Reset / Back buttons
        JPanel footer = new JPanel(new BorderLayout(0, 12));
        footer.setOpaque(false);

        statusLabel = new JLabel("Click any keybind button, then press a key to assign.", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        statusLabel.setForeground(Color.LIGHT_GRAY);
        footer.add(statusLabel, BorderLayout.NORTH);

        JPanel footerButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        footerButtons.setOpaque(false);

        JButton resetButton = createActionButton("RESET DEFAULTS", 200);
        resetButton.addActionListener(e -> {
            cancelListening();
            settings.resetToDefaults();
            refreshLabels();
            gameWindow.refreshKeyBindings();
            gameWindow.repaintActiveScreens();
            setStatus("All settings reset to defaults.", LAWN_GREEN);
        });

        JButton backButton = createActionButton("BACK", 160);
        backButton.addActionListener(e -> {
            cancelListening();
            gameWindow.refreshKeyBindings();
            gameWindow.closeSettings();
        });

        footerButtons.add(resetButton);
        footerButtons.add(backButton);
        footer.add(footerButtons, BorderLayout.SOUTH);

        add(footer, BorderLayout.SOUTH);

        // Capture next key press globally while listening for a keybind (so SPACE/TAB/ESC are captured)
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (listeningActionId == null || !isShowing()) {
                return false;
            }
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                handleKeyCaptured(e.getKeyCode());
                return true;
            }
            return e.getID() == KeyEvent.KEY_TYPED || e.getID() == KeyEvent.KEY_RELEASED;
        });

        refreshLabels();
    }

    private JPanel createSectionPanel(String headerText) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(22, 22, 22));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 70), 2),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)
        ));

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        headerRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel header = new JLabel(headerText);
        header.setFont(new Font("Arial", Font.BOLD, 16));
        header.setForeground(GOLD);
        headerRow.add(header, BorderLayout.CENTER);

        panel.add(headerRow);
        panel.add(Box.createVerticalStrut(10));
        return panel;
    }

    private void addKeybindRow(JPanel parent, String labelText, String actionId) {
        JButton button = createOptionButton();
        button.addActionListener(e -> startListening(actionId, labelText));
        keybindButtons.put(actionId, button);
        addOptionRow(parent, labelText, button);
    }

    private void addOptionRow(JPanel parent, String labelText, JButton controlButton) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.PLAIN, 15));
        label.setForeground(Color.WHITE);

        row.add(label, BorderLayout.WEST);
        row.add(controlButton, BorderLayout.EAST);

        parent.add(row);
        parent.add(Box.createVerticalStrut(6));
    }

    private JButton createOptionButton() {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(135, 30));
        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setFocusable(false);
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(40, 40, 40));
        button.setBorder(BorderFactory.createLineBorder(new Color(95, 95, 95), 1));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createActionButton(String text, int width) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(width, 44));
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setFocusable(false);
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(40, 40, 40));
        button.setBorder(BorderFactory.createLineBorder(new Color(90, 90, 90), 2));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(60, 60, 60));
                button.setBorder(BorderFactory.createLineBorder(LAWN_GREEN, 2));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(40, 40, 40));
                button.setBorder(BorderFactory.createLineBorder(new Color(90, 90, 90), 2));
            }
        });
        return button;
    }

    private void startListening(String actionId, String labelText) {
        listeningActionId = actionId;
        refreshLabels();
        JButton btn = keybindButtons.get(actionId);
        if (btn != null) {
            btn.setText("PRESS KEY...");
            btn.setForeground(GOLD);
            btn.setBorder(BorderFactory.createLineBorder(GOLD, 2));
        }
        setStatus("Listening for '" + labelText + "' — press any key to bind.", GOLD);
    }

    public void cancelListening() {
        if (listeningActionId != null) {
            listeningActionId = null;
            refreshLabels();
        }
    }

    private void handleKeyCaptured(int keyCode) {
        if (keyCode == KeyEvent.VK_UNDEFINED) {
            return;
        }
        String actionId = listeningActionId;
        String conflict = settings.findConflictingAction(keyCode, actionId);
        if (conflict != null) {
            listeningActionId = null;
            refreshLabels();
            setStatus(GameSettings.formatKey(keyCode) + " is already bound to " + conflict + ".", new Color(255, 110, 110));
            return;
        }

        assignKey(actionId, keyCode);
        listeningActionId = null;
        refreshLabels();
        gameWindow.refreshKeyBindings();
        setStatus("Bound to " + GameSettings.formatKey(keyCode) + ".", LAWN_GREEN);
    }

    private void assignKey(String actionId, int keyCode) {
        if ("SWITCH_LAWN".equals(actionId)) {
            settings.setSwitchLawnKey(keyCode);
        } else if ("RESTART".equals(actionId)) {
            settings.setRestartKey(keyCode);
        } else if ("MENU".equals(actionId)) {
            settings.setMenuKey(keyCode);
        } else if (actionId.startsWith("SLOT_")) {
            int idx = Integer.parseInt(actionId.substring(5));
            settings.setSlotKey(idx, keyCode);
        }
    }

    private int getKeyForAction(String actionId) {
        if ("SWITCH_LAWN".equals(actionId)) return settings.getSwitchLawnKey();
        if ("RESTART".equals(actionId)) return settings.getRestartKey();
        if ("MENU".equals(actionId)) return settings.getMenuKey();
        if (actionId.startsWith("SLOT_")) {
            return settings.getSlotKey(Integer.parseInt(actionId.substring(5)));
        }
        return KeyEvent.VK_UNDEFINED;
    }

    public void refreshLabels() {
        for (Map.Entry<String, JButton> entry : keybindButtons.entrySet()) {
            JButton btn = entry.getValue();
            btn.setText(GameSettings.formatKey(getKeyForAction(entry.getKey())));
            btn.setForeground(Color.WHITE);
            btn.setBorder(BorderFactory.createLineBorder(new Color(95, 95, 95), 1));
        }
        gridToggleButton.setText(settings.isShowGridOverlay() ? "ON" : "OFF");
        gridToggleButton.setForeground(settings.isShowGridOverlay() ? LAWN_GREEN : Color.LIGHT_GRAY);
        difficultyButton.setText(settings.getDifficulty().getLabel().toUpperCase());
        startingSunButton.setText(String.valueOf(settings.getStartingSun()));
    }

    private void setStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }
}
