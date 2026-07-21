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

import java.time.LocalDateTime;
import java.util.*;
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

    /**
     * A user has at most one reaction per message.
     * - No existing reaction  -> create it.
     * - Same emoji as before  -> remove it (toggle off).
     * - Different emoji       -> overwrite it.
     * Returns the new reaction dto, or null if it was removed.
     */
    @Transactional
    public MessageReactionDto setReaction(Long messageId, Long userId, String emoji) {
        if (!AllowedReactions.isAllowed(emoji)) {
            throw new IllegalArgumentException("Unsupported reaction emoji");
        }

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));
        Long conversationId = message.getConversation().getId();

        Optional<MessageReaction> existing = reactionRepository.findByMessageIdAndUserId(messageId, userId);

        if (existing.isPresent() && existing.get().getEmoji().equals(emoji)) {
            reactionRepository.delete(existing.get());

            Map<String, Object> payload = new HashMap<>();
            payload.put("messageId", messageId);
            payload.put("userId", userId);
            payload.put("emoji", emoji);
            payload.put("conversationId", conversationId);
            eventPublisher.publishEvent(new WebSocketEvent<>("REACTION_REMOVED", payload));
            return null;
        }

        MessageReaction reaction = existing.orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            return new MessageReaction(message, user, emoji);
        });
        reaction.setEmoji(emoji);
        reaction.setCreatedAt(LocalDateTime.now());

        MessageReaction saved = reactionRepository.save(reaction);
        MessageReactionDto dto = new MessageReactionDto(saved);

        // Also represents "replace" — the frontend clears this user's previous
        // reaction on this message before applying the new one.
        eventPublisher.publishEvent(new WebSocketEvent<>("REACTION_ADDED", dto));
        return dto;
    }

    public List<MessageReactionDto> getReactionsForMessage(Long messageId) {
        return reactionRepository.findByMessageId(messageId).stream().map(MessageReactionDto::new).toList();
    }

    public Map<String, Long> getReactionCounts(Long messageId) {
        return reactionRepository.findByMessageId(messageId).stream()
                .collect(Collectors.groupingBy(MessageReaction::getEmoji, Collectors.counting()));
    }
}