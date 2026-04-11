package world.bentobox.upgrades.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SpawnerSpawnEvent;

import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.api.UpgradeAPI;
import world.bentobox.upgrades.config.Settings;
import world.bentobox.upgrades.dataobjects.UpgradeTier;
import world.bentobox.upgrades.dataobjects.UpgradesData;
import world.bentobox.upgrades.dataobjects.rewards.RewardDB;
import world.bentobox.upgrades.dataobjects.rewards.SpawnerRewardDB;
import world.bentobox.upgrades.upgrades.DatabaseUpgrade;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SpawnerUpgradeListener implements Listener {

    private final UpgradesAddon addon;

    public SpawnerUpgradeListener(UpgradesAddon addon) {
        this.addon = addon;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpawnerSpawn(SpawnerSpawnEvent event) {
        Island island = addon.getPlugin().getIslands()
                .getIslandAt(event.getLocation()).orElse(null);
        if (island == null) return;

        double bonus = computeBonus(island);
        if (bonus <= 0) return;

        int guaranteed = (int) bonus;
        double chance = bonus - guaranteed;
        int extra = guaranteed + (Math.random() < chance ? 1 : 0);
        if (extra <= 0) return;

        EntityType type = event.getEntityType();
        Location loc = event.getLocation();
        // Schedule 1 tick later to avoid SpawnerSpawnEvent recursion
        Bukkit.getScheduler().runTask(addon.getPlugin(), () -> {
            for (int i = 0; i < extra; i++) {
                loc.getWorld().spawnEntity(loc, type);
            }
        });
    }

    private double computeBonus(Island island) {
        double total = 0.0;
        long islandLevel = addon.getUpgradesManager().getIslandLevel(island);
        int memberCount = island.getMemberSet().size();
        UpgradesData data = addon.getUpgradesLevels(island.getUniqueId());

        for (UpgradeAPI upgradeAPI : addon.getAvailableUpgrades()) {
            if (!(upgradeAPI instanceof DatabaseUpgrade)) continue;
            DatabaseUpgrade dbUpgrade = (DatabaseUpgrade) upgradeAPI;

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
                if (!(rewardDB instanceof SpawnerRewardDB)) continue;
                SpawnerRewardDB spawnerDB = (SpawnerRewardDB) rewardDB;

                Map<String, Double> vars = new TreeMap<>();
                vars.put("[level]", (double) currentLevel);
                vars.put("[islandLevel]", (double) islandLevel);
                vars.put("[numberPlayer]", (double) memberCount);
                try {
                    total += Settings.evaluate(spawnerDB.getSpawnBonusEquation(), vars);
                } catch (Exception ignored) {
                    // Malformed formula — skip
                }
            }
        }
        return total;
    }
}
