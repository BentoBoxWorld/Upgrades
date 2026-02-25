package world.bentobox.upgrades.dataobjects;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link UpgradesData} — per-island upgrade level storage.
 *
 * Critical invariant: getUpgradeLevel() must return 0 for a key that has
 * never been set.  If it returned 1 (the old broken default), a tier
 * with startLevel=0..endLevel=0 would never match because findNextTier()
 * would see currentLevel=0 and the check 0<=0<=0 is true, but with the
 * old default of 1, it would see currentLevel=1 and 0<=1<=0 is false —
 * causing every DB upgrade to appear permanently maxed on a fresh island.
 */
public class UpgradesDataTest {

    private static final String KEY = "BSkyBlock_diamond";
    private static final String ISLAND_ID = "island-uuid-1234";

    private UpgradesData upgradesData;

    @BeforeEach
    void setUp() {
        upgradesData = new UpgradesData(ISLAND_ID);
    }

    @Test
    void getUniqueId_returnsConstructorValue() {
        assertEquals(ISLAND_ID, upgradesData.getUniqueId());
    }

    @Test
    void getUpgradeLevel_freshKey_returnsZeroNotOne() {
        // This is the core regression test: the level must start at 0 so that
        // a tier configured as startLevel=0, endLevel=0 is immediately purchasable.
        assertEquals(0, upgradesData.getUpgradeLevel(KEY),
                "Fresh island upgrade level must be 0 — returning 1 would make all DB upgrades appear permanently maxed");
    }

    @Test
    void getUpgradeLevel_calledTwice_staysAtZero() {
        // putIfAbsent must not overwrite an existing 0 on the second call.
        assertEquals(0, upgradesData.getUpgradeLevel(KEY));
        assertEquals(0, upgradesData.getUpgradeLevel(KEY));
    }

    @Test
    void setAndGet_roundTrip() {
        upgradesData.setUpgradeLevel(KEY, 5);
        assertEquals(5, upgradesData.getUpgradeLevel(KEY));
    }

    @Test
    void setUpgradeLevel_toZero_returnsZero() {
        upgradesData.setUpgradeLevel(KEY, 3);
        upgradesData.setUpgradeLevel(KEY, 0);
        assertEquals(0, upgradesData.getUpgradeLevel(KEY));
    }

    @Test
    void differentUpgradesStoredIndependently() {
        upgradesData.setUpgradeLevel("upgrade_a", 2);
        upgradesData.setUpgradeLevel("upgrade_b", 7);
        assertEquals(2, upgradesData.getUpgradeLevel("upgrade_a"));
        assertEquals(7, upgradesData.getUpgradeLevel("upgrade_b"));
    }

    @Test
    void getUpgradeLevel_afterSetToZero_doesNotResetToDefault() {
        // Explicitly set to 0, then read again — must NOT re-insert the default.
        upgradesData.setUpgradeLevel(KEY, 0);
        assertEquals(0, upgradesData.getUpgradeLevel(KEY));
    }
}
