package com.evolt.chatapp.models.mappers;

import com.evolt.chatapp.models.Message;
import com.evolt.chatapp.models.dto.MessageDTO;
import com.evolt.chatapp.models.dto.UserDTO;

public class MessageMapper {

    public static MessageDTO toDTO(Message msg) {
        if (msg == null) return null;

        MessageDTO dto = new MessageDTO();

        dto.setId(msg.getId());
        dto.setContent(msg.getContent());
        dto.setTimestamp(msg.getTimestamp());

        if (msg.getSender() != null) {
            UserDTO sender = new UserDTO();
            sender.setUsername(msg.getSender().getUsername());
            dto.setSender(sender);
        }

        if (msg.getReceiver() != null) {
            UserDTO receiver = new UserDTO();
            receiver.setUsername(msg.getReceiver().getUsername());
            dto.setReceiver(receiver);
        }

        dto.setAttachments(msg.getAttachments());

        return dto;
    }
}