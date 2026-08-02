package world.bentobox.upgrades.dataobjects.prices;


public abstract class PriceDB {

    public abstract Class<? extends Price> getPriceType();

    public abstract boolean isValid();

}
