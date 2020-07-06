package world.bentobox.upgrades.upgrades;

import java.util.List;
import java.util.Map;

import org.bukkit.Material;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.api.Upgrade;
import world.bentobox.upgrades.dataobjects.UpgradesData;

public class CommandUpgrade extends Upgrade {
	
	public CommandUpgrade(UpgradesAddon addon, String cmdId, Material icon) {
		super(addon, "command-" + cmdId, addon.getSettings().getCommandName(cmdId), icon);
		this.cmdId = cmdId;
	}
	
	@Override
	public void updateUpgradeValue(User user, Island island) {
		UpgradesAddon upgradesAddon = this.getUpgradesAddon();
		UpgradesData islandData = upgradesAddon.getUpgradesLevels(island.getUniqueId());
		int upgradeLevel = islandData.getUpgradeLevel(this.getName());
		int numberPeople = island.getMemberSet().size();
		int islandLevel = upgradesAddon.getUpgradesManager().getIslandLevel(island);
		
		Map<String, Integer> upgradeInfos = upgradesAddon.getUpgradesManager().getCommandUpgradeInfos(this.cmdId, upgradeLevel, islandLevel, numberPeople, island.getWorld());
		UpgradeValues upgrade;
		
		String description = upgradesAddon.getUpgradesManager().getCommandUpgradeTierName(this.cmdId, upgradeLevel, island.getWorld()) + " (" + upgradeLevel + "/" +
				upgradesAddon.getUpgradesManager().getCommandUpgradeMax(this.cmdId, island.getWorld()) + ")";
		
		this.setOwnDescription(user, description);
		
		if (upgradeInfos == null)
			upgrade = null;
		else
			upgrade = new UpgradeValues(upgradeInfos.get("islandMinLevel"), upgradeInfos.get("vaultCost"), upgradeInfos.get("upgrade"));
		
		this.setUpgradeValues(user, upgrade);
	}
	
	@Override
	public boolean doUpgrade(User user, Island island) {
		UpgradesAddon upgradeAddon = this.getUpgradesAddon();
		UpgradesData islandData = upgradeAddon.getUpgradesLevels(island.getUniqueId());
		int upgradeLevel = islandData.getUpgradeLevel(this.getName());
		
		if (!super.doUpgrade(user, island))
			return false;
		
		List<String> commands = upgradeAddon.getUpgradesManager().getCommandList(this.cmdId, upgradeLevel, island.getWorld(), user.getName());
		Boolean isConsole = upgradeAddon.getUpgradesManager().isCommantConsole(this.cmdId, upgradeLevel, island.getWorld());
		
		commands.forEach(cmd -> {
			if (isConsole) {
				upgradeAddon.getServer().dispatchCommand(upgradeAddon.getServer().getConsoleSender(), cmd);
			} else {
				upgradeAddon.getServer().dispatchCommand(user.getSender(), cmd);
			}
		});
		return true;
	}
	
	private String cmdId;
	
}
