#!/bin/bash
# PreToolUse Hook: 阻止 test-engineer 意外修改业务代码（非测试目录）
# 
# 触发条件：当 Write/Edit 工具调用尝试修改 src/main/ 或 web/src/ 时拦截
# 放行条件：目标文件在 src/test/、__tests__/ 或 *.test.* 等测试路径中
#
# 退出码说明：
#   0 = 放行（继续执行工具调用）
#   2 = 拦截（终止工具调用并返回错误）

input=$(cat)

# 从 JSON 输入中提取目标文件路径（兼容不同字段名）
file=$(echo "$input" | jq -r '.tool_input.file_path // .tool_input.path // empty')

# 如果无法提取文件路径，放行
if [ -z "$file" ]; then
    exit 0
fi

# 检查是否为测试文件
case "$file" in
    */src/test/*|*/__tests__/*|*\.test\.*|*Test\.java|*Tests\.java|*Spec\.ts|*spec\.tsx)
        # 测试目录/测试文件，放行
        exit 0
        ;;
esac

# 检查是否为业务代码路径
case "$file" in
    */src/main/*|*/web/src/*)
        # 业务代码，拦截
        echo "BLOCKED: test-engineer 不得修改业务代码（$file）。" >&2
        echo "发现缺陷请在'偏差与风险'中报告，由主 Agent 派对应工程师修复。" >&2
        exit 2
        ;;
esac

# 其他路径放行（如写文档、契约、测试报告等）
exit 0
