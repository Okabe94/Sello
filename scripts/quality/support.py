"""Shared fixtures for quality-tool tests: a throwaway Git repository with a board."""
import subprocess
from pathlib import Path

SECTIONS = """### Outcome
A complete, independently verifiable task.

### Deliverables
An integrated implementation and evidence.

### Acceptance criteria
- The existing project keeps working.

### Tests
Meaningful red and green verification.

### Working checkpoint
The app builds and its affected flow runs.

### Context and starting points
Read the actual baseline and completed predecessor contracts.

### Implementation plan
1. Implement the owned boundary.

### Concrete cases and pitfalls
Reject incomplete outcomes.

### Verification recipe
Run focused tests and the assigned integrated gate.
"""

EPIC = "docs/planning/mvp/epics/E01-foundation.md"


def ticket(identifier, status="Backlog", dependencies="None", gate="G1"):
    return (
        f"## {identifier} — Complete a working task\n\n- **Type:** Task\n- **Priority:** P0\n"
        f"- **Status:** {status}\n- **Depends on:** {dependencies}\n- **Gate:** {gate}\n\n{SECTIONS}"
    )


def epic(*tickets):
    header = (
        "# SELLO-E01 — Foundation\n\n- **Status:** In Progress\n"
        "- **Goal:** Build a trustworthy foundation.\n- **Exit:** Working app and verified contracts.\n\n"
    )
    return header + "\n".join(tickets)


def git(root, *arguments):
    return subprocess.run(
        ["git", "-C", str(root), *arguments], check=True, capture_output=True, text=True
    ).stdout


def write(root, relative, text):
    path = Path(root) / relative
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding="utf-8")


def make_repo(root, epic_text=None):
    git(root, "init", "-q", "-b", "main")
    git(root, "config", "user.name", "Test")
    git(root, "config", "user.email", "test@example.invalid")
    write(root, ".gitignore", "build/\n")
    write(root, "app/src/Main.kt", "fun main() = Unit\n")
    write(root, "docs/planning/mvp/BOARD.md", "generated\n")
    write(root, EPIC, epic_text or epic(
        ticket("SELLO-003", status="Done"),
        ticket("SELLO-004", status="In Progress", dependencies="SELLO-003"),
    ))
    git(root, "add", "-A")
    git(root, "commit", "-q", "-m", "initial")
    return Path(root)
