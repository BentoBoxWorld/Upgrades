package world.bentobox.upgrades.dataobjects.rewards;

import com.google.gson.annotations.Expose;
import world.bentobox.bentobox.database.objects.DataObject;

public abstract class RewardDB {

    abstract public Class<? extends Reward> getRewardType();

    abstract public boolean isValid();

}
