package world.bentobox.upgrades;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.database.Database;
import world.bentobox.bentobox.hooks.VaultHook;
import world.bentobox.upgrades.api.Upgrade;
import world.bentobox.upgrades.command.PlayerUpgradeCommand;
import world.bentobox.upgrades.config.Settings;
import world.bentobox.upgrades.dataobjects.UpgradesData;
import world.bentobox.upgrades.listeners.IslandChangeListener;
import world.bentobox.upgrades.listeners.JoinPermCheckListener;
import world.bentobox.upgrades.upgrades.BlockLimitsUpgrade;
import world.bentobox.upgrades.upgrades.CommandUpgrade;
import world.bentobox.upgrades.upgrades.EntityLimitsUpgrade;
import world.bentobox.upgrades.upgrades.RangeUpgrade;
import world.bentobox.level.Level;
import world.bentobox.limits.Limits;

public class UpgradesAddon extends Addon {

	@Override
	public void onLoad() {
		super.onLoad();
		this.saveDefaultConfig();
		this.settings = new Settings(this);
	}
	
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
					
					this.hooked = true;
					hookedGameModes.add(g.getDescription().getName());
				}
			});
		
		if (this.hooked) {
			this.upgradesManager = new UpgradesManager(this);
			this.upgradesManager.addGameModes(hookedGameModes);
			
			this.upgrade = new HashSet<>();
			
			this.database = new Database<>(this, UpgradesData.class);
			this.upgradesCache = new HashMap<>();
			
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
				this.getSettings().getMaterialsLimitsUpgrade().forEach(mat -> this.registerUpgrade(new BlockLimitsUpgrade(this, mat)));
			}
			
			this.getSettings().getCommandUpgrade().forEach(cmd -> this.registerUpgrade(new CommandUpgrade(this, cmd, this.getSettings().getCommandIcon(cmd))));
			
			if (this.getSettings().getHasRangeUpgrade())
				this.registerUpgrade(new RangeUpgrade(this));
			
			this.registerListener(new IslandChangeListener(this));
			
			if (this.isLimitsProvided())
				this.registerListener(new JoinPermCheckListener());
			
			this.log("Upgrades addon enabled");
		} else {
			this.logError("Upgrades addon could not hook into any GameMode and therefore will not do anything");
			this.setState(State.DISABLED);
		}
	}
	
	@Override
	public void onDisable() {
		if (this.upgradesCache != null)
			this.upgradesCache.values().forEach(this.database::saveObjectAsync);
	}
	
	@Override
	public void onReload() {
		super.onReload();
		
		if (this.hooked)
			this.settings = new Settings(this);
			this.log("Island upgrade addon reloaded");
	}
	
	/**
	 * @return the settings
	 */
	public Settings getSettings() {
		return settings;
	}

	/**
	 * @return the islandUpgradesManager
	 */
	public UpgradesManager getUpgradesManager() {
		return upgradesManager;
	}
	
	public Database<UpgradesData> getDatabase() {
		return this.database;
	}
	
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
	
	public void uncacheIsland(@Nullable String targetIsland, boolean save) {
		UpgradesData data = this.upgradesCache.remove(targetIsland);
		if (data == null)
			return;
		if (save)
			this.database.saveObjectAsync(data);
	}
	
	public Level getLevelAddon() {
		return this.levelAddon;
	}

	public Limits getLimitsAddon() {
		return this.limitsAddon;
	}
	
	public VaultHook getVaultHook() {
		return this.vault;
	}
	
	public boolean isLevelProvided() {
		return this.levelAddon != null;
	}

	public boolean isLimitsProvided() {
		return this.limitsAddon != null;
	}
	
	public boolean isVaultProvided() {
		return this.vault != null;
	}
	
	public Set<Upgrade> getAvailableUpgrades() {
		return this.upgrade;
	}
	
	public void registerUpgrade(Upgrade upgrade) {
		this.upgrade.add(upgrade);
	}

	private Settings settings;
	
	private boolean hooked;
	
	private UpgradesManager upgradesManager;
	
	private Set<Upgrade> upgrade;
	
	private Database<UpgradesData> database;
	
	private Map<String, UpgradesData> upgradesCache;
	
	private Level levelAddon;

	private Limits limitsAddon;
	
	private VaultHook vault;
	
}