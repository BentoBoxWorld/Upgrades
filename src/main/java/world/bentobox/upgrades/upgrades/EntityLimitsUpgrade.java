package world.bentobox.upgrades.upgrades;

import java.util.Map;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.EntityType;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.limits.objects.IslandBlockCount;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.dataobjects.UpgradesData;

public class EntityLimitsUpgrade extends LimitsUpgrade {

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
            this.setOwnDescription(user, null);
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
            newDisplayName = user.getTranslation("upgrades.ui.upgradepanel.nolimitsupgrade", BLOCK,
                    this.entity.toString());
        } else {
            newDisplayName = user.getTranslation("upgrades.ui.upgradepanel.limitsupgrade", BLOCK,
                    this.entity.toString(), LEVEL, Integer.toString(upgrade.getUpgradeValue()));
        }

        this.setDisplayName(newDisplayName);
    }

    @Override
    protected int getPermissionLevel(int upgradeLevel, World world) {
        return this.getUpgradesAddon().getUpgradesManager().getEntityLimitsPermissionLevel(this.entity, upgradeLevel,
                world);
    }

    @Override
    protected void applyOffset(IslandBlockCount isb, Environment env, int amount) {
        isb.setEntityLimitsOffset(env, this.entity, isb.getEntityLimitOffset(env, this.entity) + amount);
    }

    @Override
    protected String getTargetName() {
        return this.entity.toString();
    }

    private EntityType entity;

}
