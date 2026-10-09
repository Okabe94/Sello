"""Fingerprint of the inputs a quality run verified.

Covered: every tracked or untracked-but-not-ignored file, so code, tests, build
configuration, guardrails and ticket acceptance text all count.
Not covered: ignored outputs, generated board views, retained quality reports, and
ticket bookkeeping (status lines, Execution progress and Delivery evidence sections).
"""
import hashlib
import re
import subprocess
from pathlib import Path

BOARD_DIR = "docs/planning/mvp/"
EPIC_DIR = BOARD_DIR + "epics/"
RETAINED_REPORT_DIR = BOARD_DIR + "quality-reports/"
GENERATED_VIEWS = frozenset(BOARD_DIR + name for name in (
    "BOARD.md", "ROADMAP.md", "jira-import.csv", "dependency-links.csv",
    "roadmap-jira-import.csv", "roadmap-dependency-links.csv",
))
BOOKKEEPING = re.compile(
    r"^### (?:Execution progress|Delivery evidence)\n[\s\S]*?(?=^###? |\Z)", re.MULTILINE
)
STATUS_LINE = re.compile(r"^- \*\*Status:\*\* .*\n", re.MULTILINE)


def acceptance_text(epic_text):
    """Epic source with status and progress/evidence bookkeeping removed."""
    return BOOKKEEPING.sub("", STATUS_LINE.sub("", epic_text)).rstrip() + "\n"


def input_files(root):
    listed = subprocess.run(
        ["git", "-C", str(root), "ls-files", "-z", "--cached", "--others", "--exclude-standard"],
        check=True, capture_output=True,
    ).stdout.decode().split("\0")
    return sorted(
        name for name in set(filter(None, listed))
        if name not in GENERATED_VIEWS and not name.startswith(RETAINED_REPORT_DIR)
        and (Path(root) / name).is_file()
    )


def input_fingerprint(root):
    digest = hashlib.sha256()
    names = input_files(root)
    for name in names:
        content = (Path(root) / name).read_bytes()
        if name.startswith(EPIC_DIR) and name.endswith(".md"):
            content = acceptance_text(content.decode("utf-8")).encode("utf-8")
        digest.update(name.encode("utf-8") + b"\0" + hashlib.sha256(content).digest())
    return {"algorithm": "sha256", "value": digest.hexdigest(), "files": len(names)}


def ticket_section(epic_text, ticket_id):
    match = re.search(rf"^## {re.escape(ticket_id)} — [\s\S]*?(?=^## |\Z)", epic_text, re.MULTILINE)
    return match.group(0) if match else None


def ticket_acceptance(root, ticket_id):
    """Hash of one ticket's own text without bookkeeping, or None for an unknown ticket."""
    for path in sorted((Path(root) / EPIC_DIR).glob("*.md")):
        section = ticket_section(path.read_text(encoding="utf-8"), ticket_id)
        if section is not None:
            return hashlib.sha256(acceptance_text(section).encode("utf-8")).hexdigest()
    return None
