package world.bentobox.upgrades.dataobjects.rewards;


public abstract class RewardDB {

    public abstract Class<? extends Reward> getRewardType();

    public abstract boolean isValid();

}
