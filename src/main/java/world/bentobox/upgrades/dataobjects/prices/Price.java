package world.bentobox.upgrades.dataobjects.prices;

import java.util.List;

import org.bukkit.Material;

public abstract class Price {
	
	private String name;
	private Material icon;

	public Price(String name, Material icon) {
		this.name = name;
		this.icon = icon;
	}
	
	public String getName() {
		return name;
	}
	
	public Material getIcon() {
		return icon;
	}
	
	abstract public List<String> getDescription();
	
	abstract public List<String> getAdminDescription();
	
	public boolean isValid() {
		return true;
	}
	
	abstract public boolean Activate();
	
}
