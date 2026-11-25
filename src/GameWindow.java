import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class GameWindow extends JFrame {
    public static final String MENU_SCREEN = "MENU";
    public static final String GAME_SCREEN = "GAME";
    public static final int CANVAS_WIDTH = 1024;
    public static final int CANVAS_HEIGHT = 577;

    private final CardLayout cardLayout;
    private final JPanel containerPanel;
    private final GamePanel gamePanel;

    public GameWindow() {
        setTitle("Plants vs. Zombies");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        containerPanel = new JPanel(cardLayout);
        containerPanel.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));

        MainMenuPanel menuPanel = new MainMenuPanel(this);
        gamePanel = new GamePanel(this);

        containerPanel.add(menuPanel, MENU_SCREEN);
        containerPanel.add(gamePanel, GAME_SCREEN);

        add(containerPanel);
        pack();
        setResizable(false);
        setLocationRelativeTo(null);

        // Number keys 1-6 select the corresponding seed slot regardless of button focus
        InputMap inputMap = containerPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = containerPanel.getActionMap();
        for (int i = 0; i < 6; i++) {
            final int slotIndex = i;
            String actionKey = "selectSlot" + i;
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_1 + i, 0), actionKey);
            actionMap.put(actionKey, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (gamePanel.isShowing()) {
                        gamePanel.getTopBar().selectSlot(slotIndex);
                    }
                }
            });
        }

        cardLayout.show(containerPanel, MENU_SCREEN);
    }

    public void showMenu() {
        gamePanel.pauseGame();
        cardLayout.show(containerPanel, MENU_SCREEN);
    }

    public void showGame() {
        cardLayout.show(containerPanel, GAME_SCREEN);
        gamePanel.startGame();
    }
}
