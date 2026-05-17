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
    avatar_id VARCHAR(64) DEFAULT '',
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

CREATE TABLE IF NOT EXISTS user_personas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS system_presets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Add new columns to characters table (idempotent)
SET @sql_tags = IF(
    (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'characters' AND column_name = 'tags') = 0,
    'ALTER TABLE characters ADD COLUMN tags VARCHAR(500) DEFAULT \'\' AFTER model_config_id',
    'SELECT 1'
);
PREPARE stmt FROM @sql_tags;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql_author_note = IF(
    (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'characters' AND column_name = 'author_note') = 0,
    'ALTER TABLE characters ADD COLUMN author_note TEXT AFTER tags',
    'SELECT 1'
);
PREPARE stmt FROM @sql_author_note;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql_bg_id = IF(
    (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'characters' AND column_name = 'background_id') = 0,
    'ALTER TABLE characters ADD COLUMN background_id VARCHAR(64) DEFAULT \'\' AFTER author_note',
    'SELECT 1'
);
PREPARE stmt FROM @sql_bg_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS lorebook_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    character_id BIGINT NOT NULL,
    `keys` VARCHAR(500) NOT NULL,
    content TEXT NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (character_id) REFERENCES characters(id) ON DELETE CASCADE
);

-- Add is_active columns to model_configs, user_personas, system_presets (idempotent)
SET @sql_mc_active = IF(
    (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'model_configs' AND column_name = 'is_active') = 0,
    'ALTER TABLE model_configs ADD COLUMN is_active BOOLEAN DEFAULT FALSE',
    'SELECT 1'
);
PREPARE stmt FROM @sql_mc_active;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql_up_active = IF(
    (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'user_personas' AND column_name = 'is_active') = 0,
    'ALTER TABLE user_personas ADD COLUMN is_active BOOLEAN DEFAULT FALSE',
    'SELECT 1'
);
PREPARE stmt FROM @sql_up_active;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql_sp_active = IF(
    (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'system_presets' AND column_name = 'is_active') = 0,
    'ALTER TABLE system_presets ADD COLUMN is_active BOOLEAN DEFAULT FALSE',
    'SELECT 1'
);
PREPARE stmt FROM @sql_sp_active;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Idempotent index
SET @sql_lorebook_idx = IF(
    (SELECT COUNT(*) FROM information_schema.statistics
     WHERE table_schema = DATABASE() AND table_name = 'lorebook_entries' AND index_name = 'idx_lorebook_character_id') = 0,
    'CREATE INDEX idx_lorebook_character_id ON lorebook_entries(character_id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql_lorebook_idx;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
