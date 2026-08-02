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
import world.bentobox.upgrades.dataobjects.FormulaVariables;

/**
 * Represents the settings and configurations for the UpgradesAddon.
 * Handles configuration data parsing and storage, enabling upgrades and
 * managing limits for various game aspects like blocks, entities, and commands.
 */
public class Settings {

    private static final String BLOCK_LIMITS_UPGRADE = "block-limits-upgrade";
    private static final String ENTITY_LIMITS_UPGRADE = "entity-limits-upgrade";
    private static final String ENTITY_GROUP_LIMITS_UPGRADE = "entity-group-limits-upgrade";
    private static final String ISLAND_MIN_LEVEL = "island-min-level";
    private static final String VAULT_COST = "vault-cost";
    private static final String PERMISSION_LEVEL = "permission-level";

    /**
     * The UpgradesAddon instance associated with this settings object.
     */
    private final UpgradesAddon addon;

    /**
     * Set of game modes where the upgrades are disabled.
     */
    private final Set<String> disabledGameModes;

    /**
     * The escape string for chat input prompts.
     */
    private String chatInputEscape;



    /**
     * Default block limits upgrade tiers for each material.
     */
    private Map<Material, Map<String, UpgradeTier>> blockLimitsUpgradeTierMap = new EnumMap<>(Material.class);

    /**
     * Custom block limits upgrade tiers per game mode.
     */
    private final Map<String, Map<Material, Map<String, UpgradeTier>>> customBlockLimitsUpgradeTierMap = new TreeMap<>();


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
    private final Map<String, Map<EntityType, Map<String, UpgradeTier>>> customEntityLimitsUpgradeTierMap = new TreeMap<>();

    /**
     * Custom entity group limits upgrade tiers per game mode.
     */
    private final Map<String, Map<String, Map<String, UpgradeTier>>> customEntityGroupLimitsUpgradeTierMap = new TreeMap<>();


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

        this.disabledGameModes = new HashSet<>(this.addon.getConfig().getStringList("disabled-gamemodes"));

        this.chatInputEscape = this.addon.getConfig().getString("chat-input-escape", "END");

        loadBlockLimitsUpgrades();
        loadEntityLimitsUpgrades();
        loadEntityGroupLimitsUpgrades();
        loadGameModeOverrides();
    }

    private void loadBlockLimitsUpgrades() {
        if (this.addon.getConfig().isSet(BLOCK_LIMITS_UPGRADE)) {
            ConfigurationSection section = this.addon.getConfig().getConfigurationSection(BLOCK_LIMITS_UPGRADE);
            this.blockLimitsUpgradeTierMap = this.loadBlockLimits(section, null);
        }
    }

    private void loadEntityLimitsUpgrades() {
        if (this.addon.getConfig().isSet(ENTITY_LIMITS_UPGRADE)) {
            ConfigurationSection section = this.addon.getConfig().getConfigurationSection(ENTITY_LIMITS_UPGRADE);
            this.entityLimitsUpgradeTierMap = this.loadEntityLimits(section, null);
        }
    }

    private void loadEntityGroupLimitsUpgrades() {
        if (this.addon.getConfig().isSet(ENTITY_GROUP_LIMITS_UPGRADE)) {
            ConfigurationSection section = this.addon.getConfig()
                    .getConfigurationSection(ENTITY_GROUP_LIMITS_UPGRADE);
            this.entityGroupLimitsUpgradeTierMap = this.loadEntityGroupLimits(section, null);
        }
    }

    private void loadGameModeOverrides() {
        if (this.addon.getConfig().isSet("gamemodes")) {
            ConfigurationSection section = this.addon.getConfig().getConfigurationSection("gamemodes");

            for (String gameMode : Objects.requireNonNull(section).getKeys(false)) {
                ConfigurationSection gameModeSection = section.getConfigurationSection(gameMode);
                loadGameModeBlockLimitsUpgrades(gameMode, gameModeSection);
                loadGameModeEntityLimitsUpgrades(gameMode, gameModeSection);
                loadGameModeEntityGroupLimitsUpgrades(gameMode, gameModeSection);
            }
        }
    }

    private void loadGameModeBlockLimitsUpgrades(String gameMode, ConfigurationSection gameModeSection) {
        if (gameModeSection.isSet(BLOCK_LIMITS_UPGRADE)) {
            ConfigurationSection lowSection = gameModeSection.getConfigurationSection(BLOCK_LIMITS_UPGRADE);
            this.customBlockLimitsUpgradeTierMap.computeIfAbsent(gameMode,
                    k -> loadBlockLimits(lowSection, gameMode));
        }
    }

    private void loadGameModeEntityLimitsUpgrades(String gameMode, ConfigurationSection gameModeSection) {
        if (gameModeSection.isSet(ENTITY_LIMITS_UPGRADE)) {
            ConfigurationSection lowSection = gameModeSection.getConfigurationSection(ENTITY_LIMITS_UPGRADE);
            this.customEntityLimitsUpgradeTierMap.computeIfAbsent(gameMode,
                    k -> loadEntityLimits(lowSection, gameMode));
        }
    }

    private void loadGameModeEntityGroupLimitsUpgrades(String gameMode, ConfigurationSection gameModeSection) {
        if (gameModeSection.isSet(ENTITY_GROUP_LIMITS_UPGRADE)) {
            ConfigurationSection lowSection = gameModeSection
                    .getConfigurationSection(ENTITY_GROUP_LIMITS_UPGRADE);
            this.customEntityGroupLimitsUpgradeTierMap.computeIfAbsent(gameMode,
                    k -> loadEntityGroupLimits(lowSection, gameMode));
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
            if (ent != null) {
                Map<String, UpgradeTier> tier = new TreeMap<>();
                ConfigurationSection entSection = section.getConfigurationSection(entity);
                for (String key : Objects.requireNonNull(entSection).getKeys(false)) {
                    UpgradeTier newUpgrade = addUpgradeSection(entSection, key);
                    tier.put(key, newUpgrade);
                }
                ents.put(ent, tier);
            } else {
                this.addon.logError("Entity " + entity + " is not a valid entity. Skipping...");
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
                tier.put(key, newUpgrade);
            }
            ents.put(entitygroup, tier);
        }
        return ents;
    }

    @NonNull
    private UpgradeTier addUpgradeSection(ConfigurationSection section, String key) {
        ConfigurationSection tierSection = section.getConfigurationSection(key);
        UpgradeTier upgradeTier = new UpgradeTier(key);
        upgradeTier.setTierName(tierSection.getName());
        upgradeTier.setMaxLevel(tierSection.getInt("max-level"));
        upgradeTier.setUpgrade(parse(tierSection.getString("upgrade"), upgradeTier.getExpressionVariable()));

        if (tierSection.isSet(ISLAND_MIN_LEVEL))
            upgradeTier.setIslandMinLevel(
                    parse(tierSection.getString(ISLAND_MIN_LEVEL), upgradeTier.getExpressionVariable()));
        else
            upgradeTier.setIslandMinLevel(parse("0", upgradeTier.getExpressionVariable()));

        if (tierSection.isSet(VAULT_COST))
            upgradeTier.setVaultCost(parse(tierSection.getString(VAULT_COST), upgradeTier.getExpressionVariable()));
        else
            upgradeTier.setVaultCost(parse("0", upgradeTier.getExpressionVariable()));

        if (tierSection.isSet(PERMISSION_LEVEL))
            upgradeTier.setPermissionLevel(tierSection.getInt(PERMISSION_LEVEL));
        else
            upgradeTier.setPermissionLevel(0);

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
    
    public String getChatInputEscape() {
    	return this.chatInputEscape;
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
        private final Map<String, Double> expressionVariables;

        /**
         * Creates a new UpgradeTier instance.
         *
         * @param id The unique identifier for the upgrade tier.
         */
        public UpgradeTier(String id) {
            this.id = id;
            this.expressionVariables = new TreeMap<>();
            this.expressionVariables.put(FormulaVariables.LEVEL_VAR, 0.0);
            this.expressionVariables.put(FormulaVariables.ISLAND_LEVEL_VAR, 0.0);
            this.expressionVariables.put(FormulaVariables.NUMBER_PLAYER_VAR, 0.0);
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
            int pos = -1;
            int ch;

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
                    throw new FormulaParseException("Unexpected: " + (char) ch);
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
                        Expression a = x;
                        Expression b = parseTerm();
                        x = (() -> a.eval() + b.eval());
                    } else if (eat('-')) {
                        Expression a = x;
                        Expression b = parseTerm();
                        x = (() -> a.eval() - b.eval());
                    } else
                        return x;
                }
            }

            Expression parseTerm() {
                Expression x = parseFactor();
                for (;;) {
                    if (eat('*')) {
                        Expression a = x;
                        Expression b = parseFactor();
                        x = (() -> a.eval() * b.eval());
                    } else if (eat('/')) {
                        Expression a = x;
                        Expression b = parseFactor();
                        x = (() -> a.eval() / b.eval());
                    } else
                        return x;
                }
            }

            Expression parseFactor() {
                if (eat('+'))
                    return parseFactor(); // unary plus
                if (eat('-')) {
                    return (() -> -parseFactor().eval()); // unary minus
                }

                Expression x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                } else if (isNumberChar(ch)) { // numbers
                    x = parseNumber(startPos);
                } else if (isIdentifierStart(ch)) { // functions and variables
                    x = parseFunctionOrVariable(startPos);
                } else {
                    throw new FormulaParseException("Unexpected: " + (char) ch);
                }

                if (eat('^')) {
                    Expression a = x;
                    Expression b = parseFactor();
                    x = (() -> Math.pow(a.eval(), b.eval())); // exponentiation
                }

                return x;
            }

            boolean isNumberChar(int c) {
                return (c >= '0' && c <= '9') || c == '.';
            }

            boolean isIdentifierStart(int c) {
                return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '[' || c == ']';
            }

            Expression parseNumber(int startPos) {
                while ((ch >= '0' && ch <= '9') || ch == '.')
                    nextChar();
                final int innerPos = this.pos;
                return (() -> Double.parseDouble(str.substring(startPos, innerPos)));
            }

            Expression parseFunctionOrVariable(int startPos) {
                while ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '[' || ch == ']')
                    nextChar();
                String func = str.substring(startPos, this.pos);
                if (funct.contains(func)) {
                    return parseBuiltInFunction(func);
                } else {
                    return (() -> variables.get(func));
                }
            }

            Expression parseBuiltInFunction(String func) {
                Expression a = parseFactor();
                return switch (func) {
                    case "sqrt" -> (() -> Math.sqrt(a.eval()));
                    case "sin" -> (() -> Math.sin(Math.toRadians(a.eval())));
                    case "cos" -> (() -> Math.cos(Math.toRadians(a.eval())));
                    case "tan" -> (() -> Math.tan(Math.toRadians(a.eval())));
                    default -> throw new FormulaParseException("Unknown function: " + func);
                };
            }
        }.parse();
    }

    /**
     * Evaluate a formula string with the given variables and return the result.
     *
     * @param equation   The formula string (e.g. "100*[level]")
     * @param variables  Variable bindings (e.g. "[level]" -> 5.0)
     * @return The evaluated result as a double
     */
    public static double evaluate(String equation, Map<String, Double> variables) {
        return parse(equation, variables).eval();
    }

}