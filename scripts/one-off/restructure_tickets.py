#!/usr/bin/env python3
"""One-time move from epic files to ticket folders (docs/tickets/<epic>/<SELLO-0NN>/).

Run once from the repository root, then review `git status`. It uses `git mv` for the
files that move so history follows them, rewrites every relative Markdown link that
pointed at a moved file, and keeps each ticket's acceptance text byte-identical
(scripts/quality/tickets.py split_epic_text). Safe to delete after the move merges.
"""
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "scripts/quality"))
import tickets  # noqa: E402

EPICS = ROOT / "docs/planning/mvp/epics"
REPORTS = ROOT / "docs/planning/mvp/quality-reports"
EVIDENCE = ROOT / "docs/testing/evidence"
TESTING = ROOT / "docs/testing"
TICKETS = ROOT / "docs/tickets"
HANDOFF = ROOT / "docs/development/HANDOFF.md"
LINK = re.compile(r"(?<!\\)\]\(<?([^)\s>]+)(?:\s+\"[^\"]*\")?>?\)")
TICKET_ANCHOR = re.compile(r"^#sello-(\d{3})(?:--|$)")
DRY = "--dry-run" in sys.argv


def git(*arguments):
    if DRY:
        print("git", *arguments)
        return ""
    return subprocess.run(["git", "-C", str(ROOT), *arguments], check=True, capture_output=True, text=True).stdout


def write(path, text):
    if DRY:
        print(f"write {path.relative_to(ROOT)} ({len(text)} chars)")
        return
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding="utf-8")


def relative(target, from_dir):
    """A POSIX relative path from one directory to a target, like Markdown links use."""
    import os
    return Path(os.path.relpath(target, from_dir)).as_posix()


def main():
    moves = {}  # old absolute path -> new absolute path (files)
    anchor_moves = {}  # (old epic path, ticket id) -> new ticket.md
    folder_of = {}  # ticket id -> ticket folder
    handoff_rows = parse_handoff_rows()
    for epic_path in sorted(EPICS.glob("*.md")):
        text = epic_path.read_text(encoding="utf-8")
        readme, parts = tickets.split_epic_text(text)
        folder = TICKETS / epic_path.stem
        readme_path = folder / "README.md"
        moves[epic_path] = readme_path
        for identifier, ticket_md, evidence in parts:
            ticket_dir = folder / identifier
            folder_of[identifier] = ticket_dir
            anchor_moves[(epic_path, identifier)] = ticket_dir / "ticket.md"
            report = REPORTS / f"{identifier}.json"
            if report.is_file():
                moves[report] = ticket_dir / "quality-report.json"
            artifacts = EVIDENCE / identifier
            if artifacts.is_dir():
                for file in artifacts.rglob("*"):
                    if file.is_file():
                        moves[file] = ticket_dir / "artifacts" / file.relative_to(artifacts)
            for loose in list(TESTING.glob(f"{identifier}-*.md")) + list((TESTING / "reviews").glob(f"{identifier}-*.md")):
                moves[loose] = ticket_dir / "artifacts" / loose.name
    moves[ROOT / "docs/planning/mvp/README.md"] = TICKETS / "README.md"
    moves[ROOT / "docs/planning/mvp/QUALITY_FLOW.md"] = ROOT / "docs/development/quality-gates.md"
    moves[ROOT / "docs/planning/mvp/EXECUTION_GUIDE.md"] = ROOT / "docs/development/execution-guide.md"
    moves[ROOT / "docs/planning/mvp/BOARD.md"] = TICKETS / "README.md"
    moves[ROOT / "docs/planning/mvp/ROADMAP.md"] = TICKETS / "README.md"
    moves[ROOT / "docs/planning/mvp/DECISION_INPUTS.md"] = ROOT / "docs/decisions/0001-mvp-contract.md"

    def reroot(text, old_dir, new_dir, self_old=None):
        """Rewrite relative links so they resolve from new_dir to the (possibly moved) target."""
        def replace(match):
            target = match.group(1)
            if target.startswith(("http://", "https://", "mailto:", "tel:")):
                return match.group(0)
            file_part, _, anchor = target.partition("#")
            old_target = (old_dir / file_part).resolve() if file_part else self_old
            if old_target is None:
                return match.group(0)
            ticket_hit = TICKET_ANCHOR.match("#" + anchor) if anchor else None
            if ticket_hit and (old_target, f"SELLO-{ticket_hit.group(1)}") in anchor_moves:
                new_target, anchor = anchor_moves[(old_target, f"SELLO-{ticket_hit.group(1)}")], ""
            else:
                new_target = moves.get(old_target, old_target)
            if new_target == new_dir / "ticket.md" and self_old is not None and not file_part:
                return match.group(0)  # same-file anchor stays
            path = relative(new_target, new_dir)
            if new_target.is_dir() or (not new_target.exists() and old_target.is_dir()):
                path = path.rstrip("/") + "/"
            return match.group(0).replace(target, path + (f"#{anchor}" if anchor else ""), 1)
        return LINK.sub(replace, text)

    # 1. Rewrite links in every Markdown file that stays where it is.
    moved = set(moves)
    for path in sorted(ROOT.rglob("*.md")):
        if any(part in ("build", ".git", ".gradle", ".kotlin", ".idea") for part in path.parts) or path in moved or path.is_relative_to(EPICS):
            continue
        text = path.read_text(encoding="utf-8")
        new = reroot(text, path.parent, path.parent, self_old=path)
        if new != text:
            write(path, new)
    # 2. Split epics into folders.
    for epic_path in sorted(EPICS.glob("*.md")):
        text = epic_path.read_text(encoding="utf-8")
        readme, parts = tickets.split_epic_text(text)
        folder = TICKETS / epic_path.stem
        if not DRY and not (folder / "README.md").exists():
            folder.mkdir(parents=True, exist_ok=True)
            git("mv", str(epic_path.relative_to(ROOT)), str((folder / "README.md").relative_to(ROOT)))
        write(folder / "README.md", reroot(readme, epic_path.parent, folder, self_old=epic_path))
        for identifier, ticket_md, evidence in parts:
            ticket_dir = folder / identifier
            title = re.search(r"^# SELLO-\d{3} — (.+)$", ticket_md, re.MULTILINE).group(1)
            write(ticket_dir / "ticket.md", reroot(ticket_md, epic_path.parent, ticket_dir, self_old=epic_path))
            if evidence:
                body = evidence.split("\n", 1)[1].lstrip("\n")
                body = body.replace(f"docs/planning/mvp/quality-reports/{identifier}.json", f"docs/tickets/{epic_path.stem}/{identifier}/quality-report.json")
                write(ticket_dir / "evidence.md", reroot(f"# {identifier} — {title}\n\n{body}", epic_path.parent, ticket_dir, self_old=epic_path))
            status = re.search(r"^- \*\*Status:\*\* (.+)$", ticket_md, re.MULTILINE).group(1)
            if status == "Done":
                row = handoff_rows.get(identifier)
                if row:
                    handoff = reroot(row, HANDOFF.parent, ticket_dir, self_old=HANDOFF)
                else:
                    outcome = tickets.sections(ticket_md).get("Outcome", "").strip()
                    handoff = outcome + "\n\n(From the ticket's Outcome; the ticket closed before per-ticket handoffs existed.)"
                    handoff = reroot(handoff, epic_path.parent, ticket_dir, self_old=epic_path)
                write(ticket_dir / "handoff.md", f"# {identifier} — {title}\n\n{handoff}\n")
    # 3. Move reports, artifacts and the planning documents with git so history follows.
    for old, new in sorted(moves.items()):
        if old.is_relative_to(EPICS) or not old.exists() or new.exists():
            continue
        if old.name in ("BOARD.md", "ROADMAP.md", "DECISION_INPUTS.md"):
            continue  # deleted below; their links already point at the new home
        if not DRY:
            new.parent.mkdir(parents=True, exist_ok=True)
        text = old.read_text(encoding="utf-8") if old.suffix == ".md" else None
        git("mv", str(old.relative_to(ROOT)), str(new.relative_to(ROOT)))
        if text is not None:
            write(new, reroot(text, old.parent, new.parent, self_old=old))
    for name in ("BOARD.md", "ROADMAP.md", "DECISION_INPUTS.md", "jira-import.csv", "dependency-links.csv",
                 "roadmap-jira-import.csv", "roadmap-dependency-links.csv", "board.py", "test_board.py"):
        path = ROOT / "docs/planning/mvp" / name
        if path.exists():
            git("rm", "-q", str(path.relative_to(ROOT)))
    print(f"{len(folder_of)} tickets, {len(moves)} moved files")


def parse_handoff_rows():
    rows = {}
    if not HANDOFF.is_file():
        return rows
    for line in HANDOFF.read_text(encoding="utf-8").splitlines():
        match = re.match(r"^\| \[(SELLO-\d{3})\]\([^)]*\) \| (.+) \|$", line)
        if match:
            rows[match.group(1)] = match.group(2).strip()
    return rows


if __name__ == "__main__":
    main()
