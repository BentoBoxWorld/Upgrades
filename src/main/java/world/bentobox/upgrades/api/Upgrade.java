package world.bentobox.upgrades.api;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Material;

import net.milkbowl.vault.economy.EconomyResponse;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.addons.Addon.State;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.dataobjects.UpgradesData;

/**
 * Upgrade Object for IslandUpgradeAddon. Extend this to create a new upgrade
 * 
 * @author Ikkino
 *
 */
public abstract class Upgrade {
	
	/**
	 * Initialize the upgrade object
	 * you should call it in your init methode
	 * 
	 * @param addon: This should be your addon
	 * @param name: This is the name for the upgrade that will be used in the DataBase
	 * @param displayName: This is the name that is shown to the user 
	 * @param icon: This is the icon shown to the user
	 */
	public Upgrade(Addon addon, String name, String displayName, Material icon) {
		this.name = name;
		this.displayName = displayName;
		this.icon = icon;
		this.addon = addon;
		
		this.playerCache = new HashMap<>();
		
		Optional<Addon> islandUpgrade = this.addon.getAddonByName("upgrades");
		if (!islandUpgrade.isPresent()) {
			this.addon.logError("Island Upgrade Addon couldn't be found");
			this.addon.setState(State.DISABLED);
		} else {
			this.upgradesAddon = (UpgradesAddon) islandUpgrade.get();
			this.addon.log("Added upgrade -> " + name);
		}
	}
	
	/**
	 * This fonction is called every times a user open the interface
	 * You should make it update the upgradeValues
	 * 
	 * @param user: This is the user that ask for the interface
	 * @param island: This is the island concerned by the interface
	 */
	public abstract void updateUpgradeValue(User user, Island island);
	
	/**
	 * This function return true if the user can upgrade for this island.
	 * You can override it and call the super.
	 * 
	 * The super test for islandLevel and for money
	 * 
	 * @param user: This is the user that try to upgrade
	 * @param island: This is the island that is concerned
	 * @return: Can upgrade
	 */
	public boolean canUpgrade(User user, Island island) {
		UpgradeValues upgradeValues = this.getUpgradeValues(user);
		boolean can = true;
		
		if (this.upgradesAddon.isLevelProvided() &&
			this.upgradesAddon.getUpgradesManager().getIslandLevel(island) < upgradeValues.getIslandLevel()) {
			
			can = false;
		}
		
		if (this.upgradesAddon.isVaultProvided() &&
			!this.upgradesAddon.getVaultHook().has(user, upgradeValues.getMoneyCost())) {
			
			can = false;
		}
		
		return can;
	}
	
	/**
	 * This function is called when the user is upgrading for the island
	 * It is called after the canUpgrade function
	 * 
	 * You should call the super to update the balance of the user as well as the level is the island
	 * 
	 * @param user: This is the user that do the upgrade
	 * @param island: This is the island that is concerned
	 * @return: If upgrade was successful
	 */
	public boolean doUpgrade(User user, Island island) {
		UpgradeValues upgradeValues = this.getUpgradeValues(user);
		
		if (this.upgradesAddon.isVaultProvided()) {
			EconomyResponse response = this.upgradesAddon.getVaultHook().withdraw(user, upgradeValues.getMoneyCost());
			if (!response.transactionSuccess()) {
				this.addon.logWarning("User Money withdrawing failed user: " + user.getName() + " reason: " + response.errorMessage);
				user.sendMessage("upgrades.error.costwithdraw");
				return false;
			}
		}
		
		UpgradesData data = this.upgradesAddon.getUpgradesLevels(island.getUniqueId());
		data.setUpgradeLevel(this.name, data.getUpgradeLevel(this.name) + 1);
		
		return true;
	}
	
	/**
	 * @return: The name that is used for the DataBase
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * @return: The name that is displayed to the user
	 */
	public String getDisplayName() {
		return this.displayName;
	}
	
	/**
	 * @param displayName: To update the name to display to the user
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	/**
	 * @return: The icon that is displayed to the user
	 */
	public Material getIcon() {
		return this.icon;
	}
	
	/**
	 * @return: The actual upgradeValues
	 */
	public UpgradeValues getUpgradeValues(User user) {
		return this.playerCache.get(user.getUniqueId());
	}
	
	/**
	 * @param upgrade: Values to upgrades
	 */
	public void setUpgradeValues(User user, UpgradeValues upgrade) {
		this.playerCache.put(user.getUniqueId(), upgrade);
	}
	
	/**
	 * Function that get the upgrades addon
	 * You should use it to use the upgrades addon methods
	 * 
	 * @return
	 */
	public UpgradesAddon getUpgradesAddon() {
		return this.upgradesAddon;
	}
	
	/**
	 * You shouldn't override this function
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		
		if (obj == null)
			return false;
		
		if (!(obj instanceof Upgrade))
			return false;
		
		Upgrade cobj = (Upgrade) obj;
		
		return this.name == cobj.getName();
	}
	
	private final String name;
	private String displayName;
	private Material icon;
	private Addon addon;
	private UpgradesAddon upgradesAddon;
	private Map<UUID, UpgradeValues> playerCache;
	
	public class UpgradeValues {
		
		public UpgradeValues(Integer islandLevel, Integer moneyCost, Integer upgradeValue) {
			this.islandLevel = islandLevel;
			this.moneyCost = moneyCost;
			this.upgradeValue = upgradeValue;
		}
		
		public int getIslandLevel() {
			return islandLevel;
		}

		public void setIslandLevel(int islandLevel) {
			this.islandLevel = islandLevel;
		}

		public int getMoneyCost() {
			return moneyCost;
		}
		
		public void setMoneyCost(int moneyCost) {
			this.moneyCost = moneyCost;
		}
		
		public int getUpgradeValue() {
			return upgradeValue;
		}

		public void setUpgradeValue(int upgradeValue) {
			this.upgradeValue = upgradeValue;
		}

		private int islandLevel;
		private int moneyCost;
		private int upgradeValue;
	}

}
