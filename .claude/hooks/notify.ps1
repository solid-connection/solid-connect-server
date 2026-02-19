Add-Type -AssemblyName System.Windows.Forms
$n = New-Object System.Windows.Forms.NotifyIcon
$n.Icon = [System.Drawing.SystemIcons]::Information
$n.Visible = $true
$n.BalloonTipTitle = "Claude Code"
$n.BalloonTipText = "Awaiting your input"
$n.ShowBalloonTip(5000)
Start-Sleep -Milliseconds 5100
$n.Dispose()
