package world.bentobox.upgrades;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.World;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.Database;
import world.bentobox.upgrades.dataobjects.UpgradeData;
import world.bentobox.upgrades.dataobjects.UpgradeTier;

public class UpgradesDataManager {
	
	// ------------------------------------------------------------
	// Section: Constant
	// ------------------------------------------------------------
	
	private static final String WHAT = "[what]";
	private static final String UPGRADEID = "[upgradeId]";
	
	// ------------------------------------------------------------
	// Section: Variables
	// ------------------------------------------------------------
	
	private UpgradesAddon addon;
	
	private Database<UpgradeData> databaseUpgradeData;
	
	private Database<UpgradeTier> databaseUpgradeTier;
	
	private Map<String, UpgradeData> upgradeDataCache;
	
	private Map<String, UpgradeTier> upgradeTierCache;
	
	// ------------------------------------------------------------
	// Section: Constructor
	// ------------------------------------------------------------
	
	public UpgradesDataManager(UpgradesAddon addon) {
		this.addon = addon;
		
		this.addon.log("Loading upgrade data");
		
		this.databaseUpgradeData = new Database<UpgradeData>(this.addon, UpgradeData.class);
		this.databaseUpgradeTier = new Database<UpgradeTier>(this.addon, UpgradeTier.class);
		this.upgradeDataCache = new HashMap<String, UpgradeData>();
		this.upgradeTierCache = new HashMap<String, UpgradeTier>();
		
		this.load();
	}
	
	// ------------------------------------------------------------
	// Section: Utils methods
	// ------------------------------------------------------------
	
	public void saveAll() {
		this.upgradeDataCache.values().forEach(this.databaseUpgradeData::saveObjectAsync);
		this.upgradeTierCache.values().forEach(this.databaseUpgradeTier::saveObjectAsync);
		this.validate();
	}
	
	public void reload() {
		this.addon.log("Reloading upgrade data");
		this.load();
	}
	
	// ------------------------------------------------------------
	// Section: Comparators
	// ------------------------------------------------------------
	
	private final Comparator<UpgradeData> upgradeDataComparator = (upgrade1, upgrade2) -> {
		if (upgrade1.getOrder() == upgrade2.getOrder()) {
			return upgrade1.getName().compareToIgnoreCase(upgrade2.getName());
		} else {
			if (upgrade1.getOrder() < 0 || upgrade2.getOrder() < 0)
				return Boolean.compare(upgrade1.getOrder() < 0, upgrade2.getOrder() < 0);
			return Integer.compare(upgrade1.getOrder(), upgrade2.getOrder());
		}
	};
	
	private final Comparator<UpgradeTier> upgradeTierComparator = (upgrade1, upgrade2) -> {
		return Integer.compare(upgrade1.getStartLevel(), upgrade2.getStartLevel());
	};
	
	// ------------------------------------------------------------
	// Section: Loading
	// ------------------------------------------------------------
	
	private void load() {
		this.databaseUpgradeData.loadObjects().forEach(this::loadUpgradeData);
		this.databaseUpgradeTier.loadObjects().forEach(this::loadUpgradeTier);
	}
	
	// ------------------------------------------------------------
	// Section: Loading / UpgradeData
	// ------------------------------------------------------------
	
	private boolean loadUpgradeData(UpgradeData upgrade) {
		return this.loadUpgradeData(upgrade, true, null);
	}
	
	public boolean loadUpgradeData(UpgradeData upgrade, boolean overwrite, User user) {
		if (upgrade == null) {
			if (user != null)
				user.sendMessage("upgrades.error.loaderror", WHAT, "Upgrade data");
			this.addon.logWarning("Couldn't load upgrade data from database");
			return false;
		}
		if (!upgrade.isValid()) {
			if (user != null)
				user.sendMessage("upgrades.error.upgradeinvalid",
						UPGRADEID, upgrade.getUniqueId(),
						WHAT, "data");
			this.addon.logWarning("Data for upgrade data " + upgrade.getUniqueId() + " is invalid. You should look in the database");
			return false;
		}
		if (this.upgradeDataCache.containsKey(upgrade.getUniqueId())) {
			if (!overwrite) {
				if (user != null)
					user.sendMessage("upgrades.message.skipupgradeload",
							UPGRADEID, upgrade.getUniqueId(),
							WHAT, "data");
				this.addon.logWarning("Tried to load " + upgrade.getUniqueId() + " but it was already loaded");
				return false;
			}
		}
		this.upgradeDataCache.put(upgrade.getUniqueId(), upgrade);
		if (user != null) {
			user.sendMessage("upgrades.message.upgradeload",
					UPGRADEID, upgrade.getUniqueId(),
					WHAT, "data");
		}
		this.addon.log("Upgrade data " + upgrade.getUniqueId() + " was loaded");
		return true;
	}
	
	// ------------------------------------------------------------
	// Section: Loading / UpgradeTier
	// ------------------------------------------------------------
	
	private boolean loadUpgradeTier(UpgradeTier upgade) {
		return this.loadUpgradeTier(upgade, true, null);
	}
	
	public boolean loadUpgradeTier(UpgradeTier upgrade, boolean overwrite, User user) {
		if (upgrade == null) {
			if (user != null)
				user.sendMessage("upgrades.error.loaderror", WHAT, "Upgrade tier");
			this.addon.logWarning("Couldn't load upgrade tier from database");
			return false;
		}
		if (!upgrade.isValid()) {
			if (user != null)
				user.sendMessage("upgrades.error.upgradeinvalid",
						UPGRADEID, upgrade.getUniqueId(),
						WHAT, "tier");
			this.addon.logWarning("Data for upgrade tier " + upgrade.getUniqueId() + " is invalid. You should look in the database");
			return false;
		}
		if (this.upgradeTierCache.containsKey(upgrade.getUniqueId())) {
			if (!overwrite) {
				if (user != null)
					user.sendMessage("upgrades.message.skipupgradeload",
							UPGRADEID, upgrade.getUniqueId(),
							WHAT, "tier");
				this.addon.logWarning("Tried to load " + upgrade.getUniqueId() + " but it was already loaded");
				return false;
			}
		}
		this.upgradeTierCache.put(upgrade.getUniqueId(), upgrade);
		if (user != null) {
			user.sendMessage("upgrades.message.upgradeload",
					UPGRADEID, upgrade.getUniqueId(),
					WHAT, "tier");
		}
		this.addon.log("Upgrade tier " + upgrade.getUniqueId() + " was loaded");
		return true;
	}
	
	// ------------------------------------------------------------
	// Section: Validate loading
	// ------------------------------------------------------------
	
	private void validate() {
		this.validateUpgradeTier();
		this.validateUpgradeData();
	}
	
	private void validateUpgradeTier() {
		this.upgradeTierCache.values().forEach(tier -> {
			if (!this.upgradeDataCache.containsKey(tier.getUpgrade())) {
				this.addon.logWarning("Upgrade tier " + tier.getUniqueId() + " has a reference to an unknow upgrade data. It will be skiped");
				this.upgradeTierCache.remove(tier.getUniqueId());
			}
		});
	}
	
	private void validateUpgradeData() {
		this.upgradeDataCache.values().forEach(upgrade -> {
			upgrade.getTiers().forEach(tier -> {
				if (!this.upgradeTierCache.containsKey(tier)) {
					this.addon.logWarning("Upgrade data " + upgrade.getUniqueId() + " has a reference to an unknow upgrade tier. It will be skiped");
					this.upgradeDataCache.remove(upgrade).getUniqueId();
				}
			});
		});
	}
	
	// ------------------------------------------------------------
	// Section: Getting UpgradeData
	// ------------------------------------------------------------
	
	public List<UpgradeData> getUpgradeDataByGameMode(World world) {
		return this.addon.getPlugin().getIWM().getAddon(world)
				.map(gameMode -> this.getUpgradeDataByGameMode(gameMode.getDescription().getName()))
				.orElse(Collections.emptyList());
	}
	
	public List<UpgradeData> getUpgradeDataByGameMode(String world) {
		return this.upgradeDataCache.values().stream()
				.filter(upgrade -> upgrade.getWorld() == world)
				.sorted(this.upgradeDataComparator)
				.collect(Collectors.toList());
	}
	
	// ------------------------------------------------------------
	// Section: Getting UpgradeTier
	// ------------------------------------------------------------
	
	public List<UpgradeTier> getUpgradeTierByUpgradeData(UpgradeData upgradeData) {
		return this.getUpgradeTierByUpgradeData(upgradeData.getUniqueId());
	}
	
	public List<UpgradeTier> getUpgradeTierByUpgradeData(String upgradeDataId) {
		return this.upgradeTierCache.values().stream()
				.filter(upgrade -> upgrade.getUpgrade() == upgradeDataId)
				.sorted(this.upgradeTierComparator)
				.collect(Collectors.toList());
	}

}
