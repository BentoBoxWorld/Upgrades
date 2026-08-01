package world.bentobox.upgrades.ui;

import org.bukkit.Material;

import world.bentobox.bentobox.api.user.User;

public interface PanelAdminItem {

	public abstract Material getIcon();

	public abstract String getName();

	public abstract String getAdminName(User user);

	public abstract String getAdminDescription(User user);
}
