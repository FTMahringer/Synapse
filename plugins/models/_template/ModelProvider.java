package dev.synapse.plugins.model;

import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * Core interface that every SYNAPSE model provider plugin must implement.
 *
 * <p>A <em>model provider</em> wraps an external LLM API (e.g. Anthropic, OpenAI,
 * Ollama) and exposes a unified interface that the SYNAPSE agent runtime uses to
 * generate completions, stream tokens, introspect available models, and check
 * provider capabilities.
 *
 * <h2>Lifecycle</h2>
 * <ol>
 *   <li>Admin installs plugin → {@link #configure(Map)} is called with the validated
 *       config map.</li>
 *   <li>Agent requests a completion → SYNAPSE calls {@link #complete(ModelRequest)} or
 *       {@link #stream(ModelRequest)} depending on whether streaming is enabled.</li>
 *   <li>Admin uninstalls plugin → SYNAPSE calls a shutdown hook (not declared here)
 *       to release HTTP clients and other resources.</li>
 * </ol>
 *
 * <h2>Reactive streaming</h2>
 * {@link #stream(ModelRequest)} returns a {@link Flux}{@code <String>} of token
 * fragments. SYNAPSE subscribes on a virtual-thread scheduler and merges fragments
 * into the agent's response stream. Implementations backed by SSE-based APIs should
 * use the provider SDK's streaming client directly and wrap it in {@code Flux.create}.
 *
 * <h2>Java 25 features used</h2>
 * <ul>
 *   <li>Records for all value objects ({@link ModelRequest}, {@link ModelResponse},
 *       {@link Message}, {@link ModelCapabilities}).</li>
 *   <li>Sealed interface for {@link ProviderStatus} to allow exhaustive pattern
 *       matching in switch expressions.</li>
 *   <li>Enum for {@link Message.Role} with pattern matching support.</li>
 * </ul>
 *
 * @since 0.1.0
 */
public interface ModelProvider {

    // -------------------------------------------------------------------------
    // Identity
    // -------------------------------------------------------------------------

    /**
     * Returns the unique plugin identifier as declared in {@code manifest.yml}.
     *
     * @return non-null, non-empty provider id (e.g. {@code "anthropic"})
     */
    String getId();

    // -------------------------------------------------------------------------
    // Lifecycle hooks
    // -------------------------------------------------------------------------

    /**
     * Called by SYNAPSE when the plugin is installed or its configuration is updated.
     *
     * <p>Implementations should:
     * <ul>
     *   <li>Extract and validate all required config values (API key, base URL, etc.).</li>
     *   <li>Initialise the provider SDK or HTTP client.</li>
     *   <li>Optionally perform a lightweight health-check call to verify credentials.</li>
     * </ul>
     *
     * @param config validated key/value map derived from the manifest
     *               {@code config_schema}; keys match field names defined there
     * @throws IllegalArgumentException if a required config value is missing or invalid
     * @throws IllegalStateException    if the provider cannot be reached with the
     *                                  supplied credentials
     */
    void configure(Map<String, Object> config);

    // -------------------------------------------------------------------------
    // Completion
    // -------------------------------------------------------------------------

    /**
     * Generates a synchronous (non-streaming) completion for the given request.
     *
     * <p>The method blocks until the full response has been received from the
     * provider API. For long responses, prefer {@link #stream(ModelRequest)}.
     *
     * <p>SYNAPSE calls this method on a virtual thread so blocking I/O is safe.
     *
     * @param request the completion request including model, messages, and parameters
     * @return a {@link ModelResponse} containing the generated content and metadata
     * @throws IllegalStateException if the provider is not yet configured
     * @throws RuntimeException      if the provider API returns an error
     */
    ModelResponse complete(ModelRequest request);

    /**
     * Generates a streaming completion for the given request.
     *
     * <p>Returns a {@link Flux} that emits token fragments as they arrive from the
     * provider API. The Flux completes when the full response has been received, or
     * errors if the connection is interrupted.
     *
     * <p>SYNAPSE subscribes on its virtual-thread scheduler. Implementations should
     * not block inside the Flux's {@code create} callback; use async SDK methods or
     * a separate virtual thread to avoid backpressure issues.
     *
     * @param request the completion request; must target a model that supports streaming
     * @return cold {@link Flux} of token fragment strings; never null
     * @throws IllegalStateException if the provider is not yet configured or does
     *                               not support streaming
     */
    Flux<String> stream(ModelRequest request);

    // -------------------------------------------------------------------------
    // Model enumeration
    // -------------------------------------------------------------------------

    /**
     * Returns the list of model identifiers available through this provider.
     *
     * <p>Implementations may return a static list (from the manifest
     * {@code supported_models} field) or fetch the list dynamically from the
     * provider API on each call.
     *
     * @return non-null, possibly empty list of model identifier strings
     */
    List<String> listModels();

    // -------------------------------------------------------------------------
    // Introspection
    // -------------------------------------------------------------------------

    /**
     * Returns the static capability set of this provider.
     *
     * <p>SYNAPSE uses these flags to decide which completion path to take and which
     * UI features to expose for agents using this provider.
     *
     * @return non-null {@link ModelCapabilities} record
     */
    ModelCapabilities getCapabilities();

    /**
     * Returns the current operational status of this provider plugin.
     *
     * @return non-null {@link ProviderStatus}; never null
     */
    ProviderStatus getStatus();

    // =========================================================================
    // Inner types
    // =========================================================================

    /**
     * Represents the role of a participant in a conversation.
     */
    enum Role {
        /** A message authored by the end-user or external system. */
        USER,
        /** A message authored by the AI assistant. */
        ASSISTANT,
        /** A system-level instruction that frames the assistant's behaviour. */
        SYSTEM
    }

    /**
     * Immutable value object representing a single message in a conversation.
     *
     * @param role    the participant role ({@link Role#USER}, {@link Role#ASSISTANT},
     *                or {@link Role#SYSTEM})
     * @param content the text content of the message; may contain Markdown
     */
    record Message(Role role, String content) {
        public Message {
            if (role == null) throw new IllegalArgumentException("role must not be null");
            if (content == null) content = "";
        }
    }

    /**
     * Immutable value object representing a tool/function that the model may call.
     *
     * @param name        machine-readable tool name (snake_case recommended)
     * @param description human-readable description of what the tool does
     * @param inputSchema JSON Schema string describing the tool's input parameters
     */
    record Tool(String name, String description, String inputSchema) {}

    /**
     * Immutable value object encapsulating all parameters for a single completion request.
     *
     * @param model       the model identifier to use (must be in {@link #listModels()})
     * @param messages    the ordered list of conversation messages; must not be empty
     * @param maxTokens   maximum number of tokens to generate; null uses provider default
     * @param temperature sampling temperature in the range [0.0, 2.0]; null uses provider default
     * @param tools       optional list of tools the model may invoke; null or empty disables
     *                    function calling
     */
    record ModelRequest(
            String model,
            List<Message> messages,
            Integer maxTokens,
            Double temperature,
            List<Tool> tools
    ) {
        public ModelRequest {
            if (model == null || model.isBlank())
                throw new IllegalArgumentException("model must not be blank");
            if (messages == null || messages.isEmpty())
                throw new IllegalArgumentException("messages must not be empty");
            messages = List.copyOf(messages);
            tools = tools == null ? List.of() : List.copyOf(tools);
        }
    }

    /**
     * Immutable value object representing the result of a completed (non-streaming)
     * model completion request.
     *
     * @param content    the generated text content
     * @param tokensIn   number of input tokens consumed (prompt tokens)
     * @param tokensOut  number of output tokens generated (completion tokens)
     * @param model      model identifier that produced the response
     * @param provider   provider id that handled the request (matches {@link #getId()})
     * @param latencyMs  wall-clock latency in milliseconds from request to first byte
     */
    record ModelResponse(
            String content,
            int tokensIn,
            int tokensOut,
            String model,
            String provider,
            long latencyMs
    ) {}

    /**
     * Immutable record describing the static capabilities of a model provider.
     *
     * @param streaming       {@code true} if the provider supports streaming via SSE
     * @param functionCalling {@code true} if the provider supports tool/function calling
     * @param vision          {@code true} if the provider supports image inputs
     */
    record ModelCapabilities(boolean streaming, boolean functionCalling, boolean vision) {}

    /**
     * Sealed interface representing the possible operational states of a model provider.
     *
     * <p>Using a sealed interface allows exhaustive pattern matching in switch expressions
     * and future extension with richer status types without breaking callers.
     */
    sealed interface ProviderStatus permits ProviderStatus.Simple, ProviderStatus.ErrorStatus {

        /** Returns a short, human-readable label for display in the admin dashboard. */
        String label();

        /**
         * The three standard status values shared by all model provider implementations.
         */
        enum Simple implements ProviderStatus {
            /** The provider is configured and ready to accept completion requests. */
            READY {
                @Override public String label() { return "Ready"; }
            },
            /** The provider has not yet been configured (onInstall not called). */
            NOT_CONFIGURED {
                @Override public String label() { return "Not Configured"; }
            },
            /** The provider encountered an unrecoverable error. */
            ERROR {
                @Override public String label() { return "Error"; }
            }
        }

        /**
         * Rich error status carrying the causing exception and a machine-readable code.
         *
         * @param cause     the exception that caused the error state
         * @param errorCode short machine-readable error code (e.g. {@code "AUTH_FAILED"})
         */
        record ErrorStatus(Throwable cause, String errorCode) implements ProviderStatus {
            @Override
            public String label() {
                return "Error [" + errorCode + "]: " + cause.getMessage();
            }
        }
    }
}
