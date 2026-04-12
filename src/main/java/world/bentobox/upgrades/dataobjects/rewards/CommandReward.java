package world.bentobox.upgrades.dataobjects.rewards;

import org.bukkit.Material;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.dataobjects.UpgradeTier;
import world.bentobox.upgrades.ui.utils.AbPanel;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

public class CommandReward extends Reward {

    public CommandReward() {
        super("command_reward", Material.COMMAND_BLOCK);
    }

    @Override
    public String getAdminName(User user) {
        return user.getTranslation("upgrades.rewards.command.name");
    }

    @Override
    public String getAdminDescription(User user) {
        return user.getTranslation("upgrades.rewards.command.admindescription");
    }

    @Override
    public String getPublicName(User user) {
        return user.getTranslation("upgrades.rewards.command.name");
    }

    @Override
    public String getPublicDescription(User user) {
        return user.getTranslation("upgrades.rewards.command.description");
    }

    @Override
    public void apply(UpgradesAddon addon, User user, Island island, RewardDB rewardDB, int currentLevel) {
        CommandRewardDB db = (CommandRewardDB) rewardDB;
        String playerName = user.getName();
        String ownerName = island.getPlugin().getPlayers().getName(island.getOwner());
        if (ownerName == null) ownerName = "";

        for (String cmd : db.getCommands()) {
            String formatted = cmd
                    .replace("[player]", playerName)
                    .replace("[owner]", ownerName);

            if (db.isConsole()) {
                addon.getServer().dispatchCommand(addon.getServer().getConsoleSender(), formatted);
            } else {
                addon.getServer().dispatchCommand(user.getSender(), formatted);
            }
        }
    }

    @Override
    public AbPanel getAdminPanel(UpgradesAddon addon, GameModeAddon gamemode, User user, AbPanel parent,
                                 UpgradeTier tier, @Nullable RewardDB saved) {
        CommandRewardDB dbObject;

        if (saved == null) {
            dbObject = new CommandRewardDB();
            List<RewardDB> rewards = tier.getRewards();
            rewards.add(dbObject);
            tier.setRewards(rewards);
        } else if (saved instanceof CommandRewardDB) {
            dbObject = (CommandRewardDB) saved;
        } else {
            throw new InvalidParameterException("DB object in CommandReward which is not a CommandRewardDB");
        }

        return new CommandRewardPanel(addon, gamemode, user, parent, tier, dbObject);
    }

    private final class CommandRewardPanel extends AbPanel {

        private static final String VALID = "valid";
        private static final String INVALID = "invalid";
        private static final String TOGGLE = "toggle";
        private static final String ADD_CMD = "addcmd";
        private static final String CLEAR_CMD = "clearcmd";

        private final UpgradeTier tier;
        private final CommandRewardDB saved;

        public CommandRewardPanel(UpgradesAddon addon, GameModeAddon gamemode, User user,
                                  AbPanel parent, @NonNull UpgradeTier tier,
                                  @NonNull CommandRewardDB saved) {
            super(addon, gamemode, user, user.getTranslation("upgrades.rewards.command.paneltitle"), parent);
            this.tier = tier;
            this.saved = saved;
            this.createInterface();
        }

        private void createInterface() {
            this.fillBorder(Material.BLACK_STAINED_GLASS_PANE);

            if (this.saved.isValid()) {
                this.setItems(VALID, new PanelItemBuilder().name(this.getUser()
                                .getTranslation("upgrades.ui.buttons.validconf"))
                        .icon(Material.GREEN_CONCRETE).build(), 10);
            } else {
                this.setItems(INVALID, new PanelItemBuilder().name(this.getUser()
                                .getTranslation("upgrades.ui.buttons.invalidconf"))
                        .icon(Material.RED_CONCRETE).build(), 10);
            }

            Material toggleIcon = this.saved.isConsole() ? Material.GREEN_CONCRETE : Material.YELLOW_CONCRETE;
            String toggleLabel = this.saved.isConsole() ? "Console" : "Player";
            this.setItems(TOGGLE, new PanelItemBuilder()
                    .name(toggleLabel)
                    .icon(toggleIcon)
                    .clickHandler(this.onToggle())
                    .build(), 20);

            List<String> cmdLore = new ArrayList<>(this.saved.getCommands());
            this.setItems(ADD_CMD, new PanelItemBuilder()
                    .name("Add command")
                    .description(cmdLore)
                    .icon(Material.PAPER)
                    .clickHandler(this.onAddCommand())
                    .build(), 22);

            this.setItems(CLEAR_CMD, new PanelItemBuilder()
                    .name("Clear all commands")
                    .icon(Material.RED_CONCRETE)
                    .clickHandler(this.onClearCommands())
                    .build(), 24);
        }

        private PanelItem.ClickHandler onToggle() {
            return (panel, client, click, slot) -> {
                this.saved.setConsole(!this.saved.isConsole());
                this.createInterface();
                this.getBuild().build();
                return true;
            };
        }

        private PanelItem.ClickHandler onAddCommand() {
            return (panel, client, click, slot) -> {
                this.getAddon().getChatInput().askOneInput(
                        cmd -> {
                            List<String> cmds = new ArrayList<>(this.saved.getCommands());
                            cmds.add(cmd);
                            this.saved.setCommands(cmds);
                            this.createInterface();
                            this.getBuild().build();
                        },
                        input -> true,
                        "Enter command (use [player], [owner] as placeholders)",
                        "", client, false);
                return true;
            };
        }

        private PanelItem.ClickHandler onClearCommands() {
            return (panel, client, click, slot) -> {
                this.saved.setCommands(new ArrayList<>());
                this.createInterface();
                this.getBuild().build();
                return true;
            };
        }
    }
}
