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
 * Represents an upgrade that increases the protection range of an island.
 * This class extends the {@link Upgrade} base class and provides functionality
 * for managing range upgrades within the BentoBox ecosystem.
 *
 * <p>The {@code RangeUpgrade} dynamically calculates upgrade levels, updates
 * protection ranges, and ensures that the new range stays within configured limits.</p>
 * 
 * <p>Administrators can configure this upgrade to allow players to extend their
 * island's protected area based on various factors such as the island's level,
 * number of members, and the current game mode.</p>
 * 
 * @author Ikkino, tastybento
 */
public class RangeUpgrade extends Upgrade {

    /**
     * Constructs a new {@code RangeUpgrade} instance.
     *
     * @param addon The instance of the {@code UpgradesAddon}.
     */
    public RangeUpgrade(UpgradesAddon addon) {
        super(addon, "RangeUpgrade", "RangeUpgrade", Material.OAK_FENCE);
    }

    /**
     * Updates the upgrade values when the user opens the upgrade interface.
     * This method dynamically calculates and sets the upgrade's details such as
     * the current level, maximum level, and associated costs.
     *
     * @param user The user viewing the upgrade interface.
     * @param island The island for which the upgrade is being viewed.
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
        int islandLevel = islandAddon.getUpgradesManager().getIslandLevel(island);

        // Get upgrades infos of range upgrade from settings
        Map<String, Integer> upgradeInfos = islandAddon.getUpgradesManager().getRangeUpgradeInfos(upgradeLevel,
                islandLevel, numberPeople, island.getWorld());
        UpgradeValues upgrade;

        // If null -> no next upgrades
        if (upgradeInfos == null) {
            upgrade = null;
        } else {
            // Get new description
            String description = user.getTranslation("upgrades.ui.upgradepanel.tiernameandlevel",
                    "[name]",  islandAddon.getUpgradesManager().getRangeUpgradeTierName(upgradeLevel, island.getWorld()),
                    "[current]", Integer.toString(upgradeLevel),
                    "[max]", Integer.toString(islandAddon.getUpgradesManager().getRangeUpgradeMax(island.getWorld())));

            // Set new description
            this.setOwnDescription(user, description);

            upgrade = new UpgradeValues(upgradeInfos.get("islandMinLevel"), upgradeInfos.get("vaultCost"), upgradeInfos.get("upgrade"));
        }
        // Update the upgrade values
        this.setUpgradeValues(user, upgrade);

        // Update the display name
        String newDisplayName;

        if (upgrade == null) {
            // No next upgrade -> lang message
            newDisplayName = user.getTranslation("upgrades.ui.upgradepanel.norangeupgrade");
        } else {
            // get lang message
            newDisplayName = user.getTranslation("upgrades.ui.upgradepanel.rangeupgrade", "[rangelevel]",
                    Integer.toString(upgrade.getUpgradeValue()));
        }

        this.setDisplayName(newDisplayName);
    }

    /**
     * Determines whether the upgrade should be displayed in the user's interface.
     * This involves checking user permissions and other contextual requirements.
     *
     * @param user The user requesting the visibility check.
     * @param island The island associated with the upgrade.
     * @return {@code true} if the upgrade should be visible; {@code false} otherwise.
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
        int permissionLevel = upgradesAddon.getUpgradesManager().getRangePermissionLevel(upgradeLevel,
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
     * Logs an error related to permissions or configuration issues.
     *
     * @param name The name of the player associated with the error.
     * @param perm The permission string causing the error.
     * @param error A description of the specific error to log.
     */
    private void logError(String name, String perm, String error) {
        this.getUpgradesAddon()
        .logError("Player " + name + " has permission: '" + perm + "' but " + error + " Ignoring...");
    }

    /**
     * Applies the range upgrade to the specified island.
     * Increases the island's protection range and triggers relevant events to
     * reflect the change. Ensures that the new range does not exceed the maximum limit.
     *
     * @param user The user performing the upgrade.
     * @param island The island to which the upgrade is applied.
     * @return {@code true} if the upgrade was successfully applied; {@code false} otherwise.
     */
    @Override
    public boolean doUpgrade(User user, Island island) {
        // Get the new range
        int newRange = island.getProtectionRange() + this.getUpgradeValues(user).getUpgradeValue();

        // If newRange is more than the authorized range (Config problem)
        if (newRange > island.getRange()) {
            this.getUpgradesAddon().logWarning(
                    "User tried to upgrade their island range over the max. This is probably a configuration problem.");
            user.sendMessage("upgrades.error.rangeovermax");
            return false;
        }

        // if super doUpgrade not worked
        if (!super.doUpgrade(user, island))
            return false;

        // Save oldRange for rangeChange event
        int oldRange = island.getProtectionRange();

        // Add range bonus
        island.addBonusRange(this.getUpgradesAddon().getDescription().getName(), this.getUpgradeValues(user).getUpgradeValue(), "");

        // Launch range change event
        IslandEvent.builder().island(island).location(island.getCenter()).reason(IslandEvent.Reason.RANGE_CHANGE)
        .involvedPlayer(user.getUniqueId()).admin(false).protectionRange(island.getProtectionRange(), oldRange).build();

        user.sendMessage("upgrades.ui.upgradepanel.rangeupgradedone", "[rangelevel]",
                Integer.toString(this.getUpgradeValues(user).getUpgradeValue()));

        return true;
    }

}
