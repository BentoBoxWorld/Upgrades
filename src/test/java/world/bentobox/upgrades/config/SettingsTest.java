package world.bentobox.upgrades.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.config.Settings.Expression;

/**
 * @author tastybento
 */
class SettingsTest {

    @Mock
    private UpgradesAddon addon;
    private Settings settings;


    /**
     * @throws java.lang.Exception
     */
    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        MockBukkit.mock();
        // Config
        YamlConfiguration config = new YamlConfiguration();
        File configFile = new File("src/main/resources/config.yml");
        assertTrue(configFile.exists());
        config.load(configFile);

        when(addon.getConfig()).thenReturn(config);

        settings = new Settings(addon);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#Settings(world.bentobox.upgrades.UpgradesAddon)}.
     */
    @Test
    void testSettings() {
        assertNotNull(settings);
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getDisabledGameModes()}.
     */
    @Test
    void testGetDisabledGameModes() {
        assertTrue(settings.getDisabledGameModes().isEmpty());
    }


    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getDefaultBlockLimitsUpgradeTierMap()}.
     */
    @Test
    void testGetDefaultBlockLimitsUpgradeTierMap() {
        assertFalse(settings.getDefaultBlockLimitsUpgradeTierMap().isEmpty());

    }

    /**
     * The limits sections are the only part of config.yml still read at runtime: the
     * perm-check listeners use these key sets to decide which Limits permission checks
     * to suppress. Pin the shipped keys so a config edit cannot silently break that.
     */
    @Test
    void testShippedConfigDeclaresLimitsKeysUsedByPermCheckListeners() {
        assertTrue(settings.getDefaultBlockLimitsUpgradeTierMap().containsKey(Material.HOPPER));
        assertTrue(settings.getDefaultEntityLimitsUpgradeTierMap().containsKey(EntityType.CHICKEN));
        assertTrue(settings.getDefaultEntityGroupLimitsUpgradeTierMap().containsKey("group1"));
    }

    /**
     * Entity limits used to require a matching entry in the removed entity-icon section.
     * CHICKEN must still parse now that no icons are configured.
     */
    @Test
    void testEntityLimitsParseWithoutIconSection() {
        assertFalse(settings.getDefaultEntityLimitsUpgradeTierMap().isEmpty());
        assertTrue(settings.getDefaultEntityLimitsUpgradeTierMap().containsKey(EntityType.CHICKEN));
    }

    @Test
    void testGameModeOverridesDeclareLimitsKeys() {
        assertTrue(settings.getAddonBlockLimitsUpgradeTierMap("BSkyBlock").containsKey(Material.HOPPER));
        assertTrue(settings.getAddonEntityLimitsUpgradeTierMap("BSkyBlock").containsKey(EntityType.CHICKEN));
        assertTrue(settings.getAddonEntityGroupLimitsUpgradeTierMap("BSkyBlock").containsKey("group1"));
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getAddonBlockLimitsUpgradeTierMap(java.lang.String)}.
     */
    @Test
    void testGetAddonBlockLimitsUpgradeTierMap() {
        assertTrue(settings.getAddonBlockLimitsUpgradeTierMap("").isEmpty());
    }


    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getDefaultEntityLimitsUpgradeTierMap()}.
     */
    @Test
    void testGetDefaultEntityLimitsUpgradeTierMap() {
        assertFalse(settings.getDefaultEntityLimitsUpgradeTierMap().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getDefaultEntityGroupLimitsUpgradeTierMap()}.
     */
    @Test
    void testGetDefaultEntityGroupLimitsUpgradeTierMap() {
        assertFalse(settings.getDefaultEntityGroupLimitsUpgradeTierMap().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getAddonEntityLimitsUpgradeTierMap(java.lang.String)}.
     */
    @Test
    void testGetAddonEntityLimitsUpgradeTierMap() {
        assertTrue(settings.getAddonEntityLimitsUpgradeTierMap("").isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getAddonEntityGroupLimitsUpgradeTierMap(java.lang.String)}.
     */
    @Test
    void testGetAddonEntityGroupLimitsUpgradeTierMap() {
        assertTrue(settings.getAddonEntityGroupLimitsUpgradeTierMap("").isEmpty());
    }


    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#parse(java.lang.String, java.util.Map)}.
     */
    @Test
    void testParse() {
        Expression expression = Settings.parse("40*200", Map.of());
        assertEquals(8000D, expression.eval(), 0.1D);
    }

}
