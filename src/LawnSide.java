/**
 * Represents which horizontal orientation of the rooftop lawn is active.
 */
public enum LawnSide {
    NORMAL("Normal", "/assets/lawn.jpg", "assets/lawn.jpg", 1),
    MIRRORED("Mirrored", "/assets/lawn_mirrored.jpg", "assets/lawn_mirrored.jpg", -1);

    private final String displayName;
    private final String classpathPath;
    private final String filePath;
    private final int projectileDirection;

    LawnSide(String displayName, String classpathPath, String filePath, int projectileDirection) {
        this.displayName = displayName;
        this.classpathPath = classpathPath;
        this.filePath = filePath;
        this.projectileDirection = projectileDirection;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getClasspathPath() {
        return classpathPath;
    }

    public String getFilePath() {
        return filePath;
    }

    /**
     * Horizontal travel direction for plant projectiles (+1 right, -1 left).
     */
    public int getProjectileDirection() {
        return projectileDirection;
    }

    public boolean isMirrored() {
        return this == MIRRORED;
    }

    /**
     * Returns the opposite lawn orientation.
     */
    public LawnSide toggle() {
        return this == NORMAL ? MIRRORED : NORMAL;
    }
}
