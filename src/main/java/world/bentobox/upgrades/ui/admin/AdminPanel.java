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

/**
 * The admin panel for managing DB-backed upgrades.
 * Opens directly as a list view — no intermediate screen.
 * Left-click an upgrade to edit it; right-click to delete it.
 * An "Add upgrade" button is always visible.
 */
public class AdminPanel extends AbPanel {

	private static final String ADD = "add";
	private static final String EMPTY = "empty";

	public AdminPanel(UpgradesAddon addon, GameModeAddon gamemode, User user) {
		this(addon, gamemode, user, null);
	}

	public AdminPanel(UpgradesAddon addon, GameModeAddon gamemode, User user, AbPanel parent) {
		super(addon, gamemode, user, user.getTranslation("upgrades.ui.titles.adminupgrade"), parent);
		this.fillBorder(Material.BLACK_STAINED_GLASS_PANE);
		// Content is built in onBuildHook, called by getBuild()
	}

	/**
	 * Called every time getBuild() is invoked. Clears stale items and rebuilds
	 * from the current DB state so returning from EditUpgradePanel always shows
	 * fresh data.
	 */
	@Override
	protected void onBuildHook() {
		this.clearItems();
		this.setupNavigationButton();
		this.createInterface();
	}

	private void createInterface() {
		this.setItems(ADD, new PanelItemBuilder()
				.name(this.getUser().getTranslation("upgrades.ui.buttons.addupgrade"))
				.icon(Material.ANVIL)
				.clickHandler(this.onAdd)
				.build(), 4);

		List<UpgradeData> upgrades = this.getAddon().getUpgradeDataManager()
				.getUpgradeDataByGameMode(this.getGamemode().getDescription().getName());

		if (upgrades.isEmpty()) {
			this.setItems(EMPTY, new PanelItemBuilder()
					.name(this.getUser().getTranslation("upgrades.ui.buttons.noupgrades"))
					.icon(Material.BARRIER)
					.build(), 22);
		} else {
			IntStream.range(0, upgrades.size()).forEach(idx -> {
				UpgradeData upgrade = upgrades.get(idx);
				int pos = ((idx / 7) * 9) + (idx % 7) + 10;
				this.setItems(upgrade.getUniqueId(), new PanelItemBuilder()
						.name(upgrade.getName())
						.icon(upgrade.getIcon())
						.description(
								this.getUser().getTranslation("upgrades.ui.buttons.leftclickedit"),
								this.getUser().getTranslation("upgrades.ui.buttons.rightclickdelete"))
						.clickHandler(this.onClickUpgrade(upgrade))
						.build(), pos);
			});
		}
	}

	private final ClickHandler onAdd = (Panel panel, User client, ClickType click, int slot) -> {
		this.getAddon().getChatInput().askOneInput(this.addUpgrade,
				input -> {
					String uniqueId = this.getGamemode().getDescription().getName() + "_" + input;
					return !this.getAddon().getUpgradeDataManager().hasUpgradeData(uniqueId);
				},
				client.getTranslation("upgrades.chatinput.admin.question.getupgradeid"),
				client.getTranslation("upgrades.chatinput.admin.invalid.getupgradeid"),
				client, true);
		return true;
	};

	private final Consumer<String> addUpgrade = input -> {
		if (input == null) {
			// Conversation was cancelled or escaped — just reopen the panel
			this.getBuild().build();
			return;
		}

		String uniqueId = this.getGamemode().getDescription().getName() + "_" + input;

		UpgradeData newUpgrade = this.getAddon().getUpgradeDataManager()
				.createUpgradeData(uniqueId, this.getGamemode().getDescription().getName(), this.getUser());

		if (newUpgrade == null) {
			this.getUser().sendMessage("upgrades.error.unknownerror");
			this.getAddon().logError("Couldn't create the upgradeData with id " + uniqueId);
			return;
		}

		// Use the user-typed name as the display name (#70 item 1)
		newUpgrade.setName(input);
		this.getAddon().getUpgradeDataManager().saveUpgradeData(newUpgrade);

		// Register the new upgrade in the player-facing shop immediately.
		// Without this call the upgrade lives in the DB but is never added to
		// addon.upgrade, so /is upgrade shows nothing for it.
		this.getAddon().refreshDatabaseUpgrades();

		new EditUpgradePanel(this.getAddon(), this.getGamemode(), this.getUser(), newUpgrade, this)
				.getBuild().build();
	};

	private ClickHandler onClickUpgrade(UpgradeData upgrade) {
		return (Panel panel, User client, ClickType click, int slot) -> {
			if (click.isLeftClick()) {
				new EditUpgradePanel(this.getAddon(), this.getGamemode(), client, upgrade, this)
						.getBuild().build();
			} else if (click.isRightClick()) {
				new YesNoPanel(this.getAddon(),
						this.getGamemode(), client,
						client.getTranslation("upgrades.ui.titles.delete",
								"[name]", upgrade.getName()),
						this, delete -> {
							if (delete) {
								this.getAddon().getUpgradeDataManager().deleteUpgradeData(upgrade);
								this.getAddon().refreshDatabaseUpgrades();
							}
							// getBuild() triggers onBuildHook() which refreshes the list
							this.getBuild().build();
						}).getBuild().build();
			}
			return true;
		};
	}

}
