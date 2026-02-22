# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Upgrades is a BentoBox addon (Paper/Bukkit plugin) that lets island players purchase upgrades using Vault economy. It hooks into BentoBox GameMode addons (BSkyBlock, AcidIsland, CaveBlock, SkyGrid, AOneBlock) and optionally integrates with the Level and Limits addons.

## Build & Test Commands

```bash
# Build (produces JAR in target/)
mvn clean package

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=UpgradesAddonTest

# Run a specific test method
mvn test -Dtest=UpgradesAddonTest#testOnEnable
```

Java 21 is required. The surefire plugin is configured with `--add-opens` flags so tests work with MockBukkit.

## Architecture

### Core Components

**`UpgradesAddon`** — Main entry point extending BentoBox's `Addon`. Lifecycle:
- `onLoad`: loads config, creates `Settings`
- `onEnable`: iterates all non-disabled GameMode addons, registers `PlayerUpgradeCommand` in each, creates `UpgradesManager`, hooks optional soft-deps (Level, Limits, Vault), instantiates and registers all `Upgrade` objects
- `onDisable`: async-saves all cached `UpgradesData` to DB

**`UpgradesManager`** — Resolves upgrade tiers per-world by merging global defaults with game-mode-specific overrides from `Settings`. Every public method takes a `World` to look up the active GameModeAddon name and apply the right tier config.

**`Settings`** — Parses `config.yml` into typed tier maps. Per-upgrade-type maps exist in two layers: default (global) and custom (per game mode). `UpgradesManager` merges them on each lookup.

**`Upgrade` (abstract)** — Base class for all upgrade types. Key contract:
- `updateUpgradeValue(user, island)` — called each time the panel opens; must populate `playerCache` via `setUpgradeValues()` and `setOwnDescription()`
- `canUpgrade(user, island)` — checks island level and Vault balance; override and call `super`
- `doUpgrade(user, island)` — withdraws Vault cost, increments level in `UpgradesData`; override and call `super`
- `isShowed(user, island)` — returns `true` by default; override to hide when maxed out

Concrete subclasses: `RangeUpgrade`, `BlockLimitsUpgrade`, `EntityLimitsUpgrade`, `EntityGroupLimitsUpgrade`, `CommandUpgrade`.

**`UpgradesData`** — BentoBox `DataObject` (table `UpgradesData`) storing a `Map<String, Integer>` of upgrade name → current level per island (uniqueId = island UUID). Levels start at 1 (first call to `getUpgradeLevel` inserts 1 via `putIfAbsent`).

**`Panel` / `PanelClick`** — BentoBox Panel API GUI. `Panel.showPanel()` iterates registered upgrades, calls `updateUpgradeValue`, and builds panel items. `PanelClick` handles the click → `canUpgrade` → `doUpgrade` flow.

### Data Flow for a Purchase

1. Player runs `/[gamemode] upgrades` → `PlayerUpgradeCommand` → opens `Panel`
2. Panel calls `upgrade.updateUpgradeValue()` for each registered upgrade (populates per-user cache)
3. Player clicks an item → `PanelClick.onClick()` → `upgrade.canUpgrade()` → `upgrade.doUpgrade()` → panel reopens

### Key Design Notes

- Island upgrade data is memory-cached in `UpgradesAddon.upgradesCache` (Map<islandUniqueId, UpgradesData>) and saved async on disable or island uncache.
- Per-user `UpgradeValues` (islandLevel req, moneyCost, upgradeValue) are stored in `Upgrade.playerCache` (Map<UUID, UpgradeValues>) and are stale between panel opens.
- Soft dependencies (Level, Limits, Vault) are each guarded by `isLevelProvided()`, `isLimitsProvided()`, `isVaultProvided()`. Block/Entity Limits upgrades are only registered if Limits is present.
- Permission-based max level overrides: upgrades check player permissions (e.g. `[gamemode].island.maxrange.[n]`) to cap upgrades, taking the highest permission value found.
- Tier configs support formula strings for `islandMinLevel`, `vaultCost`, and `upgrade` values that can reference `%level%`, `%islandlevel%`, and `%numberofmembers%`.

### Adding a New Upgrade Type

1. Extend `Upgrade`, implement `updateUpgradeValue()` and override `doUpgrade()`/`canUpgrade()`/`isShowed()` as needed
2. Register an instance via `UpgradesAddon.registerUpgrade()` in `onEnable()`
3. Add any new config sections to `Settings` and expose them via `UpgradesManager`
4. Add locale keys to `src/main/resources/locales/en-US.yml`
