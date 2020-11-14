package world.bentobox.upgrades.ui.admin;

import org.bukkit.Material;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.ui.utils.AbPanel;

public class AdminPanel extends AbPanel {
	
	protected static final String ADD = "add";
	protected static final String EDIT = "edit";
	protected static final String DELETE = "delete";

	public AdminPanel(UpgradesAddon addon, GameModeAddon gamemode, User user) {
		super(addon, gamemode, user, user.getTranslation("upgrades.ui.titles.adminupgrade"), null);
		
		this.fillBorder(Material.BLACK_STAINED_GLASS_PANE);
		
		this.setItems(ADD, new PanelItemBuilder()
				.name(this.getUser().getTranslation("upgrades.ui.buttons.addupgrade"))
				.icon(Material.ANVIL)
				.clickHandler((panel, client, click, slot) -> {
					new AbPanel(addon, gamemode, user, "add panel", this).getBuild().build();
					return true;
				})
				.build(), 13);
		this.setItems(EDIT, new PanelItemBuilder()
				.name(this.getUser().getTranslation("upgrades.ui.buttons.editupgrade"))
				.icon(Material.WRITABLE_BOOK)
				.clickHandler((panel, client, click, slot) -> {
					new AbPanel(addon, gamemode, user, "edit panel", this).getBuild().build();
					return true;
				})
				.build(), 22);
		this.setItems(DELETE, new PanelItemBuilder()
				.name(this.getUser().getTranslation("upgrades.ui.buttons.deleteupgrade"))
				.icon(Material.LAVA_BUCKET)
				.clickHandler((panel, client, click, slot) -> {
					new AbPanel(addon, gamemode, user, "delete panel", this).getBuild().build();
					return true;
				})
				.build(), 31);
	}
	
}
