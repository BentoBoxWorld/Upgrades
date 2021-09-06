package world.bentobox.upgrades.ui.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.panels.PanelItem.ClickHandler;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.dataobjects.UpgradeTier;
import world.bentobox.upgrades.dataobjects.prices.Price;
import world.bentobox.upgrades.dataobjects.prices.PriceDB;
import world.bentobox.upgrades.ui.utils.AbPanel;

public final class EditTierPanel extends AbPanel {

    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";
    private static final String ICON = "icon";
    private static final String NB_LEVEL = "nblevel";
    private static final String ORDER = "order";
    private static final String PRICES = "prices";
    private static final String REWARDS = "rewards";

    private UpgradeTier tier;
    private List<UpgradeTier> tiers;
    private List<Integer> tiersLengths;

    public EditTierPanel(UpgradesAddon addon, GameModeAddon gamemode, User user, UpgradeTier tier,
                         List<UpgradeTier> tiers, AbPanel parent) {
        super(addon, gamemode, user, tier.getUniqueId(), parent);

        this.tier = tier;
        this.tiers = tiers;
        this.tiersLengths = this.computeTierLength(this.tiers);

        this.fillBorder(Material.BLACK_STAINED_GLASS_PANE);
        this.setButton();
    }

    private void setButton() {

        this.setItems(NAME,
                new PanelItemBuilder().name(this.tier.getName())
                        .description(this.getUser()
                                .getTranslation("upgrades.ui.edittierpanel.namedesc"))
                        .icon(Material.NAME_TAG)
                        .clickHandler(this.onSetName)
                        .build(),
                12);

        this.setItems(DESCRIPTION,
                new PanelItemBuilder().name(
                                this.getUser()
                                        .getTranslation("upgrades.ui.edittierpanel.description"))
                        .description(this.tier.getDescription())
                        .icon(Material.WRITTEN_BOOK)
                        .clickHandler(this.onSetDescription)
                        .build(),
                13);

        this.setItems(ICON,
                new PanelItemBuilder().name(
                                this.getUser()
                                        .getTranslation("upgrades.ui.edittierpanel.icon"))
                        .description(this.getUser()
                                .getTranslation("upgrades.ui.edittierpanel.icondesc"))
                        .icon(this.tier.getIcon())
                        .clickHandler(this.onSetIcon)
                        .build(),
                14);

        this.setItems(NB_LEVEL,
                new PanelItemBuilder().name(
                                this.getUser()
                                        .getTranslation("upgrades.ui.edittierpanel.nblevel"))
                        .description(
                                this.getUser()
                                        .getTranslation("upgrades.ui.edittierpanel.nbleveldesc",
                                                "[actual]",
                                                Integer.toString(
                                                        this.tier.getEndLevel() - this.tier.getStartLevel() + 1)))
                        .icon(Material.EXPERIENCE_BOTTLE)
                        .clickHandler(this.onSetNbLevel)
                        .build(),
                21);

        this.setItems(ORDER,
                new PanelItemBuilder().name(
                                this.getUser()
                                        .getTranslation("upgrades.ui.edittierpanel.order"))
                        .description(this.getUser()
                                .getTranslation("upgrades.ui.edittierpanel.orderdesc",
                                        "[actual]",
                                        Integer.toString(this.tiers.indexOf(this.tier) + 1), "[max]",
                                        Integer.toString(this.tiers.size())))
                        .icon(Material.OAK_SIGN)
                        .clickHandler(this.onSetOrder)
                        .build(),
                23);

        this.setItems(PRICES,
                new PanelItemBuilder().name(
                                this.getUser()
                                        .getTranslation("upgrades.ui.edittierpanel.prices"))
                        .icon(Material.GOLD_NUGGET)
                        .clickHandler(this.onSetPrices)
                        .build(),
                30);

        this.setItems(REWARDS,
                new PanelItemBuilder().name(
                                this.getUser()
                                        .getTranslation("upgrades.ui.edittierpanel.rewards"))
                        .icon(Material.DIAMOND_BLOCK)
                        .clickHandler(null)
                        .build(),
                32);

    }

    private final ClickHandler onSetName = (panel, client, click, slot) -> {
        this.getAddon()
                .getChatInput()
                .askOneInput(this.doSetName, input -> true, this.getUser()
                                .getTranslation("upgrades.chatinput.admin.question.getupgradetiername", "[name]",
                                        this.tier.getName()),
                        "You're not supposed to fail that...", this.getUser(), false);
        return true;
    };

    private final Consumer<String> doSetName = (name) -> {
        this.tier.setName(name);
        this.setButton();
        this.getBuild()
                .build();
    };

    private final ClickHandler onSetDescription = (panel, client, click, slot) -> {
        this.getAddon()
                .getChatInput()
                .askMultiLine(this.doSetDescription, input -> true,
                        this.getUser()
                                .getTranslation("upgrades.chatinput.admin.question.getupgradetierdesc",
                                        "[end]",
                                        this.getAddon()
                                                .getSettings()
                                                .getChatInputEscape()),
                        "You're not supposed to fail that...", this.getUser());
        return true;
    };

    private final Consumer<List<String>> doSetDescription = (description) -> {
        if (description == null)
            return;
        this.tier.setDescription(description);
        this.setButton();
        this.getBuild()
                .build();
    };

    private final ClickHandler onSetIcon = (panel, client, click, slot) -> {
        ItemStack inHand = Objects.requireNonNull(client.getInventory())
                .getItemInMainHand();

        if (BADICON.contains(inHand.getType())) {
            client.sendMessage("upgrades.error.noiteminhand");
            client.closeInventory();
            return true;
        }
        this.tier.setIcon(new ItemStack(inHand.getType()));
        this.setButton();
        this.getBuild()
                .build();
        return true;
    };

    private final ClickHandler onSetNbLevel = (panel, client, click, slot) -> {
        int index = this.tiers.indexOf(this.tier);
        int actual = this.tiersLengths.get(index);

        if (click.isLeftClick() && actual > 1) {
            this.tiersLengths.set(index, actual - 1);
        } else if (click.isRightClick()) {
            this.tiersLengths.set(index, actual + 1);
        } else
            return true;

        this.updateTierLevel(this.tiers, this.tiersLengths);
        this.setButton();
        this.getBuild()
                .build();
        return true;
    };

    private final ClickHandler onSetOrder = (panel, client, click, slot) -> {
        int index = this.tiers.indexOf(this.tier);
        int length = this.tiersLengths.get(index);
        int newIndex = index;

        if (click.isLeftClick() && index > 0)
            newIndex--;
        else if (click.isRightClick() && index < this.tiers.size() - 1)
            newIndex++;
        else
            return true;

        this.tiersLengths.set(index, this.tiersLengths.get(newIndex));
        this.tiers.set(index, this.tiers.get(newIndex));
        this.tiersLengths.set(newIndex, length);
        this.tiers.set(newIndex, this.tier);
        this.updateTierLevel(this.tiers, this.tiersLengths);
        this.setButton();
        this.getBuild()
                .build();
        return true;
    };

    private final ClickHandler onSetPrices = (panel, client, click, slot) -> {
        String title = client.getTranslation("upgrades.ui.listadmintierpricepanel.title");
        String create = client.getTranslation("upgrades.ui.listadmintierpricepanel.create");
        String leftDesc = client.getTranslation("upgrades.ui.listadmintierpricepanel.leftdesc");
        String rightDesc = client.getTranslation("upgrades.ui.listadmintierpricepanel.rightdesc");
        List<Price> prices = this.tier.getPrices()
                .stream()
                .map((PriceDB p) ->
                        this.getAddon()
                                .getUpgradesManager()
                                .searchPrice(p.getPriceType())
                )
                .collect(Collectors.toList());

        new AdminList<>(this.getAddon(), this.getGamemode(), client,
                title, this, prices,
                this.onSelectPrice, this.onDeletePrice, this.onCreatePrice,
                create, leftDesc, rightDesc).getBuild()
                .build();
        return true;
    };

    private final ClickHandler onSetRewards = (panel, client, click, slot) -> {
        return true;
    };

    private final Consumer<Price> onSelectPrice = (price) -> {
        PriceDB selected = this.tier.getPrices()
                .stream()
                .filter((p) -> p.getPriceType() == price.getClass())
                .findFirst()
                .orElse(null);

        price.getAdminPanel(this.getAddon(), this.getGamemode(), this.getUser(), this, this.tier,
                        selected)
                .getBuild()
                .build();
    };

    private final Consumer<Price> onDeletePrice = (price) ->
            new YesNoPanel(this.getAddon(), this.getGamemode(), this.getUser(),
                    this.getUser()
                            .getTranslation("upgrades.ui.titles.delete"), this, delete -> {
                if (delete) {
                    List<PriceDB> prices = this.tier.getPrices();
                    prices = prices.stream()
                            .filter(p -> p.getPriceType() == price.getClass())
                            .collect(
                                    Collectors.toList());
                    this.tier.setPrices(prices);
                }
                this.getBuild()
                        .build();
            });

    private final Runnable onCreatePrice = () -> {
        String title = this.getUser()
                .getTranslation("upgrades.ui.listadminpricepanel.title");
        List<Price> prices = this.getAddon()
                .getUpgradesManager()
                .getPrices();

        new AdminList<>(this.getAddon(), this.getGamemode(), this.getUser(), title, this, prices,
                this.onSelectNewPrice, null, null, null, null, null)
                .getBuild()
                .build();
    };

    private final Consumer<Price> onSelectNewPrice = (price) ->
            price.getAdminPanel(this.getAddon(), this.getGamemode(), this.getUser(), this, this.tier,
                            null)
                    .getBuild()
                    .build();

    private List<Integer> computeTierLength(List<UpgradeTier> tiers) {
        List<Integer> lengths = new ArrayList<>();

        tiers.forEach(tier ->
                lengths.add(tier.getEndLevel() - tier.getStartLevel() + 1)
        );

        return lengths;
    }

    private void updateTierLevel(List<UpgradeTier> tiers, List<Integer> lengths) {
        int level = 0;

        for (int i = 0; i < tiers.size(); i++) {
            tiers.get(i)
                    .setStartLevel(level);
            level += lengths.get(i);
            tiers.get(i)
                    .setEndLevel(level - 1);
        }
    }

}
