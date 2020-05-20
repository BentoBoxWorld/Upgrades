package world.bentobox.islandupgrades.ui;

import java.util.ArrayList;
import java.util.List;

import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.islandupgrades.IslandUpgradesAddon;
import world.bentobox.islandupgrades.api.IslandUpgradeObject;

public class Panel {
	
	public Panel(IslandUpgradesAddon addon) {
		super();
		this.addon = addon;
	}
	
	public void showPanel(User user) {
		Island island = this.addon.getIslands().getIsland(user.getWorld(), user);
		long islandLevel = this.addon.getIslandUpgradesManager().getIslandLevel(island);
		
		PanelBuilder pb = new PanelBuilder().name(user.getTranslation("islandupgrades.ui.upgradepanel.title"));
		
		this.addon.getIslandUpgradeObjectList().forEach(upgrade -> {
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
	
	private List<String> getDescription(User user, IslandUpgradeObject upgrade, long islandLevel) {
		List<String> descrip = new ArrayList<>();
		if (upgrade.getUpgradeValues() == null)
			descrip.add(user.getTranslation("islandupgrades.ui.upgradepanel.maxlevel"));
		else {
			boolean hasMoney = this.addon.getVaultHook().has(user, upgrade.getUpgradeValues().getMoneyCost());
			descrip.add((upgrade.getUpgradeValues().getIslandLevel() <= islandLevel ? "§a" : "§c") + 
				user.getTranslation("islandupgrades.ui.upgradepanel.islandneed",
					"[islandlevel]", upgrade.getUpgradeValues().getIslandLevel().toString()));
			
			descrip.add((hasMoney ? "§a" : "§c") + 
				user.getTranslation("islandupgrades.ui.upgradepanel.moneycost",
					"[cost]", upgrade.getUpgradeValues().getMoneyCost().toString()));
			
			if (upgrade.getUpgradeValues().getIslandLevel() > islandLevel) {
				descrip.add("§8" + user.getTranslation("islandupgrades.ui.upgradepanel.tryreloadlevel"));
			}
		}
		
		return descrip;
	}
	
	private IslandUpgradesAddon addon;
	
}
