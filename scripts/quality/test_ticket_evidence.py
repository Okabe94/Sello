"""tickets.py completion validation against real retained reports in a throwaway repository."""
import io
import sys
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

import evidence
import runner
import tickets
from support import epic, git, make_repo, ticket, write, write_epic
from test_runner import Harness

EVIDENCE = "\n### Delivery evidence\n" + "".join(f"- **{name}:** verified evidence\n" for name in tickets.DELIVERY_FIELDS)
DONE_PREDECESSOR = ticket("SELLO-003", status="Done") + EVIDENCE


class BoardEvidenceTests(unittest.TestCase):
    def setUp(self):
        directory = tempfile.TemporaryDirectory()
        self.addCleanup(directory.cleanup)
        self.root = make_repo(directory.name)
        write_epic(self.root, epic(DONE_PREDECESSOR, ticket("SELLO-004", status="In Progress", dependencies="SELLO-003")))
        self.assertEqual(0, self.board("--skip-evidence")[0])
        git(self.root, "add", "-A")
        git(self.root, "commit", "-qm", "tickets")
        self.harness = Harness(self.root)

    def board(self, *arguments):
        """Run tickets.py check against the throwaway repository; return (exit code, stderr)."""
        arguments = [argument for argument in arguments if argument != "--check"]
        with patch("sys.stdout", new_callable=io.StringIO), patch("sys.stderr", new_callable=io.StringIO) as errors:
            return tickets.main(["check", *arguments], root=self.root), errors.getvalue()

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
        write_epic(self.root, epic(DONE_PREDECESSOR, body))
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
        write(self.root, "app/src/Main.kt", "fun main() = println(4)\n")
        self.commit("complete SELLO-004 with an unverified change")
        code, errors = self.board("--check", "--base", base)
        self.assertEqual(1, code)
        self.assertIn("SELLO-004: report is stale", errors)

    def test_a_verified_pull_request_merged_onto_a_moved_main_is_accepted(self):
        git(self.root, "checkout", "-q", "-b", "ticket")
        write(self.root, "app/src/Feature.kt", "class Feature\n")
        self.set_status("Done", self.verify())
        self.commit("complete SELLO-004")
        git(self.root, "checkout", "-q", "main")
        write(self.root, "app/src/Other.kt", "class Other\n")
        self.commit("another ticket merged first")
        moved = git(self.root, "rev-parse", "HEAD").strip()
        git(self.root, "merge", "-q", "--no-ff", "-m", "candidate", "ticket")
        self.assertEqual((0, ""), self.board("--check", "--base", moved))

    def test_each_step_of_a_batched_candidate_binds_its_own_ticket(self):
        base = git(self.root, "rev-parse", "HEAD").strip()
        self.set_status("Done", self.verify())
        self.commit("complete SELLO-004")
        write(self.root, "app/src/Other.kt", "class Other\n")
        self.commit("a later unticketed step the gate itself verifies")
        self.assertEqual((0, ""), self.board("--check", "--base", base))

    def test_done_ticket_is_rejected_when_its_acceptance_is_rewritten(self):
        self.set_status("Done", self.verify())
        self.commit("complete SELLO-004")
        path = self.root / "docs/tickets/E01-foundation/SELLO-004/ticket.md"
        write(self.root, str(path.relative_to(self.root)), path.read_text(encoding="utf-8").replace("keeps working", "mostly works"))
        self.board("--skip-evidence")
        self.assertIn("SELLO-004: ticket acceptance text changed", self.board("--check")[1])

    def test_a_recorded_link_only_migration_keeps_historical_evidence(self):
        import fingerprint
        self.set_status("Done", self.verify())
        self.commit("complete SELLO-004")
        before = fingerprint.ticket_acceptance(self.root, "SELLO-004")
        path = self.root / "docs/tickets/E01-foundation/SELLO-004/ticket.md"
        original = path.read_text(encoding="utf-8")
        moved = original.replace("### Outcome\n", "### Outcome\nSee [the guide](../../../development/new.md).\n")
        path.write_text(moved, encoding="utf-8")
        after = fingerprint.ticket_acceptance(self.root, "SELLO-004")
        self.assertIn("SELLO-004: ticket acceptance text changed", self.board("--check")[1])
        evidence_path = self.root / "docs/tickets/E01-foundation/SELLO-004/evidence.md"
        recorded = evidence_path.read_text(encoding="utf-8")
        evidence_path.write_text(recorded + f"- **Acceptance migrated:** {before} → {'0' * 64} link paths only\n", encoding="utf-8")
        self.assertIn("SELLO-004: ticket acceptance text changed", self.board("--check")[1])
        evidence_path.write_text(recorded + f"- **Acceptance migrated:** {before} → {after} link paths only\n", encoding="utf-8")
        self.assertEqual((0, ""), self.board("--check"))
        path.write_text(moved.replace("keeps working", "mostly works"), encoding="utf-8")
        self.assertIn("SELLO-004: ticket acceptance text changed", self.board("--check")[1])

    def test_structure_only_check_ignores_report_contents(self):
        self.set_status("Review", "run 20260101T000000Z-00000000, G1, passed")
        self.assertEqual(1, self.board("--check")[0])
        self.assertEqual((0, ""), self.board("--check", "--skip-evidence"))


if __name__ == "__main__":
    unittest.main()
