import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GamePanel extends JPanel implements ActionListener {
    // ---- Grid constants aligned to the lawn brick pattern ----
    // The stone bricks in the lawn image occupy roughly:
    //   X: 75 to 820   (745 px wide, ~9 columns → ~83 px each)
    //   Y: 150 to 530   (380 px tall, ~5 rows   → ~76 px each)
    public static final int GRID_ROWS = 5;
    public static final int GRID_COLS = 9;
    public static final int GRID_X_OFFSET = 75;
    public static final int GRID_Y_OFFSET = 150;
    public static final int GRID_CELL_W = 83;
    public static final int GRID_CELL_H = 76;

    private final GameWindow gameWindow;
    private final TopBar topBar;
    private BufferedImage lawnImage;

    // Game entities
    private final Plant[][] grid = new Plant[GRID_ROWS][GRID_COLS];
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<Sun> suns = new ArrayList<>();
    private final List<Zombie> zombies = new ArrayList<>();

    // Game loop
    private final Timer gameTimer;
    private long lastUpdateTime;

    // Zombie spawning
    private long zombieSpawnTimer = 0;
    private long zombieSpawnInterval = 10000; // first zombie at 10 s
    private int zombiesSpawned = 0;

    // Plant images
    private BufferedImage p1Image;
    private BufferedImage p2Image;
    private BufferedImage zombieImage;

    // Game state
    private boolean gameOver = false;

    public GamePanel(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        setLayout(new BorderLayout());
        loadLawnImage();
        loadPlantImages();

        // Top bar HUD with sun counter, seed slots, shovel, and menu button
        this.topBar = new TopBar(gameWindow, 6);
        add(topBar, BorderLayout.NORTH);

        // Assign plant types to the first 2 seed slots
        topBar.assignPlantType(0, PlantType.SHOOTER, p1Image);
        topBar.assignPlantType(1, PlantType.SUNFLOWER, p2Image);
        topBar.updateAffordability();

        // Mouse listener for placing plants and collecting suns
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });

        // Game loop at ~60fps (started when game screen becomes active)
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

    public TopBar getTopBar() {
        return topBar;
    }

    // ---- Grid helpers (fixed cell sizes) ----

    public int getCellWidth() {
        return GRID_CELL_W;
    }

    public int getCellHeight() {
        return GRID_CELL_H;
    }

    public int[] getCellPosition(int row, int col) {
        return new int[]{GRID_X_OFFSET + col * GRID_CELL_W,
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

        // 2) Check if placing a plant on the grid
        int selectedSlot = topBar.getSelectedSlotIndex();
        if (selectedSlot < 0) return;

        PlantType type = topBar.getPlantType(selectedSlot);
        if (type == null) return;

        // Check sun cost
        if (topBar.getSunCount() < type.getSunCost()) return;

        // Check grid bounds
        int[] cell = getGridCell(px, py);
        if (cell == null) return;

        int row = cell[0], col = cell[1];
        if (grid[row][col] != null) return; // already occupied

        // Place plant
        Plant plant;
        switch (type) {
            case SHOOTER:
                plant = new ShooterPlant(row, col, p1Image);
                break;
            case SUNFLOWER:
                plant = new SunFlowerPlant(row, col, p2Image);
                break;
            default:
                return;
        }

        grid[row][col] = plant;
        topBar.setSunCount(topBar.getSunCount() - type.getSunCost());
    }

    private int[] getGridCell(int px, int py) {
        if (GRID_CELL_W <= 0 || GRID_CELL_H <= 0) return null;

        int col = (px - GRID_X_OFFSET) / GRID_CELL_W;
        int row = (py - GRID_Y_OFFSET) / GRID_CELL_H;

        if (row >= 0 && row < GRID_ROWS && col >= 0 && col < GRID_COLS) {
            return new int[]{row, col};
        }
        return null;
    }

    // ---- Zombie spawning ----

    private void spawnZombie() {
        int row = (int) (Math.random() * GRID_ROWS);
        int panelWidth = getWidth() > 0 ? getWidth() : GameWindow.CANVAS_WIDTH;
        double startX = panelWidth + 20; // just off-screen right
        double startY = GRID_Y_OFFSET + row * GRID_CELL_H;
        zombies.add(new Zombie(row, startX, startY, zombieImage));
        zombiesSpawned++;

        // Speed up spawning over time (minimum 4 seconds between spawns)
        zombieSpawnInterval = Math.max(4000, 10000 - zombiesSpawned * 500L);
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

        // ---- Zombie spawning ----
        zombieSpawnTimer += deltaMs;
        if (zombieSpawnTimer >= zombieSpawnInterval) {
            zombieSpawnTimer -= zombieSpawnInterval;
            spawnZombie();
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

            // Check collision with zombies
            Rectangle projBounds = p.getBounds();
            boolean hit = false;
            for (Zombie z : zombies) {
                if (z.getRow() == p.getRow()) {
                    Rectangle zBounds = z.getBounds(GRID_CELL_H);
                    if (projBounds.intersects(zBounds)) {
                        z.takeDamage(25);
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
        Iterator<Zombie> zombieIt = zombies.iterator();
        while (zombieIt.hasNext()) {
            Zombie z = zombieIt.next();
            z.update(this, deltaMs);

            if (z.isDead()) {
                zombieIt.remove();
                continue;
            }

            // Check if zombie reached the left edge (game over)
            if (z.getX() < GRID_X_OFFSET - 40) {
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

        repaint();
    }

    // ---- Rendering ----

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw lawn background
        if (lawnImage != null) {
            g2d.drawImage(lawnImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g2d.setColor(new Color(34, 139, 34));
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        // Draw subtle grid overlay aligned to bricks
        g2d.setColor(new Color(255, 255, 255, 18));
        g2d.setStroke(new BasicStroke(1f));
        for (int r = 0; r <= GRID_ROWS; r++) {
            int y = GRID_Y_OFFSET + r * GRID_CELL_H;
            g2d.drawLine(GRID_X_OFFSET, y, GRID_X_OFFSET + GRID_COLS * GRID_CELL_W, y);
        }
        for (int c = 0; c <= GRID_COLS; c++) {
            int x = GRID_X_OFFSET + c * GRID_CELL_W;
            g2d.drawLine(x, GRID_Y_OFFSET, x, GRID_Y_OFFSET + GRID_ROWS * GRID_CELL_H);
        }

        // Draw plants on the grid
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                if (grid[r][c] != null) {
                    int x = GRID_X_OFFSET + c * GRID_CELL_W;
                    int y = GRID_Y_OFFSET + r * GRID_CELL_H;
                    grid[r][c].draw(g2d, x, y, GRID_CELL_W, GRID_CELL_H);
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

        // Game Over overlay
        if (gameOver) {
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(0, 0, getWidth(), getHeight());

            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Arial", Font.BOLD, 60));
            String msg = "GAME OVER";
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(msg,
                    (getWidth() - fm.stringWidth(msg)) / 2,
                    getHeight() / 2);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.PLAIN, 20));
            String sub = "The zombies reached the wall!";
            fm = g2d.getFontMetrics();
            g2d.drawString(sub,
                    (getWidth() - fm.stringWidth(sub)) / 2,
                    getHeight() / 2 + 40);
        }

        g2d.dispose();
    }

    // ---- Image loading ----

    private void loadLawnImage() {
        lawnImage = loadImage("lawn.jpg");
    }

    private void loadPlantImages() {
        p1Image = loadImage("p1.jpg");
        p2Image = loadImage("p2.jpg");
        zombieImage = loadImage("zombie.jpg");
    }

    private BufferedImage loadImage(String name) {
        try {
            try (InputStream is = getClass().getResourceAsStream("/assets/" + name)) {
                if (is != null) return ImageIO.read(is);
            }
            File file = new File("assets/" + name);
            if (file.exists()) return ImageIO.read(file);
            file = new File("src/assets/" + name);
            if (file.exists()) return ImageIO.read(file);
        } catch (Exception e) {
            System.err.println("Failed to load image " + name + ": " + e.getMessage());
        }
        return null;
    }
}
