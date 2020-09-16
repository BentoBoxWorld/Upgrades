package world.bentobox.upgrades.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Objects;
import org.bukkit.Bukkit;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.NonNull;

import world.bentobox.upgrades.UpgradesAddon;

public class Settings {
	
	public Settings(UpgradesAddon addon) {
		this.addon = addon;
		this.addon.saveDefaultConfig();
		
		this.hasRangeUpgrade = false;
		
		this.disabledGameModes = new HashSet<>(this.addon.getConfig().getStringList("disabled-gamemodes"));
        
        this.paymentItem = this.addon.getConfig().getItemStack("payment-item");
		
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
		
		if (this.addon.getConfig().isSet("entity-limits-upgrade")) {
			ConfigurationSection section = this.addon.getConfig().getConfigurationSection("entity-limits-upgrade");
			this.entityLimitsUpgradeTierMap = this.loadEntityLimits(section, null);
		}
		
		if (this.addon.getConfig().isSet("command-icon")) {
			ConfigurationSection section = this.addon.getConfig().getConfigurationSection("command-icon");
			for (String commandId: Objects.requireNonNull(section).getKeys(false)) {
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
						
						if (this.customMaxRangeUpgrade.get(gameMode) == null || this.customMaxRangeUpgrade.get(gameMode) < newUpgrade.getMaxLevel())
							this.customMaxRangeUpgrade.put(gameMode, newUpgrade.getMaxLevel());
						
						this.hasRangeUpgrade = true;
						
						this.customRangeUpgradeTierMap.computeIfAbsent(gameMode, k -> new HashMap<>()).put(key, newUpgrade);
					}
				}
				
				if (gameModeSection.isSet("block-limits-upgrade")) {
					ConfigurationSection lowSection = gameModeSection.getConfigurationSection("block-limits-upgrade");
					this.customBlockLimitsUpgradeTierMap.computeIfAbsent(gameMode, k -> loadBlockLimits(lowSection, gameMode));
				}
				
				if (gameModeSection.isSet("entity-limits-upgrade")) {
					ConfigurationSection lowSection = gameModeSection.getConfigurationSection("entity-limits-upgrade");
					this.customEntityLimitsUpgradeTierMap.computeIfAbsent(gameMode, k -> loadEntityLimits(lowSection, gameMode));
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
		for (String material: Objects.requireNonNull(section).getKeys(false)) {
			Material mat = Material.getMaterial(material);
			if (mat != null && mat.isBlock()) {
				Map<String, UpgradeTier> tier = new HashMap<>();
				ConfigurationSection matSection = section.getConfigurationSection(material);
				for (String key : Objects.requireNonNull(matSection).getKeys(false)) {
					UpgradeTier newUpgrade = addUpgradeSection(matSection, key);
					
					if (gameMode == null) {
						if (this.maxBlockLimitsUpgrade.get(mat) == null || this.maxBlockLimitsUpgrade.get(mat) < newUpgrade.getMaxLevel())
							this.maxBlockLimitsUpgrade.put(mat, newUpgrade.getMaxLevel());
					} else {
						if (this.customMaxBlockLimitsUpgrade.get(gameMode) == null) {
							Map<Material, Integer> newMap = new EnumMap<>(Material.class);
							newMap.put(mat, newUpgrade.getMaxLevel());
							this.customMaxBlockLimitsUpgrade.put(gameMode, newMap);
						} else {
							if (this.customMaxBlockLimitsUpgrade.get(gameMode).get(mat) == null || this.customMaxBlockLimitsUpgrade.get(gameMode).get(mat) < newUpgrade.getMaxLevel())
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
		for (String entity: Objects.requireNonNull(section).getKeys(false)) {
			EntityType ent = this.getEntityType(entity);
			if (ent != null && this.entityIcon.containsKey(ent)) {
				Map<String, UpgradeTier> tier = new HashMap<>();
				ConfigurationSection entSection = section.getConfigurationSection(entity);
				for (String key : Objects.requireNonNull(entSection).getKeys(false)) {
					UpgradeTier newUpgrade = addUpgradeSection(entSection, key);
					
					if (gameMode == null) {
						if (this.maxEntityLimitsUpgrade.get(ent) == null || this.maxEntityLimitsUpgrade.get(ent) < newUpgrade.getMaxLevel())
							this.maxEntityLimitsUpgrade.put(ent, newUpgrade.getMaxLevel());
					} else {
						if (this.customMaxEntityLimitsUpgrade.get(gameMode) == null) {
							Map<EntityType, Integer> newMap = new EnumMap<>(EntityType.class);
							newMap.put(ent, newUpgrade.getMaxLevel());
							this.customMaxEntityLimitsUpgrade.put(gameMode, newMap);
						} else {
							if (this.customMaxEntityLimitsUpgrade.get(gameMode).get(ent) == null || this.customMaxEntityLimitsUpgrade.get(gameMode).get(ent) < newUpgrade.getMaxLevel())
								this.customMaxEntityLimitsUpgrade.get(gameMode).put(ent, newUpgrade.getMaxLevel());
						}
					}

					tier.put(key, newUpgrade);
				}
				ents.put(ent, tier);
			} else {
				if (ent != null)
					this.addon.logError("Entity " + entity+ " is not a valid entity. Skipping...");
				else
					this.addon.logError("Entity " + entity+ " is missing a corresponding icon. Skipping...");
			}
		}
		return ents;
	}
	
	private Map<String, Map<String, CommandUpgradeTier>> loadCommand(ConfigurationSection section, String gamemode) {
		Map<String, Map<String, CommandUpgradeTier>> commands = new HashMap<>();
		
		for (String commandId: Objects.requireNonNull(section).getKeys(false)) {
			if (this.commandIcon.containsKey(commandId)) {
				String name = commandId;
				Map<String, CommandUpgradeTier> tier = new HashMap<>();
				ConfigurationSection cmdSection = section.getConfigurationSection(commandId);
				for (String key: Objects.requireNonNull(cmdSection).getKeys(false)) {
					if (key.equals("name")) {
						name = cmdSection.getString(key);
					} else {
						CommandUpgradeTier newUpgrade = addCommandUpgradeSection(cmdSection, key);
						
						if (gamemode == null) {
							if (this.maxCommandUpgrade.get(key) == null || this.maxCommandUpgrade.get(key) < newUpgrade.getMaxLevel())
								this.maxCommandUpgrade.put(key, newUpgrade.getMaxLevel());
						} else {
							if (this.customMaxCommandUpgrade.get(gamemode) == null) {
								Map<String, Integer> newMap = new HashMap<>();
								newMap.put(key, newUpgrade.getMaxLevel());
								this.customMaxCommandUpgrade.put(gamemode, newMap);
							} else {
								if (this.customMaxCommandUpgrade.get(gamemode).get(key) == null || this.customMaxCommandUpgrade.get(gamemode).get(key) < newUpgrade.getMaxLevel())
									this.customMaxCommandUpgrade.get(gamemode).put(key, newUpgrade.getMaxLevel());
							}
						}
						
						tier.put(key, newUpgrade);
					}
				}
				if (!this.commandName.containsKey(commandId) || name != commandId)
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
		upgradeTier.setUseVault(tierSection.getBoolean("use-vault"));
		upgradeTier.setUpgrade(parse(tierSection.getString("upgrade"), upgradeTier.getExpressionVariable()));
		
		if (tierSection.isSet("island-min-level"))
			upgradeTier.setIslandMinLevel(parse(tierSection.getString("island-min-level"), upgradeTier.getExpressionVariable()));
		else
			upgradeTier.setIslandMinLevel(parse("0", upgradeTier.getExpressionVariable()));
		
		if (tierSection.isSet("vault-cost"))
			upgradeTier.setVaultCost(parse(tierSection.getString("vault-cost"), upgradeTier.getExpressionVariable()));
		else
			upgradeTier.setVaultCost(parse("0", upgradeTier.getExpressionVariable()));
        
		if (tierSection.isSet("item-cost"))
			upgradeTier.setItemCost(parse(tierSection.getString("item-cost"), upgradeTier.getExpressionVariable()));
		else
			upgradeTier.setItemCost(parse("0", upgradeTier.getExpressionVariable()));
		
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
        upgradeTier.setUseVault(tierSection.getBoolean("use-vault"));
		upgradeTier.setUpgrade(parse("0", upgradeTier.getExpressionVariable()));
		
		if (tierSection.isSet("island-min-level"))
			upgradeTier.setIslandMinLevel(parse(tierSection.getString("island-min-level"), upgradeTier.getExpressionVariable()));
		else
			upgradeTier.setIslandMinLevel(parse("0", upgradeTier.getExpressionVariable()));
		
		if (tierSection.isSet("vault-cost"))
			upgradeTier.setVaultCost(parse(tierSection.getString("vault-cost"), upgradeTier.getExpressionVariable()));
		else
			upgradeTier.setVaultCost(parse("0", upgradeTier.getExpressionVariable()));
        
		if (tierSection.isSet("item-cost"))
			upgradeTier.setItemCost(parse(tierSection.getString("item-cost"), upgradeTier.getExpressionVariable()));
		else
			upgradeTier.setItemCost(parse("0", upgradeTier.getExpressionVariable()));
		
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
	 * @return the disabledGameModes
	 */
	public Set<String> getDisabledGameModes() {
		return disabledGameModes;
	}

    public ItemStack getPaymentItem() {
        return paymentItem;
    }
    
	public boolean getHasRangeUpgrade() {
		return this.hasRangeUpgrade;
	}
	
	public int getMaxRangeUpgrade(String addon) {
		return this.customMaxRangeUpgrade.getOrDefault(addon, this.maxRangeUpgrade);
	}
	
	public Map<String, UpgradeTier> getDefaultRangeUpgradeTierMap() {
		return this.rangeUpgradeTierMap;
	}
	
	/**
	 * @return the rangeUpgradeTierMap
	 */
	public Map<String, UpgradeTier> getAddonRangeUpgradeTierMap(String addon) {
		return this.customRangeUpgradeTierMap.getOrDefault(addon, Collections.emptyMap());
	}
	
	public int getMaxBlockLimitsUpgrade(Material mat, String addon) {
		return this.customMaxBlockLimitsUpgrade.getOrDefault(addon, this.maxBlockLimitsUpgrade).getOrDefault(mat, 0);
	}
	
	public Map<Material, Map<String, UpgradeTier>> getDefaultBlockLimitsUpgradeTierMap() {
		return this.blockLimitsUpgradeTierMap;
	}
	
	/**
	 * @return the rangeUpgradeTierMap
	 */
	public Map<Material, Map<String, UpgradeTier>> getAddonBlockLimitsUpgradeTierMap(String addon) {
		return this.customBlockLimitsUpgradeTierMap.getOrDefault(addon, Collections.emptyMap());
	}
	
	public Set<Material> getMaterialsLimitsUpgrade() {
		Set<Material> materials = new HashSet<>();
		
		this.customBlockLimitsUpgradeTierMap.forEach((addon, addonUpgrade) -> {
			materials.addAll(addonUpgrade.keySet());
		});
		materials.addAll(this.blockLimitsUpgradeTierMap.keySet());
		
		return materials;
	}
	
	public Material getEntityIcon(EntityType entity) {
		return this.entityIcon.getOrDefault(entity, null);
	}
	
	public int getMaxEntityLimitsUpgrade(EntityType entity, String addon) {
		return this.customMaxEntityLimitsUpgrade.getOrDefault(addon, this.maxEntityLimitsUpgrade).getOrDefault(entity, 0);
	}
	
	public Map<EntityType, Map<String, UpgradeTier>> getDefaultEntityLimitsUpgradeTierMap() {
		return this.entityLimitsUpgradeTierMap;
	}
	
	/**
	 * @return the rangeUpgradeTierMap
	 */
	public Map<EntityType, Map<String, UpgradeTier>> getAddonEntityLimitsUpgradeTierMap(String addon) {
		return this.customEntityLimitsUpgradeTierMap.getOrDefault(addon, Collections.emptyMap());
	}
	
	public Set<EntityType> getEntityLimitsUpgrade() {
		Set<EntityType> entity = new HashSet<>();
		
		this.customEntityLimitsUpgradeTierMap.forEach((addon, addonUpgrade) -> {
			entity.addAll(addonUpgrade.keySet());
		});
		entity.addAll(this.entityLimitsUpgradeTierMap.keySet());
		
		return entity;
	}
	
	private EntityType getEntityType(String key) {
        return Arrays.stream(EntityType.values()).filter(v -> v.name().equalsIgnoreCase(key)).findFirst().orElse(null);
    }
	
	public int getMaxCommandUpgrade(String commandUpgrade, String addon) {
		return this.customMaxCommandUpgrade.getOrDefault(addon, this.maxCommandUpgrade).getOrDefault(commandUpgrade, 0);
	}
	
	public Map<String, Map<String, CommandUpgradeTier>> getDefaultCommandUpgradeTierMap() {
		return this.commandUpgradeTierMap;
	}
	
	/**
	 * @return the rangeUpgradeTierMap
	 */
	public Map<String, Map<String, CommandUpgradeTier>> getAddonCommandUpgradeTierMap(String addon) {
		return this.customCommandUpgradeTierMap.getOrDefault(addon, Collections.emptyMap());
	}
	
	public Set<String> getCommandUpgrade() {
		Set<String> command = new HashSet<>();
		
		this.customCommandUpgradeTierMap.forEach((addon, addonUpgrade) -> {
			command.addAll(addonUpgrade.keySet());
		});
		command.addAll(this.commandUpgradeTierMap.keySet());
		
		return command;
	}
	
	public Material getCommandIcon(String command) {
		return this.commandIcon.getOrDefault(command, null);
	}
	
	public String getCommandName(String command) {
		return this.commandName.get(command);
	}

	private UpgradesAddon addon;
	
	private Set<String> disabledGameModes;
    
    private ItemStack paymentItem;
	
	private int maxRangeUpgrade = 0;
	
	private boolean hasRangeUpgrade;
	
	private Map<String, Integer> customMaxRangeUpgrade = new HashMap<>();
	
	private Map<String, UpgradeTier> rangeUpgradeTierMap = new HashMap<>();
	
	private Map<String, Map<String, UpgradeTier>> customRangeUpgradeTierMap = new HashMap<>();
	
	private Map<Material, Integer> maxBlockLimitsUpgrade = new EnumMap<>(Material.class);
	
	private Map<String, Map<Material, Integer>> customMaxBlockLimitsUpgrade = new HashMap<>();
	
	private Map<Material, Map<String, UpgradeTier>> blockLimitsUpgradeTierMap = new EnumMap<>(Material.class);
	
	private Map<String, Map<Material, Map<String, UpgradeTier>>> customBlockLimitsUpgradeTierMap = new HashMap<>();
	
	private Map<EntityType, Material> entityIcon = new EnumMap<>(EntityType.class); 
	
	private Map<EntityType, Integer> maxEntityLimitsUpgrade = new EnumMap<>(EntityType.class);
	
	private Map<String, Map<EntityType, Integer>> customMaxEntityLimitsUpgrade = new HashMap<>();
	
	private Map<EntityType, Map<String, UpgradeTier>> entityLimitsUpgradeTierMap = new EnumMap<>(EntityType.class);
	
	private Map<String, Map<EntityType, Map<String, UpgradeTier>>> customEntityLimitsUpgradeTierMap = new HashMap<>();
	
	private Map<String, Integer> maxCommandUpgrade = new HashMap<>();
	
	private Map<String, Map<String, Integer>> customMaxCommandUpgrade = new HashMap<>();
	
	private Map<String, Map<String, CommandUpgradeTier>> commandUpgradeTierMap = new HashMap<>();
	
	private Map<String, Map<String, Map<String, CommandUpgradeTier>>> customCommandUpgradeTierMap = new HashMap<>();
	
	private Map<String, Material> commandIcon = new HashMap<>();
	
	private Map<String, String> commandName = new HashMap<>();
	
	// ------------------------------------------------------------------
	// Section: Private object
	// ------------------------------------------------------------------
	
	public class UpgradeTier {
		/**
		 * Constructor UpgradeTier create a new UpgradeTier instance
		 * and set expressionVariables to default value
		 * 
		 * @param id
		 */
		public UpgradeTier(String id) {
			this.id = id;
			this.expressionVariables = new HashMap<>();
			this.expressionVariables.put("[level]", 0.0);
			this.expressionVariables.put("[islandLevel]", 0.0);
			this.expressionVariables.put("[numberPlayer]", 0.0);
			
		}
		
		// --------------------------------------------------------------
		// Section: Methods
		// --------------------------------------------------------------
		
		/**
		 * @return the id
		 */
		public String getId() {
			return id;
		}
		
		public String getTierName() {
			return this.tierName;
		}
		
		public void setTierName(String tierName) {
			this.tierName = tierName;
		}
		
		/**
		 * @return the maxLevel
		 */
		public int getMaxLevel() {
			return maxLevel;
		}

		/**
		 * @param maxLevel the maxLevel to set
		 */
		public void setMaxLevel(int maxLevel) {
			this.maxLevel = maxLevel;
		}
		
		/**
		 * @return the level of permission
		 */
		public Integer getPermissionLevel() {
			return this.permissionLevel;
		}
		
		/**
		 * @param level of permission to set
		 */
		public void setPermissionLevel(Integer level) {
			this.permissionLevel = level;
		}

        /**
         * @return whether vault or items should be used
         */
        public boolean isUseVault() {
            return useVault;
        }

        /**
         * @param usevault whether vault or items should be used
         */
        public void setUseVault(boolean usevault) {
            this.useVault = usevault;
        }

		/**
		 * @return the upgradeRange
		 */
		public Expression getUpgrade() {
			return upgrade;
		}

		/**
		 * @param upgradeRange the upgradeRange to set
		 */
		public void setUpgrade(Expression upgrade) {
			this.upgrade = upgrade;
		}

		/**
		 * @return the islandMinLevel
		 */
		public Expression getIslandMinLevel() {
			return islandMinLevel;
		}

		/**
		 * @param islandMinLevel the islandMinLevel to set
		 */
		public void setIslandMinLevel(Expression islandMinLevel) {
			this.islandMinLevel = islandMinLevel;
		}

		/**
		 * @return the vaultCost
		 */
		public Expression getVaultCost() {
			return vaultCost;
		}

		/**
		 * @param vaultCost the vaultCost to set
		 */
		public void setVaultCost(Expression vaultCost) {
			this.vaultCost = vaultCost;
		}
        
		/**
		 * @return the itemCost
		 */
		public Expression getItemCost() {
			return itemCost;
		}

		/**
		 * @param itemCost the itemCost to set
		 */
		public void setItemCost(Expression itemCost) {
			this.itemCost = itemCost;
		}
		
		/**
		 * Value to set for the math parser
		 * @param key
		 * @param value
		 */
		public void updateExpressionVariable(String key, double value) {
			this.expressionVariables.put(key, value);
		}
		
		public Map<String, Double> getExpressionVariable() {
			return expressionVariables;
		}
		
		public double calculateUpgrade(double level, double islandLevel, double numberPeople) {
			this.updateExpressionVariable("[level]", level);
			this.updateExpressionVariable("[islandLevel]", islandLevel);
			this.updateExpressionVariable("[numberPlayer]", numberPeople);
			return this.getUpgrade().eval();
		}
		
		public double calculateIslandMinLevel(double level, double islandLevel, double numberPeople) {
			this.updateExpressionVariable("[level]", level);
			this.updateExpressionVariable("[islandLevel]", islandLevel);
			this.updateExpressionVariable("[numberPlayer]", numberPeople);
			return this.getIslandMinLevel().eval();
		}
		
		public double calculateVaultCost(double level, double islandLevel, double numberPeople) {
			this.updateExpressionVariable("[level]", level);
			this.updateExpressionVariable("[islandLevel]", islandLevel);
			this.updateExpressionVariable("[numberPlayer]", numberPeople);
			return this.getVaultCost().eval();
		}
        
		public double calculateItemCost(double level, double islandLevel, double numberPeople) {
			this.updateExpressionVariable("[level]", level);
			this.updateExpressionVariable("[islandLevel]", islandLevel);
			this.updateExpressionVariable("[numberPlayer]", numberPeople);
			return this.getItemCost().eval();
		}


		// ----------------------------------------------------------------------
		// Section: Variables
		// ----------------------------------------------------------------------


		private final String id;

		private int maxLevel = -1;
		
		private String tierName;
		
		private Integer permissionLevel = 0;
        
        private boolean useVault;

		private Expression upgrade;
		
		private Expression islandMinLevel;
		
		private Expression vaultCost;

        private Expression itemCost;
		
		private Map<String, Double> expressionVariables;
	}
	
	public class CommandUpgradeTier extends UpgradeTier {
		
		public CommandUpgradeTier(String id) {
			super(id);
			this.commandList = new ArrayList<String>();
		}
		
		public void setConsole(Boolean console) {
			this.console = console;
		}
		
		public Boolean getConsole() {
			return this.console;
		}
		
		public void setCommandList(List<String> commandsList) {
			this.commandList = commandsList;
		}
		
		public List<String> getCommandList(String playerName, int level) {
			List<String> formatedList = new ArrayList<String>(this.commandList.size());
			
			this.commandList.forEach(cmd -> {
				String fcmd = cmd.replace("[player]", playerName)
						.replace("[level]", Integer.toString(level));
				formatedList.add(fcmd);
			});
			return formatedList;
		}
		
		private List<String> commandList;
		
		private Boolean console;
		
	}
	
	
	// -------------------------------------------------------------------------
	// Section: Arithmetic expressions Parser
	// Thanks to Boann on StackOverflow
	// Link: https://stackoverflow.com/questions/3422673/how-to-evaluate-a-math-expression-given-in-string-form
	// -------------------------------------------------------------------------
	
	@FunctionalInterface
	interface Expression {
		double eval();
	}
	
	public static Expression parse(final String str, Map<String, Double> variables) {
	    return new Object() {
	        int pos = -1, ch;

	        void nextChar() {
	            ch = (++pos < str.length()) ? str.charAt(pos) : -1;
	        }

	        boolean eat(int charToEat) {
	            while (ch == ' ') nextChar();
	            if (ch == charToEat) {
	                nextChar();
	                return true;
	            }
	            return false;
	        }

	        Expression parse() {
	            nextChar();
	            Expression x = parseExpression();
	            if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
	            return x;
	        }

	        // Grammar:
	        // expression = term | expression `+` term | expression `-` term
	        // term = factor | term `*` factor | term `/` factor
	        // factor = `+` factor | `-` factor | `(` expression `)`
	        //        | number | functionName factor | factor `^` factor

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
	            if (eat('+')) return parseFactor(); // unary plus
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
	                while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
	                final Integer innerPos = new Integer(this.pos);
	                x = (() -> Double.parseDouble(str.substring(startPos, innerPos)));
	            } else if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '[' || ch == ']') { // functions
	                while ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '[' || ch == ']') nextChar();
	                String func = str.substring(startPos, this.pos);
	                if (funct.contains(func)) {
	                	Expression a = parseFactor();
	                	if (func.equals("sqrt")) x = (() -> Math.sqrt(a.eval()));
		                else if (func.equals("sin")) x = (() -> Math.sin(Math.toRadians(a.eval())));
		                else if (func.equals("cos")) x = (() -> Math.cos(Math.toRadians(a.eval())));
		                else if (func.equals("tan")) x = (() -> Math.tan(Math.toRadians(a.eval())));
		                else throw new RuntimeException("Unknown function: " + func);
	                } else {
	                	x = (() -> variables.get(func));
	                }
	            } else {
	                throw new RuntimeException("Unexpected: " + (char)ch);
	            }

	            if (eat('^')) {
	            	Expression a = x, b = parseFactor();
	            	x = (() -> Math.pow(a.eval(), b.eval())); // exponentiation
	            }

	            return x;
	        }
	    }.parse();
	}
	
	private static final List<String> funct = new ArrayList<>();
    static {
    	funct.add("sqrt");
    	funct.add("sin");
    	funct.add("cos");
    	funct.add("tan");
    }

}