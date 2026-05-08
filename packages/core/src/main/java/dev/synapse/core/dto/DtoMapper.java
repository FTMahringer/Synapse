package dev.synapse.core.dto;

import dev.synapse.core.domain.*;

public class DtoMapper {

    public static UserDTO toDTO(User user) {
        return new UserDTO(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole().name(),
            user.getSettings(),
            user.getCreatedAt()
        );
    }

    public static User fromCreateRequest(CreateUserRequest request) {
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setRole(User.UserRole.valueOf(request.role()));
        if (request.settings() != null) {
            user.setSettings(request.settings());
        }
        return user;
    }

    public static AgentDTO toDTO(Agent agent) {
        return new AgentDTO(
            agent.getId(),
            agent.getName(),
            agent.getType().name(),
            agent.getStatus().name(),
            agent.getConfig(),
            agent.getCreatedAt()
        );
    }

    public static Agent fromCreateRequest(CreateAgentRequest request) {
        Agent agent = new Agent();
        agent.setId(request.id());
        agent.setName(request.name());
        agent.setType(Agent.AgentType.valueOf(request.type()));
        if (request.config() != null) {
            agent.setConfig(request.config());
        }
        return agent;
    }

    public static ConversationDTO toDTO(Conversation conversation) {
        return new ConversationDTO(
            conversation.getId(),
            conversation.getAgentId(),
            conversation.getUserId(),
            conversation.getChannelId(),
            conversation.getStatus().name(),
            conversation.getStartedAt()
        );
    }

    public static Conversation fromCreateRequest(CreateConversationRequest request, UUID userId) {
        Conversation conversation = new Conversation();
        conversation.setAgentId(request.agentId());
        conversation.setUserId(userId);
        return conversation;
    }

    public static MessageDTO toDTO(Message message) {
        return new MessageDTO(
            message.getId(),
            message.getConversationId(),
            message.getRole().name(),
            message.getContent(),
            message.getTokens(),
            message.getCreatedAt()
        );
    }

    public static Message fromCreateRequest(CreateMessageRequest request) {
        Message message = new Message();
        message.setConversationId(request.conversationId());
        message.setRole(Message.MessageRole.valueOf(request.role()));
        message.setContent(request.content());
        message.setTokens(request.tokens());
        return message;
    }

    public static TaskDTO toDTO(Task task) {
        return new TaskDTO(
            task.getId(),
            task.getProjectId(),
            task.getTitle(),
            task.getStatus().name(),
            task.getAssignedAgentId(),
            task.getSize() != null ? task.getSize().name() : null,
            task.getVersion(),
            task.getCreatedAt()
        );
    }

    public static Task fromCreateRequest(CreateTaskRequest request) {
        Task task = new Task();
        task.setProjectId(request.projectId());
        task.setTitle(request.title());
        task.setAssignedAgentId(request.assignedAgentId());
        if (request.size() != null) {
            task.setSize(Task.TaskSize.valueOf(request.size()));
        }
        return task;
    }

    public static PluginDTO toDTO(Plugin plugin) {
        return new PluginDTO(
            plugin.getId(),
            plugin.getName(),
            plugin.getType().name(),
            plugin.getVersion(),
            plugin.getStatus().name(),
            plugin.getManifest(),
            plugin.getCreatedAt()
        );
    }

    public static AgentTeamDTO toDTO(AgentTeam team) {
        return new AgentTeamDTO(
            team.getId(),
            team.getName(),
            team.getLeaderAgentId(),
            team.getConfig(),
            team.getCreatedAt()
        );
    }

    public static AgentTeam fromCreateRequest(CreateAgentTeamRequest request) {
        AgentTeam team = new AgentTeam();
        team.setId(request.id());
        team.setName(request.name());
        team.setLeaderAgentId(request.leaderAgentId());
        if (request.config() != null) {
            team.setConfig(request.config());
        }
        return team;
    }
}
