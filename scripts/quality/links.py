#!/usr/bin/env python3
"""Check every relative Markdown link and heading anchor in the repository's Markdown files.

Part of G0, so a moved or deleted document breaks the gate instead of a reader's path.
External URLs are not fetched. Links inside fenced code blocks are ignored.
"""
import re
import subprocess
import sys
from pathlib import Path
from urllib.parse import unquote

LINK = re.compile(r"(?<!\\)\]\(<?([^)\s>]+)(?:\s+\"[^\"]*\")?>?\)")
HEADING = re.compile(r"^#{1,6}\s+(.+?)\s*#*\s*$", re.MULTILINE)
FENCE = re.compile(r"^(```|~~~).*?^\1\s*$", re.MULTILINE | re.DOTALL)
EXTERNAL = ("http://", "https://", "mailto:", "tel:")
IGNORED_DIRS = {".git", "build", ".gradle", ".kotlin", ".idea", "node_modules"}


def markdown_files(root):
    root = Path(root)
    if (root / ".git").exists():
        listed = subprocess.run(["git", "-C", str(root), "ls-files", "-z", "--cached", "--others", "--exclude-standard", "--", "*.md"],
                                check=True, capture_output=True).stdout.decode().split("\0")
        return sorted(root / name for name in filter(None, listed) if (root / name).is_file())
    return sorted(path for path in root.rglob("*.md") if not IGNORED_DIRS & set(path.relative_to(root).parts))


def slug(heading):
    text = re.sub(r"`([^`]*)`", r"\1", heading)
    text = re.sub(r"\[([^\]]*)\]\([^)]*\)", r"\1", text)
    text = text.replace("**", "").replace("__", "")
    text = re.sub(r"[^\w\- ]", "", text.lower())
    return text.replace(" ", "-")


def anchors(text):
    seen, result = {}, set()
    for match in HEADING.finditer(FENCE.sub("", text)):
        base = slug(match.group(1))
        count = seen.get(base, 0)
        seen[base] = count + 1
        result.add(base if count == 0 else f"{base}-{count}")
    return result


def broken_links(root):
    root = Path(root)
    problems, count = [], 0
    texts = {}

    def text_of(path):
        if path not in texts:
            texts[path] = path.read_text(encoding="utf-8", errors="replace")
        return texts[path]

    for path in markdown_files(root):
        body = FENCE.sub(lambda match: "\n" * match.group(0).count("\n"), text_of(path))
        for match in LINK.finditer(body):
            target = match.group(1)
            if target.startswith(EXTERNAL):
                continue
            count += 1
            line = body.count("\n", 0, match.start()) + 1
            location = f"{path.relative_to(root)}:{line}"
            file_part, _, anchor = target.partition("#")
            destination = path if not file_part else (path.parent / unquote(file_part)).resolve()
            if not destination.exists():
                problems.append(f"{location}: missing target {target}")
                continue
            if anchor and destination.suffix == ".md" and destination.is_file():
                if unquote(anchor).lower() not in anchors(text_of(destination)):
                    problems.append(f"{location}: missing heading #{anchor} in {destination.relative_to(root)}")
    return problems, count, len(markdown_files(root))


def main(argv=None, root=None):
    root = Path(root) if root else Path(__file__).resolve().parents[2]
    problems, links, files = broken_links(root)
    for problem in problems:
        print(f"BROKEN: {problem}", file=sys.stderr)
    if problems:
        print(f"FAILED: {len(problems)} broken links in {files} Markdown files", file=sys.stderr)
        return 1
    print(f"PASS: {links} relative links and anchors in {files} Markdown files")
    return 0


if __name__ == "__main__":
    sys.exit(main())
