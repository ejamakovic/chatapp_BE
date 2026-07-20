package com.evolt.chatapp.models.dto;

import java.util.List;

public class MessageWindowDto {
    private List<MessageDto> messages;
    private boolean hasMoreOlder;
    private boolean hasMoreNewer;
    private Long lastSeenMessageId;

    public MessageWindowDto(List<MessageDto> messages, boolean hasMoreOlder, boolean hasMoreNewer, Long lastSeenMessageId) {
        this.messages = messages;
        this.hasMoreOlder = hasMoreOlder;
        this.hasMoreNewer = hasMoreNewer;
        this.lastSeenMessageId = lastSeenMessageId;
    }

    public List<MessageDto> getMessages() { return messages; }
    public boolean isHasMoreOlder() { return hasMoreOlder; }
    public boolean isHasMoreNewer() { return hasMoreNewer; }
    public Long getLastSeenMessageId() { return lastSeenMessageId; }
}