package world.bentobox.upgrades.ui.admin;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.panels.PanelItem.ClickHandler;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.dataobjects.UpgradeData;
import world.bentobox.upgrades.dataobjects.UpgradeTier;
import world.bentobox.upgrades.ui.utils.AbPanel;

public class EditUpgradePanel extends AbPanel {
	
	protected static final String ACTIVE = "active";
	protected static final String DESCRIPTION = "description";
	protected static final String ICON = "icon";
	protected static final String NAME = "name";
	protected static final String ORDER = "order";
	protected static final String TIERADD = "tieradd";
	protected static final String TIEREDIT = "tieredit";
	protected static final String TIERDELETE = "tierdelete";
	
	protected static final Set<Material> BADICON = new HashSet<Material>(Arrays.asList(
			Material.AIR,
			Material.CAVE_AIR,
			Material.VOID_AIR
		));
	
	private UpgradeData upgrade;

	public EditUpgradePanel(UpgradesAddon addon, GameModeAddon gamemode, User user, UpgradeData upgrade, AbPanel parent) {
		super(addon, gamemode, user, upgrade.getUniqueId(), parent);
		this.upgrade = upgrade;
		
		this.fillBorder(Material.BLACK_STAINED_GLASS_PANE);
		this.setButton();
	}
	
	private void setButton() {
		
		if (this.upgrade.isActive()) {
			this.setItems(ACTIVE, new PanelItemBuilder()
				.name(this.getUser().getTranslation("upgrades.ui.editupgradepanel.active"))
				.description(this.getUser().getTranslation("upgrades.ui.editupgradepanel.activedesc"))
				.icon(Material.GREEN_CONCRETE)
				.clickHandler(this.onActive)
				.build(), 10);
		} else {
			this.setItems(ACTIVE, new PanelItemBuilder()
				.name(this.getUser().getTranslation("upgrades.ui.editupgradepanel.inactive"))
				.description(this.getUser().getTranslation("upgrades.ui.editupgradepanel.inactivedesc"))
				.icon(Material.RED_CONCRETE)
				.clickHandler(this.onActive)
				.build(), 10);
		}
		
		this.setItems(NAME, new PanelItemBuilder()
				.name(this.upgrade.getName())
				.description(this.getUser().getTranslation("upgrades.ui.editupgradepanel.namedesc"))
				.icon(Material.NAME_TAG)
				.clickHandler(this.onSetName)
				.build(), 12);
		
		this.setItems(DESCRIPTION, new PanelItemBuilder()
				.name(this.getUser().getTranslation("upgrades.ui.editupgradepanel.description"))
				.description(this.upgrade.getDescription())
				.icon(Material.WRITTEN_BOOK)
				.clickHandler(this.onSetDescription)
				.build(), 21);
		
		this.setItems(ICON, new PanelItemBuilder()
				.name(this.getUser().getTranslation("upgrades.ui.editupgradepanel.icon"))
				.description(this.getUser().getTranslation("upgrades.ui.editupgradepanel.icondesc"))
				.icon(this.upgrade.getIcon())
				.clickHandler(this.onSetIcon)
				.build(), 30);
		
		this.setItems(TIERADD, new PanelItemBuilder()
				.name(this.getUser().getTranslation("upgrades.ui.editupgradepanel.tieradd"))
				.icon(Material.ANVIL)
				.clickHandler(this.onTierAdd)
				.build(), 14);
		
		this.setItems(TIEREDIT, new PanelItemBuilder()
				.name(this.getUser().getTranslation("upgrades.ui.editupgradepanel.tieredit"))
				.icon(Material.WRITABLE_BOOK)
				.clickHandler(this.onTierEdit)
				.build(), 23);
		
		this.setItems(TIERDELETE, new PanelItemBuilder()
				.name(this.getUser().getTranslation("upgrades.ui.editupgradepanel.tierdelete"))
				.icon(Material.LAVA_BUCKET)
				.clickHandler(this.onTierDelete)
				.build(), 32);
		
		this.setItems(ORDER, new PanelItemBuilder()
				.name(this.getUser().getTranslation("upgrades.ui.editupgradepanel.order",
					"[order]", Integer.toString(this.upgrade.getOrder())))
				.description(this.getUser().getTranslation("upgrades.ui.editupgradepanel.orderdesc"))
				.icon(Material.OAK_SIGN)
				.clickHandler(this.onSetOrder)
				.build(), 16);
	}
	
	private ClickHandler onActive = (panel, client, click, slot) -> {
		this.upgrade.setActive(!this.upgrade.isActive());
		this.setButton();
		this.getBuild().build();
		return true;
	};
	
	private ClickHandler onSetName = (panel, client, click, slot) -> {
		this.getAddon().getChatInput().askOneInput(this.doSetName,
			input -> true,
			this.getUser().getTranslation("upgrades.chatinput.admin.question.getupgradedataname",
					"[name]", this.upgrade.getName()),
			"You're not supposed to fail that...",
			this.getUser(), false);
		return true;
	};
	
	private Consumer<String> doSetName = (name) -> {
		this.upgrade.setName(name);
		this.setButton();
		this.getBuild().build();
	};
	
	private ClickHandler onSetDescription = (panel, client, click, slot) -> {
		this.getAddon().getChatInput().askMultiLine(this.doSetDescription,
			input -> true,
			this.getUser().getTranslation("upgrades.chatinput.admin.question.getupgradedatadesc",
				"[end]", this.getAddon().getSettings().getChatInputEscape()),
			"You're not supposed to fail that...",
			this.getUser());
		return true;
	};
	
	private Consumer<List<String>> doSetDescription = (descrip) -> {
		if (descrip == null)
			return;
		this.upgrade.setDescription(descrip);
		this.setButton();
		this.getBuild().build();
	};
	
	private ClickHandler onSetIcon = (panel, client, click, slot) -> {
		ItemStack inHand = client.getInventory().getItemInMainHand();
		
		if (inHand == null || BADICON.contains(inHand.getType())) {
			client.sendMessage("upgrades.error.noiteminhand");
			client.closeInventory();
			return true;
		}
		this.getAddon().log("Try to change icon to " + inHand.getType().toString());
		upgrade.setIcon(new ItemStack(inHand.getType()));
		this.setButton();
		this.getBuild().build();
		return true;
	};
	
	private ClickHandler onTierAdd = (panel, client, click, slot) -> {
		this.getAddon().getChatInput().askOneInput(this.doTierAdd,
			input -> {
				String uniqueId = this.getGamemode().getDescription().getName() + "_" + input;
				return !this.getAddon().getUpgradeDataManager().hasUpgradeTier(uniqueId);
			},
			client.getTranslation("upgrades.chatinput.admin.question.gettierid"),
			client.getTranslation("upgrades.chatinput.admin.invalid.gettierid"),
			client, true);
		return true;
	};
	
	private Consumer<String> doTierAdd = input -> {
		String uniqueId = this.getGamemode().getDescription().getName() + "_" + input;
		
		UpgradeTier newTier = this.getAddon().getUpgradeDataManager()
				.createUpgradeTier(uniqueId, this.upgrade, 0, 1, this.getUser());
		
		if (newTier == null) {
			this.getUser().sendMessage("upgrades.error.unknownerror");
			this.getAddon().logError("Couldn't create the upgradeTier with id " + uniqueId);
			return;
		}
		
		new EditTierPanel(this.getAddon(),
				this.getGamemode(), this.getUser(),
				newTier, this)
			.getBuild().build();
	};
	
	private ClickHandler onTierEdit = (panel, client, click, slot) -> {
		new ListUpgradeTierPanel(this.getAddon(),
				this.getGamemode(), client, this.upgrade,
				client.getTranslation("upgrades.ui.titles.editlist"),
				this,
				tier -> {
					new EditTierPanel(this.getAddon(),
							this.getGamemode(), client,
							tier, this)
						.getBuild().build();
				})
			.getBuild().build();
		return true;
	};
	
	private ClickHandler onTierDelete = (panel, client, click, slot) -> {
		new ListUpgradeTierPanel(this.getAddon(),
				this.getGamemode(), client, this.upgrade,
				client.getTranslation("upgrades.ui.titles.deletelist"),
				this, this.doTierDelete)
			.getBuild().build();
		return true;
	};
	
	private Consumer<UpgradeTier> doTierDelete = tier -> {
		new YesNoPanel(this.getAddon(),
				this.getGamemode(), this.getUser(),
				this.getUser().getTranslation("upgrades.ui.titles.delete",
						"[name]", tier.getUniqueId()),
				this, delete -> {
					if (delete) {
						this.getAddon().getUpgradeDataManager().deleteUpgradeTier(tier);
					}
					this.getBuild().build();
				}).getBuild().build();
	};
	
	private ClickHandler onSetOrder = (panel, client, click, slot) -> {
		this.getAddon().getChatInput().askOneNumber(this.doSetOrder,
			input -> input.intValue() > -1,
			this.getUser().getTranslation("upgrades.chatinput.admin.question.getupgradedataorder"),
			this.getUser().getTranslation("upgrades.chatinput.admin.invalid.getupgradedataorder"),
			this.getUser());
		return true;
	};
	
	private Consumer<Number> doSetOrder = (order) -> {
		this.upgrade.setOrder(order.intValue());
		this.setButton();
		this.getBuild().build();
	};
	
}
