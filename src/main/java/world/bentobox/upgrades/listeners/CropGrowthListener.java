package world.bentobox.upgrades.listeners;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;

import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.api.UpgradeAPI;
import world.bentobox.upgrades.config.Settings;
import world.bentobox.upgrades.dataobjects.UpgradeTier;
import world.bentobox.upgrades.dataobjects.UpgradesData;
import world.bentobox.upgrades.dataobjects.rewards.CropGrowthRewardDB;
import world.bentobox.upgrades.dataobjects.rewards.RewardDB;
import world.bentobox.upgrades.upgrades.DatabaseUpgrade;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CropGrowthListener implements Listener {

    private final UpgradesAddon addon;

    public CropGrowthListener(UpgradesAddon addon) {
        this.addon = addon;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockGrow(BlockGrowEvent event) {
        Block block = event.getBlock();
        if (!isCrop(block)) return;

        Island island = addon.getPlugin().getIslands()
                .getIslandAt(block.getLocation()).orElse(null);
        if (island == null) return;

        double bonus = computeBonus(island);
        if (bonus <= 0) return;

        int guaranteed = (int) bonus;
        double chance = bonus - guaranteed;
        int extra = guaranteed + (Math.random() < chance ? 1 : 0);
        if (extra <= 0) return;

        Bukkit.getScheduler().runTask(addon.getPlugin(), () -> {
            for (int i = 0; i < extra; i++) {
                block.applyBoneMeal(BlockFace.UP);
            }
        });
    }

    private boolean isCrop(Block block) {
        return switch (block.getType()) {
            case WHEAT, CARROTS, POTATOES, BEETROOTS, NETHER_WART,
                 SWEET_BERRY_BUSH, TORCHFLOWER_CROP, PITCHER_CROP -> true;
            default -> false;
        };
    }

    private double computeBonus(Island island) {
        double total = 0.0;
        long islandLevel = addon.getUpgradesManager().getIslandLevel(island);
        int memberCount = island.getMemberSet().size();

        for (UpgradeAPI upgradeAPI : addon.getAvailableUpgrades()) {
            if (!(upgradeAPI instanceof DatabaseUpgrade)) continue;
            DatabaseUpgrade dbUpgrade = (DatabaseUpgrade) upgradeAPI;

            UpgradesData data = addon.getUpgradesLevels(island.getUniqueId());
            int currentLevel = data.getUpgradeLevel(dbUpgrade.getName());
            if (currentLevel <= 0) continue;

            // Find the most recently purchased tier (startLevel <= currentLevel-1 <= endLevel)
            List<UpgradeTier> tiers = addon.getUpgradeDataManager()
                    .getUpgradeTierByUpgradeData(dbUpgrade.getUpgradeData());
            UpgradeTier activeTier = null;
            for (UpgradeTier tier : tiers) {
                if (tier.getStartLevel() <= currentLevel - 1 && currentLevel - 1 <= tier.getEndLevel()) {
                    activeTier = tier;
                    break;
                }
            }
            if (activeTier == null) continue;

            for (RewardDB rewardDB : activeTier.getRewards()) {
                if (!(rewardDB instanceof CropGrowthRewardDB)) continue;
                CropGrowthRewardDB cropDB = (CropGrowthRewardDB) rewardDB;

                Map<String, Double> vars = new TreeMap<>();
                vars.put("[level]", (double) currentLevel);
                vars.put("[islandLevel]", (double) islandLevel);
                vars.put("[numberPlayer]", (double) memberCount);
                try {
                    total += Settings.evaluate(cropDB.getGrowthBonusEquation(), vars);
                } catch (Exception ignored) {
                    // Malformed formula — skip
                }
            }
        }
        return total;
    }
}
