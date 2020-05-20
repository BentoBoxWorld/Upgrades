package world.bentobox.islandupgrades;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.database.Database;
import world.bentobox.bentobox.hooks.VaultHook;
import world.bentobox.islandupgrades.command.IslandUpgradesPlayerCommand;
import world.bentobox.islandupgrades.config.Settings;
import world.bentobox.islandupgrades.task.IslandUpgradesRangeUpgrade;
import world.bentobox.level.Level;

public class IslandUpgradesAddon extends Addon {

	@Override
	public void onLoad() {
		super.onLoad();
		this.saveDefaultConfig();
		this.settings = new Settings(this);
	}
	
	@Override
	public void onEnable() {
		if (this.getState().equals(State.DISABLED)) {
			this.logWarning("Island Upgrade Addon is not available or disabled!");
			return;
		}
		
		List<String> hookedGameModes = new ArrayList<>();
		
		getPlugin().getAddonsManager().getGameModeAddons().stream()
			.filter(g -> !settings.getDisabledGameModes().contains(g.getDescription().getName()))
			.forEach(g -> {
				if (g.getPlayerCommand().isPresent()) {
					
					new IslandUpgradesPlayerCommand(this, g.getPlayerCommand().get());
					
					this.hooked = true;
					hookedGameModes.add(g.getDescription().getName());
				}
			});
		
		if (this.hooked) {
			this.islandUpgradesManager = new IslandUpgradesManager(this);
			this.islandUpgradesManager.addGameModes(hookedGameModes);
			
			this.islandUpgradesRangeUpgrade = new IslandUpgradesRangeUpgrade(this);
			
			this.dataBase = new Database<>(this, IslandUpgradesData.class);
			this.islandUpgradesCache = new HashMap<>();
			
			Optional<Addon> level = this.getAddonByName("Level");
			
			if (!level.isPresent()) {
				this.logWarning("Level addon not found so Island Upgrade won't look for IslandLevel");
				this.levelAddon = null;
			} else
				this.levelAddon = (Level) level.get();
			
			Optional<VaultHook> vault = this.getPlugin().getVault();
			if (!vault.isPresent()) {
				this.logWarning("Vault plugin not found si Island Upgrade won't look for money");
				this.vault = null;
			} else
				this.vault = vault.get();
			
			this.log("Island upgrade addon enabled");
		} else {
			this.logError("Island upgrade addon could not hook into any GameMode ans so, will not do anythings");
			this.setState(State.DISABLED);
		}
	}
	
	@Override
	public void onDisable() {
		if (this.islandUpgradesCache != null)
			this.islandUpgradesCache.values().forEach(this.dataBase::saveObjectAsync);
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
	public IslandUpgradesManager getIslandUpgradesManager() {
		return islandUpgradesManager;
	}
	
	public IslandUpgradesRangeUpgrade getIslandUpgradesRangeUpgrade() {
		return this.islandUpgradesRangeUpgrade;
	}
	
	public Database<IslandUpgradesData> getDataBase() {
		return this.dataBase;
	}
	
	public IslandUpgradesData getIslandUpgradesLevel(@NonNull String targetIsland) {
		IslandUpgradesData islandUpgradesData = this.islandUpgradesCache.get(targetIsland);
		if (islandUpgradesData != null)
			return islandUpgradesData;
		IslandUpgradesData data = this.dataBase.objectExists(targetIsland) ?
			Optional.ofNullable(this.dataBase.loadObject(targetIsland)).orElse(new IslandUpgradesData(targetIsland)) :
			new IslandUpgradesData(targetIsland);
		this.islandUpgradesCache.put(targetIsland, data);
		return data;
	}
	
	public void uncacheIsland(@Nullable String targetIsland, boolean save) {
		IslandUpgradesData data = this.islandUpgradesCache.remove(targetIsland);
		if (data == null)
			return;
		if (save)
			this.dataBase.saveObjectAsync(data);
	}
	
	public Level getLevelAddon() {
		return this.levelAddon;
	}
	
	public VaultHook getVaultHook() {
		return this.vault;
	}
	
	public boolean isLevelProvided() {
		return this.levelAddon != null;
	}
	
	public boolean isVaultProvided() {
		return this.vault != null;
	}

	private Settings settings;
	
	private boolean hooked;
	
	private IslandUpgradesManager islandUpgradesManager;
	
	private IslandUpgradesRangeUpgrade islandUpgradesRangeUpgrade;
	
	private Database<IslandUpgradesData> dataBase;
	
	private Map<String, IslandUpgradesData> islandUpgradesCache;
	
	private Level levelAddon;
	
	private VaultHook vault;
	
}
