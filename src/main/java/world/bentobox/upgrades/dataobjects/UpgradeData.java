package world.bentobox.upgrades.dataobjects;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.google.gson.annotations.Expose;

import world.bentobox.bentobox.database.objects.DataObject;
import world.bentobox.bentobox.database.objects.Table;

/**
 * This class represent a DataObject that store registered upgrades
 * 
 * @author Ikkino
 *
 */
@Table(name = "UpgradeData")
public class UpgradeData implements DataObject {
	
	// ------------------------------------------------------------
	// Section: Variables
	// ------------------------------------------------------------

	/**
	 * Unique id for each upgrades
	 */
	@Expose
	private String uniqueId;
	
	/**
	 * gamemode in wich this upgrade is used
	 */
	@Expose
	private String world;
	
	/**
	 * Readable name for this upgrade
	 * If not present, uniqueId will be used
	 * Can contain color Codes
	 */
	@Expose
	private String name;
	
	/**
	 * Description of this upgrade
	 * If not present, default description will be used
	 * Can contain color Codes
	 */
	@Expose
	private String description;
	
	/**
	 * Icon representing this upgrade
	 * Will be used in interfaces
	 * Default is chest
	 */
	@Expose
	private ItemStack icon = new ItemStack(Material.CHEST);
	
	/**
	 * Order of upgrades in the interfaces
	 * From lowest to highest
	 * If tie, then sort by character
	 * If < 0 then placed after the others
	 * Default to -1
	 */
	@Expose
	private int order = -1;
	
	/**
	 * If this upgrade is shown to the user
	 * Default to false
	 */
	@Expose
	private boolean active = false;
	
	/**
	 * List of the tiers in this upgrade
	 * Tiers represented by their uniqueId
	 */
	@Expose
	private Set<String> tiers = new HashSet<String>();
	
	// ------------------------------------------------------------
	// Section: Getters
	// ------------------------------------------------------------
	
	/**
	 * @return the uniqueId
	 */
	@Override
	public String getUniqueId() {
		return uniqueId;
	}

	/**
	 * @return the world
	 */
	public String getWorld() {
		return world;
	}

	/**
	 * @return the name or uniqueId if empty
	 */
	public String getName() {
		return name.isEmpty() ? uniqueId : name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return clone if the icon or default chest icon if empty
	 */
	public ItemStack getIcon() {
		return icon != null ? icon.clone() : new ItemStack(Material.CHEST);
	}

	/**
	 * @return the order
	 */
	public int getOrder() {
		return order;
	}

	/**
	 * @return the active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * @return the tiers
	 */
	public Set<String> getTiers() {
		return tiers;
	}
	
	// ------------------------------------------------------------
	// Section: Setters
	// ------------------------------------------------------------

	/**
	 * @param uniqueId the uniqueId to set
	 */
	@Override
	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	/**
	 * @param world the world to set
	 */
	public void setWorld(String world) {
		this.world = world;
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
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @param icon the icon to set
	 */
	public void setIcon(ItemStack icon) {
		this.icon = icon;
	}

	/**
	 * @param order the order to set
	 */
	public void setOrder(int order) {
		this.order = order;
	}

	/**
	 * @param active the active to set
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * @param tiers the tiers to set
	 */
	public void setTiers(Set<String> tiers) {
		this.tiers = tiers;
	}
	
	// ------------------------------------------------------------
	// Section: Utils Methods
	// ------------------------------------------------------------
	
	public boolean isValid() {
		return this.uniqueId != null &&
			this.description != null &&
			this.icon != null &&
			this.name != null &&
			this.tiers != null &&
			this.world != null;
	}
	
}
