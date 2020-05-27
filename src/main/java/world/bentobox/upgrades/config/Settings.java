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

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.eclipse.jdt.annotation.NonNull;

import world.bentobox.upgrades.UpgradesAddon;

public class Settings {
	
	public Settings(UpgradesAddon addon) {
		this.addon = addon;
		this.addon.saveDefaultConfig();
		
		this.disabledGameModes = new HashSet<>(this.addon.getConfig().getStringList("disabled-gamemodes"));
		
		if (this.addon.getConfig().isSet("range-upgrade")) {
			ConfigurationSection section = this.addon.getConfig().getConfigurationSection("range-upgrade");
			for (String key : Objects.requireNonNull(section).getKeys(false)) {
				this.rangeUpgradeTierMap.put(key, addUpgradeSection(section, key));
			}
		}
		
		if (this.addon.getConfig().isSet("block-limits-upgrade")) {
			ConfigurationSection section = this.addon.getConfig().getConfigurationSection("block-limits-upgrade");
			this.blockLimitsUpgradeTierMap = this.loadBlockLimits(section);
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
			this.entityLimitsUpgradeTierMap = this.loadEntityLimits(section);
		}
		
		if (this.addon.getConfig().isSet("gamemodes")) {
			ConfigurationSection section = this.addon.getConfig().getConfigurationSection("gamemodes");
			
			for (String gameMode : Objects.requireNonNull(section).getKeys(false)) {
				ConfigurationSection gameModeSection = section.getConfigurationSection(gameMode);
				
				if (gameModeSection.isSet("range-upgrade")) {
					ConfigurationSection lowSection = gameModeSection.getConfigurationSection("range-upgrade");
					for (String key : Objects.requireNonNull(lowSection).getKeys(false)) {
						
						this.customRangeUpgradeTierMap.computeIfAbsent(gameMode, k -> new HashMap<>()).put(key, addUpgradeSection(lowSection, key));
					}
				}
				
				if (gameModeSection.isSet("block-limits-upgrade")) {
					ConfigurationSection lowSection = gameModeSection.getConfigurationSection("block-limits-upgrade");
					this.customBlockLimitsUpgradeTierMap.computeIfAbsent(gameMode, k -> loadBlockLimits(lowSection));
				}
				
				if (gameModeSection.isSet("entity-limits-upgrade")) {
					ConfigurationSection lowSection = gameModeSection.getConfigurationSection("entity-limits-upgrade");
					this.customEntityLimitsUpgradeTierMap.computeIfAbsent(gameMode, k -> loadEntityLimits(lowSection));
				}
			}
		}
		
	}
	
	private Map<Material, Map<String, UpgradeTier>> loadBlockLimits(ConfigurationSection section) {
		Map<Material, Map<String, UpgradeTier>> mats = new EnumMap<>(Material.class);
		for (String material: Objects.requireNonNull(section).getKeys(false)) {
			Material mat = Material.getMaterial(material);
			if (mat != null && mat.isBlock()) {
				Map<String, UpgradeTier> tier = new HashMap<>();
				ConfigurationSection matSection = section.getConfigurationSection(material);
				for (String key : Objects.requireNonNull(matSection).getKeys(false)) {
					tier.put(key, addUpgradeSection(matSection, key));
				}
				mats.put(mat, tier);
			} else {
				this.addon.logError("Material " + material + " is not a valid material. Skipping...");
			}
		}
		return mats;
	}
	
	private Map<EntityType, Map<String, UpgradeTier>> loadEntityLimits(ConfigurationSection section) {
		Map<EntityType, Map<String, UpgradeTier>> ents = new EnumMap<>(EntityType.class);
		for (String entity: Objects.requireNonNull(section).getKeys(false)) {
			EntityType ent = this.getEntityType(entity);
			if (ent != null && this.entityIcon.containsKey(ent)) {
				Map<String, UpgradeTier> tier = new HashMap<>();
				ConfigurationSection entSection = section.getConfigurationSection(entity);
				for (String key : Objects.requireNonNull(entSection).getKeys(false)) {
					tier.put(key, addUpgradeSection(entSection, key));
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
	
	@NonNull
	private UpgradeTier addUpgradeSection(ConfigurationSection section, String key) {
		ConfigurationSection tierSection = section.getConfigurationSection(key);
		UpgradeTier upgradeTier = new UpgradeTier(key);
		upgradeTier.setMaxLevel(tierSection.getInt("max-level"));
		upgradeTier.setUpgrade(parse(tierSection.getString("upgrade"), upgradeTier.getExpressionVariable()));
		
		if (tierSection.isSet("island-min-level"))
			upgradeTier.setIslandMinLevel(parse(tierSection.getString("island-min-level"), upgradeTier.getExpressionVariable()));
		else
			upgradeTier.setIslandMinLevel(parse("0", upgradeTier.getExpressionVariable()));
		
		if (tierSection.isSet("vault-cost"))
			upgradeTier.setVaultCost(parse(tierSection.getString("vault-cost"), upgradeTier.getExpressionVariable()));
		else
			upgradeTier.setVaultCost(parse("0", upgradeTier.getExpressionVariable()));
		
		return upgradeTier;
		
	}
	
	/**
	 * @return the disabledGameModes
	 */
	public Set<String> getDisabledGameModes() {
		return disabledGameModes;
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

	private UpgradesAddon addon;
	
	private Set<String> disabledGameModes;
	
	private Map<String, UpgradeTier> rangeUpgradeTierMap = new HashMap<>();
	
	private Map<String, Map<String, UpgradeTier>> customRangeUpgradeTierMap = new HashMap<>();
	
	private Map<Material, Map<String, UpgradeTier>> blockLimitsUpgradeTierMap = new EnumMap<>(Material.class);
	
	private Map<String, Map<Material, Map<String, UpgradeTier>>> customBlockLimitsUpgradeTierMap = new HashMap<>();
	
	private Map<EntityType, Material> entityIcon = new EnumMap<>(EntityType.class); 
	
	private Map<EntityType, Map<String, UpgradeTier>> entityLimitsUpgradeTierMap = new EnumMap<>(EntityType.class);
	
	private Map<String, Map<EntityType, Map<String, UpgradeTier>>> customEntityLimitsUpgradeTierMap = new HashMap<>();
	
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


		// ----------------------------------------------------------------------
		// Section: Variables
		// ----------------------------------------------------------------------


		private final String id;

		private int maxLevel = -1;

		private Expression upgrade;
		
		private Expression islandMinLevel;
		
		private Expression vaultCost;
		
		private Map<String, Double> expressionVariables;
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