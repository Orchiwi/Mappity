package fr.horizonsmp.mappity.client.config;

public final class ZoomLevel {

    public static final float[] LEVELS = {0.5f, 1.0f, 2.0f, 4.0f, 8.0f};
    public static final int DEFAULT_INDEX = 1;

    private ZoomLevel() {}

    public static int clamp(int index) {
        if (index < 0) return 0;
        if (index >= LEVELS.length) return LEVELS.length - 1;
        return index;
    }

    public static float scale(int index) {
        return LEVELS[clamp(index)];
    }

    public static int zoomIn(int index) {
        return clamp(index + 1);
    }

    public static int zoomOut(int index) {
        return clamp(index - 1);
    }
}
