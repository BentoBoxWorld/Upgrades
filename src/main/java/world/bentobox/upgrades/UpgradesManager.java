package world.bentobox.upgrades;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.limits.objects.IslandBlockCount;
import world.bentobox.upgrades.config.Settings;

/**
 * Manages upgrades in the BentoBox Upgrades addon.
 * <p>
 * The {@code UpgradesManager} class handles the retrieval and management of upgrade tiers, 
 * permissions, and configurations for different upgrade types. It provides methods to 
 * interact with upgrade data, such as determining upgrade effects, costs, and eligibility.
 * </p>
 * 
 * <p>
 * This class supports multiple upgrade types, including range upgrades, block limits upgrades, 
 * entity limits upgrades, entity group limits upgrades, and command upgrades. It also manages 
 * customization for different game modes and tracks compatibility with specific worlds.
 * </p>
 */
public class UpgradesManager {

    /**
     * Constructs a new {@code UpgradesManager}.
     *
     * @param addon The {@link UpgradesAddon} instance associated with this manager.
     */
	public UpgradesManager(UpgradesAddon addon) {
		this.addon = addon;
		this.hookedGameModes = new HashSet<>();
	}

    /**
     * Adds the specified game modes to the list of hooked game modes.
     *
     * @param gameModes A list of game mode names to be hooked.
     */
	protected void addGameModes(List<String> gameModes) {
		this.hookedGameModes.addAll(gameModes);
	}

    /**
     * Checks if the manager can operate in the specified world.
     *
     * @param world The world to check.
     * @return {@code true} if operations are allowed in the world; {@code false} otherwise.
     */
	public boolean canOperateInWorld(World world) {
		Optional<GameModeAddon> addon = this.addon.getPlugin().getIWM().getAddon(world);

		return addon.isPresent() && this.hookedGameModes.contains(addon.get().getDescription().getName());
	}

    /**
     * Retrieves the level of the specified island. Level addon must be available.
     *
     * @param island The island whose level is to be retrieved.
     * @return The level of the island, or 0 if invalid or not found.
     */
	public int getIslandLevel(Island island) {
		if (!this.addon.isLevelProvided())
			return 0;

		if (island == null) {
			this.addon.logError("Island couldn't be found");
			return 0;
		}
        // Get the island's level
        return (int) this.addon.getLevelAddon().getManager().getLevelsData(island).getLevel();

	}

    /**
     * Retrieves all range upgrade tiers for the specified world.
     *
     * @param world The world for which range upgrade tiers are requested.
     * @return A list of {@link Settings.UpgradeTier} representing the range upgrade tiers.
     */
	public List<Settings.UpgradeTier> getAllRangeUpgradeTiers(World world) {
		String name = this.addon.getPlugin().getIWM().getAddon(world).map(a -> a.getDescription().getName())
				.orElse(null);
		if (name == null)
			return Collections.emptyList();

		Map<String, Settings.UpgradeTier> defaultTiers = this.addon.getSettings().getDefaultRangeUpgradeTierMap();
		Map<String, Settings.UpgradeTier> customAddonTiers = this.addon.getSettings().getAddonRangeUpgradeTierMap(name);

		List<Settings.UpgradeTier> tierList;

		if (customAddonTiers.isEmpty())
			tierList = new ArrayList<>(defaultTiers.values());
		else {
			Set<String> uniqueIDSet = new HashSet<>(customAddonTiers.keySet());
			uniqueIDSet.addAll(defaultTiers.keySet());
			tierList = new ArrayList<>(uniqueIDSet.size());

			uniqueIDSet.forEach(id -> tierList.add(customAddonTiers.getOrDefault(id, defaultTiers.get(id))));
		}

		if (tierList.isEmpty())
			return Collections.emptyList();

		tierList.sort(Comparator.comparingInt(Settings.UpgradeTier::getMaxLevel));

		return tierList;
	}

    /**
     * Retrieves all block limits upgrade tiers for the specified world.
     *
     * @param world The world for which block limits upgrade tiers are requested.
     * @return A map of {@link Material} to a list of {@link Settings.UpgradeTier}.
     */
	public Map<Material, List<Settings.UpgradeTier>> getAllBlockLimitsUpgradeTiers(World world) {
		String name = this.addon.getPlugin().getIWM().getAddon(world).map(a -> a.getDescription().getName())
				.orElse(null);
		if (name == null) {
			return Collections.emptyMap();
		}

		Map<Material, Map<String, Settings.UpgradeTier>> defaultTiers = this.addon.getSettings()
				.getDefaultBlockLimitsUpgradeTierMap();
		Map<Material, Map<String, Settings.UpgradeTier>> customAddonTiers = this.addon.getSettings()
				.getAddonBlockLimitsUpgradeTierMap(name);

		Map<Material, List<Settings.UpgradeTier>> tierList = new EnumMap<>(Material.class);

		if (customAddonTiers.isEmpty()) {
			defaultTiers.forEach((mat, tiers) -> tierList.put(mat, new ArrayList<>(tiers.values())));
		} else {
			customAddonTiers.forEach((mat, tiers) -> {
				Set<String> uniqueIDSet = new HashSet<>(tiers.keySet());
				if (defaultTiers.containsKey(mat))
					uniqueIDSet.addAll(defaultTiers.get(mat).keySet());
				List<Settings.UpgradeTier> matTier = new ArrayList<>(uniqueIDSet.size());

				uniqueIDSet.forEach(id -> matTier.add(tiers.getOrDefault(id, defaultTiers.get(mat).get(id))));
				tierList.put(mat, matTier);
			});

			defaultTiers.forEach((mat, tiers) -> tierList.putIfAbsent(mat, new ArrayList<>(tiers.values())));
		}

		if (tierList.isEmpty()) {
			return Collections.emptyMap();
		}

		tierList.forEach((mat, tiers) -> tiers.sort(Comparator.comparingInt(Settings.UpgradeTier::getMaxLevel)));

		return tierList;
	}

    /**
     * Retrieves all entity limits upgrade tiers for the specified world.
     *
     * @param world The world for which entity limits upgrade tiers are requested.
     * @return A map of {@link EntityType} to a list of {@link Settings.UpgradeTier}.
     */
	public Map<EntityType, List<Settings.UpgradeTier>> getAllEntityLimitsUpgradeTiers(World world) {
		String name = this.addon.getPlugin().getIWM().getAddon(world).map(a -> a.getDescription().getName())
				.orElse(null);
		if (name == null) {
			return Collections.emptyMap();
		}

		Map<EntityType, Map<String, Settings.UpgradeTier>> defaultTiers = this.addon.getSettings()
				.getDefaultEntityLimitsUpgradeTierMap();
		Map<EntityType, Map<String, Settings.UpgradeTier>> customAddonTiers = this.addon.getSettings()
				.getAddonEntityLimitsUpgradeTierMap(name);

		Map<EntityType, List<Settings.UpgradeTier>> tierList = new EnumMap<>(EntityType.class);

		if (customAddonTiers.isEmpty()) {
			defaultTiers.forEach((ent, tiers) -> tierList.put(ent, new ArrayList<>(tiers.values())));
		} else {
			customAddonTiers.forEach((ent, tiers) -> {
				Set<String> uniqueIDSet = new HashSet<>(tiers.keySet());
				if (defaultTiers.containsKey(ent))
					uniqueIDSet.addAll(defaultTiers.get(ent).keySet());
				List<Settings.UpgradeTier> entTier = new ArrayList<>(uniqueIDSet.size());

				uniqueIDSet.forEach(id -> entTier.add(tiers.getOrDefault(id, defaultTiers.get(ent).get(id))));
				tierList.put(ent, entTier);
			});

			defaultTiers.forEach((ent, tiers) -> tierList.putIfAbsent(ent, new ArrayList<>(tiers.values())));
		}

		if (tierList.isEmpty()) {
			return Collections.emptyMap();
		}

		tierList.forEach((ent, tiers) -> tiers.sort(Comparator.comparingInt(Settings.UpgradeTier::getMaxLevel)));

		return tierList;
	}

    /**
     * Retrieves all entity group limits upgrade tiers for the specified world.
     *
     * @param world The world for which entity group limits upgrade tiers are requested.
     * @return A map of entity group names to a list of {@link Settings.UpgradeTier}.
     */
	public Map<String, List<Settings.UpgradeTier>> getAllEntityGroupLimitsUpgradeTiers(World world) {
		String name = this.addon.getPlugin().getIWM().getAddon(world).map(a -> a.getDescription().getName())
				.orElse(null);
		if (name == null) {
			return Collections.emptyMap();
		}

		Map<String, Map<String, Settings.UpgradeTier>> defaultTiers = this.addon.getSettings()
				.getDefaultEntityGroupLimitsUpgradeTierMap();
		Map<String, Map<String, Settings.UpgradeTier>> customAddonTiers = this.addon.getSettings()
				.getAddonEntityGroupLimitsUpgradeTierMap(name);

		Map<String, List<Settings.UpgradeTier>> tierList = new TreeMap<>();

		if (customAddonTiers.isEmpty()) {
			defaultTiers.forEach((ent, tiers) -> tierList.put(ent, new ArrayList<>(tiers.values())));
		} else {
			customAddonTiers.forEach((ent, tiers) -> {
				Set<String> uniqueIDSet = new HashSet<>(tiers.keySet());
				if (defaultTiers.containsKey(ent))
					uniqueIDSet.addAll(defaultTiers.get(ent).keySet());
				List<Settings.UpgradeTier> entTier = new ArrayList<>(uniqueIDSet.size());

				uniqueIDSet.forEach(id -> entTier.add(tiers.getOrDefault(id, defaultTiers.get(ent).get(id))));
				tierList.put(ent, entTier);
			});

			defaultTiers.forEach((ent, tiers) -> tierList.putIfAbsent(ent, new ArrayList<>(tiers.values())));
		}

		if (tierList.isEmpty()) {
			return Collections.emptyMap();
		}

		tierList.forEach((ent, tiers) -> tiers.sort(Comparator.comparingInt(Settings.UpgradeTier::getMaxLevel)));

		return tierList;
	}

    /**
     * Retrieves all command upgrade tiers for the specified world.
     *
     * @param world The world for which command upgrade tiers are requested.
     * @return A map of command names to a list of {@link Settings.CommandUpgradeTier}.
     */
	public Map<String, List<Settings.CommandUpgradeTier>> getAllCommandUpgradeTiers(World world) {
		String name = this.addon.getPlugin().getIWM().getAddon(world).map(a -> a.getDescription().getName())
				.orElse(null);
		if (name == null) {
			return Collections.emptyMap();
		}

		Map<String, Map<String, Settings.CommandUpgradeTier>> defaultTiers = this.addon.getSettings()
				.getDefaultCommandUpgradeTierMap();
		Map<String, Map<String, Settings.CommandUpgradeTier>> customAddonTiers = this.addon.getSettings()
				.getAddonCommandUpgradeTierMap(name);

		Map<String, List<Settings.CommandUpgradeTier>> tierList = new TreeMap<>();

		if (customAddonTiers.isEmpty()) {
			defaultTiers.forEach((cmd, tiers) -> tierList.put(cmd, new ArrayList<>(tiers.values())));
		} else {
			customAddonTiers.forEach((cmd, tiers) -> {
				Set<String> uniqueIDSet = new HashSet<>(tiers.keySet());
				if (defaultTiers.containsKey(cmd))
					uniqueIDSet.addAll(defaultTiers.get(cmd).keySet());
				List<Settings.CommandUpgradeTier> cmdTier = new ArrayList<>(uniqueIDSet.size());

				uniqueIDSet.forEach(id -> cmdTier.add(tiers.getOrDefault(id, defaultTiers.get(cmd).get(id))));
				tierList.put(cmd, cmdTier);
			});

			defaultTiers.forEach((cmd, tiers) -> tierList.putIfAbsent(cmd, new ArrayList<>(tiers.values())));
		}

		if (tierList.isEmpty()) {
			return Collections.emptyMap();
		}

		tierList.forEach((cmd, tiers) -> tiers.sort(Comparator.comparingInt(Settings.UpgradeTier::getMaxLevel)));

		return tierList;
	}

    /**
     * Retrieves the tier information for a range upgrade at the specified level and world.
     *
     * @param rangeLevel The level of the range upgrade.
     * @param world The world in which the upgrade is being applied.
     * @return The {@link Settings.UpgradeTier} for the specified level and world, or {@code null} if not found.
     */
	public Settings.UpgradeTier getRangeUpgradeTier(int rangeLevel, World world) {
		List<Settings.UpgradeTier> tierList = this.getAllRangeUpgradeTiers(world);

		if (tierList.isEmpty())
			return null;

		Settings.UpgradeTier rangeUpgradeTier = tierList.get(0);

		if (rangeUpgradeTier.getMaxLevel() < 0)
			return rangeUpgradeTier;

		for (int i = 0; i < tierList.size(); i++) {
			if (rangeLevel <= tierList.get(i).getMaxLevel())
				return tierList.get(i);
		}

		return null;
	}

	/**
	 * Retrieves the tier information for a block limits upgrade at the specified level and world.
	 *
	 * @param mat The material type for the block limits upgrade.
	 * @param limitsLevel The current level of the block limits upgrade.
	 * @param world The world in which the upgrade is being applied.
	 * @return The {@link Settings.UpgradeTier} for the specified level and world, or {@code null} if not found.
	 */
	public Settings.UpgradeTier getBlockLimitsUpgradeTier(Material mat, int limitsLevel, World world) {
		Map<Material, List<Settings.UpgradeTier>> matTierList = this.getAllBlockLimitsUpgradeTiers(world);

		if (matTierList.isEmpty()) {
			return null;
		}

		if (!matTierList.containsKey(mat)) {
			return null;
		}

		List<Settings.UpgradeTier> tierList = matTierList.get(mat);

		for (int i = 0; i < tierList.size(); i++) {
			if (limitsLevel <= tierList.get(i).getMaxLevel())
				return tierList.get(i);
		}
		return null;
	}

	/**
	 * Retrieves the tier information for an entity limits upgrade at the specified level and world.
	 *
	 * @param ent The entity type for the entity limits upgrade.
	 * @param limitsLevel The current level of the entity limits upgrade.
	 * @param world The world in which the upgrade is being applied.
	 * @return The {@link Settings.UpgradeTier} for the specified level and world, or {@code null} if not found.
	 */
	public Settings.UpgradeTier getEntityLimitsUpgradeTier(EntityType ent, int limitsLevel, World world) {
		Map<EntityType, List<Settings.UpgradeTier>> entTierList = this.getAllEntityLimitsUpgradeTiers(world);

		if (entTierList.isEmpty()) {
			return null;
		}

		if (!entTierList.containsKey(ent)) {
			return null;
		}

		List<Settings.UpgradeTier> tierList = entTierList.get(ent);

		for (int i = 0; i < tierList.size(); i++) {
			if (limitsLevel <= tierList.get(i).getMaxLevel())
				return tierList.get(i);
		}

		return null;
	}

	/**
	 * Retrieves the tier information for an entity group limits upgrade at the specified level and world.
	 *
	 * @param group The entity group name for the entity group limits upgrade.
	 * @param limitsLevel The current level of the entity group limits upgrade.
	 * @param world The world in which the upgrade is being applied.
	 * @return The {@link Settings.UpgradeTier} for the specified level and world, or {@code null} if not found.
	 */
	public Settings.UpgradeTier getEntityGroupLimitsUpgradeTier(String group, int limitsLevel, World world) {
		Map<String, List<Settings.UpgradeTier>> entTierList = this.getAllEntityGroupLimitsUpgradeTiers(world);

		if (entTierList.isEmpty()) {
			return null;
		}

		if (!entTierList.containsKey(group)) {
			return null;
		}

		List<Settings.UpgradeTier> tierList = entTierList.get(group);

		for (int i = 0; i < tierList.size(); i++) {
			if (limitsLevel <= tierList.get(i).getMaxLevel())
				return tierList.get(i);
		}

		return null;
	}

	/**
	 * Retrieves the tier information for a command upgrade at the specified level and world.
	 *
	 * @param cmd The command name for the command upgrade.
	 * @param cmdLevel The current level of the command upgrade.
	 * @param world The world in which the upgrade is being applied.
	 * @return The {@link Settings.CommandUpgradeTier} for the specified level and world, or {@code null} if not found.
	 */
	public Settings.CommandUpgradeTier getCommandUpgradeTier(String cmd, int cmdLevel, World world) {
		Map<String, List<Settings.CommandUpgradeTier>> cmdTierList = this.getAllCommandUpgradeTiers(world);

		if (cmdTierList.isEmpty()) {
			return null;
		}

		if (!cmdTierList.containsKey(cmd)) {
			return null;
		}

		List<Settings.CommandUpgradeTier> tierList = cmdTierList.get(cmd);

		for (int i = 0; i < tierList.size(); i++) {
			if (cmdLevel <= tierList.get(i).getMaxLevel())
				return tierList.get(i);
		}

		return null;
	}

    /**
     * Retrieves detailed information about the range upgrade tier, such as costs and effects.
     *
     * @param rangeLevel The level of the range upgrade.
     * @param islandLevel The level of the island.
     * @param numberPeople The number of people on the island.
     * @param world The world in which the upgrade is being applied.
     * @return A map containing information about the range upgrade tier.
     */
	public Map<String, Integer> getRangeUpgradeInfos(int rangeLevel, int islandLevel, int numberPeople, World world) {
		Settings.UpgradeTier rangeUpgradeTier = this.getRangeUpgradeTier(rangeLevel, world);

		if (rangeUpgradeTier == null)
			return null;

		Map<String, Integer> info = new TreeMap<>();

		info.put("islandMinLevel",
				(int) rangeUpgradeTier.calculateIslandMinLevel(rangeLevel, islandLevel, numberPeople));
		info.put("vaultCost", (int) rangeUpgradeTier.calculateVaultCost(rangeLevel, islandLevel, numberPeople));
		info.put("upgrade", (int) rangeUpgradeTier.calculateUpgrade(rangeLevel, islandLevel, numberPeople));

		return info;
	}

	/**
	 * Retrieves the permission level required for a range upgrade at the specified level.
	 *
	 * @param rangeLevel The level of the range upgrade.
	 * @param world The world in which the upgrade is being applied.
	 * @return The permission level required, or 0 if not found.
	 */
	public int getRangePermissionLevel(int rangeLevel, World world) {
		Settings.UpgradeTier rangeUpgradeTier = this.getRangeUpgradeTier(rangeLevel, world);

		if (rangeUpgradeTier == null)
			return 0;
		return rangeUpgradeTier.getPermissionLevel();
	}

	/**
	 * Retrieves the tier name for a range upgrade at the specified level.
	 *
	 * @param rangeLevel The level of the range upgrade.
	 * @param world The world in which the upgrade is being applied.
	 * @return The tier name, or {@code null} if not found.
	 */
	public String getRangeUpgradeTierName(int rangeLevel, World world) {
		Settings.UpgradeTier rangeUpgradeTier = this.getRangeUpgradeTier(rangeLevel, world);

		if (rangeUpgradeTier == null)
			return null;
		return rangeUpgradeTier.getTierName();
	}

	/**
	 * Retrieves the maximum level for range upgrades in the specified world.
	 *
	 * @param world The world to check.
	 * @return The maximum range upgrade level.
	 */
	public int getRangeUpgradeMax(World world) {
		String name = this.addon.getPlugin().getIWM().getAddon(world).map(a -> a.getDescription().getName())
				.orElse(null);
		return this.addon.getSettings().getMaxRangeUpgrade(name);
	}

	/**
	 * Retrieves detailed information about the block limits upgrade tier, such as costs and effects.
	 *
	 * @param mat The material type for the block limits upgrade.
	 * @param limitsLevel The level of the block limits upgrade.
	 * @param islandLevel The level of the island.
	 * @param numberPeople The number of people on the island.
	 * @param world The world in which the upgrade is being applied.
	 * @return A map containing information about the block limits upgrade tier, or {@code null} if not found.
	 */
	public Map<String, Integer> getBlockLimitsUpgradeInfos(Material mat, int limitsLevel, int islandLevel,
			int numberPeople, World world) {
		Settings.UpgradeTier limitsUpgradeTier = this.getBlockLimitsUpgradeTier(mat, limitsLevel, world);
		if (limitsUpgradeTier == null) {
			return null;
		}

		Map<String, Integer> info = new TreeMap<>();

		info.put("islandMinLevel",
				(int) limitsUpgradeTier.calculateIslandMinLevel(limitsLevel, islandLevel, numberPeople));
		info.put("vaultCost", (int) limitsUpgradeTier.calculateVaultCost(limitsLevel, islandLevel, numberPeople));
		info.put("upgrade", (int) limitsUpgradeTier.calculateUpgrade(limitsLevel, islandLevel, numberPeople));

		return info;
	}

	/**
	 * Retrieves the permission level required for a block limits upgrade at the specified level.
	 *
	 * @param mat The material type for the block limits upgrade.
	 * @param limitsLevel The level of the block limits upgrade.
	 * @param world The world in which the upgrade is being applied.
	 * @return The permission level required, or 0 if not found.
	 */
	public int getBlockLimitsPermissionLevel(Material mat, int limitsLevel, World world) {
		Settings.UpgradeTier limitsUpgradeTier = this.getBlockLimitsUpgradeTier(mat, limitsLevel, world);

		if (limitsUpgradeTier == null)
			return 0;
		return limitsUpgradeTier.getPermissionLevel();
	}

	/**
	 * Retrieves the tier name for a block limits upgrade at the specified level.
	 *
	 * @param mat The material type for the block limits upgrade.
	 * @param limitsLevel The level of the block limits upgrade.
	 * @param world The world in which the upgrade is being applied.
	 * @return The tier name, or {@code null} if not found.
	 */
	public String getBlockLimitsUpgradeTierName(Material mat, int limitsLevel, World world) {
		Settings.UpgradeTier limitsUpgradeTier = this.getBlockLimitsUpgradeTier(mat, limitsLevel, world);

		if (limitsUpgradeTier == null)
			return null;
		return limitsUpgradeTier.getTierName();
	}

	/**
	 * Retrieves the maximum level for block limits upgrades for a specific material in the specified world.
	 *
	 * @param mat The material type for the block limits upgrade.
	 * @param world The world to check.
	 * @return The maximum block limits upgrade level for the material.
	 */
	public int getBlockLimitsUpgradeMax(Material mat, World world) {
		String name = this.addon.getPlugin().getIWM().getAddon(world).map(a -> a.getDescription().getName())
				.orElse(null);
		return this.addon.getSettings().getMaxBlockLimitsUpgrade(mat, name);
	}

	/**
	 * Retrieves detailed information about the entity limits upgrade tier, such as costs and effects.
	 *
	 * @param ent The entity type for the entity limits upgrade.
	 * @param limitsLevel The level of the entity limits upgrade.
	 * @param islandLevel The level of the island.
	 * @param numberPeople The number of people on the island.
	 * @param world The world in which the upgrade is being applied.
	 * @return A map containing information about the entity limits upgrade tier, or {@code null} if not found.
	 */
	public Map<String, Integer> getEntityLimitsUpgradeInfos(EntityType ent, int limitsLevel, int islandLevel,
			int numberPeople, World world) {
		Settings.UpgradeTier limitsUpgradeTier = this.getEntityLimitsUpgradeTier(ent, limitsLevel, world);
		if (limitsUpgradeTier == null) {
			return null;
		}

		Map<String, Integer> info = new TreeMap<>();

		info.put("islandMinLevel",
				(int) limitsUpgradeTier.calculateIslandMinLevel(limitsLevel, islandLevel, numberPeople));
		info.put("vaultCost", (int) limitsUpgradeTier.calculateVaultCost(limitsLevel, islandLevel, numberPeople));
		info.put("upgrade", (int) limitsUpgradeTier.calculateUpgrade(limitsLevel, islandLevel, numberPeople));

		return info;
	}

	/**
	 * Retrieves detailed information about the entity group limits upgrade tier, such as costs and effects.
	 *
	 * @param group The entity group name for the entity group limits upgrade.
	 * @param limitsLevel The level of the entity group limits upgrade.
	 * @param islandLevel The level of the island.
	 * @param numberPeople The number of people on the island.
	 * @param world The world in which the upgrade is being applied.
	 * @return A map containing information about the entity group limits upgrade tier, or {@code null} if not found.
	 */
	public Map<String, Integer> getEntityGroupLimitsUpgradeInfos(String group, int limitsLevel, int islandLevel,
			int numberPeople, World world) {
		Settings.UpgradeTier limitsUpgradeTier = this.getEntityGroupLimitsUpgradeTier(group, limitsLevel, world);
		if (limitsUpgradeTier == null) {
			return null;
		}

		Map<String, Integer> info = new TreeMap<>();

		info.put("islandMinLevel",
				(int) limitsUpgradeTier.calculateIslandMinLevel(limitsLevel, islandLevel, numberPeople));
		info.put("vaultCost", (int) limitsUpgradeTier.calculateVaultCost(limitsLevel, islandLevel, numberPeople));
		info.put("upgrade", (int) limitsUpgradeTier.calculateUpgrade(limitsLevel, islandLevel, numberPeople));

		return info;
	}

	/**
	 * Retrieves the permission level required for an entity limits upgrade at the specified level.
	 *
	 * @param ent The entity type for the entity limits upgrade.
	 * @param limitsLevel The level of the entity limits upgrade.
	 * @param world The world in which the upgrade is being applied.
	 * @return The permission level required, or 0 if not found.
	 */
	public int getEntityLimitsPermissionLevel(EntityType ent, int limitsLevel, World world) {
		Settings.UpgradeTier limitsUpgradeTier = this.getEntityLimitsUpgradeTier(ent, limitsLevel, world);

		if (limitsUpgradeTier == null)
			return 0;
		return limitsUpgradeTier.getPermissionLevel();
	}

	/**
	 * Retrieves the permission level required for an entity group limits upgrade at the specified level.
	 *
	 * @param group The entity group name for the entity group limits upgrade.
	 * @param limitsLevel The level of the entity group limits upgrade.
	 * @param world The world in which the upgrade is being applied.
	 * @return The permission level required, or 0 if not found.
	 */
	public int getEntityGroupLimitsPermissionLevel(String group, int limitsLevel, World world) {
		Settings.UpgradeTier limitsUpgradeTier = this.getEntityGroupLimitsUpgradeTier(group, limitsLevel, world);

		if (limitsUpgradeTier == null)
			return 0;
		return limitsUpgradeTier.getPermissionLevel();
	}

	/**
	 * Retrieves the tier name for an entity limits upgrade at the specified level.
	 *
	 * @param ent The entity type for the entity limits upgrade.
	 * @param limitsLevel The level of the entity limits upgrade.
	 * @param world The world in which the upgrade is being applied.
	 * @return The tier name, or {@code null} if not found.
	 */
	public String getEntityLimitsUpgradeTierName(EntityType ent, int limitsLevel, World world) {
		Settings.UpgradeTier limitsUpgradeTier = this.getEntityLimitsUpgradeTier(ent, limitsLevel, world);

		if (limitsUpgradeTier == null)
			return null;
		return limitsUpgradeTier.getTierName();
	}

	/**
	 * Retrieves the tier name for an entity group limits upgrade at the specified level.
	 *
	 * @param group The entity group name for the entity group limits upgrade.
	 * @param limitsLevel The level of the entity group limits upgrade.
	 * @param world The world in which the upgrade is being applied.
	 * @return The tier name, or {@code null} if not found.
	 */
	public String getEntityGroupLimitsUpgradeTierName(String group, int limitsLevel, World world) {
		Settings.UpgradeTier limitsUpgradeTier = this.getEntityGroupLimitsUpgradeTier(group, limitsLevel, world);

		if (limitsUpgradeTier == null)
			return null;
		return limitsUpgradeTier.getTierName();
	}

	/**
	 * Retrieves the maximum level for entity limits upgrades for a specific entity type in the specified world.
	 *
	 * @param ent The entity type for the entity limits upgrade.
	 * @param world The world to check.
	 * @return The maximum entity limits upgrade level for the entity type.
	 */
	public int getEntityLimitsUpgradeMax(EntityType ent, World world) {
		String name = this.addon.getPlugin().getIWM().getAddon(world).map(a -> a.getDescription().getName())
				.orElse(null);
		return this.addon.getSettings().getMaxEntityLimitsUpgrade(ent, name);
	}

	/**
	 * Retrieves the maximum level for entity group limits upgrades for a specific group in the specified world.
	 *
	 * @param group The entity group name for the entity group limits upgrade.
	 * @param world The world to check.
	 * @return The maximum entity group limits upgrade level for the group.
	 */
	public int getEntityGroupLimitsUpgradeMax(String group, World world) {
		String name = this.addon.getPlugin().getIWM().getAddon(world).map(a -> a.getDescription().getName())
				.orElse(null);
		return this.addon.getSettings().getMaxEntityGroupLimitsUpgrade(group, name);
	}

	/**
	 * Retrieves detailed information about the command upgrade tier, such as costs and effects.
	 *
	 * @param cmd The command name for the command upgrade.
	 * @param cmdLevel The level of the command upgrade.
	 * @param islandLevel The level of the island.
	 * @param numberPeople The number of people on the island.
	 * @param world The world in which the upgrade is being applied.
	 * @return A map containing information about the command upgrade tier, or {@code null} if not found.
	 */
	public Map<String, Integer> getCommandUpgradeInfos(String cmd, int cmdLevel, int islandLevel, int numberPeople,
			World world) {
		Settings.CommandUpgradeTier cmdUpgradeTier = this.getCommandUpgradeTier(cmd, cmdLevel, world);
		if (cmdUpgradeTier == null) {
			return null;
		}

		Map<String, Integer> info = new TreeMap<>();

		info.put("islandMinLevel", (int) cmdUpgradeTier.calculateIslandMinLevel(cmdLevel, islandLevel, numberPeople));
		info.put("vaultCost", (int) cmdUpgradeTier.calculateVaultCost(cmdLevel, islandLevel, numberPeople));
		info.put("upgrade", (int) cmdUpgradeTier.calculateUpgrade(cmdLevel, islandLevel, numberPeople));

		return info;
	}

	/**
	 * Retrieves the permission level required for a command upgrade at the specified level.
	 *
	 * @param cmd The command name for the command upgrade.
	 * @param cmdLevel The level of the command upgrade.
	 * @param world The world in which the upgrade is being applied.
	 * @return The permission level required, or 0 if not found.
	 */
	public int getCommandPermissionLevel(String cmd, int cmdLevel, World world) {
		Settings.CommandUpgradeTier cmdUpgradeTier = this.getCommandUpgradeTier(cmd, cmdLevel, world);

		if (cmdUpgradeTier == null)
			return 0;
		return cmdUpgradeTier.getPermissionLevel();
	}

	/**
	 * Retrieves the tier name for a command upgrade at the specified level.
	 *
	 * @param cmd The command name for the command upgrade.
	 * @param cmdLevel The level of the command upgrade.
	 * @param world The world in which the upgrade is being applied.
	 * @return The tier name, or {@code null} if not found.
	 */
	public String getCommandUpgradeTierName(String cmd, int cmdLevel, World world) {
		Settings.CommandUpgradeTier cmdUpgradeTier = this.getCommandUpgradeTier(cmd, cmdLevel, world);

		if (cmdUpgradeTier == null)
			return null;
		return cmdUpgradeTier.getTierName();
	}

	/**
	 * Retrieves the maximum level for command upgrades for a specific command in the specified world.
	 *
	 * @param cmd The command name for the command upgrade.
	 * @param world The world to check.
	 * @return The maximum command upgrade level for the command.
	 */
	public int getCommandUpgradeMax(String cmd, World world) {
		String name = this.addon.getPlugin().getIWM().getAddon(world).map(a -> a.getDescription().getName())
				.orElse(null);
		return this.addon.getSettings().getMaxCommandUpgrade(cmd, name);
	}

	/**
	 * Retrieves the list of commands to execute for a command upgrade.
	 *
	 * @param cmd The command name for the command upgrade.
	 * @param cmdLevel The level of the command upgrade.
	 * @param island The island on which the upgrade is being applied.
	 * @param playerName The name of the player purchasing the upgrade.
	 * @return A list of commands to execute, or an empty list if not found.
	 */
	public List<String> getCommandList(String cmd, int cmdLevel, Island island, String playerName) {
		Settings.CommandUpgradeTier cmdUpgradeTier = this.getCommandUpgradeTier(cmd, cmdLevel, island.getWorld());

		if (cmdUpgradeTier == null)
			return Collections.emptyList();
		return cmdUpgradeTier.getCommandList(playerName, island, cmdLevel);
	}

	/**
	 * Checks if the command upgrade should be executed from the console.
	 *
	 * @param cmd The command name for the command upgrade.
	 * @param cmdLevel The level of the command upgrade.
	 * @param world The world in which the upgrade is being applied.
	 * @return {@code true} if the command should be executed from console, {@code false} otherwise.
	 */
	public Boolean isCommantConsole(String cmd, int cmdLevel, World world) {
		Settings.CommandUpgradeTier cmdUpgradeTier = this.getCommandUpgradeTier(cmd, cmdLevel, world);

		if (cmdUpgradeTier == null)
			return false;
		return cmdUpgradeTier.getConsole();
	}

    /**
     * Retrieves the current entity limits for the specified island.
     *
     * @param island The island for which entity limits are requested.
     * @return A map of {@link EntityType} to their respective limits.
     */
	public Map<EntityType, Integer> getEntityLimits(Island island) {
		if (!this.addon.isLimitsProvided())
			return Collections.emptyMap();

		Map<EntityType, Integer> entityLimits = new TreeMap<>(this.addon.getLimitsAddon().getSettings().getLimits());
		IslandBlockCount ibc = this.addon.getLimitsAddon().getBlockLimitListener().getIsland(island.getUniqueId());
		if (ibc != null)
			ibc.getEntityLimits().forEach(entityLimits::put);
		return entityLimits;
	}

    /**
     * Retrieves the current entity group limits for the specified island.
     *
     * @param island The island for which entity group limits are requested.
     * @return A map of group names to their respective limits.
     */
	public Map<String, Integer> getEntityGroupLimits(Island island) {
		if (!this.addon.isLimitsProvided())
			return Collections.emptyMap();

		Map<String, Integer> entityGroupLimits = new TreeMap<>(
				this.addon.getLimitsAddon().getSettings().getGroupLimits().values().stream().flatMap(e -> e.stream())
						.distinct().collect(Collectors.toMap(e -> e.getName(), e -> e.getLimit())));
		IslandBlockCount ibc = this.addon.getLimitsAddon().getBlockLimitListener().getIsland(island.getUniqueId());
		if (ibc != null)
			ibc.getEntityGroupLimits().forEach(entityGroupLimits::put);
		return entityGroupLimits;
	}

	/**
	 * The UpgradesAddon instance this manager is associated with.
	 */
	private UpgradesAddon addon;

	/**
	 * Set of game mode names that this manager has successfully hooked into.
	 */
	private Set<String> hookedGameModes;

}
