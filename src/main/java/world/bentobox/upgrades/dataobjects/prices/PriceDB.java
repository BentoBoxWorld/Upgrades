package world.bentobox.upgrades.dataobjects.prices;

import com.google.gson.annotations.Expose;
import world.bentobox.bentobox.database.objects.DataObject;

public abstract class PriceDB {

    abstract public Class<? extends Price> getPriceType();

    abstract public boolean isValid();

}
