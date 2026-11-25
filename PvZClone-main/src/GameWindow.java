import javax.swing.*;
import java.awt.*;

public class GameWindow extends JFrame {
    public static final String MENU_SCREEN = "MENU";
    public static final String GAME_SCREEN = "GAME";
    public static final int CANVAS_WIDTH = 1024;
    public static final int CANVAS_HEIGHT = 577;

    private final CardLayout cardLayout;
    private final JPanel containerPanel;

    public GameWindow() {
        setTitle("Plants vs. Zombies");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        containerPanel = new JPanel(cardLayout);
        containerPanel.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));

        MainMenuPanel menuPanel = new MainMenuPanel(this);
        GamePanel gamePanel = new GamePanel(this);

        containerPanel.add(menuPanel, MENU_SCREEN);
        containerPanel.add(gamePanel, GAME_SCREEN);

        add(containerPanel);
        pack();
        setResizable(false);
        setLocationRelativeTo(null);

        cardLayout.show(containerPanel, MENU_SCREEN);
    }

    public void showMenu() {
        cardLayout.show(containerPanel, MENU_SCREEN);
    }

    public void showGame() {
        cardLayout.show(containerPanel, GAME_SCREEN);
    }
}
