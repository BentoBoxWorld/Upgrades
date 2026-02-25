package world.bentobox.upgrades.dataobjects.rewards;

import com.google.gson.annotations.Expose;

public class SpawnerRewardDB extends RewardDB {

    @Expose
    private String spawnBonusEquation = "0";

    public String getSpawnBonusEquation() {
        return spawnBonusEquation;
    }

    public void setSpawnBonusEquation(String spawnBonusEquation) {
        this.spawnBonusEquation = spawnBonusEquation;
    }

    @Override
    public boolean isValid() {
        return spawnBonusEquation != null && !spawnBonusEquation.isEmpty();
    }

    @Override
    public Class<SpawnerReward> getRewardType() {
        return SpawnerReward.class;
    }

}
