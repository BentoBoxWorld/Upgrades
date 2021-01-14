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
import world.bentobox.upgrades.dataobjects.UpgradeData;
import world.bentobox.upgrades.ui.utils.AbPanel;

public class AdminPanel extends AbPanel {
	
	protected static final String ADD = "add";
	protected static final String EDIT = "edit";
	protected static final String DELETE = "delete";

	public AdminPanel(UpgradesAddon addon, GameModeAddon gamemode, User user) {
		this(addon, gamemode, user, null);
	}
	
	public AdminPanel(UpgradesAddon addon, GameModeAddon gamemode, User user, AbPanel parent) {
		super(addon, gamemode, user, user.getTranslation("upgrades.ui.titles.adminupgrade"), parent);
		
		this.fillBorder(Material.BLACK_STAINED_GLASS_PANE);
		
		this.setItems(ADD, new PanelItemBuilder()
				.name(this.getUser().getTranslation("upgrades.ui.buttons.addupgrade"))
				.icon(Material.ANVIL)
				.clickHandler(this.onAdd)
				.build(), 13);
		this.setItems(EDIT, new PanelItemBuilder()
				.name(this.getUser().getTranslation("upgrades.ui.buttons.editupgrade"))
				.icon(Material.WRITABLE_BOOK)
				.clickHandler(this.onEdit)
				.build(), 22);
		this.setItems(DELETE, new PanelItemBuilder()
				.name(this.getUser().getTranslation("upgrades.ui.buttons.deleteupgrade"))
				.icon(Material.LAVA_BUCKET)
				.clickHandler(this.onDelete)
				.build(), 31);
	}
	
	private ClickHandler onAdd = (Panel panel, User client, ClickType click, int slot) -> {
		this.getAddon().getChatInput().askOneInput(this.addUpgrade,
			input -> {
				// Input validation
				String uniqueId = this.getGamemode().getDescription().getName() + "_" + input;
				// Invalid if uniqueId already exist
				return !this.getAddon().getUpgradeDataManager().hasUpgradeData(uniqueId);
			},
			client.getTranslation("upgrades.chatinput.admin.question.getupgradeid"), 
			client.getTranslation("upgrades.chatinput.admin.invalid.getupgradeid"),
			client, true);
		return true;
	};
	
	private ClickHandler onEdit = (Panel panel, User client, ClickType click, int slot) -> {
		new ListUpgradeDataPanel(this.getAddon(),
				this.getGamemode(), client,
				client.getTranslation("upgrades.ui.titles.editlist"),
				this, this.doEdit)
			.getBuild().build();
		return true;
	};
	
	private Consumer<UpgradeData> doEdit = (UpgradeData upgrade) -> {
		new EditUpgradePanel(this.getAddon(), this.getGamemode(), this.getUser(), upgrade, this)
			.getBuild().build();
	};
	
	private ClickHandler onDelete = (Panel panel, User client, ClickType click, int slot) -> {
		new ListUpgradeDataPanel(this.getAddon(),
				this.getGamemode(), client,
				client.getTranslation("upgrades.ui.titles.deletelist"),
				this, this.doDelete)
			.getBuild().build();
		return true;
	};
	
	private Consumer<UpgradeData> doDelete = (UpgradeData upgrade) -> {
		new YesNoPanel(this.getAddon(),
				this.getGamemode(), this.getUser(),
				this.getUser().getTranslation("upgrades.ui.titles.delete",
						"[name]", upgrade.getUniqueId()),
				this, delete -> {
					if (delete) {
						this.getAddon().getUpgradeDataManager().deleteUpgradeData(upgrade);
					}
					
					this.getBuild().build();
				}).getBuild().build();
	};
	
	private Consumer<String> addUpgrade = input -> {
		String uniqueId = this.getGamemode().getDescription().getName() + "_" + input;
		
		UpgradeData newUpgrade = this.getAddon().getUpgradeDataManager()
				.createUpgradeData(uniqueId, this.getGamemode().getDescription().getName(), this.getUser());
		
		if (newUpgrade == null) {
			this.getUser().sendMessage("upgrades.error.unknownerror");
			this.getAddon().logError("Couldn't create the upgradeData with id " + uniqueId);
			return;
		}
		
		new EditUpgradePanel(this.getAddon(), this.getGamemode(), this.getUser(), newUpgrade, this)
			.getBuild().build();
	};
	
}
