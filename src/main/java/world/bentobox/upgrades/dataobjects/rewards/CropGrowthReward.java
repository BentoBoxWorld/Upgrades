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

public class CropGrowthReward extends Reward {

    public CropGrowthReward() {
        super("crop_growth_reward", Material.WHEAT);
    }

    @Override
    public String getAdminName(User user) {
        return user.getTranslation("upgrades.rewards.cropgrowth.name");
    }

    @Override
    public String getAdminDescription(User user) {
        return user.getTranslation("upgrades.rewards.cropgrowth.admindescription");
    }

    @Override
    public String getPublicName(User user) {
        return user.getTranslation("upgrades.rewards.cropgrowth.name");
    }

    @Override
    public String getPublicDescription(User user) {
        return user.getTranslation("upgrades.rewards.cropgrowth.description");
    }

    @Override
    public String getPublicDescription(User user, RewardDB rewardDB) {
        CropGrowthRewardDB db = (CropGrowthRewardDB) rewardDB;
        return user.getTranslation("upgrades.rewards.cropgrowth.description",
                "[amount]", db.getGrowthBonusEquation());
    }

    @Override
    public void apply(UpgradesAddon addon, User user, Island island, RewardDB rewardDB) {
        // No-op: ongoing effect handled by CropGrowthListener
    }

    @Override
    public AbPanel getAdminPanel(UpgradesAddon addon, GameModeAddon gamemode, User user, AbPanel parent,
                                 UpgradeTier tier, @Nullable RewardDB saved) {
        CropGrowthRewardDB dbObject;

        if (saved == null) {
            dbObject = new CropGrowthRewardDB();
            List<RewardDB> rewards = tier.getRewards();
            rewards.add(dbObject);
            tier.setRewards(rewards);
        } else if (saved instanceof CropGrowthRewardDB) {
            dbObject = (CropGrowthRewardDB) saved;
        } else {
            throw new InvalidParameterException(
                    "DB object in CropGrowthReward which is not a CropGrowthRewardDB");
        }

        return new CropGrowthRewardPanel(addon, gamemode, user, parent, tier, dbObject);
    }

    private final class CropGrowthRewardPanel extends AbPanel {
        private static final String VALID = "valid";
        private static final String INVALID = "invalid";
        private static final String RULE = "rule";

        private final UpgradeTier tier;
        private final CropGrowthRewardDB saved;

        public CropGrowthRewardPanel(UpgradesAddon addon, GameModeAddon gamemode,
                                     User user, AbPanel parent,
                                     @NonNull UpgradeTier tier,
                                     @NonNull CropGrowthRewardDB saved) {
            super(addon, gamemode, user, user.getTranslation("upgrades.rewards.cropgrowth.paneltitle"),
                    parent);

            this.tier = tier;
            this.saved = saved;

            this.createInterface();
        }

        private void createInterface() {
            this.fillBorder(Material.BLACK_STAINED_GLASS_PANE);

            if (this.saved.isValid()) {
                this.setItems(VALID, new PanelItemBuilder()
                        .name(this.getUser().getTranslation("upgrades.rewards.cropgrowth.formulastatus"))
                        .description(this.saved.getGrowthBonusEquation())
                        .icon(Material.GREEN_CONCRETE)
                        .build(), 10);
            } else {
                this.setItems(INVALID, new PanelItemBuilder()
                        .name(this.getUser().getTranslation("upgrades.rewards.cropgrowth.formulaneeded"))
                        .description(this.getUser().getTranslation("upgrades.rewards.cropgrowth.formulaneededdesc"))
                        .icon(Material.RED_CONCRETE)
                        .build(), 10);
            }

            this.setItems(RULE, new PanelItemBuilder()
                    .name(this.getUser().getTranslation("upgrades.rewards.cropgrowth.setformula"))
                    .description(this.saved.getGrowthBonusEquation())
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
                                client.getTranslation("upgrades.rewards.cropgrowth.rulequestion",
                                        "[actual]", this.saved.getGrowthBonusEquation()),
                                client.getTranslation("upgrades.rewards.cropgrowth.invalidrule"),
                                client, false);
                return true;
            };
        }

        private Consumer<String> doSetRule() {
            return (rule) -> {
                this.saved.setGrowthBonusEquation(rule);
                this.createInterface();
                this.getBuild().build();
            };
        }
    }
}
