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

public class JoinPermCheckListener implements Listener {

	public JoinPermCheckListener(UpgradesAddon addon) {
		this.addon = addon;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void onLimitsPermCheckEvent(LimitsPermCheckEvent e) {
		Material block = e.getMaterial();
		EntityType et = e.getEntityType();
		EntityGroup entgroup = e.getEntityGroup();
		World world = e.getPlayer().getWorld();
		
		if (block != null) {
			if (this.addon.getUpgradesManager().getAllBlockLimitsUpgradeTiers(world).containsKey(block)) {
				e.setCancelled(true);
			}
		}
		
		if (et != null) {
			if (this.addon.getUpgradesManager().getAllEntityLimitsUpgradeTiers(world).containsKey(et)) {
				e.setCancelled(true);
			}
		}
		
		if (entgroup != null) {
			if (this.addon.getUpgradesManager().getAllEntityGroupLimitsUpgradeTiers(world).containsKey(entgroup.getName())) {
				e.setCancelled(true);
			}
		}
	}
	
	UpgradesAddon addon;
	
}
