package fr.horizonsmp.mappity.client;

import fr.horizonsmp.mappity.client.config.ConfigLoader;
import fr.horizonsmp.mappity.client.config.ModConfig;
import fr.horizonsmp.mappity.client.waypoint.WaypointPersistence;
import fr.horizonsmp.mappity.client.waypoint.WaypointStore;
import fr.horizonsmp.mappity.client.world.WorldIdentity;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton holding the live mod config + waypoint store. Initialized at
 * client startup; the waypoint store is reloaded on world join.
 */
public final class ClientState {

    private static final Logger LOGGER = LoggerFactory.getLogger("mappity/state");

    private static ClientState instance;

    private final ConfigLoader configLoader;
    private final AtomicReference<ModConfig> config;
    private final WaypointStore waypoints = new WaypointStore();
    private final AtomicReference<String> loadedWorldId = new AtomicReference<>(null);

    private ClientState(ConfigLoader loader, ModConfig initial) {
        this.configLoader = loader;
        this.config = new AtomicReference<>(initial);
    }

    public static synchronized void init() {
        if (instance != null) return;
        Path configFile = FabricLoader.getInstance().getConfigDir().resolve("mappity.json");
        ConfigLoader loader = new ConfigLoader(configFile);
        ModConfig initial = loader.loadOrDefault();
        instance = new ClientState(loader, initial);
    }

    public static ClientState get() {
        if (instance == null) {
            throw new IllegalStateException("ClientState not initialized yet");
        }
        return instance;
    }

    public ModConfig config() {
        return config.get();
    }

    public void updateConfig(java.util.function.UnaryOperator<ModConfig> fn) {
        ModConfig updated = config.updateAndGet(fn);
        try {
            configLoader.save(updated);
        } catch (IOException ex) {
            LOGGER.warn("Failed to persist config", ex);
        }
    }

    public WaypointStore waypoints() {
        return waypoints;
    }

    public void ensureWaypointsLoadedForCurrentWorld() {
        String worldId = WorldIdentity.current();
        String loaded = loadedWorldId.get();
        if (worldId.equals(loaded)) return;
        Path file = waypointsFile(worldId);
        WaypointPersistence.load(file, waypoints);
        loadedWorldId.set(worldId);
    }

    public void persistWaypoints() {
        String worldId = loadedWorldId.get();
        if (worldId == null) {
            worldId = WorldIdentity.current();
            loadedWorldId.set(worldId);
        }
        Path file = waypointsFile(worldId);
        WaypointPersistence.save(file, waypoints);
    }

    private static Path waypointsFile(String worldId) {
        return FabricLoader.getInstance().getGameDir()
            .resolve("mappity")
            .resolve(worldId)
            .resolve("waypoints.json");
    }
}
