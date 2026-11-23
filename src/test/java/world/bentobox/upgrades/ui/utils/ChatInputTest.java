package world.bentobox.upgrades.ui.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.prompts.ChatInputListener;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.upgrades.UpgradesAddon;
import world.bentobox.upgrades.WhiteBox;
import world.bentobox.upgrades.config.Settings;

/**
 * Tests that {@link ChatInput} hands its questions to BentoBox's prompts and passes the
 * answers back the way its callers expect: the value on success, and null when the player
 * backs out.
 * <p>
 * Dialogs cannot be built without a server, so they are switched off here and every prompt
 * is answered in chat. Translations come back as their locale key, so BentoBox's cancel and
 * done words are the keys.
 */
class ChatInputTest {

    private static final String CANCEL = "prompts.chat.cancel-word";
    private static final String DONE = "prompts.chat.done-word";

    @Mock
    private BentoBox plugin;
    @Mock
    private UpgradesAddon addon;
    @Mock
    private Settings settings;
    @Mock
    private User user;
    @Mock
    private Player player;
    @Mock
    private BukkitScheduler sch;

    private MockedStatic<Bukkit> mockedBukkit;
    private AutoCloseable closeable;
    private final UUID uuid = UUID.randomUUID();
    private final ChatInputListener listener = new ChatInputListener();
    private final List<Object> answers = new ArrayList<>();
    private ChatInput chatInput;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        closeable = MockitoAnnotations.openMocks(this);
        WhiteBox.setInternalState(BentoBox.class, "instance", plugin);
        mockedBukkit = Mockito.mockStatic(Bukkit.class, Mockito.CALLS_REAL_METHODS);
        mockedBukkit.when(Bukkit::getScheduler).thenReturn(sch);

        world.bentobox.bentobox.Settings coreSettings = new world.bentobox.bentobox.Settings();
        coreSettings.setDialogPrompts(false);
        when(plugin.getSettings()).thenReturn(coreSettings);

        when(addon.getSettings()).thenReturn(settings);
        when(settings.getChatInputEscape()).thenReturn("END");

        when(player.getUniqueId()).thenReturn(uuid);
        when(user.isPlayer()).thenReturn(true);
        when(user.getPlayer()).thenReturn(player);
        when(user.getUniqueId()).thenReturn(uuid);
        when(user.getTranslation(anyString())).thenAnswer(inv -> inv.getArgument(0, String.class));

        chatInput = new ChatInput(addon);
    }

    @AfterEach
    void tearDown() throws Exception {
        PlayerQuitEvent quit = mock(PlayerQuitEvent.class);
        when(quit.getPlayer()).thenReturn(player);
        listener.onQuit(quit);
        mockedBukkit.closeOnDemand();
        closeable.close();
        MockBukkit.unmock();
    }

    /**
     * Sends a chat message as the player and runs the task that carries it to the main thread.
     */
    private void say(String text) {
        AsyncChatEvent chat = mock(AsyncChatEvent.class);
        when(chat.getPlayer()).thenReturn(player);
        when(chat.message()).thenReturn(Component.text(text));
        listener.onChat(chat);
        ArgumentCaptor<Runnable> task = ArgumentCaptor.forClass(Runnable.class);
        verify(sch, atLeastOnce()).runTask(eq(plugin), task.capture());
        task.getValue().run();
    }

    @Test
    void testAskOneInput() {
        chatInput.askOneInput(answers::add, input -> true, "Name?", "Bad", user, false);
        verify(user).closeInventory();
        assertTrue(ChatInputListener.isWaitingFor(uuid));

        say("My Upgrade");

        assertEquals(List.of("My Upgrade"), answers);
        assertFalse(ChatInputListener.isWaitingFor(uuid));
    }

    @Test
    void testAskOneInputSanitized() {
        List<String> checked = new ArrayList<>();
        chatInput.askOneInput(answers::add, input -> {
            checked.add(input);
            return true;
        }, "Id?", "Bad", user, true);

        say("My Up-grade");

        // Validation and the consumer both see the sanitized text
        assertEquals("my_up_grade", checked.getFirst());
        assertEquals(List.of("my_up_grade"), answers);
    }

    @Test
    void testAskOneInputInvalidIsAskedAgain() {
        chatInput.askOneInput(answers::add, input -> input.startsWith("a"), "Name?", "Bad", user, false);

        say("bee");

        assertTrue(answers.isEmpty());
        verify(user).sendRawMessage("Bad");
        assertTrue(ChatInputListener.isWaitingFor(uuid));

        say("ant");
        assertEquals(List.of("ant"), answers);
    }

    @Test
    void testAskOneInputCancelGivesNull() {
        chatInput.askOneInput(answers::add, input -> true, "Name?", "Bad", user, false);

        say(CANCEL);

        assertEquals(1, answers.size());
        assertNull(answers.getFirst());
        assertFalse(ChatInputListener.isWaitingFor(uuid));
    }

    /** The addon's own escape word still backs out, whatever the validation says */
    @Test
    void testAskOneInputEscapeWordGivesNull() {
        chatInput.askOneInput(answers::add, input -> false, "Name?", "Bad", user, false);

        say("end");

        assertEquals(1, answers.size());
        assertNull(answers.getFirst());
    }

    @Test
    void testAskMultiLine() {
        chatInput.askMultiLine(answers::add, input -> true, "Describe", "Bad", user);

        say("First");
        say("Second");
        assertTrue(answers.isEmpty());
        say(DONE);

        assertEquals(List.of(List.of("First", "Second")), answers);
    }

    /** A line holding the escape word ends the text there, as it used to end the conversation */
    @Test
    void testAskMultiLineStopsAtEscapeWord() {
        chatInput.askMultiLine(answers::add, input -> true, "Describe", "Bad", user);

        say("First");
        say("END");
        say("Ignored");
        say(DONE);

        assertEquals(List.of(List.of("First")), answers);
    }

    @Test
    void testAskMultiLineInvalidLine() {
        chatInput.askMultiLine(answers::add, input -> !input.contains("bad"), "Describe", "Bad", user);

        say("a bad line");
        say(DONE);

        assertTrue(answers.isEmpty());
        verify(user).sendRawMessage("Bad");
        assertTrue(ChatInputListener.isWaitingFor(uuid));
    }

    @Test
    void testAskMultiLineCancelGivesNull() {
        chatInput.askMultiLine(answers::add, input -> true, "Describe", "Bad", user);

        say("First");
        say(CANCEL);

        assertEquals(1, answers.size());
        assertNull(answers.getFirst());
    }

    @Test
    void testAskOneNumber() {
        chatInput.askOneNumber(answers::add, number -> number.intValue() > -1, "Order?", "Bad", user);

        say("lots");
        say("-3");
        assertTrue(answers.isEmpty());
        assertTrue(ChatInputListener.isWaitingFor(uuid));

        say("7");

        assertEquals(List.of(7), answers);
    }

    @Test
    void testAskOneNumberCancelGivesNull() {
        chatInput.askOneNumber(answers::add, number -> true, "Order?", "Bad", user);

        say(CANCEL);

        assertEquals(1, answers.size());
        assertNull(answers.getFirst());
    }

    @Test
    void testParseNumber() {
        assertEquals(42, ChatInput.parseNumber(" 42 "));
        assertEquals(5_000_000_000L, ChatInput.parseNumber("5000000000"));
        assertEquals(2.5, ChatInput.parseNumber("2.5"));
        assertNull(ChatInput.parseNumber("NaN"));
        assertNull(ChatInput.parseNumber("seven"));
    }
}
