package world.bentobox.upgrades.upgrades;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.api.UpgradeAPI;
import world.bentobox.upgrades.dataobjects.UpgradeData;
import world.bentobox.upgrades.dataobjects.UpgradeTier;
import world.bentobox.upgrades.dataobjects.UpgradesData;
import world.bentobox.upgrades.dataobjects.prices.Price;
import world.bentobox.upgrades.dataobjects.prices.PriceDB;
import world.bentobox.upgrades.dataobjects.rewards.Reward;
import world.bentobox.upgrades.dataobjects.rewards.RewardDB;

import java.util.List;

/**
 * Bridge between database-configured UpgradeData/UpgradeTier and the player shop.
 */
public class DatabaseUpgrade extends UpgradeAPI {

    private final UpgradeData upgradeData;

    public DatabaseUpgrade(UpgradesAddon addon, UpgradeData upgradeData) {
        super(addon, upgradeData.getUniqueId(), upgradeData.getName(),
                upgradeData.getIcon().getType());
        this.upgradeData = upgradeData;
    }

    @Override
    public void updateUpgradeValue(User user, Island island) {
        UpgradesData data = this.getUpgradesAddon().getUpgradesLevels(island.getUniqueId());
        int currentLevel = data.getUpgradeLevel(this.getName());

        UpgradeTier nextTier = findNextTier(currentLevel);

        if (nextTier == null) {
            // Max level reached — clear both so Panel shows "Max level"
            this.setOwnDescription(user, null);
            this.setUpgradeValues(user, null);
            this.setDisplayName(upgradeData.getName());
            return;
        }

        // Build description from prices and rewards, using DB-aware overloads so that
        // formula placeholders (e.g. [amount] in MoneyPrice) are substituted correctly.
        StringBuilder sb = new StringBuilder();
        for (PriceDB priceDB : nextTier.getPrices()) {
            Price price = this.getUpgradesAddon().getUpgradesManager().searchPrice(priceDB.getPriceType());
            if (price != null) {
                sb.append(price.getPublicDescription(user, priceDB)).append("\n");
            }
        }
        for (RewardDB rewardDB : nextTier.getRewards()) {
            Reward reward = this.getUpgradesAddon().getUpgradesManager().searchReward(rewardDB.getRewardType());
            if (reward != null) {
                sb.append(reward.getPublicDescription(user, rewardDB)).append("\n");
            }
        }

        String description = sb.toString().trim();
        // Always set a non-null ownDescription when a tier is found.
        // PanelClick uses (upgradeValues == null && ownDescription == null) to detect
        // the maxed-out state, so ownDescription must be non-null here even when the
        // tier has no prices or rewards configured (free upgrade).
        this.setOwnDescription(user, description.isEmpty() ? nextTier.getName() : description);
        // Do NOT set upgradeValues so Panel skips legacy vault/level display
        this.setUpgradeValues(user, null);
        this.setDisplayName(upgradeData.getName());
    }

    @Override
    public boolean canUpgrade(User user, Island island) {
        UpgradesData data = this.getUpgradesAddon().getUpgradesLevels(island.getUniqueId());
        int currentLevel = data.getUpgradeLevel(this.getName());

        UpgradeTier nextTier = findNextTier(currentLevel);
        if (nextTier == null) return false;

        for (PriceDB priceDB : nextTier.getPrices()) {
            Price price = this.getUpgradesAddon().getUpgradesManager().searchPrice(priceDB.getPriceType());
            if (price == null) continue;
            if (!price.canPay(this.getUpgradesAddon(), user, island, priceDB, currentLevel)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean doUpgrade(User user, Island island) {
        UpgradesData data = this.getUpgradesAddon().getUpgradesLevels(island.getUniqueId());
        int currentLevel = data.getUpgradeLevel(this.getName());

        UpgradeTier nextTier = findNextTier(currentLevel);
        if (nextTier == null) return false;

        // Collect prices first to check for issues before paying
        for (PriceDB priceDB : nextTier.getPrices()) {
            Price price = this.getUpgradesAddon().getUpgradesManager().searchPrice(priceDB.getPriceType());
            if (price == null) continue;
            price.pay(this.getUpgradesAddon(), user, island, priceDB, currentLevel);
        }

        for (RewardDB rewardDB : nextTier.getRewards()) {
            Reward reward = this.getUpgradesAddon().getUpgradesManager().searchReward(rewardDB.getRewardType());
            if (reward == null) continue;
            reward.apply(this.getUpgradesAddon(), user, island, rewardDB, currentLevel);
        }

        data.setUpgradeLevel(this.getName(), currentLevel + 1);
        return true;
    }

    @Override
    public boolean isShowed(User user, Island island) {
        if (!upgradeData.isActive()) return false;
        // Don't show upgrades with no tiers configured — they'd appear "maxed out"
        List<UpgradeTier> tiers = this.getUpgradesAddon().getUpgradeDataManager()
                .getUpgradeTierByUpgradeData(upgradeData);
        if (tiers.isEmpty()) return false;
        return this.getUpgradesAddon().getPlugin().getIWM().getAddon(island.getWorld())
                .map(a -> a.getDescription().getName())
                .orElse("")
                .equals(upgradeData.getWorld());
    }

    public UpgradeData getUpgradeData() {
        return upgradeData;
    }

    /**
     * Find the tier that covers the current level (i.e. the next purchase available).
     * Level 0 = not yet purchased; a tier with startLevel=0, endLevel=0 means one purchase.
     */
    private UpgradeTier findNextTier(int currentLevel) {
        List<UpgradeTier> tiers = this.getUpgradesAddon().getUpgradeDataManager()
                .getUpgradeTierByUpgradeData(upgradeData);
        for (UpgradeTier tier : tiers) {
            if (tier.getStartLevel() <= currentLevel && currentLevel <= tier.getEndLevel()) {
                return tier;
            }
        }
        return null;
    }
}
