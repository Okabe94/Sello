"""Which gate CI asks for: the device gate, unless nothing a device test could see changed."""
import io
import json
import tempfile
import unittest
from pathlib import Path

import runner
from support import EPIC, epic, git, make_repo, ticket, write, write_epic
from test_runner import DEVICE, Harness


class CandidateGateTests(unittest.TestCase):
    def setUp(self):
        directory = tempfile.TemporaryDirectory()
        self.addCleanup(directory.cleanup)
        self.root = make_repo(directory.name, epic(
            ticket("SELLO-003", status="Done"),
            ticket("SELLO-004", status="Backlog", dependencies="SELLO-003", gate="G2"),
        ))
        self.base = git(self.root, "rev-parse", "HEAD").strip()

    def commit(self, **files):
        for relative, text in files.items():
            write(self.root, relative, text)
        git(self.root, "add", "-A")
        git(self.root, "commit", "-qm", "change")

    def gate(self):
        return runner.candidate_gate(self.root, self.base)

    def test_documents_are_what_lives_under_docs_and_markdown_at_the_top(self):
        documents = [
            "docs/development/storage.md", "docs/tickets/E11-bug-stabilization/README.md",
            "docs/tickets/E11-bug-stabilization/SELLO-045/ticket.md", "docs/tickets/E11-bug-stabilization/SELLO-045/handoff.md",
            "docs/tickets/E11-bug-stabilization/SELLO-045/quality-report.json", "docs/tickets/E11-bug-stabilization/SELLO-045/artifacts/a.png",
            "docs/design/sello-spec.html", "ARCHITECTURE.md", "AGENTS.md", "README.md",
        ]
        other = [
            "app/src/main/java/com/software/sello/MainActivity.kt", "app/src/main/res/values/strings.xml",
            "app/src/androidTestDebug/java/com/software/sello/Waits.kt", "data/schemas/x/1.json",
            "build.gradle.kts", "gradle/libs.versions.toml", "gradle.properties", "settings.gradle.kts",
            ".github/workflows/quality.yml", ".github/pull_request_template.md",
            "scripts/verify-ticket", "scripts/quality/runner.py", "scripts/quality/notes.md",
            # Code that happens to live beside the documents is still code.
            "scripts/quality/tickets.py", "docs/tickets/tool.py",
            "app/README.md", "docs.md/Main.kt", "docsx/a.md",
        ]
        self.assertEqual([], [path for path in documents if not runner.is_document(path)])
        self.assertEqual([], [path for path in other if runner.is_document(path)])

    def test_a_change_to_documents_alone_needs_no_device(self):
        self.commit(**{"docs/development/notes.md": "text\n", "ARCHITECTURE.md": "rules\n"})
        self.assertEqual("G0", self.gate())

    def test_any_code_change_needs_the_device_gate(self):
        self.commit(**{"app/src/Main.kt": "fun main() = println()\n"})
        self.assertEqual("G2", self.gate())

    def test_documents_changed_together_with_code_need_the_device_gate(self):
        self.commit(**{"docs/development/notes.md": "text\n", "app/src/Other.kt": "val a = 1\n"})
        self.assertEqual("G2", self.gate())

    def test_the_workflow_the_tools_and_the_build_are_not_documents(self):
        for relative in (".github/workflows/quality.yml", "scripts/quality/x.py", "build.gradle.kts",
                         "scripts/quality/tickets.py"):
            with self.subTest(relative):
                self.commit(**{relative: "changed\n"})
                self.assertEqual("G2", self.gate())
                git(self.root, "reset", "-q", "--hard", self.base)

    def test_a_document_change_that_advances_a_ticket_still_needs_the_device_gate(self):
        write_epic(self.root, epic(
            ticket("SELLO-003", status="Done"),
            ticket("SELLO-004", status="Done", dependencies="SELLO-003", gate="G2"),
        ))
        self.commit()
        self.assertEqual(["SELLO-004"], runner.merge_candidate_tickets(self.root, self.base))
        self.assertEqual("G2", self.gate())

    def test_a_ticket_note_that_advances_nothing_is_a_document_change(self):
        write_epic(self.root, epic(
            ticket("SELLO-003", status="Done"),
            ticket("SELLO-004", status="Backlog", dependencies="SELLO-003", gate="G2") + "\nA note.\n",
        ))
        self.commit()
        self.assertEqual("G0", self.gate())

    def test_a_file_deleted_or_moved_out_of_the_code_counts_as_a_code_change(self):
        git(self.root, "mv", "app/src/Main.kt", "docs/Main.md")
        git(self.root, "commit", "-qm", "moved")
        self.assertEqual("G2", self.gate())
        git(self.root, "reset", "-q", "--hard", self.base)
        git(self.root, "rm", "-q", "app/src/Main.kt")
        git(self.root, "commit", "-qm", "deleted")
        self.assertEqual("G2", self.gate())

    def test_code_not_committed_yet_counts_too(self):
        self.commit(**{"docs/development/notes.md": "text\n"})
        write(self.root, "app/src/Untracked.kt", "val a = 1\n")
        self.assertEqual("G2", self.gate())
        (self.root / "app/src/Untracked.kt").unlink()
        write(self.root, "app/src/Main.kt", "fun main() = println(2)\n")
        self.assertEqual("G2", self.gate())

    def test_nothing_changed_needs_no_device(self):
        self.assertEqual([], runner.changed_paths(self.root, self.base))
        self.assertEqual("G0", self.gate())

    def test_an_unknown_base_is_treated_as_needing_the_device_gate(self):
        self.assertEqual("G2", runner.candidate_gate(self.root, "0" * 40))


class LandedTests(unittest.TestCase):
    """After a merge: the candidate already passed its own gate, so the host gate runs."""

    def setUp(self):
        directory = tempfile.TemporaryDirectory()
        self.addCleanup(directory.cleanup)
        self.root = make_repo(directory.name, epic(
            ticket("SELLO-003", status="Done"),
            ticket("SELLO-004", status="Backlog", dependencies="SELLO-003", gate="G2"),
        ))
        self.base = git(self.root, "rev-parse", "HEAD").strip()
        self.harness = Harness(self.root)

    def land(self, base=None):
        self.stream = io.StringIO()
        return runner.run_landed(
            base or self.base, self.root, execute=self.harness.execute,
            environment=lambda root: {}, devices=lambda: [], stream=self.stream,
        )

    def complete_the_device_ticket(self):
        write_epic(self.root, epic(
            ticket("SELLO-003", status="Done"),
            ticket("SELLO-004", status="Done", dependencies="SELLO-003", gate="G2"),
        ))
        write(self.root, "app/src/Main.kt", "fun main() = println()\n")
        git(self.root, "add", "-A")
        git(self.root, "commit", "-qm", "ticket")

    def test_a_merged_device_ticket_is_checked_on_the_host_without_a_device(self):
        self.complete_the_device_ticket()
        code, paths = self.land()
        report = json.loads(Path(paths[0]).read_text(encoding="utf-8"))
        self.assertEqual(0, code)
        self.assertEqual("UNTICKETED", report["ticket"])
        self.assertEqual({"required": "G1", "executed": "G1"}, report["gate"])
        self.assertIn("gradle-host", self.harness.executed)
        self.assertNotIn("gradle-device", self.harness.executed)

    def test_a_failing_host_check_fails_the_run_on_main(self):
        self.complete_the_device_ticket()
        self.harness.exit_codes["gradle-host"] = 1
        self.assertNotEqual(0, self.land()[0])

    def test_an_unknown_base_is_refused_before_anything_runs(self):
        code, paths = self.land(base="0" * 40)
        self.assertNotEqual(0, code)
        self.assertEqual([], paths)
        self.assertEqual([], self.harness.executed)


if __name__ == "__main__":
    unittest.main()
