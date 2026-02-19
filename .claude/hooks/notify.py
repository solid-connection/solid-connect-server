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
    subprocess.run([
        "powershell", "-NoProfile", "-ExecutionPolicy", "Bypass",
        "-File", ps1_path
    ])
