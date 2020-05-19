package world.bentobox.islandshop.command;

import java.util.List;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.islandshop.IslandShopAddon;
import world.bentobox.islandshop.ui.Panel;

public class IslandShopPlayerCommand extends CompositeCommand {

	public IslandShopPlayerCommand(IslandShopAddon addon, CompositeCommand cmd) {
		super(addon, cmd, "upgrade");
		
		this.addon = addon;
	}
	
	@Override
	public void setup() {
		this.setDescription("islandshop.commands.main.description");
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
	
	IslandShopAddon addon;
	
}
