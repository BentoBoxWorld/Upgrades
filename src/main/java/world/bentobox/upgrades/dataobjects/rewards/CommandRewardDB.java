package world.bentobox.upgrades.dataobjects.rewards;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

public class CommandRewardDB extends RewardDB {

    @Expose
    private List<String> commands = new ArrayList<>();

    @Expose
    private boolean isConsole = true;

    public List<String> getCommands() {
        return commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    public boolean isConsole() {
        return isConsole;
    }

    public void setConsole(boolean console) {
        isConsole = console;
    }

    @Override
    public Class<CommandReward> getRewardType() {
        return CommandReward.class;
    }

    @Override
    public boolean isValid() {
        return commands != null && !commands.isEmpty();
    }

}
