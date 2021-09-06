package world.bentobox.upgrades.dataobjects.prices;

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
import java.util.List;
import java.util.function.Consumer;

public class IslandLevelPrice extends Price {

    // ------------------------------------------------------------
    // Section: Variables
    // ------------------------------------------------------------

    public IslandLevelPrice() {
        super("island_level", Material.EXPERIENCE_BOTTLE);
    }

    @Override
    public String getPublicName(User user) {
        return user.getTranslation("upgrades.prices.islandlevel.name");
    }

    @Override
    public String getAdminName(User user) {
        return user.getTranslation("upgrades.prices.islandlevel.name");
    }

    @Override
    public String getPublicDescription(User user) {
        return user.getTranslation("upgrades.prices.islandlevel.description");
    }

    @Override
    public String getAdminDescription(User user) {
        return user.getTranslation("upgrades.prices.islandlevel.admindescription");
    }

    @Override
    public AbPanel getAdminPanel(UpgradesAddon addon, GameModeAddon gamemode, User user, AbPanel parent,
                                 UpgradeTier tier, @Nullable PriceDB saved) {
        IslandLevelPriceDB dbObject;

        if (saved == null) {
            dbObject = new IslandLevelPriceDB("0");
            List<PriceDB> prices = tier.getPrices();

            prices.add(dbObject);
            tier.setPrices(prices);
        } else if (saved instanceof IslandLevelPriceDB) {
            dbObject = (IslandLevelPriceDB) saved;
        } else {
            throw new InvalidParameterException(
                    "DB object in IslandLevelPrice which is not an IslandLevelPriceDB");
        }

        return new IslandLevelPricePanel(addon, gamemode, user, parent, tier, dbObject);
    }

    private final class IslandLevelPricePanel extends AbPanel {

        private static final String VALID = "valid";
        private static final String INVALID = "invalid";
        private static final String RULE = "rule";

        private final UpgradeTier tier;
        private final IslandLevelPriceDB saved;

        public IslandLevelPricePanel(UpgradesAddon addon, GameModeAddon gamemode, User user,
                                     AbPanel parent, @NonNull UpgradeTier tier,
                                     @NonNull IslandLevelPriceDB saved) {
            super(addon, gamemode, user, user.getTranslation("upgrades.prices.islandlevel.paneltitle"),
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

            this.setItems(RULE, new PanelItemBuilder().name(this.saved.getLevelNeededEquation())
                    .icon(Material.PAPER)
                    .clickHandler(this.onSetRule())
                    .build(), 22);
        }

        private PanelItem.ClickHandler onSetRule() {
            return (panel, client, click, slot) -> {
                this.getAddon()
                        .getChatInput()
                        .askOneInput(this.doSetRule(), input -> true,
                                client.getTranslation("upgrades.prices.islandlevel.rulequestion",
                                        "[actual]", this.saved.getLevelNeededEquation()), "", client,
                                false);
                return true;
            };
        }

        private Consumer<String> doSetRule() {
            return (rule) -> {
                this.saved.setLevelNeededEquation(rule);
                this.createInterface();
                this.getBuild()
                        .build();
            };
        }
    }
}
