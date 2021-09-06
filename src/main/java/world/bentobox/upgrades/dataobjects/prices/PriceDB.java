package world.bentobox.upgrades.dataobjects.prices;

public abstract class PriceDB {

	abstract public Class<? extends Price> getPriceType();

	abstract public boolean isValid();

	abstract public boolean Activate();

}
