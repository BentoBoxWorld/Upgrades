package world.bentobox.upgrades.listeners;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import world.bentobox.limits.Settings.EntityGroup;
import world.bentobox.limits.events.LimitsPermCheckEvent;
import world.bentobox.upgrades.UpgradesAddon;

/**
 * Checks perms of players if Limits changes something
 */
public class LimitsPermCheckListener implements Listener {

    private UpgradesAddon addon;

	public LimitsPermCheckListener(UpgradesAddon addon) {
		this.addon = addon;
	}
	
	/**
     * Limits changed a permission - cancel it if it's being managed by Upgrades
     * @param e LimitsPermCheckEvent
     */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void onLimitsPermCheckEvent(LimitsPermCheckEvent e) {
		Material block = e.getMaterial();
		EntityType et = e.getEntityType();
		EntityGroup entgroup = e.getEntityGroup();
		World world = e.getPlayer().getWorld();
		
        // Cancel the event if this block is handled by Upgrades
		if (block != null) {
			if (this.addon.getUpgradesManager().getAllBlockLimitsUpgradeTiers(world).containsKey(block)) {
				e.setCancelled(true);
			}
		}
		
        // Cancel the event if this entity is being covered by Upgrades
		if (et != null) {
			if (this.addon.getUpgradesManager().getAllEntityLimitsUpgradeTiers(world).containsKey(et)) {
				e.setCancelled(true);
			}
		}
		
        // Cancel if this Entity Group is handled by Upgrades
		if (entgroup != null) {
			if (this.addon.getUpgradesManager().getAllEntityGroupLimitsUpgradeTiers(world).containsKey(entgroup.getName())) {
				e.setCancelled(true);
			}
		}
	}
	
}
