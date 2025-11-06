-- HHS (Health Hack System) 数据库初始化脚本
-- 执行前请手动创建数据库：CREATE DATABASE `hhs` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `ai_conversation`;
DROP TABLE IF EXISTS `collect`;
DROP TABLE IF EXISTS `like_record`;
DROP TABLE IF EXISTS `comment`;
DROP TABLE IF EXISTS `health_tip`;
DROP TABLE IF EXISTS `sys_user`;

CREATE TABLE `sys_user` (
    `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`      VARCHAR(64)  NOT NULL COMMENT '用户名',
    `password`      VARCHAR(128) NOT NULL COMMENT '密码（加密存储）',
    `nickname`      VARCHAR(64)  NOT NULL COMMENT '昵称',
    `avatar`        VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
    `email`         VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    `phone`         VARCHAR(32)  DEFAULT NULL COMMENT '手机号',
    `status`        TINYINT      DEFAULT 1 COMMENT '用户状态 1-正常 0-禁用',
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE `health_tip` (
    `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`        BIGINT UNSIGNED NOT NULL COMMENT '作者ID',
    `title`          VARCHAR(128) NOT NULL COMMENT '标题',
    `content`        MEDIUMTEXT   NOT NULL COMMENT '富文本内容',
    `summary`        VARCHAR(512) DEFAULT NULL COMMENT '内容摘要',
    `category`       VARCHAR(32)  NOT NULL COMMENT '分类',
    `tags`           VARCHAR(256) DEFAULT NULL COMMENT '标签，逗号分隔',
    `view_count`     INT          DEFAULT 0 COMMENT '浏览量',
    `like_count`     INT          DEFAULT 0 COMMENT '点赞数',
    `collect_count`  INT          DEFAULT 0 COMMENT '收藏数',
    `status`         TINYINT      DEFAULT 1 COMMENT '状态 1-正常 0-下架',
    `publish_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    `update_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_category` (`category`),
    FULLTEXT KEY `idx_title_content` (`title`, `content`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康技巧表';

CREATE TABLE `comment` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tip_id`      BIGINT UNSIGNED NOT NULL COMMENT '技巧ID',
    `user_id`     BIGINT UNSIGNED NOT NULL COMMENT '评论人ID',
    `parent_id`   BIGINT UNSIGNED DEFAULT 0 COMMENT '父评论ID，0为根评论',
    `content`     VARCHAR(512)    NOT NULL COMMENT '评论内容',
    `like_count`  INT             DEFAULT 0 COMMENT '点赞数',
    `create_time` DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_tip_id` (`tip_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

CREATE TABLE `like_record` (
    `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`      BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `target_id`    BIGINT UNSIGNED NOT NULL COMMENT '目标ID',
    `target_type`  TINYINT         NOT NULL COMMENT '目标类型 1-技巧 2-评论',
    `create_time`  DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_target` (`user_id`, `target_id`, `target_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞记录表';

CREATE TABLE `collect` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `tip_id`      BIGINT UNSIGNED NOT NULL COMMENT '技巧ID',
    `create_time` DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_tip` (`user_id`, `tip_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

CREATE TABLE `ai_conversation` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT UNSIGNED DEFAULT NULL COMMENT '用户ID，匿名为NULL',
    `question`    TEXT            NOT NULL COMMENT '提问内容',
    `answer`      MEDIUMTEXT      NOT NULL COMMENT 'AI回答',
    `create_time` DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话历史表';

SET FOREIGN_KEY_CHECKS = 1;
