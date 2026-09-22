#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""E志愿志愿者服务平台 V1.0 源程序文档生成工具。

按计算机软件著作权登记对源程序提交格式的要求，把项目自身的源程序整理为
一份连续文档：

* 每页固定行数（默认 50 行，符合“每页不少于 50 行”的要求）；
* 每页页首输出页眉，页眉包含软件名称、版本号与页码；
* 每个源码文件前输出文件名，便于审查时定位；
* 可选只输出前 30 页与后 30 页，用于按“前后各连续 30 页”方式提交。

用法示例：

    python3 tools/source_doc.py --output docs/源程序.txt
    python3 tools/source_doc.py --output docs/源程序.txt --lines-per-page 50 --head-tail 30
"""

import argparse
import sys
from pathlib import Path

SOFTWARE_NAME = "E志愿志愿者服务平台"
SOFTWARE_VERSION = "V1.0"

# 参与源程序文档的源码目录或文件，按提交顺序排列
SOURCE_PATTERNS = [
    ("Java 源程序", "src/main/java/**/*.java"),
    ("MyBatis 映射文件", "src/main/resources/mapper/*.xml"),
    ("系统配置", "src/main/resources/application.properties"),
    ("数据库脚本", "src/main/resources/db/*.sql"),
    ("前端页面", "src/main/resources/templates/**/*.html"),
    ("单元测试", "src/test/java/**/*.java"),
]


def collect_files(project_root: Path):
    """按预设顺序收集源程序文件。"""
    collected = []
    for group_name, pattern in SOURCE_PATTERNS:
        for path in sorted(project_root.glob(pattern)):
            if path.is_file():
                collected.append((group_name, path))
    return collected


def build_lines(project_root: Path, files):
    """把源码文件展开为文档正文行。"""
    lines = []
    for index, (group_name, path) in enumerate(files, start=1):
        relative = path.relative_to(project_root).as_posix()
        lines.append("=" * 96)
        lines.append("源码文件 %d：%s（%s）" % (index, relative, group_name))
        lines.append("=" * 96)
        text = path.read_text(encoding="utf-8", errors="replace")
        body = text.split("\n")
        # 去掉文件末尾由换行产生的空行，保持页数统计稳定
        while body and body[-1] == "":
            body.pop()
        lines.extend(body)
        lines.append("")
    return lines


def paginate(lines, software_name, software_version, lines_per_page):
    """按每页固定行数分页，并为每页添加页眉。"""
    pages = []
    total = len(lines)
    for start in range(0, total, lines_per_page):
        page_lines = lines[start:start + lines_per_page]
        page_no = start // lines_per_page + 1
        header = "%s %s" % (software_name, software_version)
        header = header + " " * max(1, 84 - len(header) * 2) + "第 %d 页" % page_no
        pages.append([header] + page_lines)
    return pages


def main(argv=None):
    parser = argparse.ArgumentParser(description="生成源程序文档")
    parser.add_argument("--project", default=".", help="项目根目录，默认为当前目录")
    parser.add_argument("--output", default="源程序.txt", help="输出文件路径")
    parser.add_argument("--lines-per-page", type=int, default=50, help="每页正文行数，默认 50")
    parser.add_argument("--head-tail", type=int, default=0,
                        help="仅输出前 N 页与后 N 页（用于前后各连续 30 页的提交方式），默认输出全部页面")
    parser.add_argument("--name", default=SOFTWARE_NAME, help="软件名称")
    parser.add_argument("--version", default=SOFTWARE_VERSION, help="软件版本号")
    args = parser.parse_args(argv)

    project_root = Path(args.project).resolve()
    files = collect_files(project_root)
    if not files:
        print("未在 %s 下找到任何源程序文件" % project_root, file=sys.stderr)
        return 1

    lines = build_lines(project_root, files)
    pages = paginate(lines, args.name, args.version, args.lines_per_page)

    total_pages = len(pages)
    if args.head_tail and total_pages > args.head_tail * 2:
        selected = pages[:args.head_tail] + pages[-args.head_tail:]
        note = "按前后各 %d 页输出" % args.head_tail
    else:
        selected = pages
        note = "输出全部页面"

    output_lines = []
    for page in selected:
        output_lines.extend(page)

    output_path = Path(args.output)
    if output_path.parent and not output_path.parent.exists():
        output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text("\n".join(output_lines) + "\n", encoding="utf-8")

    print("软件名称：%s %s" % (args.name, args.version))
    print("源程序文件：%d 个" % len(files))
    print("源程序正文：%d 行" % len(lines))
    print("每页行数：%d 行" % args.lines_per_page)
    print("源程序共 %d 页（%s）" % (total_pages, note))
    print("已生成：%s" % output_path)
    return 0


if __name__ == "__main__":
    sys.exit(main())
