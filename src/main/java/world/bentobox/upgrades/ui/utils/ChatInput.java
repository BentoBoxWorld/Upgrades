package world.bentobox.upgrades.ui.utils;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.ValidatingPrompt;

import com.mongodb.lang.NonNull;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.upgrades.UpgradesAddon;

public class ChatInput {
	
	// ------------------------------------------------------------
	// Section: Variables
	// ------------------------------------------------------------
	
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
	 * Start conversation with user about question
	 * If validation return true about user input
	 * Then call consumer with user input
	 * If the user cancel the conversation or if it timeout, then consumer get null
	 * InvalidText used when player input is refused by validation
	 * 
	 * @param consumer Called when conversation end
	 * @param validation Called for checking input
	 * @param question Showed to the user for asking input
	 * @param invalidText Showed to the user when it's input is invalid
	 * @param user User to converse with
	 */
	public void askOneInput(Consumer<String> consumer, Function<String, Boolean> validation, String question, String invalidText, User user) {
		// Create conversation
		Conversation conv = new ConversationFactory(this.addon.getPlugin())
			// Can escape conversation by using the chat-input-escape value in setting
			.withEscapeSequence(this.addon.getSettings().getChatInputEscape())
			// Display user input
			.withLocalEcho(true)
			// When conversation end
			.addConversationAbandonedListener(abandoned -> {
				// If conversation was ended by timeout or cancel
				if (!abandoned.gracefulExit())
					consumer.accept(null);
			})
			.withFirstPrompt(new ValidatingPrompt() {
				
				@Override
				public String getPromptText(ConversationContext context) {
					// Close user inventory
					user.closeInventory();
					return question;
				}
				
				@Override
				protected boolean isInputValid(ConversationContext context, String input) {
					// Get clean input, lowcase, no ' ' and no '-'
					String clean = sanitizeInput(input);
					// Check if input is valid
					return validation.apply(clean);
				}
				
				@Override
				protected Prompt acceptValidatedInput(ConversationContext context, String input) {
					// Get clean input, lowcase, no ' ' and no '-'
					String clean = sanitizeInput(input);
					// Call consumer with user input
					consumer.accept(clean);
					// End conversation
					return Prompt.END_OF_CONVERSATION;
				}
				
				@Override
				protected String getFailedValidationText(ConversationContext context, String invalidInput) {
					return invalidInput;
				}
			})
			.buildConversation(user.getPlayer());
		
		// Start conversation
		conv.begin();
	}
	
	// ------------------------------------------------------------
	// Section: Utils methods
	// ------------------------------------------------------------
	
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
