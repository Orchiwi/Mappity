package fr.horizonsmp.mappity.client;

import fr.horizonsmp.mappity.client.keybinding.KeyBindings;
import fr.horizonsmp.mappity.client.minimap.MinimapHudElement;
import fr.horizonsmp.mappity.client.minimap.MinimapRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public final class MappityClient implements ClientModInitializer {

    private static final Identifier HUD_MINIMAP = Identifier.fromNamespaceAndPath("mappity", "minimap");

    @Override
    public void onInitializeClient() {
        ClientState.init();
        KeyBindings.register();

        MinimapRenderer renderer = new MinimapRenderer();
        HudElementRegistry.addLast(HUD_MINIMAP, new MinimapHudElement(renderer));

        // On entering a world, ensure waypoints for that world are loaded.
        ClientTickEvents.END_CLIENT_TICK.register(MappityClient::onTick);
    }

    private static void onTick(Minecraft mc) {
        if (mc.level != null && mc.player != null) {
            ClientState.get().ensureWaypointsLoadedForCurrentWorld();
        }
    }
}
