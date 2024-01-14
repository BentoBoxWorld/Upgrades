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
	
	public Panel(UpgradesAddon addon, Island island) {
		super();
		this.addon = addon;
		this.island = island;
	}
	
	public void showPanel(User user) {
		int islandLevel = this.addon.getUpgradesManager().getIslandLevel(this.island);
		
		PanelBuilder pb = new PanelBuilder().name(user.getTranslation("upgrades.ui.upgradepanel.title"));
		
		this.addon.getAvailableUpgrades().forEach(upgrade -> {
			upgrade.updateUpgradeValue(user, this.island);
			
			if (!upgrade.isShowed(user, this.island))
				return;
			
			String ownDescription = upgrade.getOwnDescription(user);
			List<String> fullDescription = new ArrayList<>();
			
			if (ownDescription != null && upgrade.getUpgradeValues(user) != null)
				fullDescription.add(ownDescription);
			fullDescription.addAll(this.getDescription(user, upgrade, islandLevel));
			
			pb.item(new PanelItemBuilder()
					.name(upgrade.getDisplayName())
					.icon(upgrade.getIcon())
					.description(fullDescription)
					.clickHandler(new PanelClick(upgrade, this.island))
					.build());
		});
		
		pb.user(user).build();
	}
	
	private List<String> getDescription(User user, Upgrade upgrade, int islandLevel) {
		List<String> descrip = new ArrayList<>();
		
		if (upgrade.getUpgradeValues(user) == null)
			descrip.add(user.getTranslation("upgrades.ui.upgradepanel.maxlevel"));
		else {
			if (this.addon.isLevelProvided()) {
				descrip.add((upgrade.getUpgradeValues(user).getIslandLevel() <= islandLevel ? "§a" : "§c") + 
					user.getTranslation("upgrades.ui.upgradepanel.islandneed",
						"[islandlevel]", Integer.toString(upgrade.getUpgradeValues(user).getIslandLevel())));
			}
			
			if (this.addon.isVaultProvided()) {
				boolean hasMoney = this.addon.getVaultHook().has(user, upgrade.getUpgradeValues(user).getMoneyCost());
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
	private Island island;
	
}
