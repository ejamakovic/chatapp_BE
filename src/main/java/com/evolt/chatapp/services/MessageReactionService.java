package com.evolt.chatapp.services;

import com.evolt.chatapp.models.Message;
import com.evolt.chatapp.models.MessageReaction;
import com.evolt.chatapp.models.User;
import com.evolt.chatapp.models.dto.MessageReactionDto;
import com.evolt.chatapp.models.enums.AllowedReactions;
import com.evolt.chatapp.repositories.MessageReactionRepository;
import com.evolt.chatapp.repositories.MessageRepository;
import com.evolt.chatapp.repositories.UserRepository;
import com.evolt.chatapp.websocket.WebSocketEvent;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MessageReactionService {

    private final MessageReactionRepository reactionRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public MessageReactionService(
            MessageReactionRepository reactionRepository,
            MessageRepository messageRepository,
            UserRepository userRepository,
            ApplicationEventPublisher eventPublisher) {
        this.reactionRepository = reactionRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public MessageReactionDto addReaction(Long messageId, Long userId, String emoji) {
        if (!AllowedReactions.isAllowed(emoji)) {
            throw new IllegalArgumentException("Unsupported reaction emoji");
        }

        // Idempotent: same user + same emoji + same message is a no-op, not an error
        if (reactionRepository.existsByMessageIdAndUserIdAndEmoji(messageId, userId, emoji)) {
            return null;
        }

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        MessageReaction saved = reactionRepository.save(new MessageReaction(message, user, emoji));
        MessageReactionDto dto = new MessageReactionDto(saved);

        eventPublisher.publishEvent(new WebSocketEvent<>("REACTION_ADDED", dto));
        return dto;
    }

    @Transactional
    public void removeReaction(Long messageId, Long userId, String emoji) {
        int deleted = reactionRepository.deleteByMessageIdAndUserIdAndEmoji(messageId, userId, emoji);
        if (deleted > 0) {
            eventPublisher.publishEvent(new WebSocketEvent<>(
                    "REACTION_REMOVED",
                    Map.of("messageId", messageId, "userId", userId, "emoji", emoji)
            ));
        }
    }

    public List<MessageReactionDto> getReactionsForMessage(Long messageId) {
        return reactionRepository.findByMessageId(messageId)
                .stream()
                .map(MessageReactionDto::new)
                .toList();
    }

    /** Grouped view: emoji -> count, handy for rendering pill badges under a message. */
    public Map<String, Long> getReactionCounts(Long messageId) {
        return reactionRepository.findByMessageId(messageId)
                .stream()
                .collect(Collectors.groupingBy(MessageReaction::getEmoji, Collectors.counting()));
    }
}