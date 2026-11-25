import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

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

        // Number keys 1-6 select the corresponding seed slot in the top bar
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                if (key >= KeyEvent.VK_1 && key <= KeyEvent.VK_6) {
                    gamePanel.getTopBar().selectSlot(key - KeyEvent.VK_1);
                }
            }
        });
        setFocusable(true);
        requestFocusInWindow();

        cardLayout.show(containerPanel, MENU_SCREEN);
    }

    public void showMenu() {
        cardLayout.show(containerPanel, MENU_SCREEN);
    }

    public void showGame() {
        cardLayout.show(containerPanel, GAME_SCREEN);
    }
}
