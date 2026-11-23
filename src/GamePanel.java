import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

public class GamePanel extends JPanel {
    private final GameWindow gameWindow;
    private BufferedImage lawnImage;

    public GamePanel(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        setLayout(new BorderLayout());
        loadLawnImage();

        // Top bar for in-game controls (PvZ-style Menu button in top right)
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JButton menuButton = createMenuButton("MENU");
        menuButton.addActionListener(e -> gameWindow.showMenu());

        topBar.add(menuButton, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);
    }

    private void loadLawnImage() {
        try {
            // Attempt 1: Load from classpath
            InputStream is = getClass().getResourceAsStream("/assets/lawn.jpg");
            if (is != null) {
                lawnImage = ImageIO.read(is);
                return;
            }

            // Attempt 2: Load from root assets/ directory
            File file = new File("assets/lawn.jpg");
            if (file.exists()) {
                lawnImage = ImageIO.read(file);
                return;
            }

            // Attempt 3: Load from src/assets/ directory
            file = new File("src/assets/lawn.jpg");
            if (file.exists()) {
                lawnImage = ImageIO.read(file);
            }
        } catch (Exception e) {
            System.err.println("Failed to load lawn image: " + e.getMessage());
        }
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(110, 40));
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(25, 25, 25, 220));
        button.setBorder(BorderFactory.createLineBorder(new Color(124, 252, 0), 2));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(50, 50, 50, 240));
                button.setBorder(BorderFactory.createLineBorder(new Color(255, 215, 0), 2));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(25, 25, 25, 220));
                button.setBorder(BorderFactory.createLineBorder(new Color(124, 252, 0), 2));
            }
        });

        return button;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (lawnImage != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(lawnImage, 0, 0, getWidth(), getHeight(), this);
            g2d.dispose();
        } else {
            g.setColor(new Color(34, 139, 34));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
