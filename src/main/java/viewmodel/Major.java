package viewmodel;

public enum Major {
    CS("Computer Science"),
    CPIS("Computer Information Systems"),
    ENGLISH("English"),
    MATHEMATICS("Mathematics"),
    PHYSICS("Physics");

    private final String displayName;

    Major(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static Major fromString(String text) {
        for (Major major : Major.values()) {
            if (major.displayName.equalsIgnoreCase(text)) {
                return major;
            }
        }
        throw new IllegalArgumentException("No major found for: " + text);
    }
}