# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Upgrades is a BentoBox addon (Paper/Bukkit plugin) that lets island players purchase upgrades using Vault economy. It hooks into BentoBox GameMode addons (BSkyBlock, AcidIsland, CaveBlock, SkyGrid, AOneBlock) and optionally integrates with the Level and Limits addons.

### Relation to other addons for BentoBox

Upgrades runs within the BentoBox system as a plugin/addon to it. For coding, testing, and other patterns, it can and should draw on what other addons and BentoBox itself does. So the wider code base should be utilized when needed. This can be found at https://bentobox.world (GitHub) and the CI system is in https://ci.bentobox.world (CodeMC).

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

## Dependency Source Lookup

When you need to inspect source code for a dependency (e.g., BentoBox, addons):

1. **Check local Maven repo first**: `~/.m2/repository/` — sources jars are named `*-sources.jar`
2. **Check the workspace**: Look for sibling directories or Git submodules that may contain the dependency as a local project (e.g., `../bentoBox`, `../addon-*`)
3. **Check Maven local cache for already-extracted sources** before downloading anything
4. Only download a jar or fetch from the internet if the above steps yield nothing useful

Prefer reading `.java` source files directly from a local Git clone over decompiling or extracting a jar.

In general, the latest version of BentoBox should be targeted.

## Project Layout

Related projects are checked out as siblings under `~/git/`:

**Core:**
- `bentobox/` — core BentoBox framework

**Game modes:**
- `addon-acidisland/` — AcidIsland game mode
- `addon-bskyblock/` — BSkyBlock game mode
- `Boxed/` — Boxed game mode (expandable box area)
- `CaveBlock/` — CaveBlock game mode
- `OneBlock/` — AOneBlock game mode
- `SkyGrid/` — SkyGrid game mode
- `RaftMode/` — Raft survival game mode
- `StrangerRealms/` — StrangerRealms game mode
- `Brix/` — plot game mode
- `parkour/` — Parkour game mode
- `poseidon/` — Poseidon game mode
- `gg/` — gg game mode

**Addons:**
- `addon-level/` — island level calculation
- `addon-challenges/` — challenges system
- `addon-welcomewarpsigns/` — warp signs
- `addon-limits/` — block/entity limits
- `addon-invSwitcher/` / `invSwitcher/` — inventory switcher
- `addon-biomes/` / `Biomes/` — biomes management
- `Bank/` — island bank
- `Border/` — world border for islands
- `Chat/` — island chat
- `CheckMeOut/` — island submission/voting
- `ControlPanel/` — game mode control panel
- `Converter/` — ASkyBlock to BSkyBlock converter
- `DimensionalTrees/` — dimension-specific trees
- `discordwebhook/` — Discord integration
- `Downloads/` — BentoBox downloads site
- `DragonFights/` — per-island ender dragon fights
- `ExtraMobs/` — additional mob spawning rules
- `FarmersDance/` — twerking crop growth
- `GravityFlux/` — gravity addon
- `Greenhouses-addon/` — greenhouse biomes
- `IslandFly/` — island flight permission
- `IslandRankup/` — island rankup system
- `Likes/` — island likes/dislikes
- `Limits/` — block/entity limits
- `lost-sheep/` — lost sheep adventure
- `MagicCobblestoneGenerator/` — custom cobblestone generator
- `PortalStart/` — portal-based island start
- `pp/` — pp addon
- `Regionerator/` — region management
- `Residence/` — residence addon
- `TopBlock/` — top ten for OneBlock
- `TwerkingForTrees/` — twerking tree growth
- `Upgrades/` — island upgrades (Vault)
- `Visit/` — island visiting
- `weblink/` — web link addon
- `CrowdBound/` — CrowdBound addon

**Data packs:**
- `BoxedDataPack/` — advancement datapack for Boxed

**Documentation & tools:**
- `docs/` — main documentation site
- `docs-chinese/` — Chinese documentation
- `docs-french/` — French documentation
- `BentoBoxWorld.github.io/` — GitHub Pages site
- `website/` — website
- `translation-tool/` — translation tool

Check these for source before any network fetch.

## Key Dependencies (source locations)

- `world.bentobox:bentobox` → `~/git/bentobox/src/`
