SAP ERP 适配  v1.0.0

运行（需 JDK 8+，推荐 17）：
  chmod +x start.sh
  ./start.sh

浏览器打开 http://127.0.0.1:8085
默认账号（如有登录）：admin / admin123

数据文件写在当前目录 data/ 下。换端口：
  SERVER_PORT=9090 ./start.sh

默认启用 H2 内嵌库。接 MySQL：SPRING_PROFILES_ACTIVE= ./start.sh（并配置 DB_*）。
自检：
  ./smoke.sh

停止：Ctrl+C
