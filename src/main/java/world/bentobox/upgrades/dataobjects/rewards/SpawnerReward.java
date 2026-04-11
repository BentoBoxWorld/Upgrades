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
import world.bentobox.upgrades.config.Settings;
import world.bentobox.upgrades.dataobjects.UpgradeTier;
import world.bentobox.upgrades.ui.utils.AbPanel;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

public class SpawnerReward extends Reward {

    public SpawnerReward() {
        super("spawner_reward", Material.SPAWNER);
    }

    @Override
    public String getAdminName(User user) {
        return user.getTranslation("upgrades.rewards.spawner.name");
    }

    @Override
    public String getAdminDescription(User user) {
        return user.getTranslation("upgrades.rewards.spawner.admindescription");
    }

    @Override
    public String getPublicName(User user) {
        return user.getTranslation("upgrades.rewards.spawner.name");
    }

    @Override
    public String getPublicDescription(User user) {
        return user.getTranslation("upgrades.rewards.spawner.description");
    }

    @Override
    public String getPublicDescription(User user, RewardDB rewardDB) {
        SpawnerRewardDB db = (SpawnerRewardDB) rewardDB;
        return user.getTranslation("upgrades.rewards.spawner.description",
                "[amount]", db.getSpawnBonusEquation());
    }

    @Override
    public void apply(UpgradesAddon addon, User user, Island island, RewardDB rewardDB, int currentLevel) {
        // No-op: ongoing effect handled by SpawnerUpgradeListener
    }

    @Override
    public AbPanel getAdminPanel(UpgradesAddon addon, GameModeAddon gamemode, User user, AbPanel parent,
                                 UpgradeTier tier, @Nullable RewardDB saved) {
        SpawnerRewardDB dbObject;

        if (saved == null) {
            dbObject = new SpawnerRewardDB();
            List<RewardDB> rewards = tier.getRewards();
            rewards.add(dbObject);
            tier.setRewards(rewards);
        } else if (saved instanceof SpawnerRewardDB) {
            dbObject = (SpawnerRewardDB) saved;
        } else {
            throw new InvalidParameterException(
                    "DB object in SpawnerReward which is not a SpawnerRewardDB");
        }

        return new SpawnerRewardPanel(addon, gamemode, user, parent, tier, dbObject);
    }

    private final class SpawnerRewardPanel extends AbPanel {
        private static final String VALID = "valid";
        private static final String INVALID = "invalid";
        private static final String RULE = "rule";

        private final UpgradeTier tier;
        private final SpawnerRewardDB saved;

        public SpawnerRewardPanel(UpgradesAddon addon, GameModeAddon gamemode,
                                  User user, AbPanel parent,
                                  @NonNull UpgradeTier tier,
                                  @NonNull SpawnerRewardDB saved) {
            super(addon, gamemode, user, user.getTranslation("upgrades.rewards.spawner.paneltitle"),
                    parent);

            this.tier = tier;
            this.saved = saved;

            this.createInterface();
        }

        private void createInterface() {
            this.fillBorder(Material.BLACK_STAINED_GLASS_PANE);

            if (this.saved.isValid()) {
                this.setItems(VALID, new PanelItemBuilder()
                        .name(this.getUser().getTranslation("upgrades.rewards.spawner.formulastatus"))
                        .description(this.saved.getSpawnBonusEquation())
                        .icon(Material.GREEN_CONCRETE)
                        .build(), 10);
            } else {
                this.setItems(INVALID, new PanelItemBuilder()
                        .name(this.getUser().getTranslation("upgrades.rewards.spawner.formulaneeded"))
                        .description(this.getUser().getTranslation("upgrades.rewards.spawner.formulaneededdesc"))
                        .icon(Material.RED_CONCRETE)
                        .build(), 10);
            }

            this.setItems(RULE, new PanelItemBuilder()
                    .name(this.getUser().getTranslation("upgrades.rewards.spawner.setformula"))
                    .description(this.saved.getSpawnBonusEquation())
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
                                    try {
                                        Map<String, Double> vars = new TreeMap<>();
                                        vars.put("[level]", 1.0);
                                        vars.put("[islandLevel]", 1.0);
                                        vars.put("[numberPlayer]", 1.0);
                                        Settings.evaluate(input, vars);
                                        return true;
                                    } catch (Exception e) {
                                        return false;
                                    }
                                },
                                client.getTranslation("upgrades.rewards.spawner.rulequestion",
                                        "[actual]", this.saved.getSpawnBonusEquation()),
                                client.getTranslation("upgrades.rewards.spawner.invalidrule"),
                                client, false);
                return true;
            };
        }

        private Consumer<String> doSetRule() {
            return (rule) -> {
                this.saved.setSpawnBonusEquation(rule);
                this.createInterface();
                this.getBuild().build();
            };
        }
    }
}
