package world.bentobox.upgrades.listeners;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.api.UpgradeAPI;
import world.bentobox.upgrades.config.Settings;
import world.bentobox.upgrades.dataobjects.UpgradeTier;
import world.bentobox.upgrades.dataobjects.UpgradesData;
import world.bentobox.upgrades.dataobjects.rewards.Reward;
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
        double total = 0.0;
        long islandLevel = addon.getUpgradesManager().getIslandLevel(island);
        int memberCount = island.getMemberSet().size();
        UpgradesData data = addon.getUpgradesLevels(island.getUniqueId());

        for (UpgradeAPI upgradeAPI : addon.getAvailableUpgrades()) {
            if (!(upgradeAPI instanceof DatabaseUpgrade dbUpgrade)) continue;

            int currentLevel = data.getUpgradeLevel(dbUpgrade.getName());
            if (currentLevel <= 0) continue;

            UpgradeTier activeTier = findActiveTier(addon, dbUpgrade, currentLevel);
            if (activeTier == null) continue;

            total += sumTierRewards(activeTier, rewardType, equationExtractor,
                    currentLevel, islandLevel, memberCount);
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
        Map<String, Double> vars = new TreeMap<>();
        vars.put(Reward.LEVEL_VAR, (double) currentLevel);
        vars.put(Reward.ISLAND_LEVEL_VAR, (double) islandLevel);
        vars.put(Reward.NUMBER_PLAYER_VAR, (double) memberCount);

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
