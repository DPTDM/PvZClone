import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

public class GamePanel extends JPanel {
    private final GameWindow gameWindow;
    private final TopBar topBar;
    private BufferedImage lawnImage;

    public GamePanel(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        setLayout(new BorderLayout());
        loadLawnImage();

        // Top bar HUD with sun counter, seed slots (squares), shovel, and menu button
        this.topBar = new TopBar(gameWindow, 6);
        add(topBar, BorderLayout.NORTH);
    }

    public TopBar getTopBar() {
        return topBar;
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
