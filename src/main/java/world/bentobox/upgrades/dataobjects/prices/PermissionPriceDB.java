package world.bentobox.upgrades.dataobjects.prices;

import com.google.gson.annotations.Expose;

public class PermissionPriceDB extends PriceDB {

    @Expose
    private String permission = "";

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    @Override
    public Class<PermissionPrice> getPriceType() {
        return PermissionPrice.class;
    }

    @Override
    public boolean isValid() {
        return permission != null && !permission.isEmpty();
    }

}
