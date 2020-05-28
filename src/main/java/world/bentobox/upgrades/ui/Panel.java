package world.bentobox.upgrades.ui;

import java.util.ArrayList;
import java.util.List;

import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.api.Upgrade;

public class Panel {
	
	public Panel(UpgradesAddon addon) {
		super();
		this.addon = addon;
	}
	
	public void showPanel(User user) {
		Island island = this.addon.getIslands().getIsland(user.getWorld(), user);
		int islandLevel = this.addon.getUpgradesManager().getIslandLevel(island);
		
		PanelBuilder pb = new PanelBuilder().name(user.getTranslation("upgrades.ui.upgradepanel.title"));
		
		this.addon.getAvailableUpgrades().forEach(upgrade -> {
			upgrade.updateUpgradeValue(user, island);
			
			pb.item(new PanelItemBuilder()
					.name(upgrade.getDisplayName())
					.icon(upgrade.getIcon())
					.description(this.getDescription(user, upgrade, islandLevel))
					.clickHandler(new PanelClick(this.addon, upgrade))
					.build());
		});
		
		pb.user(user).build();
	}
	
	private List<String> getDescription(User user, Upgrade upgrade, int islandLevel) {
		List<String> descrip = new ArrayList<>();
		if (upgrade.getUpgradeValues(user) == null)
			descrip.add(user.getTranslation("upgrades.ui.upgradepanel.maxlevel"));
		else {
			boolean hasMoney = this.addon.getVaultHook().has(user, upgrade.getUpgradeValues(user).getMoneyCost());
			if (this.addon.isLevelProvided()) {
				descrip.add((upgrade.getUpgradeValues(user).getIslandLevel() <= islandLevel ? "§a" : "§c") + 
					user.getTranslation("upgrades.ui.upgradepanel.islandneed",
						"[islandlevel]", Integer.toString(upgrade.getUpgradeValues(user).getIslandLevel())));
			}
			
			if (this.addon.isVaultProvided()) {
				descrip.add((hasMoney ? "§a" : "§c") + 
					user.getTranslation("upgrades.ui.upgradepanel.moneycost",
						"[cost]", Integer.toString(upgrade.getUpgradeValues(user).getMoneyCost())));
			}
			
			if (this.addon.isLevelProvided() && upgrade.getUpgradeValues(user).getIslandLevel() > islandLevel) {
				descrip.add("§8" + user.getTranslation("upgrades.ui.upgradepanel.tryreloadlevel"));
			}
		}
		
		return descrip;
	}
	
	private UpgradesAddon addon;
	
}
