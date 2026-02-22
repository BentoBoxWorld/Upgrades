package world.bentobox.upgrades.api;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.bukkit.Material;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import net.milkbowl.vault.economy.EconomyResponse;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.hooks.VaultHook;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.UpgradesManager;
import world.bentobox.upgrades.dataobjects.UpgradesData;

/**
 * @author tastybento
 */
public class UpgradeTest {

    @Mock
    private UpgradesAddon addon;
    @Mock
    private User user;
    @Mock
    private Island island;
    @Mock
    private UpgradesData upgradesData;
    @Mock
    private UpgradesManager um;
    @Mock
    private VaultHook vh;

    private TestUpgrade testUpgrade;
    private UUID userId;
    private String islandId;

    @BeforeEach
    public void setUp() {
        MockBukkit.mock();

        MockitoAnnotations.openMocks(this);

        userId = UUID.randomUUID();
        islandId = UUID.randomUUID().toString();

        when(user.getUniqueId()).thenReturn(userId);
        when(island.getUniqueId()).thenReturn(islandId);
        when(addon.getAddonByName("upgrades")).thenReturn(java.util.Optional.of(addon));
        when(addon.getUpgradesLevels(islandId)).thenReturn(upgradesData);

        when(um.getIslandLevel(island)).thenReturn(20);
        when(addon.getUpgradesManager()).thenReturn(um);

        when(vh.has(any(), anyDouble())).thenReturn(true); // Player has money
        when(addon.getVaultHook()).thenReturn(vh);

        testUpgrade = new TestUpgrade(addon, "test_upgrade", "Test Upgrade", Material.DIAMOND);
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void testUpgradeInitialization() {
        assertNotNull(testUpgrade.getUpgradesAddon());
        assertEquals("test_upgrade", testUpgrade.getName());
        assertEquals("Test Upgrade", testUpgrade.getDisplayName());
        assertEquals(Material.DIAMOND, testUpgrade.getIcon());
    }

    @Test
    public void testCanUpgrade_WithSufficientResources() {
        UpgradeAPI.UpgradeValues upgradeValues = testUpgrade.new UpgradeValues(5, 100, 1);
        testUpgrade.setUpgradeValues(user, upgradeValues);

        when(addon.isLevelProvided()).thenReturn(true);
        when(addon.getUpgradesManager().getIslandLevel(island)).thenReturn(5);
        when(addon.isVaultProvided()).thenReturn(true);
        when(addon.getVaultHook().has(user, 100)).thenReturn(true);

        assertTrue(testUpgrade.canUpgrade(user, island));
    }

    @Test
    public void testCanUpgrade_WithInsufficientResources() {
        UpgradeAPI.UpgradeValues upgradeValues = testUpgrade.new UpgradeValues(10, 200, 1);
        testUpgrade.setUpgradeValues(user, upgradeValues);

        when(addon.isLevelProvided()).thenReturn(true);
        when(addon.getUpgradesManager().getIslandLevel(island)).thenReturn(5);
        when(addon.isVaultProvided()).thenReturn(true);
        when(addon.getVaultHook().has(user, 200)).thenReturn(false);

        assertFalse(testUpgrade.canUpgrade(user, island));
    }

    @Test
    public void testDoUpgrade_SuccessfulTransaction() {
        UpgradeAPI.UpgradeValues upgradeValues = testUpgrade.new UpgradeValues(5, 100, 1);
        testUpgrade.setUpgradeValues(user, upgradeValues);

        when(addon.isVaultProvided()).thenReturn(true);
        when(addon.getVaultHook().withdraw(user, 100))
                .thenReturn(new EconomyResponse(100, 0, EconomyResponse.ResponseType.SUCCESS, ""));

        testUpgrade.doUpgrade(user, island);
        verify(upgradesData).setUpgradeLevel("test_upgrade", 1);
    }

    @Test
    public void testDoUpgrade_FailedTransaction() {
        UpgradeAPI.UpgradeValues upgradeValues = testUpgrade.new UpgradeValues(5, 100, 1);
        testUpgrade.setUpgradeValues(user, upgradeValues);

        when(addon.isVaultProvided()).thenReturn(true);
        when(addon.getVaultHook().withdraw(user, 100))
                .thenReturn(new EconomyResponse(100, 0, EconomyResponse.ResponseType.FAILURE, "Error"));

        assertFalse(testUpgrade.doUpgrade(user, island));
    }

    @Test
    public void testGetAndSetDescription() {
        String description = "Upgrade description";
        testUpgrade.setOwnDescription(user, description);

        assertEquals(description, testUpgrade.getOwnDescription(user));
    }

    @Test
    public void testGetAndSetUpgradeValues() {
        UpgradeAPI.UpgradeValues upgradeValues = testUpgrade.new UpgradeValues(5, 100, 1);
        testUpgrade.setUpgradeValues(user, upgradeValues);

        assertEquals(upgradeValues, testUpgrade.getUpgradeValues(user));
    }

    private static class TestUpgrade extends UpgradeAPI {

        public TestUpgrade(UpgradesAddon addon, String name, String displayName, Material icon) {
            super(addon, name, displayName, icon);
        }

        @Override
        public void updateUpgradeValue(User user, Island island) {
            // Test implementation
        }

        @Override
        public boolean isShowed(User user, Island island) {
            return true;
        }
    }
}
