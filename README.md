# Quiz App Backend - 答题竞赛小程序后端

基于 Spring Boot + MySQL + Agnes AI 的答题竞赛小程序后端服务。

## 功能特性

- 微信小程序登录
- AI 动态出题（常识 + 逻辑推理各5题）
- 答题记录保存与判分
- 排行榜（按题型：正确率 + 用时）
- 用户答题历史查询

## 技术栈

- Java 17
- Spring Boot 3.2
- MyBatis-Plus 3.5
- MySQL 8.0
- OkHttp 4.12
- Lombok

## 快速开始

### 1. 环境准备

- JDK 17+
- Maven 3.8+
- MySQL 8.0+

### 2. 数据库初始化

```bash
mysql -u root -p < src/main/resources/db/schema.sql
```

### 3. 配置修改

编辑 `src/main/resources/application.yml`：

```yaml
# 替换为你的真实配置
spring.datasource.username=root
spring.datasource.password=your_mysql_password
wx.app-id=YOUR_WX_APP_ID
wx.app-secret=YOUR_WX_APP_SECRET
agnes.ai.api-key=YOUR_AGNES_API_KEY
```

### 4. 启动项目

```bash
mvn spring-boot:run
```

服务将在 `http://localhost:8080` 启动。

## API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/wx/login | 微信登录 |
| POST | /api/quiz/start | 开始答题 |
| POST | /api/quiz/submit | 提交答案 |
| GET | /api/leaderboard/{type} | 获取排行榜 |
| GET | /api/user/records | 我的答题记录 |

## 项目结构

```
src/main/java/com/quiz/app/
├── QuizAppApplication.java     # 启动类
├── config/                      # 配置类
├── controller/                  # 控制器
├── dto/                         # 数据传输对象
├── entity/                      # 实体类
├── mapper/                      # MyBatis Mapper
└── service/                     # 业务逻辑
    └── impl/                    # 实现类
```
