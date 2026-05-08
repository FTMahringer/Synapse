# Chat Runtime API

## Conversation Management

### Create Conversation
```http
POST /api/conversations
Content-Type: application/json
X-User-ID: {user-uuid}

{
  "agentId": "main-agent"
}
```

Response:
```json
{
  "id": "uuid",
  "agentId": "main-agent",
  "userId": "uuid",
  "channelId": null,
  "startedAt": "2026-05-08T23:00:00Z",
  "status": "ACTIVE"
}
```

### List Conversations
```http
GET /api/conversations
X-User-ID: {user-uuid}
```

Response: Array of conversation objects

### Get Conversation
```http
GET /api/conversations/{id}
```

### Get Messages
```http
GET /api/conversations/{id}/messages
```

Response:
```json
[
  {
    "id": "uuid",
    "conversationId": "uuid",
    "role": "USER",
    "content": "Hello!",
    "tokens": null,
    "providerId": null,
    "modelName": null,
    "latencyMs": null,
    "promptTokens": null,
    "completionTokens": null,
    "createdAt": "2026-05-08T23:00:00Z"
  },
  {
    "id": "uuid",
    "conversationId": "uuid",
    "role": "ASSISTANT",
    "content": "Hi! How can I help you?",
    "tokens": 450,
    "providerId": "uuid",
    "modelName": "llama3.2",
    "latencyMs": 2340,
    "promptTokens": 250,
    "completionTokens": 200,
    "createdAt": "2026-05-08T23:00:02Z"
  }
]
```

### Send Message
```http
POST /api/conversations/{id}/messages
Content-Type: application/json

{
  "content": "What can you do?"
}
```

Response: User message object (assistant response will appear in GET messages)

## Implementation Notes

- Conversations are tied to users via X-User-ID header
- Main Agent system prompt loaded from classpath resources
- First enabled Ollama provider used for responses
- Error messages saved as assistant messages with ⚠️ prefix
- Token counts and latency tracked per message
- Provider metadata enables cost/performance analytics

## Dashboard Integration (v1.8.0)

Future dashboard screens will:
- List user conversations
- Display messages in chat UI
- Show token/cost metrics
- Handle real-time streaming updates (v1.6.0)
