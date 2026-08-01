package world.bentobox.upgrades.dataobjects.prices;

import org.bukkit.Material;

import org.eclipse.jdt.annotation.Nullable;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.dataobjects.FormulaVariables;
import world.bentobox.upgrades.dataobjects.UpgradeTier;
import world.bentobox.upgrades.ui.PanelAdminItem;
import world.bentobox.upgrades.ui.PanelPublicItem;
import world.bentobox.upgrades.ui.utils.AbPanel;

public abstract class Price implements PanelAdminItem, PanelPublicItem {

    public static final String LEVEL_VAR = FormulaVariables.LEVEL_VAR;
    public static final String ISLAND_LEVEL_VAR = FormulaVariables.ISLAND_LEVEL_VAR;
    public static final String NUMBER_PLAYER_VAR = FormulaVariables.NUMBER_PLAYER_VAR;

    private final String name;
    private final Material icon;

    protected Price(String name, Material icon) {
        this.name = name;
        this.icon = icon;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Material getIcon() {
        return this.icon;
    }

    public abstract AbPanel getAdminPanel(UpgradesAddon addon, GameModeAddon gamemode, User user,
                                          AbPanel parent, UpgradeTier tier, @Nullable PriceDB saved);

    /**
     * Returns a player-facing description with any formula placeholders substituted
     * using values from the stored DB object.  Override in concrete Price types that
     * carry a formula (e.g. MoneyPrice substitutes [amount]).
     * The default falls back to {@link PanelPublicItem#getPublicDescription(User)}.
     */
    public String getPublicDescription(User user, PriceDB priceDB) {
        return this.getPublicDescription(user);
    }

    public abstract boolean canPay(UpgradesAddon addon, User user, Island island, PriceDB priceDB, int currentLevel);

    public abstract void pay(UpgradesAddon addon, User user, Island island, PriceDB priceDB, int currentLevel);

}
