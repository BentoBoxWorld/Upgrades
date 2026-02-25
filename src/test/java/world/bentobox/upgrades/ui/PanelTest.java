package world.bentobox.upgrades.ui;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.Mockito;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.UpgradesManager;
import world.bentobox.upgrades.WhiteBox;

/**
 * @author tastybento
 */
public class PanelTest {

    @Mock
    private UpgradesAddon addon;
    @Mock
    private Island island;
    @Mock
    private UpgradesManager um;
    @Mock
    private World world;
    @Mock
    private User user;
    @Mock
    private BentoBox plugin;

    @Mock
    private Location location;
    @Mock
    private IslandsManager im;
    @Mock
    private Player p;
    private Panel panel;
    private MockedStatic<Bukkit> mockBukkit;
    private File dataFolder;

    /**
     */
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        MockBukkit.mock();

        // Set up BentoBox singleton (needed for TemplatedPanel internal error logging)
        WhiteBox.setInternalState(BentoBox.class, "instance", plugin);

        // World
        when(world.toString()).thenReturn("world");
        // Player
        when(user.isOp()).thenReturn(false);
        UUID uuid = UUID.randomUUID();
        when(user.getUniqueId()).thenReturn(uuid);
        when(user.getPlayer()).thenReturn(p);
        when(user.getName()).thenReturn("tastybento");
        when(user.getPermissionValue(anyString(), anyInt())).thenReturn(-1);
        when(user.isPlayer()).thenReturn(true);
        when(user.getLocation()).thenReturn(location);
        when(user.getTranslation(anyString())).thenReturn("translation");

        when(um.getIslandLevel(island)).thenReturn(20);
        when(addon.getUpgradesManager()).thenReturn(um);
        when(addon.getAvailableUpgrades()).thenReturn(Collections.emptySet());
        when(island.getWorld()).thenReturn(world);

        // Set up data folder with panel template
        dataFolder = Files.createTempDirectory("upgrades-panel-test").toFile();
        File panelsDir = new File(dataFolder, "panels");
        panelsDir.mkdirs();
        Files.copy(Paths.get("src/main/resources/panels/upgrades_panel.yml"),
                new File(panelsDir, "upgrades_panel.yml").toPath());
        when(addon.getDataFolder()).thenReturn(dataFolder);

        // Bukkit
        mockBukkit = Mockito.mockStatic(Bukkit.class, Mockito.RETURNS_MOCKS);

        panel = new Panel(addon, island);
    }

    /**
     */
    @AfterEach
    public void tearDown() {
        User.clearUsers();
        WhiteBox.setInternalState(BentoBox.class, "instance", null);
        mockBukkit.closeOnDemand();
        MockBukkit.unmock();
        if (dataFolder != null) {
            Files.walk(dataFolder.toPath())
                    .sorted(Comparator.reverseOrder())
                    .map(java.nio.file.Path::toFile)
                    .forEach(File::delete);
        }
    }

    /**
     * Test method for {@link world.bentobox.upgrades.ui.Panel#Panel(world.bentobox.upgrades.UpgradesAddon, world.bentobox.bentobox.database.objects.Island)}.
     */
    @Test
    public void testPanel() {
        assertNotNull(panel);
    }

    /**
     * Test method for {@link world.bentobox.upgrades.ui.Panel#showPanel(world.bentobox.bentobox.api.user.User)}.
     */
    @Test
    public void testShowPanel() {
        panel.showPanel(user);
        verify(p).openInventory(any(Inventory.class));
    }

}
