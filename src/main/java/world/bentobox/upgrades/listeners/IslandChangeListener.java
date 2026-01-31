package world.bentobox.upgrades.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import world.bentobox.bentobox.api.events.island.IslandDeleteEvent;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.upgrades.UpgradesAddon;

/**
 * Listener that handles island-related events for the Upgrades addon.
 * This listener is responsible for cleaning up upgrade data when islands are deleted.
 *
 * @author tastybento
 * @since 1.0.0
 */
public class IslandChangeListener implements Listener {

    /**
     * Constructs a new IslandChangeListener.
     *
     * @param addon the Upgrades addon instance
     */
    public IslandChangeListener(UpgradesAddon addon) {
        this.addon = addon;
    }

    /**
     * Handles the island deletion event.
     * When an island is deleted, this method removes the island's upgrade data
     * from the cache and deletes it from the database to prevent orphaned data.
     *
     * @param e the IslandDeleteEvent containing information about the deleted island
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onIslandDeleteEvent(IslandDeleteEvent e) {
        Island island = e.getIsland();
        this.addon.uncacheIsland(island.getUniqueId(), false);
        this.addon.getDatabase().deleteID(island.getUniqueId());
    }

    /**
     * The Upgrades addon instance.
     */
    private UpgradesAddon addon;

}
