package fr.horizonsmp.mappity.client.minimap;

import com.mojang.blaze3d.platform.NativeImage;
import fr.horizonsmp.mappity.client.config.MinimapShape;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

/**
 * Owns the in-memory minimap pixel buffer + GPU texture. Re-samples when
 * the player moves enough or zoom/shape changes. Single-threaded; called
 * from the render thread.
 */
public final class MinimapTileBuffer implements AutoCloseable {

    public static final int SIZE = 128;
    public static final Identifier TEXTURE_ID = Identifier.fromNamespaceAndPath("mappity", "minimap_tile");

    private NativeImage image;
    private DynamicTexture texture;
    private boolean ready = false;

    private int lastCenterX = Integer.MIN_VALUE;
    private int lastCenterZ = Integer.MIN_VALUE;
    private float lastScale = -1f;
    private MinimapShape lastShape = null;

    public MinimapTileBuffer() {
        // GPU resources (NativeImage backing + DynamicTexture) are created
        // lazily on first render — at onInitializeClient time the
        // RenderSystem device isn't ready yet.
    }

    public Identifier textureId() {
        return TEXTURE_ID;
    }

    /** Re-samples the buffer if the player has moved enough or zoom/shape changed. */
    public void updateIfNeeded(ClientLevel level, int centerX, int centerZ, float zoomScale, MinimapShape shape) {
        if (!ensureReady()) return;
        boolean scaleChanged = Math.abs(zoomScale - lastScale) > 1e-6f;
        boolean shapeChanged = shape != lastShape;
        boolean moved = centerX != lastCenterX || centerZ != lastCenterZ;
        if (!scaleChanged && !shapeChanged && !moved) {
            return;
        }
        resample(level, centerX, centerZ, zoomScale, shape);
        lastCenterX = centerX;
        lastCenterZ = centerZ;
        lastScale = zoomScale;
        lastShape = shape;
        texture.upload();
    }

    private boolean ensureReady() {
        if (ready) return true;
        try {
            image = new NativeImage(SIZE, SIZE, true);
            texture = new DynamicTexture(() -> "mappity-minimap", image);
            Minecraft.getInstance().getTextureManager().register(TEXTURE_ID, texture);
            ready = true;
            return true;
        } catch (RuntimeException ex) {
            // RenderSystem not ready yet — try again next frame.
            return false;
        }
    }

    private void resample(ClientLevel level, int centerX, int centerZ, float zoomScale, MinimapShape shape) {
        float half = SIZE / 2f;
        float radiusSq = half * half;
        for (int j = 0; j < SIZE; j++) {
            for (int i = 0; i < SIZE; i++) {
                if (shape == MinimapShape.CIRCLE) {
                    float dx = (i + 0.5f) - half;
                    float dy = (j + 0.5f) - half;
                    if (dx * dx + dy * dy > radiusSq) {
                        image.setPixelABGR(i, j, 0x00000000);
                        continue;
                    }
                }
                int worldX = centerX + Math.round((i - half) / zoomScale);
                int worldZ = centerZ + Math.round((j - half) / zoomScale);
                int argb = ChunkColorSampler.sampleArgb(level, worldX, worldZ);
                if (argb == 0) {
                    // Unloaded — dark gray, semi-opaque
                    image.setPixelABGR(i, j, 0xFF1A1A1A);
                } else {
                    image.setPixelABGR(i, j, ChunkColorSampler.argbToAbgr(argb));
                }
            }
        }
    }

    public void forceInvalidate() {
        lastCenterX = Integer.MIN_VALUE;
        lastCenterZ = Integer.MIN_VALUE;
        lastScale = -1f;
        lastShape = null;
    }

    @Override
    public void close() {
        if (!ready) return;
        Minecraft.getInstance().getTextureManager().release(TEXTURE_ID);
        if (texture != null) texture.close();
        ready = false;
    }
}
