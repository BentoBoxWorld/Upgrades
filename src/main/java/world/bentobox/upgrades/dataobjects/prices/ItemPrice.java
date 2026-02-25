package world.bentobox.upgrades.dataobjects.prices;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
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

public class ItemPrice extends Price {

    public ItemPrice() {
        super("item_price", Material.CHEST);
    }

    @Override
    public String getPublicName(User user) {
        return user.getTranslation("upgrades.prices.item.name");
    }

    @Override
    public String getAdminName(User user) {
        return user.getTranslation("upgrades.prices.item.name");
    }

    @Override
    public String getPublicDescription(User user) {
        return user.getTranslation("upgrades.prices.item.description", "[amount]", "?", "[item]", "?");
    }

    @Override
    public String getPublicDescription(User user, PriceDB priceDB) {
        ItemPriceDB db = (ItemPriceDB) priceDB;
        return user.getTranslation("upgrades.prices.item.description",
                "[amount]", Integer.toString(db.getAmount()),
                "[item]", db.getMaterial());
    }

    @Override
    public String getAdminDescription(User user) {
        return user.getTranslation("upgrades.prices.item.admindescription");
    }

    @Override
    public boolean canPay(UpgradesAddon addon, User user, Island island, PriceDB priceDB) {
        ItemPriceDB db = (ItemPriceDB) priceDB;
        try {
            Material mat = Material.valueOf(db.getMaterial().toUpperCase());
            return user.getInventory().containsAtLeast(new ItemStack(mat), db.getAmount());
        } catch (IllegalArgumentException e) {
            addon.logWarning("ItemPrice: invalid material '" + db.getMaterial() + "'");
            return false;
        }
    }

    @Override
    public void pay(UpgradesAddon addon, User user, Island island, PriceDB priceDB) {
        ItemPriceDB db = (ItemPriceDB) priceDB;
        try {
            Material mat = Material.valueOf(db.getMaterial().toUpperCase());
            user.getInventory().removeItem(new ItemStack(mat, db.getAmount()));
        } catch (IllegalArgumentException e) {
            addon.logWarning("ItemPrice: invalid material '" + db.getMaterial() + "'");
        }
    }

    @Override
    public AbPanel getAdminPanel(UpgradesAddon addon, GameModeAddon gamemode, User user, AbPanel parent,
                                 UpgradeTier tier, @Nullable PriceDB saved) {
        ItemPriceDB dbObject;

        if (saved == null) {
            dbObject = new ItemPriceDB();
            List<PriceDB> prices = tier.getPrices();
            prices.add(dbObject);
            tier.setPrices(prices);
        } else if (saved instanceof ItemPriceDB) {
            dbObject = (ItemPriceDB) saved;
        } else {
            throw new InvalidParameterException("DB object in ItemPrice which is not an ItemPriceDB");
        }

        return new ItemPricePanel(addon, gamemode, user, parent, tier, dbObject);
    }

    private final class ItemPricePanel extends AbPanel {

        private static final String VALID = "valid";
        private static final String INVALID = "invalid";
        private static final String SET_ITEM = "setitem";
        private static final String SET_AMOUNT = "setamount";

        private final UpgradeTier tier;
        private final ItemPriceDB saved;

        public ItemPricePanel(UpgradesAddon addon, GameModeAddon gamemode, User user,
                              AbPanel parent, @NonNull UpgradeTier tier,
                              @NonNull ItemPriceDB saved) {
            super(addon, gamemode, user, user.getTranslation("upgrades.prices.item.paneltitle"), parent);
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

            Material iconMat = Material.CHEST;
            if (!this.saved.getMaterial().isEmpty()) {
                try {
                    iconMat = Material.valueOf(this.saved.getMaterial().toUpperCase());
                } catch (IllegalArgumentException ignored) {}
            }

            this.setItems(SET_ITEM, new PanelItemBuilder()
                    .name(this.saved.getMaterial().isEmpty() ? "Click to set item (hold in hand)" : this.saved.getMaterial())
                    .icon(iconMat)
                    .clickHandler(this.onSetItem())
                    .build(), 20);

            this.setItems(SET_AMOUNT, new PanelItemBuilder()
                    .name(Integer.toString(this.saved.getAmount()))
                    .icon(Material.PAPER)
                    .clickHandler(this.onSetAmount())
                    .build(), 24);
        }

        private PanelItem.ClickHandler onSetItem() {
            return (panel, client, click, slot) -> {
                ItemStack inHand = client.getInventory().getItemInMainHand();
                if (inHand.getType() == Material.AIR) {
                    client.sendMessage(client.getTranslation("upgrades.error.noiteminhand"));
                } else {
                    this.saved.setMaterial(inHand.getType().name());
                    this.createInterface();
                    this.getBuild().build();
                }
                return true;
            };
        }

        private PanelItem.ClickHandler onSetAmount() {
            return (panel, client, click, slot) -> {
                this.getAddon().getChatInput().askOneInput(
                        input -> {
                            try {
                                int amt = Integer.parseInt(input);
                                if (amt > 0) {
                                    this.saved.setAmount(amt);
                                    this.createInterface();
                                    this.getBuild().build();
                                }
                            } catch (NumberFormatException ignored) {}
                        },
                        input -> {
                            try {
                                return Integer.parseInt(input) > 0;
                            } catch (NumberFormatException e) {
                                return false;
                            }
                        },
                        "Enter the amount (current: " + this.saved.getAmount() + ")", "", client, false);
                return true;
            };
        }
    }
}
