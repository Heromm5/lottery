# 大乐透数据分析与预测系统 - 部署文档

## 1. 系统概述

基于 Spring Boot + Vue 3 的大乐透历史数据分析与智能预测系统，包含 10 种预测算法、持续学习权重调整、历史回测验证等功能。

### 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Spring Boot 2.7.18, Java 8, MyBatis-Plus 3.5.5 |
| 前端 | Vue 3, TypeScript, Vite, Element Plus, ECharts |
| 数据库 | MySQL 8.0 |
| 缓存 | Caffeine |

---

## 2. 服务器环境

### 2.1 虚拟机信息

| 项目 | 值 |
|------|-----|
| 服务器地址 | 192.168.202.101 |
| SSH 别名 | server01 |
| 访问地址 | http://192.168.202.101:9060 |
| 部署目录 | /opt/lottery/app/ |
| 日志文件 | /opt/lottery/app/app.log |

### 2.2 数据库信息

| 项目 | 值 |
|------|-----|
| 地址 | 192.168.202.101:3306 |
| 数据库名 | lottery |
| 用户 | root |
| 密码 | Atguigu.123 |

### 2.3 SSH 配置

文件：`C:\Users\69401\.ssh\config`

```bash
Host server01
    HostName 192.168.202.101
    User root
    Port 22
    IdentityFile ~/.ssh/id_vm
    PreferredAuthentications publickey
    ServerAliveInterval 60
    ServerAliveCountMax 5
```

连接命令：`ssh server01`

---

## 3. 部署流程

### 3.1 本地构建

```bash
# 1. 进入前端目录
cd frontend

# 2. 安装依赖（首次或依赖变更时）
npm install

# 3. 构建前端
npm run build

# 4. 复制前端文件到静态资源目录
cp -r dist/* ../src/main/resources/static/

# 5. 回到项目根目录
cd ..

# 6. 打包后端
mvn clean package -DskipTests
```

### 3.2 上传部署

```bash
# 1. 上传 JAR 文件到服务器
scp target/lottery-java-1.0-SNAPSHOT.jar server01:/opt/lottery/app/lottery-java.jar

# 2. SSH 连接并重启服务
ssh server01 "pkill -f lottery-java.jar; sleep 2; cd /opt/lottery/app && nohup java -Xms512m -Xmx1024m -jar lottery-java.jar --server.port=9060 > app.log 2>&1 &"
```

### 3.3 验证部署

```bash
# 等待应用启动
sleep 15

# 检查端口
ssh server01 "netstat -tlnp | grep 9060"

# 测试首页
curl http://192.168.202.101:9060/

# 测试 API
curl http://192.168.202.101:9060/api/lottery/latest
```

---

## 4. 服务管理

### 4.1 启动服务

```bash
ssh server01 "cd /opt/lottery/app && nohup java -Xms512m -Xmx1024m -jar lottery-java.jar --server.port=9060 > app.log 2>&1 &"
```

### 4.2 停止服务

```bash
ssh server01 "pkill -f lottery-java.jar"
```

### 4.3 查看日志

```bash
# 实时日志
ssh server01 "tail -f /opt/lottery/app/app.log"

# 最后 50 行
ssh server01 "tail -50 /opt/lottery/app/app.log"
```

### 4.4 查看进程

```bash
ssh server01 "ps aux | grep java"
```

---

## 5. 一键部署命令

```bash
# 完整部署流程
cd frontend && npm run build && cp -r dist/* ../src/main/resources/static/ && cd .. && mvn clean package -DskipTests && scp target/lottery-java-1.0-SNAPSHOT.jar server01:/opt/lottery/app/lottery-java.jar && ssh server01 "pkill -f lottery-java.jar; sleep 2; cd /opt/lottery/app && nohup java -Xms512m -Xmx1024m -jar lottery-java.jar --server.port=9060 > app.log 2>&1 &"
```

---

## 6. 常见问题

### 6.1 端口占用

**错误**：`java.net.BindException: 地址已在使用`

**解决**：
```bash
ssh server01 "pkill -f lottery-java.jar"
sleep 2
ssh server01 "cd /opt/lottery/app && nohup java -Xms512m -Xmx1024m -jar lottery-java.jar --server.port=9060 > app.log 2>&1 &"
```

### 6.2 前端页面 404

**错误**：访问 `/verification`、`/prediction` 等返回 404

**原因**：IndexController 未正确配置 SPA 路由转发

**解决**：确保 IndexController 包含：
```java
@GetMapping(value = "/{path:[^\\.]+}")
public String spaForward() {
    return "forward:/index.html";
}
```

### 6.3 JS/CSS 加载失败

**错误**：`Failed to load module script`，Content-Type 为 text/html

**原因**：SPA 路由规则错误，静态资源被转发到 index.html

**解决**：路由规则应只匹配不包含点（.）的路径：
```java
// 正确
@GetMapping(value = "/{path:[^\\.]+}")

// 错误（会导致静态资源也被转发）
@GetMapping(value = "/{path:[^\\.]*}")
@GetMapping(value = "/{path:[^\\.]*}/**}")
```

### 6.4 前端静态文件缺失

**错误**：访问根路径返回 404

**解决**：
```bash
cd frontend && npm run build
cp -r dist/* ../src/main/resources/static/
cd .. && mvn clean package -DskipTests
```

---

## 7. 验证清单

- [ ] `netstat -tlnp | grep 9060` → 端口监听正常
- [ ] `curl http://192.168.202.101:9060/` → 首页返回 HTML
- [ ] `curl http://192.168.202.101:9060/api/lottery/latest` → API 返回 JSON
- [ ] 浏览器访问 http://192.168.202.101:9060/verification → 页面正常显示

---

## 8. 项目结构

```
lottery-java/
├── src/main/
│   ├── java/com/hobart/lottery/
│   │   ├── controller/       # 控制器
│   │   ├── service/         # 业务逻辑
│   │   ├── mapper/          # 数据访问
│   │   ├── entity/          # 实体类
│   │   ├── config/          # 配置类
│   │   └── predictor/       # 预测算法
│   └── resources/
│       ├── application.yml   # 应用配置
│       ├── static/          # 前端静态文件（构建生成）
│       └── db/              # 数据库脚本
├── frontend/                 # Vue 3 前端
│   ├── src/                # 源码
│   └── dist/               # 构建产物
└── target/                 # Maven 构建输出
```

---

## 9. 本地开发

### 9.1 后端运行

```bash
cd lottery-java
mvn spring-boot:run
```

访问：http://localhost:9060

### 9.2 前端开发

```bash
cd frontend
npm install
npm run dev
```

- 前端：http://localhost:3000
- API 代理到：http://localhost:9060

---

## 10. API 文档

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/lottery/latest | GET | 最新开奖结果 |
| /api/lottery/list | GET | 开奖列表 |
| /api/prediction/generate | POST | 生成预测 |
| /api/analysis/frequency | GET | 频率统计 |
| /api/analysis/missing | GET | 遗漏分析 |
| /api/verification/verify | POST | 验证预测 |
| /api/learning/weights | GET/POST | 权重管理 |
