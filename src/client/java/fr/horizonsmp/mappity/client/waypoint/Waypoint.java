package fr.horizonsmp.mappity.client.waypoint;

import java.util.UUID;

public record Waypoint(
    UUID id,
    String name,
    int x,
    int y,
    int z,
    int colorRgb,
    boolean visible
) {

    public static Waypoint of(String name, int x, int y, int z, int colorRgb) {
        return new Waypoint(UUID.randomUUID(), name, x, y, z, colorRgb, true);
    }

    public Waypoint withName(String v) {
        return new Waypoint(id, v, x, y, z, colorRgb, visible);
    }

    public Waypoint withCoords(int nx, int ny, int nz) {
        return new Waypoint(id, name, nx, ny, nz, colorRgb, visible);
    }

    public Waypoint withColor(int v) {
        return new Waypoint(id, name, x, y, z, v, visible);
    }

    public Waypoint withVisible(boolean v) {
        return new Waypoint(id, name, x, y, z, colorRgb, v);
    }
}
