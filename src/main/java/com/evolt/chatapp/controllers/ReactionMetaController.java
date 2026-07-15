package com.evolt.chatapp.controllers;

import com.evolt.chatapp.models.enums.AllowedReactions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reactions")
public class ReactionMetaController {

    @GetMapping("/available")
    public ResponseEntity<?> getAvailableEmojis() {
        return ResponseEntity.ok(AllowedReactions.EMOJIS);
    }
}