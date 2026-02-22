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
import world.bentobox.upgrades.config.Settings;
import world.bentobox.upgrades.dataobjects.UpgradeTier;
import world.bentobox.upgrades.ui.utils.AbPanel;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

public class MoneyPrice extends Price {

    public MoneyPrice() {
        super("money_price", Material.GOLD_INGOT);
    }

    @Override
    public String getPublicName(User user) {
        return user.getTranslation("upgrades.prices.money.name");
    }

    @Override
    public String getAdminName(User user) {
        return user.getTranslation("upgrades.prices.money.name");
    }

    @Override
    public String getPublicDescription(User user) {
        return user.getTranslation("upgrades.prices.money.description");
    }

    @Override
    public String getAdminDescription(User user) {
        return user.getTranslation("upgrades.prices.money.admindescription");
    }

    @Override
    public boolean canPay(UpgradesAddon addon, User user, Island island, PriceDB priceDB) {
        if (!addon.isVaultProvided()) return true;
        MoneyPriceDB db = (MoneyPriceDB) priceDB;
        Map<String, Double> variables = new TreeMap<>();
        variables.put("[level]", 0.0);
        variables.put("[islandLevel]", (double) addon.getUpgradesManager().getIslandLevel(island));
        variables.put("[numberPlayer]", (double) island.getMemberSet().size());
        double amount = Settings.evaluate(db.getAmountEquation(), variables);
        return addon.getVaultHook().has(user, amount);
    }

    @Override
    public void pay(UpgradesAddon addon, User user, Island island, PriceDB priceDB) {
        if (!addon.isVaultProvided()) return;
        MoneyPriceDB db = (MoneyPriceDB) priceDB;
        Map<String, Double> variables = new TreeMap<>();
        variables.put("[level]", 0.0);
        variables.put("[islandLevel]", (double) addon.getUpgradesManager().getIslandLevel(island));
        variables.put("[numberPlayer]", (double) island.getMemberSet().size());
        double amount = Settings.evaluate(db.getAmountEquation(), variables);
        var response = addon.getVaultHook().withdraw(user, amount);
        if (!response.transactionSuccess()) {
            addon.logWarning("Money withdrawal failed for user " + user.getName() + ": " + response.errorMessage);
        }
    }

    @Override
    public AbPanel getAdminPanel(UpgradesAddon addon, GameModeAddon gamemode, User user, AbPanel parent,
                                 UpgradeTier tier, @Nullable PriceDB saved) {
        MoneyPriceDB dbObject;

        if (saved == null) {
            dbObject = new MoneyPriceDB();
            List<PriceDB> prices = tier.getPrices();
            prices.add(dbObject);
            tier.setPrices(prices);
        } else if (saved instanceof MoneyPriceDB) {
            dbObject = (MoneyPriceDB) saved;
        } else {
            throw new InvalidParameterException("DB object in MoneyPrice which is not a MoneyPriceDB");
        }

        return new MoneyPricePanel(addon, gamemode, user, parent, tier, dbObject);
    }

    private final class MoneyPricePanel extends AbPanel {

        private static final String VALID = "valid";
        private static final String INVALID = "invalid";
        private static final String RULE = "rule";

        private final UpgradeTier tier;
        private final MoneyPriceDB saved;

        public MoneyPricePanel(UpgradesAddon addon, GameModeAddon gamemode, User user,
                               AbPanel parent, @NonNull UpgradeTier tier,
                               @NonNull MoneyPriceDB saved) {
            super(addon, gamemode, user, user.getTranslation("upgrades.prices.money.paneltitle"), parent);
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

            this.setItems(RULE, new PanelItemBuilder().name(this.saved.getAmountEquation())
                    .icon(Material.PAPER)
                    .clickHandler(this.onSetRule())
                    .build(), 22);
        }

        private PanelItem.ClickHandler onSetRule() {
            return (panel, client, click, slot) -> {
                this.getAddon().getChatInput().askOneInput(this.doSetRule(), input -> true,
                        client.getTranslation("upgrades.prices.money.rulequestion",
                                "[actual]", this.saved.getAmountEquation()), "", client, false);
                return true;
            };
        }

        private Consumer<String> doSetRule() {
            return (rule) -> {
                this.saved.setAmountEquation(rule);
                this.createInterface();
                this.getBuild().build();
            };
        }
    }
}
