package world.bentobox.upgrades.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.panels.builders.TemplatedPanelBuilder;
import world.bentobox.bentobox.api.panels.reader.ItemTemplateRecord;
import world.bentobox.bentobox.api.panels.TemplatedPanel;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.api.UpgradeAPI;

/**
 * User interface for Upgrades
 */
public class Panel {

    private static final String UPGRADE = "UPGRADE";
    private final UpgradesAddon addon;
    private final Island island;
    private int page;

    /**
     * Start to create a panel for this island
     * @param addon Upgrades
     * @param island island
     */
    public Panel(UpgradesAddon addon, Island island) {
        super();
        this.addon = addon;
        this.island = island;
        this.page = 0;
    }

    /**
     * Show the GUI to the user
     * @param user user
     */
    public void showPanel(User user) {
        // Every registered upgrade refreshes its cached values on each panel open,
        // including the ones that end up hidden.
        Set<UpgradeAPI> upgrades = addon.getAvailableUpgrades();
        upgrades.forEach(u -> u.updateUpgradeValue(user, island));

        List<UpgradeAPI> visible = upgrades.stream()
                .filter(u -> u.isShowed(user, island))
                .toList();

        new TemplatedPanelBuilder()
                .user(user)
                .world(island.getWorld())
                .template("upgrades_panel", new File(addon.getDataFolder(), "panels"))
                .registerTypeBuilder(UPGRADE, (t, s) -> createUpgradeButton(s, user, visible))
                .registerTypeBuilder("NEXT", (t, s) -> createNextButton(t, s, visible))
                .registerTypeBuilder("PREVIOUS", (t, s) -> createPreviousButton(t))
                .build();
    }

    private PanelItem createUpgradeButton(TemplatedPanel.ItemSlot slot,
            User user, List<UpgradeAPI> visible) {
        int upgradesPerPage = slot.amount("UPGRADE");
        int index = slot.slot() + page * upgradesPerPage;

        if (index >= visible.size()) {
            return null;
        }

        UpgradeAPI upgrade = visible.get(index);
        int islandLevel = addon.getUpgradesManager().getIslandLevel(island);

        String ownDescription = upgrade.getOwnDescription(user);
        List<String> fullDescription = new ArrayList<>();

        if (ownDescription != null) {
            fullDescription.add(ownDescription);
            if (upgrade.getUpgradeValues(user) != null) {
                fullDescription.addAll(getDescription(user, upgrade, islandLevel));
            }
        } else {
            fullDescription.addAll(getDescription(user, upgrade, islandLevel));
        }

        return new PanelItemBuilder()
                .name(upgrade.getDisplayName())
                .icon(upgrade.getIcon())
                .description(fullDescription)
                .clickHandler(new PanelClick(upgrade, island))
                .build();
    }

    private PanelItem createNextButton(ItemTemplateRecord template, TemplatedPanel.ItemSlot slot,
            List<UpgradeAPI> visible) {
        if ((page + 1) * slot.amount("UPGRADE") >= visible.size()) {
            return null;
        }

        return new PanelItemBuilder()
                .icon(template.icon())
                .name(template.title())
                .description(template.description())
                .clickHandler((panel, clicker, clickType, slotNumber) -> {
                    page++;
                    showPanel(clicker);
                    return true;
                })
                .build();
    }

    private PanelItem createPreviousButton(ItemTemplateRecord template) {
        if (page == 0) {
            return null;
        }

        return new PanelItemBuilder()
                .icon(template.icon())
                .name(template.title())
                .description(template.description())
                .clickHandler((panel, clicker, clickType, slotNumber) -> {
                    page--;
                    showPanel(clicker);
                    return true;
                })
                .build();
    }

    private List<String> getDescription(User user, UpgradeAPI upgrade, int islandLevel) {
        List<String> descrip = new ArrayList<>();

        if (upgrade.getUpgradeValues(user) == null)
            descrip.add(user.getTranslation("upgrades.ui.upgradepanel.maxlevel"));
        else {
            if (this.addon.isLevelProvided()) {
                descrip.add((upgrade.getUpgradeValues(user).getIslandLevel() <= islandLevel ? "§a" : "§c")
                        + user.getTranslation("upgrades.ui.upgradepanel.islandneed", "[islandlevel]",
                                Integer.toString(upgrade.getUpgradeValues(user).getIslandLevel())));
            }

            if (this.addon.isVaultProvided()) {
                boolean hasMoney = this.addon.getVaultHook().has(user, upgrade.getUpgradeValues(user).getMoneyCost());
                descrip.add((hasMoney ? "§a" : "§c") + user.getTranslation("upgrades.ui.upgradepanel.moneycost",
                        "[cost]", Integer.toString(upgrade.getUpgradeValues(user).getMoneyCost())));
            }

            if (this.addon.isLevelProvided() && upgrade.getUpgradeValues(user).getIslandLevel() > islandLevel) {
                descrip.add("§8" + user.getTranslation("upgrades.ui.upgradepanel.tryreloadlevel"));
            }
        }

        return descrip;
    }

}
