# 验证 Hook 脚本是否能正确拦截/放行

$ErrorActionPreference = "Stop"
Set-Location d:\test\test1-qoder

Write-Host "`n=== Hook 验证测试 ===" -ForegroundColor Cyan

$testCases = @(
    @{
        Name = "业务代码拦截";
        Input = '{"tool_input":{"file_path":"server/src/main/java/com/taskboard/service/TaskService.java"}}';
        Expected = 2; # 应该被拦截
        ShouldBlock = $true
    },
    @{
        Name = "前端业务代码拦截";
        Input = '{"tool_input":{"file_path":"web/src/pages/BoardPage.tsx"}}';
        Expected = 2;
        ShouldBlock = $true
    },
    @{
        Name = "测试文件放行";
        Input = '{"tool_input":{"file_path":"server/src/test/java/com/taskboard/controller/TaskServiceTest.java"}}';
        Expected = 0;
        ShouldBlock = $false
    },
    @{
        Name = "文档路径放行";
        Input = '{"tool_input":{"file_path":"docs/agent-audit.md"}}';
        Expected = 0;
        ShouldBlock = $false
    }
)

$results = @()

foreach ($tc in $testCases) {
    Write-Host "`n测试: $($tc.Name)" -ForegroundColor Yellow
    Write-Host "输入: $($tc.Input.Substring(0, [Math]::Min(80, $tc.Input.Length)))..." -ForegroundColor Gray
    
    # 使用 PowerShell 执行 Hook
    $scriptPath = Join-Path $PWD ".qoder\hooks\guard-test-engineer.ps1"
    
    try {
        $output = @()
        $exitCode = 0
        
        # 模拟 stdin 输入并捕获 stdout/stderr
        $processStartInfo = New-Object System.Diagnostics.ProcessStartInfo
        $processStartInfo.FileName = "powershell.exe"
        $processStartInfo.Arguments = "-NoProfile -ExecutionPolicy Bypass -File `"$scriptPath`""
        $processStartInfo.UseShellExecute = $false
        $processStartInfo.RedirectStandardInput = $true
        $processStartInfo.RedirectStandardOutput = $true
        $processStartInfo.RedirectStandardError = $true
        
        $process = [System.Diagnostics.Process]::Start($processStartInfo)
        $process.StandardInput.WriteLine($tc.Input)
        $process.StandardInput.Close()
        
        $stdout = $process.StandardOutput.ReadToEnd()
        $stderr = $process.StandardError.ReadToEnd()
        $process.WaitForExit()
        $exitCode = $process.ExitCode
        
        $results += @{
            TestCase = $tc.Name
            Expected = $tc.Expected
            Actual = $exitCode
            Matched = ($exitCode -eq $tc.Expected)
            Output = "$stdout$stderr"
        }
        
        if ($tc.ShouldBlock -and $exitCode -eq 2) {
            Write-Host "✅ 正确拦截" -ForegroundColor Green
            if ($stdout) { Write-Host "   输出: $stdout" -ForegroundColor DarkGray }
        } elseif ($exitCode -eq 0) {
            Write-Host "✅ 正确放行" -ForegroundColor Green
        } else {
            Write-Host "❌ 失败 (期望:$($tc.Expected), 实际:$exitCode)" -ForegroundColor Red
        }
    } catch {
        Write-Host "❌ 执行错误: $_" -ForegroundColor Red
        $results += @{
            TestCase = $tc.Name
            Expected = $tc.Expected
            Actual = -1
            Matched = $false
            Output = $_.Exception.Message
        }
    }
}

# 汇总结果
Write-Host "`n=== 验证汇总 ===" -ForegroundColor Cyan
$passed = ($results | Where-Object { $_.Matched }).Count
$total = $results.Count

Write-Host "通过: $passed/$total" -ForegroundColor $(if ($passed -eq $total) {"Green"} else {"Red"})

foreach ($r in $results) {
    if (-not $r.Matched) {
        Write-Host "`n失败的测试: $($r.TestCase)" -ForegroundColor Red
        Write-Host "  期望退出码: $($r.Expected)" -ForegroundColor Yellow
        Write-Host "  实际退出码: $($r.Actual)" -ForegroundColor Yellow
        if ($r.Output) {
            Write-Host "  输出: $($r.Output)" -ForegroundColor DarkGray
        }
    }
}

if ($passed -eq $total) {
    Write-Host "`n🎉 所有测试通过！Hook 机制正常工作。" -ForegroundColor Green
} else {
    Write-Host "`n⚠️ 有 $($total - $passed) 个测试失败，需要修复 Hook 脚本。" -ForegroundColor Red
}
