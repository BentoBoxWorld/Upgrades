package world.bentobox.upgrades.dataobjects.rewards;

import com.google.gson.annotations.Expose;

public class RangeReward extends Reward {
	
	// ------------------------------------------------------------
	// Section: Variables
	// ------------------------------------------------------------
	
	/**
	 * How much is added at each upgrades
	 * It's a string representing an equation to calculate the range upgrade
	 */
	@Expose
	private String rangeUpgradeEquation;
	
	// ------------------------------------------------------------
	// Section: Getters
	// ------------------------------------------------------------

	/**
	 * @return the rangeUpgradeEquation
	 */
	public String getRangeUpgradeEquation() {
		return rangeUpgradeEquation;
	}
	
	// ------------------------------------------------------------
	// Section: Setters
	// ------------------------------------------------------------

	/**
	 * @param rangeUpgradeEquation the rangeUpgradeEquation to set
	 */
	public void setRangeUpgradeEquation(String rangeUpgradeEquation) {
		this.rangeUpgradeEquation = rangeUpgradeEquation;
	}
	
	// ------------------------------------------------------------
	// Section: Utils Methods
	// ------------------------------------------------------------
	
	@Override
	public boolean isValid() {
		return super.isValid() &&
			rangeUpgradeEquation != null &&
			!rangeUpgradeEquation.isEmpty();
	}
}
