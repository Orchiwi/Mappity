package fr.horizonsmp.mappity.client.screen;

import fr.horizonsmp.mappity.client.ClientState;
import fr.horizonsmp.mappity.client.waypoint.Waypoint;
import fr.horizonsmp.mappity.client.waypoint.WaypointPalette;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class WaypointEditScreen extends Screen {

    private final WaypointManagerScreen parent;
    private final Waypoint original;
    private final boolean isNew;

    private EditBox nameBox;
    private EditBox xBox;
    private EditBox yBox;
    private EditBox zBox;
    private Button colorBtn;
    private int currentColor;

    public WaypointEditScreen(WaypointManagerScreen parent, Waypoint original, boolean isNew) {
        super(Component.translatable("mappity.waypoint.edit.title"));
        this.parent = parent;
        this.original = original;
        this.isNew = isNew;
        this.currentColor = original.colorRgb();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int boxW = 200;
        int boxH = 20;
        int rowH = 28;
        int x = centerX - boxW / 2;
        int y = 40;

        nameBox = new EditBox(this.font, x, y, boxW, boxH, Component.translatable("mappity.waypoint.edit.name"));
        nameBox.setMaxLength(64);
        nameBox.setValue(original.name());
        addRenderableWidget(nameBox);
        y += rowH;

        int coordW = (boxW - 16) / 3;
        xBox = new EditBox(this.font, x, y, coordW, boxH, Component.translatable("mappity.waypoint.edit.x"));
        xBox.setValue(String.valueOf(original.x()));
        addRenderableWidget(xBox);
        yBox = new EditBox(this.font, x + coordW + 8, y, coordW, boxH, Component.translatable("mappity.waypoint.edit.y"));
        yBox.setValue(String.valueOf(original.y()));
        addRenderableWidget(yBox);
        zBox = new EditBox(this.font, x + 2 * (coordW + 8), y, coordW, boxH, Component.translatable("mappity.waypoint.edit.z"));
        zBox.setValue(String.valueOf(original.z()));
        addRenderableWidget(zBox);
        y += rowH;

        colorBtn = Button.builder(colorLabel(), b -> {
            currentColor = WaypointPalette.cycle(currentColor);
            colorBtn.setMessage(colorLabel());
        }).bounds(x, y, boxW, boxH).build();
        addRenderableWidget(colorBtn);
        y += rowH + 8;

        int halfW = (boxW - 8) / 2;
        addRenderableWidget(Button.builder(Component.translatable("mappity.waypoint.edit.save"), b -> save())
            .bounds(x, y, halfW, boxH).build());
        addRenderableWidget(Button.builder(Component.translatable("mappity.waypoint.edit.cancel"), b -> cancel())
            .bounds(x + halfW + 8, y, halfW, boxH).build());

        setInitialFocus(nameBox);
    }

    private Component colorLabel() {
        return Component.translatable("mappity.waypoint.edit.color")
            .append(": #" + String.format("%06X", currentColor & 0xFFFFFF));
    }

    private void save() {
        int x = parseInt(xBox.getValue(), original.x());
        int y = parseInt(yBox.getValue(), original.y());
        int z = parseInt(zBox.getValue(), original.z());
        String name = nameBox.getValue().trim();
        if (name.isEmpty()) name = original.name();

        Waypoint updated = new Waypoint(original.id(), name, x, y, z, currentColor, original.visible());
        ClientState state = ClientState.get();
        String dim = parent.dimension();
        if (isNew) {
            state.waypoints().add(dim, updated);
        } else {
            state.waypoints().update(dim, updated);
        }
        state.persistWaypoints();
        Minecraft.getInstance().setScreen(parent);
    }

    private void cancel() {
        Minecraft.getInstance().setScreen(parent);
    }

    private static int parseInt(String s, int fallback) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }
}
