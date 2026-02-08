# Lottery Java 项目部署文档

## 1. 部署架构

```
┌─────────────────────────────────────────────────────────────┐
│                        虚拟机 server01                       │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  Nginx (8080端口)                                   │   │
│  │  ├── 前端静态文件: /usr/share/nginx/html/lottery/   │   │
│  │  └── /api/ 代理到 localhost:9060                   │   │
│  └─────────────────────────────────────────────────────┘   │
│                          │                                  │
│                          ▼                                  │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  Java应用 (9060端口)                                │   │
│  │  └── /opt/lottery/app/lottery-java.jar             │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  依赖服务:                                                   │
│  ├── MySQL (3306) - 数据持久化                             │
│  └── Redis (6379) - 缓存/消息队列                          │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## 2. 快速部署命令

### 2.1 后端部署 (Java)

```bash
# 1. 本地打包
mvn clean package -DskipTests

# 2. 上传到虚拟机
scp target/lottery-java-1.0-SNAPSHOT.jar server01:/opt/lottery/app/lottery-java.jar

# 3. SSH连接虚拟机，停止旧进程并重启
ssh server01 << 'EOF'
# 查找并停止旧进程
ps aux | grep lottery-java.jar | grep -v grep | awk '{print $2}' | xargs -r kill -9

# 等待端口释放
sleep 2

# 重新启动应用 (指定内存: 512m-1024m, 端口: 9060)
cd /opt/lottery/app
nohup java -Xms512m -Xmx1024m -jar lottery-java.jar --server.port=9060 > app.log 2>&1 &

# 等待启动
sleep 8

# 验证启动
netstat -tlnp | grep 9060
curl -s http://localhost:9060/ 2>/dev/null | head -5
EOF
```

### 2.2 前端部署 (Vue/Nginx)

```bash
# 1. 本地打包前端
cd frontend
npm install
npm run build

# 2. 上传静态文件到虚拟机
scp -r dist/* server01:/usr/share/nginx/html/lottery/

# 3. 重启Nginx使配置生效
ssh server01 "systemctl restart nginx"
```

## 3. 端口访问说明

| 服务 | 地址 | 说明 |
|------|------|------|
| **前端页面** | http://server01:8080 | Vue构建的静态页面 |
| **后端API** | http://server01:9060 | Spring Boot REST API |
| **API代理** | http://server01:8080/api/* | Nginx代理到后端 |

> ⚠️ 注意：前端应用配置在 **8080 端口**，而非默认的 80 端口。

## 4. 常见错误及解决方案

### 4.1 端口占用错误

**错误日志**:
```
Caused by: java.net.BindException: 地址已在使用
```

**原因**: 之前的Java进程未完全关闭，仍占用9060端口。

**解决方案**:
```bash
# 查找占用端口的进程
netstat -tlnp | grep 9060

# 强制杀死进程
kill -9 <PID>

# 或使用pkill
pkill -f lottery-java.jar
```

### 4.2 部署后应用启动失败

**检查步骤**:
```bash
# 1. 查看应用日志
tail -50 /opt/lottery/app/app.log

# 2. 检查进程是否运行
ps aux | grep lottery-java

# 3. 验证端口监听
netstat -tlnp | grep 9060

# 4. 测试API响应
curl http://localhost:9060/
```

### 4.3 前端页面显示CentOS欢迎页

**原因**: 访问了80端口而非8080端口。

**解决方案**:
- 前端应用在 **8080 端口**，请访问 http://server01:8080
- 或修改Nginx配置将前端改到80端口

### 4.4 Nginx未启动

**错误现象**: 无法访问前端页面

**解决方案**:
```bash
# 检查Nginx状态
systemctl status nginx

# 启动Nginx
systemctl start nginx

# 设置开机自启
systemctl enable nginx
```

## 5. 一键部署脚本

在项目根目录创建 `deploy.sh`:

```bash
#!/bin/bash

echo "=== Lottery Java 项目部署 ==="

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'

# 1. 打包后端
echo "1/4 打包后端..."
mvn clean package -DskipTests -q
if [ $? -ne 0 ]; then
    echo -e "${RED}后端打包失败${NC}"
    exit 1
fi
echo -e "${GREEN}✓ 后端打包成功${NC}"

# 2. 打包前端
echo "2/4 打包前端..."
cd frontend
npm install --silent
npm run build > /dev/null 2>&1
if [ $? -ne 0 ]; then
    echo -e "${RED}前端打包失败${NC}"
    exit 1
fi
cd ..
echo -e "${GREEN}✓ 前端打包成功${NC}"

# 3. 上传文件
echo "3/4 上传文件到服务器..."
scp target/lottery-java-1.0-SNAPSHOT.jar server01:/opt/lottery/app/lottery-java.jar
scp -r frontend/dist/* server01:/usr/share/nginx/html/lottery/
echo -e "${GREEN}✓ 文件上传成功${NC}"

# 4. 重启服务
echo "4/4 重启服务..."
ssh server01 << 'EOF'
# 停止旧进程
pkill -f lottery-java.jar 2>/dev/null
sleep 2

# 启动后端
cd /opt/lottery/app
nohup java -Xms512m -Xmx1024m -jar lottery-java.jar --server.port=9060 > app.log 2>&1 &

# 重启Nginx
systemctl restart nginx

# 等待启动
sleep 8

# 验证
if netstat -tlnp | grep -q 9060; then
    echo -e "\033[0;32m✓ 后端服务运行正常 (端口9060)\033[0m"
else
    echo -e "\033[0;31m✗ 后端服务启动失败\033[0m"
    tail -30 /opt/lottery/app/app.log
fi

if systemctl is-active --quiet nginx; then
    echo -e "\033[0;32m✓ Nginx运行正常\033[0m"
else
    echo -e "\033[0;31m✗ Nginx启动失败\033[0m"
fi
EOF

echo ""
echo "=== 部署完成 ==="
echo "前端页面: http://server01:8080"
echo "后端API: http://server01:9060"
```

使用:
```bash
chmod +x deploy.sh
./deploy.sh
```

## 6. 服务管理命令

```bash
# 后端服务
ssh server01 "cd /opt/lottery/app && java -Xms512m -Xmx1024m -jar lottery-java.jar --server.port=9060"  # 启动
ps aux | grep lottery-java.jar | awk '{print $2}' | xargs -r kill -9                         # 停止
tail -f /opt/lottery/app/app.log                                                            # 查看日志

# Nginx服务
ssh server01 "systemctl start nginx"   # 启动
ssh server01 "systemctl stop nginx"    # 停止
ssh server01 "systemctl restart nginx" # 重启
ssh server01 "systemctl status nginx"  # 状态
```

## 7. 配置信息

| 项目 | 值 |
|------|-----|
| 虚拟机地址 | server01 |
| 后端端口 | 9060 |
| 前端端口 | 8080 |
| 前端文件目录 | /usr/share/nginx/html/lottery/ |
| 后端JAR目录 | /opt/lottery/app/lottery-java.jar |
| 后端日志 | /opt/lottery/app/app.log |

## 8. 验证清单

部署完成后，依次检查:

- [ ] `netstat -tlnp | grep 9060` → Java进程监听9060端口
- [ ] `curl http://localhost:9060/` → 后端API可访问
- [ ] `systemctl status nginx` → Nginx运行中
- [ ] `curl http://server01:8080/` → 前端页面可访问
- [ ] `curl http://server01:8080/api/xxx` → API代理正常工作
- [ ] 查看日志 `tail -20 /opt/lottery/app/app.log` 无报错
