package world.bentobox.upgrades.ui.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.bukkit.Material;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.panels.PanelItem.ClickHandler;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.ui.PanelAdminItem;
import world.bentobox.upgrades.ui.utils.AbPanel;

public final class AdminList<Item extends PanelAdminItem> extends AbPanel {

    private static final String CREATE = "create";

    @NonNull
    private final List<Item> items;

    @Nullable
    private final Consumer<Item> onLeftClick;
    @Nullable
    private final Consumer<Item> onRightClick;
    @Nullable
    private Runnable createButton;

    @Nullable
    private final String createName;
    @Nullable
    private final String leftClickDesc;
    @Nullable
    private final String rightClickDesc;

    public AdminList(UpgradesAddon addon, GameModeAddon gamemode, User user, String title,
                     AbPanel parent,
                     @NonNull List<Item> items, @Nullable Consumer<Item> onLeftClick,
                     @Nullable Consumer<Item> onRightClick, @Nullable Runnable createButton,
                     @Nullable String createName, @Nullable String leftClickDesc,
                     @Nullable String rightClickDesc) {
        super(addon, gamemode, user, title, parent);

        this.items = items;

        this.onLeftClick = onLeftClick;
        this.onRightClick = onRightClick;
        this.createButton = createButton;

        this.createName = createName;
        this.leftClickDesc = leftClickDesc;
        this.rightClickDesc = rightClickDesc;

        this.createInterface();
    }

    private void createInterface() {
        this.fillBorder(Material.BLACK_STAINED_GLASS_PANE);

        if (this.createButton != null && this.createName != null) {
            this.setItems(CREATE, new PanelItemBuilder().name(this.createName)
                    .icon(Material.GREEN_STAINED_GLASS_PANE)
                    .clickHandler(this.onClickCreate)
                    .build(), 4);
        }

        IntStream.range(0, this.items.size())
                .forEach(idx -> {
                    Item item = this.items.get(idx);
                    int pos = ((idx / 7) * 9) + (idx % 7) + 10;
                    List<String> desc = new ArrayList<>();

                    if (this.leftClickDesc != null)
                        desc.add(this.leftClickDesc);

                    if (this.rightClickDesc != null)
                        desc.add(this.rightClickDesc);

                    desc.add(item.getAdminDescription(this.getUser()));

                    this.setItems(item.getName() + "-" + idx,
                            new PanelItemBuilder().name(item.getAdminName(this.getUser()))
                                    .description(desc)
                                    .icon(item.getIcon())
                                    .clickHandler(this.onClickItem(item))
                                    .build(), pos);
                });
    }

    private final ClickHandler onClickCreate = (panel, client, click, slot) -> {
        this.createButton.run();
        return true;
    };

    private ClickHandler onClickItem(Item item) {
        return (panel, client, click, slot) -> {
            if (click.isLeftClick() && this.onLeftClick != null) {
                this.onLeftClick.accept(item);
            } else if (click.isRightClick() && this.onRightClick != null) {
                this.onRightClick.accept(item);
            } else {
                return true;
            }

            return true;
        };
    }

}
