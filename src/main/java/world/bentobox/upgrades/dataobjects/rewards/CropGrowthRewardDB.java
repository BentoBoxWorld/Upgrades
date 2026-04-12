package world.bentobox.upgrades.dataobjects.rewards;

import com.google.gson.annotations.Expose;

public class CropGrowthRewardDB extends RewardDB {

    @Expose
    private String growthBonusEquation = "0";

    public String getGrowthBonusEquation() {
        return growthBonusEquation;
    }

    public void setGrowthBonusEquation(String growthBonusEquation) {
        this.growthBonusEquation = growthBonusEquation;
    }

    @Override
    public boolean isValid() {
        return growthBonusEquation != null && !growthBonusEquation.isEmpty();
    }

    @Override
    public Class<CropGrowthReward> getRewardType() {
        return CropGrowthReward.class;
    }

}
