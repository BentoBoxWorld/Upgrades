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
import world.bentobox.upgrades.UpgradesManager;

public class JoinPermCheckListener implements Listener {

    private final UpgradesAddon addon;
    private final UpgradesManager m;

    public JoinPermCheckListener(UpgradesAddon addon) {
        this.addon = addon;
        this.m = this.addon.getUpgradesManager();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onLimitsPermCheckEvent(LimitsPermCheckEvent e) {
        Material block = e.getMaterial();
        EntityType et = e.getEntityType();
        EntityGroup entgroup = e.getEntityGroup();
        World world = e.getPlayer().getWorld();

        e.setCancelled((
                (block != null && m.getAllBlockLimitsUpgradeTiers(world).containsKey(block)) ||
                (et != null && m.getAllEntityLimitsUpgradeTiers(world).containsKey(et)) ||
                (entgroup != null && m.getAllEntityGroupLimitsUpgradeTiers(world).containsKey(entgroup.getName())))
                );

    }

}
