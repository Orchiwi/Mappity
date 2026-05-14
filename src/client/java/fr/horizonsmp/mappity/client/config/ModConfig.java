package fr.horizonsmp.mappity.client.config;

public record ModConfig(
    boolean minimapEnabled,
    MinimapShape shape,
    MinimapCorner corner,
    int zoomIndex,
    boolean showEntities,
    boolean showItems,
    boolean showOtherPlayers,
    boolean showWaypoints
) {

    public static ModConfig defaults() {
        return new ModConfig(
            true,
            MinimapShape.SQUARE,
            MinimapCorner.TOP_RIGHT,
            ZoomLevel.DEFAULT_INDEX,
            true,
            true,
            true,
            true
        );
    }

    public ModConfig withMinimapEnabled(boolean v) {
        return new ModConfig(v, shape, corner, zoomIndex, showEntities, showItems, showOtherPlayers, showWaypoints);
    }

    public ModConfig withShape(MinimapShape v) {
        return new ModConfig(minimapEnabled, v, corner, zoomIndex, showEntities, showItems, showOtherPlayers, showWaypoints);
    }

    public ModConfig withCorner(MinimapCorner v) {
        return new ModConfig(minimapEnabled, shape, v, zoomIndex, showEntities, showItems, showOtherPlayers, showWaypoints);
    }

    public ModConfig withZoomIndex(int v) {
        return new ModConfig(minimapEnabled, shape, corner, ZoomLevel.clamp(v), showEntities, showItems, showOtherPlayers, showWaypoints);
    }

    public ModConfig withShowEntities(boolean v) {
        return new ModConfig(minimapEnabled, shape, corner, zoomIndex, v, showItems, showOtherPlayers, showWaypoints);
    }

    public ModConfig withShowItems(boolean v) {
        return new ModConfig(minimapEnabled, shape, corner, zoomIndex, showEntities, v, showOtherPlayers, showWaypoints);
    }

    public ModConfig withShowOtherPlayers(boolean v) {
        return new ModConfig(minimapEnabled, shape, corner, zoomIndex, showEntities, showItems, v, showWaypoints);
    }

    public ModConfig withShowWaypoints(boolean v) {
        return new ModConfig(minimapEnabled, shape, corner, zoomIndex, showEntities, showItems, showOtherPlayers, v);
    }

    public float zoomScale() {
        return ZoomLevel.scale(zoomIndex);
    }
}
