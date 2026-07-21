package com.evolt.chatapp.services;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Small "hot" ranking helpers — a Reddit-style formula: a log-scaled
 * engagement term (so a handful of extra reactions on a huge thread
 * doesn't dominate) minus a time-decay term, so fresh content with
 * some traction rises but old content eventually falls off.
 */
public final class RankingUtil {

    private RankingUtil() {}

    /** Higher = hotter. Friend posts get a flat bonus so they surface first for similar engagement. */
    public static double postScore(long reactionCount, long commentCount, LocalDateTime createdAt, boolean isFriend) {
        double engagement = reactionCount + commentCount * 1.5;
        double order = Math.log10(Math.max(engagement, 1));
        double ageHours = Math.max(Duration.between(createdAt, LocalDateTime.now()).toMinutes() / 60.0, 0);
        double decay = ageHours / 12.0;
        double score = order - decay;
        if (isFriend) score += 3.0;
        return score;
    }

    public static double commentScore(long reactionCount, long replyCount, LocalDateTime createdAt) {
        double engagement = reactionCount + replyCount * 0.75;
        double order = Math.log10(Math.max(engagement, 1));
        double ageHours = Math.max(Duration.between(createdAt, LocalDateTime.now()).toMinutes() / 60.0, 0);
        double decay = ageHours / 24.0;
        return order - decay;
    }

    public static <T> List<T> paginate(List<T> sorted, int page, int size) {
        int from = Math.min(page * size, sorted.size());
        int to = Math.min(from + size, sorted.size());
        return sorted.subList(from, to);
    }
}