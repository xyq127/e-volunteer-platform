# E志愿志愿者服务平台 V1.0

E志愿志愿者服务平台面向志愿者、志愿者组织与平台管理员，提供志愿活动申报、志愿活动审核、志愿者报名与报名审核、
组织资料维护以及志愿服务信息管理等服务，是一个基于 Spring Boot 的志愿活动管理 Web 应用。

---

## 一、软件基本信息

| 项目 | 内容 |
| --- | --- |
| 软件全称 | E志愿志愿者服务平台 |
| 软件简称 | E志愿 |
| 版本号 | V1.0 |
| 软件分类 | 应用软件 — 志愿活动管理平台 |
| 开发语言 | Java、JavaScript、HTML、SQL |
| 源程序量 | 189 个源程序文件，共 33794 行（Java 源程序 12477 行、MyBatis 映射文件 2066 行、前端页面 16333 行、系统配置 52 行、数据库脚本 2292 行、单元测试 574 行），按每页 50 行排版共 676 页；使用源程序文档生成工具生成的源程序文档（含文件标题行与页眉）共 691 页 |
| 开发工具 | IntelliJ IDEA、Apache Maven 3.9、MySQL 8.0 |
| 开发环境 | JDK 11、Spring Boot 2.6.6、MyBatis-Plus 3.5.1、MySQL 8.0 |
| 运行环境 | JDK 11 及以上、MySQL 8.0 及以上、浏览器（Chrome / Edge / Firefox） |
| 运行硬件 | CPU 双核 2.0GHz 及以上、内存 4GB 及以上、硬盘 20GB 及以上 |
| 主要功能 | 志愿者注册与登录、用户登录与角色权限控制、志愿者工作台、志愿活动申报、志愿活动审核、活动分状态管理、可信签到与志愿服务时长核算、智能供需匹配推荐、活动报名与名额候补管理、志愿服务信用分与活动结算、志愿服务证明与在线校验、活动报名人员管理、报名审核、组织资料维护 |

> 提交软件著作权登记申请时，著作权人名称、开发完成日期、首次发表日期等信息由申请人填写，详见
> [`docs/软件著作权申请信息.md`](docs/软件著作权申请信息.md)。

## 二、功能模块

| 模块 | 使用角色 | 功能说明 |
| --- | --- | --- |
| 公众门户 | 公众 | 展示志愿者组织与志愿活动风采，提供志愿者注册与用户登录入口 |
| 志愿者注册 | 公众 | 手机号查重校验、填写姓名与密码完成注册，由数据库生成志愿者编号并同时开通登录账号 |
| 用户登录与权限控制 | 全部角色 | 统一账号认证，按角色跳转志愿者工作台、志愿者组织工作台或平台管理员工作台；未登录访问受保护页面自动跳转登录页；跨角色访问返回 403 |
| 志愿者工作台 | 志愿者 | 展示已核定累计服务时长、参与活动数、志愿服务信用分与星级，给出距下一星级的成长进度与推荐活动 |
| 活动广场与报名 | 志愿者 | 按技能、距离、时间冲突与组织历史参与计算的匹配度排序展示可报名活动，支持一键报名与撤回报名 |
| 我的报名与签到 | 志愿者 | 查询本人报名状态（待审核、已通过、候补、未通过）、在现场使用签到码与定位完成签到签退，查看服务时长与复核结果 |
| 我的档案与密码 | 志愿者 | 维护服务技能标签与常住定位，作为供需匹配依据；自助修改登录密码 |
| 消息通知 | 全部角色 | 报名提交与审核结果、参加确认、服务时长复核结果、活动公告、活动开始与结束等节点推送站内通知，未读数量在页面展示 |
| 培训与活动公告 | 志愿者 | 查看本人已报名活动的志愿培训安排与活动公告 |
| 志愿秀 | 志愿者 | 分享志愿服务经历并上传图片，公开发布到门户志愿秀广场，支持点赞（同一志愿者只计一次） |
| 志愿服务证明 | 志愿者 | 生成含累计服务时长、服务记录明细、星级与校验码的服务证明，支持打印与第三方在线校验 |
| 可信签到与时长核算 | 志愿者、志愿者组织 | 现场签到码、签到时间窗口与地理围栏三重校验，签到签退自动核算服务时长，异常轨迹标记后交组织复核，复核通过才计入累计时长 |
| 短时轮换签到码 | 志愿者、志愿者组织 | 签到码由活动签到密钥与时间窗口派生，每 120 秒轮换一次，志愿者组织可更换密钥立即作废旧码，降低签到码被转发复用的风险 |
| 服务对象反向确认 | 志愿者、志愿者组织、服务对象 | 志愿者组织生成服务确认单，服务对象用确认码与本人手机号在公开页面确认或否认本次服务；否认且已计入时长时平台立即冲销并通知组织核实 |
| 时长异常规则引擎 | 志愿者组织、平台管理员 | 规则表可配置阈值，内置单日累计上限、单条上限、夜间服务、补录条数上限四条规则；命中规则的记录不计入累计时长，转平台管理员裁定（计入/维持计入或驳回/冲销），并有定时巡检标记事后异常 |
| 供需智能匹配 | 志愿者、志愿者组织 | 志愿者活动广场按匹配度推荐活动并给出匹配理由；志愿者组织审核报名时可查看报名者与本活动的匹配度 |
| 名额与候补管理 | 志愿者、志愿者组织 | 报名满员自动进入候补队列，撤回或驳回释放名额后自动递补；报名信用分低于 60 分时暂停报名 |
| 活动结算与信用分 | 志愿者、志愿者组织 | 报名通过后需确认参加，未按期确认自动释放名额；活动结束后结算考勤，记录爽约并扣减信用分；服务时长复核通过后累加累计时长并奖励信用分 |
| 志愿者组织工作台 | 志愿者组织 | 按活动状态统计志愿活动数量，展示审核中的活动与组织资料 |
| 志愿活动申报 | 志愿者组织 | 活动名称查重、活动时间与招募人数校验，申报后活动进入「审核中」状态 |
| 志愿活动管理 | 志愿者组织 | 按审核中、审核未通过、未开始、进行中、已结束分类查询活动，支持查看活动详情与逻辑删除活动 |
| 活动报名人员管理 | 志愿者组织 | 分页查询活动报名志愿者名单与匹配度，通过或驳回报名，确认参加状态可视化，可移除报名并自动递补候补志愿者 |
| 报名审核 | 志愿者组织 | 按报名记录逐条审核，审核结果与审核时间写入数据库，并推送站内通知告知志愿者 |
| 活动状态流转与结算 | 志愿者组织 | 人工将活动置为进行中或已结束，按活动时间自动流转；活动结束后结算考勤，记录爽约并扣减信用分 |
| 服务时长补录与导出 | 志愿者组织 | 为线下服务补录时长（提交平台管理员复核），导出活动报名考勤名单与服务时长明细 |
| 培训与活动公告管理 | 志愿者组织 | 维护志愿培训安排与志愿者培训情况；为活动发布公告并通知已报名的志愿者 |
| 组织资料维护 | 志愿者组织 | 查询并修改本组织的名称与简介 |
| 平台管理员工作台 | 平台管理员 | 统计待审核志愿活动与待复核补录记录数量 |
| 志愿活动审核 | 平台管理员 | 查看活动详情，对志愿活动作出审核通过与审核不通过的处理，审核不通过须选择结构化原因 |
| 补录时长复核 | 平台管理员 | 复核志愿者组织补录的服务时长，确认后计入志愿者累计服务时长 |
| 通知公告与附件 | 平台管理员 | 发布、修改与删除通知公告，上传与删除公告附件，门户同步展示 |
| 账号管理与审计日志 | 平台管理员 | 开通志愿者组织账号、启用或停用组织与志愿者账号、重置账号密码；按操作类型与账号查询审计日志 |
| 志愿秀管理 | 平台管理员 | 查看并删除违规志愿秀 |

## 三、技术架构

- **表现层**：Spring MVC 控制器 + 原生 HTML/CSS/JavaScript 页面，页面通过 Ajax 调用后端接口并渲染数据；
  全部列表接口统一分页参数 `page`/`size`/`keyword`，统一返回 `list`/`total`/`page`/`size`。
- **业务层**：`service` / `service.impl` 分层接口与实现，业务实现基于 MyBatis-Plus `ServiceImpl` 提供标准增删改查能力，
  并实现活动申报、活动审核、重新申报、状态流转、可信签到、供需匹配、报名候补、服务时长补录与复核、活动结算、
  站内通知、账号管理与操作审计等业务方法。
- **数据访问层**：MyBatis-Plus `BaseMapper` + 自定义映射文件 `resources/mapper/*.xml`；
  报名、活动审核、活动申报、状态流转、签到签退、时长复核与补录、活动结算等涉及多表写库或需要行锁校验的业务
  通过 MySQL 存储过程实现，保证业务数据的一致性。
- **定时任务**：`PlatformTaskScheduler` 周期执行，按活动时间自动流转活动状态、释放活动开始前仍未确认参加的报名名额，
  并按异常规则巡检单日累计超限的已计入记录，标记后通知平台管理员裁定。
- **时长异常规则引擎**：规则阈值保存在 `anomaly_rule` 表，可配置阈值与启停；`service_anomaly_check` 在服务时长复核与
  补录时逐条判定，`service_anomaly_scan` 定时巡检事后异常，命中记录的裁定结果（计入、维持计入、驳回、冲销）由平台管理员作出。
- **安全控制**：Spring Security 实现登录认证与基于角色的页面、接口访问控制；密码以 PBKDF2 加盐摘要方式存储与校验，
  历史 MD5 摘要密文在账号登录成功后自动升级为加盐密文；账号停用后无法登录；关键操作写入操作审计表。
- **匹配算法**：`utils/MatchCalculator` 按技能命中比例、球面距离、时间冲突与组织历史参与四个维度计算 0 至 100 的匹配度，
  距离由 MySQL `ST_Distance_Sphere` 计算，匹配打分为纯函数并配有单元测试。
- **文件与导出**：`FileStorageService` 以随机存储名保存白名单类型的附件与图片并按下发路径下载；
  数据导出按 RFC 4180 生成带 UTF-8 字节序标记的 CSV，可直接用表格软件打开。
- **证明与确认防伪**：志愿服务证明的校验码由平台密钥对累计服务数据计算 HMAC 摘要；服务确认单的确认码为随机码，
  且必须与服务对象预留手机号同时匹配才能确认，服务对象否认时已计入时长会被冲销。
- **数据模型**：`volunteer` 志愿者表、`volunteer_skill` 志愿者技能表、`organization` 志愿者组织表、`admin` 平台管理员表、
  `e_user` 统一用户表、`activity` 志愿活动表、`activity_skill` 活动技能表、`participate` 活动报名表、
  `checkin` 志愿服务记录表、`training` 志愿培训表、`volunteer_training_situation` 志愿者培训情况表、
  `act_announcement` 志愿活动公告表、`policy_announcement` 通知公告表、`policy_file` 公告附件表、
  `show` 志愿秀表、`show_picture` 志愿秀图片表、`show_like_record` 志愿秀点赞记录表、
  `notification` 站内通知表、`audit_log` 操作审计表、`anomaly_rule` 时长异常规则表。

## 四、目录结构

```
e-volunteer-platform
├── pom.xml                                    Maven 构建配置
├── README.md                                  软件说明
├── docs                                       项目文档与软件著作权申请材料
│   ├── 痛点分析与突破说明.md                    行业痛点调研与本次功能突破说明
│   ├── 软件说明书.md                           软件使用说明书（含操作截图）
│   ├── 软件著作权申请信息.md                    申请表所需软件信息
│   └── images                                 说明书插图
├── tools
│   └── source_doc.py                          源程序文档生成工具
└── src
    ├── main
    │   ├── java/com/evolunteer
    │   │   ├── EVolunteerApplication.java     平台启动类
    │   │   ├── config                         安全配置、MyBatis-Plus 配置
    │   │   ├── controller                     控制器：活动、组织、报名、注册、签到、志愿者、证书、平台管理员
    │   │   ├── entity                         实体类
    │   │   ├── enums                          枚举类
    │   │   ├── mapper                         数据访问接口
    │   │   ├── service                        业务接口与实现
    │   │   └── utils                          匹配计算、证明校验码、时间解析、密码编码与升级、登录成功处理器
    │   └── resources
    │       ├── application.properties         系统配置
    │       ├── db/e_volunteer_platform.sql             数据库脚本（建库、建表、存储过程、初始化数据）
    │       ├── mapper                          MyBatis 映射文件
    │       ├── static                          前端静态资源
    │       └── templates                       前端页面（公众门户、志愿者、志愿者组织、平台管理员）
    └── test/java/com/evolunteer                单元测试
```

## 五、快速开始

### 1. 准备数据库

```bash
mysql -uroot -p < src/main/resources/db/e_volunteer_platform.sql
```

脚本会自动完成建库、建表、创建存储过程并写入初始化数据。

### 2. 修改数据库连接（可选）

默认连接 `localhost:3306/e_volunteer_platform`，账号 `root`，密码 `801`。如与实际环境不一致，
可修改 `src/main/resources/application.properties`，或使用环境变量覆盖：

```bash
export EVOLUNTEER_DB_URL="jdbc:mysql://localhost:3306/e_volunteer_platform?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai"
export EVOLUNTEER_DB_USERNAME=root
export EVOLUNTEER_DB_PASSWORD=你的密码
```

志愿服务证明的校验码由平台密钥计算，部署环境建议通过环境变量指定专属密钥（不设置时使用默认值）：

```bash
export EVOLUNTEER_CERT_SECRET=你的证明密钥
```

上传的公告附件与志愿秀图片保存在 `evolunteer.upload.dir` 指定目录（默认工作目录下的 `upload`），
定时任务的执行间隔可通过环境变量调整：

```bash
export EVOLUNTEER_UPLOAD_DIR=/data/evolunteer/upload
export EVOLUNTEER_SCHEDULE_INTERVAL_MS=60000
```

### 3. 构建与运行

```bash
mvn clean package -DskipTests
java -jar target/e-volunteer-platform-1.0.0.jar
```

### 4. 访问系统

| 页面 | 地址 |
| --- | --- |
| 公众门户 | http://localhost:8111/portal/index.html |
| 用户登录 | http://localhost:8111/admin/login.html |
| 志愿者注册 | http://localhost:8111/admin/sign-up.html |

新页面一览：

| 页面 | 地址 |
| --- | --- |
| 志愿者工作台 | http://localhost:8111/volunteer/index.html |
| 活动广场 | http://localhost:8111/volunteer/activity_plaza.html |
| 我的报名（签到签退、确认参加） | http://localhost:8111/volunteer/my_participation.html |
| 我的档案与密码 | http://localhost:8111/volunteer/profile.html |
| 消息通知 | http://localhost:8111/volunteer/notifications.html |
| 培训与活动公告 | http://localhost:8111/volunteer/training.html |
| 志愿秀 | http://localhost:8111/volunteer/show.html |
| 志愿服务证明 | http://localhost:8111/volunteer/certificate.html |
| 签到与时长复核（组织） | http://localhost:8111/org/checkin_review.html |
| 培训管理（组织） | http://localhost:8111/org/training.html |
| 活动公告（组织） | http://localhost:8111/org/announcements.html |
| 组织账号（管理员） | http://localhost:8111/admin/organizations.html |
| 志愿者名单（管理员） | http://localhost:8111/admin/volunteers.html |
| 补录时长复核（管理员） | http://localhost:8111/admin/manual_hours.html |
| 通知公告（管理员） | http://localhost:8111/admin/announcements.html |
| 审计日志（管理员） | http://localhost:8111/admin/audit_log.html |
| 志愿秀广场（公众） | http://localhost:8111/portal/shows.html |
| 服务证明校验（公众） | http://localhost:8111/portal/verify.html |
| 服务确认（公众） | http://localhost:8111/portal/service-confirm.html |

初始化账号（初始密码均为 `123456`）：

| 角色 | 登录账号 | 登录后进入 |
| --- | --- | --- |
| 平台管理员 | `admin_001` | 平台管理员工作台 |
| 志愿者组织 | `org_001`、`org_002` | 志愿者组织工作台 |
| 志愿者 | `vol_00001`～`vol_00004` | 志愿者工作台 |

### 5. 运行测试

```bash
mvn test
```

## 六、软件著作权申请材料

| 材料 | 说明 |
| --- | --- |
| 源程序文档 | 已生成 [`docs/源程序.txt`](docs/源程序.txt)（189 个源程序文件、35229 行，每页 50 行共 691 页）；源程序变动后可执行 `python3 tools/source_doc.py --output docs/源程序.txt` 重新生成，按“前后各连续 30 页”提交时追加 `--head-tail 30` |
| 软件说明书 | 见 [`docs/软件说明书.md`](docs/软件说明书.md)，含软件概述、运行环境、功能模块与操作说明及界面截图（插图见 `docs/images/`） |
| 申请信息 | 见 [`docs/软件著作权申请信息.md`](docs/软件著作权申请信息.md)，汇总申请表所需的软件信息 |
| 痛点分析与突破 | 见 [`docs/痛点分析与突破说明.md`](docs/痛点分析与突破说明.md)，汇总行业痛点调研来源、本次功能突破的规则取值与验证方式 |
