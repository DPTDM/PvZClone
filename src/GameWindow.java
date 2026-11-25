import javax.swing.*;
import java.awt.*;

public class GameWindow extends JFrame {
    public static final String MENU_SCREEN = "MENU";
    public static final String NORMAL_GAME_SCREEN = "GAME_NORMAL";
    public static final String MIRRORED_GAME_SCREEN = "GAME_MIRRORED";
    public static final String GAME_SCREEN = NORMAL_GAME_SCREEN;
    public static final int CANVAS_WIDTH = 1024;
    public static final int CANVAS_HEIGHT = 577;

    private final CardLayout cardLayout;
    private final JPanel containerPanel;
    private final GamePanel normalGamePanel;
    private final GamePanel mirroredGamePanel;
    private LawnSide currentLawnSide = LawnSide.NORMAL;

    public GameWindow() {
        setTitle("Plants vs. Zombies");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        containerPanel = new JPanel(cardLayout);
        containerPanel.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));

        MainMenuPanel menuPanel = new MainMenuPanel(this);
        normalGamePanel = new GamePanel(this, LawnSide.NORMAL);
        mirroredGamePanel = new GamePanel(this, LawnSide.MIRRORED);

        containerPanel.add(menuPanel, MENU_SCREEN);
        containerPanel.add(normalGamePanel, NORMAL_GAME_SCREEN);
        containerPanel.add(mirroredGamePanel, MIRRORED_GAME_SCREEN);

        add(containerPanel);
        pack();
        setResizable(false);
        setLocationRelativeTo(null);

        cardLayout.show(containerPanel, MENU_SCREEN);
    }

    public LawnSide getLawnSide() {
        return currentLawnSide;
    }

    /**
     * Returns the GamePanel corresponding to the opposite lawn side.
     */
    public GamePanel getOppositePanel(LawnSide activeSide) {
        return activeSide == LawnSide.NORMAL ? mirroredGamePanel : normalGamePanel;
    }

    /**
     * Transfers the active view to the opposite lawn.
     */
    public void toggleLawnSide() {
        showGame(currentLawnSide.toggle());
    }

    public void showMenu() {
        cardLayout.show(containerPanel, MENU_SCREEN);
    }

    public void showGame() {
        showGame(currentLawnSide);
    }

    public void showGame(LawnSide side) {
        if (side == null) {
            side = LawnSide.NORMAL;
        }
        this.currentLawnSide = side;
        String cardName = side == LawnSide.MIRRORED ? MIRRORED_GAME_SCREEN : NORMAL_GAME_SCREEN;
        cardLayout.show(containerPanel, cardName);
    }
}
