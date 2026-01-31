package world.bentobox.upgrades.upgrades;

import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;

import world.bentobox.limits.listeners.BlockLimitsListener;
import world.bentobox.limits.objects.IslandBlockCount;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.api.Upgrade;
import world.bentobox.upgrades.dataobjects.UpgradesData;

/**
 * An upgrade of limits for a specific block type
 */
public class BlockLimitsUpgrade extends Upgrade {

    private Material block;

    /**
     * Initializes a BlockLimitsUpgrade for a specific block type.
     *
     * @param addon The instance of the UpgradesAddon.
     * @param block The Material representing the block type for this upgrade.
     */
    public BlockLimitsUpgrade(UpgradesAddon addon, Material block) {
        super(addon, "LimitsUpgrade-" + block.toString(), block.toString() + " limits Upgrade", block);
        this.block = block;
    }

    /**
     * Updates the upgrade values for the specified user and island.
     * This includes calculating the upgrade level, adjusting descriptions,
     * and setting the display name for the upgrade based on current configurations.
     *
     * @param user The user for whom the upgrade values are being updated.
     * @param island The island associated with the upgrade.
     */
    @Override
    public void updateUpgradeValue(User user, Island island) {
        UpgradesAddon upgradeAddon = this.getUpgradesAddon();
        UpgradesData islandData = upgradeAddon.getUpgradesLevels(island.getUniqueId());
        int upgradeLevel = islandData.getUpgradeLevel(getName());
        int numberPeople = island.getMemberSet().size();
        int islandLevel = upgradeAddon.getUpgradesManager().getIslandLevel(island);

        Map<String, Integer> upgradeInfos = upgradeAddon.getUpgradesManager().getBlockLimitsUpgradeInfos(this.block,
                upgradeLevel, islandLevel, numberPeople, island.getWorld());
        UpgradeValues upgrade;

        if (upgradeInfos == null) {
            upgrade = null;
        } else {
            // Get new description
            String description =  user.getTranslation("upgrades.ui.upgradepanel.tiernameandlevel",
                    "[name]", upgradeAddon.getUpgradesManager().getBlockLimitsUpgradeTierName(this.block, upgradeLevel, island.getWorld()),
                    "[current]", Integer.toString(upgradeLevel),
                    "[max]", Integer.toString(upgradeAddon.getUpgradesManager().getBlockLimitsUpgradeMax(this.block, island.getWorld())));

            // Set new description
            this.setOwnDescription(user, description);

            upgrade = new UpgradeValues(upgradeInfos.get("islandMinLevel"), upgradeInfos.get("vaultCost"),
                    upgradeInfos.get("upgrade"));
        }

        this.setUpgradeValues(user, upgrade);

        String newDisplayName;

        if (upgrade == null) {
            newDisplayName = user.getTranslation("upgrades.ui.upgradepanel.nolimitsupgrade", "[block]",
                    this.block.toString());
        } else {
            newDisplayName = user.getTranslation("upgrades.ui.upgradepanel.limitsupgrade", "[block]",
                    this.block.toString(), "[level]", Integer.toString(upgrade.getUpgradeValue()));
        }

        this.setDisplayName(newDisplayName);
    }

    /**
     * Determines whether this upgrade should be displayed to the user.
     * Checks permissions and other configurations to ensure visibility.
     *
     * @param user The user requesting the visibility check.
     * @param island The island associated with the upgrade.
     * @return true if the upgrade should be displayed; false otherwise.
     */
    @Override
    public boolean isShowed(User user, Island island) {
        // Get the addon
        UpgradesAddon upgradesAddon = this.getUpgradesAddon();
        // Get the data from upgrades
        UpgradesData islandData = upgradesAddon.getUpgradesLevels(island.getUniqueId());
        // Get level of the upgrade
        int upgradeLevel = islandData.getUpgradeLevel(this.getName());
        // Permission level required
        int permissionLevel = upgradesAddon.getUpgradesManager().getBlockLimitsPermissionLevel(this.block, upgradeLevel,
                island.getWorld());

        // If default permission, then true
        if (permissionLevel == 0)
            return true;

        Player player = user.getPlayer();
        String gamemode = island.getGameMode();
        String permissionStart = gamemode + ".upgrades." + this.getName() + ".";
        permissionStart = permissionStart.toLowerCase();

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

    /**
     * Logs an error message for issues related to permissions or configurations.
     *
     * @param name The name of the player associated with the error.
     * @param perm The permission string causing the error.
     * @param error The specific error message to log.
     */
    private void logError(String name, String perm, String error) {
        this.getUpgradesAddon()
        .logError("Player " + name + " has permission: '" + perm + "' but " + error + " Ignoring...");
    }

    /**
     * Performs the upgrade for the specified user and island.
     * This involves applying the upgrade's effects, such as modifying block limits.
     *
     * @param user The user performing the upgrade.
     * @param island The island to which the upgrade is being applied.
     * @return true if the upgrade is successfully applied; false otherwise.
     */
    @Override
    public boolean doUpgrade(User user, Island island) {
        UpgradesAddon islandAddon = this.getUpgradesAddon();

        if (!islandAddon.isLimitsProvided())
            return false;

        BlockLimitsListener bLListener = islandAddon.getLimitsAddon().getBlockLimitListener();
        IslandBlockCount isb = bLListener.getIsland(island);

        if (!super.doUpgrade(user, island))
            return false;

        int oldCount = isb.getBlockLimitsOffset().getOrDefault(block, 0);
        int newCount = oldCount + this.getUpgradeValues(user).getUpgradeValue();
        isb.setBlockLimitsOffset(block, newCount);

        user.sendMessage("upgrades.ui.upgradepanel.limitsupgradedone", "[block]", this.block.toString(), "[level]",
                Integer.toString(this.getUpgradeValues(user).getUpgradeValue()));

        return true;
    }

}
