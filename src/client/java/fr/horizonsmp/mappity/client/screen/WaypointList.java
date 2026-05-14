package fr.horizonsmp.mappity.client.screen;

import fr.horizonsmp.mappity.client.waypoint.Waypoint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public final class WaypointList extends ObjectSelectionList<WaypointList.Entry> {

    private final WaypointManagerScreen parent;

    public WaypointList(Minecraft mc, int x, int y, int width, int height, WaypointManagerScreen parent) {
        super(mc, x, y, width, height);
        this.parent = parent;
        this.setX(x);
        this.setY(y);
    }

    public void reload() {
        this.clearEntries();
        for (Waypoint wp : parent.currentWaypoints()) {
            this.addEntry(new Entry(wp));
        }
    }

    public Waypoint selectedWaypoint() {
        Entry e = this.getSelected();
        return e == null ? null : e.waypoint;
    }

    @Override
    public void setSelected(Entry entry) {
        super.setSelected(entry);
        parent.updateButtons();
    }

    public final class Entry extends ObjectSelectionList.Entry<Entry> {
        final Waypoint waypoint;

        Entry(Waypoint waypoint) {
            this.waypoint = waypoint;
        }

        @Override
        public Component getNarration() {
            return Component.literal(waypoint.name());
        }

        @Override
        public void extractContent(GuiGraphicsExtractor gfx, int mouseX, int mouseY, boolean hovered, float partialTick) {
            Minecraft mc = Minecraft.getInstance();
            int top = getContentY();
            int left = getContentX();
            int color = waypoint.visible() ? 0xFFFFFFFF : 0xFF888888;
            gfx.fill(left + 2, top + 2, left + 14, top + 14, waypoint.colorRgb());
            gfx.text(mc.font, Component.literal(waypoint.name()), left + 20, top + 2, color, false);
            String coords = "(" + waypoint.x() + ", " + waypoint.y() + ", " + waypoint.z() + ")";
            gfx.text(mc.font, Component.literal(coords), left + 20, top + 14, 0xFFAAAAAA, false);
        }
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput out) {
        // No-op: per-entry narration is delegated.
    }
}
