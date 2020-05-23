package world.bentobox.upgrades.upgrades;

import java.util.Map;

import org.bukkit.Material;

import world.bentobox.bentobox.api.events.island.IslandEvent;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.dataobjects.UpgradesData;
import world.bentobox.upgrades.api.Upgrade;

/**
 * Upgrade Object for range upgrade
 * 
 * @author Ikkino
 *
 */
public class RangeUpgrade extends Upgrade {

	public RangeUpgrade(UpgradesAddon addon) {
		super(addon, "RangeUpgrade", "RangeUpgrade", Material.OAK_FENCE);
	}

	/**
	 * When user open the interface
	 */
	@Override
	public void updateUpgradeValue(User user, Island island) {
		// Get the addon
		UpgradesAddon islandAddon = this.getUpgradesAddon();
		// Get the data from IslandUpgrade
		UpgradesData islandData = islandAddon.getUpgradesLevels(island.getUniqueId());
		// The level of this upgrade
		long upgradeLevel = islandData.getUpgradeLevel(getName());
		// The number of members on the island
		long numberPeople = island.getMemberSet().size();
		// The level of the island from Level Addon
		long islandLevel;
		
		// If level addon is provided
		if (islandAddon.isLevelProvided())
			islandLevel = islandAddon.getUpgradesManager().getIslandLevel(island);
		else 
			islandLevel = 0L;
		
		// Get upgrades infos of range upgrade from settings
		Map<String, Integer> upgradeInfos = islandAddon.getUpgradesManager().getRangeUpgradeInfos(upgradeLevel, islandLevel, numberPeople, island.getWorld());
		UpgradeValues upgrade;
		
		// If null -> no next upgrades
		if (upgradeInfos == null)
			upgrade = null;
		else
			upgrade = new UpgradeValues(upgradeInfos.get("islandMinLevel"), upgradeInfos.get("vaultCost"), upgradeInfos.get("upgradeRange"));
		
		// Update the upgrade values
		this.setUpgradeValues(upgrade);
		
		// Update the display name
		String newDisplayName;
		
		if (upgrade == null) {
			// No next upgrade -> lang message
			newDisplayName = user.getTranslation("islandupgrades.ui.upgradepanel.norangeupgrade");
		} else {
			// get lang message
			newDisplayName = user.getTranslation("islandupgrades.ui.upgradepanel.rangeupgrade",
				"[rangelevel]", upgrade.getUpgradeValue().toString());
		}
		
		this.setDisplayName(newDisplayName);
	}
	
	/**
	 * When user do upgrade
	 */
	@Override
	public boolean doUpgrade(User user, Island island) {
		// Get the new range
		long newRange = island.getProtectionRange() + this.getUpgradeValues().getUpgradeValue();
		
		// If newRange is more than the authorized range (Config problem)
		if (newRange > island.getRange()) {
			this.getUpgradesAddon().logWarning("User tried to upgrade their island range over the max. This is probably a configuration problem.");
			user.sendMessage("upgrades.error.rangeovermax");
			return false;
		}
		
		// if super doUpgrade not worked
		if (!super.doUpgrade(user, island))
			return false;
		
		// Save oldRange for rangeChange event
		int oldRange = island.getProtectionRange();
		
		// Set range
		island.setProtectionRange((int) newRange);
		
		// Launch range change event
		IslandEvent.builder()
		.island(island)
		.location(island.getCenter())
		.reason(IslandEvent.Reason.RANGE_CHANGE)
		.involvedPlayer(user.getUniqueId())
		.admin(false)
		.protectionRange((int) newRange, oldRange)
		.build();
		
		user.sendMessage("upgrades.ui.upgradepanel.rangeupgradedone",
			"[rangelevel]", this.getUpgradeValues().getUpgradeValue().toString());

		return true;
	}
	
}
