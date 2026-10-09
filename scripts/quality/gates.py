"""Gate profiles: which checks each gate runs and what results they must leave behind."""
import re
import xml.etree.ElementTree as ElementTree
from dataclasses import dataclass
from pathlib import Path

GATES = ("G0", "G1", "G2", "G3")
MINIMUM_GATE = "G1"

HOST_TASKS = (
    "ktlintCheck", ":domain:test", ":data:testDebugUnitTest", ":design-system:testDebugUnitTest",
    ":app:testDebugUnitTest", ":catalog:testDebugUnitTest", "lintDebug",
    ":app:assembleDebug", ":app:assembleRelease", ":catalog:assembleDebug",
)
DEVICE_TASKS = (
    ":data:connectedDebugAndroidTest", ":design-system:connectedDebugAndroidTest",
    ":app:connectedDebugAndroidTest", ":catalog:connectedDebugAndroidTest",
)
HOST_RESULTS = "*/build/test-results/**/TEST-*.xml"
DEVICE_RESULTS = "*/build/outputs/androidTest-results/connected/**/TEST-*.xml"
LINT_RESULTS = "*/build/reports/lint-results-debug.xml"


class Unavailable(Exception):
    """A gate cannot be executed with the checks that exist today."""


@dataclass(frozen=True)
class Check:
    name: str
    argv: tuple
    needs_device: bool = False
    results: str = ""
    fresh_results: bool = False
    lint: bool = False


def plan(gate):
    if gate not in GATES:
        raise Unavailable(f"Unknown gate {gate}")
    if gate == "G3":
        raise Unavailable(
            "G3 release checks (signed release, artifact inspection, performance) are delivered by "
            "SELLO-035 and are not available; a lower gate cannot stand in for them"
        )
    checks = [
        # Structure and generated views only: a run cannot depend on its own final evidence.
        Check("board-check", ("python3", "docs/planning/mvp/board.py", "--check", "--skip-evidence")),
        Check("board-tests", ("python3", "-m", "unittest", "discover", "-s", "docs/planning/mvp", "-p", "test_board.py")),
        Check("quality-tests", ("python3", "-m", "unittest", "discover", "-s", "scripts/quality", "-p", "test_*.py")),
        Check("architecture", ("python3", "scripts/quality/architecture.py")),
        Check("gradle-host", ("./gradlew", *HOST_TASKS, "--continue"), results=HOST_RESULTS, lint=True),
    ]
    if gate == "G2":
        checks.append(Check(
            "gradle-device", ("./gradlew", *DEVICE_TASKS, "--continue"),
            needs_device=True, results=DEVICE_RESULTS, fresh_results=True,
        ))
    return checks


def test_results(root, pattern, newer_than=None):
    totals = {"suites": 0, "tests": 0, "failures": 0, "errors": 0, "skipped": 0}
    for path in sorted(Path(root).glob(pattern)):
        if newer_than is not None and path.stat().st_mtime_ns < newer_than:
            continue
        suite = ElementTree.parse(path).getroot()
        totals["suites"] += 1
        for name in ("tests", "failures", "errors", "skipped"):
            totals[name] += int(suite.get(name, 0))
    return totals


def lint_results(root):
    totals = {"reports": 0, "errors": 0, "warnings": 0}
    for path in sorted(Path(root).glob(LINT_RESULTS)):
        text = path.read_text(encoding="utf-8")
        totals["reports"] += 1
        totals["errors"] += len(re.findall(r'severity="(?:Error|Fatal)"', text))
        totals["warnings"] += len(re.findall(r'severity="Warning"', text))
    return totals
