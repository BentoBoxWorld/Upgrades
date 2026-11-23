package world.bentobox.upgrades.ui.utils;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import net.kyori.adventure.text.Component;
import world.bentobox.bentobox.api.prompts.Prompts;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.upgrades.UpgradesAddon;

/**
 * Asks a player for typed input. These methods keep the shape they had when they were
 * built on Bukkit's conversation API, which Paper is removing, and now hand the work to
 * BentoBox's {@link Prompts}: the player gets a dialog with a text box where the server
 * supports dialogs, and is asked in chat otherwise.
 * <p>
 * The <code>chat-input-escape</code> word from the addon's config still works. Typed as a
 * single-line answer it cancels, and in a multi-line answer it ends the text at that line.
 */
public class ChatInput {

	// ------------------------------------------------------------
	// Section: Variables
	// ------------------------------------------------------------

	/**
	 * The heading of every prompt. The question itself is shown under it.
	 */
	private static final Component TITLE = Component.text("Upgrades");

	private UpgradesAddon addon;

	// ------------------------------------------------------------
	// Section: Constructor
	// ------------------------------------------------------------

	/**
	 * Init ChatInput
	 *
	 * @param addon
	 */
	public ChatInput(@NonNull UpgradesAddon addon) {
		this.addon = addon;
	}

	// ------------------------------------------------------------
	// Section: Input methods
	// ------------------------------------------------------------

	/**
	 * Closes the user's inventory and asks for one line of text.
	 * If validation returns true for the input, the consumer is called with it.
	 * If the user cancels, types the escape word or does not answer, the consumer gets null.
	 *
	 * @param consumer Called when the question has been answered or abandoned
	 * @param validation Called for checking input
	 * @param question Showed to the user for asking input
	 * @param invalidText Showed to the user when its input is invalid
	 * @param user User to ask
	 * @param sanitize Whether the input is {@link #sanitizeInput(String) sanitized} before it is checked and handed over
	 */
	public void askOneInput(Consumer<String> consumer, Predicate<String> validation, String question, String invalidText, User user, boolean sanitize) {
		user.closeInventory();
		Prompts.text(user)
			.title(TITLE)
			.message(Util.parseMiniMessageOrLegacy(question))
			// The escape word is let through here and turned into a cancel below
			.validate(input -> this.isEscape(input) || validation.test(clean(input, sanitize)) ? null : invalidText)
			.onSubmit(input -> consumer.accept(this.isEscape(input) ? null : clean(input, sanitize)))
			.onCancel(() -> consumer.accept(null))
			.open();
	}

	/**
	 * Closes the user's inventory and asks for several lines of text.
	 * Every line must pass validation. A line holding only the escape word ends the text there.
	 * If the user cancels or does not answer, the consumer gets null.
	 *
	 * @param consumer Called when the question has been answered or abandoned
	 * @param validation Called for checking each line
	 * @param question Showed to the user for asking input
	 * @param invalidText Showed to the user when a line is invalid
	 * @param user User to ask
	 */
	public void askMultiLine(Consumer<List<String>> consumer, Predicate<String> validation, String question, String invalidText, User user) {
		user.closeInventory();
		Prompts.lines(user)
			.title(TITLE)
			.message(Util.parseMiniMessageOrLegacy(question))
			.validate(lines -> this.untilEscape(lines).stream().allMatch(validation) ? null : invalidText)
			.onSubmit(lines -> consumer.accept(this.untilEscape(lines)))
			.onCancel(() -> consumer.accept(null))
			.open();
	}

	/**
	 * Closes the user's inventory and asks for a number.
	 * If the input is a number and validation returns true for it, the consumer is called with it.
	 * If the user cancels, types the escape word or does not answer, the consumer gets null.
	 *
	 * @param consumer Called when the question has been answered or abandoned
	 * @param validation Called for checking the number
	 * @param question Showed to the user for asking input
	 * @param invalidText Showed to the user when its input is not a number or is invalid
	 * @param user User to ask
	 */
	public void askOneNumber(Consumer<Number> consumer, Predicate<Number> validation, String question, String invalidText, User user) {
		user.closeInventory();
		Prompts.text(user)
			.title(TITLE)
			.message(Util.parseMiniMessageOrLegacy(question))
			.validate(input -> {
				if (this.isEscape(input)) {
					return null;
				}
				Number number = parseNumber(input);
				return number != null && validation.test(number) ? null : invalidText;
			})
			.onSubmit(input -> consumer.accept(this.isEscape(input) ? null : parseNumber(input)))
			.onCancel(() -> consumer.accept(null))
			.open();
	}

	// ------------------------------------------------------------
	// Section: Utils methods
	// ------------------------------------------------------------

	private boolean isEscape(String input) {
		String escape = this.addon.getSettings().getChatInputEscape();
		return escape != null && !escape.isBlank() && escape.equalsIgnoreCase(input.strip());
	}

	/**
	 * @return the lines before the first one that holds only the escape word
	 */
	private List<String> untilEscape(List<String> lines) {
		for (int i = 0; i < lines.size(); i++) {
			if (this.isEscape(lines.get(i))) {
				return List.copyOf(lines.subList(0, i));
			}
		}
		return lines;
	}

	private static String clean(String input, boolean sanitize) {
		return sanitize ? sanitizeInput(input) : input;
	}

	/**
	 * @return the input as an Integer or Long if it is a whole number that fits, otherwise
	 *         as a Double, or null if it is not a number
	 */
	@Nullable
	static Number parseNumber(String input) {
		String text = input.strip();
		try {
			long whole = Long.parseLong(text);
			if (whole >= Integer.MIN_VALUE && whole <= Integer.MAX_VALUE) {
				return (int) whole;
			}
			return whole;
		} catch (NumberFormatException _) {
			// Not a whole number
		}
		try {
			double value = Double.parseDouble(text);
			return Double.isNaN(value) || Double.isInfinite(value) ? null : value;
		} catch (NumberFormatException _) {
			return null;
		}
	}

	/**
	 * Sanitizes the provided input.
	 * It replaces spaces and hyphens with underscores and lower cases the input.
	 * @param input input to sanitize
	 * @return sanitized input
	 * @author BONNe
	 */
	public static String sanitizeInput(String input)
	{
		return input.toLowerCase(Locale.ENGLISH).replace(" ", "_").replace("-", "_");
	}

}
