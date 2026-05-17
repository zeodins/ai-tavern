CREATE TABLE IF NOT EXISTS model_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    api_base_url VARCHAR(500) NOT NULL,
    api_key VARCHAR(500) NOT NULL,
    model_name VARCHAR(100) NOT NULL,
    temperature DECIMAL(3,2) DEFAULT 0.7,
    max_tokens INT DEFAULT 2048,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS characters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    avatar_path VARCHAR(500) DEFAULT '',
    description TEXT,
    personality TEXT,
    first_message TEXT,
    scenario TEXT,
    system_prompt TEXT,
    mes_example TEXT,
    model_config_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (model_config_id) REFERENCES model_configs(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    character_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (character_id) REFERENCES characters(id) ON DELETE CASCADE
);

-- Idempotent index creation (MySQL 5.7/8 compatible)
SET @sql_char_idx = IF(
    (SELECT COUNT(*) FROM information_schema.statistics
     WHERE table_schema = DATABASE() AND table_name = 'characters' AND index_name = 'idx_characters_model_config_id') = 0,
    'CREATE INDEX idx_characters_model_config_id ON characters(model_config_id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql_char_idx;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql_chat_idx = IF(
    (SELECT COUNT(*) FROM information_schema.statistics
     WHERE table_schema = DATABASE() AND table_name = 'chat_messages' AND index_name = 'idx_chat_messages_character_id') = 0,
    'CREATE INDEX idx_chat_messages_character_id ON chat_messages(character_id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql_chat_idx;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
