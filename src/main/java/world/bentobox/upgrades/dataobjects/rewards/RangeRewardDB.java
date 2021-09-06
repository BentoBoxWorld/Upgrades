package world.bentobox.upgrades.dataobjects.rewards;

import com.google.gson.annotations.Expose;

public class RangeRewardDB extends RewardDB {

    // ------------------------------------------------------------
    // Section: Variables
    // ------------------------------------------------------------

    /**
     * How much is added at each upgrades
     * It's a string representing an equation to calculate the range upgrade
     */
    @Expose
    private String rangeUpgradeEquation = "0";

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
        return rangeUpgradeEquation != null &&
                !rangeUpgradeEquation.isEmpty();
    }

    @Override
    public Class<? extends Reward> getRewardType() {
        return RangeReward.class;
    }

}
