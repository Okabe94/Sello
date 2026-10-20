"""The hosted workflow must keep reporting on every candidate and must not lose a check silently."""
import os
import re
import subprocess
import tempfile
import textwrap
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
        # GitHub appends matrix values to a literal job name. Referencing the
        # matrix explicitly prevents that suffix and preserves the required check.
        self.assertIn("    name: ${{ matrix.cache-profile && 'quality' }}\n", self.code)

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
        # SDK image caching must not introduce a second cache of Gradle state.
        for step in self.steps.values():
            self.assertNotRegex(step, r"uses: actions/cache@")
            if "uses: actions/cache/" in step:
                self.assertIn("path: ${{ steps.android-image.outputs.path }}", step)

    def test_sdk_image_cache_is_separate_and_only_used_for_the_device_gate(self):
        locate = self.steps.get("Locate Android system image", "")
        restore = self.steps.get("Restore Android system image", "")
        self.assertIn("if: steps.plan.outputs.gate == 'G2'", locate)
        self.assertIn('path=$ANDROID_HOME/system-images/android-30/google_apis/x86_64', locate)
        self.assertIn("if: steps.plan.outputs.gate == 'G2'", restore)
        self.assertRegex(restore, r"uses: actions/cache/restore@[0-9a-f]{40}\b")
        self.assertIn("path: ${{ steps.android-image.outputs.path }}", restore)
        names = list(self.steps)
        self.assertLess(names.index("Decide what this run verifies"), names.index("Locate Android system image"))
        self.assertLess(names.index("Restore Android system image"), names.index("Verify on a device (G2 on API 30)"))

    def test_sdk_image_cache_publication_is_main_only_and_tracks_installed_metadata(self):
        identify = self.steps.get("Identify installed Android system image", "")
        save = self.steps.get("Save Android system image", "")
        trusted = "steps.plan.outputs.gate == 'G2' && github.event_name != 'pull_request' && github.ref == 'refs/heads/main'"
        self.assertIn("if: " + trusted, identify)
        self.assertIn("if: " + trusted, save)
        self.assertIn('sha256sum "$ANDROID_HOME/system-images/android-30/google_apis/x86_64/source.properties"', identify)
        self.assertIn("steps.android-image-cache.outputs.cache-matched-key != steps.android-image-key.outputs.key", save)
        self.assertRegex(save, r"uses: actions/cache/save@[0-9a-f]{40}\b")
        self.assertIn("key: ${{ steps.android-image-key.outputs.key }}", save)
        self.assertIn("path: ${{ steps.android-image.outputs.path }}", save)

        # Execute the workflow's key-generation code: a vendor revision change
        # must produce a new immutable cache key, while identical metadata reuses it.
        script = textwrap.dedent(identify.split("run: |\n", 1)[1])
        script = script.replace("${{ runner.os }}", "Linux").replace("${{ runner.arch }}", "X64")
        with tempfile.TemporaryDirectory() as directory:
            sdk = Path(directory) / "sdk"
            metadata = sdk / "system-images/android-30/google_apis/x86_64/source.properties"
            metadata.parent.mkdir(parents=True)
            output = Path(directory) / "output"
            environment = dict(os.environ, ANDROID_HOME=str(sdk), GITHUB_OUTPUT=str(output))

            def key(revision):
                metadata.write_text(f"Pkg.Revision={revision}\n", encoding="utf-8")
                output.write_text("", encoding="utf-8")
                subprocess.run(["bash", "-eu", "-c", script], env=environment, check=True, capture_output=True)
                return output.read_text(encoding="utf-8").strip()

            original = key(16)
            self.assertRegex(original, r"^key=android-image-v1-Linux-X64-api30-google_apis-x86_64-[0-9a-f]{64}$")
            self.assertEqual(original, key(16))
            self.assertNotEqual(original, key(17))

    def test_each_device_run_still_creates_a_fresh_avd_and_cold_boots(self):
        device = self.steps["Verify on a device (G2 on API 30)"]
        self.assertIn("force-avd-creation: true", device)
        self.assertIn("-no-snapshot", device)
        self.assertIn("api-level: 30", device)
        self.assertIn("target: google_apis", device)
        self.assertIn("arch: x86_64", device)
        self.assertIn("cores: 2", device)

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

    def test_host_pushes_and_full_runs_have_separate_singleton_cache_profiles(self):
        # Matrix context is part of setup-gradle's immutable key. A host push
        # must not occupy the key used to publish the nightly device build.
        profile = re.search(r"(?m)^        cache-profile:\n((?:          .+\n)+)", self.code)
        self.assertIsNotNone(profile, "Host and device runs still share one immutable cache key")
        self.assertEqual(
            ["- ${{ github.event_name == 'push' && 'host' || 'device' }}"],
            [line.strip() for line in profile.group(1).splitlines()],
        )
        strategy = self.code.split("    strategy:\n", 1)[1].split("    env:\n", 1)[0]
        self.assertEqual("      matrix:\n        cache-profile:\n" + profile.group(1), strategy)
        cache = self.steps["Set up Gradle caching"]
        self.assertNotIn("workflow-job-context:", cache)  # Let the action read the real matrix.
        self.assertNotIn("gradle-home-cache-strict-match:", cache)  # Retain cross-profile fallback.

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
