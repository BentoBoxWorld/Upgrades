package world.bentobox.upgrades.ui.admin;

import java.util.List;
import java.util.function.Consumer;
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
	
	private UpgradeData upgrade;
	private List<UpgradeTier> tiers;
	
	private Consumer<UpgradeTier> consumer;
	
	public ListUpgradeTierPanel(UpgradesAddon addon, GameModeAddon gamemode, User user, UpgradeData upgrade, String title, AbPanel parent, Consumer<UpgradeTier> consumer) {
		super(addon, gamemode, user, title, parent);
		
		this.upgrade = upgrade;
		this.consumer = consumer;
		
		this.tiers = this.getAddon().getUpgradeDataManager().getUpgradeTierByUpgradeData(this.upgrade);
		this.createInterface();
	}
	
	private void createInterface() {
		this.fillBorder(Material.BLACK_STAINED_GLASS_PANE);
		
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
			this.consumer.accept(tier);
			return true;
		};
	}
	
}
