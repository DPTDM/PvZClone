import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class GameWindow extends JFrame {
    public static final String MENU_SCREEN = "MENU";
    public static final String SETTINGS_SCREEN = "SETTINGS";
    public static final String NORMAL_GAME_SCREEN = "GAME_NORMAL";
    public static final String MIRRORED_GAME_SCREEN = "GAME_MIRRORED";
    public static final String GAME_SCREEN = NORMAL_GAME_SCREEN;
    public static final int CANVAS_WIDTH = 1024;
    public static final int CANVAS_HEIGHT = 577;

    private final GameSettings settings = new GameSettings();
    private final CardLayout cardLayout;
    private final JPanel containerPanel;
    private final SettingsPanel settingsPanel;
    private final GamePanel normalGamePanel;
    private final GamePanel mirroredGamePanel;

    private LawnSide currentLawnSide = LawnSide.NORMAL;
    private String currentScreen = MENU_SCREEN;
    private String settingsReturnScreen = MENU_SCREEN;
    private boolean matchStarted = false;

    public GameWindow() {
        setTitle("Plants vs. Zombies");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        containerPanel = new JPanel(cardLayout);
        containerPanel.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));

        MainMenuPanel menuPanel = new MainMenuPanel(this);
        settingsPanel = new SettingsPanel(this, settings);
        normalGamePanel = new GamePanel(this, LawnSide.NORMAL);
        mirroredGamePanel = new GamePanel(this, LawnSide.MIRRORED);

        containerPanel.add(menuPanel, MENU_SCREEN);
        containerPanel.add(settingsPanel, SETTINGS_SCREEN);
        containerPanel.add(normalGamePanel, NORMAL_GAME_SCREEN);
        containerPanel.add(mirroredGamePanel, MIRRORED_GAME_SCREEN);

        add(containerPanel);
        pack();
        setResizable(false);
        setLocationRelativeTo(null);

        refreshKeyBindings();
        cardLayout.show(containerPanel, MENU_SCREEN);
    }

    public GameSettings getSettings() {
        return settings;
    }

    public LawnSide getLawnSide() {
        return currentLawnSide;
    }

    public String getActiveGameCardName() {
        return currentLawnSide == LawnSide.MIRRORED ? MIRRORED_GAME_SCREEN : NORMAL_GAME_SCREEN;
    }

    public GamePanel getActiveGamePanel() {
        return currentLawnSide == LawnSide.MIRRORED ? mirroredGamePanel : normalGamePanel;
    }

    /**
     * Returns the GamePanel corresponding to the opposite lawn side.
     */
    public GamePanel getOppositePanel(LawnSide activeSide) {
        return activeSide == LawnSide.NORMAL ? mirroredGamePanel : normalGamePanel;
    }

    /**
     * Rebuilds window keybindings from the current GameSettings.
     */
    public void refreshKeyBindings() {
        InputMap inputMap = containerPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = containerPanel.getActionMap();
        inputMap.clear();
        actionMap.clear();

        // Seed slots 1-6
        for (int i = 0; i < GameSettings.SLOT_COUNT; i++) {
            final int slotIndex = i;
            String actionKey = "selectSlot" + i;
            inputMap.put(KeyStroke.getKeyStroke(settings.getSlotKey(i), 0), actionKey);
            actionMap.put(actionKey, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (isInGame()) {
                        getActiveGamePanel().getTopBar().selectSlot(slotIndex);
                    }
                }
            });
        }

        // Switch lawn (default: Space)
        inputMap.put(KeyStroke.getKeyStroke(settings.getSwitchLawnKey(), 0), "switchLawn");
        actionMap.put("switchLawn", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isInGame()) {
                    toggleLawnSide();
                }
            }
        });

        // Restart game (default: R)
        inputMap.put(KeyStroke.getKeyStroke(settings.getRestartKey(), 0), "restartGame");
        actionMap.put("restartGame", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isInGame()) {
                    restartGame();
                }
            }
        });

        // Back to menu / close settings (default: Escape)
        inputMap.put(KeyStroke.getKeyStroke(settings.getMenuKey(), 0), "menuAction");
        actionMap.put("menuAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (SETTINGS_SCREEN.equals(currentScreen)) {
                    closeSettings();
                } else if (isInGame()) {
                    showMenu();
                }
            }
        });

        repaintActiveScreens();
    }

    public boolean isInGame() {
        return NORMAL_GAME_SCREEN.equals(currentScreen) || MIRRORED_GAME_SCREEN.equals(currentScreen);
    }

    public void repaintActiveScreens() {
        normalGamePanel.repaint();
        mirroredGamePanel.repaint();
    }

    /**
     * Keeps the shared sun count synchronized across both lawns.
     */
    public void syncSunCount(int sunCount, TopBar source) {
        if (normalGamePanel.getTopBar() != source) {
            normalGamePanel.getTopBar().applySunCount(sunCount);
        }
        if (mirroredGamePanel.getTopBar() != source) {
            mirroredGamePanel.getTopBar().applySunCount(sunCount);
        }
    }

    /**
     * Transfers the active view to the opposite lawn.
     */
    public void toggleLawnSide() {
        showGame(currentLawnSide.toggle());
    }

    /**
     * Resets both lawns and starts a fresh match from the Normal lawn.
     */
    public void restartGame() {
        int startingSun = settings.getStartingSun();
        normalGamePanel.resetGame(startingSun);
        mirroredGamePanel.resetGame(startingSun);
        matchStarted = true;
        showGame(LawnSide.NORMAL);
    }

    public void showMenu() {
        normalGamePanel.pauseGame();
        mirroredGamePanel.pauseGame();
        currentScreen = MENU_SCREEN;
        cardLayout.show(containerPanel, MENU_SCREEN);
    }

    public void showSettings(String returnScreen) {
        normalGamePanel.pauseGame();
        mirroredGamePanel.pauseGame();
        settingsReturnScreen = returnScreen != null ? returnScreen : MENU_SCREEN;
        settingsPanel.cancelListening();
        settingsPanel.refreshLabels();
        currentScreen = SETTINGS_SCREEN;
        cardLayout.show(containerPanel, SETTINGS_SCREEN);
    }

    public void closeSettings() {
        if (NORMAL_GAME_SCREEN.equals(settingsReturnScreen)) {
            showGame(LawnSide.NORMAL);
        } else if (MIRRORED_GAME_SCREEN.equals(settingsReturnScreen)) {
            showGame(LawnSide.MIRRORED);
        } else {
            showMenu();
        }
    }

    public void showGame() {
        if (!matchStarted || normalGamePanel.isGameOver() || mirroredGamePanel.isGameOver()) {
            restartGame();
        } else {
            showGame(currentLawnSide);
        }
    }

    public void showGame(LawnSide side) {
        if (side == null) {
            side = LawnSide.NORMAL;
        }
        this.currentLawnSide = side;
        if (!matchStarted) {
            int startingSun = settings.getStartingSun();
            normalGamePanel.resetGame(startingSun);
            mirroredGamePanel.resetGame(startingSun);
            matchStarted = true;
        }
        currentScreen = getActiveGameCardName();
        cardLayout.show(containerPanel, currentScreen);
        normalGamePanel.startGame();
        mirroredGamePanel.startGame();
    }
}
