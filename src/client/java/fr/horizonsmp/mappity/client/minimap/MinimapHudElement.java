package fr.horizonsmp.mappity.client.minimap;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class MinimapHudElement implements HudElement {

    private final MinimapRenderer renderer;

    public MinimapHudElement(MinimapRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        renderer.render(graphics);
    }
}
