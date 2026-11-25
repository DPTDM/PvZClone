import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainMenuPanel extends JPanel {
    private final GameWindow gameWindow;

    public MainMenuPanel(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        setLayout(new GridBagLayout());
        setBackground(Color.BLACK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 10, 15, 10);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title Label
        JLabel titleLabel = new JLabel("PLANTS vs. ZOMBIES", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 46));
        titleLabel.setForeground(new Color(124, 252, 0)); // Lawn Green

        JLabel subtitleLabel = new JLabel("OOP Edition", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        subtitleLabel.setForeground(Color.LIGHT_GRAY);

        // Buttons
        JButton playButton = createMenuButton("PLAY");
        JButton settingsButton = createMenuButton("SETTINGS");
        JButton quitButton = createMenuButton("QUIT");

        // Action listeners
        playButton.addActionListener(e -> gameWindow.showGame());
        settingsButton.addActionListener(e -> gameWindow.showSettings(GameWindow.MENU_SCREEN));
        quitButton.addActionListener(e -> System.exit(0));

        // Add components to layout
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 10, 5, 10);
        add(titleLabel, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 10, 40, 10);
        add(subtitleLabel, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(10, 10, 10, 10);
        add(playButton, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(10, 10, 10, 10);
        add(settingsButton, gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(10, 10, 20, 10);
        add(quitButton, gbc);
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(220, 50));
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setFocusPainted(false);
        button.setFocusable(false);
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(40, 40, 40));
        button.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 2));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(60, 60, 60));
                button.setBorder(BorderFactory.createLineBorder(new Color(124, 252, 0), 2));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(40, 40, 40));
                button.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 2));
            }
        });

        return button;
    }
}
