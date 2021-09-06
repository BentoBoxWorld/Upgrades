package world.bentobox.upgrades.dataobjects.prices;

import com.google.gson.annotations.Expose;

public class IslandLevelPriceDB extends PriceDB {


    /**
     * Level needed to make the upgrade It's a string representing an equation to
     * calculate the level needed
     */
    @Expose
    private String levelNeededEquation;

    public IslandLevelPriceDB(String levelNeededEquation) {
        this.levelNeededEquation = levelNeededEquation;
    }

    public IslandLevelPriceDB() {
        this.levelNeededEquation = "";
    }

    // ------------------------------------------------------------
    // Section: Getters
    // ------------------------------------------------------------

    /**
     * @return the levelNeededEquation
     */
    public String getLevelNeededEquation() {
        return levelNeededEquation;
    }

    // ------------------------------------------------------------
    // Section: Setters
    // ------------------------------------------------------------

    /**
     * @param levelNeededEquation the levelNeededEquation to set
     */
    public void setLevelNeededEquation(String levelNeededEquation) {
        this.levelNeededEquation = levelNeededEquation;
    }

    // ------------------------------------------------------------
    // Section: Utils Methods
    // ------------------------------------------------------------

    @Override
    public Class<IslandLevelPrice> getPriceType() {
        return IslandLevelPrice.class;
    }

    @Override
    public boolean isValid() {
        return levelNeededEquation != null && !levelNeededEquation.isEmpty();
    }

    @Override
    public boolean Activate() {
        // TODO Auto-generated method stub
        return true;
    }

}
