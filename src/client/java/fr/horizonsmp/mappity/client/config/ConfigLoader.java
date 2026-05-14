package fr.horizonsmp.mappity.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConfigLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger("mappity/config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final Path configFile;

    public ConfigLoader(Path configFile) {
        this.configFile = configFile;
    }

    public ModConfig loadOrDefault() {
        if (!Files.exists(configFile)) {
            ModConfig defaults = ModConfig.defaults();
            try {
                save(defaults);
            } catch (IOException ex) {
                LOGGER.warn("Failed to write default config to {}", configFile, ex);
            }
            return defaults;
        }
        try {
            String content = Files.readString(configFile);
            return parse(content);
        } catch (IOException ex) {
            LOGGER.warn("Failed to read config from {}, falling back to defaults", configFile, ex);
            return ModConfig.defaults();
        } catch (RuntimeException ex) {
            LOGGER.warn("Config at {} is malformed, falling back to defaults", configFile, ex);
            return ModConfig.defaults();
        }
    }

    public void save(ModConfig config) throws IOException {
        Files.createDirectories(configFile.getParent());
        Path tmp = configFile.resolveSibling(configFile.getFileName() + ".tmp");
        Files.writeString(tmp, render(config));
        try {
            Files.move(tmp, configFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(tmp, configFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private ModConfig parse(String content) {
        JsonObject root = JsonParser.parseString(content).getAsJsonObject();
        ModConfig defaults = ModConfig.defaults();

        boolean minimapEnabled = readBoolean(root, "minimap_enabled", defaults.minimapEnabled());
        MinimapShape shape = readEnum(root, "shape", MinimapShape.class, defaults.shape());
        MinimapCorner corner = readEnum(root, "corner", MinimapCorner.class, defaults.corner());
        int zoomIndex = readInt(root, "zoom_index", defaults.zoomIndex());
        boolean showEntities = readBoolean(root, "show_entities", defaults.showEntities());
        boolean showItems = readBoolean(root, "show_items", defaults.showItems());
        boolean showOtherPlayers = readBoolean(root, "show_other_players", defaults.showOtherPlayers());
        boolean showWaypoints = readBoolean(root, "show_waypoints", defaults.showWaypoints());

        return new ModConfig(
            minimapEnabled, shape, corner, ZoomLevel.clamp(zoomIndex),
            showEntities, showItems, showOtherPlayers, showWaypoints
        );
    }

    private String render(ModConfig config) {
        JsonObject root = new JsonObject();
        root.addProperty("minimap_enabled", config.minimapEnabled());
        root.addProperty("shape", config.shape().name());
        root.addProperty("corner", config.corner().name());
        root.addProperty("zoom_index", config.zoomIndex());
        root.addProperty("show_entities", config.showEntities());
        root.addProperty("show_items", config.showItems());
        root.addProperty("show_other_players", config.showOtherPlayers());
        root.addProperty("show_waypoints", config.showWaypoints());
        return GSON.toJson(root);
    }

    private static <E extends Enum<E>> E readEnum(JsonObject root, String key, Class<E> type, E fallback) {
        if (!root.has(key)) return fallback;
        try {
            return Enum.valueOf(type, root.get(key).getAsString());
        } catch (RuntimeException ex) {
            return fallback;
        }
    }

    private static boolean readBoolean(JsonObject root, String key, boolean fallback) {
        if (!root.has(key)) return fallback;
        try {
            return root.get(key).getAsBoolean();
        } catch (RuntimeException ex) {
            return fallback;
        }
    }

    private static int readInt(JsonObject root, String key, int fallback) {
        if (!root.has(key)) return fallback;
        try {
            return root.get(key).getAsInt();
        } catch (RuntimeException ex) {
            return fallback;
        }
    }
}
