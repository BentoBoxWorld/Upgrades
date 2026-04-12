package world.bentobox.upgrades.ui;

import org.bukkit.event.inventory.ClickType;

import world.bentobox.bentobox.api.panels.Panel;
import world.bentobox.bentobox.api.panels.PanelItem.ClickHandler;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.upgrades.api.UpgradeAPI;

public class PanelClick implements ClickHandler {

	public PanelClick(UpgradeAPI upgrade, Island island) {
		this.upgrade = upgrade;
		this.island = island;
	}
	
	@Override
	@SuppressWarnings("java:S3516")
	public boolean onClick(Panel panel, User user, ClickType clickType, int slot) {
		// The ClickHandler contract returns true to consume the click event regardless
		// of whether the upgrade actually ran.
		if (this.upgrade != null
				&& (this.upgrade.getUpgradeValues(user) != null
						|| this.upgrade.getOwnDescription(user) != null)
				&& this.upgrade.canUpgrade(user, this.island)) {
			user.closeInventory();
			this.upgrade.doUpgrade(user, this.island);
		}
		return true;
	}
	
	private UpgradeAPI upgrade;
	private Island island;
	
}
