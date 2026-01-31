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
import world.bentobox.upgrades.api.Upgrade;
import world.bentobox.upgrades.dataobjects.UpgradesData;

/**
 * Represents an upgrade that executes a specific command when applied.
 * This class extends the base functionality of {@link Upgrade} to support
 * command-based upgrades for islands or players.
 *
 * <p>The {@code CommandUpgrade} class enables server administrators to
 * configure upgrades that trigger predefined commands, allowing for
 * customized upgrade behaviors.</p>
 */
public class CommandUpgrade extends Upgrade {
	
    private String cmdId;

    /**
     * Constructs a new {@code CommandUpgrade} instance.
     *
     * @param addon The instance of the {@code UpgradesAddon}.
     * @param cmdId The command
     * @param icon The material to represent the upgrade visually in the UI.
     */
    public CommandUpgrade(UpgradesAddon addon, String cmdId, Material icon) {
		super(addon, "command-" + cmdId, addon.getSettings().getCommandName(cmdId), icon);
		this.cmdId = cmdId;
	}
	
    /**
     * Updates the upgrade values for the specified user and island.
     * This method sets the upgrade's display name and other relevant
     * attributes based on the current configuration and context.
     *
     * @param user The user for whom the upgrade values are being updated.
     * @param island The island associated with the upgrade.
     */
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
	
    /**
     * Determines whether this upgrade should be displayed to the user.
     * Checks the visibility conditions for the upgrade, including
     * permissions and other contextual requirements.
     *
     * @param user The user requesting the visibility check.
     * @param island The island associated with the upgrade.
     * @return {@code true} if the upgrade should be displayed; {@code false} otherwise.
     */
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
	
    /**
     * Logs an error message for issues related to the command upgrade configuration.
     *
     * @param user The user associated with the error.
     * @param command The command causing the error.
     * @param message The specific error message to log.
     */
	private void logError(String name, String perm, String error) {
        this.getUpgradesAddon().logError("Player " + name + " has permission: '" + perm + "' but " + error + " Ignoring...");
    }
	
    /**
     * Executes the command associated with the upgrade.
     * This method is triggered when the upgrade is applied and performs
     * the configured command action.
     *
     * @param user The user triggering the upgrade.
     * @param island The island on which the upgrade is applied.
     * @return {@code true} if the command was executed successfully; {@code false} otherwise.
     */
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
	
}
