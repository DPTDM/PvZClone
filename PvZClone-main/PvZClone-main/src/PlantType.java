public enum PlantType {
    SHOOTER("P1", 100, "p1.jpg"),
    SUNFLOWER("P2", 50, "p2.jpg");

    private final String displayName;
    private final int sunCost;
    private final String imageFile;

    PlantType(String displayName, int sunCost, String imageFile) {
        this.displayName = displayName;
        this.sunCost = sunCost;
        this.imageFile = imageFile;
    }

    public String getDisplayName() { return displayName; }
    public int getSunCost() { return sunCost; }
    public String getImageFile() { return imageFile; }
}
