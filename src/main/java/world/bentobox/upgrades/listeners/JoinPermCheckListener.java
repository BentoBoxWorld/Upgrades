package world.bentobox.upgrades.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import world.bentobox.limits.events.LimitsJoinPermCheckEvent;

public class JoinPermCheckListener implements Listener {

	public JoinPermCheckListener() {
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void onLimitsJoinPermCheckEvent(LimitsJoinPermCheckEvent e) {
		// Stop LimitsJoinPermCheck else reset limits upgrades when player join
		e.setCancelled(true);
	}
	
}
