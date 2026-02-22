package world.bentobox.upgrades.dataobjects.rewards;

import com.google.gson.annotations.Expose;
import org.bukkit.Material;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.dataobjects.UpgradeTier;
import world.bentobox.upgrades.ui.utils.AbPanel;

import java.security.InvalidParameterException;
import java.util.Date;
import java.util.List;
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

            if (this.saved.isValid()) {
                this.setItems(VALID, new PanelItemBuilder().name(this.getUser()
                                .getTranslation("upgrades.ui.buttons.validconf"))
                        .icon(Material.GREEN_CONCRETE)
                        .build(), 10);
            } else {
                this.setItems(INVALID, new PanelItemBuilder().name(this.getUser()
                                .getTranslation("upgrades.ui.buttons.invalidconf"))
                        .icon(Material.RED_CONCRETE)
                        .build(), 10);
            }

            this.setItems(RULE, new PanelItemBuilder().name(this.saved.getRangeUpgradeEquation())
                    .icon(Material.PAPER)
                    .clickHandler(this.onSetRule())
                    .build(), 22);
        }

        private PanelItem.ClickHandler onSetRule() {
            return (panel, client, click, slot) -> {
                this.getAddon()
                        .getChatInput()
                        .askOneInput(this.doSetRule(), input -> true,
                                client.getTranslation("upgrades.rewards.rangeupgrade.rulequestion",
                                        "[actual]", this.saved.getRangeUpgradeEquation()), "", client,
                                false);
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
