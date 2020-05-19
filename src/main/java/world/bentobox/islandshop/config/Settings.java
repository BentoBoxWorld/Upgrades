package world.bentobox.islandshop.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Objects;

import org.bukkit.configuration.ConfigurationSection;
import org.eclipse.jdt.annotation.NonNull;

import world.bentobox.islandshop.IslandShopAddon;

public class Settings {
	
	public Settings(IslandShopAddon addon)
	{
		this.addon = addon;
		this.addon.saveDefaultConfig();
		
		this.disabledGameModes = new HashSet<>(this.addon.getConfig().getStringList("disabled-gamemodes"));
		
		if (this.addon.getConfig().isSet("range-upgrade")) {
			ConfigurationSection section = this.addon.getConfig().getConfigurationSection("range-upgrade");
			for (String key : Objects.requireNonNull(section).getKeys(false)) {
				this.rangeUpgradeTierMap.put(key, addRangeSection(section, key));
			}
		}
		
		if (this.addon.getConfig().isSet("gamemodes")) {
			ConfigurationSection section = this.addon.getConfig().getConfigurationSection("gamemodes");
			
			for (String gameMode : Objects.requireNonNull(section).getKeys(false)) {
				
				ConfigurationSection gameModeSection = section.getConfigurationSection(gameMode);
				for (String key : Objects.requireNonNull(gameModeSection).getKeys(false)) {
					
					this.customRangeUpgradeTierMap.computeIfAbsent(gameMode, k -> new HashMap<>()).put(key, addRangeSection(gameModeSection, key));
				}
			}
		}
		
	}
	
	@NonNull
	private RangeUpgradeTier addRangeSection(ConfigurationSection section, String key) {
		ConfigurationSection tierSection = section.getConfigurationSection(key);
		RangeUpgradeTier rangeUpgradeTier = new RangeUpgradeTier(key);
		rangeUpgradeTier.setMaxLevel(tierSection.getInt("max-level"));
		rangeUpgradeTier.setUpgradeRange(eval(tierSection.getString("upgrade-range"), rangeUpgradeTier.getExpressionVariable()));
		
		if (tierSection.isSet("island-min-level"))
			rangeUpgradeTier.setIslandMinLevel(eval(tierSection.getString("island-min-level"), rangeUpgradeTier.getExpressionVariable()));
		else
			rangeUpgradeTier.setIslandMinLevel(eval("0", rangeUpgradeTier.getExpressionVariable()));
		
		if (tierSection.isSet("vault-cost"))
			rangeUpgradeTier.setVaultCost(eval(tierSection.getString("vault-cost"), rangeUpgradeTier.getExpressionVariable()));
		else
			rangeUpgradeTier.setVaultCost(eval("0", rangeUpgradeTier.getExpressionVariable()));
		
		return rangeUpgradeTier;
		
	}
	
	/**
	 * @return the disabledGameModes
	 */
	public Set<String> getDisabledGameModes() {
		return disabledGameModes;
	}
	
	public Map<String, RangeUpgradeTier> getDefaultRangeUpgradeTierMap() {
		return this.rangeUpgradeTierMap;
	}
	
	/**
	 * @return the rangeUpgradeTierMap
	 */
	public Map<String, RangeUpgradeTier> getAddonRangeUpgradeTierMap(String addon) {
		return this.customRangeUpgradeTierMap.getOrDefault(addon, Collections.emptyMap());
	}

	private IslandShopAddon addon;
	
	private Set<String> disabledGameModes;
	
	private Map<String, RangeUpgradeTier> rangeUpgradeTierMap = new HashMap<>();
	
	private Map<String, Map<String, RangeUpgradeTier>> customRangeUpgradeTierMap = new HashMap<>();
	
	// ------------------------------------------------------------------
	// Section: Private object
	// ------------------------------------------------------------------
	
	public class RangeUpgradeTier {
		/**
		 * Constructor RangeUpgradeTier create a new RangeUpgradeTier instance
		 * and set expressionVariables to default value
		 * 
		 * @param id
		 */
		public RangeUpgradeTier(String id) {
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
		public Expression getUpgradeRange() {
			return upgradeRange;
		}

		/**
		 * @param upgradeRange the upgradeRange to set
		 */
		public void setUpgradeRange(Expression upgradeRange) {
			this.upgradeRange = upgradeRange;
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
		
		public double calculateUpgradeRange(double level, double islandLevel, double numberPeople) {
			this.updateExpressionVariable("[level]", level);
			this.updateExpressionVariable("[islandLevel]", islandLevel);
			this.updateExpressionVariable("[numberPlayer]", numberPeople);
			return this.getUpgradeRange().eval();
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

		private Expression upgradeRange;
		
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
	
	public static Expression eval(final String str, Map<String, Double> variables) {
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
	                x = (() -> Double.parseDouble(str.substring(startPos, this.pos)));
	            } else if ((ch >= 'a' && ch <= 'z') || ch == '[' || ch == ']') { // functions
	                while ((ch >= 'a' && ch <= 'z') || ch == '[' || ch == ']') nextChar();
	                String func = str.substring(startPos, this.pos);
	                if (variables.containsKey(func)) x = (() -> variables.get(func));
	                else {
	                	Expression a = parseFactor();
	                	if (func.equals("sqrt")) x = (() -> Math.sqrt(a.eval()));
		                else if (func.equals("sin")) x = (() -> Math.sin(Math.toRadians(a.eval())));
		                else if (func.equals("cos")) x = (() -> Math.cos(Math.toRadians(a.eval())));
		                else if (func.equals("tan")) x = (() -> Math.tan(Math.toRadians(a.eval())));
		                else throw new RuntimeException("Unknown function: " + func);
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

}
