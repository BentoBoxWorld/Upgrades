package world.bentobox.upgrades.ui.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.bukkit.Material;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.panels.PanelItem.ClickHandler;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.dataobjects.UpgradeTier;
import world.bentobox.upgrades.dataobjects.prices.Price;
import world.bentobox.upgrades.ui.utils.AbPanel;

public class ListUpgradePricePanel extends AbPanel {
	
	protected static final String CREATE = "create";
	
	private UpgradeTier tier;
	private List<Price> prices;
	
	private Consumer<Price> select;
	private Consumer<Price> delete;
	private Runnable create;
	
	public ListUpgradePricePanel(UpgradesAddon addon, GameModeAddon gamemode, User user, UpgradeTier tier, String title, AbPanel parent, Consumer<Price> select, Consumer<Price> delete, Runnable create) {
		super(addon, gamemode, user, title, parent);
		
		this.tier = tier;
		this.select = select;
		this.delete = delete;
		this.create = create;
		this.prices = this.tier.getPrices();
		
		this.createInterface();
	}
	
	private void createInterface() {
		this.fillBorder(Material.BLACK_STAINED_GLASS_PANE);
		
		this.setItems(CREATE, new PanelItemBuilder()
				.name(this.getUser().getTranslation("upgrades.ui.listpricepanel.create"))
				.icon(Material.GREEN_STAINED_GLASS_PANE)
				.clickHandler(this.onClickCreate)
				.build(), 5);
		
		IntStream.range(0, this.prices.size())
			.forEach(idx -> {
				Price price = this.prices.get(idx);
				List<String> desc = new ArrayList<String>();
				desc.add(this.getUser().getTranslation("upgrades.ui.listpricepanel.selectpricedesc"));
				if (price.getDescription() != null)
					desc.addAll(price.getDescription());
				int pos = ((idx / 7) * 9) + (idx % 7) + 10;
				this.setItems(price.getName() + "-" + idx, new PanelItemBuilder()
						.name(price.getName())
						.description(desc)
						.icon(price.getIcon())
						.clickHandler(this.onClickPrice(price))
						.build(), pos);
			});
	}
	
	private ClickHandler onClickCreate = (panel, client, click, slot) -> {
		this.create.run();
		return true;
	};
	
	private ClickHandler onClickPrice(Price price) {
		return (panel, client, click, slot) -> {
			
			if (click.isLeftClick()) {
				this.select.accept(price);
			} else if (click.isRightClick()) {
				this.delete.accept(price);
			} else {
				return false;
			}
			
			return true;
		};
	}
}
