# Lottery Java 项目部署文档

## 1. 部署架构（当前）

```
┌─────────────────────────────────────────────────────────────┐
│                        虚拟机 server01                       │
│  192.168.202.101                                           │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  Spring Boot 应用 (9060端口)                         │   │
│  │  ├── 后端 REST API: /api/*                          │   │
│  │  └── 前端静态文件: classpath:/static/               │   │
│  └─────────────────────────────────────────────────────┘   │
│                          │                                  │
│                          ▼                                  │
│  依赖服务:                                                   │
│  └── MySQL (192.168.202.101:3306) - 数据持久化            │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

> **注**: 当前采用前后端一体化部署，Spring Boot 直接提供前端静态文件和 API。

---

## 2. SSH 连接配置

### 2.1 本地 SSH 配置

文件: `C:\Users\69401\.ssh\config`

```bash
# 192.168.202.101
Host server01
    HostName 192.168.202.101
    User root
    Port 22
    IdentityFile ~/.ssh/id_vm
    PreferredAuthentications publickey
    ServerAliveInterval 60
    ServerAliveCountMax 5
```

### 2.2 连接命令

```bash
# 使用别名连接
ssh server01

# 或直接使用 IP
ssh root@192.168.202.101
```

---

## 3. 虚拟机内部署信息

| 项目 | 值 |
|------|-----|
| 虚拟机地址 | 192.168.202.101 |
| 部署用户 | atguigu |
| 后端端口 | 9060 |
| 访问地址 | http://192.168.202.101:9060/ |
| 应用目录 | /opt/lottery/app/ |
| JAR 文件 | /opt/lottery/app/lottery-java.jar |
| 日志文件 | /opt/lottery/app/app.log |
| 数据库地址 | 192.168.202.101:3306 |
| 数据库名称 | lottery |
| 数据库用户 | root |

---

## 4. 快速部署命令

### 4.1 本地构建

```bash
# 1. 构建前端
cd frontend
npm install
npm run build

# 2. 复制前端文件到静态资源目录
cp -r dist/* ../src/main/resources/static/

# 3. 打包后端
cd ..
mvn clean package -DskipTests
```

### 4.2 部署到虚拟机

```bash
# 1. 上传 JAR 文件
scp target/lottery-java-1.0-SNAPSHOT.jar server01:/opt/lottery/app/lottery-java.jar

# 2. SSH 连接并重启服务
ssh server01 "pkill -f lottery-java.jar; sleep 2; cd /opt/lottery/app && nohup su - atguigu -c 'java -Xms512m -Xmx1024m -jar lottery-java.jar --server.port=9060' > app.log 2>&1 &"
```

### 4.3 验证部署

```bash
# 等待应用启动
sleep 15

# 检查端口
ssh server01 "netstat -tlnp | grep 9060"

# 测试访问
curl http://192.168.202.101:9060/
curl http://192.168.202.101:9060/api/lottery/latest
```

---

## 5. 服务管理命令

### 5.1 启动服务

```bash
# 使用 atguigu 用户启动（避免权限问题）
ssh server01 "cd /opt/lottery/app && nohup su - atguigu -c 'java -Xms512m -Xmx1024m -jar lottery-java.jar --server.port=9060' > app.log 2>&1 &"
```

### 5.2 停止服务

```bash
ssh server01 "pkill -f lottery-java.jar"
```

### 5.3 查看日志

```bash
# 实时日志
ssh server01 "tail -f /opt/lottery/app/app.log"

# 最后 50 行
ssh server01 "tail -50 /opt/lottery/app/app.log"
```

### 5.4 查看进程

```bash
ssh server01 "ps aux | grep java"
```

---

## 6. 常见错误及解决方案

### 6.1 端口占用错误

**错误日志**:
```
Caused by: java.net.BindException: 地址已在使用
```

**解决方案**:
```bash
# 1. 查找占用端口的进程
ssh server01 "netstat -tlnp | grep 9060"

# 2. 强制杀死进程
ssh server01 "pkill -f lottery-java.jar"

# 3. 等待端口释放
sleep 2

# 4. 重新启动
ssh server01 "cd /opt/lottery/app && nohup su - atguigu -c 'java -Xms512m -Xmx1024m -jar lottery-java.jar --server.port=9060' > app.log 2>&1 &"
```

### 6.2 前端页面 404

**错误现象**: 访问 `/verification`、`/prediction` 等路径返回 404

**原因**: IndexController 未正确配置 SPA 路由转发

**解决方案**: 确保 IndexController 包含以下配置:
```java
@GetMapping(value = "/{path:[^\\.]+}")
public String spaForward() {
    return "forward:/index.html";
}
```

重新打包部署后即可。

### 6.3 JS/CSS 文件加载失败

**错误日志**:
```
Failed to load module script: Expected a JavaScript module but...
Content-Type: text/html
```

**原因**: SPA 路由转发规则错误，导致 JS/CSS 请求被错误转发到 index.html

**解决方案**: 
1. 确保路由规则只匹配不包含点（.）的路径：`/{path:[^\\.]+}`
2. 不要使用 `/{path:[^\\.]*}` 或 `/{path:[^\\.]*}/**}` 这样的规则
3. 验证 JS 文件的 Content-Type 是否正确（应为 application/javascript）

### 6.4 前端静态文件缺失

**错误现象**: 访问根路径返回 404

**原因**: 打包时未先构建前端

**解决方案**:
```bash
# 1. 构建前端
cd frontend && npm run build

# 2. 复制到静态资源目录
cp -r dist/* ../src/main/resources/static/

# 3. 重新打包后端
cd ..
mvn clean package -DskipTests
```

---

## 7. 项目配置

### 7.1 application.yml 关键配置

```yaml
server:
  port: 9060

spring:
  datasource:
    url: jdbc:mysql://192.168.202.101:3306/lottery
    username: root
    password: Atguigu.123
```

### 7.2 CORS 配置

文件: `src/main/java/com/hobart/lottery/config/WebMvcConfig.java`

```java
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
            .allowedOriginPatterns("http://localhost:*", "http://192.168.202.101:*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
}
```

---

## 8. 验证清单

部署完成后，依次检查:

- [ ] `ssh server01 "netstat -tlnp | grep 9060"` → Java进程监听9060端口
- [ ] `curl http://192.168.202.101:9060/` → 前端首页可访问
- [ ] `curl http://192.168.202.101:9060/api/lottery/latest` → API 正常返回数据
- [ ] 浏览器访问 http://192.168.202.101:9060/verification → 验证中心页面正常
- [ ] `ssh server01 "tail -20 /opt/lottery/app/app.log"` → 日志无报错

---

## 9. 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Spring Boot 2.7.18 + Java 8 |
| ORM | MyBatis-Plus 3.5.5 |
| 数据库 | MySQL 8.0 |
| 前端 | Vue 3 + Vite + Element Plus |
| 构建 | Maven + npm |

---

## 10. 目录结构

```
lottery-java/
├── src/main/
│   ├── java/com/hobart/lottery/
│   │   ├── controller/      # 控制器
│   │   ├── service/        # 业务逻辑
│   │   ├── mapper/         # 数据访问
│   │   ├── entity/        # 实体类
│   │   ├── config/        # 配置类
│   │   └── predictor/     # 预测算法
│   └── resources/
│       ├── application.yml
│       ├── static/        # 前端静态文件（构建生成）
│       └── db/            # 数据库脚本
├── frontend/
│   ├── src/              # Vue 源码
│   ├── dist/             # 构建产物
│   └── vite.config.ts
└── target/               # Maven 构建输出
```
