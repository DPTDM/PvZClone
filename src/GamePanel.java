import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

public class GamePanel extends JPanel {
    private final GameWindow gameWindow;
    private final TopBar topBar;
    private final LawnSide lawnSide;
    private BufferedImage lawnImage;

    public GamePanel(GameWindow gameWindow) {
        this(gameWindow, LawnSide.NORMAL);
    }

    public GamePanel(GameWindow gameWindow, LawnSide lawnSide) {
        this.gameWindow = gameWindow;
        this.lawnSide = lawnSide != null ? lawnSide : LawnSide.NORMAL;
        setLayout(new BorderLayout());
        loadLawnImage();

        // Top bar HUD with sun counter, seed slots (squares), shovel, and menu button
        this.topBar = new TopBar(gameWindow, 6);
        add(topBar, BorderLayout.NORTH);

        // Bottom-corner live preview of the opposite lawn (bottom-right on normal, bottom-left on mirrored)
        int previewAlignment = this.lawnSide.isMirrored() ? FlowLayout.LEFT : FlowLayout.RIGHT;
        JPanel bottomCorner = new JPanel(new FlowLayout(previewAlignment, 14, 14));
        bottomCorner.setOpaque(false);
        LawnPreviewPanel previewPanel = new LawnPreviewPanel(
                () -> gameWindow.getOppositePanel(this.lawnSide),
                gameWindow::toggleLawnSide
        );
        bottomCorner.add(previewPanel);
        add(bottomCorner, BorderLayout.SOUTH);
    }

    public TopBar getTopBar() {
        return topBar;
    }

    public LawnSide getLawnSide() {
        return lawnSide;
    }

    /**
     * Renders a scaled live snapshot of this lawn's scene (without HUD overlays)
     * into the provided graphics context for the opposite lawn's corner preview.
     */
    public void renderLivePreview(Graphics2D g2d, int targetWidth, int targetHeight) {
        int srcW = getWidth() > 0 ? getWidth() : GameWindow.CANVAS_WIDTH;
        int srcH = getHeight() > 0 ? getHeight() : GameWindow.CANVAS_HEIGHT;
        if (getWidth() <= 0 || getHeight() <= 0) {
            setSize(srcW, srcH);
        }

        Graphics2D scaled = (Graphics2D) g2d.create();
        scaled.scale((double) targetWidth / srcW, (double) targetHeight / srcH);
        paintComponent(scaled);
        scaled.dispose();
    }

    private void loadLawnImage() {
        lawnImage = loadImage(lawnSide.getClasspathPath(), lawnSide.getFilePath());

        // Fallback: generate horizontally flipped image from normal lawn if mirrored asset is missing
        if (lawnImage == null && lawnSide == LawnSide.MIRRORED) {
            BufferedImage normal = loadImage(LawnSide.NORMAL.getClasspathPath(), LawnSide.NORMAL.getFilePath());
            if (normal != null) {
                lawnImage = flipHorizontally(normal);
            }
        }
    }

    private BufferedImage loadImage(String classpathPath, String filePath) {
        try {
            // Attempt 1: Load from classpath
            try (InputStream is = getClass().getResourceAsStream(classpathPath)) {
                if (is != null) {
                    return ImageIO.read(is);
                }
            }

            // Attempt 2: Load from root assets/ directory
            File file = new File(filePath);
            if (file.exists()) {
                return ImageIO.read(file);
            }

            // Attempt 3: Load from src/assets/ directory
            file = new File("src/" + filePath);
            if (file.exists()) {
                return ImageIO.read(file);
            }
        } catch (Exception e) {
            System.err.println("Failed to load image (" + filePath + "): " + e.getMessage());
        }
        return null;
    }

    private BufferedImage flipHorizontally(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        BufferedImage flipped = new BufferedImage(width, height, source.getType() != 0 ? source.getType() : BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = flipped.createGraphics();
        AffineTransform transform = AffineTransform.getScaleInstance(-1, 1);
        transform.translate(-width, 0);
        g2d.drawImage(source, transform, null);
        g2d.dispose();
        return flipped;
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
