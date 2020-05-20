package world.bentobox.islandupgrades;

import com.google.gson.annotations.Expose;

import world.bentobox.bentobox.database.objects.DataObject;

public class IslandUpgradesData implements DataObject {

	@Expose
	private String uniqueId;
	
	@Expose
	private long rangeUpgradeLevel;
	
	public IslandUpgradesData() {}
	
	public IslandUpgradesData(String uniqueId, long rangeUpgradeLevel) {
		this.uniqueId = uniqueId;
		this.rangeUpgradeLevel = rangeUpgradeLevel;
	}
	
	public IslandUpgradesData(String uniqueId) {
		this(uniqueId, 1);
	}
	
	@Override
	public String getUniqueId() {
		return uniqueId;
	}
	
	@Override
	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}
	
	public long getRangeUpgradeLevel() {
		return rangeUpgradeLevel;
	}
	
	public void setRangeUpgradeLevel(long rangeUpgradeLevel) {
		this.rangeUpgradeLevel = rangeUpgradeLevel;
	}
	
}
