package world.bentobox.upgrades;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.upgrades.config.Settings;
import world.bentobox.upgrades.dataobjects.prices.Price;
import world.bentobox.upgrades.dataobjects.rewards.Reward;

public class UpgradesManager {

    public UpgradesManager(UpgradesAddon addon) {
        this.addon = addon;
        this.hookedGameModes = new HashSet<>();
        this.activatedPrices = new HashMap<>();
        this.activatedRewards = new HashMap<>();
    }

    protected void addGameModes(List<String> gameModes) {
        this.hookedGameModes.addAll(gameModes);
    }

    public boolean canOperateInWorld(World world) {
        return this.addon.getPlugin()
                .getIWM()
                .getAddon(world)
                .map(a -> this.hookedGameModes.contains(a.getDescription()
                        .getName()))
                .orElse(false);
    }

    public void addPrice(Price price) {
        this.activatedPrices.put(price.getClass(), price);
    }

    public List<Price> getPrices() {
        return new ArrayList<>(this.activatedPrices.values());
    }

    public Price searchPrice(Class<? extends Price> price) {
        return this.activatedPrices.get(price);
    }

    public void addReward(Reward reward) {
        this.activatedRewards.put(reward.getClass(), reward);
    }

    public List<Reward> getRewards() {
        return new ArrayList<>(this.activatedRewards.values());
    }

    public Reward searchReward(Class<? extends Reward> reward) {
        return this.activatedRewards.get(reward);
    }

    private Optional<String> getGameModeName(World world) {
        return this.addon.getPlugin()
                .getIWM()
                .getAddon(world)
                .map(a -> a.getDescription()
                        .getName());
    }

    public int getIslandLevel(Island island) {
        if (!this.addon.isLevelProvided())
            return 0;

        if (island == null) {
            this.addon.logError("Island couldn't be found");
            return 0;
        }

        return (int) this.addon.getLevelAddon().getManager().getLevelsData(island).getLevel();
    }

    public Map<Material, List<Settings.UpgradeTier>> getAllBlockLimitsUpgradeTiers(World world) {
        Optional<String> nameOpt = getGameModeName(world);
        if (nameOpt.isEmpty()) {
            return Collections.emptyMap();
        }
        String name = nameOpt.get();

        Map<Material, Map<String, Settings.UpgradeTier>> defaultTiers = this.addon.getSettings()
                .getDefaultBlockLimitsUpgradeTierMap();
        Map<Material, Map<String, Settings.UpgradeTier>> customAddonTiers = this.addon.getSettings()
                .getAddonBlockLimitsUpgradeTierMap(name);

        Map<Material, List<Settings.UpgradeTier>> tierList = new EnumMap<>(Material.class);

        if (customAddonTiers.isEmpty()) {
            defaultTiers.forEach((mat, tiers) -> tierList.put(mat, new ArrayList<>(tiers.values())));
        } else {
            customAddonTiers.forEach((mat, tiers) -> {
                Set<String> uniqueIDSet = new HashSet<>(tiers.keySet());
                if (defaultTiers.containsKey(mat))
                    uniqueIDSet.addAll(defaultTiers.get(mat)
                            .keySet());
                List<Settings.UpgradeTier> matTier = new ArrayList<>(uniqueIDSet.size());

                uniqueIDSet.forEach(id -> matTier.add(tiers.getOrDefault(id, defaultTiers.get(mat)
                        .get(id))));
                tierList.put(mat, matTier);
            });

            defaultTiers.forEach(
                    (mat, tiers) -> tierList.putIfAbsent(mat, new ArrayList<>(tiers.values())));
        }

        if (tierList.isEmpty()) {
            return Collections.emptyMap();
        }

        tierList.forEach(
                (mat, tiers) -> tiers.sort(Comparator.comparingInt(Settings.UpgradeTier::getMaxLevel)));

        return tierList;
    }

    public Map<EntityType, List<Settings.UpgradeTier>> getAllEntityLimitsUpgradeTiers(World world) {
        Optional<String> nameOpt = getGameModeName(world);
        if (nameOpt.isEmpty()) {
            return Collections.emptyMap();
        }
        String name = nameOpt.get();

        Map<EntityType, Map<String, Settings.UpgradeTier>> defaultTiers = this.addon.getSettings()
                .getDefaultEntityLimitsUpgradeTierMap();
        Map<EntityType, Map<String, Settings.UpgradeTier>> customAddonTiers = this.addon.getSettings()
                .getAddonEntityLimitsUpgradeTierMap(name);

        Map<EntityType, List<Settings.UpgradeTier>> tierList = new EnumMap<>(EntityType.class);

        if (customAddonTiers.isEmpty()) {
            defaultTiers.forEach((ent, tiers) -> tierList.put(ent, new ArrayList<>(tiers.values())));
        } else {
            customAddonTiers.forEach((ent, tiers) -> {
                Set<String> uniqueIDSet = new HashSet<>(tiers.keySet());
                if (defaultTiers.containsKey(ent))
                    uniqueIDSet.addAll(defaultTiers.get(ent)
                            .keySet());
                List<Settings.UpgradeTier> entTier = new ArrayList<>(uniqueIDSet.size());

                uniqueIDSet.forEach(id -> entTier.add(tiers.getOrDefault(id, defaultTiers.get(ent)
                        .get(id))));
                tierList.put(ent, entTier);
            });

            defaultTiers.forEach(
                    (ent, tiers) -> tierList.putIfAbsent(ent, new ArrayList<>(tiers.values())));
        }

        if (tierList.isEmpty()) {
            return Collections.emptyMap();
        }

        tierList.forEach(
                (ent, tiers) -> tiers.sort(Comparator.comparingInt(Settings.UpgradeTier::getMaxLevel)));

        return tierList;
    }

    public Map<String, List<Settings.UpgradeTier>> getAllEntityGroupLimitsUpgradeTiers(World world) {
        Optional<String> nameOpt = getGameModeName(world);
        if (nameOpt.isEmpty()) {
            return Collections.emptyMap();
        }
        String name = nameOpt.get();

        Map<String, Map<String, Settings.UpgradeTier>> defaultTiers = this.addon.getSettings()
                .getDefaultEntityGroupLimitsUpgradeTierMap();
        Map<String, Map<String, Settings.UpgradeTier>> customAddonTiers = this.addon.getSettings()
                .getAddonEntityGroupLimitsUpgradeTierMap(name);

        Map<String, List<Settings.UpgradeTier>> tierList = new HashMap<>();

        if (customAddonTiers.isEmpty()) {
            defaultTiers.forEach((ent, tiers) -> tierList.put(ent, new ArrayList<>(tiers.values())));
        } else {
            customAddonTiers.forEach((ent, tiers) -> {
                Set<String> uniqueIDSet = new HashSet<>(tiers.keySet());
                if (defaultTiers.containsKey(ent))
                    uniqueIDSet.addAll(defaultTiers.get(ent)
                            .keySet());
                List<Settings.UpgradeTier> entTier = new ArrayList<>(uniqueIDSet.size());

                uniqueIDSet.forEach(id -> entTier.add(tiers.getOrDefault(id, defaultTiers.get(ent)
                        .get(id))));
                tierList.put(ent, entTier);
            });

            defaultTiers.forEach(
                    (ent, tiers) -> tierList.putIfAbsent(ent, new ArrayList<>(tiers.values())));
        }

        if (tierList.isEmpty()) {
            return Collections.emptyMap();
        }

        tierList.forEach(
                (ent, tiers) -> tiers.sort(Comparator.comparingInt(Settings.UpgradeTier::getMaxLevel)));

        return tierList;
    }


    private UpgradesAddon addon;

    private Set<String> hookedGameModes;

    private Map<Class<? extends Price>, Price> activatedPrices;

    private Map<Class<? extends Reward>, Reward> activatedRewards;

}
