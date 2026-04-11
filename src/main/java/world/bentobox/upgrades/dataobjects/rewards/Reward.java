package world.bentobox.upgrades.dataobjects.rewards;

import org.bukkit.Material;
import org.eclipse.jdt.annotation.Nullable;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.dataobjects.UpgradeTier;
import world.bentobox.upgrades.ui.PanelAdminItem;
import world.bentobox.upgrades.ui.PanelPublicItem;
import world.bentobox.upgrades.ui.utils.AbPanel;

public abstract class Reward implements PanelAdminItem, PanelPublicItem {

    private final String name;
    private final Material icon;

    public Reward(String name, Material icon) {
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

    /**
     * Returns a player-facing description with any formula placeholders substituted
     * using values from the stored DB object.  Override in concrete Reward types that
     * carry a formula (e.g. RangeReward substitutes the range amount).
     * The default falls back to {@link PanelPublicItem#getPublicDescription(User)}.
     */
    public String getPublicDescription(User user, RewardDB rewardDB) {
        return this.getPublicDescription(user);
    }

    public abstract AbPanel getAdminPanel(UpgradesAddon addon, GameModeAddon gamemode, User user,
                                          AbPanel parent, UpgradeTier tier, @Nullable RewardDB saved);

    public abstract void apply(UpgradesAddon addon, User user, Island island, RewardDB rewardDB, int currentLevel);

}
