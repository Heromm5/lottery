# 快速参考

## 一键部署

```bash
cd frontend && npm run build && cp -r dist/* ../src/main/resources/static/ && cd .. && mvn clean package -DskipTests && scp target/lottery-java-1.0-SNAPSHOT.jar server01:/opt/lottery/app/lottery-java.jar && ssh server01 "pkill -f lottery-java.jar; sleep 2; cd /opt/lottery/app && nohup java -Xms512m -Xmx1024m -jar lottery-java.jar --server.port=9060 > app.log 2>&1 &"
```

## 常用命令

| 操作 | 命令 |
|------|------|
| 连接服务器 | `ssh server01` |
| 查看进程 | `ssh server01 "ps aux \| grep java"` |
| 查看端口 | `ssh server01 "netstat -tlnp \| grep 9060"` |
| 查看日志 | `ssh server01 "tail -50 /opt/lottery/app/app.log"` |
| 停止应用 | `ssh server01 "pkill -f lottery-java.jar"` |
| 启动应用 | `ssh server01 "cd /opt/lottery/app && nohup java -Xms512m -Xmx1024m -jar lottery-java.jar --server.port=9060 > app.log 2>&1 &"` |

## 访问地址

- **前端**: http://192.168.202.101:9060/
- **API**: http://192.168.202.101:9060/api/

## 快速排查

```bash
# 1. 检查进程
ssh server01 "ps aux | grep java"

# 2. 检查端口
ssh server01 "netstat -tlnp | grep 9060"

# 3. 检查日志
ssh server01 "tail -30 /opt/lottery/app/app.log"

# 4. 测试 API
curl http://192.168.202.101:9060/api/lottery/latest
```

## 关键路径

| 项目 | 路径 |
|------|------|
| JAR | /opt/lottery/app/lottery-java.jar |
| 日志 | /opt/lottery/app/app.log |
| 源码 | E:\MyCode\MyThought\lottery-java |
| 部署文档 | DEPLOYMENT.md |
