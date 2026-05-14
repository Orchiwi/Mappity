package fr.horizonsmp.mappity.client.waypoint;

public final class WaypointPalette {

    public static final int[] COLORS = {
        0xFFFF5555, // red
        0xFFFFAA00, // orange
        0xFFFFFF55, // yellow
        0xFF55FF55, // green
        0xFF55FFFF, // cyan
        0xFF5555FF, // blue
        0xFFAA00FF, // purple
        0xFFFF55FF, // pink
        0xFFFFFFFF, // white
        0xFFAAAAAA, // light gray
        0xFF555555, // dark gray
        0xFF000000  // black
    };

    public static final int DEFAULT_COLOR = COLORS[3]; // green

    private WaypointPalette() {}

    public static int cycle(int currentColor) {
        for (int i = 0; i < COLORS.length; i++) {
            if (COLORS[i] == currentColor) {
                return COLORS[(i + 1) % COLORS.length];
            }
        }
        return COLORS[0];
    }
}
