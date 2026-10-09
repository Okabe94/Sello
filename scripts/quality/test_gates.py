"""The documented gate must be the gate the runner executes."""
import re
import unittest
from pathlib import Path

import gates

README = Path(__file__).resolve().parents[2] / "docs/planning/mvp/README.md"


class GateDocumentationTests(unittest.TestCase):
    def setUp(self):
        self.text = README.read_text(encoding="utf-8")

    def test_documented_host_command_matches_the_runner(self):
        block = re.search(r"```bash\n(\./gradlew ktlintCheck[\s\S]*?)```", self.text).group(1)
        documented = block.replace("\\\n", " ").split()
        self.assertEqual(["./gradlew", *gates.HOST_TASKS, "--continue"], documented)

    def test_documented_device_tasks_match_the_runner(self):
        for task in gates.DEVICE_TASKS:
            self.assertIn(f"`{task}`", self.text)

    def test_host_gate_runs_every_module_and_both_static_checks(self):
        names = [check.name for check in gates.plan("G1")]
        self.assertEqual(["board-check", "board-tests", "quality-tests", "architecture", "gradle-host"], names)
        self.assertIn("ktlintCheck", gates.HOST_TASKS)
        self.assertIn("lintDebug", gates.HOST_TASKS)

    def test_device_gate_extends_the_host_gate(self):
        host, device = gates.plan("G1"), gates.plan("G2")
        self.assertEqual(host, device[: len(host)])
        self.assertTrue(device[-1].needs_device)

    def test_release_gate_is_unavailable(self):
        with self.assertRaises(gates.Unavailable):
            gates.plan("G3")


if __name__ == "__main__":
    unittest.main()
