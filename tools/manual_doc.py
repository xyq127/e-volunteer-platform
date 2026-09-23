#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""E志愿志愿服务管理系统 V1.0 软件说明书排版工具。

把 Markdown 版软件说明书排版为 A4 页面的 HTML，再调用无头 Chromium 打印为 PDF：

* 每页页眉为软件名称与版本号（左侧）与页码（右侧），与申请表“软件全称”“版本号”一致；
* 正文在页面外按打印时的同样宽度与字号量高，再按块排进固定高度的页面，页面排满才换页；
* 一张插图不单独占页，插图所在页继续排入后续正文，使每页正文行数尽量多；
* 超过一整页的表格、列表与代码块会在分页处拆分，不会丢内容。

用法示例：

    python3 tools/manual_doc.py
    python3 tools/manual_doc.py --input docs/软件说明书.md --output docs/软件说明书.pdf
"""

import argparse
import html
import re
import sys
from pathlib import Path

import markdown

import pdf_render

SOFTWARE_NAME = "E志愿志愿服务管理系统"
SOFTWARE_VERSION = "V1.0"

# 页面参数，与下面的样式保持一致
PAGE_WIDTH_MM = 210.0
PAGE_HEIGHT_MM = 297.0
PAGE_PADDING_TOP_MM = 17.0
PAGE_PADDING_BOTTOM_MM = 12.0
PAGE_PADDING_SIDE_MM = 15.0
IMAGE_MAX_HEIGHT_MM = 58.0

MANUAL_STYLE = """
@page { size: A4; margin: 0; }
html, body { margin: 0; padding: 0; background: #ffffff; }
/* 待排版的正文先放在页面外，用与打印相同的宽度与字体量高 */
#content { position: absolute; left: -400mm; top: 0; width: 180mm; }
.sheet-body { font-family: "Noto Sans SC", "WenQuanYi Zen Hei", "Microsoft YaHei", sans-serif;
              font-size: 10.5pt; line-height: 1.5; color: #000000; }
.sheet-body h1 { font-size: 16pt; margin: 0 0 4mm; }
.sheet-body h2 { font-size: 13pt; margin: 4mm 0 2.5mm; }
.sheet-body h3 { font-size: 11.5pt; margin: 3mm 0 2mm; }
.sheet-body p { margin: 0 0 2mm; }
.sheet-body ul, .sheet-body ol { margin: 0 0 2mm; padding-left: 6mm; }
.sheet-body li { margin: 0 0 1mm; }
.sheet-body table { border-collapse: collapse; width: 100%; margin: 0 0 3mm; font-size: 9.5pt; }
.sheet-body th, .sheet-body td { border: 0.5pt solid #888888; padding: 1mm 1.6mm;
                                 text-align: left; vertical-align: top; }
.sheet-body th { background: #f2f2f2; }
.sheet-body table:has(td:nth-child(2):last-child) td:first-child { white-space: nowrap; }
.sheet-body pre { margin: 0 0 3mm; padding: 2mm; background: #f5f5f5; font-size: 8.5pt;
                  white-space: pre-wrap; word-break: break-all; }
.sheet-body code { font-family: "DejaVu Sans Mono", "Liberation Mono", Consolas, monospace; }
.sheet-body blockquote { margin: 0 0 3mm; padding-left: 3mm;
                         border-left: 2pt solid #cccccc; color: #333333; }
.sheet-body img { max-width: 100%; max-height: 58mm; display: block; margin: 2mm auto; }
.page { width: 210mm; height: 297mm; box-sizing: border-box; padding: 17mm 15mm 12mm;
        position: relative; overflow: hidden; break-after: page; }
.page:last-child { break-after: auto; }
.page .hd { position: absolute; top: 7mm; left: 15mm; right: 15mm; display: flex;
            justify-content: space-between; padding-bottom: 1.2mm;
            border-bottom: 0.4pt solid #666666;
            font-family: "Noto Sans SC", "WenQuanYi Zen Hei", sans-serif; font-size: 9pt; }
.page .body { width: 100%; height: 100%; overflow: hidden; }
"""

# 分页脚本：在 Chromium 打印前把正文按块排进固定高度的页面，并写入页眉与页码
MANUAL_PAGER = """
(function () {
  // 插图未加载完时量到的高度不作数，因此等页面（含全部图片）加载完成后再分页
  function layout() {
    var source = document.getElementById('content');
    var pages = document.getElementById('pages');
    var current = null, body = null;

    // 量高用的容器：与打印页面同宽同字号，拆分超长块时使用
    var host = document.createElement('div');
    host.className = 'sheet-body';
    host.style.cssText = 'position:absolute;left:-400mm;top:0;width:180mm;';
    document.body.appendChild(host);

    function measure(node) {
      host.appendChild(node);
      var height = node.offsetHeight;
      host.removeChild(node);
      return height;
    }

    function startPage() {
      var page = document.createElement('div');
      page.className = 'page';
      var head = document.createElement('div');
      head.className = 'hd';
      head.innerHTML = '<span class="name">__TITLE__</span><span class="pn"></span>';
      body = document.createElement('div');
      body.className = 'body sheet-body';
      page.appendChild(head);
      page.appendChild(body);
      pages.appendChild(page);
      current = page;
    }

    function usedHeight() {
      if (!body.childElementCount) { return 0; }
      return body.lastElementChild.getBoundingClientRect().bottom - body.getBoundingClientRect().top;
    }

    function overflowOf(target) {
      var last = target.lastElementChild;
      if (!last) { return 0; }
      return last.getBoundingClientRect().bottom - target.getBoundingClientRect().top - target.clientHeight;
    }

    // 估算一页的正文行数，用于把末尾页凑足行数（每页不少于 30 行）
    function countLines(target) {
      var total = 0;
      target.querySelectorAll('p, li, h1, h2, h3, h4, td, th, blockquote, pre').forEach(function (node) {
        var lineHeight = parseFloat(getComputedStyle(node).lineHeight) || 21;
        total += Math.max(1, Math.round(node.offsetHeight / lineHeight));
      });
      return total;
    }

    function cloneShallow(node) { return node.cloneNode(false); }

    // 拆分放不下一整页的表格、列表与代码块，避免内容被裁掉
    function splitBlock(block) {
      var limit = body.clientHeight;
      var tag = block.tagName;
      if (tag === 'TABLE') {
        var tbody = block.tBodies[0];
        if (!tbody) { return [block]; }
        var headRow = block.tHead ? block.tHead.cloneNode(true) : null;
        var parts = [], piece = null, pieceBody = null;
        Array.prototype.slice.call(tbody.rows).forEach(function (row) {
          if (!piece) {
            piece = cloneShallow(block);
            if (headRow) { piece.appendChild(headRow.cloneNode(true)); }
            pieceBody = document.createElement('tbody');
            piece.appendChild(pieceBody);
            parts.push(piece);
          }
          pieceBody.appendChild(row.cloneNode(true));
          if (measure(piece) > limit && pieceBody.rows.length > 1) {
            pieceBody.removeChild(pieceBody.lastChild);
            piece = null;
          }
        });
        return parts;
      }
      if (tag === 'UL' || tag === 'OL') {
        var lists = [], list = null;
        Array.prototype.slice.call(block.children).forEach(function (item) {
          if (!list) { list = cloneShallow(block); lists.push(list); }
          list.appendChild(item.cloneNode(true));
          if (measure(list) > limit && list.children.length > 1) {
            list.removeChild(list.lastChild);
            list = null;
          }
        });
        return lists;
      }
      if (tag === 'PRE') {
        var chunks = [], chunk = [];
        block.textContent.split('\\n').forEach(function (line) {
          chunk.push(line);
          block.textContent = chunk.join('\\n');
          if (measure(block) > limit && chunk.length > 1) {
            chunk.pop();
            chunks.push(chunk.join('\\n'));
            chunk = [line];
          }
        });
        if (chunk.length) { chunks.push(chunk.join('\\n')); }
        return chunks.map(function (part) {
          var clone = cloneShallow(block);
          clone.textContent = part;
          return clone;
        });
      }
      return [block];
    }

    // 正文块留在 #content 中量高，逐块搬入页面：量好高度再决定是否换页
    while (source.firstElementChild) {
      var block = source.firstElementChild;
      if (!body) { startPage(); }
      var height = block.offsetHeight + (parseFloat(getComputedStyle(block).marginTop) || 0);
      var used = usedHeight();
      // 留 8 像素余量，避免行高取整造成末行被裁
      if (body.childElementCount > 0 && used + height + 8 > body.clientHeight) {
        startPage();
      }
      if (height > body.clientHeight) {
        var parts = splitBlock(block);
        if (parts.length > 1) {
          parts.forEach(function (part) { source.insertBefore(part, block); });
          source.removeChild(block);
          continue;
        }
      }
      body.appendChild(block);
    }

    if (body && body.childElementCount === 0) { pages.removeChild(current); }
    document.body.removeChild(host);

    // 末尾页不足 30 行时，从上一页末尾搬入正文块补足，阅读顺序保持不变
    var MIN_PAGE_LINES = 30;
    var pageList = Array.prototype.slice.call(pages.children);
    if (pageList.length > 1) {
      var lastBody = pageList[pageList.length - 1].querySelector('.body');
      var prevBody = pageList[pageList.length - 2].querySelector('.body');
      while (prevBody.childElementCount > 1 && countLines(lastBody) < MIN_PAGE_LINES
             && countLines(prevBody) > MIN_PAGE_LINES) {
        var moved = prevBody.lastElementChild;
        lastBody.insertBefore(moved, lastBody.firstElementChild);
        if (overflowOf(lastBody) > 0) {
          prevBody.appendChild(moved);
          break;
        }
      }
    }

    Array.prototype.slice.call(pages.children).forEach(function (page, index) {
      page.querySelector('.pn').textContent = '第 ' + (index + 1) + ' 页';
    });
    document.body.setAttribute('data-pages', String(pages.children.length));
    document.body.setAttribute('data-layout', 'done');
  }

  if (document.readyState === 'complete') {
    layout();
  } else {
    window.addEventListener('load', layout);
  }
})();
"""


def build_html(markdown_text, images_uri, software_name, software_version):
    """把说明书 Markdown 渲染为待打印 HTML。"""
    body = markdown.markdown(
        markdown_text,
        extensions=["tables", "fenced_code", "sane_lists", "attr_list"],
        output_format="html5",
    )
    body = re.sub(r'src="images/', 'src="%s/' % images_uri, body)
    title = html.escape("%s %s 软件说明书" % (software_name, software_version))
    pager = MANUAL_PAGER.replace("__TITLE__", html.escape("%s %s" % (software_name, software_version)))
    return "\n".join([
        "<!DOCTYPE html>",
        '<html lang="zh-CN"><head><meta charset="utf-8">',
        "<title>%s</title>" % title,
        "<style>%s</style></head><body>" % MANUAL_STYLE,
        '<div id="content" class="sheet-body">%s</div>' % body,
        '<div id="pages"></div>',
        "<script>%s</script>" % pager,
        "</body></html>",
    ])


def main(argv=None):
    parser = argparse.ArgumentParser(description="把软件说明书排版为 PDF")
    parser.add_argument("--input", default="docs/软件说明书.md", help="说明书 Markdown 文件")
    parser.add_argument("--output", default="docs/软件说明书.pdf", help="输出 PDF 文件")
    parser.add_argument("--name", default=SOFTWARE_NAME, help="软件名称")
    parser.add_argument("--version", default=SOFTWARE_VERSION, help="软件版本号")
    parser.add_argument("--chromium", default="", help="Chromium 可执行文件路径，默认自动查找")
    args = parser.parse_args(argv)

    source_path = Path(args.input)
    if not source_path.is_file():
        print("说明书文件不存在：%s" % source_path, file=sys.stderr)
        return 1

    images_uri = source_path.parent.joinpath("images").resolve().as_uri()
    html_text = build_html(source_path.read_text(encoding="utf-8"), images_uri, args.name, args.version)
    pdf_render.render_html(html_text, Path(args.output), chromium=args.chromium or None)
    print("软件名称：%s %s" % (args.name, args.version))
    print("说明书来源：%s" % source_path)
    print("已生成：%s" % args.output)
    return 0


if __name__ == "__main__":
    sys.exit(main())
