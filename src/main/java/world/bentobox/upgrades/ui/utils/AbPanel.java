package world.bentobox.upgrades.ui.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Material;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.upgrades.UpgradesAddon;

public class AbPanel {
	
	protected static final String RETURN = "return";
	protected static final String EXIT = "exit";

	/** Maximum characters per lore line before word-wrapping. */
	protected static final int LORE_MAX_WIDTH = 35;

	protected static final Set<Material> BADICON = new HashSet<Material>(Arrays.asList(
			Material.AIR,
			Material.CAVE_AIR,
			Material.VOID_AIR
		));
	
	public AbPanel(UpgradesAddon addon, GameModeAddon gamemode, User user, String title, AbPanel parent) {
		this.addon = addon;
		this.gamemode = gamemode;
		this.user = user;
		this.title = title;
		this.parent = parent;

		this.border = new ArrayList<AbPanel.PanelSlot>();
		this.items = new HashMap<String, AbPanel.PanelSlot>();

		this.setupNavigationButton();
	}

	/**
	 * Sets up (or re-sets up) the return or exit button at slot 8.
	 * Call this after clearItems() to restore the navigation button.
	 */
	protected void setupNavigationButton() {
		if (parent != null) {
			PanelItem returnItem = new PanelItemBuilder()
					.name(this.user.getTranslation("upgrades.ui.buttons.return"))
					.icon(Material.BARRIER)
					.clickHandler((panel, client, click, slot) -> {
						this.parent.getBuild().build();
						return true;
					})
					.build();
			this.setItems(RETURN, returnItem, 8);
		} else {
			PanelItem exitItem = new PanelItemBuilder()
					.name(this.user.getTranslation("upgrades.ui.buttons.exit"))
					.icon(Material.BARRIER)
					.clickHandler((panel, client, click, slot) -> {
						client.closeInventory();
						return true;
					})
					.build();
			this.setItems(EXIT, exitItem, 8);
		}
	}

	/**
	 * Hook called at the start of getBuild() before building the panel.
	 * Override in subclasses to refresh panel content dynamically.
	 */
	protected void onBuildHook() {}
	
	/**
	 * Clears all named panel items (but not the border).
	 * Use in onBuildHook() to allow dynamic refresh of panel content.
	 */
	protected void clearItems() {
		this.items.clear();
	}

	public PanelBuilder getBuild() {
		this.onBuildHook();
		PanelBuilder builder = new PanelBuilder();
		
		builder.user(this.user);
		builder.name(this.title);
		
		this.border.forEach((PanelSlot item) -> {
			builder.item(item.getSlot(), item.getItem());
		});
		
		this.items.forEach((String name, PanelSlot item) -> {
			builder.item(item.getSlot(), item.getItem());
		});
		
		return builder;
	}
	
	protected void fillBorder(Material borderMat) {
		this.fillBorder(5, borderMat);
	}
	
	protected void fillBorder(int rowCount, Material borderMat) {
		for (int x = 0; x < 9; x++) {
			this.border.add(new PanelSlot(this.getBorderItem(borderMat), x));
		}
		for (int x = rowCount * 9 - 9; x < rowCount * 9; x++) {
			this.border.add(new PanelSlot(this.getBorderItem(borderMat), x));
		}
		for (int x = 1; x < rowCount - 1; x++) {
			this.border.add(new PanelSlot(this.getBorderItem(borderMat), x * 9));
			this.border.add(new PanelSlot(this.getBorderItem(borderMat), x * 9 + 8));
		}
		
	}
	
	private PanelItem getBorderItem(Material borderMat) {
		return new PanelItemBuilder()
				.name(" ")
				.description(Collections.emptyList())
				.glow(false)
				.icon(borderMat)
				.clickHandler(null)
				.build();
	}
	
	/**
	 * @return the title
	 */
	protected String getTitle() {
		return title;
	}
	/**
	 * @param title the title to set
	 */
	protected void setTitle(String title) {
		this.title = title;
	}
	/**
	 * @return the addon
	 */
	protected UpgradesAddon getAddon() {
		return addon;
	}
	/**
	 * @return the gamemode
	 */
	protected GameModeAddon getGamemode() {
		return gamemode;
	}
	/**
	 * @return the user
	 */
	protected User getUser() {
		return user;
	}
	/**
	 * @return the parent
	 */
	protected AbPanel getParent() {
		return parent;
	}
	
	/**
	 * Word-wraps a single string into multiple lines, each no wider than
	 * {@code maxWidth} characters. Breaks only on spaces.
	 *
	 * @param text     the text to wrap (may be null or empty)
	 * @param maxWidth maximum characters per line
	 * @return list of wrapped lines
	 */
	protected static List<String> wrapText(String text, int maxWidth) {
		if (text == null || text.isEmpty()) return Collections.emptyList();
		List<String> lines = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		for (String word : text.split(" ")) {
			if (current.length() == 0) {
				current.append(word);
			} else if (current.length() + 1 + word.length() <= maxWidth) {
				current.append(' ').append(word);
			} else {
				lines.add(current.toString());
				current = new StringBuilder(word);
			}
		}
		if (current.length() > 0) lines.add(current.toString());
		return lines;
	}

	/**
	 * Word-wraps every entry in a lore list at {@link #LORE_MAX_WIDTH} characters,
	 * returning a flat list of wrapped lines.
	 *
	 * @param lore source lore lines (may contain long strings)
	 * @return wrapped lore ready for {@link PanelItemBuilder#description(List)}
	 */
	protected static List<String> wrapLore(List<String> lore) {
		if (lore == null) return Collections.emptyList();
		return lore.stream()
				.flatMap(line -> wrapText(line, LORE_MAX_WIDTH).stream())
				.collect(Collectors.toList());
	}

	protected void setItems(String name, PanelItem item, int slot) {
		this.items.put(name, new PanelSlot(item, slot));
	}

	private UpgradesAddon addon;
	private GameModeAddon gamemode;
	private User user;
	private String title;
	private AbPanel parent;
	
	private Map<String, PanelSlot> items;
	private List<PanelSlot> border;
	
	private class PanelSlot {
		public PanelSlot(PanelItem item, int slot) {
			this.item = item;
			this.slot = slot;
		}
		
		public PanelItem getItem() {
			return this.item;
		}
		
		public int getSlot() {
			return this.slot;
		}
		
		private PanelItem item;
		private int slot;
	}

}
