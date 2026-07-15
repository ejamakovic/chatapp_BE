package com.evolt.chatapp.models.enums;

import java.util.Set;

public final class AllowedReactions {

    // Curated set — extend as needed. Frontend should offer exactly this list.
    public static final Set<String> EMOJIS = Set.of(
            "👍", "👎", "❤️", "😂", "😮", "😢", "😡", "🙏", "🔥", "🎉",
            "👏", "😍", "🤔", "😅", "💯", "👀", "✅", "❌"
    );

    private AllowedReactions() {}

    public static boolean isAllowed(String emoji) {
        return emoji != null && EMOJIS.contains(emoji);
    }
}