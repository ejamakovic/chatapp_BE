// models/dto/ConversationMemberDto.java
package com.evolt.chatapp.models.dto;

import com.evolt.chatapp.models.ConversationMember;
import java.time.LocalDateTime;

public class ConversationMemberDto {
    private Long id;
    private UserDto user;
    private String role;
    private LocalDateTime joinedAt;

    public ConversationMemberDto(ConversationMember member) {
        this.id = member.getId();
        this.user = new UserDto(member.getUser());
        this.role = member.getRole().name();
        this.joinedAt = member.getJoinedAt();
    }

    public Long getId() { return id; }
    public UserDto getUser() { return user; }
    public String getRole() { return role; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
}