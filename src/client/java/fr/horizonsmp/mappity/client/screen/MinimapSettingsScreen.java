package fr.horizonsmp.mappity.client.screen;

import fr.horizonsmp.mappity.client.ClientState;
import fr.horizonsmp.mappity.client.config.MinimapCorner;
import fr.horizonsmp.mappity.client.config.MinimapShape;
import fr.horizonsmp.mappity.client.config.ModConfig;
import fr.horizonsmp.mappity.client.config.ZoomLevel;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class MinimapSettingsScreen extends Screen {

    private Button shapeBtn;
    private Button cornerBtn;
    private Button zoomBtn;
    private Button entitiesBtn;
    private Button itemsBtn;
    private Button playersBtn;
    private Button waypointsBtn;
    private Button enabledBtn;

    public MinimapSettingsScreen() {
        super(Component.translatable("mappity.settings.title"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = 40;
        int rowH = 24;
        int btnW = 220;
        int btnH = 20;
        int x = centerX - btnW / 2;

        shapeBtn = Button.builder(shapeLabel(), b -> {
            ClientState.get().updateConfig(c -> c.withShape(c.shape().next()));
            refresh();
        }).bounds(x, startY, btnW, btnH).build();
        addRenderableWidget(shapeBtn);

        cornerBtn = Button.builder(cornerLabel(), b -> {
            ClientState.get().updateConfig(c -> c.withCorner(c.corner().next()));
            refresh();
        }).bounds(x, startY + rowH, btnW, btnH).build();
        addRenderableWidget(cornerBtn);

        zoomBtn = Button.builder(zoomLabel(), b -> {
            ClientState.get().updateConfig(c -> c.withZoomIndex((c.zoomIndex() + 1) % ZoomLevel.LEVELS.length));
            refresh();
        }).bounds(x, startY + 2 * rowH, btnW, btnH).build();
        addRenderableWidget(zoomBtn);

        entitiesBtn = Button.builder(entitiesLabel(), b -> {
            ClientState.get().updateConfig(c -> c.withShowEntities(!c.showEntities()));
            refresh();
        }).bounds(x, startY + 3 * rowH, btnW, btnH).build();
        addRenderableWidget(entitiesBtn);

        itemsBtn = Button.builder(itemsLabel(), b -> {
            ClientState.get().updateConfig(c -> c.withShowItems(!c.showItems()));
            refresh();
        }).bounds(x, startY + 4 * rowH, btnW, btnH).build();
        addRenderableWidget(itemsBtn);

        playersBtn = Button.builder(playersLabel(), b -> {
            ClientState.get().updateConfig(c -> c.withShowOtherPlayers(!c.showOtherPlayers()));
            refresh();
        }).bounds(x, startY + 5 * rowH, btnW, btnH).build();
        addRenderableWidget(playersBtn);

        waypointsBtn = Button.builder(waypointsLabel(), b -> {
            ClientState.get().updateConfig(c -> c.withShowWaypoints(!c.showWaypoints()));
            refresh();
        }).bounds(x, startY + 6 * rowH, btnW, btnH).build();
        addRenderableWidget(waypointsBtn);

        enabledBtn = Button.builder(enabledLabel(), b -> {
            ClientState.get().updateConfig(c -> c.withMinimapEnabled(!c.minimapEnabled()));
            refresh();
        }).bounds(x, startY + 7 * rowH, btnW, btnH).build();
        addRenderableWidget(enabledBtn);

        addRenderableWidget(Button.builder(Component.translatable("mappity.settings.done"), b -> this.onClose())
            .bounds(x, startY + 9 * rowH, btnW, btnH).build());
    }

    private void refresh() {
        shapeBtn.setMessage(shapeLabel());
        cornerBtn.setMessage(cornerLabel());
        zoomBtn.setMessage(zoomLabel());
        entitiesBtn.setMessage(entitiesLabel());
        itemsBtn.setMessage(itemsLabel());
        playersBtn.setMessage(playersLabel());
        waypointsBtn.setMessage(waypointsLabel());
        enabledBtn.setMessage(enabledLabel());
    }

    private Component shapeLabel() {
        ModConfig c = ClientState.get().config();
        return Component.translatable("mappity.settings.shape",
            Component.translatable(c.shape().translationKey()));
    }

    private Component cornerLabel() {
        ModConfig c = ClientState.get().config();
        return Component.translatable("mappity.settings.corner",
            Component.translatable(c.corner().translationKey()));
    }

    private Component zoomLabel() {
        ModConfig c = ClientState.get().config();
        return Component.translatable("mappity.settings.zoom", formatZoom(c.zoomScale()));
    }

    private Component entitiesLabel() {
        return Component.translatable("mappity.settings.show_entities", onOff(ClientState.get().config().showEntities()));
    }

    private Component itemsLabel() {
        return Component.translatable("mappity.settings.show_items", onOff(ClientState.get().config().showItems()));
    }

    private Component playersLabel() {
        return Component.translatable("mappity.settings.show_other_players", onOff(ClientState.get().config().showOtherPlayers()));
    }

    private Component waypointsLabel() {
        return Component.translatable("mappity.settings.show_waypoints", onOff(ClientState.get().config().showWaypoints()));
    }

    private Component enabledLabel() {
        return Component.translatable("mappity.settings.minimap_enabled", onOff(ClientState.get().config().minimapEnabled()));
    }

    private static Component onOff(boolean v) {
        return Component.translatable(v ? "mappity.common.on" : "mappity.common.off");
    }

    private static String formatZoom(float v) {
        if (v == Math.floor(v)) return String.valueOf((int) v);
        return String.valueOf(v);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
