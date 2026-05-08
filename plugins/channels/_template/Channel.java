package dev.synapse.plugins.channel;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Core interface that every SYNAPSE channel plugin must implement.
 *
 * <p>A <em>channel</em> is a bidirectional integration with an external messaging
 * platform (e.g. Telegram, Discord, WhatsApp). SYNAPSE discovers channel plugins at
 * startup, loads their manifests, and calls the lifecycle hooks defined here in
 * response to admin actions and incoming/outgoing messages.
 *
 * <h2>Lifecycle</h2>
 * <ol>
 *   <li>Admin installs plugin → {@link #onInstall(Map)} is called with the
 *       validated config map from the manifest schema.</li>
 *   <li>Platform sends a message → SYNAPSE calls {@link #onMessage(ChannelMessage)}.</li>
 *   <li>Agent replies → SYNAPSE calls {@link #sendMessage(String, String, String)}.</li>
 *   <li>Admin uninstalls plugin → {@link #onUninstall()} is called so the plugin
 *       can release resources and deregister webhooks.</li>
 * </ol>
 *
 * <h2>Threading model</h2>
 * All hook methods may be called concurrently by SYNAPSE's virtual-thread executor
 * (Project Loom). Implementations must be thread-safe or delegate to thread-safe
 * clients provided by their underlying SDK.
 *
 * <h2>Java 25 features used</h2>
 * <ul>
 *   <li>Records for {@link ChannelMessage} and related value types.</li>
 *   <li>Sealed interface hierarchy for {@link ChannelStatus}.</li>
 *   <li>Pattern matching in switch expressions where applicable.</li>
 * </ul>
 *
 * @since 0.1.0
 */
public interface Channel {

    // -------------------------------------------------------------------------
    // Identity
    // -------------------------------------------------------------------------

    /**
     * Returns the unique plugin identifier as declared in {@code manifest.yml}.
     *
     * <p>SYNAPSE uses this id to route messages, store configuration, and
     * look up the plugin in the registry. The value must be stable across
     * plugin versions.
     *
     * @return non-null, non-empty plugin id (e.g. {@code "telegram-channel"})
     */
    String getId();

    // -------------------------------------------------------------------------
    // Lifecycle hooks
    // -------------------------------------------------------------------------

    /**
     * Called once by SYNAPSE after the plugin is installed or its configuration
     * is updated by an admin.
     *
     * <p>Implementations should:
     * <ul>
     *   <li>Extract and validate all required config values from {@code config}.</li>
     *   <li>Initialise the SDK client or HTTP connection to the remote platform.</li>
     *   <li>Register a webhook with the remote platform if applicable, or start a
     *       background polling loop using a virtual thread.</li>
     * </ul>
     *
     * <p>If initialisation fails, throw an {@link IllegalStateException} with a
     * descriptive message. SYNAPSE will mark the plugin as {@link ChannelStatus#ERROR}
     * and surface the error to the admin.
     *
     * @param config validated key/value map derived from the manifest
     *               {@code config_schema}; keys match field names defined there
     * @throws IllegalArgumentException if a required config value is missing or invalid
     * @throws IllegalStateException    if the remote platform connection cannot be established
     */
    void onInstall(Map<String, Object> config);

    /**
     * Called once by SYNAPSE when the plugin is uninstalled or SYNAPSE is shutting down.
     *
     * <p>Implementations should:
     * <ul>
     *   <li>Deregister any webhooks registered during {@link #onInstall(Map)}.</li>
     *   <li>Interrupt and join any background polling threads/virtual threads.</li>
     *   <li>Close HTTP clients and release other I/O resources.</li>
     * </ul>
     *
     * <p>This method must not throw. Swallow exceptions and log them instead so that
     * SYNAPSE can continue its shutdown sequence uninterrupted.
     */
    void onUninstall();

    /**
     * Called by SYNAPSE whenever the channel receives an inbound message from a user.
     *
     * <p>The method is responsible for:
     * <ul>
     *   <li>Parsing the raw platform payload into a {@link ChannelMessage}.</li>
     *   <li>Applying any allowlist / blocklist logic.</li>
     *   <li>Dispatching the message to the SYNAPSE agent router.</li>
     * </ul>
     *
     * <p>For webhook-based channels this method is typically called from the webhook
     * controller. For polling-based channels it is called from the polling loop.
     *
     * @param message the inbound message parsed from the platform payload; never null
     */
    void onMessage(ChannelMessage message);

    /**
     * Sends a text message from a SYNAPSE agent to a specific recipient on this channel.
     *
     * <p>Implementations must use the platform SDK to deliver the message. If the
     * platform supports Markdown, formatting may be applied before sending.
     *
     * <p>This method is called by SYNAPSE in response to an agent completing a turn
     * and producing a reply. It maps to the {@code on_send} manifest hook.
     *
     * @param agentId     id of the SYNAPSE agent sending the reply
     * @param content     plain-text or Markdown content of the message to send
     * @param recipientId platform-specific id of the recipient (e.g. Telegram chat id,
     *                    Discord channel id)
     * @throws RuntimeException if the platform API call fails after all retries
     */
    void sendMessage(String agentId, String content, String recipientId);

    // -------------------------------------------------------------------------
    // Status
    // -------------------------------------------------------------------------

    /**
     * Returns the current operational status of this channel plugin.
     *
     * <p>SYNAPSE polls this method to update the admin dashboard and to decide
     * whether to route messages through this channel.
     *
     * @return current {@link ChannelStatus}; never null
     */
    ChannelStatus getStatus();

    // =========================================================================
    // Inner types
    // =========================================================================

    /**
     * Immutable value object representing a single inbound message received on a channel.
     *
     * <p>Created by the channel implementation inside {@link #onMessage(ChannelMessage)}
     * and passed to the SYNAPSE agent router for processing.
     *
     * @param messageId   platform-specific unique identifier of the message
     * @param channelId   id of this channel plugin (matches {@link Channel#getId()})
     * @param senderId    platform-specific identifier of the user who sent the message
     * @param senderName  display name of the sender, if available; may be null
     * @param content     raw text content of the message
     * @param recipientId platform-specific id of the conversation, chat, or channel
     *                    where the reply should be sent (passed back to
     *                    {@link #sendMessage(String, String, String)})
     * @param timestamp   UTC instant when the message was received by the platform
     * @param metadata    arbitrary key/value pairs for platform-specific extras
     *                    (e.g. {@code "reply_to_message_id"}, {@code "thread_id"});
     *                    may be an empty map but never null
     */
    record ChannelMessage(
            String messageId,
            String channelId,
            String senderId,
            String senderName,
            String content,
            String recipientId,
            Instant timestamp,
            Map<String, Object> metadata
    ) {
        /**
         * Compact constructor: defensively copies the metadata map so that the record
         * is truly immutable even if the caller retains a reference to the original map.
         */
        public ChannelMessage {
            metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        }
    }

    /**
     * Sealed interface representing the possible operational states of a channel plugin.
     *
     * <p>Using a sealed interface (rather than a plain enum) allows future versions of
     * SYNAPSE to add richer status types (e.g. carrying an error cause) without
     * breaking existing switch expressions that already handle all permitted subtypes.
     *
     * <p>For typical use, prefer the singleton constants exposed by the implementing
     * enums: {@link ChannelStatus.Simple#CONNECTED},
     * {@link ChannelStatus.Simple#DISCONNECTED}, and
     * {@link ChannelStatus.Simple#ERROR}.
     */
    sealed interface ChannelStatus permits ChannelStatus.Simple, ChannelStatus.ErrorStatus {

        /** Returns a short, human-readable label suitable for display in the admin UI. */
        String label();

        /**
         * The three standard status values shared by all channel implementations.
         */
        enum Simple implements ChannelStatus {
            /** The channel is connected to the remote platform and ready to route messages. */
            CONNECTED {
                @Override
                public String label() { return "Connected"; }
            },

            /** The channel has been cleanly disconnected (e.g. after uninstall or shutdown). */
            DISCONNECTED {
                @Override
                public String label() { return "Disconnected"; }
            },

            /** The channel encountered an unrecoverable error; admin intervention is required. */
            ERROR {
                @Override
                public String label() { return "Error"; }
            }
        }

        /**
         * Rich error status that carries the causing exception, allowing SYNAPSE to
         * display a detailed error message in the admin dashboard.
         *
         * @param cause     the exception that caused the error state
         * @param errorCode short machine-readable error code (e.g. {@code "AUTH_FAILED"})
         */
        record ErrorStatus(Throwable cause, String errorCode) implements ChannelStatus {
            @Override
            public String label() {
                return "Error [" + errorCode + "]: " + cause.getMessage();
            }
        }
    }
}
