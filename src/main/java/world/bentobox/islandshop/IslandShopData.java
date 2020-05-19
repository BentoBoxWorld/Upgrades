package world.bentobox.islandshop;

import com.google.gson.annotations.Expose;

import world.bentobox.bentobox.database.objects.DataObject;

public class IslandShopData implements DataObject {
	
	@Expose
	private String uniqueId;
	
	@Expose
	private long rangeUpgradeLevel;
	
	public IslandShopData() {}
	
	public IslandShopData(String uniqueId, long rangeUpgradeLevel) {
		this.uniqueId = uniqueId;
		this.rangeUpgradeLevel = rangeUpgradeLevel;
	}
	
	public IslandShopData(String uniqueId) {
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
