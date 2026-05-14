package fr.horizonsmp.mappity.client.waypoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * In-memory store for waypoints, keyed by stable dimension id string.
 * Mutations are synchronous; persistence is triggered by the caller.
 */
public final class WaypointStore {

    private final Map<String, List<Waypoint>> byDimension = new LinkedHashMap<>();

    public synchronized List<Waypoint> get(String dimensionId) {
        List<Waypoint> list = byDimension.get(dimensionId);
        if (list == null) return Collections.emptyList();
        return Collections.unmodifiableList(new ArrayList<>(list));
    }

    public synchronized List<String> dimensions() {
        return new ArrayList<>(byDimension.keySet());
    }

    public synchronized void add(String dimensionId, Waypoint waypoint) {
        byDimension.computeIfAbsent(dimensionId, k -> new ArrayList<>()).add(waypoint);
    }

    public synchronized boolean update(String dimensionId, Waypoint waypoint) {
        List<Waypoint> list = byDimension.get(dimensionId);
        if (list == null) return false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).id().equals(waypoint.id())) {
                list.set(i, waypoint);
                return true;
            }
        }
        return false;
    }

    public synchronized boolean remove(String dimensionId, UUID id) {
        List<Waypoint> list = byDimension.get(dimensionId);
        if (list == null) return false;
        return list.removeIf(w -> w.id().equals(id));
    }

    public synchronized void replaceAll(Map<String, List<Waypoint>> all) {
        byDimension.clear();
        for (Map.Entry<String, List<Waypoint>> entry : all.entrySet()) {
            byDimension.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
    }

    public synchronized Map<String, List<Waypoint>> snapshot() {
        Map<String, List<Waypoint>> copy = new LinkedHashMap<>();
        for (Map.Entry<String, List<Waypoint>> entry : byDimension.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copy;
    }
}
