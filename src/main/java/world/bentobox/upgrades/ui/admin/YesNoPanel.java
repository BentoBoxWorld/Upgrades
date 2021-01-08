package world.bentobox.upgrades.ui.admin;

import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.panels.Panel;
import world.bentobox.bentobox.api.panels.PanelItem.ClickHandler;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.ui.utils.AbPanel;

public class YesNoPanel extends AbPanel {
	
	protected static final String YES = "yes";
	protected static final String NO = "no";
	
	private Consumer<Boolean> consumer;

	public YesNoPanel(UpgradesAddon addon, GameModeAddon gamemode, User user, String title, AbPanel parent, Consumer<Boolean> consumer) {
		super(addon, gamemode, user, title, parent);
		
		this.consumer = consumer;
		
		this.fillBorder(Material.RED_STAINED_GLASS_PANE);
		this.setButton();
	}
	
	private void setButton() {
		
		this.setItems(YES, new PanelItemBuilder()
				.name(this.getUser().getTranslation("upgrades.ui.buttons.yesbutton"))
				.icon(Material.GREEN_STAINED_GLASS_PANE)
				.clickHandler(this.onYes)
				.build(), 21);
		
		this.setItems(NO, new PanelItemBuilder()
				.name(this.getUser().getTranslation("upgrades.ui.buttons.nobutton"))
				.icon(Material.RED_STAINED_GLASS_PANE)
				.clickHandler(this.onNo)
				.build(), 23);
	}
	
	private ClickHandler onYes = (Panel panel, User client, ClickType click, int slot) -> {
		this.consumer.accept(true);
		return true;
	};
	
	private ClickHandler onNo = (Panel panel, User client, ClickType click, int slot) -> {
		this.consumer.accept(false);
		return true;
	};
	
}
