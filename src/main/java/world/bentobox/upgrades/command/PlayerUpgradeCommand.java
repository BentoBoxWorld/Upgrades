package world.bentobox.upgrades.command;

import java.util.List;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
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

    @Override
    public boolean execute(User user, String label, List<String> args) {
        if (args.isEmpty()) {
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

    UpgradesAddon addon;

}
