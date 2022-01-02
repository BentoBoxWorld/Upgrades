package world.bentobox.upgrades.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import world.bentobox.bentobox.api.events.island.IslandDeleteEvent;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.limits.events.LimitsJoinPermCheckEvent;
import world.bentobox.upgrades.UpgradesAddon;

public class IslandChangeListener implements Listener {

    public IslandChangeListener(UpgradesAddon addon) {
        this.addon = addon;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onLimitsJoinPermCheckEvent(LimitsJoinPermCheckEvent e) {
        // Stop LimitsJoinPermCheck else reset limits upgrades when player join
        //e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onIslandDeleteEvent(IslandDeleteEvent e) {
        Island island = e.getIsland();
        this.addon.uncacheIsland(island.getUniqueId(), false);
        this.addon.getDatabase().deleteID(island.getUniqueId());
    }

    private UpgradesAddon addon;

}
