package world.bentobox.islandshop.ui;

import java.util.Map;

import org.bukkit.event.inventory.ClickType;

import world.bentobox.bentobox.api.panels.Panel;
import world.bentobox.bentobox.api.panels.PanelItem.ClickHandler;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.islandshop.IslandShopAddon;

public class PanelRangeClick implements ClickHandler{
	
	public PanelRangeClick(IslandShopAddon addon, Map<String, Integer> rangeUpgradeInfo) {
		this.addon = addon;
		this.rangeUpgradeInfo = rangeUpgradeInfo;
	}
	
	@Override
	public boolean onClick(Panel panel, User user, ClickType clickType, int slot) {
		if (this.rangeUpgradeInfo == null)
			return true;
		
		Island island = this.addon.getIslands().getIsland(user.getWorld(), user);
		
		if (!this.addon.getIslandShopRangeUpgrade().canUpgrade(user, island, this.rangeUpgradeInfo)) {
			return true;
		}
		
		user.closeInventory();
		this.addon.getIslandShopRangeUpgrade().doUpgrade(user, island, this.rangeUpgradeInfo);
		return true;
	}
	
	private IslandShopAddon addon;
	private Map<String, Integer> rangeUpgradeInfo;
	
}
