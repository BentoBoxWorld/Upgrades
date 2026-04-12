package world.bentobox.upgrades.upgrades;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
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
import world.bentobox.upgrades.config.Settings;
import world.bentobox.upgrades.dataobjects.UpgradesData;

/**
 * Test for CommandUpgrade
 */
public class CommandUpgradeTest {

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
    private Player player;
    @Mock
    private World world;
    @Mock
    private Settings settings;

    private CommandUpgrade commandUpgrade;
    private UUID userId;
    private String islandId;

    @BeforeEach
    public void setUp() {
        MockBukkit.mock();

        MockitoAnnotations.openMocks(this);

        userId = UUID.randomUUID();
        islandId = UUID.randomUUID().toString();

        when(user.getUniqueId()).thenReturn(userId);
        when(user.getPlayer()).thenReturn(player);
        when(island.getUniqueId()).thenReturn(islandId);
        when(island.getGameMode()).thenReturn("aoneblock");
        when(island.getWorld()).thenReturn(world);
        
        when(addon.getUpgradesLevels(islandId)).thenReturn(upgradesData);
        when(addon.getUpgradesManager()).thenReturn(um);
        when(addon.getSettings()).thenReturn(settings);
        when(settings.getCommandName("coal-upgrade")).thenReturn("Coal Upgrade");

        commandUpgrade = new CommandUpgrade(addon, "coal-upgrade", Material.GRASS_BLOCK);
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    /**
     * Test that isShowed returns true when permission level is 0 (no permission required)
     */
    @Test
    public void testIsShowed_NoPermissionRequired() {
        // Set up the mock to return permission level 0
        when(upgradesData.getUpgradeLevel("command-coal-upgrade")).thenReturn(0);
        when(um.getCommandPermissionLevel("coal-upgrade", 0, world)).thenReturn(0);

        // Should show when no permission is required
        assertTrue(commandUpgrade.isShowed(user, island));
    }

    /**
     * Test that isShowed correctly uses this.getName() instead of this.cmdId
     * when getting the upgrade level from the island data.
     * 
     * This is a regression test for the bug where isShowed() was using this.cmdId
     * while updateUpgradeValue() and doUpgrade() were correctly using this.getName().
     */
    @Test
    public void testIsShowed_UsesCorrectUpgradeKey() {
        // The upgrade data is stored with the key "command-coal-upgrade" (this.getName())
        // not "coal-upgrade" (this.cmdId)
        when(upgradesData.getUpgradeLevel("command-coal-upgrade")).thenReturn(1);
        when(um.getCommandPermissionLevel("coal-upgrade", 1, world)).thenReturn(1);

        // Set up permissions for the player
        Set<PermissionAttachmentInfo> permissions = new HashSet<>();
        PermissionAttachmentInfo perm = createMockPermission("aoneblock.upgrades.command-coal-upgrade.1", true);
        permissions.add(perm);
        when(player.getEffectivePermissions()).thenReturn(permissions);

        // Should show when player has correct permission
        assertTrue(commandUpgrade.isShowed(user, island));
    }

    /**
     * Test that isShowed returns false when player doesn't have required permission
     */
    @Test
    public void testIsShowed_NoPermission() {
        when(upgradesData.getUpgradeLevel("command-coal-upgrade")).thenReturn(0);
        when(um.getCommandPermissionLevel("coal-upgrade", 0, world)).thenReturn(1);

        // Set up empty permissions
        Set<PermissionAttachmentInfo> permissions = new HashSet<>();
        when(player.getEffectivePermissions()).thenReturn(permissions);

        // Should not show when player lacks permission
        assertFalse(commandUpgrade.isShowed(user, island));
    }

    /**
     * Test that isShowed returns true when player has sufficient permission level
     */
    @Test
    public void testIsShowed_WithSufficientPermission() {
        when(upgradesData.getUpgradeLevel("command-coal-upgrade")).thenReturn(0);
        when(um.getCommandPermissionLevel("coal-upgrade", 0, world)).thenReturn(1);

        // Set up permissions - player has level 2, needs level 1
        Set<PermissionAttachmentInfo> permissions = new HashSet<>();
        PermissionAttachmentInfo perm = createMockPermission("aoneblock.upgrades.command-coal-upgrade.2", true);
        permissions.add(perm);
        when(player.getEffectivePermissions()).thenReturn(permissions);

        // Should show when player has higher permission level than required
        assertTrue(commandUpgrade.isShowed(user, island));
    }

    /**
     * Test that isShowed returns false when player has insufficient permission level
     */
    @Test
    public void testIsShowed_WithInsufficientPermission() {
        when(upgradesData.getUpgradeLevel("command-coal-upgrade")).thenReturn(0);
        when(um.getCommandPermissionLevel("coal-upgrade", 0, world)).thenReturn(2);

        // Set up permissions - player has level 1, needs level 2
        Set<PermissionAttachmentInfo> permissions = new HashSet<>();
        PermissionAttachmentInfo perm = createMockPermission("aoneblock.upgrades.command-coal-upgrade.1", true);
        permissions.add(perm);
        when(player.getEffectivePermissions()).thenReturn(permissions);

        // Should not show when player has lower permission level than required
        assertFalse(commandUpgrade.isShowed(user, island));
    }

    /**
     * Test that isShowed returns false for wildcard permissions
     */
    @Test
    public void testIsShowed_WildcardNotAllowed() {
        when(upgradesData.getUpgradeLevel("command-coal-upgrade")).thenReturn(0);
        when(um.getCommandPermissionLevel("coal-upgrade", 0, world)).thenReturn(1);

        // Set up wildcard permission
        Set<PermissionAttachmentInfo> permissions = new HashSet<>();
        PermissionAttachmentInfo perm = createMockPermission("aoneblock.upgrades.command-coal-upgrade.*", true);
        permissions.add(perm);
        when(player.getEffectivePermissions()).thenReturn(permissions);

        // Should not show when player has wildcard permission (not allowed)
        assertFalse(commandUpgrade.isShowed(user, island));
    }

    /**
     * Helper method to create a mock PermissionAttachmentInfo
     */
    private PermissionAttachmentInfo createMockPermission(String permission, boolean value) {
        PermissionAttachmentInfo info = org.mockito.Mockito.mock(PermissionAttachmentInfo.class);
        when(info.getPermission()).thenReturn(permission);
        when(info.getValue()).thenReturn(value);
        return info;
    }
}
