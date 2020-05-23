package world.bentobox.upgrades;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
	
	public long getIslandLevel(Island island) {
		if (!this.addon.isLevelProvided())
			return 0L;
		
		return this.addon.getLevelAddon().getIslandLevel(island.getWorld(), island.getOwner());
	}
	
	public List<Settings.RangeUpgradeTier> getAllRangeUpgradeTiers(World world) {
		String name = this.addon.getPlugin().getIWM().getAddon(world).map(a -> a.getDescription().getName()).orElse(null);
		if (name == null) return Collections.emptyList();
		
		Map<String, Settings.RangeUpgradeTier> defaultTiers = this.addon.getSettings().getDefaultRangeUpgradeTierMap();
		Map<String, Settings.RangeUpgradeTier> customAddonTiers = this.addon.getSettings().getAddonRangeUpgradeTierMap(name);
		
		List<Settings.RangeUpgradeTier> tierList;
		
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
		
		tierList.sort(Comparator.comparingInt(Settings.RangeUpgradeTier::getMaxLevel));
		
		return tierList;
	}
	
	public Settings.RangeUpgradeTier getRangeUpgradeTier(long rangeLevel, World world) {
		List<Settings.RangeUpgradeTier> tierList = this.getAllRangeUpgradeTiers(world);
		
		if (tierList.isEmpty())
			return null;
		
		Settings.RangeUpgradeTier rangeUpgradeTier = tierList.get(0);
		
		if (rangeUpgradeTier.getMaxLevel() < 0)
			return rangeUpgradeTier;
		
		for (int i = 0; i < tierList.size(); i++) {
			if (rangeLevel < tierList.get(i).getMaxLevel())
				return tierList.get(i);
		}
		
		return null;
	}
	
	public Map<String, Integer> getRangeUpgradeInfos(long rangeLevel, long islandLevel, long numberPeople, World world) {
		Settings.RangeUpgradeTier rangeUpgradeTier = this.getRangeUpgradeTier(rangeLevel, world);
		
		if (rangeUpgradeTier == null)
			return null;
		
		Map<String, Integer> info = new HashMap<>();
		
		info.put("islandMinLevel", (int) rangeUpgradeTier.calculateIslandMinLevel(rangeLevel, islandLevel, numberPeople));
		info.put("vaultCost", (int) rangeUpgradeTier.calculateVaultCost(rangeLevel, islandLevel, numberPeople));
		info.put("upgradeRange", (int) rangeUpgradeTier.calculateUpgradeRange(rangeLevel, islandLevel, numberPeople));
		
		return info;
	}
	
	private UpgradesAddon addon;
	
	private Set<String> hookedGameModes;
	
}
