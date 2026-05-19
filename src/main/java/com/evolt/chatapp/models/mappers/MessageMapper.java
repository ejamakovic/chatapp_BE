package com.evolt.chatapp.models.mappers;

import com.evolt.chatapp.models.Message;
import com.evolt.chatapp.models.dto.MessageDTO;
import com.evolt.chatapp.models.dto.UserDTO;

public class MessageMapper {

    public static MessageDTO toDTO(Message msg) {
        if (msg == null) return null;

        return new MessageDTO(msg);
    }
}