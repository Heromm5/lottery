#!/bin/bash

#  lottery-java 部署脚本
#  在虚拟机上执行此脚本

set -e

echo "========================================="
echo "  大乐透数据分析系统 - 分离部署脚本"
echo "========================================="

# 配置变量
BACKEND_PORT=9060
FRONTEND_DIR=/usr/share/nginx/html/lottery
BACKEND_DIR=/root/lottery-java
BACKEND_JAR=$BACKEND_DIR/target/lottery-java-1.0-SNAPSHOT.jar

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
 "${YELLOW}[    echo -eWARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查是否以 root 用户运行
if [ "$EUID" -ne 0 ]; then
    log_warn "建议以 root 用户运行此脚本"
fi

# 步骤 1: 检查 Java
log_info "检查 Java 环境..."
if ! command -v java &> /dev/null; then
    log_error "Java 未安装，请先安装 JDK 8+"
    exit 1
fi
java -version
log_info "Java 检查通过"

# 步骤 2: 检查 Nginx
log_info "检查 Nginx..."
if ! command -v nginx &> /dev/null; then
    log_warn "Nginx 未安装，正在安装..."
    apt-get update && apt-get install -y nginx
fi
nginx -v
log_info "Nginx 检查通过"

# 步骤 3: 检查 MySQL
log_info "检查 MySQL..."
if ! command -v mysql &> /dev/null; then
    log_warn "MySQL 客户端未安装，尝试安装..."
    apt-get install -y mysql-client
fi
log_info "MySQL 检查通过"

# 步骤 4: 创建目录
log_info "创建部署目录..."
mkdir -p $FRONTEND_DIR
mkdir -p $BACKEND_DIR

# 步骤 5: 从本地传输文件
# 如果本地有 frontend.tar 和 backend.tar，则解压
if [ -f "frontend.tar" ]; then
    log_info "解压前端文件..."
    tar -xf frontend.tar -C $FRONTEND_DIR
    log_info "前端文件已部署到 $FRONTEND_DIR"
else
    log_warn "未找到 frontend.tar，将从 GitHub 拉取代码构建"
fi

if [ -f "backend.tar" ]; then
    log_info "解压后端文件..."
    tar -xf backend.tar -C $BACKEND_DIR
else
    log_warn "未找到 backend.tar，将从 GitHub 拉取代码构建"
fi

# 步骤 6: 配置 Nginx
log_info "配置 Nginx..."

cat > /etc/nginx/sites-available/lottery << 'EOF'
server {
    listen 80;
    server_name localhost;

    # 前端静态文件
    root /usr/share/nginx/html/lottery;
    index index.html;

    # 前端路由支持
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API 反向代理到后端
    location /api/ {
        proxy_pass http://127.0.0.1:9060/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # WebSocket 支持（如果需要）
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
EOF

# 启用站点
ln -sf /etc/nginx/sites-available/lottery /etc/nginx/sites-enabled/lottery
rm -f /etc/nginx/sites-enabled/default

# 测试 Nginx 配置
nginx -t

# 重载 Nginx
systemctl reload nginx
log_info "Nginx 配置完成"

# 步骤 7: 停止旧的后端服务
log_info "停止旧的后端服务..."
if pgrep -f "lottery-java" > /dev/null; then
    pkill -f "lottery-java"
    sleep 2
    log_info "旧服务已停止"
fi

# 步骤 8: 启动后端服务
log_info "启动后端服务..."

# 创建 systemd 服务
cat > /etc/systemd/system/lottery.service << 'EOF'
[Unit]
Description=大乐透数据分析系统后端服务
After=network.target mysql.service

[Service]
Type=simple
User=root
WorkingDirectory=/root/lottery-java
ExecStart=/usr/bin/java -jar /root/lottery-java/target/lottery-java-1.0-SNAPSHOT.jar --server.port=9060
Restart=always
RestartSec=10

# 环境变量
Environment="JAVA_OPTS=-Xms256m -Xmx512m"

# 日志
StandardOutput=append:/var/log/lottery.log
StandardError=append:/var/log/lottery-error.log

[Install]
WantedBy=multi-user.target
EOF

# 重新加载 systemd
systemctl daemon-reload

# 启动服务
systemctl start lottery
systemctl enable lottery

log_info "后端服务已启动"

# 步骤 9: 等待服务启动
log_info "等待服务启动..."
sleep 5

# 步骤 10: 检查服务状态
log_info "检查服务状态..."

# 检查后端
if systemctl is-active --quiet lottery; then
    log_info "✅ 后端服务运行正常"
else
    log_error "❌ 后端服务启动失败"
    journalctl -u lottery --no-pager -n 20
fi

# 检查 Nginx
if systemctl is-active --quiet nginx; then
    log_info "✅ Nginx 运行正常"
else
    log_error "❌ Nginx 启动失败"
fi

# 检查端口
log_info "检查端口占用..."
netstat -tlnp | grep -E "(:80|:9060)"

# 步骤 11: 显示部署信息
echo ""
echo "========================================="
echo "  部署完成！"
echo "========================================="
echo ""
echo "📱 访问地址: http://192.168.202.101"
echo ""
echo "🔧 服务管理命令:"
echo "   后端: systemctl status lottery"
echo "   后端: systemctl restart lottery"
echo "   后端: journalctl -u lottery -f"
echo ""
echo "📝 日志位置:"
echo "   后端: /var/log/lottery.log"
echo "   Nginx: /var/log/nginx/"
echo ""
