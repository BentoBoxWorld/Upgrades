package world.bentobox.upgrades.dataobjects.prices;

import java.util.List;

import org.bukkit.Material;

import com.google.gson.annotations.Expose;

public class IslandLevelPrice extends Price {
	
	// ------------------------------------------------------------
	// Section: Variables
	// ------------------------------------------------------------
	
	public IslandLevelPrice() {
		super("Price", Material.GOLD_INGOT);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Level needed to make the upgrade
	 * It's a string representing an equation to calculate the level needed
	 */
	@Expose
	private String levelNeededEquation;
	
	// ------------------------------------------------------------
	// Section: Getters
	// ------------------------------------------------------------

	/**
	 * @return the levelNeededEquation
	 */
	public String getLevelNeededEquation() {
		return levelNeededEquation;
	}
	
	// ------------------------------------------------------------
	// Section: Setters
	// ------------------------------------------------------------

	/**
	 * @param levelNeededEquation the levelNeededEquation to set
	 */
	public void setLevelNeededEquation(String levelNeededEquation) {
		this.levelNeededEquation = levelNeededEquation;
	}
	
	// ------------------------------------------------------------
	// Section: Utils Methods
	// ------------------------------------------------------------
	
	@Override
	public boolean isValid() {
		return super.isValid() &&
			levelNeededEquation != null &&
			!levelNeededEquation.isEmpty();
	}

	@Override
	public List<String> getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getAdminDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean Activate() {
		// TODO Auto-generated method stub
		return false;
	}
}
