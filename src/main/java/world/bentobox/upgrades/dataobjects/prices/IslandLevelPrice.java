package world.bentobox.upgrades.dataobjects.prices;

import com.google.gson.annotations.Expose;

public class IslandLevelPrice extends Price {
	
	// ------------------------------------------------------------
	// Section: Variables
	// ------------------------------------------------------------
	
	/**
	 * Level needed to make the upgrade
	 * It's a string representing an equation to calculate the level needed
	 */
	@Expose
	private String levelNeededEquation;
	
	// ------------------------------------------------------------
	// Section: Getters
	// ------------------------------------------------------------

	/**
	 * @return the levelNeededEquation
	 */
	public String getLevelNeededEquation() {
		return levelNeededEquation;
	}
	
	// ------------------------------------------------------------
	// Section: Setters
	// ------------------------------------------------------------

	/**
	 * @param levelNeededEquation the levelNeededEquation to set
	 */
	public void setLevelNeededEquation(String levelNeededEquation) {
		this.levelNeededEquation = levelNeededEquation;
	}
	
	// ------------------------------------------------------------
	// Section: Utils Methods
	// ------------------------------------------------------------
	
	@Override
	public boolean isValid() {
		return super.isValid() &&
			levelNeededEquation != null &&
			!levelNeededEquation.isEmpty();
	}
}
