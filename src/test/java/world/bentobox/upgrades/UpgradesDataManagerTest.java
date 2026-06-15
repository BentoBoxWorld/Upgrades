package world.bentobox.upgrades;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.Settings;
import world.bentobox.bentobox.database.DatabaseSetup.DatabaseType;
import world.bentobox.upgrades.dataobjects.UpgradeData;
import world.bentobox.upgrades.dataobjects.UpgradeTier;

/**
 * Tests for {@link UpgradesDataManager}, focusing on the validation logic
 * that removes tiers referencing non-existent upgrade data.
 */
class UpgradesDataManagerTest {

    @Mock
    private UpgradesAddon addon;
    @Mock
    private BentoBox plugin;
    @Mock
    private Settings pluginSettings;

    private UpgradesDataManager dataManager;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        MockitoAnnotations.openMocks(this);

        // Set up the BentoBox singleton that Database construction requires
        WhiteBox.setInternalState(BentoBox.class, "instance", plugin);
        DatabaseType value = DatabaseType.JSON;
        when(plugin.getSettings()).thenReturn(pluginSettings);
        when(pluginSettings.getDatabaseType()).thenReturn(value);
        dataManager = new UpgradesDataManager(addon);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    /**
     * Regression test for the ConcurrentModificationException originally thrown
     * inside validateUpgradeTier() when removing an orphaned tier while iterating
     * the HashMap values. The fix uses entrySet().removeIf() which is safe.
     *
     * Given a tier that references an upgrade data key that does not exist in the
     * cache, calling validate should:
     *  1. Not throw any exception (ConcurrentModificationException in particular).
     *  2. Remove the orphaned tier from the cache.
     */
    @Test
    void validateUpgradeTier_orphanedTier_removedWithoutException() throws Exception {
        // Arrange: inject an orphaned tier (references "missing_upgrade") and no UpgradeData
        UpgradeTier orphan = new UpgradeTier();
        orphan.setUniqueId("test_orphan_t1");
        orphan.setUpgrade("missing_upgrade");
        orphan.setStartLevel(0);
        orphan.setEndLevel(5);

        Map<String, UpgradeTier> tierCache = getTierCache();
        tierCache.put(orphan.getUniqueId(), orphan);

        // Sanity-check the tier is in the cache before validation
        assertTrue(tierCache.containsKey("test_orphan_t1"));

        // Act + Assert: calling validate via reload() must not throw
        assertDoesNotThrow(dataManager::reload);

        // Assert: orphaned tier must have been removed
        assertFalse(getTierCache().containsKey("test_orphan_t1"),
                "Orphaned tier should be removed from the cache after validation");
    }

    /**
     * A tier whose parent upgrade data IS in the cache must be kept.
     */
    @Test
    void validateUpgradeTier_validTier_notRemoved() throws Exception {
        // Arrange: put a matching UpgradeData in the cache
        UpgradeData data = new UpgradeData();
        data.setUniqueId("valid_upgrade");
        data.setWorld("BSkyBlock");
        getDataCache().put(data.getUniqueId(), data);

        // And a tier that correctly references it
        UpgradeTier tier = new UpgradeTier();
        tier.setUniqueId("valid_upgrade_t1");
        tier.setUpgrade("valid_upgrade");
        tier.setStartLevel(0);
        tier.setEndLevel(5);
        getTierCache().put(tier.getUniqueId(), tier);

        // Act
        assertDoesNotThrow(dataManager::reload);

        // Assert: valid tier must remain
        assertTrue(getTierCache().containsKey("valid_upgrade_t1"),
                "Valid tier should remain in the cache after validation");
    }

    /**
     * Multiple orphaned tiers (the exact scenario from the reported issue) must
     * all be removed in a single validate pass without throwing.
     */
    @Test
    void validateUpgradeTier_multipleOrphanedTiers_allRemovedWithoutException() throws Exception {
        // Arrange: two orphaned tiers with no corresponding upgrade data
        for (int i = 1; i <= 2; i++) {
            UpgradeTier orphan = new UpgradeTier();
            orphan.setUniqueId("Boxed_example_orphan" + i + "_t1");
            orphan.setUpgrade("Boxed_example_orphan" + i);
            orphan.setStartLevel(0);
            orphan.setEndLevel(5);
            getTierCache().put(orphan.getUniqueId(), orphan);
        }

        // Act + Assert
        assertDoesNotThrow(dataManager::reload);

        // Assert: both orphaned tiers removed
        assertFalse(getTierCache().containsKey("Boxed_example_orphan1_t1"));
        assertFalse(getTierCache().containsKey("Boxed_example_orphan2_t1"));
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private Map<String, UpgradeTier> getTierCache() throws Exception {
        Field f = UpgradesDataManager.class.getDeclaredField("upgradeTierCache");
        f.setAccessible(true);
        return (Map<String, UpgradeTier>) f.get(dataManager);
    }

    @SuppressWarnings("unchecked")
    private Map<String, UpgradeData> getDataCache() throws Exception {
        Field f = UpgradesDataManager.class.getDeclaredField("upgradeDataCache");
        f.setAccessible(true);
        return (Map<String, UpgradeData>) f.get(dataManager);
    }
}
