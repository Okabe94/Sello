import tempfile
import unittest
from pathlib import Path

import fingerprint
from support import EPIC, epic, git, make_repo, ticket, write


class FingerprintTests(unittest.TestCase):
    def setUp(self):
        directory = tempfile.TemporaryDirectory()
        self.addCleanup(directory.cleanup)
        self.root = make_repo(directory.name)
        self.before = fingerprint.input_fingerprint(self.root)["value"]

    def after(self):
        return fingerprint.input_fingerprint(self.root)["value"]

    def test_unchanged_inputs_are_stable(self):
        self.assertEqual(self.before, self.after())

    def test_tracked_code_edit_changes_fingerprint(self):
        write(self.root, "app/src/Main.kt", "fun main() = println(1)\n")
        self.assertNotEqual(self.before, self.after())

    def test_untracked_source_changes_fingerprint(self):
        write(self.root, "app/src/New.kt", "class New\n")
        self.assertNotEqual(self.before, self.after())

    def test_deleted_source_changes_fingerprint(self):
        (self.root / "app/src/Main.kt").unlink()
        self.assertNotEqual(self.before, self.after())

    def test_ignored_build_output_is_excluded(self):
        write(self.root, "build/reports/quality/report.json", "{}")
        self.assertEqual(self.before, self.after())

    def test_generated_board_views_are_excluded(self):
        write(self.root, "docs/planning/mvp/BOARD.md", "regenerated\n")
        write(self.root, "docs/planning/mvp/jira-import.csv", "regenerated\n")
        self.assertEqual(self.before, self.after())

    def test_retained_reports_are_excluded(self):
        write(self.root, "docs/planning/mvp/quality-reports/SELLO-004.json", "{}")
        self.assertEqual(self.before, self.after())

    def test_status_change_is_bookkeeping(self):
        write(self.root, EPIC, epic(
            ticket("SELLO-003", status="Done"),
            ticket("SELLO-004", status="Review", dependencies="SELLO-003"),
        ))
        self.assertEqual(self.before, self.after())

    def test_added_progress_and_evidence_are_bookkeeping(self):
        text = (self.root / EPIC).read_text(encoding="utf-8")
        text += "\n### Execution progress\nStarted.\n\n### Delivery evidence\n- **Quality run:** q1\n"
        write(self.root, EPIC, text)
        self.assertEqual(self.before, self.after())

    def test_acceptance_edit_changes_fingerprint(self):
        text = (self.root / EPIC).read_text(encoding="utf-8")
        write(self.root, EPIC, text.replace("keeps working", "keeps working sometimes"))
        self.assertNotEqual(self.before, self.after())

    def test_gate_or_dependency_edit_changes_fingerprint(self):
        text = (self.root / EPIC).read_text(encoding="utf-8")
        write(self.root, EPIC, text.replace("- **Gate:** G1", "- **Gate:** G0"))
        self.assertNotEqual(self.before, self.after())

    def test_evidence_section_does_not_swallow_the_next_ticket(self):
        base = epic(
            ticket("SELLO-003", status="Done") + "\n### Delivery evidence\n- **Review:** ok\n",
            ticket("SELLO-004", status="In Progress", dependencies="SELLO-003"),
        )
        write(self.root, EPIC, base)
        first = self.after()
        write(self.root, EPIC, base.replace("Reject incomplete outcomes.", "Accept anything.", 2))
        self.assertNotEqual(first, self.after())

    def test_fresh_clone_matches_a_working_tree_with_converted_line_endings(self):
        git(self.root, "config", "core.autocrlf", "input")
        (self.root / "gradlew.bat").write_bytes(b"@echo off\r\necho hi\r\n")
        git(self.root, "add", "-A")
        git(self.root, "commit", "-qm", "script with Windows line endings")
        self.assertEqual("", git(self.root, "status", "--porcelain"))
        clone = tempfile.TemporaryDirectory()
        self.addCleanup(clone.cleanup)
        git(self.root, "clone", "-q", str(self.root), clone.name)
        self.assertEqual(b"@echo off\necho hi\n", (Path(clone.name) / "gradlew.bat").read_bytes())
        self.assertEqual(
            fingerprint.input_fingerprint(self.root)["value"],
            fingerprint.input_fingerprint(clone.name)["value"],
        )

    def test_reports_file_count(self):
        self.assertEqual(3, fingerprint.input_fingerprint(self.root)["files"])


if __name__ == "__main__":
    unittest.main()
