package world.bentobox.upgrades.ui;

import org.bukkit.Material;

import world.bentobox.bentobox.api.user.User;

public interface PanelPublicItem {
	abstract public Material getIcon();

	abstract public String getName();

	abstract public String getPublicName(User user);

	abstract public String getPublicDescription(User user);
}
