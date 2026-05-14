package fr.horizonsmp.mappity.client.config;

public enum MinimapCorner {
    TOP_LEFT("mappity.corner.top_left", false, false),
    TOP_RIGHT("mappity.corner.top_right", true, false),
    BOTTOM_LEFT("mappity.corner.bottom_left", false, true),
    BOTTOM_RIGHT("mappity.corner.bottom_right", true, true);

    private final String translationKey;
    private final boolean alignRight;
    private final boolean alignBottom;

    MinimapCorner(String translationKey, boolean alignRight, boolean alignBottom) {
        this.translationKey = translationKey;
        this.alignRight = alignRight;
        this.alignBottom = alignBottom;
    }

    public String translationKey() {
        return translationKey;
    }

    public boolean alignRight() {
        return alignRight;
    }

    public boolean alignBottom() {
        return alignBottom;
    }

    public MinimapCorner next() {
        MinimapCorner[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }
}
