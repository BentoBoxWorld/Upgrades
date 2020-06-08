package world.bentobox.upgrades.upgrades;

import java.util.Map;

import org.bukkit.entity.EntityType;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.limits.listeners.BlockLimitsListener;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.api.Upgrade;
import world.bentobox.upgrades.dataobjects.UpgradesData;

public class EntityLimitsUpgrade extends Upgrade {
	
	public EntityLimitsUpgrade(UpgradesAddon addon, EntityType entity) {
		super(addon, "LimitsUpgrade-" + entity.toString(), entity.toString() + " limits Upgrade", addon.getSettings().getEntityIcon(entity));
		this.entity = entity;
	}
	
	@Override
	public void updateUpgradeValue(User user, Island island) {
		UpgradesAddon upgradeAddon = this.getUpgradesAddon();
		UpgradesData islandData = upgradeAddon.getUpgradesLevels(island.getUniqueId());
		int upgradeLevel = islandData.getUpgradeLevel(this.getName());
		int numberPeople = island.getMemberSet().size();
		int islandLevel;
		
		if (upgradeAddon.isLevelProvided())
			islandLevel = upgradeAddon.getUpgradesManager().getIslandLevel(island);
		else
			islandLevel = 0;
		
		Map<String, Integer> upgradeInfos = upgradeAddon.getUpgradesManager().getEntityLimitsUpgradeInfos(this.entity, upgradeLevel, islandLevel, numberPeople, island.getWorld());
		UpgradeValues upgrade;
		
		// Get new description
		String description = upgradeAddon.getUpgradesManager().getEntityLimitsUpgradeTierName(this.entity, upgradeLevel, island.getWorld()) + " (" + upgradeLevel + "/" +
							upgradeAddon.getUpgradesManager().getEntityLimitsUpgradeMax(this.entity, island.getWorld()) + ")";
		// Set new description
		this.setOwnDescription(user, description);
		
		if (upgradeInfos == null)
			upgrade = null;
		else
			upgrade = new UpgradeValues(upgradeInfos.get("islandMinLevel"), upgradeInfos.get("vaultCost"), upgradeInfos.get("upgrade"));
		
		this.setUpgradeValues(user, upgrade);
		
		String newDisplayName;
		
		if (upgrade == null) {
			newDisplayName = user.getTranslation("upgrades.ui.upgradepanel.nolimitsupgrade",
				"[block]", this.entity.toString());
		} else {
			newDisplayName = user.getTranslation("upgrades.ui.upgradepanel.limitsupgrade",
				"[block]", this.entity.toString(), "[level]", Integer.toString(upgrade.getUpgradeValue()));
		}
		
		this.setDisplayName(newDisplayName);
	}
	
	@Override
	public boolean doUpgrade(User user, Island island) {
		UpgradesAddon islandAddon = this.getUpgradesAddon();
		
		if (!islandAddon.isLimitsProvided())
			return false;
		
		BlockLimitsListener bLListener = islandAddon.getLimitsAddon().getBlockLimitListener();
		Map<EntityType, Integer> entityLimits = islandAddon.getUpgradesManager().getEntityLimits(island);
		
		if (!entityLimits.containsKey(this.entity) || entityLimits.get(this.entity) == -1) {
			this.getUpgradesAddon().logWarning("User tried to upgrade " + this.entity.toString() + " limits but it has no limits. This is probably a configuration problem.");
			user.sendMessage("upgrades.error.increasenolimits");
			return false;
		}
		
		if (!super.doUpgrade(user, island))
			return false;
		 
		int newCount = (int) (entityLimits.get(this.entity) + this.getUpgradeValues(user).getUpgradeValue());
		
		bLListener.getIsland(island.getUniqueId()).setEntityLimit(this.entity, newCount);
		
		user.sendMessage("upgrades.ui.upgradepanel.limitsupgradedone",
			"[block]", this.entity.toString(), "[level]", Integer.toString(this.getUpgradeValues(user).getUpgradeValue()));
		
		return true;
	}
	
	private EntityType entity;

}
