package world.bentobox.upgrades;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import world.bentobox.upgrades.dataobjects.UpgradeData;
import world.bentobox.upgrades.dataobjects.UpgradeTier;
import world.bentobox.upgrades.dataobjects.prices.IslandLevelPriceDB;
import world.bentobox.upgrades.dataobjects.prices.ItemPriceDB;
import world.bentobox.upgrades.dataobjects.prices.MoneyPriceDB;
import world.bentobox.upgrades.dataobjects.prices.PermissionPriceDB;
import world.bentobox.upgrades.dataobjects.prices.PriceDB;
import world.bentobox.upgrades.dataobjects.rewards.CommandRewardDB;
import world.bentobox.upgrades.dataobjects.rewards.CropGrowthRewardDB;
import world.bentobox.upgrades.dataobjects.rewards.LimitsRewardDB;
import world.bentobox.upgrades.dataobjects.rewards.RangeRewardDB;
import world.bentobox.upgrades.dataobjects.rewards.RewardDB;
import world.bentobox.upgrades.dataobjects.rewards.SpawnerRewardDB;

/**
 * Verifies that DefaultUpgradeSeeder correctly persists all upgrade definitions
 * (UpgradeData and UpgradeTier) to the database on first installation, covering
 * every price type (money, island-level, item, permission) and every reward type
 * (range, limits, command, spawner, crop-growth).
 */
class DefaultUpgradeSeederTest {

    @TempDir
    File tempDir;

    @Mock
    private UpgradesAddon addon;
    @Mock
    private UpgradesDataManager dataManager;

    private DefaultUpgradeSeeder seeder;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        MockitoAnnotations.openMocks(this);

        when(addon.getUpgradeDataManager()).thenReturn(dataManager);
        when(addon.getDataFolder()).thenReturn(tempDir);
        when(addon.getHookedGameModes()).thenReturn(List.of("BSkyBlock"));

        // createUpgradeData returns a real object so the seeder can mutate it
        when(dataManager.createUpgradeData(anyString(), anyString(), any())).thenAnswer(inv -> {
            UpgradeData ud = new UpgradeData();
            ud.setUniqueId(inv.getArgument(0));
            ud.setWorld(inv.getArgument(1));
            return ud;
        });

        // createUpgradeTier returns a real object so the seeder can mutate it
        when(dataManager.createUpgradeTier(anyString(), any(UpgradeData.class),
                anyInt(), anyInt(), any())).thenAnswer(inv -> {
            UpgradeTier tier = new UpgradeTier();
            tier.setUniqueId(inv.getArgument(0));
            tier.setUpgrade(((UpgradeData) inv.getArgument(1)).getUniqueId());
            tier.setStartLevel(inv.getArgument(2));
            tier.setEndLevel(inv.getArgument(3));
            tier.setPrices(new ArrayList<>());
            tier.setRewards(new ArrayList<>());
            return tier;
        });

        // No pre-existing upgrades — seeding should proceed
        when(dataManager.getUpgradeDataByGameMode("BSkyBlock")).thenReturn(Collections.emptyList());

        seeder = new DefaultUpgradeSeeder(addon);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    // ─── saveUpgradeData called for every upgrade ─────────────────────────────

    @Test
    void seedIfEmpty_savesExactlyEightUpgradeData() {
        seeder.seedIfEmpty();

        ArgumentCaptor<UpgradeData> captor = ArgumentCaptor.forClass(UpgradeData.class);
        verify(dataManager, times(8)).saveUpgradeData(captor.capture());

        List<String> names = captor.getAllValues().stream()
                .map(UpgradeData::getName)
                .collect(Collectors.toList());
        assertTrue(names.contains("Border Expansion I"));
        assertTrue(names.contains("Border Expansion II"));
        assertTrue(names.contains("Hopper Limit"));
        assertTrue(names.contains("Cow Limit"));
        assertTrue(names.contains("Diamond Border"));
        assertTrue(names.contains("Donor Perk"));
        assertTrue(names.contains("Spawner Boost"));
        assertTrue(names.contains("Crop Growth Boost"));
    }

    @Test
    void seedIfEmpty_eachUpgradeDataBelongsToCorrectGameMode() {
        seeder.seedIfEmpty();

        ArgumentCaptor<UpgradeData> captor = ArgumentCaptor.forClass(UpgradeData.class);
        verify(dataManager, times(8)).saveUpgradeData(captor.capture());

        captor.getAllValues().forEach(ud ->
                assertEquals("BSkyBlock", ud.getWorld(),
                        "Expected world BSkyBlock for upgrade " + ud.getUniqueId()));
    }

    @Test
    void seedIfEmpty_upgradeDataHaveOrderSet() {
        seeder.seedIfEmpty();

        ArgumentCaptor<UpgradeData> captor = ArgumentCaptor.forClass(UpgradeData.class);
        verify(dataManager, times(8)).saveUpgradeData(captor.capture());

        captor.getAllValues().forEach(ud ->
                assertTrue(ud.getOrder() > 0,
                        "Expected positive order for upgrade " + ud.getUniqueId()));
    }

    // ─── saveUpgradeTier called for every tier ────────────────────────────────

    @Test
    void seedIfEmpty_savesExactlyEightUpgradeTiers() {
        seeder.seedIfEmpty();

        verify(dataManager, times(8)).saveUpgradeTier(any(UpgradeTier.class));
    }

    // ─── Money price (Border Expansion I, II, Hopper, Cow, Diamond, Spawner, Crop) ──

    @Test
    void seedIfEmpty_borderExpansionI_hasMoneyPriceAndRangeReward() {
        seeder.seedIfEmpty();

        UpgradeTier tier = capturedTierFor("BSkyBlock_example_range1_t1");
        assertNotNull(tier, "Tier for Border Expansion I must be saved");

        assertTrue(tier.getPrices().stream().anyMatch(p -> p instanceof MoneyPriceDB),
                "Border Expansion I must have a MoneyPrice");
        MoneyPriceDB money = (MoneyPriceDB) tier.getPrices().stream()
                .filter(p -> p instanceof MoneyPriceDB).findFirst().orElseThrow();
        assertEquals("500", money.getAmountEquation());

        assertTrue(tier.getRewards().stream().anyMatch(r -> r instanceof RangeRewardDB),
                "Border Expansion I must have a RangeReward");
        RangeRewardDB range = (RangeRewardDB) tier.getRewards().stream()
                .filter(r -> r instanceof RangeRewardDB).findFirst().orElseThrow();
        assertEquals("5", range.getRangeUpgradeEquation());
    }

    // ─── Island-level price (Border Expansion II) ─────────────────────────────

    @Test
    void seedIfEmpty_borderExpansionII_hasIslandLevelPriceAndMoneyPrice() {
        seeder.seedIfEmpty();

        UpgradeTier tier = capturedTierFor("BSkyBlock_example_range2_t1");
        assertNotNull(tier, "Tier for Border Expansion II must be saved");

        assertTrue(tier.getPrices().stream().anyMatch(p -> p instanceof IslandLevelPriceDB),
                "Border Expansion II must have an IslandLevelPrice");
        IslandLevelPriceDB levelPrice = (IslandLevelPriceDB) tier.getPrices().stream()
                .filter(p -> p instanceof IslandLevelPriceDB).findFirst().orElseThrow();
        assertEquals("100", levelPrice.getLevelNeededEquation());

        assertTrue(tier.getPrices().stream().anyMatch(p -> p instanceof MoneyPriceDB),
                "Border Expansion II must also have a MoneyPrice");
    }

    // ─── Item price (Diamond Border) ──────────────────────────────────────────

    @Test
    void seedIfEmpty_diamondBorder_hasItemPriceAndRangeReward() {
        seeder.seedIfEmpty();

        UpgradeTier tier = capturedTierFor("BSkyBlock_example_diamond_t1");
        assertNotNull(tier, "Tier for Diamond Border must be saved");

        assertTrue(tier.getPrices().stream().anyMatch(p -> p instanceof ItemPriceDB),
                "Diamond Border must have an ItemPrice");
        ItemPriceDB item = (ItemPriceDB) tier.getPrices().stream()
                .filter(p -> p instanceof ItemPriceDB).findFirst().orElseThrow();
        assertEquals("DIAMOND", item.getMaterial());
        assertEquals(10, item.getAmount());

        assertTrue(tier.getRewards().stream().anyMatch(r -> r instanceof RangeRewardDB),
                "Diamond Border must have a RangeReward");
    }

    // ─── Permission price (Donor Perk) ────────────────────────────────────────

    @Test
    void seedIfEmpty_donorPerk_hasPermissionPriceAndCommandReward() {
        seeder.seedIfEmpty();

        UpgradeTier tier = capturedTierFor("BSkyBlock_example_donor_t1");
        assertNotNull(tier, "Tier for Donor Perk must be saved");

        assertTrue(tier.getPrices().stream().anyMatch(p -> p instanceof PermissionPriceDB),
                "Donor Perk must have a PermissionPrice");
        PermissionPriceDB perm = (PermissionPriceDB) tier.getPrices().stream()
                .filter(p -> p instanceof PermissionPriceDB).findFirst().orElseThrow();
        assertEquals("upgrades.example.donor", perm.getPermission());

        assertTrue(tier.getRewards().stream().anyMatch(r -> r instanceof CommandRewardDB),
                "Donor Perk must have a CommandReward");
        CommandRewardDB cmd = (CommandRewardDB) tier.getRewards().stream()
                .filter(r -> r instanceof CommandRewardDB).findFirst().orElseThrow();
        assertFalse(cmd.getCommands().isEmpty(), "CommandReward must have at least one command");
        assertTrue(cmd.isConsole(), "Donor Perk command must be run as console");
    }

    // ─── Limits reward (Hopper = BLOCK, Cow = ENTITY) ─────────────────────────

    @Test
    void seedIfEmpty_hopperLimit_hasMoneyPriceAndBlockLimitsReward() {
        seeder.seedIfEmpty();

        UpgradeTier tier = capturedTierFor("BSkyBlock_example_hopper_t1");
        assertNotNull(tier, "Tier for Hopper Limit must be saved");

        assertTrue(tier.getRewards().stream().anyMatch(r -> r instanceof LimitsRewardDB),
                "Hopper Limit must have a LimitsReward");
        LimitsRewardDB limits = (LimitsRewardDB) tier.getRewards().stream()
                .filter(r -> r instanceof LimitsRewardDB).findFirst().orElseThrow();
        assertEquals("BLOCK", limits.getLimitType());
        assertEquals("HOPPER", limits.getTarget());
        assertEquals("2", limits.getAmountEquation());
    }

    @Test
    void seedIfEmpty_cowLimit_hasEntityLimitsReward() {
        seeder.seedIfEmpty();

        UpgradeTier tier = capturedTierFor("BSkyBlock_example_cow_t1");
        assertNotNull(tier, "Tier for Cow Limit must be saved");

        LimitsRewardDB limits = (LimitsRewardDB) tier.getRewards().stream()
                .filter(r -> r instanceof LimitsRewardDB).findFirst().orElse(null);
        assertNotNull(limits);
        assertEquals("ENTITY", limits.getLimitType());
        assertEquals("COW", limits.getTarget());
    }

    // ─── Spawner reward ───────────────────────────────────────────────────────

    @Test
    void seedIfEmpty_spawnerBoost_hasSpawnerReward() {
        seeder.seedIfEmpty();

        UpgradeTier tier = capturedTierFor("BSkyBlock_example_spawner_t1");
        assertNotNull(tier);

        assertTrue(tier.getRewards().stream()
                .anyMatch(r -> r instanceof world.bentobox.upgrades.dataobjects.rewards.SpawnerRewardDB));
        var spawner = (world.bentobox.upgrades.dataobjects.rewards.SpawnerRewardDB) tier.getRewards()
                .stream().filter(r -> r instanceof world.bentobox.upgrades.dataobjects.rewards.SpawnerRewardDB)
                .findFirst().orElseThrow();
        assertEquals("0.5", spawner.getSpawnBonusEquation());
    }

    // ─── Crop-growth reward ───────────────────────────────────────────────────

    @Test
    void seedIfEmpty_cropGrowthBoost_hasCropGrowthReward() {
        seeder.seedIfEmpty();

        UpgradeTier tier = capturedTierFor("BSkyBlock_example_cropgrowth_t1");
        assertNotNull(tier);

        assertTrue(tier.getRewards().stream()
                .anyMatch(r -> r instanceof CropGrowthRewardDB));
        CropGrowthRewardDB crop = (CropGrowthRewardDB) tier.getRewards().stream()
                .filter(r -> r instanceof CropGrowthRewardDB)
                .findFirst().orElseThrow();
        assertEquals("0.5", crop.getGrowthBonusEquation());
    }

    // ─── Re-seed guard ────────────────────────────────────────────────────────

    @Test
    void seedIfEmpty_doesNotReseedOnSecondCall() {
        seeder.seedIfEmpty();
        seeder.seedIfEmpty(); // second call: already seeded marker file exists

        // saveUpgradeData must still be exactly 8 (from first call only)
        verify(dataManager, times(8)).saveUpgradeData(any());
    }

    @Test
    void seedIfEmpty_doesNotSeedWhenUpgradesAlreadyExist() {
        UpgradeData existing = new UpgradeData();
        existing.setUniqueId("BSkyBlock_existing");
        existing.setWorld("BSkyBlock");
        when(dataManager.getUpgradeDataByGameMode("BSkyBlock"))
                .thenReturn(List.of(existing));

        seeder.seedIfEmpty();

        verify(dataManager, never()).saveUpgradeData(any());
    }

    // ─── Helper ──────────────────────────────────────────────────────────────

    private UpgradeTier capturedTierFor(String uniqueId) {
        ArgumentCaptor<UpgradeTier> captor = ArgumentCaptor.forClass(UpgradeTier.class);
        verify(dataManager, atLeastOnce()).saveUpgradeTier(captor.capture());
        return captor.getAllValues().stream()
                .filter(t -> uniqueId.equals(t.getUniqueId()))
                .findFirst()
                .orElse(null);
    }
}
