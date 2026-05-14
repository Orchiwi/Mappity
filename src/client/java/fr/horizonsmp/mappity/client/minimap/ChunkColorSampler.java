package fr.horizonsmp.mappity.client.minimap;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;

/**
 * Samples the top-of-world block color at integer (worldX, worldZ) using
 * the WORLD_SURFACE heightmap. Returns 0 (transparent) for unloaded chunks.
 */
public final class ChunkColorSampler {

    private static final int TRANSPARENT = 0x00000000;

    private ChunkColorSampler() {}

    /** Returns an ARGB color for the surface at (worldX, worldZ), or 0 if unloaded. */
    public static int sampleArgb(ClientLevel level, int worldX, int worldZ) {
        if (level == null) return TRANSPARENT;
        int chunkX = worldX >> 4;
        int chunkZ = worldZ >> 4;
        LevelChunk chunk = level.getChunkSource().getChunk(chunkX, chunkZ, false);
        if (chunk == null) return TRANSPARENT;
        int localX = worldX & 15;
        int localZ = worldZ & 15;
        int y = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, localX, localZ);
        BlockPos pos = new BlockPos(worldX, y - 1, worldZ);
        BlockState state = chunk.getBlockState(pos);
        MapColor color = state.getMapColor(level, pos);
        if (color == MapColor.NONE) return TRANSPARENT;
        return color.calculateARGBColor(MapColor.Brightness.NORMAL);
    }

    /** Converts an ARGB int to ABGR (the byte order NativeImage expects). */
    public static int argbToAbgr(int argb) {
        int a = (argb >>> 24) & 0xFF;
        int r = (argb >>> 16) & 0xFF;
        int g = (argb >>> 8) & 0xFF;
        int b = argb & 0xFF;
        return (a << 24) | (b << 16) | (g << 8) | r;
    }
}
