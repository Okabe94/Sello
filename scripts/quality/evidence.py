"""Completion evidence: retain a passing quality report and validate it later.

Shared by board.py (Review/Done validation) and CI. A retained report is accepted
only if it is well formed, passed, belongs to the ticket, ran a sufficient gate, is
the exact file the ticket's `Quality run` field names, and still matches the
ticket's acceptance text. A ticket entering Review or Done must also match the
current input fingerprint; an already-Done ticket keeps its historical snapshot.
"""
import hashlib
import json
import re
import subprocess
from pathlib import Path

import fingerprint
import selection

GATES = ("G0", "G1", "G2", "G3")
MINIMUM_GATE = "G1"
RETAINED_DIR = fingerprint.RETAINED_REPORT_DIR.rstrip("/")
SCHEMA_V1 = json.loads((Path(__file__).with_name("report-schema-v1.json")).read_text(encoding="utf-8"))
SCHEMA = json.loads((Path(__file__).with_name("report-schema-v2.json")).read_text(encoding="utf-8"))
TYPES = {"object": dict, "array": list, "string": str, "integer": int, "boolean": bool}


class EvidenceError(Exception):
    pass


def schema_problem(value, schema, where="report"):
    """First violation of the supported schema keywords, or None."""
    if "const" in schema and value != schema["const"]:
        return f"{where} must be {schema['const']!r}"
    if "enum" in schema and value not in schema["enum"]:
        return f"{where} must be one of {schema['enum']}"
    expected = schema.get("type")
    if expected and (not isinstance(value, TYPES[expected]) or (expected == "integer" and isinstance(value, bool))):
        return f"{where} must be {expected}"
    if "minimum" in schema and value < schema["minimum"]:
        return f"{where} must be at least {schema['minimum']}"
    if "pattern" in schema and not re.search(schema["pattern"], value):
        return f"{where} has an unexpected format"
    for name in schema.get("required", ()):
        if name not in value:
            return f"{where}.{name} is required".removeprefix("report.")
    for name, child in schema.get("properties", {}).items():
        if name in value:
            problem = schema_problem(value[name], child, f"{where}.{name}")
            if problem:
                return problem.removeprefix("report.")
    if "items" in schema:
        for index, item in enumerate(value):
            problem = schema_problem(item, schema["items"], f"{where}[{index}]")
            if problem:
                return problem.removeprefix("report.")
    return None


def retained_path(root, ticket_id):
    return Path(root) / RETAINED_DIR / f"{ticket_id}.json"


def load(path):
    try:
        report = json.loads(Path(path).read_text(encoding="utf-8"))
    except OSError:
        raise EvidenceError("no retained quality report; run scripts/verify-ticket with --retain") from None
    except ValueError:
        raise EvidenceError("retained report is not valid JSON") from None
    schema = SCHEMA_V1 if isinstance(report, dict) and report.get("schema_version") == 1 else SCHEMA
    problem = schema_problem(report, schema)
    if problem:
        raise EvidenceError(f"malformed report: {problem}")
    if report["schema_version"] == 2:
        problem = selection.problem(report["coverage"], report["gate"]["executed"], report["ticket"])
        if problem:
            raise EvidenceError(f"invalid coverage: {problem}")
        if report["outcome"] == "passed" and report["gate"]["executed"] in ("G2", "G3"):
            observed = {}
            for check in report["checks"]:
                for module, counts in check.get("modules", {}).items():
                    if module in observed or not isinstance(counts, dict) or type(counts.get("tests")) is not int or counts["tests"] <= 0:
                        raise EvidenceError("invalid coverage: missing or duplicate device results")
                    if any(counts.get(key) != 0 for key in ("failures", "errors", "skipped")) or check.get("fresh_results") is not True:
                        raise EvidenceError("invalid coverage: device results failed, skipped or lack freshness")
                    observed[module] = counts
            if set(observed) != set(report["coverage"]["device_modules"]):
                raise EvidenceError("invalid coverage: executed modules differ from the selected device coverage")
    return report


def quality_run_line(root, ticket_id):
    """The `Quality run` field text that identifies the retained report."""
    path = retained_path(root, ticket_id)
    report = load(path)
    digest = hashlib.sha256(path.read_bytes()).hexdigest()
    scope = report.get("coverage", {"mode": "full", "device_modules": []})
    label = "" if scope["mode"] == "full" else f"; coverage {scope['mode']} ({','.join(scope['device_modules'])})"
    return (
        f"run {report['run_id']}; {report['ticket']} {report['gate']['executed']} {report['outcome']}; "
        f"HEAD {report['snapshot']['head'][:7]}, inputs sha256 {report['snapshot']['fingerprint']['value'][:12]}; "
        f"report {RETAINED_DIR}/{ticket_id}.json sha256 {digest}{label}"
    )


def retain(root, report_path):
    """Copy a passed run's report to the committed location; return its field text."""
    report = load(report_path)
    if report["outcome"] != "passed":
        raise EvidenceError("only a passed run can be retained as completion evidence")
    if not report["ticket"].startswith("SELLO-"):
        raise EvidenceError("report is not tied to a ticket and cannot be completion evidence")
    target = retained_path(root, report["ticket"])
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_bytes(Path(report_path).read_bytes())
    return quality_run_line(root, report["ticket"])


def check(root, ticket_id, ticket_gate, quality_run, fresh):
    """Raise EvidenceError unless the retained report completes this ticket."""
    path = retained_path(root, ticket_id)
    report = load(path)
    if report["ticket"] != ticket_id:
        raise EvidenceError(f"report is for {report['ticket']}, not {ticket_id}")
    if report["outcome"] != "passed":
        raise EvidenceError(f"report outcome is {report['outcome']}")
    for record in report["checks"]:
        if record["exit_code"] != 0:
            raise EvidenceError(f"check {record['name']} exited {record['exit_code']} in a report marked passed")
    required = max(ticket_gate, MINIMUM_GATE, key=GATES.index)
    executed = report["gate"]["executed"]
    if GATES.index(executed) < GATES.index(required):
        raise EvidenceError(f"{executed} does not satisfy {required}")
    if executed == "G3" and not any(
        record["name"] == "release-production" and record["exit_code"] == 0 for record in report["checks"]
    ):
        raise EvidenceError("a G3 report completes a ticket only with the owner's production signing "
                            "(release-production); CI's throwaway signing cannot")
    if report["run_id"] not in quality_run:
        raise EvidenceError(f"Quality run does not name run {report['run_id']} of the retained report")
    if hashlib.sha256(path.read_bytes()).hexdigest() not in quality_run:
        raise EvidenceError("retained report does not match the recorded sha256; it changed after it was recorded")
    if report["snapshot"]["acceptance"] != fingerprint.ticket_acceptance(root, ticket_id):
        raise EvidenceError("ticket acceptance text changed after the run; verify again")
    if fresh and report["snapshot"]["fingerprint"]["value"] != fingerprint.input_fingerprint(root)["value"]:
        raise EvidenceError("report is stale: inputs changed after the run; verify again")


def status_at(root, base, ticket_id):
    """A ticket's status at a Git revision, or None if unknown there."""
    def git(*arguments):
        completed = subprocess.run(["git", "-C", str(root), *arguments], capture_output=True, text=True)
        return completed.stdout if completed.returncode == 0 else None

    names = git("ls-tree", "-r", "--name-only", base, "--", fingerprint.EPIC_DIR)
    for name in (names or "").splitlines():
        section = fingerprint.ticket_section(git("show", f"{base}:{name}") or "", ticket_id)
        if section:
            match = re.search(r"^- \*\*Status:\*\* (.+)$", section, re.MULTILINE)
            return match.group(1) if match else None
    return None
