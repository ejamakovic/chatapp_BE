package com.evolt.chatapp.models.mappers;

import com.evolt.chatapp.models.Message;
import com.evolt.chatapp.models.dto.MessageDto;

public class MessageMapper {

    public static MessageDto toDTO(Message msg) {
        if (msg == null) return null;

        return new MessageDto(msg);
    }
}