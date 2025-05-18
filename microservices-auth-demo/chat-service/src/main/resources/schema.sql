-- Use the existing database
USE authdb;

-- Drop foreign key constraints first (if they exist)
SET FOREIGN_KEY_CHECKS=0;

-- Create conversations table
CREATE TABLE IF NOT EXISTS conversations (
    id VARCHAR(36) PRIMARY KEY,
    type VARCHAR(10) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    last_message_id VARCHAR(36)
);

-- Create messages table
CREATE TABLE IF NOT EXISTS messages (
    id VARCHAR(36) PRIMARY KEY,
    conversation_id VARCHAR(36) NOT NULL,
    parent_message_id VARCHAR(36),
    sender_id VARCHAR(255) NOT NULL,
    content TEXT,
    content_type VARCHAR(10) NOT NULL,
    sent_at TIMESTAMP NOT NULL,
    edited BOOLEAN DEFAULT FALSE
);

-- Create direct_conversations table
CREATE TABLE IF NOT EXISTS direct_conversations (
    conversation_id VARCHAR(36) PRIMARY KEY,
    user1_id VARCHAR(255) NOT NULL,
    user2_id VARCHAR(255) NOT NULL
);

-- Drop all foreign keys first with error handling
SET @query = (SELECT IF(
    EXISTS(
        SELECT * FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_message_conversation' 
        AND table_name = 'messages'
    ),
    'ALTER TABLE messages DROP FOREIGN KEY fk_message_conversation',
    'SELECT 1'
));
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @query = (SELECT IF(
    EXISTS(
        SELECT * FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_message_parent' 
        AND table_name = 'messages'
    ),
    'ALTER TABLE messages DROP FOREIGN KEY fk_message_parent',
    'SELECT 1'
));
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @query = (SELECT IF(
    EXISTS(
        SELECT * FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_last_message' 
        AND table_name = 'conversations'
    ),
    'ALTER TABLE conversations DROP FOREIGN KEY fk_last_message',
    'SELECT 1'
));
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @query = (SELECT IF(
    EXISTS(
        SELECT * FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_direct_conversation' 
        AND table_name = 'direct_conversations'
    ),
    'ALTER TABLE direct_conversations DROP FOREIGN KEY fk_direct_conversation',
    'SELECT 1'
));
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add foreign key constraints
ALTER TABLE messages 
ADD CONSTRAINT fk_message_conversation
FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE;

ALTER TABLE messages 
ADD CONSTRAINT fk_message_parent
FOREIGN KEY (parent_message_id) REFERENCES messages(id) ON DELETE SET NULL;

ALTER TABLE conversations
ADD CONSTRAINT fk_last_message
FOREIGN KEY (last_message_id) REFERENCES messages(id) ON DELETE SET NULL;

ALTER TABLE direct_conversations
ADD CONSTRAINT fk_direct_conversation
FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE;

SET FOREIGN_KEY_CHECKS=1; 