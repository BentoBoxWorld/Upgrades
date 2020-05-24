package world.bentobox.upgrades;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.World;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.upgrades.config.Settings;

public class UpgradesManager {

	public UpgradesManager(UpgradesAddon addon) {
		this.addon = addon;
		this.hookedGameModes = new HashSet<>();
	}
	
	protected void addGameModes(List<String> gameModes) {
		this.hookedGameModes.addAll(gameModes);
	}
	
	public boolean canOperateInWorld(World world) {
		Optional<GameModeAddon> addon = this.addon.getPlugin().getIWM().getAddon(world);
		
		return addon.isPresent() && this.hookedGameModes.contains(addon.get().getDescription().getName());
	}
	
	public int getIslandLevel(Island island) {
		if (!this.addon.isLevelProvided())
			return 0;
		
		return (int) this.addon.getLevelAddon().getIslandLevel(island.getWorld(), island.getOwner());
	}
	
	public List<Settings.UpgradeTier> getAllRangeUpgradeTiers(World world) {
		String name = this.addon.getPlugin().getIWM().getAddon(world).map(a -> a.getDescription().getName()).orElse(null);
		if (name == null) return Collections.emptyList();
		
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
	
	public Map<Material, List<Settings.UpgradeTier>> getAllLimitsUpgradeTiers(World world) {
		String name = this.addon.getPlugin().getIWM().getAddon(world).map(a -> a.getDescription().getName()).orElse(null);
		if (name == null)
			return Collections.emptyMap();
		
		Map<Material, Map<String, Settings.UpgradeTier>> defaultTiers = this.addon.getSettings().getDefaultLimitsUpgradeTierMap();
		Map<Material, Map<String, Settings.UpgradeTier>> customAddonTiers = this.addon.getSettings().getAddonLimitsUpgradeTierMap(name);
		
		Map<Material, List<Settings.UpgradeTier>> tierList = new EnumMap<>(Material.class);
		
		if (customAddonTiers.isEmpty())
			defaultTiers.forEach((mat, tiers) -> tierList.put(mat, new ArrayList<>(tiers.values())));
		else {
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
		
		if (tierList.isEmpty())
			return Collections.emptyMap();
		
		tierList.forEach((mat, tiers) -> tiers.sort(Comparator.comparingInt(Settings.UpgradeTier::getMaxLevel)));
		
		return tierList;
	}
	
	public Settings.UpgradeTier getRangeUpgradeTier(int rangeLevel, World world) {
		List<Settings.UpgradeTier> tierList = this.getAllRangeUpgradeTiers(world);
		
		if (tierList.isEmpty())
			return null;
		
		Settings.UpgradeTier rangeUpgradeTier = tierList.get(0);
		
		if (rangeUpgradeTier.getMaxLevel() < 0)
			return rangeUpgradeTier;
		
		for (int i = 0; i < tierList.size(); i++) {
			if (rangeLevel < tierList.get(i).getMaxLevel())
				return tierList.get(i);
		}
		
		return null;
	}
	
	public Settings.UpgradeTier getLimitsUpgradeTier(Material mat, int limitsLevel, World world) {
		Map<Material, List<Settings.UpgradeTier>> matTierList = this.getAllLimitsUpgradeTiers(world);
		
		if (matTierList.isEmpty())
			return null;
		
		if (!matTierList.containsKey(mat))
			return null;
		
		List<Settings.UpgradeTier> tierList = matTierList.get(mat);
		
		for (int i = 0; i < tierList.size(); i++) {
			if (limitsLevel < tierList.get(i).getMaxLevel())
				return tierList.get(i);
		}
		
		return null;
	}
	
	public Map<String, Integer> getRangeUpgradeInfos(int rangeLevel, int islandLevel, int numberPeople, World world) {
		Settings.UpgradeTier rangeUpgradeTier = this.getRangeUpgradeTier(rangeLevel, world);
		
		if (rangeUpgradeTier == null)
			return null;
		
		Map<String, Integer> info = new HashMap<>();
		
		info.put("islandMinLevel", (int) rangeUpgradeTier.calculateIslandMinLevel(rangeLevel, islandLevel, numberPeople));
		info.put("vaultCost", (int) rangeUpgradeTier.calculateVaultCost(rangeLevel, islandLevel, numberPeople));
		info.put("upgradeRange", (int) rangeUpgradeTier.calculateUpgrade(rangeLevel, islandLevel, numberPeople));
		
		return info;
	}
	
	public Map<String, Integer> getLimitsUpgradeInfos(Material mat, int limitsLevel, int islandLevel, int numberPeople, World world) {
		Settings.UpgradeTier limitsUpgradeTier = this.getLimitsUpgradeTier(mat, limitsLevel, world);
		
		if (limitsUpgradeTier == null)
			return null;
		
		Map<String, Integer> info = new HashMap<>();
		
		info.put("islandMinLevel", (int) limitsUpgradeTier.calculateIslandMinLevel(limitsLevel, islandLevel, numberPeople));
		info.put("vaultCost", (int) limitsUpgradeTier.calculateVaultCost(limitsLevel, islandLevel, numberPeople));
		info.put("upgradeRange", (int) limitsUpgradeTier.calculateUpgrade(limitsLevel, islandLevel, numberPeople));
		
		return info;
	}
	
	private UpgradesAddon addon;
	
	private Set<String> hookedGameModes;
	
}
