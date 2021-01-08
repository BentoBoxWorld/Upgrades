package world.bentobox.upgrades.ui.admin;

import java.util.List;
import java.util.stream.IntStream;

import org.bukkit.Material;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.panels.PanelItem.ClickHandler;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.dataobjects.UpgradeData;
import world.bentobox.upgrades.dataobjects.UpgradeTier;
import world.bentobox.upgrades.ui.utils.AbPanel;

public class ListUpgradeTierPanel extends AbPanel {
	
	protected static final String ADD = "add";
	protected static final String DELETE = "delete";
	
	private UpgradeData upgrade;
	private List<UpgradeTier> tiers;

	public ListUpgradeTierPanel(UpgradesAddon addon, GameModeAddon gamemode, User user, UpgradeData upgrade, AbPanel parent) {
		super(addon,
			gamemode, user,
			user.getTranslation("upgrades.ui.titles.listupgradetier",
				"[upgradedata]", upgrade.getUniqueId()),
			parent);
		
		this.upgrade = upgrade;
		
		this.tiers = this.getAddon().getUpgradeDataManager().getUpgradeTierByUpgradeData(this.upgrade);
		this.createInterface();
	}
	
	private void createInterface() {
		this.fillBorder(Material.BLACK_STAINED_GLASS_PANE);
		
		this.setItems(ADD, new PanelItemBuilder()
			.name(this.getUser().getTranslation("upgrades.ui.buttons.addupgrade"))
			.icon(Material.GREEN_STAINED_GLASS_PANE)
			.clickHandler(this.onAdd)
			.build(), 3);
		
		this.setItems(DELETE, new PanelItemBuilder()
			.name(this.getUser().getTranslation("upgrades.ui.buttons.deleteupgrade"))
			.icon(Material.RED_STAINED_GLASS_PANE)
			.clickHandler(this.onDelete)
			.build(), 5);
		
		IntStream.range(0,  this.tiers.size())
			.forEach(idx -> {
				UpgradeTier tier = this.tiers.get(idx);
				int pos = ((idx / 7) * 9) + (idx % 7) + 10;
				this.setItems(tier.getUniqueId(), new PanelItemBuilder()
					.name(tier.getUniqueId())
					.icon(tier.getIcon())
					.clickHandler(this.onClickEdit(tier))
					.build(), pos);
			});
	}
	
	private ClickHandler onClickEdit(UpgradeTier tier) {
		return (panel, client, click, slot) -> {
			this.getAddon().log("Tier select: " + tier.getUniqueId());
			return true;
		};
	}
	
	private ClickHandler onAdd = (panel, client, click, slot) -> {
		this.getAddon().log("Add new tier");
		return true;
	};
	
	private ClickHandler onDelete = (panel, client, click, slot) -> {
		this.getAddon().log("Delete tier");
		return true;
	};

}
