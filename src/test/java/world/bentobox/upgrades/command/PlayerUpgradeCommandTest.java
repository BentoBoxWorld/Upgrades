package world.bentobox.upgrades.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.Mockito;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.CommandsManager;
import world.bentobox.bentobox.managers.IslandWorldManager;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.bentobox.managers.RanksManager;
import world.bentobox.bentobox.util.Util;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.UpgradesManager;
import world.bentobox.upgrades.WhiteBox;
import world.bentobox.upgrades.config.Settings;

/**
 * @author tastybento
 */
public class PlayerUpgradeCommandTest {

    @Mock
    private UpgradesAddon addon;
    @Mock
    private World world;
    @Mock
    private CompositeCommand ic;
    @Mock
    private User user;

    private UUID uuid;
    private Settings settings;

    @Mock
    private Location location;
    @Mock
    private IslandsManager im;
    @Mock
    private Island island;
    @Mock
    private Player p;
    private PlayerUpgradeCommand puc;
    @Mock
    private RanksManager rm;
    @Mock
    private IslandWorldManager iwm;
    @Mock
    private UpgradesManager um;
    private MockedStatic<Bukkit> bukkitMock;
    private MockedStatic<Util> utilMock;


    /**
     * @throws java.lang.Exception
     */
    @SuppressWarnings("deprecation")
    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        MockBukkit.mock();
        // Config
        YamlConfiguration config = new YamlConfiguration();
        File configFile = new File("src/main/resources/config.yml");
        assertTrue(configFile.exists());
        config.load(configFile);

        when(addon.getConfig()).thenReturn(config);

        // Set up plugin
        BentoBox plugin = mock(BentoBox.class);
        WhiteBox.setInternalState(BentoBox.class, "instance", plugin);
        User.setPlugin(plugin);

        when(addon.getPlugin()).thenReturn(plugin);

        // RanksManager
        when(rm.getRank(anyInt())).thenReturn(RanksManager.MEMBER_RANK_REF);
        when(plugin.getRanksManager()).thenReturn(rm);

        // Command manager
        CommandsManager cm = mock(CommandsManager.class);
        when(plugin.getCommandsManager()).thenReturn(cm);
        // Addon
        when(ic.getAddon()).thenReturn(addon);
        when(ic.getPermissionPrefix()).thenReturn("bskyblock.");
        when(ic.getLabel()).thenReturn("island");
        when(ic.getTopLabel()).thenReturn("island");
        when(ic.getWorld()).thenReturn(world);
        when(ic.getTopLabel()).thenReturn("bsb");

        // World
        when(world.toString()).thenReturn("world");
        // Player
        // Sometimes use Mockito.withSettings().verboseLogging()
        when(user.isOp()).thenReturn(false);
        uuid = UUID.randomUUID();
        when(user.getUniqueId()).thenReturn(uuid);
        when(user.getPlayer()).thenReturn(p);
        when(user.getName()).thenReturn("tastybento");
        when(user.getPermissionValue(anyString(), anyInt())).thenReturn(-1);
        when(user.isPlayer()).thenReturn(true);
        when(user.getLocation()).thenReturn(location);
        when(user.getTranslation(anyString())).thenReturn("translation");

        // Util
        utilMock = Mockito.mockStatic(Util.class);
        utilMock.when(() -> Util.getWorld(any())).thenReturn(world);

        // Island Manager
        when(plugin.getIslands()).thenReturn(im);
        Optional<Island> opIsland = Optional.of(island);
        when(im.getIslandAt(any())).thenReturn(opIsland);

        // Settings
        settings = new Settings(addon);
        when(addon.getSettings()).thenReturn(settings);

        // Island
        when(location.toVector()).thenReturn(new Vector(0, 60, 0));

        // IWM
        when(iwm.getFriendlyName(world)).thenReturn("BSkyBlock");
        when(plugin.getIWM()).thenReturn(iwm);

        // Upgrades manager
        when(um.getIslandLevel(island)).thenReturn(20);
        when(addon.getUpgradesManager()).thenReturn(um);
        // Bukkit
        bukkitMock = Mockito.mockStatic(Bukkit.class, Mockito.RETURNS_MOCKS);


        puc = new PlayerUpgradeCommand(addon, ic);
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterEach
    public void tearDown() throws Exception {
        MockBukkit.unmock();
        User.clearUsers();
        bukkitMock.closeOnDemand();
        utilMock.closeOnDemand();
    }

    /**
     * Test method for {@link world.bentobox.upgrades.command.PlayerUpgradeCommand#PlayerUpgradeCommand(world.bentobox.upgrades.UpgradesAddon, world.bentobox.bentobox.api.commands.CompositeCommand)}.
     */
    @Test
    public void testPlayerUpgradeCommand() {
        assertEquals("upgrade", puc.getLabel());
    }

    /**
     * Test method for {@link world.bentobox.upgrades.command.PlayerUpgradeCommand#setup()}.
     */
    @Test
    public void testSetup() {
        assertEquals("", puc.getPermission());
        assertEquals("upgrades.commands.main.description", puc.getDescription());
        assertTrue(puc.isOnlyPlayer());
    }

    /**
     * Test method for {@link world.bentobox.upgrades.command.PlayerUpgradeCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteNoIsland() {
        assertFalse(puc.canExecute(user, "", List.of()));
        verify(user).sendMessage("general.errors.no-island");
    }

    /**
     * Test method for {@link world.bentobox.upgrades.command.PlayerUpgradeCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteNotOnIsland() {
        when(im.getIsland(world, user)).thenReturn(island);
        assertFalse(puc.canExecute(user, "", List.of()));
        verify(user).sendMessage("upgrades.error.notonisland");
    }

    /**
     * Test method for {@link world.bentobox.upgrades.command.PlayerUpgradeCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteInsufficientRank() {
        when(im.getIsland(world, user)).thenReturn(island);
        when(island.onIsland(location)).thenReturn(true);
        assertFalse(puc.canExecute(user, "", List.of()));
        verify(user).sendMessage("general.errors.insufficient-rank", TextVariables.RANK, "translation");
    }

    /**
     * Test method for {@link world.bentobox.upgrades.command.PlayerUpgradeCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteSuccess() {
        when(im.getIsland(world, user)).thenReturn(island);
        when(island.onIsland(location)).thenReturn(true);
        when(island.isAllowed(eq(user), any())).thenReturn(true);
        assertTrue(puc.canExecute(user, "", List.of()));
    }

    /**
     * Test method for {@link world.bentobox.upgrades.command.PlayerUpgradeCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteUserStringListOfStringNoIsland() {
        assertFalse(puc.execute(user, "", List.of()));
        verify(user).sendMessage("general.errors.no-island");

    }

    /**
     * Test method for {@link world.bentobox.upgrades.command.PlayerUpgradeCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteUserStringListOfStringShowHelp() {
        assertFalse(puc.execute(user, "", List.of("random")));
        verify(user).sendMessage("commands.help.header", "[label]", "BSkyBlock");

    }

    /**
     * Test method for {@link world.bentobox.upgrades.command.PlayerUpgradeCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteNotOnIsland() {
        when(im.getIsland(world, user)).thenReturn(island);
        assertFalse(puc.execute(user, "", List.of()));
        verify(user).sendMessage("upgrades.error.notonisland");
    }

    /**
     * Test method for {@link world.bentobox.upgrades.command.PlayerUpgradeCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteSuccess() {
        when(im.getIsland(world, user)).thenReturn(island);
        when(island.onIsland(location)).thenReturn(true);
        when(island.isAllowed(eq(user), any())).thenReturn(true);
        assertTrue(puc.execute(user, "", List.of()));
    }

}
