package world.bentobox.upgrades.ui.admin;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.panels.Panel;
import world.bentobox.bentobox.api.panels.PanelItem.ClickHandler;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.dataobjects.UpgradeData;
import world.bentobox.upgrades.ui.utils.AbPanel;

public class ListUpgradeDataPanel extends AbPanel {
	
	private Consumer<UpgradeData> consumer;
	private List<UpgradeData> upgrades;
	
	public ListUpgradeDataPanel(UpgradesAddon addon, GameModeAddon gamemode, User user, String title, AbPanel parent, Consumer<UpgradeData> consumer) {
		super(addon, gamemode, user, title, parent);
		
		this.consumer = consumer;
		
		this.upgrades = this.getAddon().getUpgradeDataManager().getUpgradeDataByGameMode(this.getGamemode().getDescription().getName());
		
		this.fillBorder(Material.BLACK_STAINED_GLASS_PANE);
		this.setUpgrades();
	}
	
	private void setUpgrades() {
		IntStream.range(0, this.upgrades.size())
			.forEach(idx -> {
				UpgradeData upgrade = this.upgrades.get(idx);
				// (idx / 7) = row    (idx % 7) = column
				int pos = ((idx / 7) * 9) + (idx % 7) + 10;
				this.setItems(upgrade.getUniqueId(), new PanelItemBuilder()
						.name(upgrade.getUniqueId())
						.icon(upgrade.getIcon())
						.clickHandler(this.onClick(upgrade))
						.build(), pos);
			});
	}
	
	private ClickHandler onClick(UpgradeData upgrade) {
		return (Panel panel, User client, ClickType click, int slot) -> {
			consumer.accept(upgrade);
			return true;
		};
	}
}
