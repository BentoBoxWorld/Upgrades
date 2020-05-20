package world.bentobox.islandupgrades.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;

import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.islandupgrades.IslandUpgradesAddon;
import world.bentobox.islandupgrades.IslandUpgradesData;

public class Panel {

private IslandUpgradesAddon addon;
	
	public Panel(IslandUpgradesAddon addon) {
		super();
		this.addon = addon;
	}
	
	public void showPanel(User user) {
		Island island = this.addon.getIslands().getIsland(user.getWorld(), user);
		IslandUpgradesData data = this.addon.getIslandUpgradesLevel(island.getUniqueId());
		
		
		long rangeLevel = data.getRangeUpgradeLevel();
		long islandLevel = this.addon.getIslandUpgradesManager().getIslandLevel(island);
		long numberPeople = island.getMemberSet().size();
		
		Map<String, Integer> rangeUpgradeInfo = this.addon.getIslandUpgradesManager().getRangeUpgradeInfos(rangeLevel, islandLevel, numberPeople, user.getWorld());
		
		PanelBuilder pb = new PanelBuilder().name(user.getTranslation("islandupgrades.ui.upgradepanel.title"));
		pb.item(new PanelItemBuilder()
				.name(rangeUpgradeInfo == null ? 
					user.getTranslation("islandupgrades.ui.upgradepanel.norangeupgrade") :
					user.getTranslation("islandupgrades.ui.upgradepanel.rangeupgrade", "[rangelevel]", rangeUpgradeInfo.get("upgradeRange").toString()))
				.icon(Material.OAK_FENCE)
				.description(this.getDescription(user, rangeUpgradeInfo, islandLevel))
				.clickHandler(new PanelRangeClick(this.addon, rangeUpgradeInfo))
				.build());
		
		pb.user(user).build();
	}
	
	private List<String> getDescription(User user, Map<String, Integer> infos, long islandLevel) {
		List<String> descrip = new ArrayList<>();
		if (infos == null)
			descrip.add(user.getTranslation("islandupgrades.ui.upgradepanel.maxlevel"));
		else {
			boolean hasMoney = this.addon.getVaultHook().has(user, infos.get("vaultCost"));
			descrip.add((infos.get("islandMinLevel") <= islandLevel ? "§a" : "§c") + 
				user.getTranslation("islandupgrades.ui.upgradepanel.islandneed", "[islandlevel]", infos.get("islandMinLevel").toString()));
			
			descrip.add((hasMoney ? "§a" : "§c") + 
					user.getTranslation("islandupgrades.ui.upgradepanel.moneycost", "[cost]", infos.get("vaultCost").toString()));
			
			if (infos.get("islandMinLevel") > islandLevel) {
				descrip.add("§8" + user.getTranslation("islandupgrades.ui.upgradepanel.tryreloadlevel"));
			}
		}
		
		return descrip;
	}
	
}
