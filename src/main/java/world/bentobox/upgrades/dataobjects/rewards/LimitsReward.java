package world.bentobox.upgrades.dataobjects.rewards;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.limits.listeners.BlockLimitsListener;
import world.bentobox.limits.objects.IslandBlockCount;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.config.Settings;
import world.bentobox.upgrades.dataobjects.UpgradeTier;
import world.bentobox.upgrades.ui.utils.AbPanel;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

public class LimitsReward extends Reward {

    public LimitsReward() {
        super("limits_reward", Material.BARRIER);
    }

    @Override
    public String getAdminName(User user) {
        return user.getTranslation("upgrades.rewards.limits.name");
    }

    @Override
    public String getAdminDescription(User user) {
        return user.getTranslation("upgrades.rewards.limits.admindescription");
    }

    @Override
    public String getPublicName(User user) {
        return user.getTranslation("upgrades.rewards.limits.name");
    }

    @Override
    public String getPublicDescription(User user) {
        return user.getTranslation("upgrades.rewards.limits.description",
                "[type]", "?", "[target]", "?", "[amount]", "?");
    }

    @Override
    public String getPublicDescription(User user, RewardDB rewardDB) {
        LimitsRewardDB db = (LimitsRewardDB) rewardDB;
        return user.getTranslation("upgrades.rewards.limits.description",
                "[type]", db.getLimitType(),
                "[target]", db.getTarget(),
                "[amount]", db.getAmountEquation());
    }

    @Override
    public void apply(UpgradesAddon addon, User user, Island island, RewardDB rewardDB) {
        if (!addon.isLimitsProvided()) {
            addon.logWarning("LimitsReward: Limits addon not available");
            return;
        }

        LimitsRewardDB db = (LimitsRewardDB) rewardDB;
        Map<String, Double> variables = new TreeMap<>();
        variables.put("[level]", 0.0);
        variables.put("[islandLevel]", (double) addon.getUpgradesManager().getIslandLevel(island));
        variables.put("[numberPlayer]", (double) island.getMemberSet().size());
        int amount = (int) Settings.evaluate(db.getAmountEquation(), variables);

        BlockLimitsListener bLListener = addon.getLimitsAddon().getBlockLimitListener();
        IslandBlockCount isb = bLListener.getIsland(island);

        if (isb == null) {
            addon.logWarning("LimitsReward: IslandBlockCount not found for island " + island.getUniqueId());
            return;
        }

        switch (db.getLimitType().toUpperCase()) {
            case "BLOCK" -> {
                try {
                    Material mat = Material.valueOf(db.getTarget().toUpperCase());
                    int oldCount = isb.getBlockLimitsOffset().getOrDefault(mat.getKey(), 0);
                    isb.setBlockLimitsOffset(mat.getKey(), oldCount + amount);
                } catch (IllegalArgumentException e) {
                    addon.logWarning("LimitsReward: invalid material '" + db.getTarget() + "'");
                }
            }
            case "ENTITY" -> {
                try {
                    EntityType et = EntityType.valueOf(db.getTarget().toUpperCase());
                    int oldCount = isb.getEntityLimitsOffset().getOrDefault(et, 0);
                    isb.setEntityLimitsOffset(et, oldCount + amount);
                } catch (IllegalArgumentException e) {
                    addon.logWarning("LimitsReward: invalid entity type '" + db.getTarget() + "'");
                }
            }
            case "ENTITY_GROUP" -> {
                int oldCount = isb.getEntityGroupLimitsOffset().getOrDefault(db.getTarget(), 0);
                isb.setEntityGroupLimitsOffset(db.getTarget(), oldCount + amount);
            }
            default -> addon.logWarning("LimitsReward: unknown limit type '" + db.getLimitType() + "'");
        }
    }

    @Override
    public AbPanel getAdminPanel(UpgradesAddon addon, GameModeAddon gamemode, User user, AbPanel parent,
                                 UpgradeTier tier, @Nullable RewardDB saved) {
        LimitsRewardDB dbObject;

        if (saved == null) {
            dbObject = new LimitsRewardDB();
            List<RewardDB> rewards = tier.getRewards();
            rewards.add(dbObject);
            tier.setRewards(rewards);
        } else if (saved instanceof LimitsRewardDB) {
            dbObject = (LimitsRewardDB) saved;
        } else {
            throw new InvalidParameterException("DB object in LimitsReward which is not a LimitsRewardDB");
        }

        return new LimitsRewardPanel(addon, gamemode, user, parent, tier, dbObject);
    }

    private final class LimitsRewardPanel extends AbPanel {

        private static final String VALID = "valid";
        private static final String INVALID = "invalid";
        private static final String TYPE = "type";
        private static final String TARGET = "target";
        private static final String AMOUNT = "amount";

        private final UpgradeTier tier;
        private final LimitsRewardDB saved;

        public LimitsRewardPanel(UpgradesAddon addon, GameModeAddon gamemode, User user,
                                 AbPanel parent, @NonNull UpgradeTier tier,
                                 @NonNull LimitsRewardDB saved) {
            super(addon, gamemode, user, user.getTranslation("upgrades.rewards.limits.paneltitle"), parent);
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

            this.setItems(TYPE, new PanelItemBuilder()
                    .name(this.saved.getLimitType())
                    .icon(Material.COMPARATOR)
                    .clickHandler(this.onCycleType())
                    .build(), 20);

            this.setItems(TARGET, new PanelItemBuilder()
                    .name(this.saved.getTarget().isEmpty() ? "Not set" : this.saved.getTarget())
                    .icon(Material.PAPER)
                    .clickHandler(this.onSetTarget())
                    .build(), 22);

            this.setItems(AMOUNT, new PanelItemBuilder()
                    .name(this.saved.getAmountEquation())
                    .icon(Material.PAPER)
                    .clickHandler(this.onSetAmount())
                    .build(), 24);
        }

        private PanelItem.ClickHandler onCycleType() {
            return (panel, client, click, slot) -> {
                switch (this.saved.getLimitType()) {
                    case "BLOCK" -> this.saved.setLimitType("ENTITY");
                    case "ENTITY" -> this.saved.setLimitType("ENTITY_GROUP");
                    default -> this.saved.setLimitType("BLOCK");
                }
                this.createInterface();
                this.getBuild().build();
                return true;
            };
        }

        private PanelItem.ClickHandler onSetTarget() {
            return (panel, client, click, slot) -> {
                this.getAddon().getChatInput().askOneInput(
                        rule -> {
                            this.saved.setTarget(rule);
                            this.createInterface();
                            this.getBuild().build();
                        },
                        input -> true,
                        "Enter target (" + this.saved.getLimitType() + "). Current: " + this.saved.getTarget(),
                        "", client, false);
                return true;
            };
        }

        private PanelItem.ClickHandler onSetAmount() {
            return (panel, client, click, slot) -> {
                this.getAddon().getChatInput().askOneInput(
                        rule -> {
                            this.saved.setAmountEquation(rule);
                            this.createInterface();
                            this.getBuild().build();
                        },
                        input -> true,
                        "Enter amount formula. Current: " + this.saved.getAmountEquation(),
                        "", client, false);
                return true;
            };
        }
    }
}
