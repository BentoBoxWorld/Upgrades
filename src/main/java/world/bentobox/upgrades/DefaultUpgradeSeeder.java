package world.bentobox.upgrades;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import world.bentobox.upgrades.dataobjects.UpgradeData;
import world.bentobox.upgrades.dataobjects.UpgradeTier;
import world.bentobox.upgrades.dataobjects.prices.IslandLevelPriceDB;
import world.bentobox.upgrades.dataobjects.prices.ItemPriceDB;
import world.bentobox.upgrades.dataobjects.prices.MoneyPriceDB;
import world.bentobox.upgrades.dataobjects.prices.PermissionPriceDB;
import world.bentobox.upgrades.dataobjects.rewards.CommandRewardDB;
import world.bentobox.upgrades.dataobjects.rewards.CropGrowthRewardDB;
import world.bentobox.upgrades.dataobjects.rewards.LimitsRewardDB;
import world.bentobox.upgrades.dataobjects.rewards.RangeRewardDB;
import world.bentobox.upgrades.dataobjects.rewards.SpawnerRewardDB;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Seeds 6 example upgrades for each hooked game mode on first install.
 * Skips any game mode that already has at least one configured upgrade.
 */
public class DefaultUpgradeSeeder {

    private static final String SEEDED_FILE = ".seeded-gamemodes";

    private final UpgradesAddon addon;

    public DefaultUpgradeSeeder(UpgradesAddon addon) {
        this.addon = addon;
    }

    /**
     * For each hooked game mode that has no upgrades configured and has not
     * been seeded before, seed examples. Tracks seeded game modes in a
     * persistent marker file so deleted examples are not regenerated.
     */
    public void seedIfEmpty() {
        Set<String> alreadySeeded = loadSeededGameModes();
        UpgradesDataManager dm = addon.getUpgradeDataManager();
        boolean changed = false;
        for (String gm : addon.getHookedGameModes()) {
            if (alreadySeeded.contains(gm)) {
                continue;
            }
            if (dm.getUpgradeDataByGameMode(gm).isEmpty()) {
                seed(dm, gm);
                addon.log("Seeded 8 example upgrades for " + gm
                        + " — edit or delete them via /[gamemode] admin upgrade");
            }
            // Mark as seeded regardless — either we just seeded, or upgrades
            // already existed. Either way, don't re-seed on future startups.
            alreadySeeded.add(gm);
            changed = true;
        }
        if (changed) {
            saveSeededGameModes(alreadySeeded);
        }
    }

    /**
     * Load the set of game mode names that have already been seeded.
     */
    private Set<String> loadSeededGameModes() {
        Set<String> seeded = new HashSet<>();
        File file = new File(addon.getDataFolder(), SEEDED_FILE);
        if (!file.exists()) {
            return seeded;
        }
        try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    seeded.add(trimmed);
                }
            }
        } catch (IOException e) {
            addon.logWarning("Could not read seeded game modes file: " + e.getMessage());
        }
        return seeded;
    }

    /**
     * Persist the set of seeded game mode names.
     */
    private void saveSeededGameModes(Set<String> seeded) {
        File file = new File(addon.getDataFolder(), SEEDED_FILE);
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
            for (String gm : seeded) {
                writer.write(gm);
                writer.newLine();
            }
        } catch (IOException e) {
            addon.logWarning("Could not save seeded game modes file: " + e.getMessage());
        }
    }

    private void seed(UpgradesDataManager dm, String gm) {
        seedBorderBasic(dm, gm);
        seedBorderAdvanced(dm, gm);
        seedHopperLimit(dm, gm);
        seedCowLimit(dm, gm);
        seedDiamondBorder(dm, gm);
        seedDonorPerk(dm, gm);
        seedSpawnerBoost(dm, gm);
        seedCropBoost(dm, gm);
    }

    // ─── Example 1: Border Expansion I ───────────────────────────────────────
    // Demonstrates: simple MoneyPrice → RangeReward (5 purchases available)

    private void seedBorderBasic(UpgradesDataManager dm, String gm) {
        UpgradeData data = dm.createUpgradeData(gm + "_example_range1", gm, null);
        if (data == null) return;
        data.setName("Border Expansion I");
        data.setIcon(new ItemStack(Material.OAK_FENCE));
        data.setOrder(1);
        data.setDescription(List.of("Example: pay $500 to expand island border by 5 blocks. Up to 5 times."));
        dm.saveUpgradeData(data);

        UpgradeTier tier = dm.createUpgradeTier(gm + "_example_range1_t1", data, 0, 4, null);
        if (tier == null) return;
        tier.setName("Border Expansion I");

        MoneyPriceDB money = new MoneyPriceDB();
        money.setAmountEquation("500");
        tier.setPrices(List.of(money));

        RangeRewardDB range = new RangeRewardDB();
        range.setRangeUpgradeEquation("5");
        tier.setRewards(List.of(range));

        dm.saveUpgradeTier(tier);
    }

    // ─── Example 2: Border Expansion II ──────────────────────────────────────
    // Demonstrates: IslandLevelPrice gate + MoneyPrice → RangeReward (3 purchases)

    private void seedBorderAdvanced(UpgradesDataManager dm, String gm) {
        UpgradeData data = dm.createUpgradeData(gm + "_example_range2", gm, null);
        if (data == null) return;
        data.setName("Border Expansion II");
        data.setIcon(new ItemStack(Material.IRON_BARS));
        data.setOrder(2);
        data.setDescription(List.of("Example: requires island level 100 and costs $2000. Up to 3 times."));
        dm.saveUpgradeData(data);

        UpgradeTier tier = dm.createUpgradeTier(gm + "_example_range2_t1", data, 0, 2, null);
        if (tier == null) return;
        tier.setName("Border Expansion II");

        IslandLevelPriceDB level = new IslandLevelPriceDB();
        level.setLevelNeededEquation("100");
        MoneyPriceDB money = new MoneyPriceDB();
        money.setAmountEquation("2000");
        tier.setPrices(List.of(level, money));

        RangeRewardDB range = new RangeRewardDB();
        range.setRangeUpgradeEquation("10");
        tier.setRewards(List.of(range));

        dm.saveUpgradeTier(tier);
    }

    // ─── Example 3: Hopper Limit ──────────────────────────────────────────────
    // Demonstrates: MoneyPrice → LimitsReward (BLOCK) (5 purchases)

    private void seedHopperLimit(UpgradesDataManager dm, String gm) {
        UpgradeData data = dm.createUpgradeData(gm + "_example_hopper", gm, null);
        if (data == null) return;
        data.setName("Hopper Limit");
        data.setIcon(new ItemStack(Material.HOPPER));
        data.setOrder(3);
        data.setDescription(List.of("Example: pay $1000 to increase your hopper limit by 2. Up to 5 times."));
        dm.saveUpgradeData(data);

        UpgradeTier tier = dm.createUpgradeTier(gm + "_example_hopper_t1", data, 0, 4, null);
        if (tier == null) return;
        tier.setName("Hopper Limit");

        MoneyPriceDB money = new MoneyPriceDB();
        money.setAmountEquation("1000");
        tier.setPrices(List.of(money));

        LimitsRewardDB limits = new LimitsRewardDB();
        limits.setLimitType("BLOCK");
        limits.setTarget("HOPPER");
        limits.setAmountEquation("2");
        tier.setRewards(List.of(limits));

        dm.saveUpgradeTier(tier);
    }

    // ─── Example 4: Cow Limit ─────────────────────────────────────────────────
    // Demonstrates: MoneyPrice → LimitsReward (ENTITY) (5 purchases)

    private void seedCowLimit(UpgradesDataManager dm, String gm) {
        UpgradeData data = dm.createUpgradeData(gm + "_example_cow", gm, null);
        if (data == null) return;
        data.setName("Cow Limit");
        data.setIcon(new ItemStack(Material.BEEF));
        data.setOrder(4);
        data.setDescription(List.of("Example: pay $500 to increase your cow entity limit by 2. Up to 5 times."));
        dm.saveUpgradeData(data);

        UpgradeTier tier = dm.createUpgradeTier(gm + "_example_cow_t1", data, 0, 4, null);
        if (tier == null) return;
        tier.setName("Cow Limit");

        MoneyPriceDB money = new MoneyPriceDB();
        money.setAmountEquation("500");
        tier.setPrices(List.of(money));

        LimitsRewardDB limits = new LimitsRewardDB();
        limits.setLimitType("ENTITY");
        limits.setTarget("COW");
        limits.setAmountEquation("2");
        tier.setRewards(List.of(limits));

        dm.saveUpgradeTier(tier);
    }

    // ─── Example 5: Diamond Border ────────────────────────────────────────────
    // Demonstrates: ItemPrice → RangeReward (3 purchases)

    private void seedDiamondBorder(UpgradesDataManager dm, String gm) {
        UpgradeData data = dm.createUpgradeData(gm + "_example_diamond", gm, null);
        if (data == null) return;
        data.setName("Diamond Border");
        data.setIcon(new ItemStack(Material.DIAMOND));
        data.setOrder(5);
        data.setDescription(List.of("Example: spend 10 diamonds to expand island border by 3 blocks. Up to 3 times."));
        dm.saveUpgradeData(data);

        UpgradeTier tier = dm.createUpgradeTier(gm + "_example_diamond_t1", data, 0, 2, null);
        if (tier == null) return;
        tier.setName("Diamond Border");

        ItemPriceDB item = new ItemPriceDB();
        item.setMaterial("DIAMOND");
        item.setAmount(10);
        tier.setPrices(List.of(item));

        RangeRewardDB range = new RangeRewardDB();
        range.setRangeUpgradeEquation("3");
        tier.setRewards(List.of(range));

        dm.saveUpgradeTier(tier);
    }

    // ─── Example 6: Donor Perk ────────────────────────────────────────────────
    // Demonstrates: PermissionPrice (gate) → CommandReward (one-time purchase)

    private void seedDonorPerk(UpgradesDataManager dm, String gm) {
        UpgradeData data = dm.createUpgradeData(gm + "_example_donor", gm, null);
        if (data == null) return;
        data.setName("Donor Perk");
        data.setIcon(new ItemStack(Material.NETHER_STAR));
        data.setOrder(6);
        data.setDescription(List.of("Example: requires the upgrades.example.donor permission. Runs a console command on purchase."));
        dm.saveUpgradeData(data);

        UpgradeTier tier = dm.createUpgradeTier(gm + "_example_donor_t1", data, 0, 0, null);
        if (tier == null) return;
        tier.setName("Donor Perk");

        PermissionPriceDB perm = new PermissionPriceDB();
        perm.setPermission("upgrades.example.donor");
        tier.setPrices(List.of(perm));

        CommandRewardDB cmd = new CommandRewardDB();
        List<String> commands = new ArrayList<>();
        commands.add("say [player] has unlocked the Donor Perk on their island!");
        cmd.setCommands(commands);
        cmd.setConsole(true);
        tier.setRewards(List.of(cmd));

        dm.saveUpgradeTier(tier);
    }

    // ─── Example 7: Spawner Boost ─────────────────────────────────────────────
    // Demonstrates: MoneyPrice → SpawnerRewardDB (5 purchases)

    private void seedSpawnerBoost(UpgradesDataManager dm, String gm) {
        UpgradeData data = dm.createUpgradeData(gm + "_example_spawner", gm, null);
        if (data == null) return;
        data.setName("Spawner Boost");
        data.setIcon(new ItemStack(Material.SPAWNER));
        data.setOrder(7);
        data.setDescription(List.of("Example: pay $2000 for +0.5 extra entities per spawner trigger."));
        dm.saveUpgradeData(data);

        UpgradeTier tier = dm.createUpgradeTier(gm + "_example_spawner_t1", data, 0, 4, null);
        if (tier == null) return;
        tier.setName("Spawner Boost");

        MoneyPriceDB money = new MoneyPriceDB();
        money.setAmountEquation("2000");
        tier.setPrices(List.of(money));

        SpawnerRewardDB spawner = new SpawnerRewardDB();
        spawner.setSpawnBonusEquation("0.5");
        tier.setRewards(List.of(spawner));

        dm.saveUpgradeTier(tier);
    }

    // ─── Example 8: Crop Growth Boost ─────────────────────────────────────────
    // Demonstrates: MoneyPrice → CropGrowthRewardDB (5 purchases)

    private void seedCropBoost(UpgradesDataManager dm, String gm) {
        UpgradeData data = dm.createUpgradeData(gm + "_example_cropgrowth", gm, null);
        if (data == null) return;
        data.setName("Crop Growth Boost");
        data.setIcon(new ItemStack(Material.WHEAT));
        data.setOrder(8);
        data.setDescription(List.of("Example: pay $500 for 50% extra growth chance per crop tick."));
        dm.saveUpgradeData(data);

        UpgradeTier tier = dm.createUpgradeTier(gm + "_example_cropgrowth_t1", data, 0, 4, null);
        if (tier == null) return;
        tier.setName("Crop Growth Boost");

        MoneyPriceDB money = new MoneyPriceDB();
        money.setAmountEquation("500");
        tier.setPrices(List.of(money));

        CropGrowthRewardDB crop = new CropGrowthRewardDB();
        crop.setGrowthBonusEquation("0.5");
        tier.setRewards(List.of(crop));

        dm.saveUpgradeTier(tier);
    }
}
