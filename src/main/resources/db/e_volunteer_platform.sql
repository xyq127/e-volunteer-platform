-- =============================================================================
-- E志愿志愿者服务平台 V1.0 数据库脚本
-- 数据库：MySQL 8.0
-- 内容：数据库创建、业务表创建、存储过程创建、初始化数据
-- 执行方式：mysql -uroot -p < e_volunteer_platform.sql
-- 说明：脚本除基础志愿活动管理对象外，还包含志愿者工作台、可信签到与时长核算、
--       智能供需匹配、报名候补与信用、电子服务证明等突破能力所需的数据对象。
-- =============================================================================

-- 连接字符集与库、表的字符集保持一致，避免存储过程参数与字段比较时出现字符集冲突
SET NAMES utf8mb4 COLLATE utf8mb4_general_ci;

DROP DATABASE IF EXISTS e_volunteer_platform;
CREATE DATABASE e_volunteer_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE e_volunteer_platform;


-- -----------------------------------------------------------------------------
-- 1. 志愿者表：保存志愿者的编号、姓名、联系方式，以及技能标签之外的服务信用信息
--    volunteer_credit 志愿服务信用分（初始 100，通过时长复核加分、爽约扣分）
--    volunteer_totalduration 已核定累计服务时长（小时），仅统计经志愿者组织复核通过的时长
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS volunteer;
CREATE TABLE volunteer
(
    volunteer_num          INT AUTO_INCREMENT COMMENT '志愿者编号，主键自增',
    volunteer_id           VARCHAR(32)  DEFAULT NULL COMMENT '志愿者业务编号，注册时由数据库生成，同时作为登录账号',
    volunteer_name         VARCHAR(50)  DEFAULT NULL COMMENT '志愿者姓名',
    volunteer_registerdate DATE         DEFAULT NULL COMMENT '注册日期',
    volunteer_gender       VARCHAR(4)   DEFAULT NULL COMMENT '性别',
    volunteer_birth        DATE         DEFAULT NULL COMMENT '出生日期',
    volunteer_tel          VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
    volunteer_latitude     DECIMAL(10, 7) DEFAULT NULL COMMENT '常住地点纬度，用于供需匹配',
    volunteer_longitude    DECIMAL(10, 7) DEFAULT NULL COMMENT '常住地点经度，用于供需匹配',
    volunteer_credit       INT          DEFAULT 100 COMMENT '志愿服务信用分，初始 100',
    volunteer_noshow       INT          DEFAULT 0 COMMENT '活动爽约次数',
    volunteer_totalduration DECIMAL(10, 1) DEFAULT 0 COMMENT '已核定累计服务时长（小时）',
    volunteer_isdeleted    INT          DEFAULT 0 COMMENT '删除标记，0 未删除、1 已删除（账号被停用时置 1）',
    PRIMARY KEY (volunteer_num),
    UNIQUE KEY uk_volunteer_tel (volunteer_tel),
    KEY idx_volunteer_id (volunteer_id),
    KEY idx_volunteer_name (volunteer_name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='志愿者表';

-- -----------------------------------------------------------------------------
-- 2. 志愿者技能表：保存志愿者登记的服务技能标签，与活动技能要求求交实现供需匹配
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS volunteer_skill;
CREATE TABLE volunteer_skill
(
    skill_num     INT AUTO_INCREMENT COMMENT '技能编号，主键自增',
    volunteer_num INT         NOT NULL COMMENT '志愿者编号',
    skill_name    VARCHAR(50) NOT NULL COMMENT '服务技能标签',
    PRIMARY KEY (skill_num),
    UNIQUE KEY uk_volunteer_skill (volunteer_num, skill_name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='志愿者技能表';

-- -----------------------------------------------------------------------------
-- 3. 志愿者组织表：保存志愿者组织的账号、名称、成立日期与组织简介
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS organization;
CREATE TABLE organization
(
    organization_num           INT AUTO_INCREMENT COMMENT '志愿者组织编号，主键自增',
    organization_id            VARCHAR(32)  DEFAULT NULL COMMENT '志愿者组织登录账号',
    organization_name          VARCHAR(100) DEFAULT NULL COMMENT '志愿者组织名称',
    organization_establishdate DATE         DEFAULT NULL COMMENT '成立日期',
    organization_introduction TEXT COMMENT '组织简介',
    organization_isdeleted     INT          DEFAULT 0 COMMENT '删除标记，0 未删除、1 已删除',
    PRIMARY KEY (organization_num),
    UNIQUE KEY uk_organization_id (organization_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='志愿者组织表';

-- -----------------------------------------------------------------------------
-- 4. 平台管理员表：保存平台管理员的账号、密码与姓名
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS admin;
CREATE TABLE admin
(
    admin_num      INT AUTO_INCREMENT COMMENT '管理员编号，主键自增',
    admin_id       VARCHAR(32)  DEFAULT NULL COMMENT '管理员登录账号',
    admin_name     VARCHAR(50)  DEFAULT NULL COMMENT '管理员姓名',
    PRIMARY KEY (admin_num),
    UNIQUE KEY uk_admin_id (admin_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='平台管理员表';

-- -----------------------------------------------------------------------------
-- 5. 统一用户表：保存志愿者、志愿者组织与平台管理员的登录账号、密码密文与角色权限
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS e_user;
CREATE TABLE e_user
(
    id       VARCHAR(32)  NOT NULL COMMENT '登录账号',
    password VARCHAR(200) DEFAULT NULL COMMENT '登录密码密文',
    role     VARCHAR(64)  DEFAULT NULL COMMENT '角色权限，如 ROLE_VOLUNTEER、ROLE_ADMIN、ROLE_ORGANIZATION',
    enabled  INT          DEFAULT 1 COMMENT '账号状态，1 启用、0 停用，停用后无法登录',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='统一用户表';

-- -----------------------------------------------------------------------------
-- 6. 志愿活动表：保存志愿者组织申报的志愿活动及其审核、开展状态
--    activity_checkin_secret 签到密钥，平台按签到密钥与时间窗口派生短时轮换的现场签到码
--    activity_latitude/activity_longitude/activity_radius 活动地点与签到地理围栏
--    activity_reason_code 审核不通过的结构化原因，activity_revision 申报被退回后修改提交的次数
--    activity_settle_time 活动结算时间，结算后统计爽约并计入志愿者信用
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS activity;
CREATE TABLE activity
(
    activity_num         INT AUTO_INCREMENT COMMENT '活动编号，主键自增',
    organization_num     INT            DEFAULT NULL COMMENT '申报活动的志愿者组织编号',
    activity_id          VARCHAR(32)    DEFAULT NULL COMMENT '活动业务编号',
    activity_name        VARCHAR(100)   DEFAULT NULL COMMENT '活动名称',
    activity_detail      TEXT COMMENT '活动内容',
    activity_begintime   DATETIME       DEFAULT NULL COMMENT '活动开始时间',
    activity_endtime     DATETIME       DEFAULT NULL COMMENT '活动结束时间',
    activity_location    VARCHAR(200)   DEFAULT NULL COMMENT '活动地点',
    activity_needpeople  INT            DEFAULT NULL COMMENT '需求人数',
    activity_notice      VARCHAR(500)   DEFAULT NULL COMMENT '注意事项',
    activity_publishtime DATETIME       DEFAULT NULL COMMENT '申报时间',
    activity_signddl     DATETIME       DEFAULT NULL COMMENT '报名截止时间',
    activity_state       VARCHAR(4)     DEFAULT NULL COMMENT '活动状态，0 审核中、1 未开始、2 进行中、3 已结束、4 审核未通过',
    admin_num            INT            DEFAULT NULL COMMENT '审核该活动的管理员编号',
    activity_checkin     VARCHAR(100)   DEFAULT NULL COMMENT '签到方式',
    activity_leaderid    INT            DEFAULT NULL COMMENT '活动负责人编号',
    activity_checktime   DATETIME       DEFAULT NULL COMMENT '审核时间',
    activity_remark      VARCHAR(500)   DEFAULT NULL COMMENT '审核意见',
    activity_checkin_secret VARCHAR(32)  DEFAULT NULL COMMENT '签到密钥，用于派生短时轮换的现场签到码',
    activity_latitude    DECIMAL(10, 7) DEFAULT NULL COMMENT '活动地点纬度',
    activity_longitude   DECIMAL(10, 7) DEFAULT NULL COMMENT '活动地点经度',
    activity_radius      INT            DEFAULT 300 COMMENT '签到地理围栏半径（米）',
    activity_reason_code VARCHAR(32)    DEFAULT NULL COMMENT '审核不通过的结构化原因编码',
    activity_revision    INT            DEFAULT 0 COMMENT '修改后重新申报的次数',
    activity_settle_time DATETIME       DEFAULT NULL COMMENT '活动结算时间',
    activity_isdeleted   INT            DEFAULT 0 COMMENT '删除标记，0 未删除、1 已删除',
    PRIMARY KEY (activity_num),
    KEY idx_activity_org_state (organization_num, activity_state, activity_isdeleted),
    KEY idx_activity_state (activity_state, activity_isdeleted),
    KEY idx_activity_name (activity_name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='志愿活动表';

-- -----------------------------------------------------------------------------
-- 7. 活动技能表：保存志愿活动所需的技能标签，用于志愿者与活动的匹配度计算
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS activity_skill;
CREATE TABLE activity_skill
(
    activityskill_num INT AUTO_INCREMENT COMMENT '活动技能编号，主键自增',
    activity_num      INT         NOT NULL COMMENT '活动编号',
    skill_name        VARCHAR(50) NOT NULL COMMENT '活动所需服务技能标签',
    PRIMARY KEY (activityskill_num),
    UNIQUE KEY uk_activity_skill (activity_num, skill_name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='活动技能表';

-- -----------------------------------------------------------------------------
-- 8. 活动报名表：保存志愿者报名志愿活动的申请、审核与志愿服务时长信息
--    报名审核状态 participate_applystate：0 待审核、1 已通过、2 未通过、3 候补
--    participate_noshow 爽约标记，活动结算时对已通过但无签退记录的报名置 1
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS participate;
CREATE TABLE participate
(
    participate_num            INT AUTO_INCREMENT COMMENT '报名编号，主键自增',
    volunteer_num              INT         DEFAULT NULL COMMENT '志愿者编号',
    activity_num               INT         DEFAULT NULL COMMENT '活动编号',
    participate_applytime      DATETIME    DEFAULT NULL COMMENT '报名申请时间',
    participate_applystate     VARCHAR(4)  DEFAULT NULL COMMENT '报名审核状态，0 待审核、1 已通过、2 未通过、3 候补',
    participate_applychecktime DATETIME    DEFAULT NULL COMMENT '报名审核时间',
    participate_training       VARCHAR(100) DEFAULT NULL COMMENT '培训情况',
    participate_begintime      DATETIME    DEFAULT NULL COMMENT '服务开始时间',
    participate_endtime        DATETIME    DEFAULT NULL COMMENT '服务结束时间',
    participate_duration       DOUBLE      DEFAULT NULL COMMENT '服务时长（小时）',
    participate_timecheck      VARCHAR(4)  DEFAULT NULL COMMENT '服务时长审核状态，空 待复核、1 已确认、2 已驳回',
    participate_noshow         INT         DEFAULT 0 COMMENT '爽约标记，0 正常、1 爽约',
    participate_cancel_time    DATETIME    DEFAULT NULL COMMENT '志愿者撤回报名时间',
    participate_confirmstate   VARCHAR(4)  DEFAULT NULL COMMENT '参加确认状态，空 待确认、1 已确认、2 已放弃',
    participate_confirmtime    DATETIME    DEFAULT NULL COMMENT '参加确认时间',
    participate_isdeleted      INT         DEFAULT 0 COMMENT '删除标记，0 未删除、1 已删除',
    PRIMARY KEY (participate_num),
    KEY idx_participate_activity (activity_num),
    KEY idx_participate_volunteer (volunteer_num)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='活动报名表';

-- -----------------------------------------------------------------------------
-- 9. 志愿服务签到表：保存志愿者参与志愿服务的签到签退时间、定位与时长
--    checkin_code 签到使用的现场签到码，checkin_distance/checkin_outdistance 签到签退时与活动地点的距离（米）
--    checkin_flag 轨迹标记，1 正常、2 异常待复核（超出地理围栏或缺少定位）
--    checkin_timecheck 时长复核状态，空 待复核、1 已确认、2 已驳回
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS checkin;
CREATE TABLE checkin
(
    checkin_num        INT AUTO_INCREMENT COMMENT '签到编号，主键自增',
    participate_num    INT            DEFAULT NULL COMMENT '报名编号',
    checkin_begintime  DATETIME       DEFAULT NULL COMMENT '签到时间',
    checkin_endtime    DATETIME       DEFAULT NULL COMMENT '签退时间',
    checkin_duration   DOUBLE         DEFAULT NULL COMMENT '服务时长（小时）',
    checkin_timecheck  VARCHAR(4)     DEFAULT NULL COMMENT '服务时长复核状态，空 待复核、1 已确认、2 已驳回',
    checkin_source     VARCHAR(4)     DEFAULT '1' COMMENT '记录来源，1 平台签到、2 志愿者组织补录（需平台管理员复核）',
    checkin_anomaly    VARCHAR(32)    DEFAULT NULL COMMENT '异常规则编码，命中时长异常规则时写入，需平台管理员裁定',
    checkin_anomalyremark VARCHAR(200) DEFAULT NULL COMMENT '异常裁定意见',
    checkin_objectname VARCHAR(50)    DEFAULT NULL COMMENT '服务对象名称（受助人或带队人）',
    checkin_objectphone VARCHAR(20)   DEFAULT NULL COMMENT '服务对象手机号，仅用于服务对象确认时校验，查询接口只返回掩码',
    checkin_confirmcode VARCHAR(16)   DEFAULT NULL COMMENT '服务确认码，由志愿者组织交给服务对象',
    checkin_objectconfirm VARCHAR(4)  DEFAULT NULL COMMENT '服务对象确认状态，空 待确认、1 已确认、2 已否认',
    checkin_objectconfirmtime DATETIME DEFAULT NULL COMMENT '服务对象确认时间',
    checkin_objectremark VARCHAR(200) DEFAULT NULL COMMENT '服务对象确认意见',
    checkin_code       VARCHAR(16)    DEFAULT NULL COMMENT '签到使用的现场签到码',
    checkin_latitude   DECIMAL(10, 7) DEFAULT NULL COMMENT '签到地点纬度',
    checkin_longitude  DECIMAL(10, 7) DEFAULT NULL COMMENT '签到地点经度',
    checkin_distance   INT            DEFAULT NULL COMMENT '签到地点与活动地点的距离（米）',
    checkin_outdistance INT           DEFAULT NULL COMMENT '签退地点与活动地点的距离（米）',
    checkin_flag       VARCHAR(4)     DEFAULT '1' COMMENT '轨迹标记，1 正常、2 异常',
    checkin_remark     VARCHAR(200)   DEFAULT NULL COMMENT '志愿者组织的复核意见',
    checkin_adminremark VARCHAR(200)  DEFAULT NULL COMMENT '平台管理员对补录记录的复核意见',
    checkin_checktime  DATETIME       DEFAULT NULL COMMENT '复核时间',
    PRIMARY KEY (checkin_num),
    KEY idx_checkin_participate (participate_num)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='志愿服务签到表';

-- -----------------------------------------------------------------------------
-- 10. 志愿培训表：保存志愿活动对应的培训安排
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS training;
CREATE TABLE training
(
    training_num        INT AUTO_INCREMENT COMMENT '培训编号，主键自增',
    training_id         VARCHAR(32)  DEFAULT NULL COMMENT '培训业务编号',
    activity_num        INT          DEFAULT NULL COMMENT '所属活动编号',
    training_name       VARCHAR(100) DEFAULT NULL COMMENT '培训名称',
    training_detail     TEXT COMMENT '培训内容',
    training_begintime  DATETIME     DEFAULT NULL COMMENT '培训开始时间',
    training_endtime    DATETIME     DEFAULT NULL COMMENT '培训结束时间',
    training_location   VARCHAR(200) DEFAULT NULL COMMENT '培训地点',
    training_checkin    VARCHAR(100) DEFAULT NULL COMMENT '签到方式',
    training_isdeleted  INT          DEFAULT 0 COMMENT '删除标记，0 未删除、1 已删除',
    PRIMARY KEY (training_num),
    KEY idx_training_activity (activity_num)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='志愿培训表';

-- -----------------------------------------------------------------------------
-- 11. 志愿者培训情况表：保存志愿者参加志愿培训的情况
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS volunteer_training_situation;
CREATE TABLE volunteer_training_situation
(
    vtsituation_num       INT AUTO_INCREMENT COMMENT '培训情况编号，主键自增',
    training_num          INT         DEFAULT NULL COMMENT '培训编号',
    volunteer_num         INT         DEFAULT NULL COMMENT '志愿者编号',
    vtsituation_id        VARCHAR(32) DEFAULT NULL COMMENT '记录业务编号',
    vtsituation_begintime DATETIME    DEFAULT NULL COMMENT '培训开始时间',
    vtsituation_endtime   DATETIME    DEFAULT NULL COMMENT '培训结束时间',
    vtsituation_isdeleted INT         DEFAULT 0 COMMENT '删除标记，0 未删除、1 已删除',
    PRIMARY KEY (vtsituation_num),
    KEY idx_vtsituation_training (training_num),
    KEY idx_vtsituation_volunteer (volunteer_num)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='志愿者培训情况表';

-- -----------------------------------------------------------------------------
-- 12. 志愿活动公告表：保存志愿者组织针对志愿活动发布的公告
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS act_announcement;
CREATE TABLE act_announcement
(
    actannouncement_num       INT AUTO_INCREMENT COMMENT '公告编号，主键自增',
    activity_num              INT          DEFAULT NULL COMMENT '所属活动编号',
    actannouncement_id        VARCHAR(32)  DEFAULT NULL COMMENT '公告业务编号',
    actannouncement_name      VARCHAR(100) DEFAULT NULL COMMENT '公告标题',
    actannouncement_detail    TEXT COMMENT '公告内容',
    organization_num          INT          DEFAULT NULL COMMENT '发布公告的志愿者组织编号',
    actannouncement_isdeleted INT          DEFAULT 0 COMMENT '删除标记，0 未删除、1 已删除',
    PRIMARY KEY (actannouncement_num),
    KEY idx_actannouncement_activity (activity_num)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='志愿活动公告表';

-- -----------------------------------------------------------------------------
-- 13. 通知公告表：保存平台管理员发布的通知公告
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS policy_announcement;
CREATE TABLE policy_announcement
(
    policyannouncement_num       INT AUTO_INCREMENT COMMENT '公告编号，主键自增',
    policyannouncement_id        VARCHAR(32)  DEFAULT NULL COMMENT '公告业务编号',
    policyannouncement_name      VARCHAR(100) DEFAULT NULL COMMENT '公告标题',
    policyannouncement_detail    TEXT COMMENT '公告内容',
    policyannouncement_file      VARCHAR(200) DEFAULT NULL COMMENT '公告附件',
    admin_num                    INT          DEFAULT NULL COMMENT '发布公告的管理员编号',
    policyannouncement_isdeleted INT          DEFAULT 0 COMMENT '删除标记，0 未删除、1 已删除',
    PRIMARY KEY (policyannouncement_num)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='通知公告表';

-- -----------------------------------------------------------------------------
-- 14. 公告附件表：保存通知公告对应的附件文件信息
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS policy_file;
CREATE TABLE policy_file
(
    policyfile_num           INT AUTO_INCREMENT COMMENT '附件编号，主键自增',
    policyannouncement_num   INT          DEFAULT NULL COMMENT '所属公告编号',
    policyfile_id            VARCHAR(32)  DEFAULT NULL COMMENT '文件业务编号',
    policyfile_name          VARCHAR(200) DEFAULT NULL COMMENT '文件名称',
    policyfile_route         VARCHAR(300) DEFAULT NULL COMMENT '文件存储路径',
    policyfile_uniquename    VARCHAR(200) DEFAULT NULL COMMENT '文件唯一名称',
    policyfile_isdeleted     VARCHAR(4)   DEFAULT '0' COMMENT '删除标记，0 未删除、1 已删除',
    PRIMARY KEY (policyfile_num),
    KEY idx_policyfile_announcement (policyannouncement_num)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='公告附件表';

-- -----------------------------------------------------------------------------
-- 15. 志愿秀表：保存志愿者分享的志愿服务经历及其浏览、点赞数据
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS `show`;
CREATE TABLE `show`
(
    show_num       INT AUTO_INCREMENT COMMENT '志愿秀编号，主键自增',
    show_id        VARCHAR(32) DEFAULT NULL COMMENT '志愿秀业务编号',
    show_detail    TEXT COMMENT '志愿秀内容',
    show_sharetime DATETIME    DEFAULT NULL COMMENT '分享时间',
    volunteer_num  INT         DEFAULT NULL COMMENT '分享志愿者编号',
    activity_num   INT         DEFAULT NULL COMMENT '关联活动编号',
    show_browse    INT         DEFAULT 0 COMMENT '浏览次数',
    show_like      INT         DEFAULT 0 COMMENT '点赞次数',
    show_isdeleted INT         DEFAULT 0 COMMENT '删除标记，0 未删除、1 已删除',
    PRIMARY KEY (show_num),
    KEY idx_show_volunteer (volunteer_num)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='志愿秀表';

-- -----------------------------------------------------------------------------
-- 16. 志愿秀图片表：保存志愿秀内容对应的图片文件信息
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS show_picture;
CREATE TABLE show_picture
(
    picture_num         INT AUTO_INCREMENT COMMENT '图片编号，主键自增',
    show_num            INT          DEFAULT NULL COMMENT '所属志愿秀编号',
    picture_id          VARCHAR(32)  DEFAULT NULL COMMENT '图片业务编号',
    picture_name        VARCHAR(200) DEFAULT NULL COMMENT '图片名称',
    picture_route       VARCHAR(300) DEFAULT NULL COMMENT '图片存储路径',
    picture_uniquename  VARCHAR(200) DEFAULT NULL COMMENT '图片唯一名称',
    picture_isdeleted   INT          DEFAULT 0 COMMENT '删除标记，0 未删除、1 已删除',
    PRIMARY KEY (picture_num),
    KEY idx_picture_show (show_num)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='志愿秀图片表';

-- -----------------------------------------------------------------------------
-- 17. 志愿秀点赞记录表：记录志愿者对志愿秀的点赞，保证同一志愿者对同一志愿秀只计一次
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS show_like_record;
CREATE TABLE show_like_record
(
    like_num      INT AUTO_INCREMENT COMMENT '点赞编号，主键自增',
    show_num      INT NOT NULL COMMENT '志愿秀编号',
    volunteer_num INT NOT NULL COMMENT '点赞志愿者编号',
    PRIMARY KEY (like_num),
    UNIQUE KEY uk_show_like (show_num, volunteer_num)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='志愿秀点赞记录表';

-- -----------------------------------------------------------------------------
-- 18. 站内通知表：保存平台向志愿者、志愿者组织发送的站内通知，支撑报名、审核、复核、提醒等消息触达
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS notification;
CREATE TABLE notification
(
    notification_num    INT AUTO_INCREMENT COMMENT '通知编号，主键自增',
    receiver_id         VARCHAR(32)  DEFAULT NULL COMMENT '接收账号',
    receiver_role       VARCHAR(64)  DEFAULT NULL COMMENT '接收角色，如 ROLE_VOLUNTEER、ROLE_ORGANIZATION',
    notification_title  VARCHAR(100) DEFAULT NULL COMMENT '通知标题',
    notification_detail VARCHAR(500) DEFAULT NULL COMMENT '通知内容',
    notification_read   INT          DEFAULT 0 COMMENT '是否已读，0 未读、1 已读',
    notification_time   DATETIME     DEFAULT NULL COMMENT '通知时间',
    PRIMARY KEY (notification_num),
    KEY idx_notification_receiver (receiver_id, notification_read),
    KEY idx_notification_time (notification_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='站内通知表';

-- -----------------------------------------------------------------------------
-- 19. 操作审计表：记录平台管理员与志愿者组织的关键操作，便于事后追溯与责任界定
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS audit_log;
CREATE TABLE audit_log
(
    audit_num      INT AUTO_INCREMENT COMMENT '审计编号，主键自增',
    audit_operator VARCHAR(32)  DEFAULT NULL COMMENT '操作账号',
    audit_role     VARCHAR(64)  DEFAULT NULL COMMENT '操作角色',
    audit_action   VARCHAR(64)  DEFAULT NULL COMMENT '操作类型编码',
    audit_target   VARCHAR(200) DEFAULT NULL COMMENT '操作对象',
    audit_detail   VARCHAR(500) DEFAULT NULL COMMENT '操作详情',
    audit_ip       VARCHAR(64)  DEFAULT NULL COMMENT '操作来源 IP',
    audit_time     DATETIME     DEFAULT NULL COMMENT '操作时间',
    PRIMARY KEY (audit_num),
    KEY idx_audit_time (audit_time),
    KEY idx_audit_action (audit_action),
    KEY idx_audit_operator (audit_operator)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='操作审计表';

-- -----------------------------------------------------------------------------
-- 20. 时长异常规则表：保存服务时长异常检测规则的阈值与开关，规则由存储过程读取后执行
--     rule_threshold 含义：SINGLE_DAY_TOTAL 为单日累计小时上限、SINGLE_RECORD 为单条小时上限、
--     NIGHT_SERVICE 为夜间起始小时、MANUAL_BATCH 为同一报名补录条数上限
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS anomaly_rule;
CREATE TABLE anomaly_rule
(
    rule_num       INT AUTO_INCREMENT COMMENT '规则编号，主键自增',
    rule_code      VARCHAR(32)  NOT NULL COMMENT '规则编码',
    rule_name      VARCHAR(100) DEFAULT NULL COMMENT '规则名称',
    rule_threshold DECIMAL(6, 2) DEFAULT 0 COMMENT '规则阈值',
    rule_enabled   INT          DEFAULT 1 COMMENT '是否启用，1 启用、0 停用',
    PRIMARY KEY (rule_num),
    UNIQUE KEY uk_anomaly_rule_code (rule_code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='时长异常规则表';

-- =============================================================================
-- 存储过程与函数
-- =============================================================================
DELIMITER $$

-- -----------------------------------------------------------------------------
-- 志愿者注册：写入志愿者信息、生成志愿者编号并开通登录账号
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS volunteer_insert $$
CREATE PROCEDURE volunteer_insert(IN volunteerName VARCHAR(50),
                                  IN volunteerTel VARCHAR(20),
                                  IN volunteerPassword VARCHAR(200),
                                  OUT volunteerId VARCHAR(32))
BEGIN
    DECLARE duplicateCount INT DEFAULT 0;
    DECLARE newNum INT DEFAULT 0;
    DECLARE newVolunteerId VARCHAR(32) DEFAULT NULL;

    SELECT COUNT(*) INTO duplicateCount FROM volunteer WHERE volunteer_tel = volunteerTel;

    IF duplicateCount > 0 THEN
        SET volunteerId = NULL;
    ELSE
        INSERT INTO volunteer(volunteer_id, volunteer_name,
                              volunteer_registerdate, volunteer_tel,
                              volunteer_credit, volunteer_noshow, volunteer_totalduration)
        VALUES ('', volunteerName, CURDATE(), volunteerTel, 100, 0, 0);
        SET newNum = LAST_INSERT_ID();
        SET newVolunteerId = CONCAT('vol_', LPAD(newNum, 5, '0'));
        UPDATE volunteer SET volunteer_id = newVolunteerId WHERE volunteer_num = newNum;
        -- 注册即开通登录账号，志愿者可凭注册编号进入个人工作台
        INSERT INTO e_user(id, password, role) VALUES (newVolunteerId, volunteerPassword, 'ROLE_VOLUNTEER');
        SET volunteerId = newVolunteerId;
    END IF;
END $$

-- -----------------------------------------------------------------------------
-- 活动技能标签维护：按逗号分隔的技能标签重建活动的技能要求
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS activity_skill_replace $$
CREATE PROCEDURE activity_skill_replace(IN targetActivityNum INT, IN skillNames VARCHAR(200))
BEGIN
    DECLARE rest VARCHAR(200) DEFAULT NULL;
    DECLARE currentSkill VARCHAR(50) DEFAULT NULL;

    DELETE FROM activity_skill WHERE activity_num = targetActivityNum;

    IF skillNames IS NOT NULL AND TRIM(skillNames) <> '' THEN
        SET rest = REPLACE(TRIM(skillNames), '，', ',');
        WHILE rest <> '' DO
            IF LOCATE(',', rest) > 0 THEN
                SET currentSkill = TRIM(SUBSTRING(rest, 1, LOCATE(',', rest) - 1));
                SET rest = SUBSTRING(rest, LOCATE(',', rest) + 1);
            ELSE
                SET currentSkill = TRIM(rest);
                SET rest = '';
            END IF;
            IF currentSkill <> '' THEN
                INSERT IGNORE INTO activity_skill(activity_num, skill_name)
                VALUES (targetActivityNum, currentSkill);
            END IF;
        END WHILE;
    END IF;
END $$

-- -----------------------------------------------------------------------------
-- 志愿活动申报：志愿者组织申报志愿活动，写入技能标签与签到围栏，申报后进入审核中状态
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS organization_insert_activity $$
CREATE PROCEDURE organization_insert_activity(IN loginId VARCHAR(32),
                                              IN activityName VARCHAR(100),
                                              IN activityDetail TEXT,
                                              IN activityBegintime DATETIME,
                                              IN activityEndtime DATETIME,
                                              IN activityLocation VARCHAR(200),
                                              IN activityNeedpeople VARCHAR(20),
                                              IN activityNotice VARCHAR(500),
                                              IN activitySignddl DATETIME,
                                              IN activitySkillNames VARCHAR(200),
                                              IN activityLatitude DECIMAL(10, 7),
                                              IN activityLongitude DECIMAL(10, 7),
                                              IN activityRadius INT,
                                              OUT msg VARCHAR(200))
BEGIN
    DECLARE orgNum INT DEFAULT NULL;
    DECLARE newNum INT DEFAULT 0;
    DECLARE newSecret VARCHAR(32) DEFAULT NULL;

    SELECT organization_num INTO orgNum FROM organization WHERE organization_id = loginId LIMIT 1;

    IF orgNum IS NULL THEN
        SET msg = '未找到志愿者组织信息，请重新登录后再试';
    ELSEIF activityBegintime IS NULL OR activityEndtime IS NULL OR activityEndtime <= activityBegintime THEN
        SET msg = '活动开始时间与结束时间不正确，请重新填写';
    ELSE
        INSERT INTO activity(organization_num, activity_id, activity_name, activity_detail,
                             activity_begintime, activity_endtime, activity_location,
                             activity_needpeople, activity_notice, activity_publishtime,
                             activity_signddl, activity_state,
                             activity_latitude, activity_longitude, activity_radius,
                             activity_isdeleted)
        VALUES (orgNum, '', activityName, activityDetail,
                activityBegintime, activityEndtime, activityLocation,
                CAST(activityNeedpeople AS UNSIGNED), activityNotice, NOW(),
                activitySignddl, '0',
                activityLatitude, activityLongitude, IFNULL(activityRadius, 300),
                0);
        SET newNum = LAST_INSERT_ID();
        -- 现场签到码由签到密钥与时间窗口派生，每个活动生成独立的随机密钥
        SET newSecret = UPPER(SUBSTRING(SHA2(CONCAT(newNum, RAND(), NOW(), UUID()), 256), 1, 32));
        UPDATE activity
        SET activity_id = CONCAT('act_', LPAD(newNum, 5, '0')),
            activity_checkin_secret = newSecret
        WHERE activity_num = newNum;
        CALL activity_skill_replace(newNum, activitySkillNames);
        SET msg = '志愿活动申报成功，等待平台管理员审核';
    END IF;
END $$

-- -----------------------------------------------------------------------------
-- 志愿活动审核：平台管理员审核志愿活动，审核不通过时必须给出结构化的原因
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS admin_check_activity $$
CREATE PROCEDURE admin_check_activity(IN loginId VARCHAR(32),
                                      IN activityNum INT,
                                      IN isPass INT,
                                      IN reasonCode VARCHAR(32),
                                      IN remark VARCHAR(500),
                                      OUT msg VARCHAR(200))
BEGIN
    DECLARE adminNumValue INT DEFAULT NULL;
    DECLARE affectedRows INT DEFAULT 0;

    SELECT admin_num INTO adminNumValue FROM admin WHERE admin_id = loginId LIMIT 1;

    IF isPass <> 1 AND (reasonCode IS NULL OR TRIM(reasonCode) = '') THEN
        SET msg = '请选择审核不通过的原因';
    ELSE
        UPDATE activity
        SET activity_state       = isPass,
            admin_num            = adminNumValue,
            activity_checktime   = NOW(),
            activity_remark      = remark,
            activity_reason_code = IF(isPass = 1, NULL, reasonCode)
        WHERE activity_num = activityNum
          AND activity_isdeleted = 0;

        SET affectedRows = ROW_COUNT();

        IF affectedRows > 0 AND isPass = 1 THEN
            SET msg = '审核通过';
        ELSEIF affectedRows > 0 THEN
            SET msg = '审核未通过，请志愿者组织按原因整改后重新申报';
        ELSE
            SET msg = '审核失败，未找到对应的志愿活动';
        END IF;
    END IF;
END $$

-- -----------------------------------------------------------------------------
-- 志愿活动重新申报：志愿者组织针对审核未通过的活动修改后重新提交审核
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS organization_revise_activity $$
CREATE PROCEDURE organization_revise_activity(IN loginId VARCHAR(32),
                                              IN activityNum INT,
                                              IN activityName VARCHAR(100),
                                              IN activityDetail TEXT,
                                              IN activityBegintime DATETIME,
                                              IN activityEndtime DATETIME,
                                              IN activityLocation VARCHAR(200),
                                              IN activityNeedpeople VARCHAR(20),
                                              IN activityNotice VARCHAR(500),
                                              IN activitySignddl DATETIME,
                                              IN activitySkillNames VARCHAR(200),
                                              IN activityLatitude DECIMAL(10, 7),
                                              IN activityLongitude DECIMAL(10, 7),
                                              IN activityRadius INT,
                                              OUT msg VARCHAR(200))
BEGIN
    DECLARE orgNum INT DEFAULT NULL;
    DECLARE ownerOrgNum INT DEFAULT NULL;
    DECLARE activityState VARCHAR(4) DEFAULT NULL;
    DECLARE duplicateCount INT DEFAULT 0;

    SELECT organization_num INTO orgNum FROM organization WHERE organization_id = loginId LIMIT 1;
    SELECT organization_num, activity_state
    INTO ownerOrgNum, activityState
    FROM activity
    WHERE activity_num = activityNum
      AND activity_isdeleted = 0;

    IF activityState IS NULL THEN
        SET msg = '未找到对应的志愿活动';
    ELSEIF ownerOrgNum <> orgNum THEN
        SET msg = '只能修改本组织申报的志愿活动';
    ELSEIF activityState <> '4' THEN
        SET msg = '仅审核未通过的志愿活动支持修改后重新申报';
    ELSEIF activityBegintime IS NULL OR activityEndtime IS NULL OR activityEndtime <= activityBegintime THEN
        SET msg = '活动开始时间与结束时间不正确，请重新填写';
    ELSE
        SELECT COUNT(*) INTO duplicateCount
        FROM activity
        WHERE activity_name = activityName
          AND activity_num <> activityNum
          AND activity_isdeleted = 0;

        IF duplicateCount > 0 THEN
            SET msg = '活动名称已被使用，请更换活动名称';
        ELSE
            UPDATE activity
            SET activity_name        = activityName,
                activity_detail      = activityDetail,
                activity_begintime   = activityBegintime,
                activity_endtime     = activityEndtime,
                activity_location    = activityLocation,
                activity_needpeople  = CAST(activityNeedpeople AS UNSIGNED),
                activity_notice      = activityNotice,
                activity_signddl     = activitySignddl,
                activity_latitude    = activityLatitude,
                activity_longitude   = activityLongitude,
                activity_radius      = IFNULL(activityRadius, 300),
                activity_state       = '0',
                activity_revision    = activity_revision + 1,
                activity_publishtime = NOW(),
                admin_num            = NULL,
                activity_checktime   = NULL,
                activity_remark      = NULL,
                activity_reason_code = NULL
            WHERE activity_num = activityNum;

            CALL activity_skill_replace(activityNum, activitySkillNames);
            SET msg = '志愿活动已修改并重新提交审核';
        END IF;
    END IF;
END $$

-- -----------------------------------------------------------------------------
-- 报名审核：志愿者组织审核志愿者的活动报名申请，审核通过前校验活动剩余名额
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS organization_check_volunteer $$
CREATE PROCEDURE organization_check_volunteer(IN participateNum INT,
                                              IN isPass INT,
                                              OUT msg VARCHAR(200))
BEGIN
    DECLARE applyState VARCHAR(4) DEFAULT NULL;
    DECLARE activityNumValue INT DEFAULT NULL;
    DECLARE needPeople INT DEFAULT 0;
    DECLARE approvedCount INT DEFAULT 0;

    SELECT participate_applystate, activity_num
    INTO applyState, activityNumValue
    FROM participate
    WHERE participate_num = participateNum
      AND participate_isdeleted = 0;

    IF applyState IS NULL THEN
        SET msg = '操作失败，未找到对应的报名记录';
    ELSEIF applyState NOT IN ('0', '3') THEN
        SET msg = '该报名申请已处理，无需重复审核';
    ELSEIF isPass = 1 THEN
        -- 加锁读取活动名额，避免并发审核导致通过人数超出需求人数
        SELECT activity_needpeople INTO needPeople FROM activity WHERE activity_num = activityNumValue FOR UPDATE;
        SELECT COUNT(*) INTO approvedCount
        FROM participate
        WHERE activity_num = activityNumValue
          AND participate_applystate = '1'
          AND participate_isdeleted = 0;

        IF approvedCount >= IFNULL(needPeople, 0) THEN
            SET msg = '活动名额已满，无法再通过报名申请';
        ELSE
            UPDATE participate
            SET participate_applystate     = '1',
                participate_applychecktime = NOW()
            WHERE participate_num = participateNum;
            SET msg = '报名申请已通过';
        END IF;
    ELSE
        UPDATE participate
        SET participate_applystate     = '2',
            participate_applychecktime = NOW()
        WHERE participate_num = participateNum;
        SET msg = '报名申请未通过';
    END IF;
END $$

-- -----------------------------------------------------------------------------
-- 活动报名：志愿者报名志愿活动，校验信用分、报名截止时间与名额，满员进入候补队列
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS volunteer_apply_activity $$
CREATE PROCEDURE volunteer_apply_activity(IN volunteerNum INT,
                                          IN activityNum INT,
                                          OUT msg VARCHAR(200),
                                          OUT ok INT)
BEGIN
    DECLARE activityState VARCHAR(4) DEFAULT NULL;
    DECLARE activityEndtimeValue DATETIME DEFAULT NULL;
    DECLARE signDeadline DATETIME DEFAULT NULL;
    DECLARE needPeople INT DEFAULT 0;
    DECLARE approvedCount INT DEFAULT 0;
    DECLARE creditValue INT DEFAULT NULL;
    DECLARE appliedCount INT DEFAULT 0;

    SET ok = 0;

    SELECT activity_state, activity_endtime, activity_signddl, activity_needpeople
    INTO activityState, activityEndtimeValue, signDeadline, needPeople
    FROM activity
    WHERE activity_num = activityNum
      AND activity_isdeleted = 0
    FOR UPDATE;

    SELECT volunteer_credit INTO creditValue FROM volunteer WHERE volunteer_num = volunteerNum;

    IF activityState IS NULL THEN
        SET msg = '该志愿活动不存在或已下线';
    ELSEIF creditValue IS NULL THEN
        SET msg = '未找到志愿者信息，请重新登录后再试';
    ELSEIF activityState <> '1' THEN
        SET msg = '该志愿活动当前不接受报名';
    ELSEIF signDeadline IS NOT NULL AND NOW() > signDeadline THEN
        SET msg = '该志愿活动报名已截止';
    ELSEIF activityEndtimeValue IS NOT NULL AND NOW() > activityEndtimeValue THEN
        SET msg = '该志愿活动已结束，无法报名';
    ELSEIF creditValue < 60 THEN
        SET msg = '您的志愿服务信用分低于 60 分，暂时无法报名新的活动，可先参与已报名的服务并按时签到恢复信用';
    ELSE
        SELECT COUNT(*) INTO appliedCount
        FROM participate
        WHERE activity_num = activityNum
          AND volunteer_num = volunteerNum
          AND participate_isdeleted = 0;

        IF appliedCount > 0 THEN
            SET msg = '您已报名该活动，请勿重复报名';
        ELSE
            SELECT COUNT(*) INTO approvedCount
            FROM participate
            WHERE activity_num = activityNum
              AND participate_applystate = '1'
              AND participate_isdeleted = 0;

            IF approvedCount >= IFNULL(needPeople, 0) THEN
                INSERT INTO participate(volunteer_num, activity_num, participate_applytime,
                                        participate_applystate, participate_noshow, participate_isdeleted)
                VALUES (volunteerNum, activityNum, NOW(), '3', 0, 0);
                SET msg = '名额已满，您已进入候补队列，如有报名退出将自动递补';
                SET ok = 1;
            ELSE
                INSERT INTO participate(volunteer_num, activity_num, participate_applytime,
                                        participate_applystate, participate_noshow, participate_isdeleted)
                VALUES (volunteerNum, activityNum, NOW(), '0', 0, 0);
                SET msg = '报名申请已提交，等待志愿者组织审核';
                SET ok = 1;
            END IF;
        END IF;
    END IF;
END $$

-- -----------------------------------------------------------------------------
-- 撤回报名：志愿者撤回报名，已通过的报名释放名额并自动递补候补队列中的第一位志愿者
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS volunteer_cancel_participate $$
CREATE PROCEDURE volunteer_cancel_participate(IN participateNum INT,
                                              IN volunteerNum INT,
                                              OUT msg VARCHAR(200),
                                              OUT ok INT)
BEGIN
    DECLARE applyState VARCHAR(4) DEFAULT NULL;
    DECLARE activityNumValue INT DEFAULT NULL;
    DECLARE ownerVolunteerNum INT DEFAULT NULL;
    DECLARE nextParticipateNum INT DEFAULT NULL;
    DECLARE checkedInCount INT DEFAULT 0;

    SET ok = 0;

    SELECT participate_applystate, activity_num, volunteer_num
    INTO applyState, activityNumValue, ownerVolunteerNum
    FROM participate
    WHERE participate_num = participateNum
      AND participate_isdeleted = 0
    FOR UPDATE;

    SELECT COUNT(*) INTO checkedInCount
    FROM checkin
    WHERE participate_num = participateNum;

    IF ownerVolunteerNum IS NULL THEN
        SET msg = '未找到对应的报名记录';
    ELSEIF ownerVolunteerNum <> volunteerNum THEN
        SET msg = '只能撤回本人的报名记录';
    ELSEIF applyState = '2' THEN
        SET msg = '审核未通过的报名无需撤回';
    ELSEIF checkedInCount > 0 THEN
        SET msg = '您已签到，无法撤回报名，如需调整请联系志愿者组织';
    ELSE
        UPDATE participate
        SET participate_isdeleted = 1,
            participate_cancel_time = NOW()
        WHERE participate_num = participateNum;

        IF applyState = '1' THEN
            SELECT participate_num INTO nextParticipateNum
            FROM participate
            WHERE activity_num = activityNumValue
              AND participate_applystate = '3'
              AND participate_isdeleted = 0
            ORDER BY participate_applytime ASC, participate_num ASC
            LIMIT 1
            FOR UPDATE;

            IF nextParticipateNum IS NOT NULL THEN
                UPDATE participate SET participate_applystate = '0' WHERE participate_num = nextParticipateNum;
                SET msg = '已撤回报名并释放名额，候补志愿者已自动递补进入待审核';
                SET ok = 1;
            ELSE
                SET msg = '已撤回报名并释放名额';
                SET ok = 1;
            END IF;
        ELSE
            SET msg = '已撤回报名';
            SET ok = 1;
        END IF;
    END IF;
END $$

-- -----------------------------------------------------------------------------
-- 志愿者签到：校验现场签到码、签到时间窗口与地理围栏，写入签到轨迹
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS volunteer_checkin $$
CREATE PROCEDURE volunteer_checkin(IN volunteerNum INT,
                                   IN activityNum INT,
                                   IN inputCode VARCHAR(16),
                                   IN inputLatitude DECIMAL(10, 7),
                                   IN inputLongitude DECIMAL(10, 7),
                                   OUT msg VARCHAR(200),
                                   OUT flag VARCHAR(4),
                                   OUT ok INT)
BEGIN
    DECLARE participateNumValue INT DEFAULT NULL;
    DECLARE activityState VARCHAR(4) DEFAULT NULL;
    DECLARE activityBegintimeValue DATETIME DEFAULT NULL;
    DECLARE activityEndtimeValue DATETIME DEFAULT NULL;
    DECLARE activitySecret VARCHAR(32) DEFAULT NULL;
    DECLARE rotationIndex BIGINT DEFAULT 0;
    DECLARE activityLatitudeValue DECIMAL(10, 7) DEFAULT NULL;
    DECLARE activityLongitudeValue DECIMAL(10, 7) DEFAULT NULL;
    DECLARE activityRadiusValue INT DEFAULT NULL;
    DECLARE pendingCount INT DEFAULT 0;
    DECLARE checkedInCount INT DEFAULT 0;
    DECLARE distanceValue INT DEFAULT NULL;

    SET flag = '1';
    SET ok = 0;

    SELECT activity_state, activity_begintime, activity_endtime, activity_checkin_secret,
           activity_latitude, activity_longitude, activity_radius
    INTO activityState, activityBegintimeValue, activityEndtimeValue, activitySecret,
        activityLatitudeValue, activityLongitudeValue, activityRadiusValue
    FROM activity
    WHERE activity_num = activityNum
      AND activity_isdeleted = 0;

    -- 现场签到码每 120 秒轮换一次，接受当前与上一个时间窗口，兼容志愿者输入耗时与时钟偏差
    SET rotationIndex = FLOOR(UNIX_TIMESTAMP(NOW()) / 120);

    SELECT participate_num INTO participateNumValue
    FROM participate
    WHERE activity_num = activityNum
      AND volunteer_num = volunteerNum
      AND participate_applystate = '1'
      AND participate_isdeleted = 0
    LIMIT 1;

    IF activityState IS NULL THEN
        SET msg = '该志愿活动不存在或已下线';
    ELSEIF activityState NOT IN ('1', '2') THEN
        SET msg = '该志愿活动当前不接受签到';
    ELSEIF participateNumValue IS NULL THEN
        SET msg = '您没有该活动的有效报名记录，无法签到';
    ELSE
        SELECT COUNT(*) INTO checkedInCount
        FROM checkin
        WHERE participate_num = participateNumValue;

        SELECT COUNT(*) INTO pendingCount
        FROM checkin
        WHERE participate_num = participateNumValue
          AND checkin_endtime IS NULL;

        IF pendingCount > 0 THEN
            SET msg = '您已签到，请在活动结束后签退';
        ELSEIF checkedInCount > 0 THEN
            -- 一次报名只对应一组签到签退记录，避免重复签到造成服务时长重复累计
            SET msg = '您已完成该活动的签到签退，服务时长待志愿者组织复核，无需重复签到';
        ELSEIF NOW() < DATE_SUB(activityBegintimeValue, INTERVAL 60 MINUTE) THEN
            SET msg = '签到尚未开始，请在活动开始前 60 分钟内签到';
        ELSEIF activityEndtimeValue IS NOT NULL AND NOW() > activityEndtimeValue THEN
            SET msg = '该志愿活动已结束，无法签到';
        ELSEIF activitySecret IS NULL
            OR (UPPER(TRIM(inputCode)) <> activity_checkin_code(activitySecret, activityNum, rotationIndex)
                AND UPPER(TRIM(inputCode)) <> activity_checkin_code(activitySecret, activityNum, rotationIndex - 1)) THEN
            SET msg = '签到码不正确或已过期，请向活动负责人确认当前签到码';
        ELSE
            -- 计算签到位置与活动地点的球面距离，超出地理围栏则标记为异常待复核
            IF activityLatitudeValue IS NOT NULL AND activityLongitudeValue IS NOT NULL
                AND inputLatitude IS NOT NULL AND inputLongitude IS NOT NULL THEN
                SET distanceValue = ROUND(ST_Distance_Sphere(
                        POINT(activityLongitudeValue, activityLatitudeValue),
                        POINT(inputLongitude, inputLatitude)), 0);
                IF activityRadiusValue IS NOT NULL AND distanceValue > activityRadiusValue THEN
                    SET flag = '2';
                END IF;
            ELSE
                -- 活动地点或签到定位缺失，无法验证位置，交由志愿者组织复核
                SET flag = '2';
            END IF;

            INSERT INTO checkin(participate_num, checkin_begintime, checkin_code,
                                checkin_latitude, checkin_longitude, checkin_distance, checkin_flag)
            VALUES (participateNumValue, NOW(), UPPER(TRIM(inputCode)),
                    inputLatitude, inputLongitude, distanceValue, flag);

            UPDATE participate
            SET participate_begintime = NOW(),
                participate_timecheck = NULL
            WHERE participate_num = participateNumValue;

            IF flag = '2' THEN
                SET msg = '签到成功，但签到位置存在异常，已提交志愿者组织复核';
            ELSE
                SET msg = '签到成功，请在活动结束后签退';
            END IF;
            SET ok = 1;
        END IF;
    END IF;
END $$

-- -----------------------------------------------------------------------------
-- 志愿者签退：按签到签退时间自动核算服务时长，异常时长标记后交志愿者组织复核
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS volunteer_checkout $$
CREATE PROCEDURE volunteer_checkout(IN volunteerNum INT,
                                    IN activityNum INT,
                                    IN inputLatitude DECIMAL(10, 7),
                                    IN inputLongitude DECIMAL(10, 7),
                                    OUT msg VARCHAR(200),
                                    OUT duration DOUBLE,
                                    OUT ok INT)
BEGIN
    DECLARE participateNumValue INT DEFAULT NULL;
    DECLARE checkinNumValue INT DEFAULT NULL;
    DECLARE checkinBegintimeValue DATETIME DEFAULT NULL;
    DECLARE checkinFlagValue VARCHAR(4) DEFAULT '1';
    DECLARE activityBegintimeValue DATETIME DEFAULT NULL;
    DECLARE activityEndtimeValue DATETIME DEFAULT NULL;
    DECLARE activityLatitudeValue DECIMAL(10, 7) DEFAULT NULL;
    DECLARE activityLongitudeValue DECIMAL(10, 7) DEFAULT NULL;
    DECLARE outDistanceValue INT DEFAULT NULL;
    DECLARE planDuration DECIMAL(10, 2) DEFAULT 0;
    DECLARE flagValue VARCHAR(4) DEFAULT '1';

    SET ok = 0;
    SELECT p.participate_num INTO participateNumValue
    FROM participate p
    WHERE p.activity_num = activityNum
      AND p.volunteer_num = volunteerNum
      AND p.participate_applystate = '1'
      AND p.participate_isdeleted = 0
    LIMIT 1;

    SELECT activity_begintime, activity_endtime, activity_latitude, activity_longitude
    INTO activityBegintimeValue, activityEndtimeValue, activityLatitudeValue, activityLongitudeValue
    FROM activity
    WHERE activity_num = activityNum
      AND activity_isdeleted = 0;

    SELECT checkin_num, checkin_begintime, checkin_flag
    INTO checkinNumValue, checkinBegintimeValue, checkinFlagValue
    FROM checkin
    WHERE participate_num = participateNumValue
      AND checkin_endtime IS NULL
    ORDER BY checkin_num DESC
    LIMIT 1;

    IF participateNumValue IS NULL THEN
        SET msg = '您没有该活动的有效报名记录，无法签退';
    ELSEIF checkinNumValue IS NULL THEN
        SET msg = '未找到签到记录，请先签到后再签退';
    ELSEIF activityEndtimeValue IS NOT NULL AND NOW() > DATE_ADD(activityEndtimeValue, INTERVAL 120 MINUTE) THEN
        SET msg = '签退时间已超出允许范围，请联系志愿者组织手工核定时长';
    ELSE
        SET duration = ROUND(TIMESTAMPDIFF(SECOND, checkinBegintimeValue, NOW()) / 3600, 2);
        SET planDuration = TIMESTAMPDIFF(SECOND, activityBegintimeValue, activityEndtimeValue) / 3600;

        IF activityLatitudeValue IS NOT NULL AND activityLongitudeValue IS NOT NULL
            AND inputLatitude IS NOT NULL AND inputLongitude IS NOT NULL THEN
            SET outDistanceValue = ROUND(ST_Distance_Sphere(
                    POINT(activityLongitudeValue, activityLatitudeValue),
                    POINT(inputLongitude, inputLatitude)), 0);
        END IF;

        -- 时长过短或远超活动计划时长、以及签到阶段已标记的异常，均保留异常标记交由组织复核
        IF checkinFlagValue = '2' OR duration < 0.5 OR duration > planDuration * 2 + 1 THEN
            SET flagValue = '2';
        END IF;

        UPDATE checkin
        SET checkin_endtime    = NOW(),
            checkin_duration   = duration,
            checkin_outdistance = outDistanceValue,
            checkin_flag       = flagValue
        WHERE checkin_num = checkinNumValue;

        UPDATE participate
        SET participate_endtime   = NOW(),
            participate_duration  = duration,
            participate_timecheck = NULL
        WHERE participate_num = participateNumValue;

        IF flagValue = '2' THEN
            SET msg = CONCAT('签退成功，本次服务时长 ', duration, ' 小时，因轨迹或时长异常需志愿者组织复核');
        ELSE
            SET msg = CONCAT('签退成功，本次服务时长 ', duration, ' 小时，待志愿者组织复核后计入累计时长');
        END IF;
        SET ok = 1;
    END IF;
END $$

-- -----------------------------------------------------------------------------
-- 服务时长复核：志愿者组织确认或驳回签到核算出的服务时长，确认后计入累计时长并奖励信用分
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS organization_check_checkin $$
CREATE PROCEDURE organization_check_checkin(IN checkinNum INT,
                                            IN isPass INT,
                                            IN remark VARCHAR(200),
                                            OUT msg VARCHAR(200),
                                            OUT ok INT)
BEGIN
    DECLARE timecheckValue VARCHAR(4) DEFAULT NULL;
    DECLARE durationValue DOUBLE DEFAULT NULL;
    DECLARE participateNumValue INT DEFAULT NULL;
    DECLARE volunteerNumValue INT DEFAULT NULL;
    DECLARE sourceValue VARCHAR(4) DEFAULT NULL;
    DECLARE objectConfirmValue VARCHAR(4) DEFAULT NULL;
    DECLARE anomalyCode VARCHAR(32) DEFAULT NULL;
    DECLARE anomalyMessage VARCHAR(200) DEFAULT NULL;

    SET ok = 0;

    SELECT checkin_timecheck, checkin_duration, participate_num, checkin_source, checkin_objectconfirm
    INTO timecheckValue, durationValue, participateNumValue, sourceValue, objectConfirmValue
    FROM checkin
    WHERE checkin_num = checkinNum;

    IF sourceValue = '2' THEN
        -- 志愿者组织补录的记录由平台管理员复核，避免录入与复核由同一方完成
        SET msg = '该记录为志愿者组织补录，需由平台管理员复核';
    ELSEIF participateNumValue IS NULL THEN
        SET msg = '未找到对应的签到记录';
    ELSEIF timecheckValue IS NOT NULL THEN
        SET msg = '该服务时长已复核，无需重复处理';
    ELSEIF objectConfirmValue = '2' THEN
        -- 服务对象已否认该次服务，责任方不能直接确认时长，需由平台管理员裁定
        SET msg = '服务对象已否认该次服务，请核实后联系平台管理员处理';
    ELSEIF isPass = 1 THEN
        CALL service_anomaly_check(checkinNum, anomalyCode, anomalyMessage);

        IF anomalyCode IS NOT NULL THEN
            -- 命中时长异常规则：本次不计入累计时长，转平台管理员裁定
            UPDATE checkin
            SET checkin_anomaly = anomalyCode,
                checkin_remark  = remark
            WHERE checkin_num = checkinNum;
            SET msg = CONCAT('已提交复核，', anomalyMessage, '，需平台管理员裁定后计入累计时长');
            SET ok = 1;
        ELSE
            SELECT volunteer_num INTO volunteerNumValue
            FROM participate
            WHERE participate_num = participateNumValue;

            UPDATE checkin
            SET checkin_timecheck = '1',
                checkin_checktime = NOW(),
                checkin_remark    = remark
            WHERE checkin_num = checkinNum;

            UPDATE participate
            SET participate_timecheck = '1',
                participate_duration  = IFNULL(durationValue, participate_duration)
            WHERE participate_num = participateNumValue;

            UPDATE volunteer
            SET volunteer_totalduration = volunteer_totalduration + IFNULL(durationValue, 0),
                volunteer_credit        = LEAST(120, volunteer_credit + 2)
            WHERE volunteer_num = volunteerNumValue;

            SET msg = CONCAT('已确认服务时长 ', IFNULL(durationValue, 0), ' 小时，并计入志愿者累计服务时长');
            SET ok = 1;
        END IF;
    ELSE
        UPDATE checkin
        SET checkin_timecheck = '2',
            checkin_checktime = NOW(),
            checkin_remark    = remark
        WHERE checkin_num = checkinNum;

        UPDATE participate
        SET participate_timecheck = '2'
        WHERE participate_num = participateNumValue;

        SET msg = '已驳回该服务时长记录，本次时长不计入累计服务时长';
        SET ok = 1;
    END IF;
END $$

-- -----------------------------------------------------------------------------
-- 活动结算：活动结束后结算考勤，对已通过报名但未签退的志愿者记爽约并扣减信用分
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS organization_settle_activity $$
CREATE PROCEDURE organization_settle_activity(IN activityNum INT,
                                              OUT msg VARCHAR(200),
                                              OUT ok INT)
BEGIN
    DECLARE settleTime DATETIME DEFAULT NULL;
    DECLARE activityEndtimeValue DATETIME DEFAULT NULL;
    DECLARE activityStateValue VARCHAR(4) DEFAULT NULL;
    DECLARE noshowCount INT DEFAULT 0;
    DECLARE done INT DEFAULT 0;
    DECLARE curParticipateNum INT DEFAULT NULL;
    DECLARE curVolunteerNum INT DEFAULT NULL;
    DECLARE absentCursor CURSOR FOR
        SELECT p.participate_num, p.volunteer_num
        FROM participate p
        WHERE p.activity_num = activityNum
          AND p.participate_applystate = '1'
          AND p.participate_isdeleted = 0
          AND p.participate_noshow = 0
          AND NOT EXISTS (SELECT 1
                          FROM checkin c
                          WHERE c.participate_num = p.participate_num
                            AND c.checkin_endtime IS NOT NULL);
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    SET ok = 0;

    SELECT activity_settle_time, activity_endtime, activity_state
    INTO settleTime, activityEndtimeValue, activityStateValue
    FROM activity
    WHERE activity_num = activityNum
      AND activity_isdeleted = 0;

    IF settleTime IS NULL AND NOT EXISTS(SELECT 1 FROM activity WHERE activity_num = activityNum AND activity_isdeleted = 0) THEN
        SET msg = '未找到对应的志愿活动';
    ELSEIF settleTime IS NOT NULL THEN
        SET msg = '该志愿活动已完成结算，无需重复结算';
    -- 活动已由志愿者组织人工置为已结束时，允许在计划结束时间之前结算
    ELSEIF activityStateValue <> '3' AND (activityEndtimeValue IS NULL OR NOW() < activityEndtimeValue) THEN
        SET msg = '活动尚未结束，暂不能结算考勤';
    ELSE
        OPEN absentCursor;
        read_loop:
        LOOP
            FETCH absentCursor INTO curParticipateNum, curVolunteerNum;
            IF done = 1 THEN
                LEAVE read_loop;
            END IF;

            UPDATE participate SET participate_noshow = 1 WHERE participate_num = curParticipateNum;
            UPDATE volunteer
            SET volunteer_noshow = volunteer_noshow + 1,
                volunteer_credit = GREATEST(0, volunteer_credit - 10)
            WHERE volunteer_num = curVolunteerNum;
            SET noshowCount = noshowCount + 1;
        END LOOP;
        CLOSE absentCursor;

        UPDATE activity SET activity_settle_time = NOW() WHERE activity_num = activityNum;
        SET msg = CONCAT('活动结算完成，共记录 ', noshowCount, ' 名志愿者爽约并扣减信用分');
        SET ok = 1;
    END IF;
END $$

-- -----------------------------------------------------------------------------
-- 活动状态流转：按活动时间自动把未开始的活动置为进行中、把到期的活动置为已结束
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS activity_state_refresh $$
CREATE PROCEDURE activity_state_refresh(OUT startedCount INT, OUT finishedCount INT)
BEGIN
    UPDATE activity
    SET activity_state = '2'
    WHERE activity_isdeleted = 0
      AND activity_state = '1'
      AND activity_begintime IS NOT NULL
      AND NOW() >= activity_begintime
      AND (activity_endtime IS NULL OR NOW() < activity_endtime);
    SET startedCount = ROW_COUNT();

    UPDATE activity
    SET activity_state = '3'
    WHERE activity_isdeleted = 0
      AND activity_state IN ('1', '2')
      AND activity_endtime IS NOT NULL
      AND NOW() >= activity_endtime;
    SET finishedCount = ROW_COUNT();
END $$

-- -----------------------------------------------------------------------------
-- 活动状态人工流转：志愿者组织将本组织申报的活动置为进行中或已结束
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS organization_change_activity_state $$
CREATE PROCEDURE organization_change_activity_state(IN loginId VARCHAR(32),
                                                    IN activityNum INT,
                                                    IN targetState VARCHAR(4),
                                                    OUT msg VARCHAR(200),
                                                    OUT ok INT)
BEGIN
    DECLARE orgNum INT DEFAULT NULL;
    DECLARE ownerOrgNum INT DEFAULT NULL;
    DECLARE activityState VARCHAR(4) DEFAULT NULL;
    DECLARE activityName VARCHAR(100) DEFAULT NULL;

    SET ok = 0;

    SELECT organization_num INTO orgNum FROM organization WHERE organization_id = loginId LIMIT 1;
    SELECT organization_num, activity_state, activity_name
    INTO ownerOrgNum, activityState, activityName
    FROM activity
    WHERE activity_num = activityNum
      AND activity_isdeleted = 0;

    IF activityState IS NULL THEN
        SET msg = '未找到对应的志愿活动';
    ELSEIF ownerOrgNum <> orgNum THEN
        SET msg = '只能管理本组织申报的志愿活动';
    ELSEIF targetState = '2' THEN
        IF activityState <> '1' THEN
            SET msg = '只有未开始的活动可以置为进行中';
        ELSE
            UPDATE activity SET activity_state = '2' WHERE activity_num = activityNum;
            SET msg = CONCAT('活动「', activityName, '」已置为进行中');
            SET ok = 1;
        END IF;
    ELSEIF targetState = '3' THEN
        IF activityState NOT IN ('1', '2') THEN
            SET msg = '只有未开始或进行中的活动可以置为已结束';
        ELSE
            UPDATE activity SET activity_state = '3' WHERE activity_num = activityNum;
            SET msg = CONCAT('活动「', activityName, '」已置为已结束，可进行考勤结算');
            SET ok = 1;
        END IF;
    ELSE
        SET msg = '不支持变更到该活动状态';
    END IF;
END $$

-- -----------------------------------------------------------------------------
-- 服务时长补录：志愿者组织为未在平台签到的服务补录时长，提交平台管理员复核
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS organization_record_service_hours $$
CREATE PROCEDURE organization_record_service_hours(IN loginId VARCHAR(32),
                                                   IN participateNum INT,
                                                   IN beginTime DATETIME,
                                                   IN endTime DATETIME,
                                                   IN remark VARCHAR(200),
                                                   OUT msg VARCHAR(200),
                                                   OUT ok INT)
BEGIN
    DECLARE orgNum INT DEFAULT NULL;
    DECLARE ownerOrgNum INT DEFAULT NULL;
    DECLARE applyState VARCHAR(4) DEFAULT NULL;
    DECLARE isDeletedValue INT DEFAULT 0;
    DECLARE durationValue DOUBLE DEFAULT NULL;
    DECLARE overlapCount INT DEFAULT 0;
    DECLARE newCheckinNum INT DEFAULT NULL;
    DECLARE anomalyCode VARCHAR(32) DEFAULT NULL;
    DECLARE anomalyMessage VARCHAR(200) DEFAULT NULL;

    SET ok = 0;

    SELECT organization_num INTO orgNum FROM organization WHERE organization_id = loginId LIMIT 1;

    SELECT p.participate_applystate, p.participate_isdeleted, a.organization_num
    INTO applyState, isDeletedValue, ownerOrgNum
    FROM participate p
    JOIN activity a ON a.activity_num = p.activity_num
    WHERE p.participate_num = participateNum;

    IF ownerOrgNum IS NULL THEN
        SET msg = '未找到对应的报名记录';
    ELSEIF ownerOrgNum <> orgNum THEN
        SET msg = '只能为本组织活动中的志愿者补录服务时长';
    ELSEIF isDeletedValue <> 0 OR applyState <> '1' THEN
        SET msg = '只有报名已通过的志愿者可以补录服务时长';
    ELSEIF beginTime IS NULL OR endTime IS NULL OR endTime <= beginTime THEN
        SET msg = '服务开始时间与结束时间不正确，请重新填写';
    ELSEIF endTime > NOW() THEN
        SET msg = '不能补录尚未结束的服务时长';
    ELSE
        SET durationValue = ROUND(TIMESTAMPDIFF(SECOND, beginTime, endTime) / 3600, 2);

        IF durationValue <= 0 OR durationValue > 24 THEN
            SET msg = '单条补录的服务时长需大于 0 且不超过 24 小时';
        ELSE
            SELECT COUNT(*) INTO overlapCount
            FROM checkin
            WHERE participate_num = participateNum
              AND checkin_begintime < endTime
              AND IFNULL(checkin_endtime, NOW()) > beginTime;

            IF overlapCount > 0 THEN
                SET msg = '该时间段与志愿者已有的服务记录重叠，请核对后重新录入';
            ELSE
                INSERT INTO checkin(participate_num, checkin_begintime, checkin_endtime, checkin_duration,
                                    checkin_source, checkin_flag, checkin_remark)
                VALUES (participateNum, beginTime, endTime, durationValue, '2', '1', remark);
                SET newCheckinNum = LAST_INSERT_ID();

                -- 补录同样接受时长异常规则检测，命中规则的记录在管理员裁定前不计入累计时长
                CALL service_anomaly_check(newCheckinNum, anomalyCode, anomalyMessage);
                IF anomalyCode IS NOT NULL THEN
                    UPDATE checkin SET checkin_anomaly = anomalyCode WHERE checkin_num = newCheckinNum;
                    SET msg = CONCAT('已补录服务时长 ', durationValue, ' 小时，但', anomalyMessage,
                        '，需平台管理员裁定后计入累计时长');
                ELSE
                    SET msg = CONCAT('已补录服务时长 ', durationValue, ' 小时，待平台管理员复核后计入累计时长');
                END IF;
                SET ok = 1;
            END IF;
        END IF;
    END IF;
END $$

-- -----------------------------------------------------------------------------
-- 服务时长裁定：平台管理员复核志愿者组织补录的服务时长，以及命中异常规则的平台签到记录
--   补录记录由平台管理员复核（录入与复核分离）；平台签到记录命中时长异常规则时同样转平台管理员裁定
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS admin_check_checkin $$
CREATE PROCEDURE admin_check_checkin(IN loginId VARCHAR(32),
                                     IN checkinNum INT,
                                     IN isPass INT,
                                     IN remark VARCHAR(200),
                                     OUT msg VARCHAR(200),
                                     OUT ok INT)
BEGIN
    DECLARE adminCount INT DEFAULT 0;
    DECLARE sourceValue VARCHAR(4) DEFAULT NULL;
    DECLARE timecheckValue VARCHAR(4) DEFAULT NULL;
    DECLARE anomalyValue VARCHAR(32) DEFAULT NULL;
    DECLARE anomalyRemarkValue VARCHAR(200) DEFAULT NULL;
    DECLARE durationValue DOUBLE DEFAULT 0;
    DECLARE participateNumValue INT DEFAULT NULL;
    DECLARE volunteerNumValue INT DEFAULT NULL;
    DECLARE confirmedCount INT DEFAULT 0;

    SET ok = 0;

    SELECT COUNT(*) INTO adminCount FROM admin WHERE admin_id = loginId;

    SELECT checkin_source, checkin_timecheck, checkin_duration, participate_num,
           checkin_anomaly, checkin_anomalyremark
    INTO sourceValue, timecheckValue, durationValue, participateNumValue,
        anomalyValue, anomalyRemarkValue
    FROM checkin
    WHERE checkin_num = checkinNum;

    IF adminCount = 0 THEN
        SET msg = '未找到平台管理员信息，请重新登录后再试';
    ELSEIF participateNumValue IS NULL THEN
        SET msg = '未找到对应的服务时长记录';
    ELSEIF anomalyValue IS NULL AND sourceValue <> '2' THEN
        SET msg = '该记录为平台签到记录且未命中异常规则，请由志愿者组织复核';
    ELSEIF anomalyValue IS NOT NULL AND anomalyRemarkValue IS NOT NULL THEN
        SET msg = '该异常记录已裁定，无需重复处理';
    ELSEIF anomalyValue IS NULL AND timecheckValue IS NOT NULL THEN
        SET msg = '该服务时长已复核，无需重复处理';
    ELSE
        SELECT volunteer_num INTO volunteerNumValue
        FROM participate WHERE participate_num = participateNumValue;

        SELECT COUNT(*) INTO confirmedCount
        FROM checkin
        WHERE participate_num = participateNumValue
          AND checkin_timecheck = '1'
          AND checkin_num <> checkinNum;

        IF isPass = 1 THEN
            IF timecheckValue = '1' THEN
                -- 已计入的异常记录：裁定维持计入，仅记录裁定意见
                UPDATE checkin
                SET checkin_adminremark   = remark,
                    checkin_anomalyremark = remark
                WHERE checkin_num = checkinNum;
                SET msg = CONCAT('已裁定维持计入该服务时长 ', IFNULL(durationValue, 0), ' 小时');
            ELSE
                UPDATE checkin
                SET checkin_timecheck     = '1',
                    checkin_checktime     = NOW(),
                    checkin_adminremark   = remark,
                    checkin_anomalyremark = IF(anomalyValue IS NULL, checkin_anomalyremark, remark)
                WHERE checkin_num = checkinNum;

                UPDATE participate
                SET participate_timecheck = '1',
                    participate_duration  = IFNULL(participate_duration, 0) + IFNULL(durationValue, 0)
                WHERE participate_num = participateNumValue;

                UPDATE volunteer
                SET volunteer_totalduration = volunteer_totalduration + IFNULL(durationValue, 0),
                    volunteer_credit        = LEAST(120, volunteer_credit + 2)
                WHERE volunteer_num = volunteerNumValue;

                SET msg = CONCAT('已确认服务时长 ', IFNULL(durationValue, 0), ' 小时，并计入志愿者累计服务时长');
            END IF;
            SET ok = 1;
        ELSEIF timecheckValue = '1' THEN
            -- 已计入的异常记录被驳回：冲销已计入的时长与信用分
            UPDATE checkin
            SET checkin_timecheck     = '3',
                checkin_checktime     = NOW(),
                checkin_adminremark   = remark,
                checkin_anomalyremark = remark
            WHERE checkin_num = checkinNum;

            UPDATE participate
            SET participate_duration  = GREATEST(0, IFNULL(participate_duration, 0) - IFNULL(durationValue, 0)),
                participate_timecheck = IF(confirmedCount > 0, '1', '2')
            WHERE participate_num = participateNumValue;

            UPDATE volunteer
            SET volunteer_totalduration = GREATEST(0, volunteer_totalduration - IFNULL(durationValue, 0)),
                volunteer_credit        = GREATEST(0, volunteer_credit - 2)
            WHERE volunteer_num = volunteerNumValue;

            SET msg = CONCAT('已冲销该服务时长 ', IFNULL(durationValue, 0), ' 小时，并从累计服务时长中扣回');
            SET ok = 1;
        ELSE
            UPDATE checkin
            SET checkin_timecheck     = '2',
                checkin_checktime     = NOW(),
                checkin_adminremark   = remark,
                checkin_anomalyremark = IF(anomalyValue IS NULL, checkin_anomalyremark, remark)
            WHERE checkin_num = checkinNum;

            UPDATE participate
            SET participate_timecheck = IF(confirmedCount > 0, '1', '2')
            WHERE participate_num = participateNumValue;

            SET msg = '已驳回该服务时长记录，本次时长不计入累计服务时长';
            SET ok = 1;
        END IF;
    END IF;
END $$

-- -----------------------------------------------------------------------------
-- 移除报名：志愿者组织移除本组织活动名单中的报名，已通过的报名释放名额并递补
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS organization_remove_participate $$
CREATE PROCEDURE organization_remove_participate(IN loginId VARCHAR(32),
                                                 IN participateNum INT,
                                                 OUT msg VARCHAR(200),
                                                 OUT ok INT)
BEGIN
    DECLARE orgNum INT DEFAULT NULL;
    DECLARE ownerOrgNum INT DEFAULT NULL;
    DECLARE applyState VARCHAR(4) DEFAULT NULL;
    DECLARE isDeletedValue INT DEFAULT 0;
    DECLARE activityNumValue INT DEFAULT NULL;
    DECLARE confirmedCount INT DEFAULT 0;
    DECLARE nextParticipateNum INT DEFAULT NULL;

    SET ok = 0;

    SELECT organization_num INTO orgNum FROM organization WHERE organization_id = loginId LIMIT 1;

    SELECT p.participate_applystate, p.activity_num, p.participate_isdeleted, a.organization_num
    INTO applyState, activityNumValue, isDeletedValue, ownerOrgNum
    FROM participate p
    JOIN activity a ON a.activity_num = p.activity_num
    WHERE p.participate_num = participateNum;

    SELECT COUNT(*) INTO confirmedCount
    FROM checkin
    WHERE participate_num = participateNum
      AND checkin_timecheck = '1';

    IF ownerOrgNum IS NULL THEN
        SET msg = '未找到对应的报名记录';
    ELSEIF ownerOrgNum <> orgNum THEN
        SET msg = '只能管理本组织活动中的报名记录';
    ELSEIF isDeletedValue <> 0 THEN
        SET msg = '该报名已不在活动名单中';
    ELSEIF confirmedCount > 0 THEN
        SET msg = '该志愿者已有核定服务时长，不能移除报名';
    ELSE
        UPDATE participate
        SET participate_isdeleted = 1,
            participate_cancel_time = NOW()
        WHERE participate_num = participateNum;

        IF applyState = '1' THEN
            SELECT participate_num INTO nextParticipateNum
            FROM participate
            WHERE activity_num = activityNumValue
              AND participate_applystate = '3'
              AND participate_isdeleted = 0
            ORDER BY participate_applytime ASC, participate_num ASC
            LIMIT 1
            FOR UPDATE;

            IF nextParticipateNum IS NOT NULL THEN
                UPDATE participate SET participate_applystate = '0' WHERE participate_num = nextParticipateNum;
                SET msg = '已移除该报名并释放名额，候补志愿者已自动递补进入待审核';
            ELSE
                SET msg = '已移除该报名并释放名额';
            END IF;
        ELSE
            SET msg = '已移除该报名';
        END IF;
        SET ok = 1;
    END IF;
END $$

-- -----------------------------------------------------------------------------
-- 参加确认：志愿者确认参加或放弃已通过的活动报名，放弃时释放名额并递补
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS volunteer_confirm_participate $$
CREATE PROCEDURE volunteer_confirm_participate(IN participateNum INT,
                                               IN volunteerNum INT,
                                               IN confirmState INT,
                                               OUT msg VARCHAR(200),
                                               OUT ok INT)
BEGIN
    DECLARE applyState VARCHAR(4) DEFAULT NULL;
    DECLARE ownerVolunteerNum INT DEFAULT NULL;
    DECLARE activityNumValue INT DEFAULT NULL;
    DECLARE confirmStateValue VARCHAR(4) DEFAULT NULL;
    DECLARE nextParticipateNum INT DEFAULT NULL;

    SET ok = 0;

    SELECT participate_applystate, volunteer_num, activity_num, participate_confirmstate
    INTO applyState, ownerVolunteerNum, activityNumValue, confirmStateValue
    FROM participate
    WHERE participate_num = participateNum
      AND participate_isdeleted = 0;

    IF ownerVolunteerNum IS NULL THEN
        SET msg = '未找到对应的报名记录';
    ELSEIF ownerVolunteerNum <> volunteerNum THEN
        SET msg = '只能确认本人的报名记录';
    ELSEIF applyState <> '1' THEN
        SET msg = '只有报名已通过的记录可以确认参加';
    ELSEIF confirmState = 1 THEN
        IF confirmStateValue = '1' THEN
            SET msg = '您已确认参加，请按时到场';
        ELSE
            UPDATE participate
            SET participate_confirmstate = '1',
                participate_confirmtime  = NOW()
            WHERE participate_num = participateNum;
            SET msg = '已确认参加，请按活动时间到场并在现场完成签到';
        END IF;
        SET ok = 1;
    ELSEIF confirmState = 2 THEN
        UPDATE participate
        SET participate_isdeleted    = 1,
            participate_cancel_time  = NOW(),
            participate_confirmstate = '2',
            participate_confirmtime  = NOW()
        WHERE participate_num = participateNum;

        SELECT participate_num INTO nextParticipateNum
        FROM participate
        WHERE activity_num = activityNumValue
          AND participate_applystate = '3'
          AND participate_isdeleted = 0
        ORDER BY participate_applytime ASC, participate_num ASC
        LIMIT 1
        FOR UPDATE;

        IF nextParticipateNum IS NOT NULL THEN
            UPDATE participate SET participate_applystate = '0' WHERE participate_num = nextParticipateNum;
            SET msg = '已放弃参加并释放名额，候补志愿者已自动递补';
        ELSE
            SET msg = '已放弃参加并释放名额';
        END IF;
        SET ok = 1;
    ELSE
        SET msg = '参加确认状态不正确';
    END IF;
END $$

-- -----------------------------------------------------------------------------
-- 参加确认到期：活动开始前 24 小时仍未确认的报名自动释放名额并递补候补志愿者
--   报名通过后至少保留 12 小时确认时间，避免临时发布的活动把已报名志愿者直接判定为放弃
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS participate_confirm_expire $$
CREATE PROCEDURE participate_confirm_expire(OUT releasedCount INT)
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE curParticipateNum INT DEFAULT NULL;
    DECLARE curActivityNum INT DEFAULT NULL;
    DECLARE nextParticipateNum INT DEFAULT NULL;
    DECLARE expireCursor CURSOR FOR
        SELECT p.participate_num, p.activity_num
        FROM participate p
        JOIN activity a ON a.activity_num = p.activity_num
        WHERE p.participate_applystate = '1'
          AND p.participate_confirmstate IS NULL
          AND p.participate_isdeleted = 0
          AND a.activity_isdeleted = 0
          AND a.activity_state IN ('1', '2')
          AND a.activity_begintime IS NOT NULL
          AND NOW() < a.activity_begintime
          AND NOW() >= DATE_SUB(a.activity_begintime, INTERVAL 24 HOUR)
          AND NOW() >= DATE_ADD(IFNULL(p.participate_applychecktime, p.participate_applytime), INTERVAL 12 HOUR);
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    SET releasedCount = 0;

    OPEN expireCursor;
    expire_loop:
    LOOP
        FETCH expireCursor INTO curParticipateNum, curActivityNum;
        IF done = 1 THEN
            LEAVE expire_loop;
        END IF;

        IF EXISTS (SELECT 1
                   FROM participate
                   WHERE participate_num = curParticipateNum
                     AND participate_isdeleted = 0
                     AND participate_confirmstate IS NULL) THEN

            UPDATE participate
            SET participate_isdeleted   = 1,
                participate_cancel_time = NOW()
            WHERE participate_num = curParticipateNum;

            SET nextParticipateNum = NULL;
            SELECT participate_num INTO nextParticipateNum
            FROM participate
            WHERE activity_num = curActivityNum
              AND participate_applystate = '3'
              AND participate_isdeleted = 0
            ORDER BY participate_applytime ASC, participate_num ASC
            LIMIT 1
            FOR UPDATE;

            IF nextParticipateNum IS NOT NULL THEN
                UPDATE participate SET participate_applystate = '0' WHERE participate_num = nextParticipateNum;
            END IF;

            SET releasedCount = releasedCount + 1;
        END IF;
    END LOOP;
    CLOSE expireCursor;
END $$

-- -----------------------------------------------------------------------------
-- 开通组织账号：平台管理员创建志愿者组织账号，同时写入组织信息与登录账号
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS admin_create_organization $$
CREATE PROCEDURE admin_create_organization(IN loginId VARCHAR(32),
                                           IN organizationId VARCHAR(32),
                                           IN organizationName VARCHAR(100),
                                           IN organizationPassword VARCHAR(200),
                                           IN organizationEstablishdate DATE,
                                           IN organizationIntroduction TEXT,
                                           OUT msg VARCHAR(200),
                                           OUT ok INT)
BEGIN
    DECLARE adminCount INT DEFAULT 0;
    DECLARE duplicateCount INT DEFAULT 0;

    SET ok = 0;

    SELECT COUNT(*) INTO adminCount FROM admin WHERE admin_id = loginId;
    SELECT COUNT(*) INTO duplicateCount FROM e_user WHERE id = TRIM(organizationId);

    IF duplicateCount = 0 THEN
        SELECT COUNT(*) INTO duplicateCount FROM organization WHERE organization_id = TRIM(organizationId);
    END IF;

    IF adminCount = 0 THEN
        SET msg = '未找到平台管理员信息，请重新登录后再试';
    ELSEIF organizationId IS NULL OR TRIM(organizationId) = ''
        OR organizationName IS NULL OR TRIM(organizationName) = '' THEN
        SET msg = '组织登录账号与组织名称不能为空';
    ELSEIF duplicateCount > 0 THEN
        SET msg = '该组织登录账号已存在，请更换登录账号';
    ELSE
        INSERT INTO organization(organization_id, organization_name, organization_establishdate,
                                 organization_introduction, organization_isdeleted)
        VALUES (TRIM(organizationId), TRIM(organizationName), organizationEstablishdate,
                organizationIntroduction, 0);
        INSERT INTO e_user(id, password, role, enabled)
        VALUES (TRIM(organizationId), organizationPassword, 'ROLE_ORGANIZATION', 1);
        SET msg = CONCAT('志愿者组织「', TRIM(organizationName), '」账号已开通');
        SET ok = 1;
    END IF;
END $$

-- -----------------------------------------------------------------------------
-- 账号启停：平台管理员停用或启用组织账号与志愿者账号
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS admin_set_account_enabled $$
CREATE PROCEDURE admin_set_account_enabled(IN loginId VARCHAR(32),
                                           IN accountId VARCHAR(32),
                                           IN enabledValue INT,
                                           OUT msg VARCHAR(200),
                                           OUT ok INT)
BEGIN
    DECLARE adminCount INT DEFAULT 0;
    DECLARE accountRole VARCHAR(64) DEFAULT NULL;

    SET ok = 0;

    SELECT COUNT(*) INTO adminCount FROM admin WHERE admin_id = loginId;
    SELECT role INTO accountRole FROM e_user WHERE id = accountId;

    IF adminCount = 0 THEN
        SET msg = '未找到平台管理员信息，请重新登录后再试';
    ELSEIF accountRole IS NULL THEN
        SET msg = '未找到对应的登录账号';
    ELSEIF accountId = loginId THEN
        SET msg = '不能停用当前登录的管理员账号';
    ELSEIF accountRole = 'ROLE_ADMIN' THEN
        SET msg = '平台管理员账号不支持在此停用';
    ELSEIF enabledValue = 1 THEN
        UPDATE e_user SET enabled = 1 WHERE id = accountId;
        IF accountRole = 'ROLE_ORGANIZATION' THEN
            UPDATE organization SET organization_isdeleted = 0 WHERE organization_id = accountId;
        ELSEIF accountRole = 'ROLE_VOLUNTEER' THEN
            UPDATE volunteer SET volunteer_isdeleted = 0 WHERE volunteer_id = accountId;
        END IF;
        SET msg = '账号已启用';
        SET ok = 1;
    ELSE
        UPDATE e_user SET enabled = 0 WHERE id = accountId;
        IF accountRole = 'ROLE_ORGANIZATION' THEN
            UPDATE organization SET organization_isdeleted = 1 WHERE organization_id = accountId;
        ELSEIF accountRole = 'ROLE_VOLUNTEER' THEN
            UPDATE volunteer SET volunteer_isdeleted = 1 WHERE volunteer_id = accountId;
        END IF;
        SET msg = '账号已停用，停用后该账号无法登录平台';
        SET ok = 1;
    END IF;
END $$

-- -----------------------------------------------------------------------------
-- 志愿秀发布：志愿者分享志愿服务经历，可关联本人已通过报名的活动
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS volunteer_publish_show $$
CREATE PROCEDURE volunteer_publish_show(IN volunteerNum INT,
                                        IN activityNum INT,
                                        IN showDetail TEXT,
                                        OUT showNum INT,
                                        OUT msg VARCHAR(200),
                                        OUT ok INT)
BEGIN
    DECLARE newNum INT DEFAULT 0;
    DECLARE joinedCount INT DEFAULT 0;

    SET ok = 0;
    SET showNum = NULL;

    IF activityNum IS NOT NULL AND activityNum > 0 THEN
        SELECT COUNT(*) INTO joinedCount
        FROM participate
        WHERE activity_num = activityNum
          AND volunteer_num = volunteerNum
          AND participate_applystate = '1'
          AND participate_isdeleted = 0;
    END IF;

    IF showDetail IS NULL OR TRIM(showDetail) = '' THEN
        SET msg = '请填写志愿服务分享内容';
    ELSEIF activityNum IS NOT NULL AND activityNum > 0 AND joinedCount = 0 THEN
        SET msg = '只能关联本人已通过报名的志愿活动';
    ELSE
        INSERT INTO `show`(show_id, show_detail, show_sharetime, volunteer_num, activity_num,
                           show_browse, show_like, show_isdeleted)
        VALUES ('', TRIM(showDetail), NOW(), volunteerNum,
                IF(activityNum IS NULL OR activityNum = 0, NULL, activityNum), 0, 0, 0);
        SET newNum = LAST_INSERT_ID();
        UPDATE `show` SET show_id = CONCAT('sho_', LPAD(newNum, 5, '0')) WHERE show_num = newNum;
        SET showNum = newNum;
        SET msg = '志愿秀已发布，可在志愿秀广场查看';
        SET ok = 1;
    END IF;
END $$

-- -----------------------------------------------------------------------------
-- 现场签到码派生函数：由活动签到密钥、活动编号与时间窗口序号派生短时轮换的签到码
--   每 120 秒为一个时间窗口，签到码为 8 位字符（字符集剔除易混淆的 0、1、I、O）
-- -----------------------------------------------------------------------------
DROP FUNCTION IF EXISTS activity_checkin_code $$
CREATE FUNCTION activity_checkin_code(secret VARCHAR(32), activityNum INT, rotationIndex BIGINT)
    RETURNS VARCHAR(16)
    DETERMINISTIC
BEGIN
    DECLARE hashValue VARCHAR(64) DEFAULT '';
    DECLARE checkinCode VARCHAR(16) DEFAULT '';
    DECLARE position INT DEFAULT 1;
    DECLARE nibble CHAR(1) DEFAULT '';
    DECLARE alphabet VARCHAR(16) DEFAULT '23456789ABCDEFGH';

    SET hashValue = UPPER(SHA2(CONCAT(secret, '#', activityNum, '#', rotationIndex), 256));

    WHILE position <= 8 DO
        SET nibble = SUBSTRING(hashValue, position, 1);
        SET checkinCode = CONCAT(checkinCode, SUBSTRING(alphabet, CONV(nibble, 16, 10) + 1, 1));
        SET position = position + 1;
    END WHILE;

    RETURN checkinCode;
END $$

-- -----------------------------------------------------------------------------
-- 时长异常规则检测：按异常规则表的阈值判断一条服务记录是否命中异常规则
--   命中时返回规则编码与说明，由调用方决定是否计入累计时长（当前实现为转平台管理员裁定）
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS service_anomaly_check $$
CREATE PROCEDURE service_anomaly_check(IN checkinNum INT,
                                       OUT anomalyCode VARCHAR(32),
                                       OUT msg VARCHAR(200))
BEGIN
    DECLARE durationValue DOUBLE DEFAULT 0;
    DECLARE beginValue DATETIME DEFAULT NULL;
    DECLARE volunteerNumValue INT DEFAULT NULL;
    DECLARE serviceDate DATE DEFAULT NULL;
    DECLARE singleRecordLimit DECIMAL(6, 2) DEFAULT 12;
    DECLARE singleDayLimit DECIMAL(6, 2) DEFAULT 12;
    DECLARE nightLimit DECIMAL(6, 2) DEFAULT 5;
    DECLARE manualBatchLimit DECIMAL(6, 2) DEFAULT 3;
    DECLARE dayTotal DOUBLE DEFAULT 0;
    DECLARE manualCount INT DEFAULT 0;

    SET anomalyCode = NULL;
    SET msg = NULL;

    SELECT c.checkin_duration, c.checkin_begintime, p.volunteer_num
    INTO durationValue, beginValue, volunteerNumValue
    FROM checkin c
    JOIN participate p ON p.participate_num = c.participate_num
    WHERE c.checkin_num = checkinNum;

    -- 规则阈值由异常规则表配置，未配置或已停用时沿用默认阈值
    SELECT rule_threshold INTO singleRecordLimit
    FROM anomaly_rule WHERE rule_code = 'SINGLE_RECORD' AND rule_enabled = 1 LIMIT 1;
    SELECT rule_threshold INTO singleDayLimit
    FROM anomaly_rule WHERE rule_code = 'SINGLE_DAY_TOTAL' AND rule_enabled = 1 LIMIT 1;
    SELECT rule_threshold INTO nightLimit
    FROM anomaly_rule WHERE rule_code = 'NIGHT_SERVICE' AND rule_enabled = 1 LIMIT 1;
    SELECT rule_threshold INTO manualBatchLimit
    FROM anomaly_rule WHERE rule_code = 'MANUAL_BATCH' AND rule_enabled = 1 LIMIT 1;

    IF durationValue IS NULL OR beginValue IS NULL OR volunteerNumValue IS NULL THEN
        SET msg = NULL;
    ELSE
        SET serviceDate = DATE(beginValue);

        -- 单日已计入时长（不含本记录）+ 本记录时长
        SELECT IFNULL(SUM(c.checkin_duration), 0) INTO dayTotal
        FROM checkin c
        JOIN participate p ON p.participate_num = c.participate_num
        WHERE p.volunteer_num = volunteerNumValue
          AND c.checkin_timecheck = '1'
          AND c.checkin_num <> checkinNum
          AND DATE(c.checkin_begintime) = serviceDate;

        SELECT COUNT(*) INTO manualCount
        FROM checkin c
        JOIN participate p ON p.participate_num = c.participate_num
        WHERE p.activity_num = (SELECT activity_num FROM participate WHERE participate_num =
                (SELECT participate_num FROM checkin WHERE checkin_num = checkinNum))
          AND p.volunteer_num = volunteerNumValue
          AND c.checkin_source = '2';

        IF durationValue > singleRecordLimit THEN
            SET anomalyCode = 'SINGLE_RECORD';
            SET msg = CONCAT('单条服务时长 ', durationValue, ' 小时超过上限 ', singleRecordLimit, ' 小时');
        ELSEIF DAYOFMONTH(beginValue) = DAYOFMONTH(beginValue)
            AND HOUR(beginValue) < nightLimit THEN
            SET anomalyCode = 'NIGHT_SERVICE';
            SET msg = CONCAT('服务开始于 ', DATE_FORMAT(beginValue, '%H:%i'),
                '，属于夜间时段（0 时至 ', nightLimit, ' 时），需人工核对');
        ELSEIF dayTotal + durationValue > singleDayLimit THEN
            SET anomalyCode = 'SINGLE_DAY_TOTAL';
            SET msg = CONCAT('当日已计入 ', ROUND(dayTotal, 2), ' 小时，叠加本次将达到 ',
                ROUND(dayTotal + durationValue, 2), ' 小时，超过单日上限 ', singleDayLimit, ' 小时');
        ELSEIF manualCount >= manualBatchLimit THEN
            SET anomalyCode = 'MANUAL_BATCH';
            SET msg = CONCAT('该志愿者在本活动已有 ', manualCount, ' 条补录记录，达到补录条数上限 ',
                manualBatchLimit, ' 条，需人工核对');
        END IF;
    END IF;
END $$

-- -----------------------------------------------------------------------------
-- 服务确认单：志愿者组织为一条已确认的服务记录生成服务确认码，交给服务对象核对
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS organization_issue_confirm_sheet $$
CREATE PROCEDURE organization_issue_confirm_sheet(IN loginId VARCHAR(32),
                                                  IN checkinNum INT,
                                                  IN objectName VARCHAR(50),
                                                  IN objectPhone VARCHAR(20),
                                                  OUT confirmCode VARCHAR(16),
                                                  OUT msg VARCHAR(200),
                                                  OUT ok INT)
BEGIN
    DECLARE orgNum INT DEFAULT NULL;
    DECLARE ownerOrgNum INT DEFAULT NULL;
    DECLARE endValue DATETIME DEFAULT NULL;
    DECLARE objectConfirmValue VARCHAR(4) DEFAULT NULL;
    DECLARE newCode VARCHAR(16) DEFAULT NULL;

    SET ok = 0;
    SET confirmCode = NULL;

    SELECT organization_num INTO orgNum FROM organization WHERE organization_id = loginId LIMIT 1;

    SELECT a.organization_num, c.checkin_endtime, c.checkin_objectconfirm
    INTO ownerOrgNum, endValue, objectConfirmValue
    FROM checkin c
    JOIN participate p ON p.participate_num = c.participate_num
    JOIN activity a ON a.activity_num = p.activity_num
    WHERE c.checkin_num = checkinNum;

    IF ownerOrgNum IS NULL THEN
        SET msg = '未找到对应的服务记录';
    ELSEIF ownerOrgNum <> orgNum THEN
        SET msg = '只能为本组织活动中的服务记录生成服务确认单';
    ELSEIF endValue IS NULL THEN
        SET msg = '该服务尚未结束，暂不能生成服务确认单';
    ELSEIF objectConfirmValue IS NOT NULL THEN
        SET msg = '该记录已完成服务对象确认，无需重复生成';
    ELSEIF objectName IS NULL OR TRIM(objectName) = ''
        OR objectPhone IS NULL OR LENGTH(TRIM(objectPhone)) < 7 THEN
        SET msg = '请填写服务对象名称与有效手机号，用于服务对象确认时校验';
    ELSE
        SET newCode = UPPER(SUBSTRING(SHA2(CONCAT(checkinNum, RAND(), NOW()), 256), 1, 8));
        UPDATE checkin
        SET checkin_objectname    = TRIM(objectName),
            checkin_objectphone   = TRIM(objectPhone),
            checkin_confirmcode   = newCode,
            checkin_objectconfirm = NULL
        WHERE checkin_num = checkinNum;

        SET confirmCode = newCode;
        SET msg = CONCAT('服务确认单已生成，请将确认码交给服务对象「', TRIM(objectName),
            '」，由其本人核对手机号后确认或否认本次服务');
        SET ok = 1;
    END IF;
END $$

-- -----------------------------------------------------------------------------
-- 服务对象确认：服务对象使用确认码与本人手机号确认或否认本次服务（无需登录）
--   确认为正向补强；否认时若该时长已计入累计时长，平台立即冲销并通知志愿者组织核实
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS service_object_confirm $$
CREATE PROCEDURE service_object_confirm(IN inputCode VARCHAR(16),
                                        IN inputPhone VARCHAR(20),
                                        IN resultValue INT,
                                        IN remark VARCHAR(200),
                                        OUT msg VARCHAR(200),
                                        OUT ok INT)
BEGIN
    DECLARE checkinNumValue INT DEFAULT NULL;
    DECLARE objectPhoneValue VARCHAR(20) DEFAULT NULL;
    DECLARE durationValue DOUBLE DEFAULT 0;
    DECLARE timecheckValue VARCHAR(4) DEFAULT NULL;
    DECLARE participateNumValue INT DEFAULT NULL;
    DECLARE volunteerNumValue INT DEFAULT NULL;
    DECLARE confirmedCount INT DEFAULT 0;

    SET ok = 0;

    SELECT checkin_num, checkin_objectphone, checkin_duration, checkin_timecheck, participate_num
    INTO checkinNumValue, objectPhoneValue, durationValue, timecheckValue, participateNumValue
    FROM checkin
    WHERE checkin_confirmcode = UPPER(TRIM(inputCode));

    IF checkinNumValue IS NULL THEN
        SET msg = '确认码不存在或已失效，请与服务组织核对';
    ELSEIF objectPhoneValue IS NULL OR TRIM(inputPhone) <> objectPhoneValue THEN
        SET msg = '手机号与预留号码不一致，无法确认本次服务';
    ELSEIF timecheckValue = '2' OR timecheckValue = '3' THEN
        SET msg = '该服务记录已处理完毕，无需再次确认';
    ELSE
        SELECT p.volunteer_num INTO volunteerNumValue
        FROM participate p WHERE p.participate_num = participateNumValue;

        IF resultValue = 1 THEN
            UPDATE checkin
            SET checkin_objectconfirm     = '1',
                checkin_objectconfirmtime = NOW(),
                checkin_objectremark      = remark
            WHERE checkin_num = checkinNumValue;
            SET msg = '感谢您的确认，本次服务已由服务对象确认';
            SET ok = 1;
        ELSEIF resultValue = 2 THEN
            UPDATE checkin
            SET checkin_objectconfirm     = '2',
                checkin_objectconfirmtime = NOW(),
                checkin_objectremark      = remark
            WHERE checkin_num = checkinNumValue;

            IF timecheckValue = '1' THEN
                -- 已计入累计时长的记录被否认时立即冲销，冲销只执行一次
                UPDATE checkin SET checkin_timecheck = '3' WHERE checkin_num = checkinNumValue;

                SELECT COUNT(*) INTO confirmedCount
                FROM checkin
                WHERE participate_num = participateNumValue
                  AND checkin_timecheck = '1'
                  AND checkin_num <> checkinNumValue;

                UPDATE participate
                SET participate_duration  = GREATEST(0, IFNULL(participate_duration, 0) - IFNULL(durationValue, 0)),
                    participate_timecheck = IF(confirmedCount > 0, '1', '2')
                WHERE participate_num = participateNumValue;

                UPDATE volunteer
                SET volunteer_totalduration = GREATEST(0, volunteer_totalduration - IFNULL(durationValue, 0)),
                    volunteer_credit        = GREATEST(0, volunteer_credit - 2)
                WHERE volunteer_num = volunteerNumValue;

                SET msg = CONCAT('已记录您的反馈，本次服务时长 ', IFNULL(durationValue, 0),
                    ' 小时已冲销，平台将通知志愿者组织核实');
            ELSE
                SET msg = '已记录您的反馈，本次服务不会被计入服务时长';
            END IF;
            SET ok = 1;
        ELSE
            SET msg = '请选择确认或否认本次服务';
        END IF;
    END IF;
END $$

-- -----------------------------------------------------------------------------
-- 异常时长巡检：定时标记单日已计入时长超过上限的服务记录，供平台管理员裁定
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS service_anomaly_scan $$
CREATE PROCEDURE service_anomaly_scan(OUT flaggedCount INT)
BEGIN
    DECLARE singleDayLimit DECIMAL(6, 2) DEFAULT 12;

    SET flaggedCount = 0;

    SELECT rule_threshold INTO singleDayLimit
    FROM anomaly_rule WHERE rule_code = 'SINGLE_DAY_TOTAL' AND rule_enabled = 1 LIMIT 1;

    UPDATE checkin c
        JOIN participate p ON p.participate_num = c.participate_num
        JOIN (SELECT p2.volunteer_num AS volunteer_num,
                     DATE(c2.checkin_begintime) AS service_date,
                     SUM(c2.checkin_duration) AS day_total
              FROM checkin c2
              JOIN participate p2 ON p2.participate_num = c2.participate_num
              WHERE c2.checkin_timecheck = '1'
              GROUP BY p2.volunteer_num, DATE(c2.checkin_begintime)) t
             ON t.volunteer_num = p.volunteer_num
                 AND t.service_date = DATE(c.checkin_begintime)
    SET c.checkin_anomaly = 'SINGLE_DAY_TOTAL'
    WHERE c.checkin_timecheck = '1'
      AND c.checkin_anomaly IS NULL
      AND t.day_total > singleDayLimit;

    SET flaggedCount = ROW_COUNT();
END $$

DELIMITER ;

-- =============================================================================
-- 初始化数据
-- 平台管理员账号：admin_001，初始密码：123456
-- 志愿者组织账号：org_001、org_002，初始密码：123456
-- 志愿者账号：vol_00001 ~ vol_00004，初始密码：123456
-- =============================================================================

INSERT INTO admin(admin_id, admin_name)
VALUES ('admin_001', '王敏');

INSERT INTO organization(organization_id, organization_name,
                         organization_establishdate, organization_introduction, organization_isdeleted)
VALUES ('org_001', '阳光青年志愿者协会',
        '2019-03-05', '阳光青年志愿者协会成立于2019年，长期开展助学、助老、环保等志愿服务活动。', 0),
       ('org_002', '城市公益服务中心',
        '2020-06-18', '城市公益服务中心面向社区居民开展公益服务与志愿服务活动。', 0);

INSERT INTO e_user(id, password, role)
VALUES ('admin_001', 'e10adc3949ba59abbe56e057f20f883e', 'ROLE_ADMIN'),
       ('org_001', 'e10adc3949ba59abbe56e057f20f883e', 'ROLE_ORGANIZATION'),
       ('org_002', 'e10adc3949ba59abbe56e057f20f883e', 'ROLE_ORGANIZATION'),
       ('vol_00001', 'e10adc3949ba59abbe56e057f20f883e', 'ROLE_VOLUNTEER'),
       ('vol_00002', 'e10adc3949ba59abbe56e057f20f883e', 'ROLE_VOLUNTEER'),
       ('vol_00003', 'e10adc3949ba59abbe56e057f20f883e', 'ROLE_VOLUNTEER'),
       ('vol_00004', 'e10adc3949ba59abbe56e057f20f883e', 'ROLE_VOLUNTEER');

INSERT INTO volunteer(volunteer_id, volunteer_name, volunteer_registerdate,
                      volunteer_gender, volunteer_birth, volunteer_tel,
                      volunteer_latitude, volunteer_longitude, volunteer_credit,
                      volunteer_noshow, volunteer_totalduration)
VALUES ('vol_00001', '张三', '2023-02-11', '男', '2001-05-20', '13800000001',
        31.2304000, 121.4737000, 102, 0, 3.0),
       ('vol_00002', '李四', '2023-03-02', '女', '2002-09-14', '13800000002',
        31.2500000, 121.5000000, 92, 1, 3.0),
       ('vol_00003', '王五', '2023-03-18', '男', '2000-12-01', '13800000003',
        NULL, NULL, 100, 0, 0),
       ('vol_00004', '赵六', '2023-04-06', '女', '2001-07-30', '13800000004',
        31.2320000, 121.4750000, 102, 0, 3.0);

INSERT INTO volunteer_skill(volunteer_num, skill_name)
VALUES (1, '助学支教'), (1, '环保公益'), (1, '社区服务'),
       (2, '助老服务'), (2, '医疗健康'),
       (3, '信息技术'), (3, '大型赛会'),
       (4, '社区服务'), (4, '文化宣传');

INSERT INTO activity(organization_num, activity_id, activity_name, activity_detail,
                     activity_begintime, activity_endtime, activity_location, activity_needpeople,
                     activity_notice, activity_publishtime, activity_signddl, activity_state,
                     admin_num, activity_checkin, activity_checkin_secret,
                     activity_latitude, activity_longitude, activity_radius,
                     activity_checktime, activity_remark, activity_reason_code,
                     activity_revision, activity_settle_time, activity_isdeleted)
VALUES (1, 'act_00001', '社区爱心助学志愿活动',
        '为社区困难家庭学生提供课业辅导与阅读陪伴，每周六上午开展一次。',
        '2023-09-02 09:00:00', '2023-09-02 11:30:00', '阳光社区活动中心', 12,
        '请提前十分钟到场签到，佩戴志愿者证。', '2023-08-10 10:20:00', '2023-08-30 18:00:00', '0',
        NULL, '现场签到', NULL, 31.2304000, 121.4737000, 300, NULL, NULL, NULL, 0, NULL, 0),
       (1, 'act_00002', '敬老院陪伴志愿服务',
        '到敬老院陪伴老人聊天、整理房间并开展文艺表演。',
        '2023-09-16 14:00:00', '2023-09-16 17:00:00', '幸福敬老院', 8,
        '注意与老人沟通的语气，服从现场负责人安排。', '2023-08-12 09:00:00', '2023-09-12 18:00:00', '0',
        NULL, '现场签到', NULL, NULL, NULL, 300, NULL, NULL, NULL, 0, NULL, 0),
       (1, 'act_00003', '城市环保徒步宣传',
        '沿城市公园步道开展垃圾分类宣传与沿途垃圾清理。',
        '2023-10-14 08:30:00', '2023-10-14 11:30:00', '城市中心公园南门', 20,
        '请穿着运动鞋，自备饮用水。', '2023-08-20 15:00:00', '2023-10-08 18:00:00', '1',
        1, '现场签到', '9F2C41A7D3E85B60C1A94F7D2E638B51', 31.2304000, 121.4737000, 300, '2023-08-21 10:00:00', '活动方案完整，审核通过', NULL, 0, NULL, 0),
       (1, 'act_00004', '图书馆图书整理志愿服务',
        '协助图书馆整理书架、修补图书并引导读者检索。',
        '2023-08-05 09:00:00', '2023-08-05 12:00:00', '市图书馆三楼', 6,
        '保持安静，服从图书管理员安排。', '2023-07-20 11:00:00', '2023-08-01 18:00:00', '3',
        1, '现场签到', '3D7A18C4F92B60E51A7C3D8F49B216E7', 31.2400000, 121.4800000, 300, '2023-07-21 09:30:00', '审核通过', NULL, 0, '2023-08-06 10:30:00', 0),
       (1, 'act_00005', '暑期乡村支教志愿活动',
        '到乡村小学开展暑期支教活动，内容包含兴趣课程与安全教育。',
        '2023-07-10 09:00:00', '2023-07-20 17:00:00', '青山乡中心小学', 15,
        '需连续参与，请提前安排时间。', '2023-06-25 10:00:00', '2023-07-05 18:00:00', '4',
        1, '现场签到', NULL, NULL, NULL, 300, '2023-06-26 14:00:00', '活动周期与人员安排不明确，请完善后重新申报', 'PEOPLE_UNCLEAR', 0, NULL, 0),
       (2, 'act_00006', '社区义诊志愿服务',
        '协助社区医院开展义诊活动，负责引导与登记工作。',
        '2023-09-09 08:30:00', '2023-09-09 12:00:00', '和平社区广场', 10,
        '请服从医护人员安排。', '2023-08-15 10:00:00', '2023-09-05 18:00:00', '0',
        NULL, '现场签到', NULL, NULL, NULL, 300, NULL, NULL, NULL, 0, NULL, 0),
       (1, 'act_00007', '社区图书漂流志愿活动',
        '整理社区图书漂流角、登记图书流转信息并引导居民参与图书交换。',
        '2026-10-18 09:00:00', '2026-10-18 12:00:00', '阳光社区活动中心', 1,
        '请提前十分钟到场，携带本人志愿者编号。', '2026-09-15 10:00:00', '2026-10-15 18:00:00', '1',
        1, '现场签到', 'C41F9A72D3B865E01F4A7C29D8B36E15', 31.2304000, 121.4737000, 300, '2026-09-16 09:00:00', '活动方案完整，审核通过', NULL, 0, NULL, 0),
       (1, 'act_00008', '城市应急救护知识宣传',
        '在公园向市民演示心肺复苏与止血包扎，发放应急救护手册。',
        '2026-10-25 09:00:00', '2026-10-25 16:00:00', '城市中心公园北门', 10,
        '服务时长较长，请自备饮用水与防晒用品。', '2026-09-18 14:00:00', '2026-10-20 18:00:00', '1',
        1, '现场签到', '7E1B93D5A2C64F80B3D7E19C4A28F65D', 31.2360000, 121.4800000, 300, '2026-09-19 10:30:00', '审核通过', NULL, 0, NULL, 0),
       (2, 'act_00009', '敬老助餐志愿服务',
        '在社区食堂协助分餐、送餐并陪伴独居老人用餐。',
        '2026-11-01 08:30:00', '2026-11-01 12:00:00', '和平社区广场', 8,
        '请穿着便于活动的服装，注意食品卫生。', '2026-09-20 09:00:00', '2026-10-28 18:00:00', '1',
        1, '现场签到', 'A6C2E84F1B973D50E2A6C4F89B1D7350', 31.3000000, 121.6000000, 500, '2026-09-20 15:00:00', '审核通过', NULL, 0, NULL, 0);

-- 为未单独指定签到密钥的历史活动补充随机密钥，保证每个活动都能派生轮换签到码
UPDATE activity
SET activity_checkin_secret = UPPER(SUBSTRING(SHA2(CONCAT(activity_num, RAND(), NOW(), UUID()), 256), 1, 32))
WHERE activity_checkin_secret IS NULL;

INSERT INTO activity_skill(activity_num, skill_name)
VALUES (1, '助学支教'),
       (2, '助老服务'),
       (3, '环保公益'),
       (4, '社区服务'),
       (6, '医疗健康'),
       (7, '社区服务'), (7, '文化宣传'),
       (8, '应急救援'), (8, '医疗健康'),
       (9, '助老服务'), (9, '社区服务');

INSERT INTO participate(volunteer_num, activity_num, participate_applytime, participate_applystate,
                        participate_applychecktime, participate_training, participate_begintime,
                        participate_endtime, participate_duration, participate_timecheck,
                        participate_noshow, participate_cancel_time,
                        participate_confirmstate, participate_confirmtime, participate_isdeleted)
VALUES (1, 3, '2023-08-25 09:10:00', '1', '2023-08-26 10:00:00', '已完成岗前培训',
        '2023-10-14 08:25:00', '2023-10-14 11:35:00', 3.0, '1', 0, NULL, '1', '2023-08-26 10:30:00', 0),
       (2, 3, '2023-08-26 14:20:00', '1', '2023-08-27 09:30:00', '已完成岗前培训',
        '2023-10-14 08:28:00', '2023-10-14 11:32:00', 3.0, NULL, 0, NULL, '1', '2023-08-27 10:00:00', 0),
       (3, 3, '2023-08-28 19:05:00', '0', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, 0),
       (4, 4, '2023-07-25 10:00:00', '1', '2023-07-26 11:00:00', '已完成岗前培训',
        '2023-08-05 09:02:00', '2023-08-05 12:05:00', 3.0, '1', 0, NULL, '1', '2023-07-26 11:30:00', 0),
       (3, 7, '2026-09-20 10:00:00', '3', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, 0),
       (4, 7, '2026-09-19 09:00:00', '1', '2026-09-19 15:00:00', '已完成岗前培训',
        NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, 0),
       (2, 4, '2023-07-25 15:30:00', '1', '2023-07-26 11:00:00', '已完成岗前培训',
        NULL, NULL, NULL, NULL, 1, NULL, NULL, NULL, 0);

INSERT INTO checkin(participate_num, checkin_begintime, checkin_endtime, checkin_duration, checkin_timecheck,
                    checkin_code, checkin_latitude, checkin_longitude, checkin_distance, checkin_outdistance,
                    checkin_flag, checkin_remark, checkin_checktime, checkin_source, checkin_adminremark)
VALUES (1, '2023-10-14 08:25:00', '2023-10-14 11:35:00', 3.0, '1', 'H8HH72G5',
        31.2305000, 121.4738000, 120, 95, '1', '签到轨迹与活动地点一致，服务时长予以确认', '2023-10-15 09:00:00', '1', NULL),
       (2, '2023-10-14 08:28:00', '2023-10-14 11:32:00', 3.0, NULL, 'H8HH72G5',
        31.2500000, 121.5000000, 1860, 60, '2', NULL, NULL, '1', NULL),
       (4, '2023-08-05 09:02:00', '2023-08-05 12:05:00', 3.0, '1', 'G3F8B2D7',
        31.2401000, 121.4801000, 85, 70, '1', '服务时长与活动计划一致，予以确认', '2023-08-06 10:00:00', '1', NULL),
       (4, '2023-08-05 13:00:00', '2023-08-05 15:00:00', 2.0, NULL, NULL,
        NULL, NULL, NULL, NULL, '1', '当日午后加做图书整理，由志愿者组织补录', NULL, '2', NULL);

INSERT INTO training(training_id, activity_num, training_name, training_detail,
                     training_begintime, training_endtime, training_location, training_checkin, training_isdeleted)
VALUES ('tra_00001', 6, '义诊志愿服务岗前培训',
        '讲解义诊流程、岗位分工与沟通注意事项。',
        '2023-09-06 14:00:00', '2023-09-06 16:00:00', '和平社区活动室', '现场签到', 0);

INSERT INTO volunteer_training_situation(training_num, volunteer_num, vtsituation_id,
                                         vtsituation_begintime, vtsituation_endtime, vtsituation_isdeleted)
VALUES (1, 1, 'vts_00001', '2023-09-06 14:00:00', '2023-09-06 16:00:00', 0);

INSERT INTO act_announcement(activity_num, actannouncement_id, actannouncement_name,
                             actannouncement_detail, organization_num, actannouncement_isdeleted)
VALUES (3, 'ann_00001', '城市环保徒步宣传活动集合通知',
        '请报名志愿者于10月14日8:30在城市中心公园南门集合，统一领取志愿服与工具。', 1, 0);

INSERT INTO policy_announcement(policyannouncement_id, policyannouncement_name, policyannouncement_detail,
                                policyannouncement_file, admin_num, policyannouncement_isdeleted)
VALUES ('pol_00001', '志愿服务时长记录说明',
        '志愿者参与志愿服务活动后，需在现场使用志愿者组织公布的签到码完成签到签退，服务时长经志愿者组织复核通过后计入个人累计服务时长，并生成可校验的志愿服务证明。',
        NULL, 1, 0);

INSERT INTO `show`(show_id, show_detail, show_sharetime, volunteer_num, activity_num,
                   show_browse, show_like, show_isdeleted)
VALUES ('sho_00001', '参加城市环保徒步宣传，向市民讲解垃圾分类知识，收获很多。',
        '2023-10-15 10:00:00', 1, 3, 36, 12, 0);

INSERT INTO show_picture(show_num, picture_id, picture_name, picture_route, picture_uniquename, picture_isdeleted)
VALUES (1, 'pic_00001', '环保徒步宣传现场', '/upload/show/2023/10/15/001.jpg', '20231015001.jpg', 0);

INSERT INTO anomaly_rule(rule_code, rule_name, rule_threshold, rule_enabled)
VALUES ('SINGLE_DAY_TOTAL', '单日累计服务时长上限（小时）', 12, 1),
       ('SINGLE_RECORD', '单条服务时长上限（小时）', 12, 1),
       ('NIGHT_SERVICE', '夜间服务开始时间（时）', 5, 1),
       ('MANUAL_BATCH', '同一报名补录条数上限', 3, 1);

INSERT INTO notification(receiver_id, receiver_role, notification_title, notification_detail,
                         notification_read, notification_time)
VALUES ('vol_00001', 'ROLE_VOLUNTEER', '报名申请已通过',
        '您报名的「城市环保徒步宣传」已通过志愿者组织审核，请按时到场并在现场完成签到签退。', 0, '2023-08-26 10:00:00'),
       ('vol_00001', 'ROLE_VOLUNTEER', '服务时长已确认',
        '「城市环保徒步宣传」的服务时长 3.0 小时已由志愿者组织复核确认，并计入您的累计服务时长。', 1, '2023-10-15 09:00:00');
