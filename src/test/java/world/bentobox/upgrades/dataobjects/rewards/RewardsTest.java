package world.bentobox.upgrades.dataobjects.rewards;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
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
 * Unit tests for the concrete {@link Reward} implementations. Covers
 * construction, name/description getters, the DB-aware description
 * overloads and the no-op branches of {@code apply}.
 */
class RewardsTest {

    @Mock private UpgradesAddon addon;
    @Mock private UpgradesManager upgradesManager;
    @Mock private User user;
    @Mock private Island island;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        MockitoAnnotations.openMocks(this);
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

    @Test
    void rewardConstants_matchExpectedPlaceholders() {
        assertEquals("[level]", Reward.LEVEL_VAR);
        assertEquals("[islandLevel]", Reward.ISLAND_LEVEL_VAR);
        assertEquals("[numberPlayer]", Reward.NUMBER_PLAYER_VAR);
    }

    // ─── RangeReward ─────────────────────────────────────────────────────

    @Test
    void rangeReward_metadataAndNames() {
        RangeReward r = new RangeReward();
        assertEquals("range_reward", r.getName());
        assertEquals(Material.OAK_FENCE, r.getIcon());
        assertNotNull(r.getAdminName(user));
        assertNotNull(r.getAdminDescription(user));
        assertNotNull(r.getPublicName(user));
        assertNotNull(r.getPublicDescription(user));
    }

    @Test
    void rangeReward_publicDescriptionWithDb() {
        RangeReward r = new RangeReward();
        RangeRewardDB db = new RangeRewardDB();
        db.setRangeUpgradeEquation("5");
        assertNotNull(r.getPublicDescription(user, db));
    }

    @Test
    void rangeRewardDB_validityChecks() {
        RangeRewardDB db = new RangeRewardDB();
        assertTrue(db.isValid());
        db.setRangeUpgradeEquation("");
        assertFalse(db.isValid());
        assertEquals(RangeReward.class, db.getRewardType());
    }

    // ─── CropGrowthReward ────────────────────────────────────────────────

    @Test
    void cropGrowthReward_metadataAndNames() {
        CropGrowthReward r = new CropGrowthReward();
        assertEquals("crop_growth_reward", r.getName());
        assertEquals(Material.WHEAT, r.getIcon());
        assertNotNull(r.getAdminName(user));
        assertNotNull(r.getAdminDescription(user));
        assertNotNull(r.getPublicName(user));
        assertNotNull(r.getPublicDescription(user));
    }

    @Test
    void cropGrowthReward_publicDescriptionWithDb() {
        CropGrowthReward r = new CropGrowthReward();
        CropGrowthRewardDB db = new CropGrowthRewardDB();
        db.setGrowthBonusEquation("0.5");
        assertNotNull(r.getPublicDescription(user, db));
    }

    @Test
    void cropGrowthReward_apply_isNoOp() {
        CropGrowthReward r = new CropGrowthReward();
        assertDoesNotThrow(() -> r.apply(addon, user, island, new CropGrowthRewardDB(), 1));
    }

    @Test
    void cropGrowthRewardDB_validityChecks() {
        CropGrowthRewardDB db = new CropGrowthRewardDB();
        assertTrue(db.isValid());
        db.setGrowthBonusEquation(null);
        assertFalse(db.isValid());
        assertEquals(CropGrowthReward.class, db.getRewardType());
    }

    // ─── SpawnerReward ───────────────────────────────────────────────────

    @Test
    void spawnerReward_metadataAndNames() {
        SpawnerReward r = new SpawnerReward();
        assertEquals("spawner_reward", r.getName());
        assertEquals(Material.SPAWNER, r.getIcon());
        assertNotNull(r.getAdminName(user));
        assertNotNull(r.getAdminDescription(user));
        assertNotNull(r.getPublicName(user));
        assertNotNull(r.getPublicDescription(user));
    }

    @Test
    void spawnerReward_publicDescriptionWithDb() {
        SpawnerReward r = new SpawnerReward();
        SpawnerRewardDB db = new SpawnerRewardDB();
        db.setSpawnBonusEquation("0.25");
        assertNotNull(r.getPublicDescription(user, db));
    }

    @Test
    void spawnerReward_apply_isNoOp() {
        SpawnerReward r = new SpawnerReward();
        assertDoesNotThrow(() -> r.apply(addon, user, island, new SpawnerRewardDB(), 1));
    }

    @Test
    void spawnerRewardDB_validityChecks() {
        SpawnerRewardDB db = new SpawnerRewardDB();
        assertTrue(db.isValid());
        db.setSpawnBonusEquation("");
        assertFalse(db.isValid());
        assertEquals(SpawnerReward.class, db.getRewardType());
    }

    // ─── LimitsReward ────────────────────────────────────────────────────

    @Test
    void limitsReward_metadataAndNames() {
        LimitsReward r = new LimitsReward();
        assertEquals("limits_reward", r.getName());
        assertEquals(Material.BARRIER, r.getIcon());
        assertNotNull(r.getAdminName(user));
        assertNotNull(r.getAdminDescription(user));
        assertNotNull(r.getPublicName(user));
        assertNotNull(r.getPublicDescription(user));
    }

    @Test
    void limitsReward_publicDescriptionWithDb() {
        LimitsReward r = new LimitsReward();
        LimitsRewardDB db = new LimitsRewardDB();
        db.setTarget("STONE");
        db.setAmountEquation("10");
        assertNotNull(r.getPublicDescription(user, db));
    }

    @Test
    void limitsReward_apply_bailsOutWhenLimitsNotProvided() {
        LimitsReward r = new LimitsReward();
        when(addon.isLimitsProvided()).thenReturn(false);
        assertDoesNotThrow(() -> r.apply(addon, user, island, new LimitsRewardDB(), 1));
        verify(addon).logWarning(anyString());
    }

    @Test
    void limitsRewardDB_validityChecks() {
        LimitsRewardDB db = new LimitsRewardDB();
        assertFalse(db.isValid());
        db.setTarget("STONE");
        db.setAmountEquation("5");
        assertTrue(db.isValid());
        assertEquals(LimitsReward.class, db.getRewardType());
    }

    // ─── CommandReward ───────────────────────────────────────────────────

    @Test
    void commandReward_metadataAndNames() {
        CommandReward r = new CommandReward();
        assertEquals("command_reward", r.getName());
        assertEquals(Material.COMMAND_BLOCK, r.getIcon());
        assertNotNull(r.getAdminName(user));
        assertNotNull(r.getAdminDescription(user));
        assertNotNull(r.getPublicName(user));
        assertNotNull(r.getPublicDescription(user));
    }

    @Test
    void commandRewardDB_validityChecks() {
        CommandRewardDB db = new CommandRewardDB();
        assertFalse(db.isValid());
        db.setCommands(List.of("say hi"));
        assertTrue(db.isValid());
        db.setConsole(false);
        assertFalse(db.isConsole());
        assertEquals(CommandReward.class, db.getRewardType());
    }
}
