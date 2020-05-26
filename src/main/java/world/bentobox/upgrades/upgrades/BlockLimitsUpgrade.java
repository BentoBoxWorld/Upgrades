package world.bentobox.upgrades.upgrades;

import java.util.Map;

import org.bukkit.Material;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;


import world.bentobox.limits.listeners.BlockLimitsListener;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.api.Upgrade;
import world.bentobox.upgrades.dataobjects.UpgradesData;

public class BlockLimitsUpgrade extends Upgrade {

	public BlockLimitsUpgrade(UpgradesAddon addon, Material block) {
		super(addon, "LimitsUpgrade-" + block.toString(), block.toString() + " limits Upgrade", block);
		this.block = block;
	}
	
	@Override
	public void updateUpgradeValue(User user, Island island) {
		UpgradesAddon upgradeAddon = this.getUpgradesAddon();
		UpgradesData islandData = upgradeAddon.getUpgradesLevels(island.getUniqueId());
		int upgradeLevel = islandData.getUpgradeLevel(getName());
		int numberPeople = island.getMemberSet().size();
		int islandLevel;
		
		if (upgradeAddon.isLevelProvided())
			islandLevel = upgradeAddon.getUpgradesManager().getIslandLevel(island);
		else
			islandLevel = 0;
		
		Map<String, Integer> upgradeInfos = upgradeAddon.getUpgradesManager().getBlockLimitsUpgradeInfos(this.block, upgradeLevel, islandLevel, numberPeople, island.getWorld());
		UpgradeValues upgrade;
		
		if (upgradeInfos == null)
			upgrade = null;
		else
			upgrade = new UpgradeValues(upgradeInfos.get("islandMinLevel"), upgradeInfos.get("vaultCost"), upgradeInfos.get("upgrade"));
		
		this.setUpgradeValues(upgrade);
		
		String newDisplayName;
		
		if (upgrade == null) {
			newDisplayName = user.getTranslation("upgrades.ui.upgradepanel.nolimitsupgrade",
				"[block]", this.block.toString());
		} else {
			newDisplayName = user.getTranslation("upgrades.ui.upgradepanel.limitsupgrade",
				"[block]", this.block.toString(), "[level]", Integer.toString(upgrade.getUpgradeValue()));
		}
		
		this.setDisplayName(newDisplayName);
	}
	
	@Override
	public boolean doUpgrade(User user, Island island) {
		UpgradesAddon islandAddon = this.getUpgradesAddon();
		
		if (!islandAddon.isLimitsProvided())
			return false;
		
		BlockLimitsListener bLListener = islandAddon.getLimitsAddon().getBlockLimitListener();
		Map<Material, Integer> materialLimits = bLListener.getMaterialLimits(island.getWorld(), island.getUniqueId());
		
		if (!materialLimits.containsKey(this.block) || materialLimits.get(this.block) == -1) {
			this.getUpgradesAddon().logWarning("User tried to upgrade " + this.block.toString() + " limits but it has no limits. This is probably a configuration problem.");
			user.sendMessage("upgrades.error.increasenolimits");
			return false;
		}
		
		if (!super.doUpgrade(user, island))
			return false;
		
		int oldCount = materialLimits.get(this.block); 
		int newCount = (int) (oldCount + this.getUpgradeValues().getUpgradeValue());
		
		bLListener.getIsland(island.getUniqueId()).setBlockLimit(this.block, newCount);
		
		user.sendMessage("upgrades.ui.upgradepanel.limitsupgradedone",
			"[block]", this.block.toString(), "[level]", Integer.toString(this.getUpgradeValues().getUpgradeValue()));
		
		return true;
	}
	
	private Material block;
	
}
