package world.bentobox.upgrades.dataobjects.prices;

import org.bukkit.Material;

import org.eclipse.jdt.annotation.Nullable;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.dataobjects.UpgradeTier;
import world.bentobox.upgrades.ui.PanelAdminItem;
import world.bentobox.upgrades.ui.PanelPublicItem;
import world.bentobox.upgrades.ui.utils.AbPanel;

public abstract class Price implements PanelAdminItem, PanelPublicItem {

    private final String name;
    private final Material icon;

    public Price(String name, Material icon) {
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

}
