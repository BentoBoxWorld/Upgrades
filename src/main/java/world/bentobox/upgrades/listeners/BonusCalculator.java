package world.bentobox.upgrades.listeners;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.api.UpgradeAPI;
import world.bentobox.upgrades.config.Settings;
import world.bentobox.upgrades.dataobjects.FormulaVariables;
import world.bentobox.upgrades.dataobjects.UpgradeTier;
import world.bentobox.upgrades.dataobjects.UpgradesData;
import world.bentobox.upgrades.dataobjects.rewards.RewardDB;
import world.bentobox.upgrades.upgrades.DatabaseUpgrade;

final class BonusCalculator {

    private BonusCalculator() {
    }

    /**
     * Sum the per-reward equation across every purchased tier on the island
     * whose rewards match {@code rewardType}. Malformed formulas are skipped.
     */
    static <R extends RewardDB> double sum(UpgradesAddon addon, Island island,
                                           Class<R> rewardType,
                                           Function<R, String> equationExtractor) {
        if (island == null) {
            return 0.0;
        }

        double total = 0.0;
        long islandLevel = addon.getUpgradesManager().getIslandLevel(island);
        int memberCount = island.getMemberSet().size();
        UpgradesData data = addon.getUpgradesLevels(island.getUniqueId());

        for (UpgradeAPI upgradeAPI : addon.getAvailableUpgrades()) {
            if (upgradeAPI instanceof DatabaseUpgrade dbUpgrade) {
                int currentLevel = data.getUpgradeLevel(dbUpgrade.getName());
                UpgradeTier activeTier = currentLevel <= 0 ? null
                        : findActiveTier(addon, dbUpgrade, currentLevel);
                if (activeTier != null) {
                    total += sumTierRewards(activeTier, rewardType, equationExtractor,
                            currentLevel, islandLevel, memberCount);
                }
            }
        }
        return total;
    }

    private static UpgradeTier findActiveTier(UpgradesAddon addon, DatabaseUpgrade dbUpgrade,
                                              int currentLevel) {
        List<UpgradeTier> tiers = addon.getUpgradeDataManager()
                .getUpgradeTierByUpgradeData(dbUpgrade.getUpgradeData());
        for (UpgradeTier tier : tiers) {
            if (tier.getStartLevel() <= currentLevel - 1 && currentLevel - 1 <= tier.getEndLevel()) {
                return tier;
            }
        }
        return null;
    }

    private static <R extends RewardDB> double sumTierRewards(UpgradeTier tier, Class<R> rewardType,
                                                              Function<R, String> equationExtractor,
                                                              int currentLevel, long islandLevel,
                                                              int memberCount) {
        Map<String, Double> vars = FormulaVariables.of(currentLevel, islandLevel, memberCount);

        double subtotal = 0.0;
        for (RewardDB rewardDB : tier.getRewards()) {
            if (!rewardType.isInstance(rewardDB)) continue;
            try {
                subtotal += Settings.evaluate(equationExtractor.apply(rewardType.cast(rewardDB)), vars);
            } catch (Exception ignored) {
                // Malformed formula — skip
            }
        }
        return subtotal;
    }
}
