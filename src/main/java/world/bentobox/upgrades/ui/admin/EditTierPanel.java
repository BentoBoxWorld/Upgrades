package world.bentobox.upgrades.ui.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.panels.PanelItem.ClickHandler;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.dataobjects.UpgradeTier;
import world.bentobox.upgrades.ui.utils.AbPanel;

public class EditTierPanel extends AbPanel {
	
	protected static final String NAME = "name";
	protected static final String DESCRIPTION = "description";
	protected static final String ICON = "icon";
	protected static final String NBLEVEL = "nblevel";
	protected static final String ORDER = "order";
	protected static final String PRICES = "prices";
	protected static final String REWARDS = "rewards";
	
	private UpgradeTier tier;
	private List<UpgradeTier> tiers;
	private List<Integer> tiersLengths;

	public EditTierPanel(UpgradesAddon addon, GameModeAddon gamemode, User user, UpgradeTier tier, List<UpgradeTier> tiers, AbPanel parent) {
		super(addon, gamemode, user, tier.getUniqueId(), parent);
		
		this.tier = tier;
		this.tiers = tiers;
		this.tiersLengths = this.computeTierLength(this.tiers);
		
		this.fillBorder(Material.BLACK_STAINED_GLASS_PANE);
		this.setButton();
	}
	
	private void setButton() {
		
		this.setItems(NAME, new PanelItemBuilder()
				.name(this.tier.getName())
				.description(this.getUser().getTranslation("upgrades.ui.edittierpanel.namedesc"))
				.icon(Material.NAME_TAG)
				.clickHandler(this.onSetName)
				.build(), 12);
		
		this.setItems(DESCRIPTION, new PanelItemBuilder()
				.name(this.getUser().getTranslation("upgrades.ui.edittierpanel.description"))
				.description(this.tier.getDescription())
				.icon(Material.WRITTEN_BOOK)
				.clickHandler(this.onSetDescription)
				.build(), 13);
		
		this.setItems(ICON, new PanelItemBuilder()
				.name(this.getUser().getTranslation("upgrades.ui.edittierpanel.icon"))
				.description(this.getUser().getTranslation("upgrades.ui.edittierpanel.icondesc"))
				.icon(this.tier.getIcon())
				.clickHandler(this.onSetIcon)
				.build(), 14);
		
		this.setItems(NBLEVEL, new PanelItemBuilder()
				.name(this.getUser().getTranslation("upgrades.ui.edittierpanel.nblevel"))
				.description(this.getUser().getTranslation("upgrades.ui.edittierpanel.nbleveldesc",
						"[actual]", Integer.toString(this.tier.getEndLevel() - this.tier.getStartLevel() + 1)))
				.icon(Material.EXPERIENCE_BOTTLE)
				.clickHandler(this.onSetNbLevel)
				.build(), 21);
		
		this.setItems(ORDER, new PanelItemBuilder()
				.name(this.getUser().getTranslation("upgrades.ui.edittierpanel.order"))
				.description(this.getUser().getTranslation("upgrades.ui.edittierpanel.orderdesc",
						"[actual]", Integer.toString(this.tiers.indexOf(this.tier) + 1),
						"[max]", Integer.toString(this.tiers.size())))
				.icon(Material.OAK_SIGN)
				.clickHandler(this.onSetOrder)
				.build(), 23);
		
		this.setItems(PRICES, new PanelItemBuilder()
				.name(this.getUser().getTranslation("upgrades.ui.edittierpanel.prices"))
				.icon(Material.GOLD_NUGGET)
				.clickHandler(null)
				.build(), 30);
		
		this.setItems(REWARDS, new PanelItemBuilder()
				.name(this.getUser().getTranslation("upgrades.ui.edittierpanel.rewards"))
				.icon(Material.DIAMOND_BLOCK)
				.clickHandler(null)
				.build(), 32);
		
	}
	
	private ClickHandler onSetName = (panel, client, click, slot) -> {
		this.getAddon().getChatInput().askOneInput(this.doSetName,
				input -> true,
				this.getUser().getTranslation("upgrades.chatinput.admin.question.getupgradetiername",
						"[name]", this.tier.getName()),
				"You're not supposed to fail that...",
				this.getUser(), false);
		return true;
	};
	
	private Consumer<String> doSetName = (name) -> {
		this.tier.setName(name);
		this.setButton();
		this.getBuild().build();
	};
	
	private ClickHandler onSetDescription = (panel, client, click, slot) -> {
		this.getAddon().getChatInput().askMultiLine(this.doSetDescription,
				input -> true,
				this.getUser().getTranslation("upgrades.chatinput.admin.question.getupgradetierdesc",
						"[end]", this.getAddon().getSettings().getChatInputEscape()),
				"You're not supposed to fail that...",
				this.getUser());
		return true;
	};
	
	private Consumer<List<String>> doSetDescription = (descrip) -> {
		if (descrip == null)
			return;
		this.tier.setDescription(descrip);
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
		this.tier.setIcon(new ItemStack(inHand.getType()));
		this.setButton();
		this.getBuild().build();
		return true;
	};
	
	private ClickHandler onSetNbLevel = (panel, client, click, slot) -> {
		int index = this.tiers.indexOf(this.tier);
		int actu = this.tiersLengths.get(index);
		
		if (click.isLeftClick() && actu > 1) {
			this.tiersLengths.set(index, actu - 1);
		} else if (click.isRightClick()) {
			this.tiersLengths.set(index, actu + 1);
		} else
			return true;
		
		this.updateTierLevel(this.tiers, this.tiersLengths);
		this.setButton();
		this.getBuild().build();
		return true;
	};
	
	private ClickHandler onSetOrder = (panel, client, click, slot) -> {
		int index = this.tiers.indexOf(this.tier);
		int length = this.tiersLengths.get(index);
		int newIndex = index;
		
		if (click.isLeftClick() && index > 0)
			newIndex--;
		else if (click.isRightClick() && index < this.tiers.size() - 1)
			newIndex++;
		else
			return true;
		
		this.tiersLengths.set(index, this.tiersLengths.get(newIndex));
		this.tiers.set(index, this.tiers.get(newIndex));
		this.tiersLengths.set(newIndex, length);
		this.tiers.set(newIndex, this.tier);
		this.updateTierLevel(this.tiers, this.tiersLengths);
		this.setButton();
		this.getBuild().build();
		return true;
	};
	
	private ClickHandler onSetPrices = (panel, client, click, slot) -> {
		return true;
	};
	
	private ClickHandler onSetRewards = (panel, client, click, slot) -> {
		return true;
	};
	
	private List<Integer> computeTierLength(List<UpgradeTier> tiers) {
		List<Integer> lengths = new ArrayList<Integer>();
		
		tiers.forEach(tier -> {
			lengths.add(tier.getEndLevel() - tier.getStartLevel() + 1);
		});
		
		return lengths;
	}
	
	private void updateTierLevel(List<UpgradeTier> tiers, List<Integer> lengths) {
		int level = 0;
		
		for (int i = 0; i < tiers.size(); i++) {
			tiers.get(i).setStartLevel(level);
			level += lengths.get(i);
			tiers.get(i).setEndLevel(level - 1);
		}
	}
	
}
