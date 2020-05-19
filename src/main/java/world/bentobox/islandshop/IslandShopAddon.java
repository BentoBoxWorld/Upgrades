package world.bentobox.islandshop;

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
import world.bentobox.islandshop.command.IslandShopPlayerCommand;
import world.bentobox.islandshop.config.Settings;
import world.bentobox.islandshop.task.IslandShopRangeUpgrade;
import world.bentobox.level.Level;

public class IslandShopAddon extends Addon {
	
	@Override
	public void onLoad() {
		super.onLoad();
		this.saveDefaultConfig();
		this.settings = new Settings(this);
	}
	
	@Override
	public void onEnable() {
		if (this.getState().equals(State.DISABLED)) {
			this.logWarning("Island Shop Addon is not available or disabled!");
			return;
		}
		
		List<String> hookedGameModes = new ArrayList<>();
		
		getPlugin().getAddonsManager().getGameModeAddons().stream()
			.filter(g -> !settings.getDisabledGameModes().contains(g.getDescription().getName()))
			.forEach(g -> {
				if (g.getPlayerCommand().isPresent()) {
					
					new IslandShopPlayerCommand(this, g.getPlayerCommand().get());
					
					this.hooked = true;
					hookedGameModes.add(g.getDescription().getName());
				}
			});
		
		if (this.hooked) {
			this.islandShopManager = new IslandShopManager(this);
			this.islandShopManager.addGameModes(hookedGameModes);
			
			this.islandShopRangeUpgrade = new IslandShopRangeUpgrade(this);
			
			this.dataBase = new Database<>(this, IslandShopData.class);
			this.islandShopCache = new HashMap<>();
			
			Optional<Addon> level = this.getAddonByName("Level");
			
			if (!level.isPresent()) {
				this.logWarning("Level addon not found so Island Shop won't look for IslandLevel");
				this.levelAddon = null;
			} else
				this.levelAddon = (Level) level.get();
			
			Optional<VaultHook> vault = this.getPlugin().getVault();
			if (!vault.isPresent()) {
				this.logWarning("Vault plugin not found si Island Shop won't look for money");
				this.vault = null;
			} else
				this.vault = vault.get();
			
			this.log("Island Shop addon enabled");
		} else {
			this.logError("Island Shop addon could not hook into any GameMode ans so, will not do anythings");
			this.setState(State.DISABLED);
		}
	}
	
	@Override
	public void onDisable() {
		if (this.islandShopCache != null)
			this.islandShopCache.values().forEach(this.dataBase::saveObjectAsync);
	}
	
	@Override
	public void onReload() {
		super.onReload();
		
		if (this.hooked)
			this.settings = new Settings(this);
			this.log("Island Shop addon reloaded");
	}
	
	/**
	 * @return the settings
	 */
	public Settings getSettings() {
		return settings;
	}

	/**
	 * @return the islandShopManager
	 */
	public IslandShopManager getIslandShopManager() {
		return islandShopManager;
	}
	
	public IslandShopRangeUpgrade getIslandShopRangeUpgrade() {
		return this.islandShopRangeUpgrade;
	}
	
	public Database<IslandShopData> getDataBase() {
		return this.dataBase;
	}
	
	public IslandShopData getIslandShopLevel(@NonNull String targetIsland) {
		IslandShopData islandShopData = this.islandShopCache.get(targetIsland);
		if (islandShopData != null)
			return islandShopData;
		IslandShopData data = this.dataBase.objectExists(targetIsland) ?
			Optional.ofNullable(this.dataBase.loadObject(targetIsland)).orElse(new IslandShopData(targetIsland)) :
			new IslandShopData(targetIsland);
		this.islandShopCache.put(targetIsland, data);
		return data;
	}
	
	public void uncacheIsland(@Nullable String targetIsland, boolean save) {
		IslandShopData data = this.islandShopCache.remove(targetIsland);
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
	
	private IslandShopManager islandShopManager;
	
	private IslandShopRangeUpgrade islandShopRangeUpgrade;
	
	private Database<IslandShopData> dataBase;
	
	private Map<String, IslandShopData> islandShopCache;
	
	private Level levelAddon;
	
	private VaultHook vault;

}
