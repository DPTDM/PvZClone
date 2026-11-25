import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener {
    // ---- Grid constants aligned to the lawn brick pattern ----
    public static final int GRID_ROWS = 5;
    public static final int GRID_COLS = 9;
    public static final int GRID_X_OFFSET = 75;
    public static final int GRID_Y_OFFSET = 150;
    public static final int GRID_CELL_W = 83;
    public static final int GRID_CELL_H = 76;

    private final GameWindow gameWindow;
    private final TopBar topBar;
    private final LawnSide lawnSide;
    private final int gridXOffset;
    private BufferedImage lawnImage;

    // Game entities
    private final Plant[][] grid = new Plant[GRID_ROWS][GRID_COLS];
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<Sun> suns = new ArrayList<>();
    private final List<Zombie> zombies = new ArrayList<>();

    // Game loop
    private final Timer gameTimer;
    private long lastUpdateTime;
    private final Random rng = new Random();

    // Time-scaled zombie spawning (scales zombie count & rate over time, no HP inflation)
    private long elapsedGameMs = 0;
    private long zombieSpawnTimer = 0;
    private long zombieSpawnInterval;
    private long surgeTimerMs = 0;
    private int surgesTriggered = 0;
    private long waveBannerTimerMs = 0;

    // Plant and zombie images
    private BufferedImage p1Image;
    private BufferedImage p1LiteImage;
    private BufferedImage moonlampIdleImage;
    private BufferedImage moonlampBloomImage;
    private final Map<ZombieType, BufferedImage> zombieImages = new EnumMap<>(ZombieType.class);

    // Game state
    private boolean gameOver = false;
    private boolean renderingPreview = false;

    public GamePanel(GameWindow gameWindow) {
        this(gameWindow, LawnSide.NORMAL);
    }

    public GamePanel(GameWindow gameWindow, LawnSide lawnSide) {
        this.gameWindow = gameWindow;
        this.lawnSide = lawnSide != null ? lawnSide : LawnSide.NORMAL;
        this.gridXOffset = this.lawnSide.isMirrored()
                ? (GameWindow.CANVAS_WIDTH - GRID_X_OFFSET - GRID_COLS * GRID_CELL_W)
                : GRID_X_OFFSET;
        this.zombieSpawnInterval = gameWindow.getSettings().getSpawnIntervalMs(0);
        setLayout(new BorderLayout());
        loadLawnImage();
        loadPlantImages();

        // Top bar HUD with sun counter, seed slots, shovel, restart, settings, and menu button
        this.topBar = new TopBar(gameWindow, 6);
        add(topBar, BorderLayout.NORTH);

        // Assign plant types to the first 3 seed slots
        topBar.assignPlantType(0, PlantType.SHOOTER, p1Image);
        topBar.assignPlantType(1, PlantType.SUNFLOWER, moonlampBloomImage);
        topBar.assignPlantType(2, PlantType.LITE_SHOOTER, p1LiteImage);
        topBar.updateAffordability();

        // Bottom-corner live preview of the opposite lawn (bottom-right on normal, bottom-left on mirrored)
        int previewAlignment = this.lawnSide.isMirrored() ? FlowLayout.LEFT : FlowLayout.RIGHT;
        JPanel bottomCorner = new JPanel(new FlowLayout(previewAlignment, 14, 14));
        bottomCorner.setOpaque(false);
        LawnPreviewPanel previewPanel = new LawnPreviewPanel(
                () -> gameWindow.getOppositePanel(this.lawnSide),
                () -> GameSettings.formatKey(gameWindow.getSettings().getSwitchLawnKey()),
                gameWindow::toggleLawnSide
        );
        bottomCorner.add(previewPanel);
        add(bottomCorner, BorderLayout.SOUTH);

        // Mouse listener for placing plants and collecting suns
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });

        // Game loop at ~60fps (started when game becomes active)
        gameTimer = new Timer(16, this);
        lastUpdateTime = System.currentTimeMillis();
    }

    public void startGame() {
        lastUpdateTime = System.currentTimeMillis();
        if (!gameTimer.isRunning()) {
            gameTimer.start();
        }
    }

    public void pauseGame() {
        if (gameTimer.isRunning()) {
            gameTimer.stop();
        }
    }

    /**
     * Clears all entities and resets timers and sun for a fresh match.
     */
    public void resetGame(int startingSun) {
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                grid[r][c] = null;
            }
        }
        projectiles.clear();
        suns.clear();
        zombies.clear();

        gameOver = false;
        elapsedGameMs = 0;
        zombieSpawnTimer = 0;
        surgeTimerMs = 0;
        surgesTriggered = 0;
        waveBannerTimerMs = 0;
        zombieSpawnInterval = gameWindow.getSettings().getSpawnIntervalMs(0);
        lastUpdateTime = System.currentTimeMillis();

        topBar.clearSelection();
        topBar.applySunCount(startingSun);
        repaint();
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public TopBar getTopBar() {
        return topBar;
    }

    public LawnSide getLawnSide() {
        return lawnSide;
    }

    public int getGridXOffset() {
        return gridXOffset;
    }

    /**
     * Zombie travel direction (-1 left on normal lawn, +1 right on mirrored lawn).
     */
    public int getZombieDirection() {
        return -lawnSide.getProjectileDirection();
    }

    public double getZombieSpeed() {
        return gameWindow.getSettings().getDifficulty().getZombieSpeed();
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
        renderingPreview = true;
        try {
            paintComponent(scaled);
        } finally {
            renderingPreview = false;
            scaled.dispose();
        }
    }

    // ---- Grid helpers (fixed cell sizes) ----

    public int getCellWidth() {
        return GRID_CELL_W;
    }

    public int getCellHeight() {
        return GRID_CELL_H;
    }

    public int[] getCellPosition(int row, int col) {
        return new int[]{gridXOffset + col * GRID_CELL_W,
                         GRID_Y_OFFSET + row * GRID_CELL_H};
    }

    // ---- Entity management ----

    public void addProjectile(Projectile p) {
        projectiles.add(p);
    }

    public void addSun(Sun s) {
        suns.add(s);
    }

    public Plant getPlantAt(int row, int col) {
        if (row >= 0 && row < GRID_ROWS && col >= 0 && col < GRID_COLS) {
            return grid[row][col];
        }
        return null;
    }

    // ---- Click handling ----

    private void handleClick(int px, int py) {
        if (gameOver) return;

        // 1) Check if clicked on a sun (prioritize sun collection)
        for (Sun sun : suns) {
            if (!sun.isCollected() && sun.contains(px, py)) {
                sun.collect();
                topBar.setSunCount(topBar.getSunCount() + Sun.getSunValue());
                return;
            }
        }

        // 2) Resolve grid cell
        int[] cell = getGridCell(px, py);
        if (cell == null) return;

        int row = cell[0], col = cell[1];

        // 3) If shovel is active, dig up the clicked plant without refunding sun
        if (topBar.isShovelSelected()) {
            if (grid[row][col] != null) {
                grid[row][col] = null;
                topBar.setShovelSelected(false);
                repaint();
            }
            return;
        }

        // 4) Harvest bloomed Moonlamp before planting
        if (grid[row][col] instanceof SunFlowerPlant moonlamp && moonlamp.isBloomed()) {
            int gained = moonlamp.collectSun();
            if (gained > 0) {
                topBar.setSunCount(topBar.getSunCount() + gained);
                repaint();
            }
            return;
        }

        // 3) Check if placing a plant on an empty cell
        if (grid[row][col] != null) return; // already occupied

        int selectedSlot = topBar.getSelectedSlotIndex();
        if (selectedSlot < 0) return;

        PlantType type = topBar.getPlantType(selectedSlot);
        if (type == null) return;

        // Check sun cost
        if (topBar.getSunCount() < type.getSunCost()) return;

        // Place plant
        Plant plant;
        switch (type) {
            case SHOOTER:
                plant = new ShooterPlant(row, col, p1Image);
                break;
            case SUNFLOWER:
                plant = new SunFlowerPlant(row, col, moonlampIdleImage, moonlampBloomImage);
                break;
            case LITE_SHOOTER:
                plant = new ShooterPlant(row, col, 60, 2200L, 12, 20, p1LiteImage);
                break;
            default:
                return;
        }

        grid[row][col] = plant;
        topBar.setSunCount(topBar.getSunCount() - type.getSunCost());
    }

    private int[] getGridCell(int px, int py) {
        if (GRID_CELL_W <= 0 || GRID_CELL_H <= 0) return null;

        int col = (int) Math.floor((px - gridXOffset) / (double) GRID_CELL_W);
        int row = (int) Math.floor((py - GRID_Y_OFFSET) / (double) GRID_CELL_H);

        if (row >= 0 && row < GRID_ROWS && col >= 0 && col < GRID_COLS) {
            return new int[]{row, col};
        }
        return null;
    }

    // ---- Zombie spawning ----

    private void spawnZombieBatch(int count) {
        int panelWidth = getWidth() > 0 ? getWidth() : GameWindow.CANVAS_WIDTH;
        int dir = getZombieDirection();
        List<Integer> rows = new ArrayList<>(GRID_ROWS);
        for (int r = 0; r < GRID_ROWS; r++) {
            rows.add(r);
        }

        for (int i = 0; i < count; i++) {
            if (i % GRID_ROWS == 0) {
                Collections.shuffle(rows, rng);
            }
            int row = rows.get(i % GRID_ROWS);
            int stagger = (i / GRID_ROWS) * 28 + rng.nextInt(26);
            double startX = dir < 0 ? (panelWidth + 20 + stagger) : (-60 - stagger);
            double startY = GRID_Y_OFFSET + row * GRID_CELL_H;
            ZombieType type = gameWindow.getSettings().pickZombieType(elapsedGameMs, rng);
            zombies.add(new Zombie(row, startX, startY, dir, type, zombieImages.get(type)));
        }
    }

    // ---- Game loop update ----

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver) return;

        long now = System.currentTimeMillis();
        long deltaMs = now - lastUpdateTime;
        lastUpdateTime = now;

        // Cap delta to avoid jumps after window focus loss
        if (deltaMs > 100) deltaMs = 100;

        elapsedGameMs += deltaMs;
        if (waveBannerTimerMs > 0) {
            waveBannerTimerMs = Math.max(0, waveBannerTimerMs - deltaMs);
        }

        // ---- Zombie spawning (scales up spawn rate and batch size over time) ----
        zombieSpawnTimer += deltaMs;
        if (zombieSpawnTimer >= zombieSpawnInterval) {
            zombieSpawnTimer -= zombieSpawnInterval;
            int batchCount = gameWindow.getSettings().getSpawnBatchCount(elapsedGameMs, rng);
            spawnZombieBatch(batchCount);
            zombieSpawnInterval = gameWindow.getSettings().getSpawnIntervalMs(elapsedGameMs);
        }

        // ---- Periodic surge waves ----
        surgeTimerMs += deltaMs;
        if (surgeTimerMs >= GameSettings.SURGE_INTERVAL_MS) {
            surgeTimerMs -= GameSettings.SURGE_INTERVAL_MS;
            surgesTriggered++;
            int surgeCount = gameWindow.getSettings().getSurgeZombieCount(surgesTriggered);
            spawnZombieBatch(surgeCount);
            waveBannerTimerMs = 3000;
        }

        // ---- Update plants ----
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                if (grid[r][c] != null) {
                    grid[r][c].update(this, deltaMs);
                    if (grid[r][c].isDead()) {
                        grid[r][c] = null;
                    }
                }
            }
        }

        // ---- Update projectiles ----
        int panelWidth = getWidth() > 0 ? getWidth() : GameWindow.CANVAS_WIDTH;
        Iterator<Projectile> projIt = projectiles.iterator();
        while (projIt.hasNext()) {
            Projectile p = projIt.next();
            p.update(deltaMs);
            if (p.isOffScreen(panelWidth)) {
                projIt.remove();
                continue;
            }

            Rectangle projBounds = p.getBounds();
            boolean hit = false;
            for (Zombie z : zombies) {
                if (z.getRow() == p.getRow()) {
                    Rectangle zBounds = z.getBounds(GRID_CELL_H);
                    if (projBounds.intersects(zBounds)) {
                        z.takeDamage(p.getDamage());
                        hit = true;
                        break;
                    }
                }
            }
            if (hit) {
                projIt.remove();
            }
        }

        // ---- Update zombies ----
        int zombieDrawSize = (int) (GRID_CELL_H * 0.9);
        Iterator<Zombie> zombieIt = zombies.iterator();
        while (zombieIt.hasNext()) {
            Zombie z = zombieIt.next();
            z.update(this, deltaMs);

            if (z.isDead()) {
                zombieIt.remove();
                continue;
            }

            // Check if zombie reached the defended wall edge (game over)
            boolean reachedHouse = z.getDirection() < 0
                    ? z.getX() < gridXOffset - 40
                    : z.getX() + zombieDrawSize > gridXOffset + GRID_COLS * GRID_CELL_W + 40;
            if (reachedHouse) {
                gameOver = true;
                break;
            }
        }

        // ---- Update suns ----
        Iterator<Sun> sunIt = suns.iterator();
        while (sunIt.hasNext()) {
            Sun s = sunIt.next();
            s.update(deltaMs);
            if (s.isExpired()) {
                sunIt.remove();
            }
        }

        if (isShowing()) {
            repaint();
        }
    }

    // ---- Rendering ----

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int panelW = getWidth() > 0 ? getWidth() : GameWindow.CANVAS_WIDTH;
        int panelH = getHeight() > 0 ? getHeight() : GameWindow.CANVAS_HEIGHT;

        // Draw lawn background
        if (lawnImage != null) {
            g2d.drawImage(lawnImage, 0, 0, panelW, panelH, this);
        } else {
            g2d.setColor(new Color(34, 139, 34));
            g2d.fillRect(0, 0, panelW, panelH);
        }

        // Draw subtle grid overlay aligned to bricks when enabled in settings
        if (gameWindow.getSettings().isShowGridOverlay()) {
            g2d.setColor(new Color(255, 255, 255, 18));
            g2d.setStroke(new BasicStroke(1f));
            for (int r = 0; r <= GRID_ROWS; r++) {
                int y = GRID_Y_OFFSET + r * GRID_CELL_H;
                g2d.drawLine(gridXOffset, y, gridXOffset + GRID_COLS * GRID_CELL_W, y);
            }
            for (int c = 0; c <= GRID_COLS; c++) {
                int x = gridXOffset + c * GRID_CELL_W;
                g2d.drawLine(x, GRID_Y_OFFSET, x, GRID_Y_OFFSET + GRID_ROWS * GRID_CELL_H);
            }
        }

        // Draw plants on the grid
        boolean mirrored = lawnSide.isMirrored();
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                if (grid[r][c] != null) {
                    int x = gridXOffset + c * GRID_CELL_W;
                    int y = GRID_Y_OFFSET + r * GRID_CELL_H;
                    grid[r][c].draw(g2d, x, y, GRID_CELL_W, GRID_CELL_H, mirrored);
                }
            }
        }

        // Draw projectiles
        for (Projectile p : projectiles) {
            p.draw(g2d);
        }

        // Draw zombies
        for (Zombie z : zombies) {
            z.draw(g2d, GRID_CELL_H);
        }

        // Draw suns (on top of everything)
        for (Sun s : suns) {
            s.draw(g2d);
        }

        // Draw wave and match time badge at bottom-center (skipped in mini-preview)
        if (!renderingPreview) {
            drawWaveHud(g2d, panelW, panelH);
        }

        // Huge wave warning banner during surge
        if (waveBannerTimerMs > 0 && !gameOver) {
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(0, panelH / 2 - 26, panelW, 52);

            g2d.setColor(new Color(255, 70, 70));
            g2d.setFont(new Font("Arial", Font.BOLD, 26));
            String waveMsg = "A HUGE WAVE OF ZOMBIES IS APPROACHING!";
            FontMetrics wfm = g2d.getFontMetrics();
            g2d.drawString(waveMsg, (panelW - wfm.stringWidth(waveMsg)) / 2, panelH / 2 + wfm.getAscent() / 2 - 4);
        }

        // Game Over overlay
        if (gameOver) {
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(0, 0, panelW, panelH);

            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Arial", Font.BOLD, 60));
            String msg = "GAME OVER";
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(msg,
                    (panelW - fm.stringWidth(msg)) / 2,
                    panelH / 2 - 10);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.PLAIN, 20));
            String sub = "The zombies reached the wall!";
            fm = g2d.getFontMetrics();
            g2d.drawString(sub,
                    (panelW - fm.stringWidth(sub)) / 2,
                    panelH / 2 + 28);

            g2d.setColor(new Color(124, 252, 0));
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            String restartKeyName = GameSettings.formatKey(gameWindow.getSettings().getRestartKey());
            String hint = "Press [" + restartKeyName + "] or click RESTART to play again";
            fm = g2d.getFontMetrics();
            g2d.drawString(hint,
                    (panelW - fm.stringWidth(hint)) / 2,
                    panelH / 2 + 60);
        }

        g2d.dispose();
    }

    private void drawWaveHud(Graphics2D g2d, int panelW, int panelH) {
        int wave = gameWindow.getSettings().getWaveNumber(elapsedGameMs);
        long totalSec = elapsedGameMs / 1000L;
        String timeStr = String.format("%d:%02d", totalSec / 60, totalSec % 60);
        String text = "WAVE " + wave + "  •  " + timeStr;

        g2d.setFont(new Font("Arial", Font.BOLD, 13));
        FontMetrics fm = g2d.getFontMetrics();
        int pillW = fm.stringWidth(text) + 28;
        int pillH = 26;
        int pillX = (panelW - pillW) / 2;
        int pillY = panelH - pillH - 12;

        g2d.setColor(new Color(20, 20, 20, 200));
        g2d.fillRoundRect(pillX, pillY, pillW, pillH, 12, 12);
        g2d.setColor(new Color(124, 252, 0, 180));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(pillX, pillY, pillW, pillH, 12, 12);

        g2d.setColor(Color.WHITE);
        g2d.drawString(text, pillX + 14, pillY + ((pillH - fm.getHeight()) / 2) + fm.getAscent());
    }

    // ---- Image loading ----

    private void loadLawnImage() {
        lawnImage = loadImage(lawnSide.getClasspathPath(), lawnSide.getFilePath());

        if (lawnImage == null && lawnSide == LawnSide.MIRRORED) {
            BufferedImage normal = loadImage(LawnSide.NORMAL.getClasspathPath(), LawnSide.NORMAL.getFilePath());
            if (normal != null) {
                lawnImage = flipHorizontally(normal);
            }
        }
    }

    private void loadPlantImages() {
        p1Image = loadImage("/assets/p1.jpg", "assets/p1.jpg");
        p1LiteImage = p1Image != null ? tintImage(p1Image, new Color(95, 225, 210), 0.45f) : null;
        moonlampIdleImage = loadImage("/assets/moonlamp_idle.png", "assets/moonlamp_idle.png");
        moonlampBloomImage = loadImage("/assets/moonlamp_bloom.png", "assets/moonlamp_bloom.png");
        BufferedImage baseZombie = loadImage("/assets/zombie.jpg", "assets/zombie.jpg");
        if (baseZombie != null) {
            for (ZombieType type : ZombieType.values()) {
                zombieImages.put(type, type.getTintColor() == null
                        ? baseZombie
                        : tintImage(baseZombie, type.getTintColor(), 0.45f));
            }
        }
    }

    private BufferedImage tintImage(BufferedImage source, Color tint, float strength) {
        int width = source.getWidth();
        int height = source.getHeight();
        BufferedImage tinted = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int tr = tint.getRed();
        int tg = tint.getGreen();
        int tb = tint.getBlue();
        float inv = 1.0f - strength;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = source.getRGB(x, y);
                int a = (argb >>> 24) & 0xFF;
                int r = (argb >>> 16) & 0xFF;
                int g = (argb >>> 8) & 0xFF;
                int b = argb & 0xFF;

                // Blend original color toward multiplicative-tinted color to preserve detail
                int mr = (r * tr) / 255;
                int mg = (g * tg) / 255;
                int mb = (b * tb) / 255;
                int nr = Math.min(255, Math.round(r * inv + ((mr + tr) * 0.5f) * strength));
                int ng = Math.min(255, Math.round(g * inv + ((mg + tg) * 0.5f) * strength));
                int nb = Math.min(255, Math.round(b * inv + ((mb + tb) * 0.5f) * strength));

                tinted.setRGB(x, y, (a << 24) | (nr << 16) | (ng << 8) | nb);
            }
        }
        return tinted;
    }

    private BufferedImage loadImage(String classpathPath, String filePath) {
        try {
            try (InputStream is = getClass().getResourceAsStream(classpathPath)) {
                if (is != null) {
                    return ImageIO.read(is);
                }
            }
            File file = new File(filePath);
            if (file.exists()) {
                return ImageIO.read(file);
            }
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
        BufferedImage flipped = new BufferedImage(width, height,
                source.getType() != 0 ? source.getType() : BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = flipped.createGraphics();
        AffineTransform transform = AffineTransform.getScaleInstance(-1, 1);
        transform.translate(-width, 0);
        g2d.drawImage(source, transform, null);
        g2d.dispose();
        return flipped;
    }
}
