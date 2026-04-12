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
import world.bentobox.upgrades.dataobjects.rewards.CropGrowthRewardDB;

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
        return BonusCalculator.sum(addon, island, CropGrowthRewardDB.class,
                CropGrowthRewardDB::getGrowthBonusEquation);
    }
}
