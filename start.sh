#!/bin/bash
# =============================================================================
# TaskBoard 一键启动脚本（Linux/macOS）
# 并行启动后端（Spring Boot）和前端（Vite Dev Server）
# =============================================================================

set -e

echo "========================================"
echo "  TaskBoard 一键启动"
echo "========================================"
echo ""

# 检查前置依赖
echo "[检查] 验证运行环境..."

if ! command -v java &> /dev/null; then
    echo "[错误] 未找到 Java，请先安装 JDK 25+"
    echo "参考: .qoder/rules/00-project-charter.md"
    exit 1
fi

java_version=$(java -version 2>&1 | head -n 1)
echo "$java_version" | grep -qE 'version [2-9][0-9]|version 2[0-9]+' || {
    echo "[错误] Java 版本不符合要求（需要 JDK 25+）"
    exit 1
}

if ! command -v node &> /dev/null; then
    echo "[错误] 未找到 Node.js，请先安装 Node.js 18+"
    echo "参考: .qoder/rules/00-project-charter.md"
    exit 1
fi

if ! command -v npm &> /dev/null; then
    echo "[错误] 未找到 npm，请检查 Node.js 安装"
    exit 1
fi

echo "[通过] Java、Node.js、npm 已就绪"
echo ""

# 安装前端依赖（如尚未安装）
cd "$(dirname "$0")/web"
if [ ! -d "node_modules" ]; then
    echo "[准备] 安装前端依赖..."
    npm install
    if [ $? -ne 0 ]; then
        echo "[错误] 前端依赖安装失败"
        exit 1
    fi
fi

cd "$(dirname "$0")"

# 启动后端（后台进程）
echo "[启动] 后端服务（Spring Boot, http://localhost:8081）..."
nohup ./server/gradlew bootRun > /tmp/taskboard-backend.log 2>&1 &
BACKEND_PID=$!

# 等待后端初始化
echo "[等待] 后端启动中（约 10-15 秒）..."
sleep 10

# 启动前端（后台进程）
echo "[启动] 前端服务（Vite Dev, http://localhost:5173）..."
cd web
nohup npm run dev > /tmp/taskboard-frontend.log 2>&1 &
FRONTEND_PID=$!
cd ..

echo ""
echo "========================================"
echo "  启动完成！"
echo "========================================"
echo ""
echo "  后端地址: http://localhost:8081"
echo "  前端地址: http://localhost:5173"
echo "  H2 Console: http://localhost:8081/h2-console"
echo ""
echo "  健康检查: curl http://localhost:8081/api/health"
echo ""
echo "  查看日志:"
echo "    后端: tail -f /tmp/taskboard-backend.log"
echo "    前端: tail -f /tmp/taskboard-frontend.log"
echo ""
echo "  停止服务:"
echo "    kill $BACKEND_PID $FRONTEND_PID"
echo "    或删除 /tmp/taskboard-*.log"
echo ""
