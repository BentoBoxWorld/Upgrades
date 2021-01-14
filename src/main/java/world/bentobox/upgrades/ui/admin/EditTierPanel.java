package world.bentobox.upgrades.ui.admin;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.dataobjects.UpgradeTier;
import world.bentobox.upgrades.ui.utils.AbPanel;

public class EditTierPanel extends AbPanel {
	
	private UpgradeTier tier;

	public EditTierPanel(UpgradesAddon addon, GameModeAddon gamemode, User user, UpgradeTier tier, AbPanel parent) {
		super(addon, gamemode, user, tier.getUniqueId(), parent);
		
		this.tier = tier;
	}
	
}
