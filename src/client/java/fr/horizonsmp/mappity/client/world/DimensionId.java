package fr.horizonsmp.mappity.client.world;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public final class DimensionId {

    private DimensionId() {}

    /**
     * Stable string id for a dimension, safe to use as a JSON key or
     * directory name. Format: namespace_path (e.g. "minecraft_overworld").
     */
    public static String of(ResourceKey<Level> key) {
        return key.identifier().getNamespace() + "_" + key.identifier().getPath();
    }
}
