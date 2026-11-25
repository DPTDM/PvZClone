import java.awt.event.KeyEvent;
import java.util.Random;

/**
 * Holds customizable keybindings and gameplay preferences.
 */
public class GameSettings {
    public static final long WAVE_DURATION_MS = 30000L;
    public static final long SURGE_INTERVAL_MS = 45000L;
    public enum Difficulty {
        EASY("Easy", 1.4, 15.0),
        NORMAL("Normal", 1.0, 20.0),
        HARD("Hard", 0.7, 27.0);

        private final String label;
        private final double spawnMultiplier;
        private final double zombieSpeed;

        Difficulty(String label, double spawnMultiplier, double zombieSpeed) {
            this.label = label;
            this.spawnMultiplier = spawnMultiplier;
            this.zombieSpeed = zombieSpeed;
        }

        public String getLabel() {
            return label;
        }

        public double getSpawnMultiplier() {
            return spawnMultiplier;
        }

        public double getZombieSpeed() {
            return zombieSpeed;
        }

        public Difficulty next() {
            Difficulty[] values = values();
            return values[(ordinal() + 1) % values.length];
        }
    }

    public static final int SLOT_COUNT = 6;
    private static final int[] STARTING_SUN_OPTIONS = {50, 100, 150, 200};

    private int switchLawnKey;
    private int restartKey;
    private int menuKey;
    private final int[] slotKeys = new int[SLOT_COUNT];

    private boolean showGridOverlay;
    private Difficulty difficulty;
    private int startingSun;

    public GameSettings() {
        resetToDefaults();
    }

    public void resetToDefaults() {
        switchLawnKey = KeyEvent.VK_SPACE;
        restartKey = KeyEvent.VK_R;
        menuKey = KeyEvent.VK_ESCAPE;
        for (int i = 0; i < SLOT_COUNT; i++) {
            slotKeys[i] = KeyEvent.VK_1 + i;
        }
        showGridOverlay = true;
        difficulty = Difficulty.NORMAL;
        startingSun = 50;
    }

    public int getSwitchLawnKey() {
        return switchLawnKey;
    }

    public void setSwitchLawnKey(int switchLawnKey) {
        this.switchLawnKey = switchLawnKey;
    }

    public int getRestartKey() {
        return restartKey;
    }

    public void setRestartKey(int restartKey) {
        this.restartKey = restartKey;
    }

    public int getMenuKey() {
        return menuKey;
    }

    public void setMenuKey(int menuKey) {
        this.menuKey = menuKey;
    }

    public int getSlotKey(int index) {
        return slotKeys[index];
    }

    public void setSlotKey(int index, int keyCode) {
        if (index >= 0 && index < SLOT_COUNT) {
            slotKeys[index] = keyCode;
        }
    }

    public boolean isShowGridOverlay() {
        return showGridOverlay;
    }

    public void setShowGridOverlay(boolean showGridOverlay) {
        this.showGridOverlay = showGridOverlay;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public void cycleDifficulty() {
        this.difficulty = difficulty.next();
    }

    public int getStartingSun() {
        return startingSun;
    }

    public void cycleStartingSun() {
        for (int i = 0; i < STARTING_SUN_OPTIONS.length; i++) {
            if (STARTING_SUN_OPTIONS[i] == startingSun) {
                startingSun = STARTING_SUN_OPTIONS[(i + 1) % STARTING_SUN_OPTIONS.length];
                return;
            }
        }
        startingSun = STARTING_SUN_OPTIONS[0];
    }

    public int getWaveNumber(long elapsedMs) {
        return 1 + (int) (Math.max(0L, elapsedMs) / WAVE_DURATION_MS);
    }

    /**
     * Computes delay between zombie spawns as match time progresses.
     * Difficulty scales purely by spawning more zombies faster (no HP inflation).
     */
    public long getSpawnIntervalMs(long elapsedMs) {
        double minutes = Math.max(0L, elapsedMs) / 60000.0;
        long base = Math.max(1800L, Math.round(10000.0 - minutes * 3400.0));
        return Math.max(1200L, Math.round(base * difficulty.getSpawnMultiplier()));
    }

    /**
     * Returns how many zombies spawn on a regular spawn tick at the given match time.
     */
    public int getSpawnBatchCount(long elapsedMs, Random rng) {
        double seconds = Math.max(0L, elapsedMs) / 1000.0;
        if (seconds < 25.0) {
            return 1;
        }
        int minCount = Math.min(4, 1 + (int) (seconds / 60.0));
        int maxCount = Math.min(5, 2 + (int) (seconds / 45.0));

        if (difficulty == Difficulty.EASY) {
            maxCount = Math.max(1, maxCount - 1);
            minCount = Math.min(minCount, maxCount);
        } else if (difficulty == Difficulty.HARD) {
            maxCount = Math.min(6, maxCount + 1);
        }

        if (maxCount <= minCount) {
            return minCount;
        }
        return minCount + rng.nextInt(maxCount - minCount + 1);
    }

    /**
     * Returns the number of standard zombies spawned during a periodic surge wave.
     */
    public int getSurgeZombieCount(int surgeNumber) {
        int base = Math.min(8, 2 + Math.max(1, surgeNumber));
        if (difficulty == Difficulty.EASY) {
            return Math.max(2, base - 1);
        }
        if (difficulty == Difficulty.HARD) {
            return Math.min(10, base + 1);
        }
        return base;
    }

    /**
     * Picks a zombie variant appropriate for the current match elapsed time.
     */
    public ZombieType pickZombieType(long elapsedMs, Random rng) {
        double effectiveSec = Math.max(0L, elapsedMs) / 1000.0;
        if (difficulty == Difficulty.EASY) {
            effectiveSec = Math.max(0.0, effectiveSec - 15.0);
        } else if (difficulty == Difficulty.HARD) {
            effectiveSec += 15.0;
        }

        if (effectiveSec < 25.0) {
            return ZombieType.NORMAL;
        }

        int roll = rng.nextInt(100);
        if (effectiveSec < 55.0) {
            return roll < 65 ? ZombieType.NORMAL : ZombieType.RUNNER;
        }
        if (effectiveSec < 85.0) {
            if (roll < 45) return ZombieType.NORMAL;
            if (roll < 75) return ZombieType.RUNNER;
            return ZombieType.CHOMPER;
        }

        if (roll < 35) return ZombieType.NORMAL;
        if (roll < 60) return ZombieType.RUNNER;
        if (roll < 80) return ZombieType.CHOMPER;
        return ZombieType.VAULTER;
    }

    /**
     * Checks whether a keyCode is already assigned to another action, returning
     * that action's display name, or null if unused.
     */
    public String findConflictingAction(int keyCode, String targetActionId) {
        if (!"SWITCH_LAWN".equals(targetActionId) && switchLawnKey == keyCode) {
            return "Switch Lawn";
        }
        if (!"RESTART".equals(targetActionId) && restartKey == keyCode) {
            return "Restart Game";
        }
        if (!"MENU".equals(targetActionId) && menuKey == keyCode) {
            return "Back to Menu";
        }
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (!("SLOT_" + i).equals(targetActionId) && slotKeys[i] == keyCode) {
                return "Seed Slot " + (i + 1);
            }
        }
        return null;
    }

    public static String formatKey(int keyCode) {
        return KeyEvent.getKeyText(keyCode).toUpperCase();
    }
}
