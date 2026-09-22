#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""把已经分页好的 HTML 打印为 PDF（无头 Chromium）。

页面尺寸、页眉与页码全部由 HTML 自身的样式决定，本模块只负责找到浏览器、
把 HTML 写入临时文件并调用 Chromium 的 --print-to-pdf 打印，因此不依赖第三方 Python 库。

Chromium 可执行文件的查找顺序：

1. 环境变量 CHROMIUM_BIN 指定的路径；
2. 系统 PATH 中的 chromium / chromium-browser / google-chrome / microsoft-edge；
3. Playwright 与 Puppeteer 在本机的浏览器缓存。

用法示例：

    python3 tools/pdf_render.py 输入.html 输出.pdf
"""

import argparse
import glob
import os
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

ENV_VAR = "CHROMIUM_BIN"

# 系统命令名，按顺序尝试
COMMAND_NAMES = ["chromium", "chromium-browser", "google-chrome", "google-chrome-stable",
                 "microsoft-edge", "microsoft-edge-stable"]

# Playwright 与 Puppeteer 的浏览器缓存位置
CACHED_PATTERNS = [
    "~/.cache/ms-playwright/chromium-*/chrome-linux64/chrome",
    "~/.cache/ms-playwright/chromium-*/chrome-linux/chrome",
    "~/.cache/puppeteer/chrome/*/chrome-linux64/chrome",
    "~/.cache/puppeteer/chrome-headless-shell/*/chrome-headless-shell-linux64/chrome-headless-shell",
    "~/Library/Caches/ms-playwright/chromium-*/chrome-mac/Chromium.app/Contents/MacOS/Chromium",
]

# 打印 PDF 的启动参数：无头、禁用沙箱相关的系统调用、不输出浏览器自带的页眉页脚。
# 这里不使用 --user-data-dir 指定临时配置目录：在 WSL 的临时目录下，Chromium 会卡在启动
# 阶段不返回（实测始终超时），使用默认配置目录可以正常打印。
PRINT_FLAGS = [
    "--headless",
    "--disable-gpu",
    "--no-sandbox",
    "--disable-dev-shm-usage",
    "--no-pdf-header-footer",
    "--virtual-time-budget=8000",
]


def find_chromium():
    """返回可用的 Chromium 可执行文件路径，找不到时返回 None。"""
    configured = os.environ.get(ENV_VAR)
    if configured:
        path = Path(configured).expanduser()
        if path.is_file():
            return str(path)
        raise RuntimeError("环境变量 %s 指向的浏览器不存在：%s" % (ENV_VAR, configured))
    for name in COMMAND_NAMES:
        found = shutil.which(name)
        if found:
            return found
    for pattern in CACHED_PATTERNS:
        for match in sorted(glob.glob(str(Path(pattern).expanduser()))):
            if os.access(match, os.X_OK):
                return match
    return None


def render_html(html_text, output_path, chromium=None, timeout=300):
    """把 HTML 文本渲染为 PDF 并写入 output_path，返回输出路径。"""
    output_path = Path(output_path).expanduser()
    output_path.parent.mkdir(parents=True, exist_ok=True)
    chrome = chromium or find_chromium()
    if chrome is None:
        raise RuntimeError(
            "未找到 Chromium 可执行文件，请安装 Chromium 或用环境变量 %s 指定其路径" % ENV_VAR)

    with tempfile.TemporaryDirectory(prefix="evolunteer-pdf-") as workspace:
        html_path = Path(workspace) / "document.html"
        html_path.write_text(html_text, encoding="utf-8")
        command = [
            chrome,
            *PRINT_FLAGS,
            "--print-to-pdf=%s" % output_path.resolve(),
            html_path.as_uri(),
        ]
        result = subprocess.run(command, capture_output=True, text=True, timeout=timeout)
        if result.returncode != 0 or not output_path.is_file():
            raise RuntimeError("Chromium 打印 PDF 失败（退出码 %d）：\n%s\n%s"
                               % (result.returncode, result.stdout[-2000:], result.stderr[-2000:]))
    return output_path


def main(argv=None):
    parser = argparse.ArgumentParser(description="把已分页的 HTML 打印为 PDF")
    parser.add_argument("html", help="输入 HTML 文件")
    parser.add_argument("output", help="输出 PDF 文件")
    parser.add_argument("--chromium", default="", help="Chromium 可执行文件路径，默认自动查找")
    args = parser.parse_args(argv)

    html_path = Path(args.html)
    if not html_path.is_file():
        print("输入文件不存在：%s" % html_path, file=sys.stderr)
        return 1
    print("使用浏览器：%s" % (args.chromium or find_chromium()))
    render_html(html_path.read_text(encoding="utf-8"), Path(args.output), chromium=args.chromium or None)
    print("已生成：%s" % args.output)
    return 0


if __name__ == "__main__":
    sys.exit(main())
