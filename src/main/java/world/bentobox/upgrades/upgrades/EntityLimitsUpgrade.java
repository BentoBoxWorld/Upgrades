package world.bentobox.upgrades.upgrades;

import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.entity.EntityType;
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
 * Represents an upgrade that modifies the limits for a specific entity type on an island.
 * This class extends the {@link Upgrade} base class, enabling administrators to configure
 * entity-specific limit upgrades for the BentoBox ecosystem.
 *
 * <p>The {@code EntityLimitsUpgrade} class allows for the customization and management
 * of entity limits based on upgrade levels, island configurations, and permissions.</p>
 */
public class EntityLimitsUpgrade extends Upgrade {

    /**
     * Constructs a new {@code EntityLimitsUpgrade} instance for a specific entity type.
     *
     * @param addon The instance of the {@code UpgradesAddon}.
     * @param entity The {@link EntityType} associated with this upgrade.
     */
    public EntityLimitsUpgrade(UpgradesAddon addon, EntityType entity) {
        super(addon, "LimitsUpgrade-" + entity.toString(), entity.toString() + " limits Upgrade",
                addon.getSettings().getEntityIcon(entity));
        this.entity = entity;
    }

    /**
     * Updates the upgrade values for the specified user and island.
     * This method calculates and sets the upgrade's description, display name, and other
     * relevant attributes based on the current upgrade level and island context.
     *
     * @param user The user for whom the upgrade values are being updated.
     * @param island The island associated with the upgrade.
     */
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

        Map<String, Integer> upgradeInfos = upgradeAddon.getUpgradesManager().getEntityLimitsUpgradeInfos(this.entity,
                upgradeLevel, islandLevel, numberPeople, island.getWorld());
        UpgradeValues upgrade;

        if (upgradeInfos == null) {
            upgrade = null;
        } else {
            // Get new description
            String description = user.getTranslation("upgrades.ui.upgradepanel.tiernameandlevel",
                    "[name]", upgradeAddon.getUpgradesManager().getEntityLimitsUpgradeTierName(this.entity, upgradeLevel, island.getWorld()),
                    "[current]", Integer.toString(upgradeLevel),
                    "[max]", Integer.toString(upgradeAddon.getUpgradesManager().getEntityLimitsUpgradeMax(this.entity, island.getWorld())));

            // Set new description
            this.setOwnDescription(user, description);

            upgrade = new UpgradeValues(upgradeInfos.get("islandMinLevel"), upgradeInfos.get("vaultCost"), upgradeInfos.get("upgrade"));
        }

        this.setUpgradeValues(user, upgrade);

        String newDisplayName;

        if (upgrade == null) {
            newDisplayName = user.getTranslation("upgrades.ui.upgradepanel.nolimitsupgrade", "[block]",
                    this.entity.toString());
        } else {
            newDisplayName = user.getTranslation("upgrades.ui.upgradepanel.limitsupgrade", "[block]",
                    this.entity.toString(), "[level]", Integer.toString(upgrade.getUpgradeValue()));
        }

        this.setDisplayName(newDisplayName);
    }

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
        // Get the addon
        UpgradesAddon upgradesAddon = this.getUpgradesAddon();
        // Get the data from upgrades
        UpgradesData islandData = upgradesAddon.getUpgradesLevels(island.getUniqueId());
        // Get level of the upgrade
        int upgradeLevel = islandData.getUpgradeLevel(this.getName());
        // Permission level required
        int permissionLevel = upgradesAddon.getUpgradesManager().getEntityLimitsPermissionLevel(this.entity,
                upgradeLevel, island.getWorld());

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
     * This method applies the upgrade by increasing the limits for the specified entity type
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

        BlockLimitsListener bLListener = islandAddon.getLimitsAddon().getBlockLimitListener();
        IslandBlockCount isb = bLListener.getIsland(island);
        if (!super.doUpgrade(user, island))
            return false;
        int oldCount = isb.getEntityLimitsOffset().getOrDefault(entity, 0);
        int newCount = oldCount + this.getUpgradeValues(user).getUpgradeValue();

        isb.setEntityLimitsOffset(this.entity, newCount);

        user.sendMessage("upgrades.ui.upgradepanel.limitsupgradedone", "[block]", this.entity.toString(), "[level]",
                Integer.toString(this.getUpgradeValues(user).getUpgradeValue()));

        return true;
    }

    private EntityType entity;

}
