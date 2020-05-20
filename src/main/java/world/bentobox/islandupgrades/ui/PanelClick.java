package world.bentobox.islandupgrades.ui;

import org.bukkit.event.inventory.ClickType;

import world.bentobox.bentobox.api.panels.Panel;
import world.bentobox.bentobox.api.panels.PanelItem.ClickHandler;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.islandupgrades.IslandUpgradesAddon;
import world.bentobox.islandupgrades.api.IslandUpgradeObject;

public class PanelClick implements ClickHandler {

	public PanelClick(IslandUpgradesAddon addon, IslandUpgradeObject upgrade) {
		this.addon = addon;
		this.upgrade = upgrade;
	}
	
	@Override
	public boolean onClick(Panel panel, User user, ClickType clickType, int slot) {
		if (this.upgrade == null)
			return true;
		
		Island island = this.addon.getIslands().getIsland(user.getWorld(), user);
		
		if (!this.upgrade.canUpgrade(user, island)) {
			return true;
		}
		
		user.closeInventory();
		this.upgrade.doUpgrade(user, island);
		return true;
	}
	
	private IslandUpgradesAddon addon;
	private IslandUpgradeObject upgrade;
	
}
