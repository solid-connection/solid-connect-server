#!/usr/bin/env python3
import os
import platform
import subprocess

system = platform.system()
script_dir = os.path.dirname(os.path.abspath(__file__))

if system == "Darwin":
    subprocess.run([
        "osascript", "-e",
        'display notification "Awaiting your input" with title "Cursor"'
    ])
elif system == "Windows":
    ps1_path = os.path.join(script_dir, "notify.ps1")
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
