"""The hosted workflow must keep reporting on every candidate and must not lose a check silently."""
import re
import unittest
from pathlib import Path

WORKFLOW = Path(__file__).resolve().parents[2] / ".github/workflows/quality.yml"
GRADLE_PROPERTIES = WORKFLOW.parents[2] / "gradle.properties"


def steps(text):
    """Each step of the job as its own text, keyed by name ("uses" steps by what they use)."""
    found = {}
    for block in re.split(r"\n      - ", text.split("    steps:\n", 1)[1]):
        name = re.search(r"^(?:\s*- )?name: (.+)$", block, re.MULTILINE)
        uses = re.search(r"uses: (\S+)", block)
        found[name.group(1) if name else uses.group(1).split("@")[0]] = block
    return found


class HostedWorkflowTests(unittest.TestCase):
    def setUp(self):
        self.text = WORKFLOW.read_text(encoding="utf-8")
        self.code = "\n".join(line for line in self.text.splitlines() if not line.lstrip().startswith("#"))
        self.steps = steps(self.code)

    def test_no_event_is_filtered_by_path_so_the_required_check_always_reports(self):
        self.assertNotRegex(self.code, r"\bpaths(-ignore)?:")
        self.assertNotRegex(self.code, r"\bbranches-ignore:")
        self.assertRegex(self.code, r"(?m)^  pull_request:\s*$")

    def test_the_one_job_is_the_required_check(self):
        self.assertEqual(1, len(re.findall(r"(?m)^  \w[\w-]*:\n    name: ", self.code.split("jobs:\n", 1)[1])))
        self.assertIn("    name: quality\n", self.code)

    def test_a_pull_request_asks_the_runner_which_gate_it_needs(self):
        plan = self.steps["Decide what this run verifies"]
        self.assertIn('pull_request) mode=candidate; gate=$(./scripts/verify-ticket --candidate-gate "$BASE")', plan)
        self.assertIn("push)         mode=landed;    gate=G1", plan)
        self.assertIn("*)            mode=full;      gate=G2", plan)

    def test_the_device_gate_runs_whenever_the_plan_says_g2(self):
        device = self.steps["Verify on a device (G2 on API 30)"]
        self.assertIn("if: steps.plan.outputs.gate == 'G2'", device)
        self.assertIn('./scripts/verify-ticket --merge-candidate "$BASE" --gate G2', device)

    def test_every_run_that_skips_the_device_runs_the_host_gate_instead(self):
        documents = self.steps["Verify documents only on the host (G1)"]
        landed = self.steps["Verify what landed on main on the host (G1)"]
        self.assertIn("if: steps.plan.outputs.mode == 'candidate' && steps.plan.outputs.gate == 'G1'", documents)
        self.assertIn('./scripts/verify-ticket --merge-candidate "$BASE" --gate G1', documents)
        self.assertIn("if: steps.plan.outputs.mode == 'landed'", landed)
        self.assertIn('./scripts/verify-ticket --landed "$BASE"', landed)

    def test_completion_evidence_is_validated_on_every_run(self):
        evidence = self.steps["Validate completion evidence"]
        self.assertNotIn("if:", evidence)
        self.assertIn('python3 docs/planning/mvp/board.py --check --base "$BASE"', evidence)

    def test_main_still_gets_the_device_gate_on_a_schedule(self):
        self.assertRegex(self.code, r'schedule:\n    - cron: "\d+ \d+ \* \* \*"')

    def test_gradle_cache_setup_is_pinned_and_precedes_every_gate(self):
        cache = self.steps.get("Set up Gradle caching", "")
        self.assertRegex(cache, r"uses: gradle/actions/setup-gradle@[0-9a-f]{40}\b")
        self.assertNotIn("if:", cache)
        self.assertNotIn("gradle-version:", cache)  # The locked wrapper owns the version.
        names = list(self.steps)
        self.assertLess(names.index("Set up Gradle caching"), names.index("Decide what this run verifies"))
        self.assertNotRegex(self.code, r"(?m)^\s+cache: gradle\s*$")
        self.assertNotIn("uses: actions/cache", self.code)

    def test_shared_gradle_cache_is_read_only_outside_trusted_main_runs(self):
        cache = self.steps.get("Set up Gradle caching", "")
        self.assertIn(
            "cache-read-only: ${{ github.event_name == 'pull_request' || github.ref != 'refs/heads/main' }}",
            cache,
        )
        self.assertNotIn("cache-write-only:", cache)
        self.assertNotIn("cache-encryption-key:", cache)
        self.assertNotIn("secrets.", cache)
        self.assertIn("build-scan-publish: false", cache)
        self.assertIn("dependency-graph: disabled", cache)
        self.assertIn("add-job-summary-as-pr-comment: never", cache)

    def test_gradle_task_cache_is_enabled_alongside_configuration_cache(self):
        properties = GRADLE_PROPERTIES.read_text(encoding="utf-8")
        self.assertRegex(properties, r"(?m)^org\.gradle\.caching=true\s*$")
        self.assertRegex(properties, r"(?m)^org\.gradle\.configuration-cache=true\s*$")

    def test_no_check_is_allowed_to_fail_quietly(self):
        self.assertNotIn("continue-on-error", self.code)
        for name, step in self.steps.items():
            if "|| true" in step:
                self.assertEqual("Accept Android SDK licences", name)


if __name__ == "__main__":
    unittest.main()
