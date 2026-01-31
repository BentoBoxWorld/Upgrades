package world.bentobox.upgrades.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.eclipse.jdt.annotation.NonNull;

import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.upgrades.UpgradesAddon;

/**
 * Represents the settings and configurations for the UpgradesAddon.
 * Handles configuration data parsing and storage, enabling upgrades and
 * managing limits for various game aspects like blocks, entities, and commands.
 */
public class Settings {

    /**
     * The UpgradesAddon instance associated with this settings object.
     */
    private UpgradesAddon addon;

    /**
     * Set of game modes where the upgrades are disabled.
     */
    private Set<String> disabledGameModes;

    /**
     * Maximum range for range upgrades.
     */
    private int maxRangeUpgrade = 0;

    /**
     * Flag indicating if the range upgrade is enabled.
     */
    private boolean hasRangeUpgrade;

    /**
     * Custom maximum range upgrades per game mode.
     */
    private Map<String, Integer> customMaxRangeUpgrade = new TreeMap<>();

    /**
     * Default range upgrade tiers.
     */
    private Map<String, UpgradeTier> rangeUpgradeTierMap = new TreeMap<>();

    /**
     * Custom range upgrade tiers per game mode.
     */
    private Map<String, Map<String, UpgradeTier>> customRangeUpgradeTierMap = new TreeMap<>();

    /**
     * Default block limits upgrades for each material.
     */
    private Map<Material, Integer> maxBlockLimitsUpgrade = new EnumMap<>(Material.class);

    /**
     * Custom block limits upgrades for each material and game mode.
     */
    private Map<String, Map<Material, Integer>> customMaxBlockLimitsUpgrade = new TreeMap<>();

    /**
     * Default block limits upgrade tiers for each material.
     */
    private Map<Material, Map<String, UpgradeTier>> blockLimitsUpgradeTierMap = new EnumMap<>(Material.class);

    /**
     * Custom block limits upgrade tiers per game mode.
     */
    private Map<String, Map<Material, Map<String, UpgradeTier>>> customBlockLimitsUpgradeTierMap = new TreeMap<>();

    /**
     * Entity type to material icon mapping.
     */
    private Map<EntityType, Material> entityIcon = new EnumMap<>(EntityType.class);

    /**
     * Entity group to material icon mapping.
     */
    private Map<String, Material> entityGroupIcon = new TreeMap<>();

    /**
     * Default entity limits upgrades for each entity type.
     */
    private Map<EntityType, Integer> maxEntityLimitsUpgrade = new EnumMap<>(EntityType.class);

    /**
     * Default entity group limits upgrades.
     */
    private Map<String, Integer> maxEntityGroupLimitsUpgrade = new TreeMap<>();

    /**
     * Custom entity limits upgrades per game mode.
     */
    private Map<String, Map<EntityType, Integer>> customMaxEntityLimitsUpgrade = new TreeMap<>();

    /**
     * Custom entity group limits upgrades per game mode.
     */
    private Map<String, Map<String, Integer>> customMaxEntityGroupLimitsUpgrade = new TreeMap<>();

    /**
     * Default entity limits upgrade tiers for each entity type.
     */
    private Map<EntityType, Map<String, UpgradeTier>> entityLimitsUpgradeTierMap = new EnumMap<>(EntityType.class);

    /**
     * Default entity group limits upgrade tiers.
     */
    private Map<String, Map<String, UpgradeTier>> entityGroupLimitsUpgradeTierMap = new TreeMap<>();

    /**
     * Custom entity limits upgrade tiers per game mode.
     */
    private Map<String, Map<EntityType, Map<String, UpgradeTier>>> customEntityLimitsUpgradeTierMap = new TreeMap<>();

    /**
     * Custom entity group limits upgrade tiers per game mode.
     */
    private Map<String, Map<String, Map<String, UpgradeTier>>> customEntityGroupLimitsUpgradeTierMap = new TreeMap<>();

    /**
     * Default command limits upgrades.
     */
    private Map<String, Integer> maxCommandUpgrade = new TreeMap<>();

    /**
     * Custom command limits upgrades per game mode.
     */
    private Map<String, Map<String, Integer>> customMaxCommandUpgrade = new TreeMap<>();

    /**
     * Default command upgrade tiers.
     */
    private Map<String, Map<String, CommandUpgradeTier>> commandUpgradeTierMap = new TreeMap<>();

    /**
     * Custom command upgrade tiers per game mode.
     */
    private Map<String, Map<String, Map<String, CommandUpgradeTier>>> customCommandUpgradeTierMap = new TreeMap<>();

    /**
     * Command to material icon mapping.
     */
    private Map<String, Material> commandIcon = new TreeMap<>();

    /**
     * Command name mappings.
     */
    private Map<String, String> commandName = new TreeMap<>();

    private EntityType getEntityType(String key) {
        return Arrays.stream(EntityType.values()).filter(v -> v.name().equalsIgnoreCase(key)).findFirst().orElse(null);
    }

    /**
     * Constructs a new Settings object and initializes the configurations.
     *
     * @param addon The UpgradesAddon instance.
     */
    public Settings(UpgradesAddon addon) {
        this.addon = addon;
        this.addon.saveDefaultConfig();

        this.hasRangeUpgrade = false;

        this.disabledGameModes = new HashSet<>(this.addon.getConfig().getStringList("disabled-gamemodes"));

        if (this.addon.getConfig().isSet("range-upgrade")) {
            ConfigurationSection section = this.addon.getConfig().getConfigurationSection("range-upgrade");
            for (String key : Objects.requireNonNull(section).getKeys(false)) {
                UpgradeTier newUpgrade = addUpgradeSection(section, key);

                if (this.maxRangeUpgrade < newUpgrade.getMaxLevel())
                    this.maxRangeUpgrade = newUpgrade.getMaxLevel();

                this.hasRangeUpgrade = true;
                this.rangeUpgradeTierMap.put(key, newUpgrade);
            }
        }

        if (this.addon.getConfig().isSet("block-limits-upgrade")) {
            ConfigurationSection section = this.addon.getConfig().getConfigurationSection("block-limits-upgrade");
            this.blockLimitsUpgradeTierMap = this.loadBlockLimits(section, null);
        }

        if (this.addon.getConfig().isSet("entity-icon")) {
            ConfigurationSection section = this.addon.getConfig().getConfigurationSection("entity-icon");
            for (String entity : Objects.requireNonNull(section).getKeys(false)) {
                String material = section.getString(entity);
                EntityType ent = this.getEntityType(entity);
                Material mat = Material.getMaterial(material);
                if (ent == null)
                    this.addon.logError("Config: EntityType " + entity + " is not valid in icon");
                else if (mat == null)
                    this.addon.logError("Config: Material " + material + " is not a valid material");
                else
                    this.entityIcon.put(ent, mat);
            }
        }

        if (this.addon.getConfig().isSet("entity-group-icon")) {
            ConfigurationSection section = this.addon.getConfig().getConfigurationSection("entity-group-icon");
            for (String group : Objects.requireNonNull(section).getKeys(false)) {
                String material = section.getString(group);
                Material mat = Material.getMaterial(material);
                if (mat == null)
                    this.addon.logError("Config: Material " + material + " is not a valid material");
                else
                    this.entityGroupIcon.put(group, mat);
            }
        }

        if (this.addon.getConfig().isSet("entity-limits-upgrade")) {
            ConfigurationSection section = this.addon.getConfig().getConfigurationSection("entity-limits-upgrade");
            this.entityLimitsUpgradeTierMap = this.loadEntityLimits(section, null);
        }

        if (this.addon.getConfig().isSet("entity-group-limits-upgrade")) {
            ConfigurationSection section = this.addon.getConfig()
                    .getConfigurationSection("entity-group-limits-upgrade");
            this.entityGroupLimitsUpgradeTierMap = this.loadEntityGroupLimits(section, null);
        }

        if (this.addon.getConfig().isSet("command-icon")) {
            ConfigurationSection section = this.addon.getConfig().getConfigurationSection("command-icon");
            for (String commandId : Objects.requireNonNull(section).getKeys(false)) {
                String material = section.getString(commandId);
                Material mat = Material.getMaterial(material);
                if (mat == null)
                    this.addon.logError("Config: Material " + material + " is not a valid material");
                else
                    this.commandIcon.put(commandId, mat);
            }
        }

        if (this.addon.getConfig().isSet("command-upgrade")) {
            ConfigurationSection section = this.addon.getConfig().getConfigurationSection("command-upgrade");
            this.commandUpgradeTierMap = this.loadCommand(section, null);
        }

        if (this.addon.getConfig().isSet("gamemodes")) {
            ConfigurationSection section = this.addon.getConfig().getConfigurationSection("gamemodes");

            for (String gameMode : Objects.requireNonNull(section).getKeys(false)) {
                ConfigurationSection gameModeSection = section.getConfigurationSection(gameMode);

                if (gameModeSection.isSet("range-upgrade")) {
                    ConfigurationSection lowSection = gameModeSection.getConfigurationSection("range-upgrade");
                    for (String key : Objects.requireNonNull(lowSection).getKeys(false)) {
                        UpgradeTier newUpgrade = addUpgradeSection(lowSection, key);

                        if (this.customMaxRangeUpgrade.get(gameMode) == null
                                || this.customMaxRangeUpgrade.get(gameMode) < newUpgrade.getMaxLevel())
                            this.customMaxRangeUpgrade.put(gameMode, newUpgrade.getMaxLevel());

                        this.hasRangeUpgrade = true;

                        this.customRangeUpgradeTierMap.computeIfAbsent(gameMode, k -> new TreeMap<>()).put(key,
                                newUpgrade);
                    }
                }

                if (gameModeSection.isSet("block-limits-upgrade")) {
                    ConfigurationSection lowSection = gameModeSection.getConfigurationSection("block-limits-upgrade");
                    this.customBlockLimitsUpgradeTierMap.computeIfAbsent(gameMode,
                            k -> loadBlockLimits(lowSection, gameMode));
                }

                if (gameModeSection.isSet("entity-limits-upgrade")) {
                    ConfigurationSection lowSection = gameModeSection.getConfigurationSection("entity-limits-upgrade");
                    this.customEntityLimitsUpgradeTierMap.computeIfAbsent(gameMode,
                            k -> loadEntityLimits(lowSection, gameMode));
                }

                if (gameModeSection.isSet("entity-group-limits-upgrade")) {
                    ConfigurationSection lowSection = gameModeSection
                            .getConfigurationSection("entity-group-limits-upgrade");
                    this.customEntityGroupLimitsUpgradeTierMap.computeIfAbsent(gameMode,
                            k -> loadEntityGroupLimits(lowSection, gameMode));
                }

                if (gameModeSection.isSet("command-upgrade")) {
                    ConfigurationSection lowSection = gameModeSection.getConfigurationSection("command-upgrade");
                    this.customCommandUpgradeTierMap.computeIfAbsent(gameMode, k -> loadCommand(lowSection, gameMode));
                }
            }
        }
    }

    private Map<Material, Map<String, UpgradeTier>> loadBlockLimits(ConfigurationSection section, String gameMode) {
        Map<Material, Map<String, UpgradeTier>> mats = new EnumMap<>(Material.class);
        for (String material : Objects.requireNonNull(section).getKeys(false)) {
            Material mat = Material.getMaterial(material);
            if (mat != null && mat.isBlock()) {
                Map<String, UpgradeTier> tier = new TreeMap<>();
                ConfigurationSection matSection = section.getConfigurationSection(material);
                for (String key : Objects.requireNonNull(matSection).getKeys(false)) {
                    UpgradeTier newUpgrade = addUpgradeSection(matSection, key);

                    if (gameMode == null) {
                        if (this.maxBlockLimitsUpgrade.get(mat) == null
                                || this.maxBlockLimitsUpgrade.get(mat) < newUpgrade.getMaxLevel())
                            this.maxBlockLimitsUpgrade.put(mat, newUpgrade.getMaxLevel());
                    } else {
                        if (this.customMaxBlockLimitsUpgrade.get(gameMode) == null) {
                            Map<Material, Integer> newMap = new EnumMap<>(Material.class);
                            newMap.put(mat, newUpgrade.getMaxLevel());
                            this.customMaxBlockLimitsUpgrade.put(gameMode, newMap);
                        } else {
                            if (this.customMaxBlockLimitsUpgrade.get(gameMode).get(mat) == null
                                    || this.customMaxBlockLimitsUpgrade.get(gameMode).get(mat) < newUpgrade
                                            .getMaxLevel())
                                this.customMaxBlockLimitsUpgrade.get(gameMode).put(mat, newUpgrade.getMaxLevel());
                        }
                    }

                    tier.put(key, newUpgrade);
                }
                mats.put(mat, tier);
            } else {
                this.addon.logError("Material " + material + " is not a valid material. Skipping...");
            }
        }
        return mats;
    }

    private Map<EntityType, Map<String, UpgradeTier>> loadEntityLimits(ConfigurationSection section, String gameMode) {
        Map<EntityType, Map<String, UpgradeTier>> ents = new EnumMap<>(EntityType.class);
        for (String entity : Objects.requireNonNull(section).getKeys(false)) {
            EntityType ent = this.getEntityType(entity);
            if (ent != null && this.entityIcon.containsKey(ent)) {
                Map<String, UpgradeTier> tier = new TreeMap<>();
                ConfigurationSection entSection = section.getConfigurationSection(entity);
                for (String key : Objects.requireNonNull(entSection).getKeys(false)) {
                    UpgradeTier newUpgrade = addUpgradeSection(entSection, key);

                    if (gameMode == null) {
                        if (this.maxEntityLimitsUpgrade.get(ent) == null
                                || this.maxEntityLimitsUpgrade.get(ent) < newUpgrade.getMaxLevel())
                            this.maxEntityLimitsUpgrade.put(ent, newUpgrade.getMaxLevel());
                    } else {
                        if (this.customMaxEntityLimitsUpgrade.get(gameMode) == null) {
                            Map<EntityType, Integer> newMap = new EnumMap<>(EntityType.class);
                            newMap.put(ent, newUpgrade.getMaxLevel());
                            this.customMaxEntityLimitsUpgrade.put(gameMode, newMap);
                        } else {
                            if (this.customMaxEntityLimitsUpgrade.get(gameMode).get(ent) == null
                                    || this.customMaxEntityLimitsUpgrade.get(gameMode).get(ent) < newUpgrade
                                            .getMaxLevel())
                                this.customMaxEntityLimitsUpgrade.get(gameMode).put(ent, newUpgrade.getMaxLevel());
                        }
                    }

                    tier.put(key, newUpgrade);
                }
                ents.put(ent, tier);
            } else {
                if (ent != null)
                    this.addon.logError("Entity " + entity + " is not a valid entity. Skipping...");
                else
                    this.addon.logError("Entity " + entity + " is missing a corresponding icon. Skipping...");
            }
        }
        return ents;
    }

    private Map<String, Map<String, UpgradeTier>> loadEntityGroupLimits(ConfigurationSection section, String gameMode) {
        Map<String, Map<String, UpgradeTier>> ents = new TreeMap<>();
        for (String entitygroup : Objects.requireNonNull(section).getKeys(false)) {
            Map<String, UpgradeTier> tier = new TreeMap<>();
            ConfigurationSection entSection = section.getConfigurationSection(entitygroup);
            for (String key : Objects.requireNonNull(entSection).getKeys(false)) {
                UpgradeTier newUpgrade = addUpgradeSection(entSection, key);

                if (gameMode == null) {
                    if (this.maxEntityGroupLimitsUpgrade.get(entitygroup) == null
                            || this.maxEntityGroupLimitsUpgrade.get(entitygroup) < newUpgrade.getMaxLevel())
                        this.maxEntityGroupLimitsUpgrade.put(entitygroup, newUpgrade.getMaxLevel());
                } else {
                    if (this.customMaxEntityGroupLimitsUpgrade.get(gameMode) == null) {
                        Map<String, Integer> newMap = new TreeMap<>();
                        newMap.put(entitygroup, newUpgrade.getMaxLevel());
                        this.customMaxEntityGroupLimitsUpgrade.put(gameMode, newMap);
                    } else {
                        if (this.customMaxEntityGroupLimitsUpgrade.get(gameMode).get(entitygroup) == null
                                || this.customMaxEntityGroupLimitsUpgrade.get(gameMode).get(entitygroup) < newUpgrade
                                        .getMaxLevel())
                            this.customMaxEntityGroupLimitsUpgrade.get(gameMode).put(entitygroup,
                                    newUpgrade.getMaxLevel());
                    }
                }

                tier.put(key, newUpgrade);
            }
            ents.put(entitygroup, tier);
        }
        return ents;
    }

    private Map<String, Map<String, CommandUpgradeTier>> loadCommand(ConfigurationSection section, String gamemode) {
        Map<String, Map<String, CommandUpgradeTier>> commands = new TreeMap<>();

        for (String commandId : Objects.requireNonNull(section).getKeys(false)) {
            if (this.commandIcon.containsKey(commandId)) {
                String name = commandId;
                Map<String, CommandUpgradeTier> tier = new TreeMap<>();
                ConfigurationSection cmdSection = section.getConfigurationSection(commandId);
                for (String key : Objects.requireNonNull(cmdSection).getKeys(false)) {
                    if (key.equals("name")) {
                        name = cmdSection.getString(key);
                    } else {
                        CommandUpgradeTier newUpgrade = addCommandUpgradeSection(cmdSection, key);

                        if (gamemode == null) {
                            if (this.maxCommandUpgrade.get(commandId) == null
                                    || this.maxCommandUpgrade.get(commandId) < newUpgrade.getMaxLevel()) {
                                this.maxCommandUpgrade.put(commandId, newUpgrade.getMaxLevel());
                            }
                        } else {
                            if (this.customMaxCommandUpgrade.get(gamemode) == null) {
                                Map<String, Integer> newMap = new TreeMap<>();
                                newMap.put(commandId, newUpgrade.getMaxLevel());
                                this.customMaxCommandUpgrade.put(gamemode, newMap);
                            } else {
                                if (this.customMaxCommandUpgrade.get(gamemode).get(commandId) == null
                                        || this.customMaxCommandUpgrade.get(gamemode).get(commandId) < newUpgrade
                                                .getMaxLevel())
                                    this.customMaxCommandUpgrade.get(gamemode).put(commandId, newUpgrade.getMaxLevel());
                            }
                        }

                        tier.put(key, newUpgrade);
                    }
                }
                if (!this.commandName.containsKey(commandId) || !name.equals(commandId))
                    this.commandName.put(commandId, name);
                commands.put(commandId, tier);
            } else {
                this.addon.logError("Command " + commandId + " is missing a corresponding icon. Skipping...");
            }
        }

        return commands;
    }

    @NonNull
    private UpgradeTier addUpgradeSection(ConfigurationSection section, String key) {
        ConfigurationSection tierSection = section.getConfigurationSection(key);
        UpgradeTier upgradeTier = new UpgradeTier(key);
        upgradeTier.setTierName(tierSection.getName());
        upgradeTier.setMaxLevel(tierSection.getInt("max-level"));
        upgradeTier.setUpgrade(parse(tierSection.getString("upgrade"), upgradeTier.getExpressionVariable()));

        if (tierSection.isSet("island-min-level"))
            upgradeTier.setIslandMinLevel(
                    parse(tierSection.getString("island-min-level"), upgradeTier.getExpressionVariable()));
        else
            upgradeTier.setIslandMinLevel(parse("0", upgradeTier.getExpressionVariable()));

        if (tierSection.isSet("vault-cost"))
            upgradeTier.setVaultCost(parse(tierSection.getString("vault-cost"), upgradeTier.getExpressionVariable()));
        else
            upgradeTier.setVaultCost(parse("0", upgradeTier.getExpressionVariable()));

        if (tierSection.isSet("permission-level"))
            upgradeTier.setPermissionLevel(tierSection.getInt("permission-level"));
        else
            upgradeTier.setPermissionLevel(0);

        return upgradeTier;

    }

    @NonNull
    private CommandUpgradeTier addCommandUpgradeSection(ConfigurationSection section, String key) {
        ConfigurationSection tierSection = section.getConfigurationSection(key);
        CommandUpgradeTier upgradeTier = new CommandUpgradeTier(key);
        upgradeTier.setTierName(tierSection.getName());
        upgradeTier.setMaxLevel(tierSection.getInt("max-level"));
        upgradeTier.setUpgrade(parse("0", upgradeTier.getExpressionVariable()));

        if (tierSection.isSet("island-min-level"))
            upgradeTier.setIslandMinLevel(
                    parse(tierSection.getString("island-min-level"), upgradeTier.getExpressionVariable()));
        else
            upgradeTier.setIslandMinLevel(parse("0", upgradeTier.getExpressionVariable()));

        if (tierSection.isSet("vault-cost"))
            upgradeTier.setVaultCost(parse(tierSection.getString("vault-cost"), upgradeTier.getExpressionVariable()));
        else
            upgradeTier.setVaultCost(parse("0", upgradeTier.getExpressionVariable()));

        if (tierSection.isSet("permission-level"))
            upgradeTier.setPermissionLevel(tierSection.getInt("permission-level"));
        else
            upgradeTier.setPermissionLevel(0);

        if (tierSection.isSet("console") && tierSection.isBoolean("console"))
            upgradeTier.setConsole(tierSection.getBoolean("console"));
        else
            upgradeTier.setConsole(false);

        if (tierSection.isSet("command"))
            upgradeTier.setCommandList(tierSection.getStringList("command"));

        return upgradeTier;

    }

    /**
     * Retrieves the disabled game modes.
     *
     * @return A set of disabled game modes.
     */
    public Set<String> getDisabledGameModes() {
        return disabledGameModes;
    }

    /**
     * Checks if the range upgrade is enabled.
     *
     * @return True if range upgrade is enabled, otherwise false.
     */
    public boolean getHasRangeUpgrade() {
        return hasRangeUpgrade;
    }

    /**
     * Gets the maximum range upgrade for a specific addon.
     *
     * @param addon The name of the addon.
     * @return The maximum range upgrade value.
     */
    public int getMaxRangeUpgrade(String addon) {
        return customMaxRangeUpgrade.getOrDefault(addon, maxRangeUpgrade);
    }

    /**
     * Retrieves the default range upgrade tier map.
     *
     * @return A map of range upgrade tiers by their identifiers.
     */
    public Map<String, UpgradeTier> getDefaultRangeUpgradeTierMap() {
        return rangeUpgradeTierMap;
    }

    /**
     * Retrieves the range upgrade tier map for a specific addon.
     *
     * @param addon The name of the addon.
     * @return A map of range upgrade tiers specific to the addon.
     */
    public Map<String, UpgradeTier> getAddonRangeUpgradeTierMap(String addon) {
        return customRangeUpgradeTierMap.getOrDefault(addon, Collections.emptyMap());
    }

    /**
     * Gets the maximum block limits upgrade for a material and addon.
     *
     * @param mat   The material type.
     * @param addon The name of the addon.
     * @return The maximum block limit for the material.
     */
    public int getMaxBlockLimitsUpgrade(Material mat, String addon) {
        return customMaxBlockLimitsUpgrade.getOrDefault(addon, maxBlockLimitsUpgrade).getOrDefault(mat, 0);
    }

    /**
     * Retrieves the default block limits upgrade tier map.
     *
     * @return A map of block limits upgrade tiers by material.
     */
    public Map<Material, Map<String, UpgradeTier>> getDefaultBlockLimitsUpgradeTierMap() {
        return blockLimitsUpgradeTierMap;
    }

    /**
     * Retrieves the block limits upgrade tier map for a specific addon.
     *
     * @param addon The name of the addon.
     * @return A map of block limits upgrade tiers specific to the addon.
     */
    public Map<Material, Map<String, UpgradeTier>> getAddonBlockLimitsUpgradeTierMap(String addon) {
        return customBlockLimitsUpgradeTierMap.getOrDefault(addon, Collections.emptyMap());
    }

    /**
     * Retrieves all materials with block limits upgrades.
     *
     * @return A set of materials with block limits upgrades.
     */
    public Set<Material> getMaterialsLimitsUpgrade() {
        Set<Material> materials = new HashSet<>();

        customBlockLimitsUpgradeTierMap.forEach((addon, addonUpgrade) -> {
            materials.addAll(addonUpgrade.keySet());
        });
        materials.addAll(blockLimitsUpgradeTierMap.keySet());

        return materials;
    }

    /**
     * Retrieves the material icon for a specific entity type.
     *
     * @param entity The entity type.
     * @return The material icon associated with the entity type, or null if not defined.
     */
    public Material getEntityIcon(EntityType entity) {
        return entityIcon.getOrDefault(entity, null);
    }

    /**
     * Retrieves the material icon for a specific entity group.
     *
     * @param group The entity group.
     * @return The material icon associated with the entity group, or null if not defined.
     */
    public Material getEntityGroupIcon(String group) {
        return entityGroupIcon.getOrDefault(group, null);
    }

    /**
     * Gets the maximum entity limits upgrade for a specific entity type and addon.
     *
     * @param entity The entity type.
     * @param addon  The name of the addon.
     * @return The maximum entity limit for the entity type.
     */
    public int getMaxEntityLimitsUpgrade(EntityType entity, String addon) {
        return customMaxEntityLimitsUpgrade.getOrDefault(addon, maxEntityLimitsUpgrade).getOrDefault(entity, 0);
    }

    /**
     * Gets the maximum entity group limits upgrade for a specific group and addon.
     *
     * @param group The entity group.
     * @param addon The name of the addon.
     * @return The maximum entity group limit for the group.
     */
    public int getMaxEntityGroupLimitsUpgrade(String group, String addon) {
        return customMaxEntityGroupLimitsUpgrade.getOrDefault(addon, maxEntityGroupLimitsUpgrade).getOrDefault(group,
                0);
    }

    /**
     * Retrieves the default entity limits upgrade tier map.
     *
     * @return A map of entity limits upgrade tiers by entity type.
     */
    public Map<EntityType, Map<String, UpgradeTier>> getDefaultEntityLimitsUpgradeTierMap() {
        return entityLimitsUpgradeTierMap;
    }

    /**
     * Retrieves the default entity group limits upgrade tier map.
     *
     * @return A map of entity group limits upgrade tiers.
     */
    public Map<String, Map<String, UpgradeTier>> getDefaultEntityGroupLimitsUpgradeTierMap() {
        return entityGroupLimitsUpgradeTierMap;
    }

    /**
     * Retrieves the entity limits upgrade tier map for a specific addon.
     *
     * @param addon The name of the addon.
     * @return A map of entity limits upgrade tiers specific to the addon.
     */
    public Map<EntityType, Map<String, UpgradeTier>> getAddonEntityLimitsUpgradeTierMap(String addon) {
        return customEntityLimitsUpgradeTierMap.getOrDefault(addon, Collections.emptyMap());
    }

    /**
     * Retrieves the entity group limits upgrade tier map for a specific addon.
     *
     * @param addon The name of the addon.
     * @return A map of entity group limits upgrade tiers specific to the addon.
     */
    public Map<String, Map<String, UpgradeTier>> getAddonEntityGroupLimitsUpgradeTierMap(String addon) {
        return customEntityGroupLimitsUpgradeTierMap.getOrDefault(addon, Collections.emptyMap());
    }

    /**
     * Retrieves all entity types with limits upgrades.
     *
     * @return A set of entity types with limits upgrades.
     */
    public Set<EntityType> getEntityLimitsUpgrade() {
        Set<EntityType> entity = new HashSet<>();

        customEntityLimitsUpgradeTierMap.forEach((addon, addonUpgrade) -> {
            entity.addAll(addonUpgrade.keySet());
        });
        entity.addAll(entityLimitsUpgradeTierMap.keySet());

        return entity;
    }

    /**
     * Retrieves all entity groups with limits upgrades.
     *
     * @return A set of entity groups with limits upgrades.
     */
    public Set<String> getEntityGroupLimitsUpgrade() {
        Set<String> groups = new HashSet<>();

        customEntityGroupLimitsUpgradeTierMap.forEach((addon, addonUpgrade) -> {
            groups.addAll(addonUpgrade.keySet());
        });
        groups.addAll(entityGroupLimitsUpgradeTierMap.keySet());

        return groups;
    }

    /**
     * Retrieves the maximum command upgrade limit for a specific command and addon.
     *
     * @param commandUpgrade The command identifier.
     * @param addon          The name of the addon.
     * @return The maximum upgrade limit for the command.
     */
    public int getMaxCommandUpgrade(String commandUpgrade, String addon) {
        if (customMaxCommandUpgrade.containsKey(addon)) {
            if (customMaxCommandUpgrade.get(addon).containsKey(commandUpgrade)) {
                return customMaxCommandUpgrade.get(addon).get(commandUpgrade);
            }
        }
        return maxCommandUpgrade.getOrDefault(commandUpgrade, 0);
    }

    /**
     * Retrieves the default command upgrade tier map.
     *
     * @return A map of command upgrade tiers by command identifier.
     */
    public Map<String, Map<String, CommandUpgradeTier>> getDefaultCommandUpgradeTierMap() {
        return commandUpgradeTierMap;
    }

    /**
     * Retrieves the command upgrade tier map for a specific addon.
     *
     * @param addon The name of the addon.
     * @return A map of command upgrade tiers specific to the addon.
     */
    public Map<String, Map<String, CommandUpgradeTier>> getAddonCommandUpgradeTierMap(String addon) {
        return customCommandUpgradeTierMap.getOrDefault(addon, Collections.emptyMap());
    }

    /**
     * Retrieves all commands with upgrades.
     *
     * @return A set of command identifiers with upgrades.
     */
    public Set<String> getCommandUpgrade() {
        Set<String> command = new HashSet<>();

        customCommandUpgradeTierMap.forEach((addon, addonUpgrade) -> {
            command.addAll(addonUpgrade.keySet());
        });
        command.addAll(commandUpgradeTierMap.keySet());

        return command;
    }

    /**
     * Retrieves the material icon for a specific command.
     *
     * @param command The command identifier.
     * @return The material icon associated with the command, or null if not defined.
     */
    public Material getCommandIcon(String command) {
        return commandIcon.getOrDefault(command, null);
    }

    /**
     * Retrieves the display name for a specific command.
     *
     * @param command The command identifier.
     * @return The display name of the command.
     */
    public String getCommandName(String command) {
        return commandName.get(command);
    }

    /**
     * Represents an upgrade tier for a specific feature.
     */
    public class UpgradeTier {
        /**
         * The unique identifier for the upgrade tier.
         */
        private final String id;

        /**
         * The maximum level of the upgrade tier.
         */
        private int maxLevel = -1;

        /**
         * The name of the upgrade tier.
         */
        private String tierName;

        /**
         * The permission level required for the upgrade.
         */
        private Integer permissionLevel = 0;

        /**
         * The expression defining the upgrade behavior.
         */
        private Expression upgrade;

        /**
         * Minimum island level required for the upgrade.
         */
        private Expression islandMinLevel;

        /**
         * Vault cost associated with the upgrade.
         */
        private Expression vaultCost;

        /**
         * Variables used in expressions for calculations.
         */
        private Map<String, Double> expressionVariables;

        /**
         * Creates a new UpgradeTier instance.
         *
         * @param id The unique identifier for the upgrade tier.
         */
        public UpgradeTier(String id) {
            this.id = id;
            this.expressionVariables = new TreeMap<>();
            this.expressionVariables.put("[level]", 0.0);
            this.expressionVariables.put("[islandLevel]", 0.0);
            this.expressionVariables.put("[numberPlayer]", 0.0);
        }

        /**
         * Retrieves the ID of the upgrade tier.
         *
         * @return The ID of the tier.
         */
        public String getId() {
            return id;
        }

        /**
         * Retrieves the name of the upgrade tier.
         *
         * @return The name of the upgrade tier.
         */
        public String getTierName() {
            return tierName;
        }

        /**
         * Sets the name of the upgrade tier.
         *
         * @param tierName The name to set for
         */
        public void setTierName(String tierName) {
            this.tierName = tierName;
        }

        /**
         * Retrieves the maximum level of the upgrade tier.
         *
         * @return The maximum level.
         */
        public int getMaxLevel() {
            return maxLevel;
        }

        /**
         * Sets the maximum level of the upgrade tier.
         *
         * @param maxLevel The maximum level to set.
         */
        public void setMaxLevel(int maxLevel) {
            this.maxLevel = maxLevel;
        }

        /**
         * Retrieves the permission level required for the upgrade tier.
         *
         * @return The permission level.
         */
        public Integer getPermissionLevel() {
            return permissionLevel;
        }

        /**
         * Sets the permission level required for the upgrade tier.
         *
         * @param permissionLevel The permission level to set.
         */
        public void setPermissionLevel(Integer permissionLevel) {
            this.permissionLevel = permissionLevel;
        }

        /**
         * Retrieves the upgrade expression.
         *
         * @return The upgrade expression.
         */
        public Expression getUpgrade() {
            return upgrade;
        }

        /**
         * Sets the upgrade expression.
         *
         * @param upgrade The upgrade expression to set.
         */
        public void setUpgrade(Expression upgrade) {
            this.upgrade = upgrade;
        }

        /**
         * Retrieves the minimum island level required for the upgrade.
         *
         * @return The island minimum level expression.
         */
        public Expression getIslandMinLevel() {
            return islandMinLevel;
        }

        /**
         * Sets the minimum island level required for the upgrade.
         *
         * @param islandMinLevel The island minimum level expression to set.
         */
        public void setIslandMinLevel(Expression islandMinLevel) {
            this.islandMinLevel = islandMinLevel;
        }

        /**
         * Retrieves the vault cost associated with the upgrade.
         *
         * @return The vault cost expression.
         */
        public Expression getVaultCost() {
            return vaultCost;
        }

        /**
         * Sets the vault cost associated with the upgrade.
         *
         * @param vaultCost The vault cost expression to set.
         */
        public void setVaultCost(Expression vaultCost) {
            this.vaultCost = vaultCost;
        }

        /**
         * Updates a variable used in the upgrade expression calculations.
         *
         * @param key   The variable name.
         * @param value The value to set for the variable.
         */
        public void updateExpressionVariable(String key, double value) {
            this.expressionVariables.put(key, value);
        }

        /**
         * Retrieves all variables used in the upgrade expression calculations.
         *
         * @return A map of variable names to their values.
         */
        public Map<String, Double> getExpressionVariable() {
            return expressionVariables;
        }

        /**
         * Calculates the upgrade value based on the provided parameters.
         *
         * @param level        The current level.
         * @param islandLevel  The island level.
         * @param numberPeople The number of players.
         * @return The calculated upgrade value.
         */
        public double calculateUpgrade(double level, double islandLevel, double numberPeople) {
            this.updateExpressionVariable("[level]", level);
            this.updateExpressionVariable("[islandLevel]", islandLevel);
            this.updateExpressionVariable("[numberPlayer]", numberPeople);
            return this.getUpgrade().eval();
        }

        /**
         * Calculates the minimum island level required based on the provided parameters.
         *
         * @param level        The current level.
         * @param islandLevel  The island level.
         * @param numberPeople The number of players.
         * @return The calculated minimum island level.
         */
        public double calculateIslandMinLevel(double level, double islandLevel, double numberPeople) {
            this.updateExpressionVariable("[level]", level);
            this.updateExpressionVariable("[islandLevel]", islandLevel);
            this.updateExpressionVariable("[numberPlayer]", numberPeople);
            return this.getIslandMinLevel().eval();
        }

        /**
         * Calculates the vault cost based on the provided parameters.
         *
         * @param level        The current level.
         * @param islandLevel  The island level.
         * @param numberPeople The number of players.
         * @return The calculated vault cost.
         */
        public double calculateVaultCost(double level, double islandLevel, double numberPeople) {
            this.updateExpressionVariable("[level]", level);
            this.updateExpressionVariable("[islandLevel]", islandLevel);
            this.updateExpressionVariable("[numberPlayer]", numberPeople);
            return this.getVaultCost().eval();
        }
    }

    /**
     * Represents a command upgrade tier with additional properties.
     */
    public class CommandUpgradeTier extends UpgradeTier {
        /**
         * List of commands associated with this upgrade tier.
         */
        private List<String> commandList;

        /**
         * Indicates whether the commands should run on the console.
         */
        private Boolean console;

        /**
         * Creates a new CommandUpgradeTier instance.
         *
         * @param id The unique identifier for the upgrade tier.
         */
        public CommandUpgradeTier(String id) {
            super(id);
            this.commandList = new ArrayList<>();
        }

        /**
         * Sets whether the commands should run on the console.
         *
         * @param console True to run commands on the console, false otherwise.
         */
        public void setConsole(Boolean console) {
            this.console = console;
        }

        /**
         * Checks whether the commands should run on the console.
         *
         * @return True if commands run on the console, false otherwise.
         */
        public Boolean getConsole() {
            return console;
        }

        /**
         * Sets the list of commands for this upgrade tier.
         *
         * @param commandsList The list of commands to set.
         */
        public void setCommandList(List<String> commandsList) {
            this.commandList = commandsList;
        }

        /**
         * Retrieves the formatted list of commands for execution.
         *
         * @param playerName The name of the player executing the commands.
         * @param island     The island associated with the commands.
         * @param level      The current level of the upgrade.
         * @return A list of formatted commands ready for execution.
         */
        public List<String> getCommandList(String playerName, Island island, int level) {
            List<String> formattedList = new ArrayList<>(this.commandList.size());
            String owner = island.getPlugin().getPlayers().getName(island.getOwner());

            this.commandList.forEach(cmd -> {
                String formattedCmd = cmd.replace("[player]", playerName).replace("[level]", Integer.toString(level))
                        .replace("[owner]", owner);
                formattedList.add(formattedCmd);
            });
            return formattedList;
        }
    }



    // -------------------------------------------------------------------------
    // Section: Arithmetic expressions Parser
    // Thanks to Boann on StackOverflow
    // Link:
    // https://stackoverflow.com/questions/3422673/how-to-evaluate-a-math-expression-given-in-string-form
    // -------------------------------------------------------------------------

    @FunctionalInterface
    interface Expression {
        double eval();
    }

    private static final List<String> funct = List.of("sqrt", "sin", "cos", "tan");

    public static Expression parse(final String str, Map<String, Double> variables) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ')
                    nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            Expression parse() {
                nextChar();
                Expression x = parseExpression();
                if (pos < str.length())
                    throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            // | number | functionName factor | factor `^` factor

            Expression parseExpression() {
                Expression x = parseTerm();
                for (;;) {
                    if (eat('+')) {
                        Expression a = x, b = parseTerm();
                        x = (() -> a.eval() + b.eval());
                    } else if (eat('-')) {
                        Expression a = x, b = parseTerm();
                        x = (() -> a.eval() - b.eval());
                    } else
                        return x;
                }
            }

            Expression parseTerm() {
                Expression x = parseFactor();
                for (;;) {
                    if (eat('*')) {
                        Expression a = x, b = parseFactor();
                        x = (() -> a.eval() * b.eval());
                    } else if (eat('/')) {
                        Expression a = x, b = parseFactor();
                        x = (() -> a.eval() / b.eval());
                    } else
                        return x;
                }
            }

            Expression parseFactor() {
                if (eat('+'))
                    return parseFactor(); // unary plus
                if (eat('-')) {
                    Expression x = (() -> -parseFactor().eval());
                    return x; // unary minus
                }

                Expression x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.')
                        nextChar();
                    final Integer innerPos = Integer.valueOf(this.pos);
                    x = (() -> Double.parseDouble(str.substring(startPos, innerPos)));
                } else if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '[' || ch == ']') { // functions
                    while ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '[' || ch == ']')
                        nextChar();
                    String func = str.substring(startPos, this.pos);
                    if (funct.contains(func)) {
                        Expression a = parseFactor();
                        if (func.equals("sqrt"))
                            x = (() -> Math.sqrt(a.eval()));
                        else if (func.equals("sin"))
                            x = (() -> Math.sin(Math.toRadians(a.eval())));
                        else if (func.equals("cos"))
                            x = (() -> Math.cos(Math.toRadians(a.eval())));
                        else if (func.equals("tan"))
                            x = (() -> Math.tan(Math.toRadians(a.eval())));
                        else
                            throw new RuntimeException("Unknown function: " + func);
                    } else {
                        x = (() -> variables.get(func));
                    }
                } else {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }

                if (eat('^')) {
                    Expression a = x, b = parseFactor();
                    x = (() -> Math.pow(a.eval(), b.eval())); // exponentiation
                }

                return x;
            }
        }.parse();
    }


}