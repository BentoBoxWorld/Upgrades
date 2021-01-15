package world.bentobox.upgrades.ui.admin;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.dataobjects.UpgradeTier;
import world.bentobox.upgrades.ui.utils.AbPanel;

public class EditTierPanel extends AbPanel {
	
	protected static final String NAME = "name";
	protected static final String DESCRIPTION = "description";
	protected static final String ICON = "icon";
	protected static final String ASTARTLEVEL = "astartlevel";
	protected static final String MSTARTLEVEL = "mstartlevel";
	protected static final String AENDLEVEL = "aendlevel";
	protected static final String MENDLEVEL = "mendlevel";
	protected static final String PRICES = "prices";
	protected static final String REWARDS = "rewards";
	
	private UpgradeTier tier;

	public EditTierPanel(UpgradesAddon addon, GameModeAddon gamemode, User user, UpgradeTier tier, AbPanel parent) {
		super(addon, gamemode, user, tier.getUniqueId(), parent);
		
		this.tier = tier;
		
		
	}
	
	private void setButton() {
		
	}
	
}
