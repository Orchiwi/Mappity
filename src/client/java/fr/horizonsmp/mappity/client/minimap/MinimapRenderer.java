package fr.horizonsmp.mappity.client.minimap;

import fr.horizonsmp.mappity.client.ClientState;
import fr.horizonsmp.mappity.client.config.MinimapCorner;
import fr.horizonsmp.mappity.client.config.MinimapShape;
import fr.horizonsmp.mappity.client.config.ModConfig;
import fr.horizonsmp.mappity.client.waypoint.Waypoint;
import fr.horizonsmp.mappity.client.world.DimensionId;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

/**
 * Stateless renderer that draws the minimap into the HUD on each frame.
 * Reads live config + waypoints from {@link ClientState}; owns the tile
 * buffer for the texture upload.
 */
public final class MinimapRenderer {

    private static final int RENDER_SIZE_PX = 96;
    private static final int MARGIN_PX = 8;
    private static final int BORDER_COLOR = 0xFF202020;
    private static final int BACKGROUND_COLOR = 0xCC000000;
    private static final int PLAYER_DOT_COLOR = 0xFFFFFFFF;
    private static final int PLAYER_DOT_OUTLINE = 0xFF000000;
    private static final int NORTH_COLOR = 0xFFFFFFFF;

    private final MinimapTileBuffer tileBuffer = new MinimapTileBuffer();

    public void render(GuiGraphicsExtractor gfx) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        LocalPlayer player = mc.player;
        if (level == null || player == null) return;
        if (mc.options.hideGui) return;

        ModConfig config = ClientState.get().config();
        if (!config.minimapEnabled()) return;

        int screenW = gfx.guiWidth();
        int screenH = gfx.guiHeight();
        int size = RENDER_SIZE_PX;
        int originX = config.corner().alignRight() ? screenW - size - MARGIN_PX : MARGIN_PX;
        int originY = config.corner().alignBottom() ? screenH - size - MARGIN_PX : MARGIN_PX;

        int centerX = (int) Math.floor(player.getX());
        int centerZ = (int) Math.floor(player.getZ());

        // Refresh tile buffer
        tileBuffer.updateIfNeeded(level, centerX, centerZ, config.zoomScale(), config.shape());

        // Background + tile blit
        gfx.fill(originX - 1, originY - 1, originX + size + 1, originY + size + 1, BORDER_COLOR);
        gfx.fill(originX, originY, originX + size, originY + size, BACKGROUND_COLOR);
        gfx.blit(tileBuffer.textureId(), originX, originY, size, size, 0f, 0f, 1f, 1f);

        // Entity overlay
        if (config.showEntities() || config.showItems() || config.showOtherPlayers()) {
            renderEntities(gfx, level, player, config, originX, originY, size, centerX, centerZ);
        }

        // Waypoints
        if (config.showWaypoints()) {
            renderWaypoints(gfx, level, config, originX, originY, size, centerX, centerZ);
        }

        // Player dot (center) — small white square with black outline
        int cx = originX + size / 2;
        int cy = originY + size / 2;
        gfx.fill(cx - 2, cy - 2, cx + 2, cy + 2, PLAYER_DOT_OUTLINE);
        gfx.fill(cx - 1, cy - 1, cx + 1, cy + 1, PLAYER_DOT_COLOR);

        // North indicator
        Font font = mc.font;
        if (font != null) {
            gfx.text(font, Component.literal("N"), originX + size / 2 - 3, originY + 2, NORTH_COLOR, true);
        }
    }

    private void renderEntities(GuiGraphicsExtractor gfx, ClientLevel level, LocalPlayer self,
                                ModConfig config, int originX, int originY, int size,
                                int centerX, int centerZ) {
        float scale = config.zoomScale();
        int half = size / 2;
        float visibleRadiusBlocks = half / scale;
        float radSq = visibleRadiusBlocks * visibleRadiusBlocks;
        boolean circle = config.shape() == MinimapShape.CIRCLE;
        float pxRadSq = (half) * (half);

        for (Entity entity : level.entitiesForRendering()) {
            int color = EntityIcon.colorFor(entity, self, config);
            if (color == 0) continue;
            double dx = entity.getX() - centerX;
            double dz = entity.getZ() - centerZ;
            if (dx * dx + dz * dz > radSq) continue;
            int px = originX + half + (int) Math.round(dx * scale);
            int py = originY + half + (int) Math.round(dz * scale);
            if (circle) {
                float relX = px - (originX + half);
                float relY = py - (originY + half);
                if (relX * relX + relY * relY > pxRadSq) continue;
            }
            gfx.fill(px - 1, py - 1, px + 2, py + 2, 0xFF000000);
            gfx.fill(px, py, px + 1, py + 1, color);
        }
    }

    private void renderWaypoints(GuiGraphicsExtractor gfx, ClientLevel level,
                                 ModConfig config, int originX, int originY, int size,
                                 int centerX, int centerZ) {
        String dim = DimensionId.of(level.dimension());
        List<Waypoint> waypoints = ClientState.get().waypoints().get(dim);
        float scale = config.zoomScale();
        int half = size / 2;
        boolean circle = config.shape() == MinimapShape.CIRCLE;
        float pxRadSq = half * half;

        for (Waypoint wp : waypoints) {
            if (!wp.visible()) continue;
            double dx = wp.x() - centerX;
            double dz = wp.z() - centerZ;
            int px = originX + half + (int) Math.round(dx * scale);
            int py = originY + half + (int) Math.round(dz * scale);
            // Clamp inside if outside the buffer to keep marker visible at edge
            int minX = originX + 2;
            int minY = originY + 2;
            int maxX = originX + size - 3;
            int maxY = originY + size - 3;
            if (px < minX || px > maxX || py < minY || py > maxY) {
                px = Math.max(minX, Math.min(maxX, px));
                py = Math.max(minY, Math.min(maxY, py));
                if (circle) {
                    float relX = px - (originX + half);
                    float relY = py - (originY + half);
                    float d2 = relX * relX + relY * relY;
                    if (d2 > pxRadSq * 0.85f) {
                        float r = (float) Math.sqrt(pxRadSq * 0.85f);
                        float angle = (float) Math.atan2(relY, relX);
                        px = originX + half + (int) Math.round(Math.cos(angle) * r);
                        py = originY + half + (int) Math.round(Math.sin(angle) * r);
                    }
                }
            } else if (circle) {
                float relX = px - (originX + half);
                float relY = py - (originY + half);
                if (relX * relX + relY * relY > pxRadSq) continue;
            }
            gfx.fill(px - 2, py - 2, px + 3, py + 3, 0xFF000000);
            gfx.fill(px - 1, py - 1, px + 2, py + 2, wp.colorRgb());
        }
    }

    public void invalidateTileBuffer() {
        tileBuffer.forceInvalidate();
    }
}
