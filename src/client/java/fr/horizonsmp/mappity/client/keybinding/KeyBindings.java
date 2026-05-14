package fr.horizonsmp.mappity.client.keybinding;

import com.mojang.blaze3d.platform.InputConstants;
import fr.horizonsmp.mappity.client.ClientState;
import fr.horizonsmp.mappity.client.config.MinimapCorner;
import fr.horizonsmp.mappity.client.config.MinimapShape;
import fr.horizonsmp.mappity.client.config.ModConfig;
import fr.horizonsmp.mappity.client.config.ZoomLevel;
import fr.horizonsmp.mappity.client.screen.MinimapSettingsScreen;
import fr.horizonsmp.mappity.client.screen.WaypointEditScreen;
import fr.horizonsmp.mappity.client.screen.WaypointManagerScreen;
import fr.horizonsmp.mappity.client.waypoint.Waypoint;
import fr.horizonsmp.mappity.client.waypoint.WaypointPalette;
import fr.horizonsmp.mappity.client.world.DimensionId;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class KeyBindings {

    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
        Identifier.fromNamespaceAndPath("mappity", "general")
    );

    public static final KeyMapping ADD_WAYPOINT = bind("key.mappity.waypoint.add", GLFW.GLFW_KEY_B);
    public static final KeyMapping OPEN_MANAGER = bind("key.mappity.waypoint.manager", GLFW.GLFW_KEY_J);
    public static final KeyMapping OPEN_SETTINGS = bind("key.mappity.settings", GLFW.GLFW_KEY_M);
    public static final KeyMapping ZOOM_IN = bind("key.mappity.zoom.in", GLFW.GLFW_KEY_EQUAL);
    public static final KeyMapping ZOOM_OUT = bind("key.mappity.zoom.out", GLFW.GLFW_KEY_MINUS);
    public static final KeyMapping CYCLE_SHAPE = bind("key.mappity.shape.cycle", InputConstants.UNKNOWN.getValue());
    public static final KeyMapping CYCLE_CORNER = bind("key.mappity.corner.cycle", InputConstants.UNKNOWN.getValue());
    public static final KeyMapping TOGGLE_MINIMAP = bind("key.mappity.toggle", InputConstants.UNKNOWN.getValue());

    private KeyBindings() {}

    private static KeyMapping bind(String translationKey, int defaultKey) {
        return new KeyMapping(translationKey, InputConstants.Type.KEYSYM, defaultKey, CATEGORY);
    }

    public static void register() {
        KeyMappingHelper.registerKeyMapping(ADD_WAYPOINT);
        KeyMappingHelper.registerKeyMapping(OPEN_MANAGER);
        KeyMappingHelper.registerKeyMapping(OPEN_SETTINGS);
        KeyMappingHelper.registerKeyMapping(ZOOM_IN);
        KeyMappingHelper.registerKeyMapping(ZOOM_OUT);
        KeyMappingHelper.registerKeyMapping(CYCLE_SHAPE);
        KeyMappingHelper.registerKeyMapping(CYCLE_CORNER);
        KeyMappingHelper.registerKeyMapping(TOGGLE_MINIMAP);

        ClientTickEvents.END_CLIENT_TICK.register(KeyBindings::tick);
    }

    private static void tick(Minecraft mc) {
        if (mc.screen != null) return;
        while (ADD_WAYPOINT.consumeClick()) handleAddWaypoint(mc);
        while (OPEN_MANAGER.consumeClick()) handleOpenManager(mc);
        while (OPEN_SETTINGS.consumeClick()) handleOpenSettings(mc);
        while (ZOOM_IN.consumeClick()) handleZoomIn();
        while (ZOOM_OUT.consumeClick()) handleZoomOut();
        while (CYCLE_SHAPE.consumeClick()) handleCycleShape();
        while (CYCLE_CORNER.consumeClick()) handleCycleCorner();
        while (TOGGLE_MINIMAP.consumeClick()) handleToggleMinimap();
    }

    private static void handleAddWaypoint(Minecraft mc) {
        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;
        if (player == null || level == null) return;
        ClientState state = ClientState.get();
        state.ensureWaypointsLoadedForCurrentWorld();
        String dim = DimensionId.of(level.dimension());
        int n = state.waypoints().get(dim).size() + 1;
        Waypoint wp = Waypoint.of(
            "WP " + n,
            (int) Math.floor(player.getX()),
            (int) Math.floor(player.getY()),
            (int) Math.floor(player.getZ()),
            WaypointPalette.DEFAULT_COLOR
        );
        state.waypoints().add(dim, wp);
        state.persistWaypoints();
    }

    private static void handleOpenManager(Minecraft mc) {
        ClientState.get().ensureWaypointsLoadedForCurrentWorld();
        mc.setScreen(new WaypointManagerScreen());
    }

    private static void handleOpenSettings(Minecraft mc) {
        mc.setScreen(new MinimapSettingsScreen());
    }

    private static void handleZoomIn() {
        ClientState.get().updateConfig(c -> c.withZoomIndex(ZoomLevel.zoomIn(c.zoomIndex())));
    }

    private static void handleZoomOut() {
        ClientState.get().updateConfig(c -> c.withZoomIndex(ZoomLevel.zoomOut(c.zoomIndex())));
    }

    private static void handleCycleShape() {
        ClientState.get().updateConfig(c -> c.withShape(c.shape().next()));
    }

    private static void handleCycleCorner() {
        ClientState.get().updateConfig(c -> c.withCorner(c.corner().next()));
    }

    private static void handleToggleMinimap() {
        ClientState.get().updateConfig(c -> c.withMinimapEnabled(!c.minimapEnabled()));
    }
}
