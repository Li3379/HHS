# HHS - 健康生活小技巧社区

<div align="center">
  <h3>🏥 Health Hack System</h3>
  <p>一个基于 AI 的健康生活知识分享与交流平台</p>
</div>

---

## 📖 项目简介

HHS (Health Hack System) 是一个健康生活小技巧社区平台，用户可以分享和学习各种健康相关的生活技巧，包括饮食、运动、睡眠、心理健康等方面的知识。平台集成了 AI 功能，可以智能分类健康内容并提供个性化的健康建议。

### ✨ 核心功能

- **🔐 用户系统**: 注册、登录、个人信息管理（JWT认证）
- **📝 健康技巧**: 发布、浏览、搜索健康生活小技巧
- **👍 互动功能**: 点赞、收藏、评论
- **🤖 AI 分类**: 智能识别并分类健康内容
- **💬 AI 顾问**: AI 健康问答助手（开发中）
- **📊 个人中心**: 我的发布、我的收藏、统计数据（开发中）

---

## 🏗️ 技术架构

### 后端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 编程语言 |
| Spring Boot | 3.2.0 | 应用框架 |
| Spring Security | 6.x | 安全框架 |
| MyBatis-Plus | 3.5.5 | ORM框架 |
| MySQL | 8.0+ | 关系型数据库 |
| Redis | 7.0+ | 缓存数据库 |
| JWT | 0.11.5 | Token认证 |
| LangChain4j | 0.35.0 | AI集成框架 |
| HanLP | 1.8.5 | NLP工具 |
| Knife4j | 4.3.0 | API文档 |
| Hutool | 5.8.28 | 工具库 |

### 前端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.3.7 | 渐进式框架 |
| Vite | 4.5.0 | 构建工具 |
| Element Plus | 2.5.6 | UI组件库 |
| Vue Router | 4.2.5 | 路由管理 |
| Pinia | 2.1.7 | 状态管理 |
| Axios | 1.6.2 | HTTP客户端 |
| Day.js | 1.11.10 | 时间处理 |

---

## 📦 项目结构

```
HHS/
├── hhs-backend/                 # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/hhs/
│   │   │   │   ├── common/      # 通用类（Result、ErrorCode等）
│   │   │   │   ├── config/      # 配置类（Security、MyBatis等）
│   │   │   │   ├── controller/  # 控制器层
│   │   │   │   ├── dto/         # 数据传输对象
│   │   │   │   ├── entity/      # 实体类
│   │   │   │   ├── exception/   # 异常处理
│   │   │   │   ├── mapper/      # MyBatis Mapper
│   │   │   │   ├── security/    # 安全相关（JWT、Filter等）
│   │   │   │   ├── service/     # 服务层
│   │   │   │   └── vo/          # 视图对象
│   │   │   └── resources/
│   │   │       ├── application.yml        # 主配置文件
│   │   │       ├── application-dev.yml    # 开发环境配置
│   │   │       └── sql/schema.sql         # 数据库初始化脚本
│   │   └── test/                # 单元测试
│   └── pom.xml                  # Maven配置
│
├── hhs-frontend/                # 前端项目
│   ├── src/
│   │   ├── api/                 # API接口
│   │   ├── components/          # 公共组件
│   │   ├── router/              # 路由配置
│   │   ├── store/               # 状态管理
│   │   ├── utils/               # 工具函数
│   │   ├── views/               # 页面视图
│   │   └── App.vue              # 根组件
│   ├── public/                  # 静态资源
│   ├── index.html               # HTML入口
│   ├── vite.config.js           # Vite配置
│   └── package.json             # NPM配置
│
├── .gitignore                   # Git忽略配置
└── README.md                    # 项目说明文档
```

---

## 🚀 快速开始

### 环境要求

- **JDK**: 17+
- **Maven**: 3.6+
- **Node.js**: 18+
- **MySQL**: 8.0+
- **Redis**: 7.0+

### 1. 克隆项目

```bash
git clone <repository-url>
cd HHS
```

### 2. 初始化数据库

```bash
# 1. 创建数据库
mysql -u root -p
CREATE DATABASE `hhs` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

# 2. 导入表结构
mysql -u root -p hhs < hhs-backend/src/main/resources/sql/schema.sql
```

### 3. 配置后端

修改 `hhs-backend/src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/hhs?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root           # 修改为你的数据库用户名
    password: 123456         # 修改为你的数据库密码
  data:
    redis:
      host: localhost
      port: 6379
      password: root         # 修改为你的Redis密码（如果没有密码可留空）
```

### 4. 启动后端

```bash
cd hhs-backend
mvn clean install
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动

API 文档地址: `http://localhost:8080/doc.html`

### 5. 启动前端

```bash
cd hhs-frontend
npm install
npm run dev
```

前端应用将在 `http://localhost:5173` 启动

### 6. 访问应用

打开浏览器访问: `http://localhost:5173`

---

## 🔧 配置说明

### AI 功能配置（可选）

如果需要启用真实的 AI 功能，需要配置大模型 API：

1. 获取阿里云通义千问 API Key
2. 设置环境变量：

```bash
# Windows
set DASH_SCOPE_API_KEY=your-api-key

# Linux/Mac
export DASH_SCOPE_API_KEY=your-api-key
```

或在 `application-dev.yml` 中配置：

```yaml
spring:
  ai:
    langchain4j:
      openai:
        chat-model:
          api-key: your-api-key
          model-name: qwen-max
```

> **注意**: 不配置 API Key 时，AI 功能将使用 Mock 模式（基于关键词匹配）

---

## 🛑 优雅关闭服务

项目已配置 Spring Boot Actuator 的优雅关闭功能，支持通过 HTTP 请求远程关闭服务。

### 方法一：命令行停止（推荐开发环境）

在运行服务的终端中按 `Ctrl + C`，Spring Boot 会自动执行优雅关闭。

### 方法二：通过 Actuator 端点关闭

**前提条件**：
- 需要有效的 JWT Token（已登录用户）
- 服务必须运行在开发环境（`application-dev.yml`）

**调用方式**：

```bash
# 1. 先登录获取 Token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser001","password":"Test123456"}'

# 2. 使用返回的 token 调用 shutdown 端点
curl -X POST http://localhost:8080/actuator/shutdown \
  -H "Authorization: Bearer <your-jwt-token>" \
  -H "Content-Type: application/json"

# 返回示例
{
  "message": "Shutting down, bye..."
}
```

**优雅关闭特性**：
- ✅ 停止接受新请求
- ✅ 等待现有请求完成（最多 30 秒）
- ✅ 自动关闭数据库连接池
- ✅ 清理 Redis 连接
- ✅ 释放系统资源

**其他 Actuator 端点**：

| 端点 | 访问权限 | 说明 |
|------|---------|------|
| `/actuator/health` | 公开 | 健康检查 |
| `/actuator/info` | 公开 | 应用信息 |
| `/actuator/shutdown` | 需要登录 | 优雅关闭服务 |

> **安全提示**：生产环境建议禁用 shutdown 端点，使用进程管理工具（如 systemd、supervisor）控制服务生命周期。

---

## 📚 API 接口

### 用户模块

| 接口 | 方法 | 说明 | 认证 |
|------|------|------|------|
| `/api/auth/register` | POST | 用户注册 | ❌ |
| `/api/auth/login` | POST | 用户登录 | ❌ |
| `/api/user/profile` | GET | 获取个人信息 | ✅ |
| `/api/user/profile` | PUT | 更新个人信息 | ✅ |
| `/api/user/password` | PUT | 修改密码 | ✅ |

### 健康技巧模块

| 接口 | 方法 | 说明 | 认证 |
|------|------|------|------|
| `/api/tips` | POST | 发布技巧 | ✅ |
| `/api/tips` | GET | 获取技巧列表 | ❌ |
| `/api/tips/{id}` | GET | 获取技巧详情 | ❌ |
| `/api/tips/{id}/like` | POST | 点赞/取消点赞 | ✅ |
| `/api/tips/{id}/collect` | POST | 收藏/取消收藏 | ✅ |

### 评论模块

| 接口 | 方法 | 说明 | 认证 |
|------|------|------|------|
| `/api/tips/{tipId}/comments` | GET | 获取评论列表 | ❌ |
| `/api/tips/{tipId}/comments` | POST | 发布评论 | ✅ |

### AI 模块

| 接口 | 方法 | 说明 | 认证 |
|------|------|------|------|
| `/api/ai/classify` | POST | AI 内容分类 | ❌ |

完整 API 文档请访问: `http://localhost:8080/doc.html`

---

## 🧪 测试

### 运行后端测试

```bash
cd hhs-backend
mvn test
```

### 测试账号

```
用户名: testuser001
密码: Test123456
```

### 测试报告

详细测试报告请查看: [最终测试报告.md](./最终测试报告.md)

- ✅ 12项功能测试全部通过
- ✅ 数据库验证通过
- ✅ 安全性验证通过
- ✅ 基础性能测试通过

---

## 📝 开发指南

### 代码规范

- **Java**: 遵循阿里巴巴 Java 开发手册
- **Vue**: 遵循 Vue 官方风格指南
- **命名**: 使用有意义的英文命名
- **注释**: 关键代码必须添加注释

### Git 提交规范

```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式调整
refactor: 重构代码
test: 测试相关
chore: 构建/工具链相关
```

示例:
```bash
git commit -m "feat: 添加AI健康顾问功能"
git commit -m "fix: 修复登录token过期问题"
```

---

## 🗺️ 开发计划

### 已完成 ✅

- [x] 用户注册、登录、认证
- [x] 健康技巧发布、浏览
- [x] 点赞、收藏功能
- [x] 评论功能
- [x] AI 智能分类（Mock版）
- [x] 统一异常处理
- [x] API 文档

### 进行中 🚧

- [ ] AI 健康顾问对话功能
- [ ] 真实 AI 分类（集成大模型）
- [ ] 个人中心完整功能
- [ ] 搜索功能

### 计划中 📋

- [ ] 移动端适配优化
- [ ] 富文本 XSS 过滤
- [ ] Redis 缓存优化
- [ ] 图片上传功能
- [ ] 消息通知系统
- [ ] 管理后台

---

## 🤝 贡献指南

欢迎贡献代码、提出问题和建议！

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'feat: Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

---

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

---

## 👥 开发团队

- **项目负责人**: [Your Name]
- **后端开发**: [Your Name]
- **前端开发**: [Your Name]

---

## 📞 联系方式

- **Issues**: [GitHub Issues](https://github.com/your-repo/issues)
- **Email**: your-email@example.com

---

## 🙏 致谢

感谢以下开源项目：

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue.js](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)
- [MyBatis-Plus](https://baomidou.com/)
- [LangChain4j](https://docs.langchain4j.dev/)

---

<div align="center">
  <p>Made with ❤️ by HHS Team</p>
  <p>© 2025 HHS. All rights reserved.</p>
</div>

