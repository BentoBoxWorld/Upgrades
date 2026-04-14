package world.bentobox.upgrades.dataobjects.prices;

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
import java.util.List;
import java.util.function.Consumer;

public class PermissionPrice extends Price {

    public PermissionPrice() {
        super("permission_price", Material.TRIPWIRE_HOOK);
    }

    @Override
    public String getPublicName(User user) {
        return user.getTranslation("upgrades.prices.permission.name");
    }

    @Override
    public String getAdminName(User user) {
        return user.getTranslation("upgrades.prices.permission.name");
    }

    @Override
    public String getPublicDescription(User user) {
        return user.getTranslation("upgrades.prices.permission.description", "[permission]", "?");
    }

    @Override
    public String getPublicDescription(User user, PriceDB priceDB) {
        PermissionPriceDB db = (PermissionPriceDB) priceDB;
        return user.getTranslation("upgrades.prices.permission.description",
                "[permission]", db.getPermission());
    }

    @Override
    public String getAdminDescription(User user) {
        return user.getTranslation("upgrades.prices.permission.admindescription");
    }

    @Override
    public boolean canPay(UpgradesAddon addon, User user, Island island, PriceDB priceDB, int currentLevel) {
        PermissionPriceDB db = (PermissionPriceDB) priceDB;
        return user.hasPermission(db.getPermission());
    }

    @Override
    public void pay(UpgradesAddon addon, User user, Island island, PriceDB priceDB, int currentLevel) {
        // Permission is a gate, not consumed
    }

    @Override
    public AbPanel getAdminPanel(UpgradesAddon addon, GameModeAddon gamemode, User user, AbPanel parent,
                                 UpgradeTier tier, @Nullable PriceDB saved) {
        PermissionPriceDB dbObject;

        if (saved == null) {
            dbObject = new PermissionPriceDB();
            List<PriceDB> prices = tier.getPrices();
            prices.add(dbObject);
            tier.setPrices(prices);
        } else if (saved instanceof PermissionPriceDB) {
            dbObject = (PermissionPriceDB) saved;
        } else {
            throw new InvalidParameterException("DB object in PermissionPrice which is not a PermissionPriceDB");
        }

        return new PermissionPricePanel(addon, gamemode, user, parent, tier, dbObject);
    }

    private final class PermissionPricePanel extends AbPanel {

        private static final String VALID = "valid";
        private static final String INVALID = "invalid";
        private static final String RULE = "rule";

        private final UpgradeTier tier;
        private final PermissionPriceDB saved;

        public PermissionPricePanel(UpgradesAddon addon, GameModeAddon gamemode, User user,
                                    AbPanel parent, @NonNull UpgradeTier tier,
                                    @NonNull PermissionPriceDB saved) {
            super(addon, gamemode, user, user.getTranslation("upgrades.prices.permission.paneltitle"), parent);
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

            this.setItems(RULE, new PanelItemBuilder().name(this.saved.getPermission().isEmpty() ? "Not set" : this.saved.getPermission())
                    .icon(Material.PAPER)
                    .clickHandler(this.onSetRule())
                    .build(), 22);
        }

        private PanelItem.ClickHandler onSetRule() {
            return (panel, client, click, slot) -> {
                this.getAddon().getChatInput().askOneInput(this.doSetRule(), input -> true,
                        client.getTranslation("upgrades.prices.permission.rulequestion",
                                "[actual]", this.saved.getPermission()), "", client, false);
                return true;
            };
        }

        private Consumer<String> doSetRule() {
            return (rule) -> {
                this.saved.setPermission(rule);
                this.getAddon().getUpgradeDataManager().saveUpgradeTier(this.tier);
                this.createInterface();
                this.getBuild().build();
            };
        }
    }
}
