package world.bentobox.upgrades.upgrades;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.limits.objects.IslandBlockCount;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.api.UpgradeAPI;
import world.bentobox.upgrades.dataobjects.UpgradesData;

/**
 * Base class for upgrades backed by the Limits addon (block, entity and
 * entity-group limits). Holds the shared permission gating and the
 * offset-raising purchase flow; subclasses only supply the target-specific
 * lookups.
 */
public abstract class LimitsUpgrade extends UpgradeAPI {

    protected static final String BLOCK_PLACEHOLDER = "[block]";
    protected static final String LEVEL_PLACEHOLDER = "[level]";

    protected LimitsUpgrade(UpgradesAddon addon, String name, String displayName, Material icon) {
        super(addon, name, displayName, icon);
    }

    /**
     * @param upgradeLevel The current level of this upgrade on the island.
     * @param world The world the island is in.
     * @return The permission level required for this upgrade, or 0 if it is open to everyone.
     */
    protected abstract int getPermissionLevel(int upgradeLevel, World world);

    /**
     * Raises this upgrade's limit offset by {@code amount} in the given environment.
     *
     * @param isb The island's block count data.
     * @param env The environment to raise the offset in.
     * @param amount The amount to add to the current offset.
     */
    protected abstract void applyOffset(IslandBlockCount isb, Environment env, int amount);

    /**
     * @return The name of the limited block, entity or group, for player messages.
     */
    protected abstract String getTargetName();

    /**
     * Determines whether this upgrade should be displayed to the user.
     * Checks the visibility conditions for the upgrade, including permissions and other
     * contextual requirements, ensuring that only valid upgrades are shown.
     *
     * @param user The user requesting the visibility check.
     * @param island The island associated with the upgrade.
     * @return {@code true} if the upgrade should be displayed; {@code false} otherwise.
     */
    @Override
    public boolean isShowed(User user, Island island) {
        // Get the data from upgrades
        UpgradesData islandData = this.getUpgradesAddon().getUpgradesLevels(island.getUniqueId());
        // Get level of the upgrade
        int upgradeLevel = islandData.getUpgradeLevel(this.getName());
        // Permission level required
        int permissionLevel = this.getPermissionLevel(upgradeLevel, island.getWorld());

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
     * @param error A description of the specific error to log.
     */
    private void logError(String name, String perm, String error) {
        this.getUpgradesAddon()
        .logError("Player " + name + " has permission: '" + perm + "' but " + error + " Ignoring...");
    }

    /**
     * Performs the upgrade for the specified user and island.
     * This method applies the upgrade by increasing the limits for the target
     * and updating the island's limit data accordingly.
     *
     * @param user The user performing the upgrade.
     * @param island The island on which the upgrade is applied.
     * @return {@code true} if the upgrade was successfully applied; {@code false} otherwise.
     */
    @Override
    public boolean doUpgrade(User user, Island island) {
        UpgradesAddon islandAddon = this.getUpgradesAddon();

        if (!islandAddon.isLimitsProvided())
            return false;

        IslandBlockCount isb = islandAddon.getLimitsAddon().getBlockLimitListener().getIsland(island);

        if (!super.doUpgrade(user, island))
            return false;

        // Upgrades are island-wide, so raise the offset in every environment
        int amount = this.getUpgradeValues(user).getUpgradeValue();
        for (Environment env : Environment.values()) {
            this.applyOffset(isb, env, amount);
        }

        user.sendMessage("upgrades.ui.upgradepanel.limitsupgradedone", BLOCK_PLACEHOLDER, this.getTargetName(), LEVEL_PLACEHOLDER,
                Integer.toString(amount));

        return true;
    }

}
