package world.bentobox.upgrades.upgrades;

import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

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
		int upgradeLevel = islandData.getUpgradeLevel(getName());
		// The number of members on the island
		int numberPeople = island.getMemberSet().size();
		// The level of the island from Level Addon
		int islandLevel;
		
		// If level addon is provided
		if (islandAddon.isLevelProvided())
			islandLevel = islandAddon.getUpgradesManager().getIslandLevel(island);
		else 
			islandLevel = 0;
		
		// Get upgrades infos of range upgrade from settings
		Map<String, Integer> upgradeInfos = islandAddon.getUpgradesManager().getRangeUpgradeInfos(upgradeLevel, islandLevel, numberPeople, island.getWorld());
		UpgradeValues upgrade;
		
		// Get new description
		String description = islandAddon.getUpgradesManager().getRangeUpgradeTierName(upgradeLevel, island.getWorld()) + " (" + upgradeLevel + "/" +
							islandAddon.getUpgradesManager().getRangeUpgradeMax(island.getWorld()) + ")";
		// Set new description
		this.setOwnDescription(user, description);
		
		// If null -> no next upgrades
		if (upgradeInfos == null)
			upgrade = null;
		else
			upgrade = new UpgradeValues(upgradeInfos.get("islandMinLevel"), upgradeInfos.get("vaultCost"), upgradeInfos.get("upgrade"));
		
		// Update the upgrade values
		this.setUpgradeValues(user, upgrade);
		
		// Update the display name
		String newDisplayName;
		
		if (upgrade == null) {
			// No next upgrade -> lang message
			newDisplayName = user.getTranslation("upgrades.ui.upgradepanel.norangeupgrade");
		} else {
			// get lang message
			newDisplayName = user.getTranslation("upgrades.ui.upgradepanel.rangeupgrade",
				"[rangelevel]", Integer.toString(upgrade.getUpgradeValue()));
		}
		
		this.setDisplayName(newDisplayName);
	}
	
	@Override
	public boolean isShowed(User user, Island island) {
		// Get the addon
		UpgradesAddon upgradesAddon = this.getUpgradesAddon();
		// Get the data from upgrades
		UpgradesData islandData = upgradesAddon.getUpgradesLevels(island.getUniqueId());
		// Get level of the upgrade
		int upgradeLevel = islandData.getUpgradeLevel(this.getName());
		// Permission level required
		int permissionLevel = upgradesAddon.getUpgradesManager().getRangePermissionLevel(upgradeLevel, island.getWorld());
		
		// If default permission, then true
		if (permissionLevel == 0)
			return true;
		
		Player player = user.getPlayer();
		String gamemode = island.getGameMode();
		String permissionStart = gamemode + ".upgrades." + this.getName() + "."; 
		
		// For each permission of the player
		for (PermissionAttachmentInfo perms : player.getEffectivePermissions()) {
			
			// If permission is the one we search 
			if (!perms.getValue() || !perms.getPermission().startsWith(permissionStart))
				continue;
			
			if (perms.getPermission().contains(permissionStart + "*")) {
				this.logError(player.getName(), perms.getPermission(), "Wildcards are not allowed.");
				return false;
			}
			
			String[] split = perms.getPermission().split("\\.");
			if (split.length != 4) {
				logError(player.getName(), perms.getPermission(), "format must be '" + permissionStart + "LEVEL'");
				return false;
			}
			
			if (!NumberUtils.isDigits(split[3])) {
				logError(player.getName(), perms.getPermission(), "The last part must be a number");
				return false;
			}
			
			if (permissionLevel <= Integer.parseInt(split[3]))
				return true;
		}
		
		return false;
	}
	
	private void logError(String name, String perm, String error) {
        this.getUpgradesAddon().logError("Player " + name + " has permission: '" + perm + "' but " + error + " Ignoring...");
    }
	
	/**
	 * When user do upgrade
	 */
	@Override
	public boolean doUpgrade(User user, Island island) {
		// Get the new range
		long newRange = island.getProtectionRange() + this.getUpgradeValues(user).getUpgradeValue();
		
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
			"[rangelevel]", Integer.toString(this.getUpgradeValues(user).getUpgradeValue()));

		return true;
	}
	
}
