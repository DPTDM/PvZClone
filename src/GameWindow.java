import javax.swing.*;
import java.awt.*;

public class GameWindow extends JFrame {
    public static final String MENU_SCREEN = "MENU";
    public static final String GAME_SCREEN = "GAME";

    private final CardLayout cardLayout;
    private final JPanel containerPanel;

    public GameWindow() {
        setTitle("Plants vs. Zombies");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setResizable(false);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        containerPanel = new JPanel(cardLayout);

        MainMenuPanel menuPanel = new MainMenuPanel(this);
        GamePanel gamePanel = new GamePanel(this);

        containerPanel.add(menuPanel, MENU_SCREEN);
        containerPanel.add(gamePanel, GAME_SCREEN);

        add(containerPanel);
        cardLayout.show(containerPanel, MENU_SCREEN);
    }

    public void showMenu() {
        cardLayout.show(containerPanel, MENU_SCREEN);
    }

    public void showGame() {
        cardLayout.show(containerPanel, GAME_SCREEN);
    }
}
