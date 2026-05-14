package fr.horizonsmp.mappity.client.minimap;

import fr.horizonsmp.mappity.client.config.ModConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

/**
 * Maps an entity to an ARGB color for the minimap overlay, honoring the
 * user's visibility toggles. Returns 0 to skip an entity.
 */
public final class EntityIcon {

    public static final int COLOR_HOSTILE = 0xFFE53935;
    public static final int COLOR_PASSIVE = 0xFF4CAF50;
    public static final int COLOR_PLAYER = 0xFF42A5F5;
    public static final int COLOR_ITEM = 0xFFFFEB3B;

    private EntityIcon() {}

    public static int colorFor(Entity entity, Entity self, ModConfig config) {
        if (entity == self) return 0;
        if (entity instanceof Player) {
            return config.showOtherPlayers() ? COLOR_PLAYER : 0;
        }
        if (entity instanceof ItemEntity) {
            return config.showItems() ? COLOR_ITEM : 0;
        }
        if (!config.showEntities()) return 0;
        if (entity instanceof Enemy) {
            return COLOR_HOSTILE;
        }
        if (entity instanceof Mob) {
            return COLOR_PASSIVE;
        }
        return 0;
    }
}
