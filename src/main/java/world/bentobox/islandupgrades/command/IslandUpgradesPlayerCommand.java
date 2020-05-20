package world.bentobox.islandupgrades.command;

import java.util.List;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.islandupgrades.IslandUpgradesAddon;
import world.bentobox.islandupgrades.ui.Panel;

public class IslandUpgradesPlayerCommand extends CompositeCommand {

	public IslandUpgradesPlayerCommand(IslandUpgradesAddon addon, CompositeCommand cmd) {
		super(addon, cmd, "upgrade");
		
		this.addon = addon;
	}
	
	@Override
	public void setup() {
		this.setDescription("islandupgrades.commands.main.description");
		this.setOnlyPlayer(true);
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
	
	IslandUpgradesAddon addon;
	
}
