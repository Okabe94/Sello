"""board.py completion validation against real retained reports in a throwaway repository."""
import io
import sys
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

import evidence
import runner
from support import EPIC, epic, git, make_repo, ticket, write
from test_runner import Harness

board = runner.board
EVIDENCE = "\n### Delivery evidence\n" + "".join(f"- **{name}:** verified evidence\n" for name in board.DELIVERY_FIELDS)
DONE_PREDECESSOR = ticket("SELLO-003", status="Done") + EVIDENCE
README = "## Definition of done — every ticket\nShared gates\n## Milestones and safe integration\n"


class BoardEvidenceTests(unittest.TestCase):
    def setUp(self):
        directory = tempfile.TemporaryDirectory()
        self.addCleanup(directory.cleanup)
        self.root = make_repo(directory.name)
        write(self.root, "docs/planning/mvp/README.md", README)
        write(self.root, EPIC, epic(DONE_PREDECESSOR, ticket("SELLO-004", status="In Progress", dependencies="SELLO-003")))
        self.assertEqual(0, self.board()[0])
        git(self.root, "add", "-A")
        git(self.root, "commit", "-qm", "readme")
        self.harness = Harness(self.root)

    def board(self, *arguments):
        """Run board.py's main against the throwaway repository; return (exit code, stderr)."""
        location = str(self.root / "docs/planning/mvp/board.py")
        with patch.object(board, "__file__", location), patch("sys.argv", ["board.py", *arguments]), \
                patch("sys.stdout", new_callable=io.StringIO), patch("sys.stderr", new_callable=io.StringIO) as errors:
            return board.main(), errors.getvalue()

    def verify(self):
        code, path = runner.run(
            "SELLO-004", self.root, execute=self.harness.execute,
            environment=lambda root: {}, devices=lambda: [], stream=io.StringIO(),
        )
        self.assertEqual(0, code)
        return evidence.retain(self.root, path)

    def set_status(self, status, quality_run=None):
        body = ticket("SELLO-004", status=status, dependencies="SELLO-003")
        if quality_run:
            body += f"{EVIDENCE}- **Quality run:** {quality_run}\n"
        write(self.root, EPIC, epic(DONE_PREDECESSOR, body))
        self.assertEqual(0, self.board("--skip-evidence")[0])

    def commit(self, message):
        git(self.root, "add", "-A")
        git(self.root, "commit", "-qm", message)

    def test_ticket_moves_from_run_through_review_to_done(self):
        self.assertEqual(0, self.board("--check")[0])
        field = self.verify()
        self.set_status("Review", field)
        self.assertEqual((0, ""), self.board("--check"))
        self.set_status("Done", field)
        self.assertEqual((0, ""), self.board("--check"))

    def test_review_without_a_retained_report_is_rejected(self):
        self.set_status("Review", "run 20260101T000000Z-00000000, G1, passed")
        code, errors = self.board("--check")
        self.assertEqual(1, code)
        self.assertIn("SELLO-004: no retained quality report", errors)

    def test_copied_success_text_does_not_complete_a_ticket(self):
        field = self.verify()
        evidence.retained_path(self.root, "SELLO-004").unlink()
        self.set_status("Done", field)
        self.assertIn("SELLO-004: no retained quality report", self.board("--check")[1])

    def test_review_is_rejected_after_code_changes(self):
        self.set_status("Review", self.verify())
        write(self.root, "app/src/Main.kt", "fun main() = println(4)\n")
        code, errors = self.board("--check")
        self.assertEqual(1, code)
        self.assertIn("SELLO-004: report is stale", errors)

    def test_newly_done_ticket_must_match_current_inputs(self):
        field = self.verify()
        write(self.root, "app/src/Main.kt", "fun main() = println(4)\n")
        self.set_status("Done", field)
        self.assertIn("SELLO-004: report is stale", self.board("--check")[1])

    def test_committed_done_ticket_survives_later_changes(self):
        self.set_status("Done", self.verify())
        self.commit("complete SELLO-004")
        write(self.root, "app/src/Main.kt", "fun main() = println(4)\n")
        self.assertEqual((0, ""), self.board("--check"))

    def test_merge_candidate_is_compared_with_its_base(self):
        base = git(self.root, "rev-parse", "HEAD").strip()
        self.set_status("Done", self.verify())
        self.commit("complete SELLO-004")
        write(self.root, "app/src/Main.kt", "fun main() = println(4)\n")
        self.commit("unverified change in the same merge candidate")
        code, errors = self.board("--check", "--base", base)
        self.assertEqual(1, code)
        self.assertIn("SELLO-004: report is stale", errors)

    def test_done_ticket_is_rejected_when_its_acceptance_is_rewritten(self):
        self.set_status("Done", self.verify())
        self.commit("complete SELLO-004")
        text = (self.root / EPIC).read_text(encoding="utf-8")
        head, tail = text.split("## SELLO-004", 1)
        write(self.root, EPIC, head + "## SELLO-004" + tail.replace("keeps working", "mostly works"))
        self.board("--skip-evidence")
        self.assertIn("SELLO-004: ticket acceptance text changed", self.board("--check")[1])

    def test_structure_only_check_ignores_report_contents(self):
        self.set_status("Review", "run 20260101T000000Z-00000000, G1, passed")
        self.assertEqual(1, self.board("--check")[0])
        self.assertEqual((0, ""), self.board("--check", "--skip-evidence"))


if __name__ == "__main__":
    unittest.main()
