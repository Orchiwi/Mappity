# Mappity

A client-only Fabric mod that adds a configurable minimap and a full-screen
world map to Minecraft. Works on any server (vanilla or modded) because all
rendering and storage is performed client-side.

## Features (v1.0.0-alpha.1)

- **HUD minimap** with two shapes (square, circle) and four anchor corners
  (top-left, top-right, bottom-left, bottom-right).
- **Zoom levels** (0.5x to 8x) controlled by rebindable keybinds.
- **Live block rendering** sampled from the heightmap and vanilla map colors,
  so the minimap reflects what the player sees from above.
- **Entity overlay** with color-coded dots: hostile (red), passive (green),
  other players (blue), dropped items (yellow). Each layer is toggleable.
- **Waypoint system** with per-dimension persistence:
  - Quick-add at the player position via keybind.
  - In-game manager screen (list, add, edit, delete, toggle visibility).
  - Markers shown on the minimap with name tooltip.
- **Settings screen** to change shape, corner, zoom defaults and toggles.
- **Multi-dimension awareness**: waypoints are scoped to the dimension they
  were created in.

## Coming next (v1.0.0-alpha.2)

- Full-screen world map showing every chunk the player has explored,
  persisted under `~/.minecraft/mappity/<world>/<dimension>/` as PNG tiles.
- Pan and zoom in the full map.
- Add a waypoint by right-clicking on the full map.

## Default keybinds

| Action | Default |
|---|---|
| Add waypoint here | `B` |
| Open waypoint manager | `J` |
| Open minimap settings | `M` |
| Zoom in | `=` |
| Zoom out | `-` |
| Cycle minimap shape | unbound |
| Cycle minimap corner | unbound |
| Toggle minimap | unbound |

All keys are rebindable from the standard *Controls* menu under the
*Mappity* category.

## Storage

- HUD preferences: `<game>/config/mappity.json`.
- Waypoints: `<game>/mappity/<worldId>/waypoints.json` (one folder per
  singleplayer save name or per multiplayer server).

## Build

```bash
./gradlew build
```

The built jar lands in `build/libs/mappity-<version>.jar`.
