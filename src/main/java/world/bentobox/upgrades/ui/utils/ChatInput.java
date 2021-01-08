package world.bentobox.upgrades.ui.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.NumericPrompt;
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
	public void askOneInput(Consumer<String> consumer, Function<String, Boolean> validation, String question, String invalidText, User user, boolean sanitize) {
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
					if (sanitize) {
						// Get clean input, lowcase, no ' ' and no '-'
						input = sanitizeInput(input);
					}
					// Check if input is valid
					return validation.apply(input);
				}
				
				@Override
				protected Prompt acceptValidatedInput(ConversationContext context, String input) {
					if (sanitize) {
						// Get clean input, lowcase, no ' ' and no '-'
						input = sanitizeInput(input);
					}
					// Call consumer with user input
					consumer.accept(input);
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
	
	public void askMultiLine(Consumer<List<String>> consumer, Function<String, Boolean> validation, String question, String invalidText, User user) {
		List<String> list = new ArrayList<String>();
		UpgradesAddon addon = this.addon;
		Conversation conv = new ConversationFactory(addon.getPlugin())
			.withEscapeSequence(addon.getSettings().getChatInputEscape())
			.addConversationAbandonedListener(abandoned -> {
				consumer.accept(list);
			})
			.withFirstPrompt(new ValidatingPrompt() {
				
				boolean sayMessage = true;
				
				@Override
				public String getPromptText(ConversationContext context) {
					user.closeInventory();
					String message = sayMessage ? question : "";
					sayMessage = false;
					return message;
				}
				
				@Override
				protected boolean isInputValid(ConversationContext context, String input) {
					return validation.apply(input);
				}
				
				@Override
				protected Prompt acceptValidatedInput(ConversationContext context, String input) {
					list.add(input);
					return this;
				}
			})
			.buildConversation(user.getPlayer());
		
		conv.begin();
	}
	
	public void askOneNumber(Consumer<Number> consumer, Function<Number, Boolean> validation, String question, String invalidText, User user) {
		Conversation conv = new ConversationFactory(this.addon.getPlugin())
			.withEscapeSequence(this.addon.getSettings().getChatInputEscape())
			.addConversationAbandonedListener(abandoned -> {
				if (!abandoned.gracefulExit())
					consumer.accept(null);
			}).withFirstPrompt(new NumericPrompt() {
				
				@Override
				public String getPromptText(ConversationContext context) {
					user.closeInventory();
					return question;
				}
				
				@Override
				protected String getFailedValidationText(ConversationContext context, String invalidInput) {
					return invalidText;
				}
				
				@Override
				protected boolean isNumberValid(ConversationContext context, Number input) {
					return super.isNumberValid(context, input) && validation.apply(input);
				}
				
				@Override
				protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
					consumer.accept(input);
					return Prompt.END_OF_CONVERSATION;
				}
			})
			.buildConversation(user.getPlayer());
		
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
