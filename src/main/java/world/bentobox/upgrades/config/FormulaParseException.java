package world.bentobox.upgrades.config;

/**
 * Thrown when an upgrade formula from the config cannot be parsed. Extends
 * {@link RuntimeException} so existing callers that guard formula evaluation with a
 * broad catch keep working.
 */
public class FormulaParseException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FormulaParseException(String message) {
        super(message);
    }

}
