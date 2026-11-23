import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel {
    private final GameWindow gameWindow;

    public GamePanel(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        setLayout(new BorderLayout());
        setBackground(new Color(34, 139, 34)); // Lawn green

        JLabel label = new JLabel("Game Screen - Lawn Coming Soon!", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 28));
        label.setForeground(Color.WHITE);
        add(label, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomBar.setOpaque(false);
        JButton backButton = new JButton("Back to Main Menu");
        backButton.setFont(new Font("Arial", Font.BOLD, 16));
        backButton.setFocusPainted(false);
        backButton.setBackground(new Color(30, 30, 30));
        backButton.setForeground(Color.WHITE);
        backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> gameWindow.showMenu());
        bottomBar.add(backButton);

        add(bottomBar, BorderLayout.SOUTH);
    }
}
