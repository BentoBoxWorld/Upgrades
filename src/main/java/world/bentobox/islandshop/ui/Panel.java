package world.bentobox.islandshop.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;

import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.islandshop.IslandShopAddon;
import world.bentobox.islandshop.IslandShopData;

public class Panel {
	
	private IslandShopAddon addon;
	
	public Panel(IslandShopAddon addon) {
		super();
		this.addon = addon;
	}
	
	public void showPanel(User user) {
		Island island = this.addon.getIslands().getIsland(user.getWorld(), user);
		IslandShopData data = this.addon.getIslandShopLevel(island.getUniqueId());
		
		
		long rangeLevel = data.getRangeUpgradeLevel();
		long islandLevel = this.addon.getIslandShopManager().getIslandLevel(island);
		long numberPeople = island.getMemberSet().size();
		
		Map<String, Integer> rangeUpgradeInfo = this.addon.getIslandShopManager().getRangeUpgradeInfos(rangeLevel, islandLevel, numberPeople, user.getWorld());
		
		PanelBuilder pb = new PanelBuilder().name(user.getTranslation("islandshop.ui.upgradepanel.title"));
		pb.item(new PanelItemBuilder()
				.name(rangeUpgradeInfo == null ? 
					user.getTranslation("islandshop.ui.upgradepanel.norangeupgrade") :
					user.getTranslation("islandshop.ui.upgradepanel.rangeupgrade", "[rangelevel]", rangeUpgradeInfo.get("upgradeRange").toString()))
				.icon(Material.OAK_FENCE)
				.description(this.getDescription(user, rangeUpgradeInfo, islandLevel))
				.clickHandler(new PanelRangeClick(this.addon, rangeUpgradeInfo))
				.build());
		
		pb.user(user).build();
	}
	
	private List<String> getDescription(User user, Map<String, Integer> infos, long islandLevel) {
		List<String> descrip = new ArrayList<>();
		if (infos == null)
			descrip.add(user.getTranslation("islandshop.ui.upgradepanel.maxlevel"));
		else {
			boolean hasMoney = this.addon.getVaultHook().has(user, infos.get("vaultCost"));
			descrip.add((infos.get("islandMinLevel") <= islandLevel ? "§a" : "§c") + 
				user.getTranslation("islandshop.ui.upgradepanel.islandneed", "[islandlevel]", infos.get("islandMinLevel").toString()));
			
			descrip.add((hasMoney ? "§a" : "§c") + 
					user.getTranslation("islandshop.ui.upgradepanel.moneycost", "[cost]", infos.get("vaultCost").toString()));
			
			if (infos.get("islandMinLevel") > islandLevel) {
				descrip.add("§8" + user.getTranslation("islandshop.ui.upgradepanel.tryreloadlevel"));
			}
		}
		
		return descrip;
	}
}
