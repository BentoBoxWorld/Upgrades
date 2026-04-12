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
import world.bentobox.upgrades.dataobjects.rewards.SpawnerRewardDB;

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
        return BonusCalculator.sum(addon, island, SpawnerRewardDB.class,
                SpawnerRewardDB::getSpawnBonusEquation);
    }
}
