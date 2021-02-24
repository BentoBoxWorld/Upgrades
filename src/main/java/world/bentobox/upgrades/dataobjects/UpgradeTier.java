package world.bentobox.upgrades.dataobjects;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;

import com.google.gson.annotations.Expose;

import world.bentobox.bentobox.database.objects.DataObject;
import world.bentobox.bentobox.database.objects.Table;
import world.bentobox.upgrades.dataobjects.prices.Price;
import world.bentobox.upgrades.dataobjects.rewards.Reward;

@Table(name = "UpgradeTier")
public class UpgradeTier implements DataObject {

	// ------------------------------------------------------------
	// Section: Variables
	// ------------------------------------------------------------
	
	/**
	 * Unique id for each upgrade tiers
	 */
	@Expose
	private String uniqueId;
	
	/**
	 * Unique id of the parent upgrade
	 */
	@Expose
	private String upgrade;
	
	/**
	 * Readable name for this tier
	 * If not present, uniqueId will be used
	 * Can contain color codes
	 */
	@Expose
	private String name = "";
	
	/**
	 * Description of this tier
	 * If not present, no description will be used
	 * Can contain color codes
	 */
	@Expose
	private List<String> description = new ArrayList<String>();
	
	/**
	 * Icon representing this tier
	 * Will be used in interfaces
	 * If not present, it will use it's parent icon
	 */
	@Expose
	private ItemStack icon;
	
	/**
	 * Level of the upgrade at which this tier start
	 */
	@Expose
	private int startLevel;
	
	/**
	 * Level of the upgrade at which this tier end
	 */
	@Expose
	private int endLevel;
	
	/**
	 * List of prices needed for each level of this tier
	 */
	@Expose
	private List<Price> prices = new ArrayList<Price>();
	
	/**
	 * List of rewards given at each level of this tier
	 */
	@Expose
	private List<Reward> rewards = new ArrayList<Reward>();
	
	// ------------------------------------------------------------
	// Section: Getters
	// ------------------------------------------------------------

	/**
	 * @return the uniqueId
	 */
	public String getUniqueId() {
		return uniqueId;
	}

	/**
	 * @return the upgrade
	 */
	public String getUpgrade() {
		return upgrade;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name.isEmpty() ? uniqueId : name;
	}

	/**
	 * @return the description
	 */
	public List<String> getDescription() {
		return description;
	}

	/**
	 * @return the icon
	 */
	public ItemStack getIcon() {
		return icon;
	}

	/**
	 * @return the startLevel
	 */
	public int getStartLevel() {
		return startLevel;
	}

	/**
	 * @return the endLevel
	 */
	public int getEndLevel() {
		return endLevel;
	}

	/**
	 * @return the prices
	 */
	public List<Price> getPrices() {
		return prices;
	}

	/**
	 * @return the rewards
	 */
	public List<Reward> getRewards() {
		return rewards;
	}
	
	// ------------------------------------------------------------
	// Section: Setters
	// ------------------------------------------------------------

	/**
	 * @param uniqueId the uniqueId to set
	 */
	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	/**
	 * @param upgrade the upgrade to set
	 */
	public void setUpgrade(String upgrade) {
		this.upgrade = upgrade;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(List<String> description) {
		this.description = description;
	}

	/**
	 * @param icon the icon to set
	 */
	public void setIcon(ItemStack icon) {
		this.icon = icon;
	}

	/**
	 * @param startLevel the startLevel to set
	 */
	public void setStartLevel(int startLevel) {
		this.startLevel = startLevel;
	}

	/**
	 * @param endLevel the endLevel to set
	 */
	public void setEndLevel(int endLevel) {
		this.endLevel = endLevel;
	}

	/**
	 * @param prices the prices to set
	 */
	public void setPrices(List<Price> prices) {
		this.prices = prices;
	}

	/**
	 * @param rewards the rewards to set
	 */
	public void setRewards(List<Reward> rewards) {
		this.rewards = rewards;
	}
	
	// ------------------------------------------------------------
	// Section: Utils Methods
	// ------------------------------------------------------------
	
	public boolean isValid() {
		return this.uniqueId != null &&
			this.upgrade != null &&
			this.name != null &&
			this.description != null &&
			this.prices != null &&
			this.rewards != null;
	}
	
}
