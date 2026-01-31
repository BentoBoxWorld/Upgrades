package world.bentobox.upgrades.dataobjects;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.Expose;

import world.bentobox.bentobox.database.objects.DataObject;
import world.bentobox.bentobox.database.objects.Table;

/**
 * Database object for storing upgrades data
 */
@Table(name = "UpgradesData")
public class UpgradesData implements DataObject {

	@Expose
	private String uniqueId;
	
	@Expose
	private Map<String, Integer> upgradesLevels;
	
	public UpgradesData() {}
	
	public UpgradesData(String uniqueId, Map<String, Integer> upgradesLevel) {
		this.uniqueId = uniqueId;
		this.upgradesLevels = upgradesLevel;
	}
	
	public UpgradesData(String uniqueId) {
		this(uniqueId, new HashMap<>());
	}
	
	@Override
	public String getUniqueId() {
		return uniqueId;
	}
	
	@Override
	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}
	
	public int getUpgradeLevel(String name) {
		this.upgradesLevels.putIfAbsent(name, 1);
		return this.upgradesLevels.get(name);
	}
	
	public void setUpgradeLevel(String name, int value) {
		this.upgradesLevels.put(name, value);
	}
	
}
