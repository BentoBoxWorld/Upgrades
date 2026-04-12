package world.bentobox.upgrades.dataobjects.prices;

import com.google.gson.annotations.Expose;

public class MoneyPriceDB extends PriceDB {

    @Expose
    private String amountEquation = "0";

    public String getAmountEquation() {
        return amountEquation;
    }

    public void setAmountEquation(String amountEquation) {
        this.amountEquation = amountEquation;
    }

    @Override
    public Class<MoneyPrice> getPriceType() {
        return MoneyPrice.class;
    }

    @Override
    public boolean isValid() {
        return amountEquation != null && !amountEquation.isEmpty();
    }

}
