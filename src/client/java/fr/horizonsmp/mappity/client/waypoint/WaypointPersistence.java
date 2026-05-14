package fr.horizonsmp.mappity.client.waypoint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class WaypointPersistence {

    private static final Logger LOGGER = LoggerFactory.getLogger("mappity/waypoints");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private WaypointPersistence() {}

    public static void load(Path file, WaypointStore store) {
        if (!Files.exists(file)) {
            store.replaceAll(new LinkedHashMap<>());
            return;
        }
        try {
            String content = Files.readString(file);
            JsonObject root = JsonParser.parseString(content).getAsJsonObject();
            Map<String, List<Waypoint>> all = new LinkedHashMap<>();
            if (root.has("dimensions") && root.get("dimensions").isJsonObject()) {
                JsonObject dims = root.getAsJsonObject("dimensions");
                for (Map.Entry<String, JsonElement> entry : dims.entrySet()) {
                    if (!entry.getValue().isJsonArray()) continue;
                    List<Waypoint> list = new ArrayList<>();
                    for (JsonElement el : entry.getValue().getAsJsonArray()) {
                        Waypoint wp = readWaypoint(el);
                        if (wp != null) list.add(wp);
                    }
                    all.put(entry.getKey(), list);
                }
            }
            store.replaceAll(all);
        } catch (IOException ex) {
            LOGGER.warn("Failed to read waypoints from {}, starting empty", file, ex);
            store.replaceAll(new LinkedHashMap<>());
        } catch (RuntimeException ex) {
            LOGGER.warn("Waypoints file at {} is malformed, starting empty", file, ex);
            store.replaceAll(new LinkedHashMap<>());
        }
    }

    public static void save(Path file, WaypointStore store) {
        try {
            Files.createDirectories(file.getParent());
            JsonObject root = new JsonObject();
            JsonObject dims = new JsonObject();
            for (Map.Entry<String, List<Waypoint>> entry : store.snapshot().entrySet()) {
                JsonArray arr = new JsonArray();
                for (Waypoint wp : entry.getValue()) {
                    arr.add(writeWaypoint(wp));
                }
                dims.add(entry.getKey(), arr);
            }
            root.add("dimensions", dims);

            Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
            Files.writeString(tmp, GSON.toJson(root));
            try {
                Files.move(tmp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            LOGGER.warn("Failed to write waypoints to {}", file, ex);
        }
    }

    private static Waypoint readWaypoint(JsonElement el) {
        if (!el.isJsonObject()) return null;
        JsonObject obj = el.getAsJsonObject();
        try {
            UUID id = obj.has("id") ? UUID.fromString(obj.get("id").getAsString()) : UUID.randomUUID();
            String name = obj.has("name") ? obj.get("name").getAsString() : "Waypoint";
            int x = obj.has("x") ? obj.get("x").getAsInt() : 0;
            int y = obj.has("y") ? obj.get("y").getAsInt() : 64;
            int z = obj.has("z") ? obj.get("z").getAsInt() : 0;
            int color = obj.has("color") ? obj.get("color").getAsInt() : WaypointPalette.DEFAULT_COLOR;
            boolean visible = !obj.has("visible") || obj.get("visible").getAsBoolean();
            return new Waypoint(id, name, x, y, z, color, visible);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private static JsonObject writeWaypoint(Waypoint wp) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", wp.id().toString());
        obj.addProperty("name", wp.name());
        obj.addProperty("x", wp.x());
        obj.addProperty("y", wp.y());
        obj.addProperty("z", wp.z());
        obj.addProperty("color", wp.colorRgb());
        obj.addProperty("visible", wp.visible());
        return obj;
    }
}
