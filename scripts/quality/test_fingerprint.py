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


class ChangeFingerprintTests(unittest.TestCase):
    """The change set a report is bound to: the PR's own edits, not the whole tree."""

    def setUp(self):
        directory = tempfile.TemporaryDirectory()
        self.addCleanup(directory.cleanup)
        self.root = make_repo(directory.name)
        write(self.root, "app/src/Main.kt", "".join(f"line {n}\n" for n in range(1, 11)))
        self.commit("ten lines")

    def commit(self, message):
        git(self.root, "add", "-A")
        git(self.root, "commit", "-qm", message)

    def changes(self, base=None, tree=None):
        return fingerprint.change_fingerprint(self.root, base or fingerprint.change_base(self.root), tree=tree)

    def branch(self, name, path, text):
        git(self.root, "checkout", "-q", "-b", name)
        write(self.root, path, text)
        self.commit(f"{name} work")

    def main_moves(self, path, text):
        git(self.root, "checkout", "-q", "main")
        write(self.root, path, text)
        self.commit("another ticket")

    def test_a_clean_tree_on_main_has_no_changes(self):
        self.assertEqual(0, self.changes()["paths"])

    def test_the_same_edit_on_a_moved_main_has_the_same_fingerprint(self):
        self.branch("ticket", "app/src/Feature.kt", "class Feature\n")
        before = self.changes()
        self.main_moves("app/src/Other.kt", "class Other\n")
        git(self.root, "checkout", "-q", "ticket")
        git(self.root, "merge", "-q", "main")
        self.assertEqual(before["value"], self.changes()["value"])

    def test_the_same_edit_in_a_file_main_also_edited_elsewhere_has_the_same_fingerprint(self):
        lines = [f"line {n}\n" for n in range(1, 11)]
        self.branch("ticket", "app/src/Main.kt", "".join(["line 1\n", "changed 2\n", *lines[2:]]))
        before = self.changes()
        self.main_moves("app/src/Main.kt", "".join([*lines[:8], "changed 9\n", "line 10\n", "line 11\n"]))
        git(self.root, "checkout", "-q", "ticket")
        git(self.root, "merge", "-q", "main")
        self.assertEqual(before["value"], self.changes()["value"])

    def test_a_different_edit_changes_the_fingerprint(self):
        self.branch("ticket", "app/src/Feature.kt", "class Feature\n")
        before = self.changes()
        write(self.root, "app/src/Feature.kt", "class Feature { val x = 1 }\n")
        self.assertNotEqual(before["value"], self.changes()["value"])

    def test_reordering_lines_changes_the_fingerprint(self):
        self.branch("ticket", "app/src/Feature.kt", "a\nb\n")
        before = self.changes()
        write(self.root, "app/src/Feature.kt", "b\na\n")
        self.assertNotEqual(before["value"], self.changes()["value"])

    def test_bookkeeping_only_epic_edits_are_not_changes(self):
        write(self.root, EPIC, epic(
            ticket("SELLO-003", status="Done"),
            ticket("SELLO-004", status="Review", dependencies="SELLO-003") + "\n### Delivery evidence\n- **Quality run:** q1\n",
        ))
        self.assertEqual(0, self.changes()["paths"])

    def test_an_acceptance_edit_is_a_change(self):
        text = (self.root / EPIC).read_text(encoding="utf-8")
        write(self.root, EPIC, text.replace("keeps working", "keeps working sometimes"))
        self.assertEqual(1, self.changes()["paths"])

    def test_generated_views_reports_and_the_handoff_are_not_changes(self):
        write(self.root, "docs/planning/mvp/BOARD.md", "regenerated\n")
        write(self.root, "docs/planning/mvp/quality-reports/SELLO-004.json", "{}")
        write(self.root, "docs/development/HANDOFF.md", "informative only\n")
        self.assertEqual(0, self.changes()["paths"])

    def test_untracked_and_deleted_files_are_changes(self):
        write(self.root, "app/src/New.kt", "class New\n")
        self.assertEqual(1, self.changes()["paths"])
        (self.root / "app/src/Main.kt").unlink()
        self.assertEqual(2, self.changes()["paths"])

    def test_a_binary_change_is_a_change(self):
        (self.root / "app/icon.png").write_bytes(b"\x89PNG\x00\x01")
        self.commit("icon")
        before = self.changes(base="HEAD~1")
        (self.root / "app/icon.png").write_bytes(b"\x89PNG\x00\x02")
        self.assertNotEqual(before["value"], self.changes(base="HEAD~1")["value"])

    def test_a_committed_tree_matches_the_working_tree_it_came_from(self):
        self.branch("ticket", "app/src/Feature.kt", "class Feature\n")
        working = self.changes(base="main")
        committed = self.changes(base="main", tree="HEAD")
        self.assertEqual(working["value"], committed["value"])

    def test_candidates_cover_each_first_parent_step_and_the_whole_range(self):
        base = git(self.root, "rev-parse", "HEAD").strip()
        write(self.root, "app/src/Feature.kt", "class Feature\n")
        self.commit("ticket A")
        first = self.changes(base=base, tree="HEAD")["value"]
        write(self.root, "app/src/Other.kt", "class Other\n")
        self.commit("ticket B")
        second = self.changes(base="HEAD~1", tree="HEAD")["value"]
        whole = self.changes(base=base)["value"]
        self.assertEqual({first, second, whole}, fingerprint.candidate_changes(self.root, base))

    def test_candidates_without_a_base_compare_the_working_tree_with_main(self):
        self.branch("ticket", "app/src/Feature.kt", "class Feature\n")
        self.assertEqual({self.changes(base="main")["value"]}, fingerprint.candidate_changes(self.root))


if __name__ == "__main__":
    unittest.main()
