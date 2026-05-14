package fr.horizonsmp.mappity.client.screen;

import fr.horizonsmp.mappity.client.ClientState;
import fr.horizonsmp.mappity.client.waypoint.Waypoint;
import fr.horizonsmp.mappity.client.waypoint.WaypointPalette;
import fr.horizonsmp.mappity.client.world.DimensionId;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

public final class WaypointManagerScreen extends Screen {

    private Button editBtn;
    private Button deleteBtn;
    private Button toggleBtn;
    private WaypointList list;

    public WaypointManagerScreen() {
        super(Component.translatable("mappity.waypoint.manager.title", currentDim()));
    }

    private static String currentDim() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return "?";
        return DimensionId.of(level.dimension());
    }

    @Override
    protected void init() {
        int listX = 20;
        int listW = this.width - 40;
        int listTop = 30;
        int listBottom = this.height - 50;
        list = new WaypointList(this.minecraft, listX, listTop, listW, listBottom - listTop, this);
        addRenderableWidget(list);

        int btnY = this.height - 30;
        int btnW = 80;
        int btnH = 20;
        int gap = 8;
        int totalW = 4 * btnW + 3 * gap;
        int startX = (this.width - totalW) / 2;

        addRenderableWidget(Button.builder(Component.translatable("mappity.waypoint.manager.add"), b -> openCreate())
            .bounds(startX, btnY, btnW, btnH).build());
        editBtn = Button.builder(Component.translatable("mappity.waypoint.manager.edit"), b -> openEdit())
            .bounds(startX + btnW + gap, btnY, btnW, btnH).build();
        addRenderableWidget(editBtn);
        deleteBtn = Button.builder(Component.translatable("mappity.waypoint.manager.delete"), b -> deleteSelected())
            .bounds(startX + 2 * (btnW + gap), btnY, btnW, btnH).build();
        addRenderableWidget(deleteBtn);
        toggleBtn = Button.builder(toggleLabel(), b -> toggleSelected())
            .bounds(startX + 3 * (btnW + gap), btnY, btnW, btnH).build();
        addRenderableWidget(toggleBtn);

        addRenderableWidget(Button.builder(Component.translatable("mappity.waypoint.manager.done"), b -> this.onClose())
            .bounds(this.width / 2 - 50, btnY - 26, 100, btnH).build());

        list.reload();
        updateButtons();
    }

    void updateButtons() {
        Waypoint selected = list.selectedWaypoint();
        editBtn.active = selected != null;
        deleteBtn.active = selected != null;
        toggleBtn.active = selected != null;
        toggleBtn.setMessage(toggleLabel());
    }

    private Component toggleLabel() {
        Waypoint s = list != null ? list.selectedWaypoint() : null;
        boolean v = s != null && s.visible();
        return Component.translatable("mappity.waypoint.manager.toggle",
            Component.translatable(v ? "mappity.common.on" : "mappity.common.off"));
    }

    private void openCreate() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        int x = p != null ? (int) Math.floor(p.getX()) : 0;
        int y = p != null ? (int) Math.floor(p.getY()) : 64;
        int z = p != null ? (int) Math.floor(p.getZ()) : 0;
        Waypoint draft = Waypoint.of("Waypoint", x, y, z, WaypointPalette.DEFAULT_COLOR);
        mc.setScreen(new WaypointEditScreen(this, draft, true));
    }

    private void openEdit() {
        Waypoint s = list.selectedWaypoint();
        if (s == null) return;
        Minecraft.getInstance().setScreen(new WaypointEditScreen(this, s, false));
    }

    private void deleteSelected() {
        Waypoint s = list.selectedWaypoint();
        if (s == null) return;
        ClientState state = ClientState.get();
        state.waypoints().remove(currentDim(), s.id());
        state.persistWaypoints();
        list.reload();
        updateButtons();
    }

    private void toggleSelected() {
        Waypoint s = list.selectedWaypoint();
        if (s == null) return;
        ClientState state = ClientState.get();
        state.waypoints().update(currentDim(), s.withVisible(!s.visible()));
        state.persistWaypoints();
        list.reload();
        updateButtons();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    String dimension() {
        return currentDim();
    }

    List<Waypoint> currentWaypoints() {
        return ClientState.get().waypoints().get(currentDim());
    }
}
