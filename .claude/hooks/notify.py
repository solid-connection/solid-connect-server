#!/usr/bin/env python3
import os
import platform
import subprocess

system = platform.system()
script_dir = os.path.dirname(os.path.abspath(__file__))

if system == "Darwin":
    subprocess.run([
        "osascript", "-e",
        'display notification "Awaiting your input" with title "Claude Code"'
    ])
elif system == "Windows":
    ps1_path = os.path.join(script_dir, "notify.ps1")
    # VS Code extension 환경에서는 PATH에 powershell이 없을 수 있으므로 절대 경로 사용
    powershell_candidates = [
        r"C:\Windows\System32\WindowsPowerShell\v1.0\powershell.exe",
        "powershell",
    ]
    for ps in powershell_candidates:
        try:
            subprocess.run(
                [ps, "-NoProfile", "-ExecutionPolicy", "Bypass", "-File", ps1_path],
                timeout=10,
            )
            break
        except (FileNotFoundError, subprocess.TimeoutExpired):
            continue
