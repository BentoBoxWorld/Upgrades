package world.bentobox.upgrades.command.admin;

import java.util.List;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.ui.admin.AdminPanel;

public class AdminCommand extends CompositeCommand {
	
	public AdminCommand(UpgradesAddon addon, CompositeCommand cmd, GameModeAddon gameMode) {
		super(addon, cmd, "upgrade");
		
		this.addon = addon;
		this.gameMode = gameMode;
	}
	
	@Override
	public void setup() {
		this.setOnlyPlayer(true);
		this.setDescription("upgrades.commands.admin.description");
		this.setParametersHelp("upgrades.commands.admin.parameters");
		this.setPermission("admin.upgrade");
	}
	
	@Override
	public boolean execute(User user, String label, List<String> args) {
		if (user.isPlayer()) {
			new AdminPanel(this.addon, this.gameMode, user).getBuild().build();
			return true;
		}
		return false;
	}
	
	private UpgradesAddon addon;
	private GameModeAddon gameMode;

}
