CREATE INDEX IF NOT EXISTS idx_users_username
    ON users(username);

CREATE INDEX IF NOT EXISTS idx_users_connected
    ON users(connected);

CREATE INDEX IF NOT EXISTS idx_messages_sender_time
    ON messages (sender_id, timestamp DESC);

CREATE INDEX IF NOT EXISTS idx_messages_receiver_time
    ON messages (receiver_id, timestamp DESC);

CREATE INDEX IF NOT EXISTS idx_messages_pair_time
    ON messages (sender_id, receiver_id, timestamp DESC);