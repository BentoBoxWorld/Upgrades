package world.bentobox.upgrades.dataobjects.prices;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.UpgradesManager;

/**
 * Unit tests for the concrete {@link Price} implementations.  Covers
 * construction, name/description getters, the DB-aware description
 * overloads and the {@code canPay} / {@code pay} branches.
 */
class PricesTest {

    @Mock private UpgradesAddon addon;
    @Mock private UpgradesManager upgradesManager;
    @Mock private User user;
    @Mock private Island island;
    @Mock private PlayerInventory inventory;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        MockitoAnnotations.openMocks(this);
        // Stub every getTranslation invocation to echo back the key, regardless of arity.
        // User.getTranslation(String, String...) uses varargs; the single any(String[].class)
        // argument matcher matches any number of varargs parameters.
        when(user.getTranslation(anyString())).thenReturn("x");
        when(user.getTranslation(anyString(), any(String[].class))).thenReturn("x");
        when(user.getUniqueId()).thenReturn(UUID.randomUUID());
        when(island.getUniqueId()).thenReturn(UUID.randomUUID().toString());
        when(island.getMemberSet()).thenReturn(com.google.common.collect.ImmutableSet.of());
        when(addon.getUpgradesManager()).thenReturn(upgradesManager);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    // ─── Constants defined on the abstract Price class ───────────────────

    @Test
    void priceConstants_matchExpectedPlaceholders() {
        assertEquals("[level]", Price.LEVEL_VAR);
        assertEquals("[islandLevel]", Price.ISLAND_LEVEL_VAR);
        assertEquals("[numberPlayer]", Price.NUMBER_PLAYER_VAR);
    }

    // ─── IslandLevelPrice ────────────────────────────────────────────────

    @Test
    void islandLevelPrice_metadataAndNames() {
        IslandLevelPrice p = new IslandLevelPrice();
        assertEquals("island_level_price", p.getName());
        assertEquals(Material.EXPERIENCE_BOTTLE, p.getIcon());
        assertNotNull(p.getPublicName(user));
        assertNotNull(p.getAdminName(user));
        assertNotNull(p.getPublicDescription(user));
        assertNotNull(p.getAdminDescription(user));
    }

    @Test
    void islandLevelPrice_publicDescriptionWithDb_usesStoredEquation() {
        IslandLevelPrice p = new IslandLevelPrice();
        IslandLevelPriceDB db = new IslandLevelPriceDB();
        db.setLevelNeededEquation("42");
        assertNotNull(p.getPublicDescription(user, db));
    }

    @Test
    void islandLevelPrice_canPay_trueWhenIslandLevelMeetsEquation() {
        IslandLevelPrice p = new IslandLevelPrice();
        IslandLevelPriceDB db = new IslandLevelPriceDB();
        db.setLevelNeededEquation("10");
        when(upgradesManager.getIslandLevel(island)).thenReturn(15);

        assertTrue(p.canPay(addon, user, island, db, 1));
    }

    @Test
    void islandLevelPrice_canPay_falseWhenIslandLevelBelowEquation() {
        IslandLevelPrice p = new IslandLevelPrice();
        IslandLevelPriceDB db = new IslandLevelPriceDB();
        db.setLevelNeededEquation("100");
        when(upgradesManager.getIslandLevel(island)).thenReturn(5);

        assertFalse(p.canPay(addon, user, island, db, 1));
    }

    @Test
    void islandLevelPrice_pay_isNoOp() {
        IslandLevelPrice p = new IslandLevelPrice();
        assertDoesNotThrow(() -> p.pay(addon, user, island, new IslandLevelPriceDB(), 1));
    }

    @Test
    void islandLevelPriceDB_validityChecks() {
        IslandLevelPriceDB db = new IslandLevelPriceDB();
        assertFalse(db.isValid() && db.getLevelNeededEquation().isEmpty());
        db.setLevelNeededEquation("5");
        assertTrue(db.isValid());
        assertEquals(IslandLevelPrice.class, db.getPriceType());
    }

    // ─── MoneyPrice ──────────────────────────────────────────────────────

    @Test
    void moneyPrice_metadataAndNames() {
        MoneyPrice p = new MoneyPrice();
        assertEquals("money_price", p.getName());
        assertEquals(Material.GOLD_INGOT, p.getIcon());
        assertNotNull(p.getPublicName(user));
        assertNotNull(p.getAdminName(user));
        assertNotNull(p.getPublicDescription(user));
        assertNotNull(p.getAdminDescription(user));
    }

    @Test
    void moneyPrice_publicDescriptionWithDb() {
        MoneyPrice p = new MoneyPrice();
        MoneyPriceDB db = new MoneyPriceDB();
        db.setAmountEquation("500");
        assertNotNull(p.getPublicDescription(user, db));
    }

    @Test
    void moneyPrice_canPay_returnsTrueWhenVaultNotProvided() {
        MoneyPrice p = new MoneyPrice();
        when(addon.isVaultProvided()).thenReturn(false);
        assertTrue(p.canPay(addon, user, island, new MoneyPriceDB(), 1));
    }

    @Test
    void moneyPrice_pay_noOpWhenVaultNotProvided() {
        MoneyPrice p = new MoneyPrice();
        when(addon.isVaultProvided()).thenReturn(false);
        assertDoesNotThrow(() -> p.pay(addon, user, island, new MoneyPriceDB(), 1));
    }

    @Test
    void moneyPriceDB_validityChecks() {
        MoneyPriceDB db = new MoneyPriceDB();
        db.setAmountEquation("100");
        assertTrue(db.isValid());
        assertEquals(MoneyPrice.class, db.getPriceType());
    }

    // ─── PermissionPrice ─────────────────────────────────────────────────

    @Test
    void permissionPrice_metadataAndNames() {
        PermissionPrice p = new PermissionPrice();
        assertEquals("permission_price", p.getName());
        assertEquals(Material.TRIPWIRE_HOOK, p.getIcon());
        assertNotNull(p.getPublicName(user));
        assertNotNull(p.getAdminName(user));
        assertNotNull(p.getPublicDescription(user));
        assertNotNull(p.getAdminDescription(user));
    }

    @Test
    void permissionPrice_publicDescriptionWithDb() {
        PermissionPrice p = new PermissionPrice();
        PermissionPriceDB db = new PermissionPriceDB();
        db.setPermission("upgrades.special");
        assertNotNull(p.getPublicDescription(user, db));
    }

    @Test
    void permissionPrice_canPay_delegatesToUserHasPermission() {
        PermissionPrice p = new PermissionPrice();
        PermissionPriceDB db = new PermissionPriceDB();
        db.setPermission("upgrades.special");
        when(user.hasPermission("upgrades.special")).thenReturn(true);
        assertTrue(p.canPay(addon, user, island, db, 1));

        when(user.hasPermission("upgrades.special")).thenReturn(false);
        assertFalse(p.canPay(addon, user, island, db, 1));
    }

    @Test
    void permissionPrice_pay_isNoOp() {
        PermissionPrice p = new PermissionPrice();
        assertDoesNotThrow(() -> p.pay(addon, user, island, new PermissionPriceDB(), 1));
    }

    @Test
    void permissionPriceDB_validityChecks() {
        PermissionPriceDB db = new PermissionPriceDB();
        assertFalse(db.isValid());
        db.setPermission("upgrades.special");
        assertTrue(db.isValid());
        assertEquals(PermissionPrice.class, db.getPriceType());
    }

    // ─── ItemPrice ───────────────────────────────────────────────────────

    @Test
    void itemPrice_metadataAndNames() {
        ItemPrice p = new ItemPrice();
        assertEquals("item_price", p.getName());
        assertEquals(Material.CHEST, p.getIcon());
        assertNotNull(p.getPublicName(user));
        assertNotNull(p.getAdminName(user));
        assertNotNull(p.getPublicDescription(user));
        assertNotNull(p.getAdminDescription(user));
    }

    @Test
    void itemPrice_publicDescriptionWithDb() {
        ItemPrice p = new ItemPrice();
        ItemPriceDB db = new ItemPriceDB();
        db.setMaterial("DIAMOND");
        db.setAmount(3);
        assertNotNull(p.getPublicDescription(user, db));
    }

    @Test
    void itemPrice_canPay_trueWhenInventoryContainsEnough() {
        ItemPrice p = new ItemPrice();
        ItemPriceDB db = new ItemPriceDB();
        db.setMaterial("DIAMOND");
        db.setAmount(3);

        when(user.getInventory()).thenReturn(inventory);
        when(inventory.containsAtLeast(any(ItemStack.class), eq(3))).thenReturn(true);

        assertTrue(p.canPay(addon, user, island, db, 1));
    }

    @Test
    void itemPrice_canPay_falseWhenMaterialInvalid() {
        ItemPrice p = new ItemPrice();
        ItemPriceDB db = new ItemPriceDB();
        db.setMaterial("NOT_A_REAL_MATERIAL");
        db.setAmount(1);

        assertFalse(p.canPay(addon, user, island, db, 1));
        verify(addon).logWarning(anyString());
    }

    @Test
    void itemPriceDB_validityChecks() {
        ItemPriceDB db = new ItemPriceDB();
        assertFalse(db.isValid());
        db.setMaterial("DIAMOND");
        db.setAmount(5);
        assertTrue(db.isValid());
        assertEquals(ItemPrice.class, db.getPriceType());
        assertEquals(5, db.getAmount());
        assertEquals("DIAMOND", db.getMaterial());
    }
}
