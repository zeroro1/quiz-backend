-- 创建数据库
CREATE DATABASE IF NOT EXISTS quiz_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE quiz_db;

-- 用户表
CREATE TABLE IF NOT EXISTS wx_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    openid VARCHAR(64) UNIQUE NOT NULL,
    nickname VARCHAR(64) DEFAULT '',
    avatar VARCHAR(255) DEFAULT '',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 答题记录表
CREATE TABLE IF NOT EXISTS quiz_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    question_type VARCHAR(20) NOT NULL COMMENT 'COMMONSENSE or LOGIC',
    question_content TEXT NOT NULL,
    options_json TEXT NOT NULL COMMENT 'JSON array of options',
    correct_answer VARCHAR(10) NOT NULL COMMENT 'A/B/C/D',
    user_answer VARCHAR(10),
    is_correct BOOLEAN,
    time_taken_seconds INT COMMENT 'seconds per question',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_type (question_type),
    INDEX idx_created (created_at),
    FOREIGN KEY (user_id) REFERENCES wx_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 排行榜汇总表
CREATE TABLE IF NOT EXISTS leaderboard (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    question_type VARCHAR(20) NOT NULL,
    total_questions INT DEFAULT 0,
    correct_count INT DEFAULT 0,
    accuracy DECIMAL(5,2) DEFAULT 0 COMMENT 'percentage 0-100',
    avg_time DECIMAL(5,2) DEFAULT 0 COMMENT 'average seconds per question',
    total_time INT DEFAULT 0 COMMENT 'total seconds',
    rank_accuracy INT COMMENT 'updated periodically',
    rank_speed INT COMMENT 'updated periodically',
    UNIQUE KEY uk_user_type (user_id, question_type),
    INDEX idx_type_accuracy (question_type, accuracy DESC),
    INDEX idx_type_speed (question_type, avg_time ASC),
    FOREIGN KEY (user_id) REFERENCES wx_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 初始化示例数据（可选）
-- INSERT INTO leaderboard (user_id, question_type, total_questions, correct_count, accuracy, avg_time, total_time)
-- VALUES (1, 'COMMONSENSE', 10, 8, 80.00, 15.50, 155);
