package world.bentobox.upgrades.upgrades;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;

import world.bentobox.limits.objects.IslandBlockCount;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.dataobjects.UpgradesData;

public class BlockLimitsUpgrade extends LimitsUpgrade {

    private Material block;

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
        int islandLevel = upgradeAddon.getUpgradesManager().getIslandLevel(island);

        Map<String, Integer> upgradeInfos = upgradeAddon.getUpgradesManager().getBlockLimitsUpgradeInfos(this.block,
                upgradeLevel, islandLevel, numberPeople, island.getWorld());
        UpgradeValues upgrade;

        if (upgradeInfos == null) {
            upgrade = null;
            this.setOwnDescription(user, null);
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
            newDisplayName = user.getTranslation("upgrades.ui.upgradepanel.nolimitsupgrade", BLOCK,
                    this.block.toString());
        } else {
            newDisplayName = user.getTranslation("upgrades.ui.upgradepanel.limitsupgrade", BLOCK,
                    this.block.toString(), LEVEL, Integer.toString(upgrade.getUpgradeValue()));
        }

        this.setDisplayName(newDisplayName);
    }

    @Override
    protected int getPermissionLevel(int upgradeLevel, World world) {
        return this.getUpgradesAddon().getUpgradesManager().getBlockLimitsPermissionLevel(this.block, upgradeLevel,
                world);
    }

    @Override
    protected void applyOffset(IslandBlockCount isb, Environment env, int amount) {
        isb.setBlockLimitsOffset(env, block.getKey(), isb.getBlockLimitOffset(env, block.getKey()) + amount);
    }

    @Override
    protected String getTargetName() {
        return this.block.toString();
    }

}
