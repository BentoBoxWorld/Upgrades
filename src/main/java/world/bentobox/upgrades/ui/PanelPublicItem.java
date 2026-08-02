package world.bentobox.upgrades.ui;

import org.bukkit.Material;

import world.bentobox.bentobox.api.user.User;

public interface PanelPublicItem {
	public abstract Material getIcon();

	public abstract String getName();

	public abstract String getPublicName(User user);

	public abstract String getPublicDescription(User user);
}
