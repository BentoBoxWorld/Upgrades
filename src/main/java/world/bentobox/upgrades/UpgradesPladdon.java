package world.bentobox.upgrades;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.addons.Pladdon;

public class UpgradesPladdon extends Pladdon {

    private Addon addon;

    @Override
    public Addon getAddon() {
        if (addon == null) {
            addon = new UpgradesAddon();
        }

        return addon;
    }
}
