package world.bentobox.islandupgrades.task;

import java.util.Map;

import net.milkbowl.vault.economy.EconomyResponse;
import world.bentobox.bentobox.api.events.island.IslandEvent;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.islandupgrades.IslandUpgradesAddon;
import world.bentobox.islandupgrades.IslandUpgradesData;

public class IslandUpgradesRangeUpgrade {

	public IslandUpgradesRangeUpgrade(IslandUpgradesAddon addon) {
		this.addon = addon;
	}
	
	public boolean canUpgrade(User user, Island island, Map<String, Integer> rangeUpgradeInfo) {
		boolean can = true;
		
		if (this.addon.isLevelProvided() && this.addon.getIslandUpgradesManager().getIslandLevel(island) < rangeUpgradeInfo.get("islandMinLevel"))
			can = false;
		
		if (this.addon.isVaultProvided() && !this.addon.getVaultHook().has(user, rangeUpgradeInfo.get("vaultCost")))
			can = false;
		
		return can;
	}
	
	public boolean doUpgrade(User user, Island island, Map<String, Integer> rangeUpgradeInfo) {
		
		int newRange = island.getProtectionRange() + rangeUpgradeInfo.get("upgradeRange");
		if (newRange > island.getRange()) {
			this.addon.logWarning("User tried to upgrade their island range over the max. This is probably a configuration problem.");
			user.sendMessage("islandupgrades.error.rangeovermax");
			return false;
		}
		
		if (this.addon.isVaultProvided()) {
			EconomyResponse response = this.addon.getVaultHook().withdraw(user, rangeUpgradeInfo.get("vaultCost"));
			if (!response.transactionSuccess()) {
				this.addon.logWarning("User Money withdrawing failed user: " + user.getName() + " reason: " + response.errorMessage);
				user.sendMessage("islandupgrades.error.costwithdraw");
				return false;
			}
		}
		
		int oldRange = island.getProtectionRange();
		
		island.setProtectionRange(newRange);
		
		IslandEvent.builder()
		.island(island)
		.location(island.getCenter())
		.reason(IslandEvent.Reason.RANGE_CHANGE)
		.involvedPlayer(user.getUniqueId())
		.admin(false)
		.protectionRange(newRange, oldRange)
		.build();
		
		IslandUpgradesData data = this.addon.getIslandUpgradesLevel(island.getUniqueId());
		data.setRangeUpgradeLevel(data.getRangeUpgradeLevel() + 1);
		
		user.sendMessage("islandupgrades.ui.upgradepanel.rangeupgradedone",
			"[rangelevel]", rangeUpgradeInfo.get("upgradeRange").toString());

		return true;
	}
	
	private IslandUpgradesAddon addon;
	
}
