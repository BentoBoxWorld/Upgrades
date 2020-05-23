package world.bentobox.upgrades.dataobjects;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.Expose;

import world.bentobox.bentobox.database.objects.DataObject;
import world.bentobox.bentobox.database.objects.Table;

@Table(name = "UpgradesData")
public class UpgradesData implements DataObject {

	@Expose
	private String uniqueId;
	
	@Expose
	private Map<String, Long> upgradesLevels;
	
	public UpgradesData() {}
	
	public UpgradesData(String uniqueId, Map<String, Long> upgradesLevel) {
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
	
	public long getUpgradeLevel(String name) {
		this.upgradesLevels.putIfAbsent(name, (long) 0);
		return this.upgradesLevels.get(name);
	}
	
	public void setUpgradeLevel(String name, long value) {
		this.upgradesLevels.put(name, value);
	}
	
}
