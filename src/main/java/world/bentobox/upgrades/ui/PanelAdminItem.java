package world.bentobox.upgrades.ui;

import org.bukkit.Material;

import world.bentobox.bentobox.api.user.User;

public interface PanelAdminItem {

	abstract public Material getIcon();

	abstract public String getName();

	abstract public String getAdminName(User user);

	abstract public String getAdminDescription(User user);
}
