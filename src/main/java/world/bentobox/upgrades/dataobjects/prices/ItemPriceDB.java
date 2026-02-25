package world.bentobox.upgrades.dataobjects.prices;

import com.google.gson.annotations.Expose;

public class ItemPriceDB extends PriceDB {

    @Expose
    private String material = "";

    @Expose
    private int amount = 1;

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public Class<ItemPrice> getPriceType() {
        return ItemPrice.class;
    }

    @Override
    public boolean isValid() {
        return material != null && !material.isEmpty() && amount > 0;
    }

}
