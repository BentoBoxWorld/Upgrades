package world.bentobox.islandupgrades.upgrades;

import java.util.Map;

import org.bukkit.Material;

import world.bentobox.bentobox.api.events.island.IslandEvent;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.islandupgrades.IslandUpgradesAddon;
import world.bentobox.islandupgrades.IslandUpgradesData;
import world.bentobox.islandupgrades.api.IslandUpgradeObject;

public class RangeUpgrade extends IslandUpgradeObject {

	public RangeUpgrade(IslandUpgradesAddon addon) {
		super(addon, "RangeUpgrade", "RangeUpgrade", Material.OAK_FENCE);
	}

	@Override
	public void updateUpgradeValue(User user, Island island) {
		IslandUpgradesAddon islandAddon = this.getIslandUpgradeAddon();
		IslandUpgradesData islandData = islandAddon.getIslandUpgradesLevel(island.getUniqueId());
		long upgradeLevel = islandData.getUpgradeLevel(getName());
		long numberPeople = island.getMemberSet().size();
		long islandLevel;
		
		if (islandAddon.isLevelProvided())
			islandLevel = islandAddon.getIslandUpgradesManager().getIslandLevel(island);
		else 
			islandLevel = 0L;
		
		Map<String, Integer> upgradeInfos = islandAddon.getIslandUpgradesManager().getRangeUpgradeInfos(upgradeLevel, islandLevel, numberPeople, island.getWorld());
		UpgradeValues upgrade;
		
		if (upgradeInfos == null)
			upgrade = null;
		else
			upgrade = new UpgradeValues(upgradeInfos.get("islandMinLevel"), upgradeInfos.get("vaultCost"), upgradeInfos.get("upgradeRange"));
		
		this.setUpgradeValues(upgrade);
		
		String newDisplayName;
		
		if (upgrade == null)
			newDisplayName = user.getTranslation("islandupgrades.ui.upgradepanel.norangeupgrade");
		else
			newDisplayName = user.getTranslation("islandupgrades.ui.upgradepanel.rangeupgrade",
				"[rangelevel]", upgrade.getUpgradeValue().toString());
		
		this.setDisplayName(newDisplayName);
	}
	
	@Override
	public boolean doUpgrade(User user, Island island) {
		long newRange = island.getProtectionRange() + this.getUpgradeValues().getUpgradeValue();
		
		if (newRange > island.getRange()) {
			this.getIslandUpgradeAddon().logWarning("User tried to upgrade their island range over the max. This is probably a configuration problem.");
			user.sendMessage("islandupgrades.error.rangeovermax");
			return false;
		}
		
		if (!super.doUpgrade(user, island))
			return false;
		
		int oldRange = island.getProtectionRange();
		
		island.setProtectionRange((int) newRange);
		
		IslandEvent.builder()
		.island(island)
		.location(island.getCenter())
		.reason(IslandEvent.Reason.RANGE_CHANGE)
		.involvedPlayer(user.getUniqueId())
		.admin(false)
		.protectionRange((int) newRange, oldRange)
		.build();
		
		user.sendMessage("islandupgrades.ui.upgradepanel.rangeupgradedone",
			"[rangelevel]", this.getUpgradeValues().getUpgradeValue().toString());

		return true;
	}
	
}
