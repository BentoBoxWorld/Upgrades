package world.bentobox.upgrades.dataobjects;

import java.util.Map;
import java.util.TreeMap;

import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.upgrades.UpgradesAddon;

/**
 * Placeholders available to the configurable price and reward formulas, and the
 * canonical way to build the substitution map for an island.
 */
public final class FormulaVariables {

    public static final String LEVEL_VAR = "[level]";
    public static final String ISLAND_LEVEL_VAR = "[islandLevel]";
    public static final String NUMBER_PLAYER_VAR = "[numberPlayer]";

    private FormulaVariables() {
        // Utility class
    }

    /**
     * Builds the formula substitution map for an island. A null island yields zeroed
     * island values rather than throwing, matching
     * {@link world.bentobox.upgrades.UpgradesManager#getIslandLevel(Island)}.
     *
     * @param addon The addon, used to look up the island level.
     * @param island The island the formula is being evaluated for, may be null.
     * @param currentLevel The current level of the upgrade being evaluated.
     * @return A mutable map of placeholder to value.
     */
    public static Map<String, Double> of(UpgradesAddon addon, Island island, int currentLevel) {
        Map<String, Double> variables = new TreeMap<>();
        variables.put(LEVEL_VAR, (double) currentLevel);
        variables.put(ISLAND_LEVEL_VAR, (double) addon.getUpgradesManager().getIslandLevel(island));
        variables.put(NUMBER_PLAYER_VAR, island == null ? 0d : (double) island.getMemberSet().size());
        return variables;
    }

    /**
     * Builds the formula substitution map from values already resolved by the caller.
     *
     * @param currentLevel The current level of the upgrade being evaluated.
     * @param islandLevel The island level.
     * @param memberCount The number of island members.
     * @return A mutable map of placeholder to value.
     */
    public static Map<String, Double> of(int currentLevel, long islandLevel, int memberCount) {
        Map<String, Double> variables = new TreeMap<>();
        variables.put(LEVEL_VAR, (double) currentLevel);
        variables.put(ISLAND_LEVEL_VAR, (double) islandLevel);
        variables.put(NUMBER_PLAYER_VAR, (double) memberCount);
        return variables;
    }

}
