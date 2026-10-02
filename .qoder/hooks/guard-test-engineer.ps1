# PreToolUse Hook: 阻止 test-engineer 意外修改业务代码（非测试目录）
# 
# 触发条件：当 Write/Edit 工具调用尝试修改 src/main/ 或 web/src/ 时拦截
# 放行条件：目标文件在 src/test/、__tests__/ 或 *.test.* 等测试路径中
#
# 退出码说明：
#   0 = 放行（继续执行工具调用）
#   2 = 拦截（终止工具调用并返回错误）
#
# 输入格式：JSON via stdin
# { "tool_input": { "file_path": "..." } }

$ErrorActionPreference = "Stop"

try {
    $inputJson = [Console]::In.ReadLine()
    if (-not $inputJson) { exit 0 }
    
    $json = $inputJson | ConvertFrom-Json
    $file = $null
    
    # 兼容不同字段名
    if ($json.tool_input.PSObject.Properties.Name -contains 'file_path') {
        $file = $json.tool_input.file_path
    } elseif ($json.tool_input.PSObject.Properties.Name -contains 'path') {
        $file = $json.tool_input.path
    }
    
    # 如果无法提取文件路径，放行
    if (-not $file -or $file -eq '') {
        exit 0
    }
    
    # 检查是否为测试文件
    $testPatterns = @(
        'src/test/',
        '__tests__/',
        '.test.',
        'Test.java',
        'Tests.java',
        '.Spec.ts',
        '.spec.tsx'
    )
    
    foreach ($pattern in $testPatterns) {
        if ($file -match [regex]::Escape($pattern)) {
            exit 0
        }
    }
    
    # 检查是否为业务代码路径
    $businessPatterns = @(
        'src/main/',
        'web/src/'
    )
    
    foreach ($pattern in $businessPatterns) {
        if ($file -match [regex]::Escape($pattern)) {
            Write-Host "BLOCKED: test-engineer 不得修改业务代码（$file）。" -ForegroundColor Red
            Write-Host "发现缺陷请在'偏差与风险'中报告，由主 Agent 派对应工程师修复。" -ForegroundColor Yellow
            exit 2
        }
    }
    
    # 其他路径放行（如写文档、契约、测试报告等）
    exit 0
} catch {
    # 解析错误时放行，避免阻塞正常工作流
    exit 0
}
