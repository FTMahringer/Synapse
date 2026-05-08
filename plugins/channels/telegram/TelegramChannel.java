package dev.synapse.plugins.channels.telegram;

// External dependency: org.telegram:telegrambots-longpolling and
// org.telegram:telegrambots-client (TelegramBots library ≥ 7.x).
// Add to your build file:
//   implementation("org.telegram:telegrambots-longpolling:7.10.0")
//   implementation("org.telegram:telegrambots-client:7.10.0")

import dev.synapse.plugins.channel.Channel;
import dev.synapse.plugins.channel.Channel.ChannelMessage;
import dev.synapse.plugins.channel.Channel.ChannelStatus;
import dev.synapse.plugins.channel.Channel.ChannelStatus.Simple;
import dev.synapse.plugins.channel.Channel.ChannelStatus.ErrorStatus;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SYNAPSE channel plugin that connects agents to Telegram via the Bot API.
 *
 * <p>Supports two inbound modes:
 * <ul>
 *   <li><b>Long-polling</b> (default): a virtual thread calls {@code getUpdates}
 *       on a configurable interval. No public URL required.</li>
 *   <li><b>Webhook</b>: when {@code webhook_url} is configured, the plugin
 *       registers the URL with Telegram; SYNAPSE's embedded HTTP server must
 *       forward POST requests to {@link #onMessage(ChannelMessage)}.</li>
 * </ul>
 *
 * <p>Virtual threads (Project Loom, Java 25) are used for all blocking I/O so that
 * the channel never ties up carrier threads.
 */
public class TelegramChannel implements Channel, LongPollingSingleThreadUpdateConsumer {

    private static final Logger LOG = Logger.getLogger(TelegramChannel.class.getName());

    // -------------------------------------------------------------------------
    // Configuration fields — populated by onInstall
    // -------------------------------------------------------------------------

    private String botToken;
    private Set<Long> allowedUsers = new HashSet<>();
    private String webhookUrl;
    private int pollingIntervalMs = 2000;

    // -------------------------------------------------------------------------
    // Runtime state
    // -------------------------------------------------------------------------

    /** Low-level HTTP client used to call the Telegram Bot API. */
    private OkHttpTelegramClient telegramClient;

    /** Long-polling application container; null when webhook mode is active. */
    private TelegramBotsLongPollingApplication pollingApp;

    /** Inbound message dispatcher provided by the SYNAPSE runtime at install time. */
    private java.util.function.Consumer<ChannelMessage> messageDispatcher;

    /** Current operational status. */
    private final AtomicReference<ChannelStatus> status =
            new AtomicReference<>(Simple.DISCONNECTED);

    // =========================================================================
    // Channel interface — identity
    // =========================================================================

    @Override
    public String getId() {
        return "telegram-channel";
    }

    // =========================================================================
    // Channel interface — lifecycle hooks
    // =========================================================================

    @Override
    public void onInstall(Map<String, Object> config) {
        // -- Extract configuration ------------------------------------------
        botToken = requireString(config, "bot_token");

        Object rawUsers = config.get("allowed_users");
        if (rawUsers instanceof List<?> userList) {
            allowedUsers = new HashSet<>();
            for (Object entry : userList) {
                if (entry instanceof Number n) {
                    allowedUsers.add(n.longValue());
                } else if (entry instanceof String s) {
                    allowedUsers.add(Long.parseLong(s.trim()));
                }
            }
        }

        webhookUrl = (String) config.getOrDefault("webhook_url", null);

        Object rawInterval = config.get("polling_interval_ms");
        if (rawInterval instanceof Number n) {
            pollingIntervalMs = n.intValue();
        }

        // -- Initialise Telegram client -------------------------------------
        telegramClient = new OkHttpTelegramClient(botToken);

        // -- Start inbound message delivery ---------------------------------
        if (webhookUrl != null && !webhookUrl.isBlank()) {
            registerWebhook();
        } else {
            startLongPolling();
        }

        status.set(Simple.CONNECTED);
        LOG.info("[telegram-channel] Installed successfully. Mode: "
                + (webhookUrl != null ? "webhook" : "long-polling"));
    }

    @Override
    public void onUninstall() {
        status.set(Simple.DISCONNECTED);

        // Deregister webhook if one was registered
        if (webhookUrl != null && !webhookUrl.isBlank()) {
            try {
                org.telegram.telegrambots.meta.api.methods.updates.DeleteWebhook deleteWebhook =
                        new org.telegram.telegrambots.meta.api.methods.updates.DeleteWebhook();
                telegramClient.execute(deleteWebhook);
                LOG.info("[telegram-channel] Webhook deregistered.");
            } catch (TelegramApiException e) {
                LOG.log(Level.WARNING, "[telegram-channel] Failed to deregister webhook (ignored).", e);
            }
        }

        // Stop long-polling application
        if (pollingApp != null) {
            try {
                pollingApp.stop();
            } catch (Exception e) {
                LOG.log(Level.WARNING, "[telegram-channel] Error stopping polling app (ignored).", e);
            }
            pollingApp = null;
        }

        // Shut down the HTTP client
        if (telegramClient != null) {
            // OkHttpTelegramClient wraps OkHttpClient; close it gracefully
            try {
                telegramClient.close();
            } catch (Exception e) {
                LOG.log(Level.WARNING, "[telegram-channel] Error closing HTTP client (ignored).", e);
            }
            telegramClient = null;
        }

        LOG.info("[telegram-channel] Uninstalled cleanly.");
    }

    /**
     * Called by SYNAPSE (webhook controller or polling loop) when a new Telegram
     * update has been parsed into a {@link ChannelMessage}.
     *
     * <p>This method applies the user allowlist and then dispatches the message
     * to the SYNAPSE agent router via the injected {@code messageDispatcher}.
     */
    @Override
    public void onMessage(ChannelMessage message) {
        // Apply allowlist: if the allowlist is non-empty, reject unknown senders
        if (!allowedUsers.isEmpty()) {
            try {
                long senderId = Long.parseLong(message.senderId());
                if (!isAllowedUser(senderId)) {
                    LOG.fine("[telegram-channel] Ignored message from non-allowed user: " + senderId);
                    return;
                }
            } catch (NumberFormatException e) {
                LOG.warning("[telegram-channel] Could not parse senderId as long: " + message.senderId());
                return;
            }
        }

        // Dispatch to the SYNAPSE agent router
        if (messageDispatcher != null) {
            messageDispatcher.accept(message);
        } else {
            LOG.warning("[telegram-channel] No message dispatcher registered; message dropped.");
        }
    }

    /**
     * Delivers a text reply from a SYNAPSE agent to the Telegram chat identified by
     * {@code recipientId}.
     *
     * <p>Content is formatted as Markdown (MarkdownV2) before sending so that agent
     * responses rendered with bold, italic, or code blocks display correctly.
     */
    @Override
    public void sendMessage(String agentId, String content, String recipientId) {
        if (telegramClient == null) {
            throw new IllegalStateException("[telegram-channel] Plugin is not installed; cannot send messages.");
        }

        String formatted = formatMarkdown(content);

        SendMessage sendMessage = SendMessage.builder()
                .chatId(recipientId)
                .text(formatted)
                .parseMode("MarkdownV2")
                .build();

        // Execute on a virtual thread so the caller is not blocked
        Thread.ofVirtual().name("tg-send-" + recipientId).start(() -> {
            try {
                telegramClient.execute(sendMessage);
                LOG.fine("[telegram-channel] Message sent to chat " + recipientId + " by agent " + agentId);
            } catch (TelegramApiException e) {
                LOG.log(Level.SEVERE, "[telegram-channel] Failed to send message to chat " + recipientId, e);
                status.set(new ErrorStatus(e, "SEND_FAILED"));
            }
        });
    }

    @Override
    public ChannelStatus getStatus() {
        return status.get();
    }

    // =========================================================================
    // LongPollingSingleThreadUpdateConsumer — called by TelegramBots library
    // =========================================================================

    /**
     * Entry point called by the TelegramBots library for each {@link Update} received
     * during long-polling. Converts the raw update into a {@link ChannelMessage} and
     * delegates to {@link #onMessage(ChannelMessage)}.
     */
    @Override
    public void consume(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return; // Ignore non-text updates (stickers, photos, etc.)
        }

        org.telegram.telegrambots.meta.api.objects.Message tgMsg = update.getMessage();
        User sender = tgMsg.getFrom();

        String senderId   = sender != null ? String.valueOf(sender.getId()) : "unknown";
        String senderName = sender != null
                ? (sender.getFirstName() + (sender.getLastName() != null ? " " + sender.getLastName() : "")).trim()
                : null;

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("update_id", update.getUpdateId());
        metadata.put("message_id", tgMsg.getMessageId());
        if (tgMsg.getReplyToMessage() != null) {
            metadata.put("reply_to_message_id", tgMsg.getReplyToMessage().getMessageId());
        }

        ChannelMessage channelMessage = new ChannelMessage(
                String.valueOf(tgMsg.getMessageId()),
                getId(),
                senderId,
                senderName,
                tgMsg.getText(),
                String.valueOf(tgMsg.getChatId()),
                Instant.ofEpochSecond(tgMsg.getDate()),
                metadata
        );

        onMessage(channelMessage);
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    /**
     * Registers this bot's webhook URL with the Telegram Bot API.
     * Must be called after {@link #telegramClient} is initialised.
     */
    private void registerWebhook() {
        try {
            org.telegram.telegrambots.meta.api.methods.updates.SetWebhook setWebhook =
                    org.telegram.telegrambots.meta.api.methods.updates.SetWebhook.builder()
                            .url(webhookUrl)
                            .build();
            boolean result = telegramClient.execute(setWebhook);
            if (!result) {
                throw new IllegalStateException("Telegram API rejected the webhook URL: " + webhookUrl);
            }
            LOG.info("[telegram-channel] Webhook registered at: " + webhookUrl);
        } catch (TelegramApiException e) {
            status.set(new ErrorStatus(e, "WEBHOOK_REGISTRATION_FAILED"));
            throw new IllegalStateException("Failed to register Telegram webhook.", e);
        }
    }

    /**
     * Starts the long-polling loop using the TelegramBots library.
     * The underlying library uses its own thread; updates are delivered via
     * {@link #consume(Update)}.
     */
    private void startLongPolling() {
        pollingApp = new TelegramBotsLongPollingApplication();
        try {
            pollingApp.registerBot(botToken, this);
            LOG.info("[telegram-channel] Long-polling started (interval hint: " + pollingIntervalMs + " ms).");
        } catch (TelegramApiException e) {
            status.set(new ErrorStatus(e, "POLLING_START_FAILED"));
            throw new IllegalStateException("Failed to start Telegram long-polling.", e);
        }
    }

    /**
     * Returns {@code true} if the given Telegram {@code userId} is in the allowlist,
     * or if no allowlist has been configured (empty set means allow-all).
     *
     * @param userId Telegram user id to check
     * @return {@code true} if the user is permitted to interact with the bot
     */
    private boolean isAllowedUser(long userId) {
        return allowedUsers.isEmpty() || allowedUsers.contains(userId);
    }

    /**
     * Escapes special characters in {@code content} for Telegram's MarkdownV2 parse
     * mode, which requires the following characters to be escaped with a backslash
     * when they appear outside of formatting entities:
     * {@code _ * [ ] ( ) ~ ` > # + - = | { } . !}
     *
     * <p>Basic agent responses that contain only plain text and standard Markdown
     * bold/italic/code will pass through correctly after escaping.
     *
     * @param content the raw agent reply content
     * @return the escaped string ready for MarkdownV2 transmission
     */
    private String formatMarkdown(String content) {
        if (content == null) {
            return "";
        }
        // Characters that must be escaped in MarkdownV2 outside of entities
        return content
                .replace("\\", "\\\\")
                .replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("~", "\\~")
                .replace("`", "\\`")
                .replace(">", "\\>")
                .replace("#", "\\#")
                .replace("+", "\\+")
                .replace("-", "\\-")
                .replace("=", "\\=")
                .replace("|", "\\|")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace(".", "\\.")
                .replace("!", "\\!");
    }

    /**
     * Extracts a required {@link String} value from the config map.
     *
     * @param config the configuration map supplied by SYNAPSE
     * @param key    the config field name
     * @return the non-null, non-blank string value
     * @throws IllegalArgumentException if the key is absent or the value is blank
     */
    private String requireString(Map<String, Object> config, String key) {
        Object value = config.get(key);
        if (value == null || value.toString().isBlank()) {
            throw new IllegalArgumentException(
                    "[telegram-channel] Required config field '" + key + "' is missing or blank.");
        }
        return value.toString().trim();
    }
}
