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
		long islandLevel = this.addon.getUpgradesManager().getIslandLevel(island);
		
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
	
	private List<String> getDescription(User user, Upgrade upgrade, long islandLevel) {
		List<String> descrip = new ArrayList<>();
		if (upgrade.getUpgradeValues() == null)
			descrip.add(user.getTranslation("upgrades.ui.upgradepanel.maxlevel"));
		else {
			boolean hasMoney = this.addon.getVaultHook().has(user, upgrade.getUpgradeValues().getMoneyCost());
			descrip.add((upgrade.getUpgradeValues().getIslandLevel() <= islandLevel ? "§a" : "§c") + 
				user.getTranslation("upgrades.ui.upgradepanel.islandneed",
					"[islandlevel]", upgrade.getUpgradeValues().getIslandLevel().toString()));
			
			descrip.add((hasMoney ? "§a" : "§c") + 
				user.getTranslation("upgrades.ui.upgradepanel.moneycost",
					"[cost]", upgrade.getUpgradeValues().getMoneyCost().toString()));
			
			if (upgrade.getUpgradeValues().getIslandLevel() > islandLevel) {
				descrip.add("§8" + user.getTranslation("upgrades.ui.upgradepanel.tryreloadlevel"));
			}
		}
		
		return descrip;
	}
	
	private UpgradesAddon addon;
	
}
