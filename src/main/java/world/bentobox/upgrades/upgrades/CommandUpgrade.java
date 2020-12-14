package world.bentobox.upgrades.upgrades;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.api.UpgradeAPI;
import world.bentobox.upgrades.dataobjects.UpgradesData;

public class CommandUpgrade extends UpgradeAPI {
	
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

		if (upgradeInfos == null) {
			upgrade = null;
		} else {
			String description = user.getTranslation("upgrades.ui.upgradepanel.tiernameandlevel",
					"[name]",  upgradesAddon.getUpgradesManager().getCommandUpgradeTierName(this.cmdId, upgradeLevel, island.getWorld()),
					"[current]", Integer.toString(upgradeLevel),
					"[max]", Integer.toString(upgradesAddon.getUpgradesManager().getCommandUpgradeMax(this.cmdId, island.getWorld())));
			
			this.setOwnDescription(user, description);
		
			upgrade = new UpgradeValues(upgradeInfos.get("islandMinLevel"), upgradeInfos.get("vaultCost"), upgradeInfos.get("upgrade"));
		}
		
		this.setUpgradeValues(user, upgrade);
	}
	
	@Override
	public boolean isShowed(User user, Island island) {
		UpgradesAddon upgradeAddon = this.getUpgradesAddon();
		UpgradesData islandData = upgradeAddon.getUpgradesLevels(island.getUniqueId());
		int upgradeLevel = islandData.getUpgradeLevel(this.cmdId);
		int permissionLevel = upgradeAddon.getUpgradesManager().getCommandPermissionLevel(this.cmdId, upgradeLevel, island.getWorld());
		
		if (permissionLevel == 0)
			return true;
		
		Player player = user.getPlayer();
		String gamemode = island.getGameMode();
		String permissionStart = gamemode + ".upgrades." + this.getName() + ".";
		permissionStart = permissionStart.toLowerCase();
		
		upgradeAddon.log("permission: " + permissionStart);
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
	
	@Override
	public boolean doUpgrade(User user, Island island) {
		UpgradesAddon upgradeAddon = this.getUpgradesAddon();
		UpgradesData islandData = upgradeAddon.getUpgradesLevels(island.getUniqueId());
		int upgradeLevel = islandData.getUpgradeLevel(this.getName());
		
		if (!super.doUpgrade(user, island))
			return false;
		
		List<String> commands = upgradeAddon.getUpgradesManager().getCommandList(this.cmdId, upgradeLevel, island, user.getName());
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
