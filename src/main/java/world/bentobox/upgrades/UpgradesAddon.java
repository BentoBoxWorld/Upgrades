package world.bentobox.upgrades;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bukkit.Material;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.flags.Flag;
import world.bentobox.bentobox.api.flags.clicklisteners.CycleClick;
import world.bentobox.bentobox.database.Database;
import world.bentobox.bentobox.hooks.VaultHook;
import world.bentobox.bentobox.managers.RanksManager;
import world.bentobox.level.Level;
import world.bentobox.limits.Limits;
import world.bentobox.upgrades.api.Upgrade;
import world.bentobox.upgrades.command.PlayerUpgradeCommand;
import world.bentobox.upgrades.config.Settings;
import world.bentobox.upgrades.dataobjects.UpgradesData;
import world.bentobox.upgrades.listeners.IslandChangeListener;
import world.bentobox.upgrades.upgrades.BlockLimitsUpgrade;
import world.bentobox.upgrades.upgrades.CommandUpgrade;
import world.bentobox.upgrades.upgrades.EntityGroupLimitsUpgrade;
import world.bentobox.upgrades.upgrades.EntityLimitsUpgrade;
import world.bentobox.upgrades.upgrades.RangeUpgrade;

/**
 * Main addon class for the Upgrades addon.
 * This addon allows islands to purchase various upgrades including range extensions,
 * entity/block limits, and execute commands upon upgrade purchase.
 *
 * @author BONNe
 * @since 1.0.0
 */
public class UpgradesAddon extends Addon {

    /**
     * The addon settings loaded from config.yml
     */
    private Settings settings;

    /**
     * Whether the addon successfully hooked into at least one game mode
     */
    private boolean hooked;

    /**
     * Manager for handling upgrade operations
     */
    private UpgradesManager upgradesManager;

    /**
     * Set of all registered upgrades available in the addon
     */
    private Set<Upgrade> upgrade = new HashSet<>();

    /**
     * Database for storing and loading upgrade data
     */
    private Database<UpgradesData> database = new Database<>(this, UpgradesData.class);

    /**
     * Cache of upgrade data mapped by island unique ID for performance
     */
    private Map<String, UpgradesData> upgradesCache = new HashMap<>();

    /**
     * Reference to the Level addon for island level requirements
     */
    private Level levelAddon;

    /**
     * Reference to the Limits addon for entity/block limit upgrades
     */
    private Limits limitsAddon;

    /**
     * Reference to the Vault hook for economy-based upgrade costs
     */
    private VaultHook vault;

    /**
     * Protection flag that determines the minimum rank required to use upgrades.
     * Default is MEMBER_RANK, can be cycled up to OWNER_RANK.
     */
    public final static Flag UPGRADES_RANK_RIGHT = new Flag.Builder("UPGRADES_RANK_RIGHT", Material.GOLD_INGOT)
            .type(Flag.Type.PROTECTION).mode(Flag.Mode.BASIC)
            .clickHandler(new CycleClick("UPGRADES_RANK_RIGHT", RanksManager.MEMBER_RANK, RanksManager.OWNER_RANK))
            .defaultRank(RanksManager.MEMBER_RANK).build();

    /**
     * Executes when the addon is loaded.
     * Saves the default configuration file and initializes settings.
     */
    @Override
    public void onLoad() {
        super.onLoad();
        this.saveDefaultConfig();
        this.settings = new Settings(this);
    }

    /**
     * Executes when the addon is enabled.
     * Initializes the addon by:
     * <ul>
     *   <li>Hooking into available game modes that are not disabled</li>
     *   <li>Registering player commands for each game mode</li>
     *   <li>Setting up the upgrades manager</li>
     *   <li>Connecting to Level, Limits, and Vault addons/plugins if available</li>
     *   <li>Registering all configured upgrades (entity limits, block limits, range, commands)</li>
     *   <li>Registering event listeners</li>
     *   <li>Registering protection flags</li>
     * </ul>
     * If no game modes can be hooked, the addon disables itself.
     */
    @Override
    public void onEnable() {
        if (this.getState().equals(State.DISABLED)) {
            this.logWarning("Upgrades Addon is not available or disabled!");
            return;
        }

        List<String> hookedGameModes = new ArrayList<>();

        getPlugin().getAddonsManager().getGameModeAddons().stream()
        .filter(g -> !settings.getDisabledGameModes().contains(g.getDescription().getName()))
        .forEach(g -> {
            if (g.getPlayerCommand().isPresent()) {

                new PlayerUpgradeCommand(this, g.getPlayerCommand().get());

                UpgradesAddon.UPGRADES_RANK_RIGHT.addGameModeAddon(g);

                this.hooked = true;
                hookedGameModes.add(g.getDescription().getName());
            }
        });

        if (this.hooked) {
            this.upgradesManager = new UpgradesManager(this);
            this.upgradesManager.addGameModes(hookedGameModes);

            Optional<Addon> level = this.getAddonByName("Level");

            if (!level.isPresent()) {
                this.logWarning("Level addon not found so Upgrades won't look for Island Level");
                this.levelAddon = null;
            } else
                this.levelAddon = (Level) level.get();

            Optional<Addon> limits = this.getAddonByName("Limits");

            if (!limits.isPresent()) {
                this.logWarning("Limits addon not found so Island Upgrade won't look for IslandLevel");
                this.limitsAddon = null;
            } else
                this.limitsAddon = (Limits) limits.get();

            Optional<VaultHook> vault = this.getPlugin().getVault();
            if (!vault.isPresent()) {
                this.logWarning("Vault plugin not found so Upgrades won't look for money");
                this.vault = null;
            } else
                this.vault = vault.get();

            if (this.isLimitsProvided()) {
                this.getSettings().getEntityLimitsUpgrade().forEach(ent -> this.registerUpgrade(new EntityLimitsUpgrade(this, ent)));
                this.getSettings().getEntityGroupLimitsUpgrade().forEach(group -> this.registerUpgrade(new EntityGroupLimitsUpgrade(this, group)));
                this.getSettings().getMaterialsLimitsUpgrade().forEach(mat -> this.registerUpgrade(new BlockLimitsUpgrade(this, mat)));
            }

            this.getSettings().getCommandUpgrade().forEach(cmd -> this.registerUpgrade(new CommandUpgrade(this, cmd, this.getSettings().getCommandIcon(cmd))));

            if (this.getSettings().getHasRangeUpgrade())
                this.registerUpgrade(new RangeUpgrade(this));

            this.registerListener(new IslandChangeListener(this));

            //if (this.isLimitsProvided())
            //this.registerListener(new JoinPermCheckListener(this));

            getPlugin().getFlagsManager().registerFlag(UpgradesAddon.UPGRADES_RANK_RIGHT);

            this.log("Upgrades addon enabled");
        } else {
            this.logError("Upgrades addon could not hook into any GameMode and therefore will not do anything");
            this.setState(State.DISABLED);
        }
    }

    /**
     * Executes when the addon is disabled.
     * Saves all cached upgrade data to the database asynchronously.
     */
    @Override
    public void onDisable() {
        if (this.upgradesCache != null)
            this.upgradesCache.values().forEach(this.database::saveObjectAsync);
    }

    /**
     * Executes when the addon is reloaded.
     * Reloads settings from config.yml if the addon is hooked to a game mode.
     */
    @Override
    public void onReload() {
        super.onReload();

        if (this.hooked)
            this.settings = new Settings(this);
        this.log("Island upgrade addon reloaded");
    }

    /**
     * Gets the addon settings.
     *
     * @return the settings loaded from config.yml
     */
    public Settings getSettings() {
        return settings;
    }

    /**
     * Gets the upgrades manager.
     *
     * @return the manager responsible for handling upgrade operations
     */
    public UpgradesManager getUpgradesManager() {
        return upgradesManager;
    }

    /**
     * Gets the database instance for upgrade data.
     *
     * @return the database for storing and loading {@link UpgradesData}
     */
    public Database<UpgradesData> getDatabase() {
        return this.database;
    }

    /**
     * Gets the upgrade data for a specific island.
     * If the data is cached, it returns the cached version.
     * Otherwise, it loads from the database or creates a new instance.
     * The loaded data is then cached for future access.
     *
     * @param targetIsland the unique ID of the island
     * @return the upgrade data for the island
     */
    public UpgradesData getUpgradesLevels(@NonNull String targetIsland) {
        UpgradesData upgradesData = this.upgradesCache.get(targetIsland);
        if (upgradesData != null)
            return upgradesData;
        UpgradesData data = this.database.objectExists(targetIsland) ?
                Optional.ofNullable(this.database.loadObject(targetIsland)).orElse(new UpgradesData(targetIsland)) :
                    new UpgradesData(targetIsland);
        this.upgradesCache.put(targetIsland, data);
        return data;
    }

    /**
     * Removes an island's upgrade data from the cache.
     * Optionally saves the data to the database before removing it.
     *
     * @param targetIsland the unique ID of the island to uncache
     * @param save whether to save the data to the database before removing from cache
     */
    public void uncacheIsland(@Nullable String targetIsland, boolean save) {
        UpgradesData data = this.upgradesCache.remove(targetIsland);
        if (data == null)
            return;
        if (save)
            this.database.saveObjectAsync(data);
    }

    /**
     * Gets the Level addon instance.
     *
     * @return the Level addon, or null if not available
     */
    public Level getLevelAddon() {
        return this.levelAddon;
    }

    /**
     * Gets the Limits addon instance.
     *
     * @return the Limits addon, or null if not available
     */
    public Limits getLimitsAddon() {
        return this.limitsAddon;
    }

    /**
     * Gets the Vault hook instance.
     *
     * @return the Vault hook for economy operations, or null if not available
     */
    public VaultHook getVaultHook() {
        return this.vault;
    }

    /**
     * Checks if the Level addon is available.
     *
     * @return true if Level addon is present and hooked, false otherwise
     */
    public boolean isLevelProvided() {
        return this.levelAddon != null;
    }

    /**
     * Checks if the Limits addon is available.
     *
     * @return true if Limits addon is present and hooked, false otherwise
     */
    public boolean isLimitsProvided() {
        return this.limitsAddon != null;
    }

    /**
     * Checks if Vault is available.
     *
     * @return true if Vault is present and hooked, false otherwise
     */
    public boolean isVaultProvided() {
        return this.vault != null;
    }

    /**
     * Gets all registered upgrades available in the addon.
     *
     * @return an unmodifiable set of all available upgrades
     */
    public Set<Upgrade> getAvailableUpgrades() {
        return this.upgrade;
    }

    /**
     * Registers a new upgrade to the addon.
     * This allows the upgrade to be available for purchase by islands.
     *
     * @param upgrade the upgrade to register
     */
    public void registerUpgrade(Upgrade upgrade) {
        this.upgrade.add(upgrade);
    }

}