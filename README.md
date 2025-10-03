# 心理互助平台 (Mentara)

> 一个基于Spring Boot + React的心理健康互助社区平台

## 📋 项目简介

心理互助平台是一个专为大学生设计的心理健康互助社区，提供心情记录、动态分享、AI心理咨询、专家预约等功能，帮助用户更好地管理心理健康。

## ✨ 主要功能

### 🧠 心理健康管理
- **心情打卡** - 每日心情记录与统计
- **心情趋势图** - 可视化心情变化趋势
- **周/月心情报告** - 详细的心情分析报告

### 💬 社区互动
- **动态发布** - 分享生活点滴和心情
- **评论互动** - 支持点赞、评论、回复
- **标签系统** - 分类管理动态内容
- **匿名发布** - 保护用户隐私

### 🤖 AI智能助手
- **智能对话** - 基于DeepSeek的AI心理咨询
- **多角色助手** - 不同专业领域的AI助手
- **会话管理** - 保存对话历史，支持会话切换
- **记忆功能** - 记住用户偏好和对话上下文

### 👨‍⚕️ 专家预约
- **专家列表** - 查看心理专家信息
- **在线预约** - 预约专家咨询服务
- **预约管理** - 查看和管理预约记录

### 👤 用户系统
- **用户注册/登录** - 基于学号的用户认证
- **个人中心** - 个人信息和统计数据
- **头像管理** - 上传、预览、删除头像
- **权限管理** - 用户角色和权限控制

### 🔧 管理功能
- **内容审核** - 动态和评论审核
  - **用户管理** - 用户状态管理（支持软删除）
- **举报处理** - 处理用户举报
- **数据统计** - 平台使用数据统计

## 🛠️ 技术栈

### 后端 (Backend)
- **框架**: Spring Boot 3.x
- **数据库**: MySQL 8.0 + MongoDB
- **安全**: Spring Security + JWT
- **文件存储**: 本地文件系统
- **AI服务**: DeepSeek API
- **构建工具**: Maven

### 前端 (Frontend)
- **框架**: React 18
- **路由**: React Router v6
- **状态管理**: React Context API
- **样式**: CSS3 + 响应式设计
- **构建工具**: Create React App
- **HTTP客户端**: Fetch API

### 开发工具
- **IDE**: IntelliJ IDEA / VS Code
- **版本控制**: Git
- **API测试**: Postman / Apifox
- **数据库管理**: MySQL Workbench / MongoDB Compass

## 📁 项目结构

```
SE25Project-20/
├── client/                 # 前端项目
│   ├── public/            # 静态资源
│   │   ├── components/    # React组件
│   │   ├── pages/         # 页面组件
│   │   ├── services/      # API服务
│   │   ├── context/       # React Context
│   │   ├── utils/         # 工具函数
│   │   └── styles/        # 样式文件
│   └── package.json
├── server/                # 后端项目
│   ├── src/main/java/
│   │   └── com/mentara/
│   │       ├── controller/    # 控制器
│   │       ├── service/       # 服务层
│   │       ├── entity/        # 实体类
│   │       ├── repository/    # 数据访问层
│   │       ├── config/        # 配置类
│   │       └── security/      # 安全配置
│   ├── uploads/           # 文件上传目录
│   └── pom.xml
├── sqlForLocalTest/       # 数据库脚本
├── doc/                   # 项目文档
├── TechPrototype/         # 技术原型
└── UIPrototype/           # UI原型
```

## 🚀 快速开始

### 环境要求
- Node.js 16+
- Java 17+
- MySQL 8.0+
- MongoDB 5.0+

### 后端启动

1. **克隆项目**
```bash
git clone <repository-url>
cd SE25Project-20
```

2. **配置数据库**
```bash
# 创建MySQL数据库
mysql -u root -p
CREATE DATABASE mentara CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入数据库脚本
mysql -u root -p mentara < sqlForLocalTest/complete_init.sql
```

3. **配置MongoDB**
```bash
# 启动MongoDB服务
mongod --dbpath /path/to/data/db
```

4. **修改配置文件**
编辑 `server/src/main/resources/application.properties`：
```properties
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/mentara
spring.datasource.username=your_username
spring.datasource.password=your_password

# MongoDB配置
spring.data.mongodb.uri=mongodb://localhost:27017/mentara

# DeepSeek API配置
deepseek.api-key=your_api_key
```

5. **启动后端服务**
```bash
cd server
mvn spring-boot:run
```

### 前端启动

1. **安装依赖**
```bash
cd client
npm install
```

2. **启动开发服务器**
```bash
npm start
```

3. **访问应用**
打开浏览器访问 `http://localhost:3000`

## 📝 API文档

### 认证相关
- `POST /api/auth/signup` - 用户注册
- `POST /api/auth/signin` - 用户登录
- `POST /api/auth/refresh` - 刷新Token

### 用户相关
- `GET /api/users/profile` - 获取用户信息
- `PUT /api/users/profile` - 更新用户信息
- `GET /api/users/stats` - 获取用户统计

### 动态相关
- `GET /api/posts` - 获取动态列表
- `POST /api/posts` - 发布动态
- `GET /api/posts/{id}` - 获取动态详情
- `DELETE /api/posts/{id}` - 删除动态

### 心情相关
- `POST /api/mood/checkin` - 心情打卡
- `GET /api/mood/current-week` - 获取本周心情
- `GET /api/mood/week/{week}` - 获取指定周心情

### AI聊天相关
- `GET /api/llm/sessions` - 获取会话列表
- `GET /api/llm/sessions/{sessionId}/messages` - 获取消息历史
- `POST /api/llm/sessions/{sessionId}/messages` - 发送消息

### 专家预约相关
- `GET /api/experts` - 获取专家列表
- `POST /api/appointments` - 创建预约
- `GET /api/appointments` - 获取预约列表

## 🔧 开发指南
  
  ### 软删除功能
  项目实现了用户软删除机制，确保删除用户时不会影响帖子评论数统计：
  
  - **软删除标记**：用户删除时只标记 `is_deleted = true`，不物理删除数据
  - **数据完整性**：保留用户的所有操作记录，确保统计准确性
  - **用户名冲突处理**：已删除用户的用户名不能被新用户注册使用
  - **显示处理**：已删除用户显示为"已删除用户"，使用默认头像
  - **恢复功能**：支持恢复已删除用户（需检查用户名冲突）

### 代码规范
- 使用ESLint进行代码检查
- 遵循Java命名规范
- 使用统一的代码格式化配置

### 提交规范
```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式调整
refactor: 代码重构
test: 测试相关
chore: 构建过程或辅助工具的变动
```

### 分支管理
- `main` - 主分支，用于生产环境
- `develop` - 开发分支
- `feature/*` - 功能分支
- `hotfix/*` - 紧急修复分支

## 🧪 测试

### 后端测试
```bash
cd server
mvn test
```

### 前端测试
```bash
cd client
npm test
```

## 📦 部署

### 后端部署
```bash
cd server
mvn clean package
java -jar target/mentara-server-1.0.0.jar
```

### 前端部署
```bash
cd client
npm run build
# 将build目录部署到Web服务器
```

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 👥 团队成员

- **小组#20** - 心理互助平台开发团队

## 📞 联系方式

如有问题或建议，请通过以下方式联系：
- 项目Issues: [GitHub Issues](https://github.com/your-repo/issues)
- 邮箱: your-email@example.com

## 🙏 致谢

感谢所有为这个项目做出贡献的开发者和用户！

---

**注意**: 这是一个教育项目，仅供学习和研究使用。
