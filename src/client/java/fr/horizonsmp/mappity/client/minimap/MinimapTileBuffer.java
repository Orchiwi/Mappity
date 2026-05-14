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
        // Never call texture.setPixels(image) here — DynamicTexture.setPixels closes
        // the previous NativeImage before assigning the new one, which would close
        // *our* image reference if we passed it.
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

    /** Set to true to draw a fixed 5-stripe diagnostic pattern in place of real sampling.
     *  Used to disambiguate texture-pipeline bugs from sampling bugs when the minimap
     *  shows a single color in-game. When DIAGNOSTIC_PATTERN is true the player position
     *  has no effect on the rendered texture. */
    private static final boolean DIAGNOSTIC_PATTERN = true;

    private static final int[] DIAGNOSTIC_STRIPES = {
        0xFFFF0000, // red    (top)
        0xFFFFFF00, // yellow
        0xFF00FF00, // green
        0xFF00FFFF, // cyan
        0xFFFF00FF  // magenta (bottom)
    };

    private void resample(ClientLevel level, int centerX, int centerZ, float zoomScale, MinimapShape shape) {
        if (DIAGNOSTIC_PATTERN) {
            resampleDiagnostic(shape);
            return;
        }
        float half = SIZE / 2f;
        float radiusSq = half * half;
        for (int j = 0; j < SIZE; j++) {
            for (int i = 0; i < SIZE; i++) {
                if (shape == MinimapShape.CIRCLE) {
                    float dx = (i + 0.5f) - half;
                    float dy = (j + 0.5f) - half;
                    if (dx * dx + dy * dy > radiusSq) {
                        image.setPixel(i, j, 0x00000000);
                        continue;
                    }
                }
                int worldX = centerX + Math.round((i - half) / zoomScale);
                int worldZ = centerZ + Math.round((j - half) / zoomScale);
                int argb = ChunkColorSampler.sampleArgb(level, worldX, worldZ);
                if (argb == 0) {
                    image.setPixel(i, j, 0xFF1A1A1A);
                } else {
                    image.setPixel(i, j, argb);
                }
            }
        }
    }

    /** Fills the buffer with 5 horizontal stripes regardless of player position.
     *  If the in-game minimap shows these stripes the texture pipeline is correct
     *  and the bug is in the chunk sampler; if it shows a single color, the bug
     *  is in the texture upload/blit. */
    private void resampleDiagnostic(MinimapShape shape) {
        float half = SIZE / 2f;
        float radiusSq = half * half;
        int stripeHeight = SIZE / DIAGNOSTIC_STRIPES.length;
        for (int j = 0; j < SIZE; j++) {
            int stripe = Math.min(j / stripeHeight, DIAGNOSTIC_STRIPES.length - 1);
            int color = DIAGNOSTIC_STRIPES[stripe];
            for (int i = 0; i < SIZE; i++) {
                if (shape == MinimapShape.CIRCLE) {
                    float dx = (i + 0.5f) - half;
                    float dy = (j + 0.5f) - half;
                    if (dx * dx + dy * dy > radiusSq) {
                        image.setPixel(i, j, 0x00000000);
                        continue;
                    }
                }
                image.setPixel(i, j, color);
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
