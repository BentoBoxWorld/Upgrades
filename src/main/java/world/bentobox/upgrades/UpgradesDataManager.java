package world.bentobox.upgrades;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.bukkit.World;

import com.mongodb.lang.NonNull;
import com.mongodb.lang.Nullable;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.Database;
import world.bentobox.upgrades.dataobjects.UpgradeData;
import world.bentobox.upgrades.dataobjects.UpgradeTier;

public class UpgradesDataManager {
	
	// ------------------------------------------------------------
	// Section: Constants
	// ------------------------------------------------------------
	
	/**
	 * Constants for storing placeholders of lang messages
	 */
	private static final String WHAT = "[what]";
	private static final String UPGRADEID = "[upgradeId]";
	
	// ------------------------------------------------------------
	// Section: Variables
	// ------------------------------------------------------------
	
	private UpgradesAddon addon;
	
	/**
	 * Database variables
	 */
	private Database<UpgradeData> databaseUpgradeData;
	private Database<UpgradeTier> databaseUpgradeTier;
	
	/**
	 * Cache. Should be filed at load time
	 */
	private Map<String, UpgradeData> upgradeDataCache;
	private Map<String, UpgradeTier> upgradeTierCache;
	
	// ------------------------------------------------------------
	// Section: Constructor
	// ------------------------------------------------------------
	
	/**
	 * Init UpgradesDataManager
	 * 
	 * @param addon
	 */
	public UpgradesDataManager(UpgradesAddon addon) {
		this.addon = addon;
		
		this.addon.log("Loading upgrade data");
		
		// Init database and cache
		this.databaseUpgradeData = new Database<UpgradeData>(this.addon, UpgradeData.class);
		this.databaseUpgradeTier = new Database<UpgradeTier>(this.addon, UpgradeTier.class);
		this.upgradeDataCache = new HashMap<String, UpgradeData>();
		this.upgradeTierCache = new HashMap<String, UpgradeTier>();
		
		// Load cache
		this.load();
	}
	
	// ------------------------------------------------------------
	// Section: Utils methods
	// ------------------------------------------------------------
	
	/**
	 * This function reload the cache
	 */
	public void reload() {
		this.addon.log("Reloading upgrade data");
		// Load cache
		// Missing the clear of old cache. Only override
		this.load();
	}
	
	public void disable() {
		this.addon.log("Saving upgrade data");
		this.saveAll();
	}
	
	/**
	 * Check if uniqueId is in upgradeData cache
	 * 
	 * @param uniqueId to search
	 * @return if uniqueId was found
	 */
	public boolean hasUpgradeData(String uniqueId) {
		return this.getUpgradeDataById(uniqueId) != null;
	}
	
	/**
	 * Check if uniqueId is in upgradeTier cache
	 * 
	 * @param uniqueId to search
	 * @return if uniqueId was found
	 */
	public boolean hasUpgradeTier(String uniqueId) {
		return this.getUpgradeTierById(uniqueId) != null;
	}
	
	// ------------------------------------------------------------
	// Section: Comparators
	// ------------------------------------------------------------
	
	/**
	 * Comparator used to sort the upgradeData
	 * First by order from lowest to highest with < 0 at the end
	 * If tie then sort by name
	 */
	private final Comparator<UpgradeData> upgradeDataComparator = (upgrade1, upgrade2) -> {
		if (upgrade1.getOrder() == upgrade2.getOrder()) {
			return upgrade1.getName().compareToIgnoreCase(upgrade2.getName());
		} else {
			if (upgrade1.getOrder() < 0 || upgrade2.getOrder() < 0)
				return Boolean.compare(upgrade1.getOrder() < 0, upgrade2.getOrder() < 0);
			return Integer.compare(upgrade1.getOrder(), upgrade2.getOrder());
		}
	};
	
	/**
	 * Comparator used to sort the upgradeTier
	 * Compare the startLevel, from lowest to highest
	 */
	private final Comparator<UpgradeTier> upgradeTierComparator = (upgrade1, upgrade2) -> {
		return Integer.compare(upgrade1.getStartLevel(), upgrade2.getStartLevel());
	};
	
	// ------------------------------------------------------------
	// Section: Loading
	// ------------------------------------------------------------
	
	/**
	 * Load all cache and run validation on it
	 */
	private void load() {
		this.databaseUpgradeData.loadObjects().forEach(this::loadUpgradeData);
		this.databaseUpgradeTier.loadObjects().forEach(this::loadUpgradeTier);
		this.validate();
	}
	
	// ------------------------------------------------------------
	// Section: Loading / UpgradeData
	// ------------------------------------------------------------
	
	/**
	 * Use {@link #loadUpgradeData(UpgradeData, boolean, User)} by setting overwrite to true and user to null
	 * 
	 * @param upgrade to add to the cache
	 * @return If upgrade could be loaded
	 */
	private boolean loadUpgradeData(UpgradeData upgrade) {
		return this.loadUpgradeData(upgrade, true, null);
	}
	
	/**
	 * Check that upgrade is not null, that it's valid and that it can be overwrite
	 * If valid It then add it to the cache and return true
	 * Else return false and if user not null then display message
	 * 
	 * @param upgrade to add to the cache
	 * @param overwrite if uniqueId already in cache, should it overwrite it
	 * @param user if given, will send message in case of error
	 * @return true if added to the cache, false otherwise
	 */
	public boolean loadUpgradeData(UpgradeData upgrade, boolean overwrite, @Nullable User user) {
		
		// Check that upgrade is present
		if (upgrade == null) {
			if (user != null)
				user.sendMessage("upgrades.error.loaderror", WHAT, "Upgrade data");
			this.addon.logWarning("Couldn't load upgrade data from database");
			return false;
		}
		
		// Check that upgrade is valid
		if (!upgrade.isValid()) {
			if (user != null)
				user.sendMessage("upgrades.error.upgradeinvalid",
						UPGRADEID, upgrade.getUniqueId(),
						WHAT, "data");
			this.addon.logWarning("Data for upgrade data " + upgrade.getUniqueId() + " is invalid. You should look in the database");
			return false;
		}
		
		// Check if uniqueId is already in cache and if it can overwrite it
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
		
		// Add to cache
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
	
	/**
	 * use {@link #loadUpgradeTier(UpgradeTier, boolean, User)} by setting overwrite to true and use to null
	 * @param upgade tier to add to the cache
	 * @return If added to cache true, else false
	 */
	private boolean loadUpgradeTier(UpgradeTier upgade) {
		return this.loadUpgradeTier(upgade, true, null);
	}
	
	/**
	 * Check that tier is not null, that it's valid and that it can be overwrite
	 * If valid It then add it to the cache and return true
	 * Else return false and if user not null then display message
	 * 
	 * @param upgrade tier to add to the cache
	 * @param overwrite if uniqueId already in cache, should it overwrite it
	 * @param user if given, will send message in case of error
	 * @return true if added to the cache, false otherwise
	 */
	public boolean loadUpgradeTier(UpgradeTier upgrade, boolean overwrite, @Nullable User user) {
		
		// Check that upgrade tier is present
		if (upgrade == null) {
			if (user != null)
				user.sendMessage("upgrades.error.loaderror", WHAT, "Upgrade tier");
			this.addon.logWarning("Couldn't load upgrade tier from database");
			return false;
		}
		
		// Check that upgrade tier is valid
		if (!upgrade.isValid()) {
			if (user != null)
				user.sendMessage("upgrades.error.upgradeinvalid",
						UPGRADEID, upgrade.getUniqueId(),
						WHAT, "tier");
			this.addon.logWarning("Data for upgrade tier " + upgrade.getUniqueId() + " is invalid. You should look in the database");
			return false;
		}
		
		// Check if uniqueId is already in cache and if it can overwrite it
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
		
		// Add to cache
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
	
	/**
	 * Run all validate of cache
	 */
	private void validate() {
		this.validateUpgradeTier();
	}
	
	/**
	 * Check that each tier has a loaded parent
	 * Unload the tier if not
	 */
	private void validateUpgradeTier() {
		this.upgradeTierCache.values().forEach(tier -> {
			if (!this.upgradeDataCache.containsKey(tier.getUpgrade())) {
				this.addon.logWarning("Upgrade tier " + tier.getUniqueId() + " has a reference to an unknow upgrade data. It will be skiped");
				this.upgradeTierCache.remove(tier.getUniqueId());
			}
		});
	}
	
	// ------------------------------------------------------------
	// Section: Saving data
	// ------------------------------------------------------------
	
	/**
	 * Save all cache
	 */
	public void saveAll() {
		this.saveUpgradeDatas();
		this.saveUpgradeTiers();
	}
	
	/**
	 * Save all upgradeData of the upgradeData cache
	 */
	public void saveUpgradeDatas() {
		this.upgradeDataCache.values().forEach(this.databaseUpgradeData::saveObjectAsync);
	}
	
	/**
	 * Save given upgradeData async
	 * @param upgrade data to save
	 * @return
	 */
	public CompletableFuture<Boolean> saveUpgradeData(@NonNull UpgradeData upgrade) {
		return this.databaseUpgradeData.saveObjectAsync(upgrade);
	}
	
	/**
	 * Save all upgradeTier of the upgradeTier cache
	 */
	public void saveUpgradeTiers() {
		this.upgradeTierCache.values().forEach(this.databaseUpgradeTier::saveObjectAsync);
	}
	
	/**
	 * Save given upgradeTier asyn
	 * @param upgrade tier to save
	 * @return
	 */
	public CompletableFuture<Boolean> saveUpgradeTier(UpgradeTier tier) {
		return this.databaseUpgradeTier.saveObjectAsync(tier);
	}
	
	// ------------------------------------------------------------
	// Section: Getting UpgradeData
	// ------------------------------------------------------------
	
	/**
	 * use {@link #getUpgradeDataByGameMode(String)} by getting name from world
	 * @param world to filter for upgrades
	 * @return all upgradeData link to world
	 */
	public List<UpgradeData> getUpgradeDataByGameMode(@NonNull World world) {
		return this.addon.getPlugin().getIWM().getAddon(world)
				.map(gameMode -> this.getUpgradeDataByGameMode(gameMode.getDescription().getName()))
				.orElse(Collections.emptyList());
	}
	
	/**
	 * Get all upgradeData link to world
	 * Sort them using {@link #upgradeDataComparator}
	 * 
	 * @param world to look for
	 * @return all upgradeData link to world
	 */
	public List<UpgradeData> getUpgradeDataByGameMode(@NonNull String world) {
		return this.upgradeDataCache.values().stream()
				.filter(upgrade -> upgrade.getWorld().equals(world))
				.sorted(this.upgradeDataComparator)
				.collect(Collectors.toList());
	}
	
	/**
	 * Get upgradeData from it's uniqueId
	 * 
	 * @param uniqueId to look for
	 * @return UpgradeData or null if not found
	 */
	@Nullable
	public UpgradeData getUpgradeDataById(@NonNull String uniqueId) {
		return this.upgradeDataCache.get(uniqueId);
	}
	
	// ------------------------------------------------------------
	// Section: Getting UpgradeTier
	// ------------------------------------------------------------
	
	/**
	 * use {@link #getUpgradeTierByUpgradeData(String)} by getting id from upgradeData
	 * 
	 * @param upgradeData to filter for tier
	 * @return all upgradeTier link to this upgradeData
	 */
	public List<UpgradeTier> getUpgradeTierByUpgradeData(@NonNull UpgradeData upgradeData) {
		return this.getUpgradeTierByUpgradeData(upgradeData.getUniqueId());
	}
	
	/**
	 * Get all upgradeTier link to this upgradeData
	 * Sort them using {@link #upgradeTierComparator}
	 * 
	 * @param upgradeDataId to look for
	 * @return all upgradeTier link to this upgradeData
	 */
	public List<UpgradeTier> getUpgradeTierByUpgradeData(@NonNull String upgradeDataId) {
		return this.upgradeTierCache.values().stream()
				.filter(upgrade -> upgrade.getUpgrade() == upgradeDataId)
				.sorted(this.upgradeTierComparator)
				.collect(Collectors.toList());
	}
	
	/**
	 * Get upgradeTier from it's uniqueId
	 * 
	 * @param uniqueId to look for
	 * @return upgradeTier or null if not found
	 */
	@Nullable
	public UpgradeTier getUpgradeTierById(@NonNull String uniqueId) {
		return this.upgradeTierCache.get(uniqueId);
	}
	
	// ------------------------------------------------------------
	// Section: Create Methods
	// ------------------------------------------------------------
	
	/**
	 * Use {@link #createUpgradeData(String, String, User)} by getting world name from world
	 * 
	 * @param uniqueId used to create upgradeData
	 * @param world in which world this upgradeData should be used
	 * @param user if given, then send messages
	 * @return UpgradeData or null if couldn't create it
	 */
	@Nullable
	public UpgradeData createUpgradeData(@NonNull String uniqueId, @NonNull World world, @Nullable User user) {
		return this.addon.getPlugin().getIWM().getAddon(world)
			.map(gm ->  this.createUpgradeData(uniqueId, gm.getDescription().getName(), user))
			.orElse(null);
	}
	
	/**
	 * Check that uniqueId isn't already used
	 * Then create a new UpgradeData
	 * Finally save and load it
	 * 
	 * @param uniqueId used to create UpgradeData
	 * @param world in which world this upgradeData should be used
	 * @param user if given, then send messages
	 * @return UpgradeData or null if couldn't create it
	 */
	@Nullable
	public UpgradeData createUpgradeData(@NonNull String uniqueId, @NonNull String world, @Nullable User user) {
		if (this.hasUpgradeData(uniqueId))
			return null;
		
		UpgradeData upgrade = new UpgradeData();
		upgrade.setUniqueId(uniqueId);
		upgrade.setWorld(world);
		
		this.saveUpgradeData(upgrade);
		this.loadUpgradeData(upgrade, true, user);
		
		return upgrade;
	}
	
	/**
	 * Use {@link #createUpgradeTier(String, String, int, int, User)} by getting upgradeDataId from upgradeData
	 * 
	 * @param uniqueId used to create upgradeTier
	 * @param upgrade for which upgradeData this upgradeTier should be used
	 * @param startLevel Start level of this tier
	 * @param endLevel End level of this tier
	 * @param user if given, then send message
	 * @return UpgradeTier or null if couldn't create it
	 */
	@Nullable
	public UpgradeTier createUpgradeTier(@NonNull String uniqueId, @NonNull UpgradeData upgrade, int startLevel, int endLevel, @Nullable User user) {
		return this.createUpgradeTier(uniqueId, upgrade.getUniqueId(), startLevel, endLevel, user);
	}
	
	/**
	 * Check that uniqueId isn't already used
	 * Then create a new UpgradeTier
	 * Finally save and load it
	 * 
	 * @param uniqueId used to create upgradeTier
	 * @param upgradeId for which upgradeData this upgradeTier should be used
	 * @param startLevel Start level of this tier
	 * @param endLevel End level of this tier
	 * @param user if given, then send message
	 * @return UpgradeTier or null if couldn't create it
	 */
	@Nullable
	public UpgradeTier createUpgradeTier(@NonNull String uniqueId, @NonNull String upgradeId, int startLevel, int endLevel, @Nullable User user) {
		if (this.hasUpgradeTier(uniqueId))
			return null;
		
		UpgradeTier tier = new UpgradeTier();
		tier.setUniqueId(uniqueId);
		tier.setUpgrade(upgradeId);
		tier.setStartLevel(startLevel);
		tier.setEndLevel(endLevel);
		
		this.saveUpgradeTier(tier);
		this.loadUpgradeTier(tier, true, user);
		
		return tier;
	}
	
	// ------------------------------------------------------------
	// Section: Delete Methods
	// ------------------------------------------------------------
	
	/**
	 * Delete one upgradeData from the cache and database
	 * 
	 * TODO: Delete it's tier
	 * @param upgrade to delete
	 */
	public void deleteUpgradeData(UpgradeData upgrade) {
		if (this.upgradeDataCache.containsKey(upgrade.getUniqueId())) {
			this.databaseUpgradeData.deleteObject(upgrade);
			this.upgradeDataCache.remove(upgrade.getUniqueId());
		}
	}
	
	/**
	 * Delete one upgradeTier from the cache and database
	 * 
	 * @param tier to delete
	 */
	public void deleteUpgradeTier(UpgradeTier tier) {
		if (this.upgradeTierCache.containsKey(tier.getUniqueId())) {
			this.databaseUpgradeTier.deleteObject(tier);
			this.upgradeTierCache.remove(tier.getUniqueId());
		}
	}

}
