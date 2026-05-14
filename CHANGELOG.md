# Changelog

All notable changes to Mappity are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project follows [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Project bootstrap: Fabric Loom 1.16 scaffold targeting Minecraft 26.1.2,
  Java 25, split client/main source sets.
- HUD minimap with two shapes (square, circle) and four anchor corners
  (top-left, top-right, bottom-left, bottom-right). Rendered via the
  Fabric `HudElementRegistry`.
- Five zoom levels (`0.5x`, `1x`, `2x`, `4x`, `8x`) controlled by rebindable
  keybinds (`=` zoom in, `-` zoom out).
- Live block rendering: per-frame sampling around the player using the
  `WORLD_SURFACE` heightmap and vanilla `MapColor`, backed by a
  `NativeImage`-backed `DynamicTexture`.
- Entity overlay with color-coded dots: hostile (red), passive (green),
  other players (blue), dropped items (yellow). Each layer is toggleable
  in the settings screen.
- Waypoint system with per-dimension persistence:
  - Quick-add keybind (`B`) at the player position.
  - Manager screen (`J`) with add, edit, delete, toggle-visibility.
  - Edit screen with name, X/Y/Z, and a 12-color palette.
  - Markers rendered on the minimap, clamped to the edge with a small
    bias when out of view.
- Settings screen (`M`) covering shape, corner, zoom, entity/item/player
  toggles, waypoint visibility, and a master minimap toggle.
- Rebindable category-cycle and corner-cycle keybinds (unbound by default).
- Mod config persisted to `<game>/config/mappity.json` (atomic write).
- Waypoints persisted under
  `<game>/mappity/<worldId>/waypoints.json`, where `worldId` is the
  sanitized save name in singleplayer or a short SHA-1 of the server IP
  in multiplayer.
