package world.bentobox.upgrades.upgrades;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.World;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.addons.AddonDescription;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.IslandWorldManager;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.UpgradesDataManager;
import world.bentobox.upgrades.UpgradesManager;
import world.bentobox.upgrades.dataobjects.UpgradeData;
import world.bentobox.upgrades.dataobjects.UpgradeTier;
import world.bentobox.upgrades.dataobjects.UpgradesData;
import world.bentobox.upgrades.dataobjects.prices.Price;
import world.bentobox.upgrades.dataobjects.prices.PriceDB;
import world.bentobox.upgrades.dataobjects.rewards.Reward;
import world.bentobox.upgrades.dataobjects.rewards.RewardDB;

/**
 * Tests for {@link DatabaseUpgrade} — the bridge between admin-GUI-configured
 * upgrades and the player shop.
 *
 * Key invariants under test:
 *  - Fresh island level is 0, so a tier at startLevel=0..endLevel=0 is
 *    immediately purchasable (regression test for the putIfAbsent(1) bug).
 *  - After one purchase the level becomes 1; a 0-0 tier no longer matches,
 *    so the upgrade shows as "Max level".
 *  - isShowed() gates on active flag, non-empty tier list, and game-mode match.
 *  - canUpgrade() delegates to each Price.canPay().
 *  - doUpgrade() calls Price.pay(), Reward.apply(), and increments the level.
 */
public class DatabaseUpgradeTest {

    @Mock private UpgradesAddon addon;
    @Mock private BentoBox plugin;
    @Mock private IslandWorldManager iwm;
    @Mock private UpgradesDataManager upgradesDataManager;
    @Mock private UpgradesManager upgradesManager;
    @Mock private User user;
    @Mock private Island island;
    @Mock private World world;
    @Mock private GameModeAddon gameModeAddon;
    @Mock private AddonDescription gameModeDesc;

    private UpgradeData upgradeData;
    /** A single-purchase tier: startLevel=0, endLevel=0 */
    private UpgradeTier tier;
    private UpgradesData upgradesData;
    private DatabaseUpgrade databaseUpgrade;
    private String islandId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        MockitoAnnotations.openMocks(this);

        userId = UUID.randomUUID();
        islandId = UUID.randomUUID().toString();

        // Upgrade configured for BSkyBlock
        upgradeData = new UpgradeData();
        upgradeData.setUniqueId("BSkyBlock_diamond");
        upgradeData.setWorld("BSkyBlock");
        upgradeData.setName("Diamond Upgrade");
        upgradeData.setActive(true);

        // Single-purchase tier: one buy available (level 0 → 1)
        tier = new UpgradeTier();
        tier.setUniqueId("BSkyBlock_diamond_t1");
        tier.setUpgrade("BSkyBlock_diamond");
        tier.setName("Tier 1");
        tier.setStartLevel(0);
        tier.setEndLevel(0);

        // Fresh island data — must default to level 0
        upgradesData = new UpgradesData(islandId);

        when(user.getUniqueId()).thenReturn(userId);
        when(user.getTranslation(any(String.class)))
                .thenAnswer(inv -> inv.getArgument(0, String.class));
        when(island.getUniqueId()).thenReturn(islandId);
        when(island.getWorld()).thenReturn(world);

        // addon → plugin → IWM → game-mode lookup
        when(addon.getPlugin()).thenReturn(plugin);
        when(plugin.getIWM()).thenReturn(iwm);
        when(iwm.getAddon(world)).thenReturn(Optional.of(gameModeAddon));
        when(gameModeAddon.getDescription()).thenReturn(gameModeDesc);
        when(gameModeDesc.getName()).thenReturn("BSkyBlock");

        when(addon.getUpgradeDataManager()).thenReturn(upgradesDataManager);
        when(addon.getUpgradesManager()).thenReturn(upgradesManager);
        when(addon.getUpgradesLevels(islandId)).thenReturn(upgradesData);
        when(upgradesDataManager.getUpgradeTierByUpgradeData(upgradeData))
                .thenReturn(Collections.singletonList(tier));

        databaseUpgrade = new DatabaseUpgrade(addon, upgradeData);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    // ─── isShowed() ───────────────────────────────────────────────────────

    @Test
    void isShowed_inactiveUpgrade_returnsFalse() {
        upgradeData.setActive(false);
        assertFalse(databaseUpgrade.isShowed(user, island));
    }

    @Test
    void isShowed_noTiersConfigured_returnsFalse() {
        // An upgrade with no tiers would appear permanently maxed; hide it.
        when(upgradesDataManager.getUpgradeTierByUpgradeData(upgradeData))
                .thenReturn(Collections.emptyList());
        assertFalse(databaseUpgrade.isShowed(user, island));
    }

    @Test
    void isShowed_wrongGameMode_returnsFalse() {
        when(gameModeDesc.getName()).thenReturn("AcidIsland");
        assertFalse(databaseUpgrade.isShowed(user, island));
    }

    @Test
    void isShowed_gameModeWorldNotFound_returnsFalse() {
        when(iwm.getAddon(world)).thenReturn(Optional.empty());
        assertFalse(databaseUpgrade.isShowed(user, island));
    }

    @Test
    void isShowed_allConditionsMet_returnsTrue() {
        assertTrue(databaseUpgrade.isShowed(user, island));
    }

    // ─── Level storage (UpgradesData) ─────────────────────────────────────
    //
    // These tests confirm the fix: getUpgradeLevel() must start at 0, not 1.
    // A tier with startLevel=0..endLevel=0 must be reachable on a fresh island.

    @Test
    void freshIsland_levelStartsAtZero_notOne() {
        assertEquals(0, upgradesData.getUpgradeLevel("BSkyBlock_diamond"),
                "Fresh island must start at level 0 so tier 0-0 is immediately purchasable");
    }

    @Test
    void setAndGetUpgradeLevel_roundTrip() {
        upgradesData.setUpgradeLevel("BSkyBlock_diamond", 3);
        assertEquals(3, upgradesData.getUpgradeLevel("BSkyBlock_diamond"));
    }

    // ─── updateUpgradeValue() / tier resolution ────────────────────────────

    @Test
    void updateUpgradeValue_freshIsland_tierFound_upgradeIsVisible() {
        // Level 0: tier 0-0 must match (0 <= 0 <= 0) so upgrade appears in shop.
        databaseUpgrade.updateUpgradeValue(user, island);
        assertTrue(databaseUpgrade.isShowed(user, island));
    }

    @Test
    void updateUpgradeValue_afterOnePurchase_bothNullMeansMaxLevel() {
        // Level 1: tier 0-0 no longer matches (0 <= 1 <= 0 is FALSE).
        // Both ownDescription and upgradeValues are set to null → Panel shows "Max level".
        upgradesData.setUpgradeLevel(upgradeData.getUniqueId(), 1);
        databaseUpgrade.updateUpgradeValue(user, island);

        assertNull(databaseUpgrade.getUpgradeValues(user),
                "upgradeValues must be null when no tier covers the current level");
        assertNull(databaseUpgrade.getOwnDescription(user),
                "ownDescription must be null when no tier covers the current level");
    }

    @Test
    void updateUpgradeValue_multiLevelTier_midPointIsStillBuyable() {
        // Tier 0-2: three purchases; level 1 is still within range.
        tier.setEndLevel(2);
        upgradesData.setUpgradeLevel(upgradeData.getUniqueId(), 1);
        databaseUpgrade.updateUpgradeValue(user, island);

        assertTrue(databaseUpgrade.isShowed(user, island));
    }

    @Test
    void updateUpgradeValue_multiLevelTier_pastEndIsMaxed() {
        // Tier 0-2 but level is 3: past the end, should appear maxed.
        tier.setEndLevel(2);
        upgradesData.setUpgradeLevel(upgradeData.getUniqueId(), 3);
        databaseUpgrade.updateUpgradeValue(user, island);

        assertNull(databaseUpgrade.getUpgradeValues(user));
        assertNull(databaseUpgrade.getOwnDescription(user));
    }

    // ─── canUpgrade() ────────────────────────────────────────────────────

    @Test
    void canUpgrade_noTierFound_returnsFalse() {
        // Level 1 on a 0-0 tier: already maxed, cannot upgrade.
        upgradesData.setUpgradeLevel(upgradeData.getUniqueId(), 1);
        assertFalse(databaseUpgrade.canUpgrade(user, island));
    }

    @Test
    void canUpgrade_noPricesOnTier_returnsTrue() {
        // No prices configured → free upgrade → always purchasable.
        assertTrue(databaseUpgrade.canUpgrade(user, island));
    }

    @Test
    void canUpgrade_priceCheckPasses_returnsTrue() {
        PriceDB priceDB = mock(PriceDB.class);
        Price price = mock(Price.class);
        when(upgradesManager.searchPrice(any())).thenReturn(price);
        when(price.canPay(addon, user, island, priceDB)).thenReturn(true);
        tier.setPrices(List.of(priceDB));

        assertTrue(databaseUpgrade.canUpgrade(user, island));
    }

    @Test
    void canUpgrade_priceCheckFails_returnsFalse() {
        PriceDB priceDB = mock(PriceDB.class);
        Price price = mock(Price.class);
        when(upgradesManager.searchPrice(any())).thenReturn(price);
        when(price.canPay(addon, user, island, priceDB)).thenReturn(false);
        tier.setPrices(List.of(priceDB));

        assertFalse(databaseUpgrade.canUpgrade(user, island));
    }

    @Test
    void canUpgrade_nullPriceFromManager_skippedAndReturnsTrue() {
        // searchPrice returns null (unregistered type) → price is skipped, not a failure.
        PriceDB priceDB = mock(PriceDB.class);
        when(upgradesManager.searchPrice(any())).thenReturn(null);
        tier.setPrices(List.of(priceDB));

        assertTrue(databaseUpgrade.canUpgrade(user, island));
    }

    // ─── doUpgrade() ─────────────────────────────────────────────────────

    @Test
    void doUpgrade_noTierFound_returnsFalse() {
        upgradesData.setUpgradeLevel(upgradeData.getUniqueId(), 1); // already maxed
        assertFalse(databaseUpgrade.doUpgrade(user, island));
    }

    @Test
    void doUpgrade_incrementsLevelByOne() {
        assertEquals(0, upgradesData.getUpgradeLevel(upgradeData.getUniqueId()));

        boolean result = databaseUpgrade.doUpgrade(user, island);

        assertTrue(result);
        assertEquals(1, upgradesData.getUpgradeLevel(upgradeData.getUniqueId()));
    }

    @Test
    void doUpgrade_callsPricePayAndRewardApply() {
        PriceDB priceDB = mock(PriceDB.class);
        Price price = mock(Price.class);
        when(upgradesManager.searchPrice(any())).thenReturn(price);
        tier.setPrices(List.of(priceDB));

        RewardDB rewardDB = mock(RewardDB.class);
        Reward reward = mock(Reward.class);
        when(upgradesManager.searchReward(any())).thenReturn(reward);
        tier.setRewards(List.of(rewardDB));

        databaseUpgrade.doUpgrade(user, island);

        verify(price).pay(addon, user, island, priceDB);
        verify(reward).apply(addon, user, island, rewardDB);
    }

    @Test
    void doUpgrade_afterMaxing_secondCallReturnsFalse() {
        // First purchase: level 0 → 1, tier 0-0 consumed.
        assertTrue(databaseUpgrade.doUpgrade(user, island));
        // Second call: level 1, no tier covers it.
        assertFalse(databaseUpgrade.doUpgrade(user, island));
    }

    @Test
    void doUpgrade_nullRewardFromManager_skippedGracefully() {
        RewardDB rewardDB = mock(RewardDB.class);
        when(upgradesManager.searchReward(any())).thenReturn(null);
        tier.setRewards(List.of(rewardDB));

        assertTrue(databaseUpgrade.doUpgrade(user, island));
        assertEquals(1, upgradesData.getUpgradeLevel(upgradeData.getUniqueId()));
    }

    // ─── PanelClick gating regression ─────────────────────────────────────
    //
    // DB upgrades never populate upgradeValues (it stays null by design).
    // PanelClick used to check only getUpgradeValues(user) == null, which
    // silently blocked every DB upgrade click.  The fix: treat
    //   (upgradeValues == null && ownDescription == null) as "maxed".
    // So ownDescription must be non-null whenever a tier is found.

    @Test
    void updateUpgradeValue_withPricesAndRewards_ownDescriptionIsNonNull() {
        PriceDB priceDB = mock(PriceDB.class);
        Price price = mock(Price.class);
        when(upgradesManager.searchPrice(any())).thenReturn(price);
        when(price.getPublicDescription(eq(user), eq(priceDB))).thenReturn("Costs 100 money");
        tier.setPrices(List.of(priceDB));

        databaseUpgrade.updateUpgradeValue(user, island);

        assertNotNull(databaseUpgrade.getOwnDescription(user),
                "ownDescription must not be null when a tier is found — PanelClick uses null ownDescription to block clicks");
    }

    @Test
    void updateUpgradeValue_noPricesOrRewards_ownDescriptionFallsBackToTierName() {
        // A tier with no prices or rewards: description should be tier name so that
        // PanelClick can distinguish "available free upgrade" from "maxed".
        databaseUpgrade.updateUpgradeValue(user, island);

        assertEquals(tier.getName(), databaseUpgrade.getOwnDescription(user),
                "ownDescription must fall back to tier name when there are no price/reward descriptions");
    }

    @Test
    void updateUpgradeValue_maxedState_ownDescriptionIsNull() {
        // Level 1 with tier 0-0: maxed → ownDescription must be null so PanelClick blocks the click.
        upgradesData.setUpgradeLevel(upgradeData.getUniqueId(), 1);
        databaseUpgrade.updateUpgradeValue(user, island);

        assertNull(databaseUpgrade.getOwnDescription(user),
                "ownDescription must be null when maxed so PanelClick correctly blocks the click");
    }

    // ─── Description substitution ─────────────────────────────────────────

    @Test
    void updateUpgradeValue_moneyPrice_substitutesAmountInDescription() {
        PriceDB priceDB = mock(PriceDB.class);
        Price price = mock(Price.class);
        when(upgradesManager.searchPrice(any())).thenReturn(price);
        // Simulate MoneyPrice.getPublicDescription(user, priceDB) returning "Costs 500 money"
        when(price.getPublicDescription(user, priceDB)).thenReturn("Costs 500 money");
        tier.setPrices(List.of(priceDB));

        databaseUpgrade.updateUpgradeValue(user, island);

        assertTrue(databaseUpgrade.getOwnDescription(user).contains("Costs 500 money"),
                "Description must use the DB-aware overload that substitutes [amount]");
    }
}
