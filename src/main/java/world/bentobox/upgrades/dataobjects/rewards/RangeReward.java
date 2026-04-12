package world.bentobox.upgrades.dataobjects.rewards;

import com.google.gson.annotations.Expose;
import org.bukkit.Material;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.events.island.IslandEvent;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.config.Settings;
import world.bentobox.upgrades.dataobjects.UpgradeTier;
import world.bentobox.upgrades.ui.utils.AbPanel;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

public class RangeReward extends Reward {


    public RangeReward() {
        super("range_reward", Material.OAK_FENCE);
    }

    @Override
    public String getAdminName(User user) {
        return user.getTranslation("upgrades.rewards.rangeupgrade.name");
    }

    @Override
    public String getAdminDescription(User user) {
        return user.getTranslation("upgrades.rewards.rangeupgrade.admindescription");
    }

    @Override
    public String getPublicName(User user) {
        return user.getTranslation("upgrades.rewards.rangeupgrade.name");
    }

    @Override
    public String getPublicDescription(User user) {
        return user.getTranslation("upgrades.rewards.rangeupgrade.description");
    }

    @Override
    public String getPublicDescription(User user, RewardDB rewardDB) {
        RangeRewardDB db = (RangeRewardDB) rewardDB;
        return user.getTranslation("upgrades.rewards.rangeupgrade.description",
                "[rangelevel]", db.getRangeUpgradeEquation());
    }

    @Override
    public void apply(UpgradesAddon addon, User user, Island island, RewardDB rewardDB, int currentLevel) {
        RangeRewardDB db = (RangeRewardDB) rewardDB;
        Map<String, Double> variables = new TreeMap<>();
        variables.put(LEVEL_VAR, (double) currentLevel);
        variables.put(ISLAND_LEVEL_VAR, (double) addon.getUpgradesManager().getIslandLevel(island));
        variables.put(NUMBER_PLAYER_VAR, (double) island.getMemberSet().size());
        int amount = (int) Settings.evaluate(db.getRangeUpgradeEquation(), variables);

        int newRange = island.getProtectionRange() + amount;
        if (newRange > island.getRange()) {
            addon.logWarning("Tried to upgrade island range over max for island " + island.getUniqueId());
            return;
        }

        int oldRange = island.getProtectionRange();
        island.addBonusRange(addon.getDescription().getName(), amount, "");
        IslandEvent.builder().island(island).location(island.getCenter())
                .reason(IslandEvent.Reason.RANGE_CHANGE)
                .involvedPlayer(user.getUniqueId()).admin(false)
                .protectionRange(island.getProtectionRange(), oldRange).build();

        user.sendMessage("upgrades.ui.upgradepanel.rangeupgradedone", "[rangelevel]", Integer.toString(amount));
    }

    @Override
    public AbPanel getAdminPanel(UpgradesAddon addon, GameModeAddon gamemode, User user, AbPanel parent,
                                 UpgradeTier tier, @Nullable RewardDB saved) {
        RangeRewardDB dbObject;

        if (saved == null) {
            dbObject = new RangeRewardDB();
            List<RewardDB> rewards = tier.getRewards();

            rewards.add(dbObject);
            tier.setRewards(rewards);
        } else if (saved instanceof RangeRewardDB) {
            dbObject = (RangeRewardDB) saved;
        } else {
            throw new InvalidParameterException(
                    "DB object in RangeReward which is not an RangeRewardDB");
        }

        return new RangeRewardPanel(addon, gamemode, user, parent, tier, dbObject);
    }

    private final class RangeRewardPanel extends AbPanel {
        private static final String VALID = "valid";
        private static final String INVALID = "invalid";
        private static final String RULE = "rule";

        private final UpgradeTier tier;
        private final RangeRewardDB saved;

        public RangeRewardPanel(UpgradesAddon addon, GameModeAddon gamemode,
                                User user, AbPanel parent,
                                @NonNull UpgradeTier tier,
                                @NonNull RangeRewardDB saved) {
            super(addon, gamemode, user, user.getTranslation("upgrades.rewards.rangeupgrade.paneltitle"),
                    parent);

            this.tier = tier;
            this.saved = saved;

            this.createInterface();
        }

        private void createInterface() {
            this.fillBorder(Material.BLACK_STAINED_GLASS_PANE);

            // Show the formula status in-line with the formula item (#70 items 7 & 8)
            if (this.saved.isValid()) {
                this.setItems(VALID, new PanelItemBuilder()
                        .name(this.getUser().getTranslation("upgrades.rewards.rangeupgrade.formulastatus"))
                        .description(this.saved.getRangeUpgradeEquation())
                        .icon(Material.GREEN_CONCRETE)
                        .build(), 10);
            } else {
                this.setItems(INVALID, new PanelItemBuilder()
                        .name(this.getUser().getTranslation("upgrades.rewards.rangeupgrade.formulaneeded"))
                        .description(this.getUser().getTranslation("upgrades.rewards.rangeupgrade.formulaneededdesc"))
                        .icon(Material.RED_CONCRETE)
                        .build(), 10);
            }

            this.setItems(RULE, new PanelItemBuilder()
                    .name(this.getUser().getTranslation("upgrades.rewards.rangeupgrade.setformula"))
                    .description(this.saved.getRangeUpgradeEquation())
                    .icon(Material.PAPER)
                    .clickHandler(this.onSetRule())
                    .build(), 22);
        }

        private PanelItem.ClickHandler onSetRule() {
            return (panel, client, click, slot) -> {
                this.getAddon()
                        .getChatInput()
                        .askOneInput(this.doSetRule(),
                                input -> {
                                    // Validate that input is a parseable math expression (#70 item 8)
                                    try {
                                        Map<String, Double> vars = new TreeMap<>();
                                        vars.put(LEVEL_VAR, 1.0);
                                        vars.put(ISLAND_LEVEL_VAR, 1.0);
                                        vars.put(NUMBER_PLAYER_VAR, 1.0);
                                        Settings.evaluate(input, vars);
                                        return true;
                                    } catch (Exception e) {
                                        return false;
                                    }
                                },
                                client.getTranslation("upgrades.rewards.rangeupgrade.rulequestion",
                                        "[actual]", this.saved.getRangeUpgradeEquation()),
                                client.getTranslation("upgrades.rewards.rangeupgrade.invalidrule"),
                                client, false);
                return true;
            };
        }

        private Consumer<String> doSetRule() {
            return (rule) -> {
                this.saved.setRangeUpgradeEquation(rule);
                this.createInterface();
                this.getBuild()
                        .build();
            };
        }
    }
}
