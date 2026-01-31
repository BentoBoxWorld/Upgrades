package world.bentobox.upgrades.command;

import java.util.List;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.ui.Panel;

/**
 * Player command for accessing the island upgrades panel.
 * This command allows players to view and purchase upgrades for their island.
 * Players must be on their island and have sufficient rank to use this command.
 *
 * @author tastybento
 * @since 1.0.0
 */
public class PlayerUpgradeCommand extends CompositeCommand {

	/**
	 * Constructs a new player upgrade command.
	 *
	 * @param addon the Upgrades addon instance
	 * @param cmd the parent composite command to attach this command to
	 */
	public PlayerUpgradeCommand(UpgradesAddon addon, CompositeCommand cmd) {
		super(addon, cmd, "upgrade");
		
		this.addon = addon;
	}
	
	/**
	 * Sets up the command configuration.
	 * Configures the command description and restricts it to players only.
	 */
	@Override
	public void setup() {
		this.setDescription("upgrades.commands.main.description");
		this.setOnlyPlayer(true);
	}
	
	/**
	 * Checks if the user can execute the upgrade command.
	 * Validates that:
	 * <ul>
	 *   <li>The user has an island</li>
	 *   <li>The user is currently on their island</li>
	 *   <li>The user has sufficient rank (determined by UPGRADES_RANK_RIGHT flag)</li>
	 * </ul>
	 *
	 * @param user the user executing the command
	 * @param label the command label used
	 * @param args the command arguments
	 * @return {@code true} if the user can execute the command, {@code false} otherwise
	 */
	@Override
	public boolean canExecute(User user, String label, List<String> args) {
		Island island = getIslands().getIsland(this.getWorld(), user);
		
		if (island == null) {
			user.sendMessage("general.errors.no-island");
			return false;
		}
		
		if (!island.onIsland(user.getLocation())) {
			user.sendMessage("upgrades.error.notonisland");
			return false;
		}
		
		if (!island.isAllowed(user, UpgradesAddon.UPGRADES_RANK_RIGHT)) {
			user.sendMessage("general.errors.insufficient-rank",
				TextVariables.RANK,
				user.getTranslation(this.addon.getPlugin().getRanksManager().getRank(island.getRank(user))));
			return false;
		}
		
		return true;
	}
	
	/**
	 * Executes the upgrade command.
	 * If no arguments are provided, opens the upgrades panel for the user.
	 * Otherwise, displays the help information.
	 *
	 * @param user the user executing the command
	 * @param label the command label used
	 * @param args the command arguments
	 * @return {@code true} if the command executed successfully, {@code false} otherwise
	 */
	@Override
	public boolean execute(User user, String label, List<String> args) {
		if (args.size() == 0) {
			Island island = getIslands().getIsland(this.getWorld(), user); 
			
			if (island == null) {
				user.sendMessage("general.errors.no-island");
				return false;
			}
			
			if (!island.onIsland(user.getLocation())) {
				user.sendMessage("upgrades.error.notonisland");
				return false;
			}
			
			new Panel(this.addon, island).showPanel(user);
			return true;
		}
		this.showHelp(this, user);
		return false;
	}
	
	/**
	 * The Upgrades addon instance.
	 */
	UpgradesAddon addon;
	
}
