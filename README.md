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
| 源程序量 | 93 个源程序文件，共 7843 行（Java 源程序 2583 行、MyBatis 映射文件 567 行、前端页面 3971 行、系统配置 40 行、数据库脚本 521 行、单元测试 161 行），按每页 50 行排版共 157 页 |
| 开发工具 | IntelliJ IDEA、Apache Maven 3.9、MySQL 8.0 |
| 开发环境 | JDK 11、Spring Boot 2.6.6、MyBatis-Plus 3.5.1、MySQL 8.0 |
| 运行环境 | JDK 11 及以上、MySQL 8.0 及以上、浏览器（Chrome / Edge / Firefox） |
| 运行硬件 | CPU 双核 2.0GHz 及以上、内存 4GB 及以上、硬盘 20GB 及以上 |
| 主要功能 | 志愿者注册、用户登录与角色权限控制、志愿活动申报、志愿活动审核、活动分状态管理、活动报名人员管理、报名审核、志愿者组织资料维护 |

> 提交软件著作权登记申请时，著作权人名称、开发完成日期、首次发表日期等信息由申请人填写，详见
> [`docs/软件著作权申请信息.md`](docs/软件著作权申请信息.md)。

## 二、功能模块

| 模块 | 使用角色 | 功能说明 |
| --- | --- | --- |
| 公众门户 | 公众 | 展示志愿者组织与志愿活动风采，提供志愿者注册与用户登录入口 |
| 志愿者注册 | 公众 | 手机号查重校验、填写姓名与密码完成注册，由数据库生成志愿者编号 |
| 用户登录与权限控制 | 全部角色 | 统一账号认证，按角色跳转志愿者组织工作台或平台管理员工作台；未登录访问受保护页面自动跳转登录页；跨角色访问返回 403 |
| 志愿者组织工作台 | 志愿者组织 | 按活动状态统计志愿活动数量，展示审核中的活动与组织资料 |
| 志愿活动申报 | 志愿者组织 | 活动名称查重、活动时间与招募人数校验，申报后活动进入「审核中」状态 |
| 志愿活动管理 | 志愿者组织 | 按审核中、审核未通过、未开始、进行中、已结束分类查询活动，支持查看活动详情与逻辑删除活动 |
| 活动报名人员管理 | 志愿者组织 | 查询活动报名志愿者名单，通过或驳回志愿者的报名申请 |
| 报名审核 | 志愿者组织 | 按报名记录逐条审核，审核结果与审核时间写入数据库 |
| 组织资料维护 | 志愿者组织 | 查询并修改本组织的名称与简介 |
| 平台管理员工作台 | 平台管理员 | 统计并展示待审核的志愿活动 |
| 志愿活动审核 | 平台管理员 | 查看活动详情，对志愿活动作出审核通过与审核不通过的处理，审核结果、审核管理员与审核时间写入数据库 |

## 三、技术架构

- **表现层**：Spring MVC 控制器 + 原生 HTML/CSS/JavaScript 页面，页面通过 Ajax 调用后端接口并渲染数据。
- **业务层**：`service` / `service.impl` 分层接口与实现，业务实现基于 MyBatis-Plus `ServiceImpl` 提供标准增删改查能力，
  并实现活动申报、活动审核、报名审核等业务方法。
- **数据访问层**：MyBatis-Plus `BaseMapper` + 自定义映射文件 `resources/mapper/*.xml`；
  报名、活动审核、活动申报等涉及多表写库的业务通过 MySQL 存储过程实现，保证业务数据的一致性。
- **安全控制**：Spring Security 实现登录认证与基于角色的页面、接口访问控制，密码以 MD5 摘要方式存储与校验。
- **数据模型**：`volunteer` 志愿者表、`organization` 志愿者组织表、`admin` 平台管理员表、`e_user` 统一用户表、
  `activity` 志愿活动表、`participate` 活动报名表、`checkin` 志愿服务签到表、`training` 志愿培训表、
  `volunteer_training_situation` 志愿者培训情况表、`act_announcement` 志愿活动公告表、
  `policy_announcement` 通知公告表、`policy_file` 公告附件表、`show` 志愿秀表、`show_picture` 志愿秀图片表。

## 四、目录结构

```
e-zhiyuan-platform
├── pom.xml                                    Maven 构建配置
├── README.md                                  软件说明
├── docs                                       软件著作权申请材料
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
    │   │   ├── controller                     控制器：活动、组织、报名、注册、平台管理员
    │   │   ├── entity                         实体类
    │   │   ├── enums                          枚举类
    │   │   ├── mapper                         数据访问接口
    │   │   ├── service                        业务接口与实现
    │   │   └── utils                          密码摘要、密码编码器、登录成功处理器
    │   └── resources
    │       ├── application.properties         系统配置
    │       ├── db/e_volunteer.sql             数据库脚本（建库、建表、存储过程、初始化数据）
    │       ├── mapper                          MyBatis 映射文件
    │       ├── static                          前端静态资源
    │       └── templates                       前端页面（公众门户、志愿者组织、平台管理员）
    └── test/java/com/evolunteer                单元测试
```

## 五、快速开始

### 1. 准备数据库

```bash
mysql -uroot -p < src/main/resources/db/e_volunteer.sql
```

脚本会自动完成建库、建表、创建存储过程并写入初始化数据。

### 2. 修改数据库连接（可选）

默认连接 `localhost:3306/e_volunteer`，账号 `root`，密码 `801`。如与实际环境不一致，
可修改 `src/main/resources/application.properties`，或使用环境变量覆盖：

```bash
export EVOLUNTEER_DB_URL="jdbc:mysql://localhost:3306/e_volunteer?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai"
export EVOLUNTEER_DB_USERNAME=root
export EVOLUNTEER_DB_PASSWORD=你的密码
```

### 3. 构建与运行

```bash
mvn clean package -DskipTests
java -jar target/e-zhiyuan-platform-1.0.0.jar
```

### 4. 访问系统

| 页面 | 地址 |
| --- | --- |
| 公众门户 | http://localhost:8111/portal/index.html |
| 用户登录 | http://localhost:8111/admin/login.html |
| 志愿者注册 | http://localhost:8111/admin/sign-up.html |

初始化账号（初始密码均为 `123456`）：

| 角色 | 登录账号 | 登录后进入 |
| --- | --- | --- |
| 平台管理员 | `admin_001` | 平台管理员工作台 |
| 志愿者组织 | `org_001` | 志愿者组织工作台 |
| 志愿者组织 | `org_002` | 志愿者组织工作台 |

### 5. 运行测试

```bash
mvn test
```

## 六、软件著作权申请材料

| 材料 | 说明 |
| --- | --- |
| 源程序文档 | 已生成 [`docs/源程序.txt`](docs/源程序.txt)（93 个源程序文件，每页 50 行，共 165 页）；源程序变动后可执行 `python3 tools/source_doc.py --output docs/源程序.txt` 重新生成，按“前后各连续 30 页”提交时追加 `--head-tail 30` |
| 软件说明书 | 见 [`docs/软件说明书.md`](docs/软件说明书.md)，含软件概述、运行环境、功能模块与操作说明及界面截图（插图见 `docs/images/`） |
| 申请信息 | 见 [`docs/软件著作权申请信息.md`](docs/软件著作权申请信息.md)，汇总申请表所需的软件信息 |
