package world.bentobox.upgrades.ui;

import org.bukkit.event.inventory.ClickType;

import world.bentobox.bentobox.api.panels.Panel;
import world.bentobox.bentobox.api.panels.PanelItem.ClickHandler;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.api.Upgrade;

public class PanelClick implements ClickHandler {

	public PanelClick(UpgradesAddon addon, Upgrade upgrade) {
		this.addon = addon;
		this.upgrade = upgrade;
	}
	
	@Override
	public boolean onClick(Panel panel, User user, ClickType clickType, int slot) {
		if (this.upgrade == null || this.upgrade.getUpgradeValues() == null)
			return true;
		
		Island island = this.addon.getIslands().getIsland(user.getWorld(), user);
		
		if (!this.upgrade.canUpgrade(user, island)) {
			return true;
		}
		
		user.closeInventory();
		this.upgrade.doUpgrade(user, island);
		return true;
	}
	
	private UpgradesAddon addon;
	private Upgrade upgrade;
	
}
