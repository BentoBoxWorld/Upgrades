package world.bentobox.upgrades.dataobjects.rewards;

import com.google.gson.annotations.Expose;

public class LimitsRewardDB extends RewardDB {

    @Expose
    private String limitType = "BLOCK";

    @Expose
    private String target = "";

    @Expose
    private String amountEquation = "0";

    public String getLimitType() {
        return limitType;
    }

    public void setLimitType(String limitType) {
        this.limitType = limitType;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getAmountEquation() {
        return amountEquation;
    }

    public void setAmountEquation(String amountEquation) {
        this.amountEquation = amountEquation;
    }

    @Override
    public Class<LimitsReward> getRewardType() {
        return LimitsReward.class;
    }

    @Override
    public boolean isValid() {
        return limitType != null && !limitType.isEmpty()
                && target != null && !target.isEmpty()
                && amountEquation != null && !amountEquation.isEmpty();
    }

}
