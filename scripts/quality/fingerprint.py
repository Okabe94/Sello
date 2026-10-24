"""Fingerprints of what a quality run verified.

Two fingerprints are recorded. The input fingerprint covers the whole tree: every
tracked or untracked-but-not-ignored file, so code, tests, build configuration,
guardrails and ticket acceptance text all count. The change fingerprint covers only
the change set under verification: what differs between the merge base with main and
the tree, as normalized hunks, so the same edit hashes the same whether or not main
has moved elsewhere since. Completion evidence is bound to the change fingerprint;
the hosted run on the merge result verifies the combination.
Neither covers ignored outputs, generated board views, retained quality reports, the
informative handoff, or ticket bookkeeping (status lines, Execution progress and
Delivery evidence sections).
"""
import difflib
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
# Informative only (AGENTS.md): no check reads it, so its edits are not verified inputs.
EXCLUDED_FILES = frozenset({"docs/development/HANDOFF.md"})
INTEGRATION_BRANCHES = ("origin/main", "main")
MAX_CHAIN = 500
BOOKKEEPING = re.compile(
    r"^### (?:Execution progress|Delivery evidence)\n[\s\S]*?(?=^###? |\Z)", re.MULTILINE
)
STATUS_LINE = re.compile(r"^- \*\*Status:\*\* .*\n", re.MULTILINE)


def acceptance_text(epic_text):
    """Epic source with status and progress/evidence bookkeeping removed."""
    return BOOKKEEPING.sub("", STATUS_LINE.sub("", epic_text)).rstrip() + "\n"


def is_excluded(name):
    return name in GENERATED_VIEWS or name in EXCLUDED_FILES or name.startswith(RETAINED_REPORT_DIR)


def input_files(root):
    listed = subprocess.run(
        ["git", "-C", str(root), "ls-files", "-z", "--cached", "--others", "--exclude-standard"],
        check=True, capture_output=True,
    ).stdout.decode().split("\0")
    return sorted(
        name for name in set(filter(None, listed))
        if not is_excluded(name) and (Path(root) / name).is_file()
    )


def is_epic(name):
    return name.startswith(EPIC_DIR) and name.endswith(".md")


def input_fingerprint(root):
    """Hash file contents as Git would store them, so a checkout with converted
    line endings and a fresh clone of the same commit agree."""
    names = input_files(root)
    plain = [name for name in names if not is_epic(name)]
    stored = subprocess.run(
        ["git", "-C", str(root), "hash-object", "--stdin-paths"],
        input="".join(f"{name}\n" for name in plain), check=True, capture_output=True, text=True, cwd=root,
    ).stdout.split()
    identities = dict(zip(plain, stored))
    for name in names:
        if is_epic(name):
            text = (Path(root) / name).read_text(encoding="utf-8").replace("\r\n", "\n")
            identities[name] = hashlib.sha256(acceptance_text(text).encode("utf-8")).hexdigest()
    digest = hashlib.sha256()
    for name in names:
        digest.update(f"{name}\0{identities[name]}\n".encode("utf-8"))
    return {"algorithm": "sha256", "value": digest.hexdigest(), "files": len(names)}


def ticket_section(epic_text, ticket_id):
    match = re.search(rf"^## {re.escape(ticket_id)} — [\s\S]*?(?=^## |\Z)", epic_text, re.MULTILINE)
    return match.group(0) if match else None


def ticket_acceptance(root, ticket_id):
    """Hash of one ticket's own text without bookkeeping, or None for an unknown ticket."""
    for path in sorted((Path(root) / EPIC_DIR).glob("*.md")):
        section = ticket_section(path.read_text(encoding="utf-8").replace("\r\n", "\n"), ticket_id)
        if section is not None:
            return hashlib.sha256(acceptance_text(section).encode("utf-8")).hexdigest()
    return None


def _git(root, *arguments):
    return subprocess.run(["git", "-C", str(root), *arguments], capture_output=True)


def _revision(root, name):
    completed = _git(root, "rev-parse", "--verify", "--quiet", f"{name}^{{commit}}")
    if completed.returncode:
        raise ValueError(f"Unknown revision {name}")
    return completed.stdout.decode().strip()


def change_base(root):
    """The merge base of HEAD with the integration branch, or HEAD when there is none."""
    head = _revision(root, "HEAD")
    for branch in INTEGRATION_BRANCHES:
        if _git(root, "rev-parse", "--verify", "--quiet", f"{branch}^{{commit}}").returncode == 0:
            merge_base = _git(root, "merge-base", head, branch)
            if merge_base.returncode == 0:
                return merge_base.stdout.decode().strip()
    return head


def _changed_names(root, base, tree):
    if tree is None:
        listed = _git(root, "diff", "--name-only", "--no-renames", "-z", base, "--").stdout
        listed += _git(root, "ls-files", "--others", "--exclude-standard", "-z").stdout
    else:
        listed = _git(root, "diff", "--name-only", "--no-renames", "-z", base, tree, "--").stdout
    return sorted(name for name in set(filter(None, listed.decode().split("\0"))) if not is_excluded(name))


def _blob(root, revision, name):
    completed = _git(root, "show", f"{revision}:{name}")
    return completed.stdout if completed.returncode == 0 else None


def _working_file(root, name):
    path = Path(root) / name
    return path.read_bytes() if path.is_file() else None


def _is_binary(data):
    return data is not None and b"\0" in data[:8192]


def _lines(data, epic):
    text = (data or b"").decode("utf-8", "surrogateescape").replace("\r\n", "\n")
    if epic:
        text = acceptance_text(text)
    return text.splitlines(keepends=True)


def _change_entry(name, old, new):
    """One path's normalized change, or None when nothing verifiable changed."""
    if old == new:
        return None
    if _is_binary(old) or _is_binary(new):
        digests = [hashlib.sha256(data).hexdigest() if data is not None else "-" for data in (old, new)]
        return f"{name}\0binary\0{digests[0]}\0{digests[1]}\n"
    before, after = _lines(old, is_epic(name)), _lines(new, is_epic(name))
    if before == after:
        return None
    kind = "added" if old is None else "deleted" if new is None else "modified"
    parts = [f"{name}\0{kind}\n"]
    for tag, i1, i2, j1, j2 in difflib.SequenceMatcher(None, before, after, autojunk=False).get_opcodes():
        if tag != "equal":
            parts.append("".join(f"-{line}\0" for line in before[i1:i2]))
            parts.append("".join(f"+{line}\0" for line in after[j1:j2]))
    return "".join(parts)


def change_fingerprint(root, base, tree=None):
    """Hash the change set between [base] and the working tree, or the commit [tree].

    Hunk positions are left out, so an edit hashes the same after unrelated changes
    land elsewhere, even in the same file. Any difference in the edit itself, including
    the order of lines, changes the value.
    """
    base = _revision(root, base)
    if tree is not None:
        tree = _revision(root, tree)
    digest, count = hashlib.sha256(), 0
    for name in _changed_names(root, base, tree):
        new = _working_file(root, name) if tree is None else _blob(root, tree, name)
        entry = _change_entry(name, _blob(root, base, name), new)
        if entry is not None:
            digest.update(entry.encode("utf-8", "surrogateescape"))
            count += 1
    return {"algorithm": "sha256", "base": base, "value": digest.hexdigest(), "paths": count}


def candidate_changes(root, base=None):
    """Change fingerprints a retained report may match.

    Without a base: the working tree against its merge base with main, the executor's
    view. With a base: every first-parent step from HEAD back to the base, so each
    pull request in a batched merge group or a multi-commit push is bound on its own,
    plus the whole range to the working tree.
    """
    if base is None:
        return {change_fingerprint(root, change_base(root))["value"]}
    base = _revision(root, base)
    values = {change_fingerprint(root, base)["value"]}
    commit = _revision(root, "HEAD")
    for _ in range(MAX_CHAIN):
        if commit == base:
            break
        parents = _git(root, "rev-list", "--parents", "-n", "1", commit).stdout.decode().split()[1:]
        if not parents:
            break
        values.add(change_fingerprint(root, parents[0], tree=commit)["value"])
        commit = parents[0]
    return values
