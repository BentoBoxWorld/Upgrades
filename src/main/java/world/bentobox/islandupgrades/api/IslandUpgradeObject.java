package world.bentobox.islandupgrades.api;


import java.util.Optional;

import org.bukkit.Material;

import net.milkbowl.vault.economy.EconomyResponse;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.addons.Addon.State;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.islandupgrades.IslandUpgradesAddon;
import world.bentobox.islandupgrades.IslandUpgradesData;

public abstract class IslandUpgradeObject {
	
	public IslandUpgradeObject(Addon addon, String name, String displayName, Material icon) {
		this.name = name;
		this.displayName = displayName;
		this.icon = icon;
		this.upgradeValues = null;
		this.addon = addon;
		
		Optional<Addon> islandUpgrade = this.addon.getAddonByName("IslandUpgrades");
		if (!islandUpgrade.isPresent()) {
			this.addon.logError("Island Upgrade Addon couldn't be found");
			this.addon.setState(State.DISABLED);
		} else
			this.islandUpgradeAddon = (IslandUpgradesAddon) islandUpgrade.get();
	}
	
	public abstract void updateUpgradeValue(User user, Island island);
	
	public boolean canUpgrade(User user, Island island) {
		boolean can = true;
		
		if (this.islandUpgradeAddon.isLevelProvided() &&
			this.islandUpgradeAddon.getIslandUpgradesManager().getIslandLevel(island) < this.upgradeValues.getIslandLevel()) {
			
			can = false;
		}
		
		if (this.islandUpgradeAddon.isVaultProvided() &&
			!this.islandUpgradeAddon.getVaultHook().has(user, this.upgradeValues.getMoneyCost())) {
			
			can = false;
		}
		
		return can;
	}
	
	public boolean doUpgrade(User user, Island island) {
		
		if (this.islandUpgradeAddon.isVaultProvided()) {
			EconomyResponse response = this.islandUpgradeAddon.getVaultHook().withdraw(user, this.upgradeValues.getMoneyCost());
			if (!response.transactionSuccess()) {
				this.addon.logWarning("User Money withdrawing failed user: " + user.getName() + " reason: " + response.errorMessage);
				user.sendMessage("islandupgrades.error.costwithdraw");
				return false;
			}
		}
		
		IslandUpgradesData data = this.islandUpgradeAddon.getIslandUpgradesLevel(island.getUniqueId());
		data.setUpgradeLevel(this.name, data.getUpgradeLevel(this.name) + 1);
		
		return true;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getDisplayName() {
		return this.displayName;
	}
	
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	public Material getIcon() {
		return this.icon;
	}
	
	public UpgradeValues getUpgradeValues() {
		return this.upgradeValues;
	}
	
	public void setUpgradeValues(UpgradeValues upgrade) {
		this.upgradeValues = upgrade;
	}
	
	public IslandUpgradesAddon getIslandUpgradeAddon() {
		return this.islandUpgradeAddon;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		
		if (obj == null)
			return false;
		
		if (!(obj instanceof IslandUpgradeObject))
			return false;
		
		IslandUpgradeObject cobj = (IslandUpgradeObject) obj;
		
		return this.name == cobj.getName();
	}
	
	private final String name;
	private String displayName;
	private Material icon;
	private UpgradeValues upgradeValues;
	private Addon addon;
	private IslandUpgradesAddon islandUpgradeAddon;
	
	public class UpgradeValues {
		
		public UpgradeValues(Integer islandLevel, Integer moneyCost, Integer upgradeValue) {
			this.islandLevel = islandLevel;
			this.moneyCost = moneyCost;
			this.upgradeValue = upgradeValue;
		}
		
		public Long getIslandLevel() {
			return islandLevel;
		}

		public void setIslandLevel(long islandLevel) {
			this.islandLevel = islandLevel;
		}

		public Long getMoneyCost() {
			return moneyCost;
		}
		
		public void setMoneyCost(long moneyCost) {
			this.moneyCost = moneyCost;
		}
		
		public Long getUpgradeValue() {
			return upgradeValue;
		}

		public void setUpgradeValue(long upgradeValue) {
			this.upgradeValue = upgradeValue;
		}

		private long islandLevel;
		private long moneyCost;
		private long upgradeValue;
	}

}
