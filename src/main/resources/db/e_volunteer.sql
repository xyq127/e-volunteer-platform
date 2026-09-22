-- =============================================================================
-- E志愿志愿者服务平台 V1.0 数据库脚本
-- 数据库：MySQL 8.0
-- 内容：数据库创建、业务表创建、存储过程创建、初始化数据
-- 执行方式：mysql -uroot -p < e_volunteer.sql
-- =============================================================================

-- 连接字符集与库、表的字符集保持一致，避免存储过程参数与字段比较时出现字符集冲突
SET NAMES utf8mb4 COLLATE utf8mb4_general_ci;

DROP DATABASE IF EXISTS e_volunteer;
CREATE DATABASE e_volunteer DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE e_volunteer;


-- -----------------------------------------------------------------------------
-- 1. 志愿者表：保存志愿者的编号、姓名、性别、联系方式与注册日期
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS volunteer;
CREATE TABLE volunteer
(
    volunteer_num          INT AUTO_INCREMENT COMMENT '志愿者编号，主键自增',
    volunteer_id           VARCHAR(32)  DEFAULT NULL COMMENT '志愿者业务编号，注册时由数据库生成',
    volunteer_password     VARCHAR(64)  DEFAULT NULL COMMENT '登录密码密文（MD5 摘要）',
    volunteer_name         VARCHAR(50)  DEFAULT NULL COMMENT '志愿者姓名',
    volunteer_registerdate DATE         DEFAULT NULL COMMENT '注册日期',
    volunteer_gender       VARCHAR(4)   DEFAULT NULL COMMENT '性别',
    volunteer_birth        DATE         DEFAULT NULL COMMENT '出生日期',
    volunteer_tel          VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
    PRIMARY KEY (volunteer_num),
    UNIQUE KEY uk_volunteer_tel (volunteer_tel),
    KEY idx_volunteer_id (volunteer_id),
    KEY idx_volunteer_name (volunteer_name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='志愿者表';

-- -----------------------------------------------------------------------------
-- 2. 志愿者组织表：保存志愿者组织的账号、名称、成立日期与组织简介
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS organization;
CREATE TABLE organization
(
    organization_num           INT AUTO_INCREMENT COMMENT '志愿者组织编号，主键自增',
    organization_id            VARCHAR(32)  DEFAULT NULL COMMENT '志愿者组织登录账号',
    organization_password      VARCHAR(64)  DEFAULT NULL COMMENT '登录密码密文（MD5 摘要）',
    organization_name          VARCHAR(100) DEFAULT NULL COMMENT '志愿者组织名称',
    organization_establishdate DATE         DEFAULT NULL COMMENT '成立日期',
    organization_introduction TEXT COMMENT '组织简介',
    organization_isdeleted     INT          DEFAULT 0 COMMENT '删除标记，0 未删除、1 已删除',
    PRIMARY KEY (organization_num),
    UNIQUE KEY uk_organization_id (organization_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='志愿者组织表';

-- -----------------------------------------------------------------------------
-- 3. 平台管理员表：保存平台管理员的账号、密码与姓名
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS admin;
CREATE TABLE admin
(
    admin_num      INT AUTO_INCREMENT COMMENT '管理员编号，主键自增',
    admin_id       VARCHAR(32)  DEFAULT NULL COMMENT '管理员登录账号',
    admin_password VARCHAR(64)  DEFAULT NULL COMMENT '登录密码密文（MD5 摘要）',
    admin_name     VARCHAR(50)  DEFAULT NULL COMMENT '管理员姓名',
    PRIMARY KEY (admin_num),
    UNIQUE KEY uk_admin_id (admin_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='平台管理员表';

-- -----------------------------------------------------------------------------
-- 4. 统一用户表：保存志愿者组织与平台管理员的登录账号、密码密文与角色权限
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS e_user;
CREATE TABLE e_user
(
    id       VARCHAR(32) NOT NULL COMMENT '登录账号',
    password VARCHAR(64) DEFAULT NULL COMMENT '登录密码密文（MD5 摘要）',
    role     VARCHAR(64) DEFAULT NULL COMMENT '角色权限，如 ROLE_ADMIN、ROLE_ORGANIZATION',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='统一用户表';

-- -----------------------------------------------------------------------------
-- 5. 志愿活动表：保存志愿者组织申报的志愿活动及其审核、开展状态
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS activity;
CREATE TABLE activity
(
    activity_num         INT AUTO_INCREMENT COMMENT '活动编号，主键自增',
    organization_num     INT          DEFAULT NULL COMMENT '申报活动的志愿者组织编号',
    activity_id          VARCHAR(32)  DEFAULT NULL COMMENT '活动业务编号',
    activity_name        VARCHAR(100) DEFAULT NULL COMMENT '活动名称',
    activity_detail      TEXT COMMENT '活动内容',
    activity_begintime   DATETIME     DEFAULT NULL COMMENT '活动开始时间',
    activity_endtime     DATETIME     DEFAULT NULL COMMENT '活动结束时间',
    activity_location    VARCHAR(200) DEFAULT NULL COMMENT '活动地点',
    activity_needpeople  INT          DEFAULT NULL COMMENT '需求人数',
    activity_notice      VARCHAR(500) DEFAULT NULL COMMENT '注意事项',
    activity_publishtime DATETIME     DEFAULT NULL COMMENT '申报时间',
    activity_signddl     DATETIME     DEFAULT NULL COMMENT '报名截止时间',
    activity_state       VARCHAR(4)   DEFAULT NULL COMMENT '活动状态，0 审核中、1 未开始、2 进行中、3 已结束、4 审核未通过',
    admin_num            INT          DEFAULT NULL COMMENT '审核该活动的管理员编号',
    activity_checkin     VARCHAR(100) DEFAULT NULL COMMENT '签到方式',
    activity_leaderid    INT          DEFAULT NULL COMMENT '活动负责人编号',
    activity_checktime   DATETIME     DEFAULT NULL COMMENT '审核时间',
    activity_remark      VARCHAR(500) DEFAULT NULL COMMENT '审核意见',
    activity_isdeleted   INT          DEFAULT 0 COMMENT '删除标记，0 未删除、1 已删除',
    PRIMARY KEY (activity_num),
    KEY idx_activity_org_state (organization_num, activity_state, activity_isdeleted),
    KEY idx_activity_state (activity_state, activity_isdeleted),
    KEY idx_activity_name (activity_name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='志愿活动表';

-- -----------------------------------------------------------------------------
-- 6. 活动报名表：保存志愿者报名志愿活动的申请、审核与志愿服务时长信息
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS participate;
CREATE TABLE participate
(
    participate_num            INT AUTO_INCREMENT COMMENT '报名编号，主键自增',
    volunteer_num              INT         DEFAULT NULL COMMENT '志愿者编号',
    activity_num               INT         DEFAULT NULL COMMENT '活动编号',
    participate_applytime      DATETIME    DEFAULT NULL COMMENT '报名申请时间',
    participate_applystate     VARCHAR(4)  DEFAULT NULL COMMENT '报名审核状态，0 待审核、1 已通过、2 未通过',
    participate_applychecktime DATETIME    DEFAULT NULL COMMENT '报名审核时间',
    participate_training       VARCHAR(100) DEFAULT NULL COMMENT '培训情况',
    participate_begintime      DATETIME    DEFAULT NULL COMMENT '服务开始时间',
    participate_endtime        DATETIME    DEFAULT NULL COMMENT '服务结束时间',
    participate_duration       DOUBLE      DEFAULT NULL COMMENT '服务时长（小时）',
    participate_timecheck      VARCHAR(4)  DEFAULT NULL COMMENT '服务时长审核状态',
    participate_isdeleted      INT         DEFAULT 0 COMMENT '删除标记，0 未删除、1 已删除',
    PRIMARY KEY (participate_num),
    KEY idx_participate_activity (activity_num),
    KEY idx_participate_volunteer (volunteer_num)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='活动报名表';

-- -----------------------------------------------------------------------------
-- 7. 志愿服务签到表：保存志愿者参与志愿服务的签到时间与服务时长
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS checkin;
CREATE TABLE checkin
(
    checkin_num        INT AUTO_INCREMENT COMMENT '签到编号，主键自增',
    participate_num    INT         DEFAULT NULL COMMENT '报名编号',
    checkin_begintime  DATETIME    DEFAULT NULL COMMENT '签到时间',
    checkin_endtime    DATETIME    DEFAULT NULL COMMENT '签退时间',
    checkin_duration   DOUBLE      DEFAULT NULL COMMENT '服务时长（小时）',
    checkin_timecheck  VARCHAR(4)  DEFAULT NULL COMMENT '服务时长审核状态',
    PRIMARY KEY (checkin_num),
    KEY idx_checkin_participate (participate_num)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='志愿服务签到表';

-- -----------------------------------------------------------------------------
-- 8. 志愿培训表：保存志愿活动对应的培训安排
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
-- 9. 志愿者培训情况表：保存志愿者参加志愿培训的情况
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
-- 10. 志愿活动公告表：保存志愿者组织针对志愿活动发布的公告
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
-- 11. 通知公告表：保存平台管理员发布的通知公告
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
-- 12. 公告附件表：保存通知公告对应的附件文件信息
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
-- 13. 志愿秀表：保存志愿者分享的志愿服务经历及其浏览、点赞数据
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
-- 14. 志愿秀图片表：保存志愿秀内容对应的图片文件信息
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

-- =============================================================================
-- 存储过程
-- =============================================================================
DELIMITER $$

-- -----------------------------------------------------------------------------
-- 志愿者注册：写入志愿者信息并由数据库生成志愿者编号
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS volunteer_insert $$
CREATE PROCEDURE volunteer_insert(IN volunteerName VARCHAR(50),
                                  IN volunteerTel VARCHAR(20),
                                  IN volunteerPassword VARCHAR(64),
                                  OUT volunteerId VARCHAR(32))
BEGIN
    DECLARE duplicateCount INT DEFAULT 0;
    DECLARE newNum INT DEFAULT 0;

    SELECT COUNT(*) INTO duplicateCount FROM volunteer WHERE volunteer_tel = volunteerTel;

    IF duplicateCount > 0 THEN
        SET volunteerId = NULL;
    ELSE
        INSERT INTO volunteer(volunteer_id, volunteer_password, volunteer_name,
                              volunteer_registerdate, volunteer_tel)
        VALUES ('', volunteerPassword, volunteerName, CURDATE(), volunteerTel);
        SET newNum = LAST_INSERT_ID();
        SET volunteerId = CONCAT('vol_', LPAD(newNum, 5, '0'));
        UPDATE volunteer SET volunteer_id = volunteerId WHERE volunteer_num = newNum;
    END IF;
END $$

-- -----------------------------------------------------------------------------
-- 志愿活动申报：志愿者组织申报志愿活动，申报后活动进入审核中状态
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS organization_insert_activity $$
CREATE PROCEDURE organization_insert_activity(IN loginId VARCHAR(32),
                                              IN activityName VARCHAR(100),
                                              IN activityDetail TEXT,
                                              IN activityBegintime DATE,
                                              IN activityEndtime DATE,
                                              IN activityLocation VARCHAR(200),
                                              IN activityNeedpeople VARCHAR(20),
                                              IN activityNotice VARCHAR(500),
                                              IN activitySignddl DATE,
                                              OUT msg VARCHAR(200))
BEGIN
    DECLARE orgNum INT DEFAULT NULL;
    DECLARE newNum INT DEFAULT 0;

    SELECT organization_num INTO orgNum FROM organization WHERE organization_id = loginId LIMIT 1;

    IF orgNum IS NULL THEN
        SET msg = '未找到志愿者组织信息，请重新登录后再试';
    ELSE
        INSERT INTO activity(organization_num, activity_id, activity_name, activity_detail,
                             activity_begintime, activity_endtime, activity_location,
                             activity_needpeople, activity_notice, activity_publishtime,
                             activity_signddl, activity_state, activity_isdeleted)
        VALUES (orgNum, '', activityName, activityDetail,
                activityBegintime, activityEndtime, activityLocation,
                CAST(activityNeedpeople AS UNSIGNED), activityNotice, NOW(),
                activitySignddl, '0', 0);
        SET newNum = LAST_INSERT_ID();
        UPDATE activity SET activity_id = CONCAT('act_', LPAD(newNum, 5, '0')) WHERE activity_num = newNum;
        SET msg = '志愿活动申报成功，等待平台管理员审核';
    END IF;
END $$

-- -----------------------------------------------------------------------------
-- 志愿活动审核：平台管理员审核志愿活动并写入审核结果
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS admin_check_activity $$
CREATE PROCEDURE admin_check_activity(IN loginId VARCHAR(32),
                                      IN activityNum INT,
                                      IN isPass INT,
                                      IN remark VARCHAR(500),
                                      OUT msg VARCHAR(200))
BEGIN
    DECLARE adminNumValue INT DEFAULT NULL;
    DECLARE affectedRows INT DEFAULT 0;

    SELECT admin_num INTO adminNumValue FROM admin WHERE admin_id = loginId LIMIT 1;

    UPDATE activity
    SET activity_state     = isPass,
        admin_num          = adminNumValue,
        activity_checktime = NOW(),
        activity_remark    = remark
    WHERE activity_num = activityNum;

    SET affectedRows = ROW_COUNT();

    IF affectedRows > 0 AND isPass = 1 THEN
        SET msg = '审核通过';
    ELSEIF affectedRows > 0 THEN
        SET msg = '审核未通过';
    ELSE
        SET msg = '审核失败，未找到对应的志愿活动';
    END IF;
END $$

-- -----------------------------------------------------------------------------
-- 报名审核：志愿者组织审核志愿者的活动报名申请
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS organization_check_volunteer $$
CREATE PROCEDURE organization_check_volunteer(IN participateNum INT,
                                              IN isPass INT,
                                              OUT msg VARCHAR(200))
BEGIN
    DECLARE affectedRows INT DEFAULT 0;

    UPDATE participate
    SET participate_applystate     = CAST(isPass AS CHAR),
        participate_applychecktime = NOW()
    WHERE participate_num = participateNum;

    SET affectedRows = ROW_COUNT();

    IF affectedRows > 0 AND isPass = 1 THEN
        SET msg = '报名申请已通过';
    ELSEIF affectedRows > 0 THEN
        SET msg = '报名申请未通过';
    ELSE
        SET msg = '操作失败，未找到对应的报名记录';
    END IF;
END $$

DELIMITER ;

-- =============================================================================
-- 初始化数据
-- 平台管理员账号：admin_001，初始密码：123456
-- 志愿者组织账号：org_001、org_002，初始密码：123456
-- =============================================================================

INSERT INTO admin(admin_id, admin_password, admin_name)
VALUES ('admin_001', 'e10adc3949ba59abbe56e057f20f883e', '王敏');

INSERT INTO organization(organization_id, organization_password, organization_name,
                         organization_establishdate, organization_introduction, organization_isdeleted)
VALUES ('org_001', 'e10adc3949ba59abbe56e057f20f883e', '阳光青年志愿者协会',
        '2019-03-05', '阳光青年志愿者协会成立于2019年，长期开展助学、助老、环保等志愿服务活动。', 0),
       ('org_002', 'e10adc3949ba59abbe56e057f20f883e', '城市公益服务中心',
        '2020-06-18', '城市公益服务中心面向社区居民开展公益服务与志愿服务活动。', 0);

INSERT INTO e_user(id, password, role)
VALUES ('admin_001', 'e10adc3949ba59abbe56e057f20f883e', 'ROLE_ADMIN'),
       ('org_001', 'e10adc3949ba59abbe56e057f20f883e', 'ROLE_ORGANIZATION'),
       ('org_002', 'e10adc3949ba59abbe56e057f20f883e', 'ROLE_ORGANIZATION');

INSERT INTO volunteer(volunteer_id, volunteer_password, volunteer_name, volunteer_registerdate,
                      volunteer_gender, volunteer_birth, volunteer_tel)
VALUES ('vol_00001', 'e10adc3949ba59abbe56e057f20f883e', '张三', '2023-02-11', '男', '2001-05-20', '13800000001'),
       ('vol_00002', 'e10adc3949ba59abbe56e057f20f883e', '李四', '2023-03-02', '女', '2002-09-14', '13800000002'),
       ('vol_00003', 'e10adc3949ba59abbe56e057f20f883e', '王五', '2023-03-18', '男', '2000-12-01', '13800000003'),
       ('vol_00004', 'e10adc3949ba59abbe56e057f20f883e', '赵六', '2023-04-06', '女', '2001-07-30', '13800000004');

INSERT INTO activity(organization_num, activity_id, activity_name, activity_detail,
                     activity_begintime, activity_endtime, activity_location, activity_needpeople,
                     activity_notice, activity_publishtime, activity_signddl, activity_state,
                     admin_num, activity_checkin, activity_checktime, activity_remark, activity_isdeleted)
VALUES (1, 'act_00001', '社区爱心助学志愿活动',
        '为社区困难家庭学生提供课业辅导与阅读陪伴，每周六上午开展一次。',
        '2023-09-02 09:00:00', '2023-09-02 11:30:00', '阳光社区活动中心', 12,
        '请提前十分钟到场签到，佩戴志愿者证。', '2023-08-10 10:20:00', '2023-08-30 18:00:00', '0',
        NULL, '现场签到', NULL, NULL, 0),
       (1, 'act_00002', '敬老院陪伴志愿服务',
        '到敬老院陪伴老人聊天、整理房间并开展文艺表演。',
        '2023-09-16 14:00:00', '2023-09-16 17:00:00', '幸福敬老院', 8,
        '注意与老人沟通的语气，服从现场负责人安排。', '2023-08-12 09:00:00', '2023-09-12 18:00:00', '0',
        NULL, '现场签到', NULL, NULL, 0),
       (1, 'act_00003', '城市环保徒步宣传',
        '沿城市公园步道开展垃圾分类宣传与沿途垃圾清理。',
        '2023-10-14 08:30:00', '2023-10-14 11:30:00', '城市中心公园南门', 20,
        '请穿着运动鞋，自备饮用水。', '2023-08-20 15:00:00', '2023-10-08 18:00:00', '1',
        1, '现场签到', '2023-08-21 10:00:00', '活动方案完整，审核通过', 0),
       (1, 'act_00004', '图书馆图书整理志愿服务',
        '协助图书馆整理书架、修补图书并引导读者检索。',
        '2023-08-05 09:00:00', '2023-08-05 12:00:00', '市图书馆三楼', 6,
        '保持安静，服从图书管理员安排。', '2023-07-20 11:00:00', '2023-08-01 18:00:00', '3',
        1, '现场签到', '2023-07-21 09:30:00', '审核通过', 0),
       (1, 'act_00005', '暑期乡村支教志愿活动',
        '到乡村小学开展暑期支教活动，内容包含兴趣课程与安全教育。',
        '2023-07-10 09:00:00', '2023-07-20 17:00:00', '青山乡中心小学', 15,
        '需连续参与，请提前安排时间。', '2023-06-25 10:00:00', '2023-07-05 18:00:00', '4',
        1, '现场签到', '2023-06-26 14:00:00', '活动周期与人员安排不明确，请完善后重新申报', 0),
       (2, 'act_00006', '社区义诊志愿服务',
        '协助社区医院开展义诊活动，负责引导与登记工作。',
        '2023-09-09 08:30:00', '2023-09-09 12:00:00', '和平社区广场', 10,
        '请服从医护人员安排。', '2023-08-15 10:00:00', '2023-09-05 18:00:00', '0',
        NULL, '现场签到', NULL, NULL, 0);

INSERT INTO participate(volunteer_num, activity_num, participate_applytime, participate_applystate,
                        participate_applychecktime, participate_training, participate_begintime,
                        participate_endtime, participate_duration, participate_timecheck, participate_isdeleted)
VALUES (1, 3, '2023-08-25 09:10:00', '1', '2023-08-26 10:00:00', '已完成岗前培训',
        '2023-10-14 08:30:00', '2023-10-14 11:30:00', 3.0, '1', 0),
       (2, 3, '2023-08-26 14:20:00', '1', '2023-08-27 09:30:00', '已完成岗前培训',
        '2023-10-14 08:30:00', '2023-10-14 11:30:00', 3.0, '1', 0),
       (3, 3, '2023-08-28 19:05:00', '0', NULL, NULL, NULL, NULL, NULL, NULL, 0),
       (4, 4, '2023-07-25 10:00:00', '1', '2023-07-26 11:00:00', '已完成岗前培训',
        '2023-08-05 09:00:00', '2023-08-05 12:00:00', 3.0, '1', 0);

INSERT INTO training(training_id, activity_num, training_name, training_detail,
                     training_begintime, training_endtime, training_location, training_checkin, training_isdeleted)
VALUES ('tra_00001', 6, '义诊志愿服务岗前培训',
        '讲解义诊流程、岗位分工与沟通注意事项。',
        '2023-09-06 14:00:00', '2023-09-06 16:00:00', '和平社区活动室', '现场签到', 0);

INSERT INTO volunteer_training_situation(training_num, volunteer_num, vtsituation_id,
                                         vtsituation_begintime, vtsituation_endtime, vtsituation_isdeleted)
VALUES (1, 1, 'vts_00001', '2023-09-06 14:00:00', '2023-09-06 16:00:00', 0);

INSERT INTO checkin(participate_num, checkin_begintime, checkin_endtime, checkin_duration, checkin_timecheck)
VALUES (1, '2023-10-14 08:25:00', '2023-10-14 11:35:00', 3.0, '1'),
       (2, '2023-10-14 08:28:00', '2023-10-14 11:32:00', 3.0, '1');

INSERT INTO act_announcement(activity_num, actannouncement_id, actannouncement_name,
                             actannouncement_detail, organization_num, actannouncement_isdeleted)
VALUES (3, 'ann_00001', '城市环保徒步宣传活动集合通知',
        '请报名志愿者于10月14日8:30在城市中心公园南门集合，统一领取志愿服与工具。', 1, 0);

INSERT INTO policy_announcement(policyannouncement_id, policyannouncement_name, policyannouncement_detail,
                                policyannouncement_file, admin_num, policyannouncement_isdeleted)
VALUES ('pol_00001', '志愿服务时长记录说明',
        '志愿者参与志愿服务活动后，由志愿者组织统一记录服务起止时间与服务时长，平台按月汇总。',
        NULL, 1, 0);

INSERT INTO `show`(show_id, show_detail, show_sharetime, volunteer_num, activity_num,
                   show_browse, show_like, show_isdeleted)
VALUES ('sho_00001', '参加城市环保徒步宣传，向市民讲解垃圾分类知识，收获很多。',
        '2023-10-15 10:00:00', 1, 3, 36, 12, 0);

INSERT INTO show_picture(show_num, picture_id, picture_name, picture_route, picture_uniquename, picture_isdeleted)
VALUES (1, 'pic_00001', '环保徒步宣传现场', '/upload/show/2023/10/15/001.jpg', '20231015001.jpg', 0);
