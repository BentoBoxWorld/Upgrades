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
public class SettingsTest {

    @Mock
    private UpgradesAddon addon;
    private Settings settings;


    /**
     * @throws java.lang.Exception
     */
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

        settings = new Settings(addon);
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#Settings(world.bentobox.upgrades.UpgradesAddon)}.
     */
    @Test
    public void testSettings() {
        assertNotNull(settings);
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getDisabledGameModes()}.
     */
    @Test
    public void testGetDisabledGameModes() {
        assertTrue(settings.getDisabledGameModes().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getHasRangeUpgrade()}.
     */
    @Test
    public void testGetHasRangeUpgrade() {
        assertTrue(settings.getHasRangeUpgrade());
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getMaxRangeUpgrade(java.lang.String)}.
     */
    @Test
    public void testGetMaxRangeUpgrade() {
        assertEquals(10, settings.getMaxRangeUpgrade(""));
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getDefaultRangeUpgradeTierMap()}.
     */
    @Test
    public void testGetDefaultRangeUpgradeTierMap() {
        assertFalse(settings.getDefaultRangeUpgradeTierMap().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getAddonRangeUpgradeTierMap(java.lang.String)}.
     */
    @Test
    public void testGetAddonRangeUpgradeTierMap() {
        assertTrue(settings.getAddonRangeUpgradeTierMap("").isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getMaxBlockLimitsUpgrade(org.bukkit.Material, java.lang.String)}.
     */
    @Test
    public void testGetMaxBlockLimitsUpgrade() {
        assertEquals(0, settings.getMaxBlockLimitsUpgrade(Material.STONE, ""));
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getDefaultBlockLimitsUpgradeTierMap()}.
     */
    @Test
    public void testGetDefaultBlockLimitsUpgradeTierMap() {
        assertFalse(settings.getDefaultBlockLimitsUpgradeTierMap().isEmpty());

    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getAddonBlockLimitsUpgradeTierMap(java.lang.String)}.
     */
    @Test
    public void testGetAddonBlockLimitsUpgradeTierMap() {
        assertTrue(settings.getAddonBlockLimitsUpgradeTierMap("").isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getMaterialsLimitsUpgrade()}.
     */
    @Test
    public void testGetMaterialsLimitsUpgrade() {
        assertFalse(settings.getMaterialsLimitsUpgrade().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getEntityIcon(org.bukkit.entity.EntityType)}.
     */
    @Test
    public void testGetEntityIcon() {
        assertNull(settings.getEntityIcon(EntityType.CAT));
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getEntityGroupIcon(java.lang.String)}.
     */
    @Test
    public void testGetEntityGroupIcon() {
        assertNull(settings.getEntityGroupIcon(""));
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getMaxEntityLimitsUpgrade(org.bukkit.entity.EntityType, java.lang.String)}.
     */
    @Test
    public void testGetMaxEntityLimitsUpgrade() {
        assertEquals(0, settings.getMaxEntityLimitsUpgrade(EntityType.HOPPER_MINECART, ""));
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getMaxEntityGroupLimitsUpgrade(java.lang.String, java.lang.String)}.
     */
    @Test
    public void testGetMaxEntityGroupLimitsUpgrade() {
        assertEquals(0, settings.getMaxEntityGroupLimitsUpgrade("", ""));
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getDefaultEntityLimitsUpgradeTierMap()}.
     */
    @Test
    public void testGetDefaultEntityLimitsUpgradeTierMap() {
        assertFalse(settings.getDefaultEntityLimitsUpgradeTierMap().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getDefaultEntityGroupLimitsUpgradeTierMap()}.
     */
    @Test
    public void testGetDefaultEntityGroupLimitsUpgradeTierMap() {
        assertFalse(settings.getDefaultEntityGroupLimitsUpgradeTierMap().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getAddonEntityLimitsUpgradeTierMap(java.lang.String)}.
     */
    @Test
    public void testGetAddonEntityLimitsUpgradeTierMap() {
        assertTrue(settings.getAddonEntityLimitsUpgradeTierMap("").isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getAddonEntityGroupLimitsUpgradeTierMap(java.lang.String)}.
     */
    @Test
    public void testGetAddonEntityGroupLimitsUpgradeTierMap() {
        assertTrue(settings.getAddonEntityGroupLimitsUpgradeTierMap("").isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getEntityLimitsUpgrade()}.
     */
    @Test
    public void testGetEntityLimitsUpgrade() {
        assertFalse(settings.getEntityLimitsUpgrade().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getEntityGroupLimitsUpgrade()}.
     */
    @Test
    public void testGetEntityGroupLimitsUpgrade() {
        assertFalse(settings.getEntityGroupLimitsUpgrade().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getMaxCommandUpgrade(java.lang.String, java.lang.String)}.
     */
    @Test
    public void testGetMaxCommandUpgrade() {
        assertEquals(0, settings.getMaxCommandUpgrade("", ""));
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getDefaultCommandUpgradeTierMap()}.
     */
    @Test
    public void testGetDefaultCommandUpgradeTierMap() {
        assertFalse(settings.getDefaultCommandUpgradeTierMap().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getAddonCommandUpgradeTierMap(java.lang.String)}.
     */
    @Test
    public void testGetAddonCommandUpgradeTierMap() {
        assertTrue(settings.getAddonCommandUpgradeTierMap("").isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getCommandUpgrade()}.
     */
    @Test
    public void testGetCommandUpgrade() {
        assertFalse(settings.getCommandUpgrade().isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getCommandIcon(java.lang.String)}.
     */
    @Test
    public void testGetCommandIcon() {
        assertNull(settings.getCommandIcon(""));
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#getCommandName(java.lang.String)}.
     */
    @Test
    public void testGetCommandName() {
        assertNull(settings.getCommandName(""));
    }

    /**
     * Test method for {@link world.bentobox.upgrades.config.Settings#parse(java.lang.String, java.util.Map)}.
     */
    @Test
    public void testParse() {
        Expression expression = Settings.parse("40*200", Map.of());
        assertEquals(8000D, expression.eval(), 0.1D);
    }

}
