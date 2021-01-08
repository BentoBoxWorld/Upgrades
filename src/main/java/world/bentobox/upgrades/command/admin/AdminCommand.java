package world.bentobox.upgrades.command.admin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.dataobjects.UpgradeData;
import world.bentobox.upgrades.dataobjects.UpgradeTier;
import world.bentobox.upgrades.dataobjects.prices.IslandLevelPrice;
import world.bentobox.upgrades.dataobjects.prices.Price;
import world.bentobox.upgrades.dataobjects.rewards.RangeReward;
import world.bentobox.upgrades.dataobjects.rewards.Reward;
import world.bentobox.upgrades.ui.admin.AdminPanel;

public class AdminCommand extends CompositeCommand {
	
	public AdminCommand(UpgradesAddon addon, CompositeCommand cmd, GameModeAddon gameMode) {
		super(addon, cmd, "upgrade");
		
		this.addon = addon;
		this.gameMode = gameMode;
	}
	
	@Override
	public void setup() {
		this.setOnlyPlayer(true);
		this.setDescription("upgrades.commands.admin.description");
		this.setParametersHelp("upgrades.commands.admin.parameters");
		this.setPermission("admin.upgrade");
	}
	
	@Override
	public boolean execute(User user, String label, List<String> args) {
		/*UpgradeData upgrade = this.addon.getUpgradeDataManager().createUpgradeData("testUpgradeId", getWorld(), user);
		upgrade.setIcon(new ItemStack(Material.GOLD_BLOCK));
		upgrade.setName("Test upgrade");
		this.addon.getUpgradeDataManager().saveUpgradeData(upgrade);
		IslandLevelPrice price = new IslandLevelPrice();
		List<Price> prices = new ArrayList<Price>();
		prices.add(price);
		price.setLevelNeededEquation("3");
		RangeReward reward = new RangeReward();
		List<Reward> rewards = new ArrayList<Reward>();
		rewards.add(reward);
		reward.setRangeUpgradeEquation("5");
		UpgradeTier tier = this.addon.getUpgradeDataManager().createUpgradeTier("testtierid", upgrade, 0, 5, user);
		tier.setIcon(new ItemStack(Material.GOLD_INGOT));
		tier.setName("Test tier");
		tier.setPrices(prices);
		tier.setRewards(rewards);
		this.addon.getUpgradeDataManager().saveUpgradeTier(tier);*/
		
		if (user.isPlayer()) {
			new AdminPanel(this.addon, this.gameMode, user).getBuild().build();
			return true;
		}
		return false;
	}
	
	private UpgradesAddon addon;
	private GameModeAddon gameMode;

}
