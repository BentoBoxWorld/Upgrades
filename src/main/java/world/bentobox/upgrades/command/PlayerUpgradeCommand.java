package world.bentobox.upgrades.command;

import java.util.List;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.ui.Panel;

public class PlayerUpgradeCommand extends CompositeCommand {

	public PlayerUpgradeCommand(UpgradesAddon addon, CompositeCommand cmd) {
		super(addon, cmd, "upgrade");
		
		this.addon = addon;
	}
	
	@Override
	public void setup() {
		this.setDescription("upgrades.commands.main.description");
		this.setOnlyPlayer(true);
	}
	
	@Override
	public boolean canExecute(User user, String label, List<String> args) {
		boolean hasIsland = getIslands().getIsland(getWorld(), user) != null;
		
		if (!hasIsland)
			user.sendMessage("general.errors.no-island");
		return hasIsland;
	}
	
	@Override
	public boolean execute(User user, String label, List<String> args) {
		if (args.size() == 0) {
			new Panel(this.addon).showPanel(user);
			return true;
		}
		this.showHelp(this, user);
		return false;
	}
	
	UpgradesAddon addon;
	
}
