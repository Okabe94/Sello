"""Ticket folders: docs/tickets/<epic>/<SELLO-0NN>/ticket.md is the canonical ticket."""
import io
import tempfile
import unittest
from contextlib import redirect_stdout, redirect_stderr
from pathlib import Path

import tickets

SECTIONS = """### Outcome
A complete, independently verifiable task.

### Deliverables
An integrated implementation and evidence.

### Acceptance criteria
- The existing project keeps working.

### Tests
Meaningful red and green verification.

### Working checkpoint
The app builds and its affected flow runs.

### Context and starting points
Read the actual baseline and completed predecessor contracts.

### Implementation plan
1. Implement the owned boundary.
2. Verify the behavior without fake success.

### Concrete cases and pitfalls
Reject incomplete outcomes and preserve existing financial history.

### Verification recipe
Run focused tests and the assigned integrated gate.
"""
EVIDENCE = "# SELLO-001 — Complete a working task\n\n" + "".join(
    f"- **{name}:** verified evidence\n" for name in tickets.DELIVERY_FIELDS
) + "- **Quality run:** run q001; retained report\n"


def epic_readme(identifier="SELLO-E01", status="Backlog", increment=None):
    extra = f"- **Increment:** {increment}\n" if increment else ""
    return (f"# {identifier} — Foundation\n\n- **Status:** {status}\n{extra}"
            "- **Goal:** Build a trustworthy foundation.\n- **Exit:** Working app and verified contracts.\n")


def ticket_text(identifier="SELLO-001", status="Backlog", dependencies="None", gate="G1"):
    return (f"# {identifier} — Complete a working task\n\n- **Type:** Task\n- **Priority:** P0\n"
            f"- **Status:** {status}\n- **Depends on:** {dependencies}\n- **Gate:** {gate}\n\n{SECTIONS}")


class TicketFolderTests(unittest.TestCase):
    def setUp(self):
        directory = tempfile.TemporaryDirectory()
        self.addCleanup(directory.cleanup)
        self.root = Path(directory.name)

    def write(self, relative, text):
        path = self.root / relative
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(text, encoding="utf-8")

    def epic(self, folder="E01-foundation", readme=None, **ticket_files):
        self.write(f"docs/tickets/{folder}/README.md", readme or epic_readme())
        for identifier, text in ticket_files.items():
            self.write(f"docs/tickets/{folder}/{identifier.replace('_', '-')}/ticket.md", text)

    def load(self):
        return tickets.load(self.root)

    def validate(self):
        tickets.validate(self.load())

    def test_parses_a_complete_ticket(self):
        self.epic(SELLO_001=ticket_text(status="Ready"))
        epics = self.load()
        tickets.validate(epics)
        only = epics[0].tickets[0]
        self.assertEqual(("SELLO-001", "Complete a working task", (), "G1", "SELLO-E01"),
                         (only.identifier, only.title, only.dependencies, only.gate, only.epic_id))

    def test_folder_name_must_match_the_ticket_heading(self):
        self.epic(SELLO_002=ticket_text("SELLO-001"))
        with self.assertRaisesRegex(ValueError, "folder SELLO-002 holds SELLO-001"):
            self.load()

    def test_epic_folder_without_a_readme_is_rejected(self):
        self.write("docs/tickets/E01-foundation/SELLO-001/ticket.md", ticket_text())
        with self.assertRaisesRegex(ValueError, "README.md"):
            self.load()

    def test_template_folder_is_not_an_epic(self):
        self.epic(SELLO_001=ticket_text())
        self.write("docs/tickets/TEMPLATE/README.md", "# Template\n")
        self.write("docs/tickets/TEMPLATE/SELLO-0NN/ticket.md", "# SELLO-0NN — Title\n")
        self.assertEqual(["SELLO-E01"], [epic.identifier for epic in self.load()])

    def test_preserves_post_mvp_increment_and_rejects_unknown(self):
        self.epic(readme=epic_readme(increment="Post-MVP"), SELLO_001=ticket_text())
        self.assertEqual("Post-MVP", self.load()[0].increment)
        self.epic(readme=epic_readme(increment="Unknown"), SELLO_001=ticket_text())
        with self.assertRaisesRegex(ValueError, "invalid increment"):
            self.validate()

    def test_new_epic_requires_explicit_increment(self):
        self.epic("E09-carryover", readme=epic_readme("SELLO-E09"), SELLO_001=ticket_text())
        with self.assertRaisesRegex(ValueError, "missing Increment"):
            self.load()

    def test_mvp_cannot_depend_on_post_mvp(self):
        self.epic(SELLO_001=ticket_text(dependencies="SELLO-002"))
        self.epic("E09-carryover", readme=epic_readme("SELLO-E09", increment="Post-MVP"), SELLO_002=ticket_text("SELLO-002"))
        with self.assertRaisesRegex(ValueError, "MVP depends on Post-MVP"):
            self.validate()

    def test_rejects_missing_required_sections(self):
        for section in ("Acceptance criteria", "Context and starting points", "Implementation plan",
                        "Concrete cases and pitfalls", "Verification recipe"):
            self.epic(SELLO_001=ticket_text().replace(f"### {section}", "### Other"))
            with self.assertRaisesRegex(ValueError, section):
                self.load()

    def test_rejects_empty_deliverable(self):
        self.epic(SELLO_001=ticket_text().replace("An integrated implementation and evidence.", ""))
        with self.assertRaisesRegex(ValueError, "Deliverables"):
            self.load()

    def test_rejects_duplicate_ids_across_epics(self):
        self.epic(SELLO_001=ticket_text())
        self.epic("E02-design", readme=epic_readme("SELLO-E02"), SELLO_001=ticket_text())
        with self.assertRaisesRegex(ValueError, "Duplicate ticket"):
            self.validate()

    def test_rejects_missing_dependency_and_cycles(self):
        self.epic(SELLO_001=ticket_text(dependencies="SELLO-999"))
        with self.assertRaisesRegex(ValueError, "Unknown dependency"):
            self.validate()
        self.epic(SELLO_001=ticket_text(dependencies="SELLO-002"), SELLO_002=ticket_text("SELLO-002", dependencies="SELLO-001"))
        with self.assertRaisesRegex(ValueError, "cycle"):
            self.validate()

    def test_ready_requires_completed_dependencies(self):
        self.epic(SELLO_001=ticket_text(), SELLO_002=ticket_text("SELLO-002", status="Ready", dependencies="SELLO-001"))
        with self.assertRaisesRegex(ValueError, "unfinished dependencies"):
            self.validate()

    def test_downstream_task_requires_explicit_quality_dependency(self):
        self.epic(SELLO_005=ticket_text("SELLO-005"))
        with self.assertRaisesRegex(ValueError, "must depend on SELLO-004"):
            self.validate()

    def test_review_requires_quality_run_in_evidence_file(self):
        self.epic(SELLO_004=ticket_text("SELLO-004", status="Review"))
        with self.assertRaisesRegex(ValueError, "missing Quality run"):
            self.validate()
        self.write("docs/tickets/E01-foundation/SELLO-004/evidence.md", "# SELLO-004\n\n- **Quality run:** TODO\n")
        with self.assertRaisesRegex(ValueError, "placeholder Delivery evidence in Quality run"):
            self.validate()
        self.write("docs/tickets/E01-foundation/SELLO-004/evidence.md", "# SELLO-004\n\n- **Quality run:** run q004; retained report\n")
        self.validate()

    def test_done_requires_complete_evidence_and_a_handoff(self):
        self.epic(SELLO_001=ticket_text(status="Done"))
        with self.assertRaisesRegex(ValueError, "missing Delivery evidence"):
            self.validate()
        self.write("docs/tickets/E01-foundation/SELLO-001/evidence.md", EVIDENCE.replace("- **Review:** verified evidence\n", ""))
        with self.assertRaisesRegex(ValueError, "missing Review"):
            self.validate()
        self.write("docs/tickets/E01-foundation/SELLO-001/evidence.md", EVIDENCE.replace("**Gate results:** verified evidence", "**Gate results:** TBD"))
        with self.assertRaisesRegex(ValueError, "placeholder Delivery evidence"):
            self.validate()
        self.write("docs/tickets/E01-foundation/SELLO-001/evidence.md", EVIDENCE)
        with self.assertRaisesRegex(ValueError, "missing handoff.md"):
            self.validate()
        self.write("docs/tickets/E01-foundation/SELLO-001/handoff.md", "# SELLO-001\n\nWhat changed.\n")
        self.validate()

    def test_evidence_in_the_ticket_file_is_rejected(self):
        self.epic(SELLO_001=ticket_text(status="Done") + "\n### Delivery evidence\n- **Review:** here\n")
        with self.assertRaisesRegex(ValueError, "Delivery evidence belongs in evidence.md"):
            self.load()

    def test_epic_cannot_be_done_with_unfinished_children(self):
        self.epic(readme=epic_readme(status="Done"), SELLO_001=ticket_text())
        with self.assertRaisesRegex(ValueError, "unfinished children"):
            self.validate()

    def test_verify_report_hook_receives_the_quality_run_field(self):
        self.epic(SELLO_004=ticket_text("SELLO-004", status="Review"))
        self.write("docs/tickets/E01-foundation/SELLO-004/evidence.md", "# SELLO-004\n\n- **Quality run:** run q004\n")
        seen = []
        tickets.validate(self.load(), lambda ticket, fields: seen.append((ticket.identifier, fields["Quality run"])))
        self.assertEqual([("SELLO-004", "run q004")], seen)

    def test_status_table_is_printed_on_demand_not_committed(self):
        self.epic(SELLO_001=ticket_text(status="Done"), SELLO_002=ticket_text("SELLO-002", dependencies="SELLO-001"))
        table = tickets.status_table(self.load())
        self.assertIn("| SELLO-001 |", table)
        self.assertIn("Done: 1", table)
        self.assertIn("SELLO-002", table)
        self.assertEqual([], list((self.root / "docs/tickets").glob("*.md")))

    def test_cli_check_and_status(self):
        self.epic(SELLO_001=ticket_text())
        out, err = io.StringIO(), io.StringIO()
        with redirect_stdout(out), redirect_stderr(err):
            self.assertEqual(0, tickets.main(["check", "--skip-evidence"], root=self.root))
            self.assertEqual(0, tickets.main(["status", "E01"], root=self.root))
        self.assertIn("PASS: 1 epics, 1 tickets", out.getvalue())
        self.assertIn("SELLO-001", out.getvalue())
        self.epic(SELLO_001=ticket_text(status="Ready", dependencies="SELLO-999"))
        with redirect_stdout(out), redirect_stderr(err):
            self.assertEqual(1, tickets.main(["check", "--skip-evidence"], root=self.root))
        self.assertIn("Unknown dependency", err.getvalue())


class SplitEpicTests(unittest.TestCase):
    """The one-time move from epic files keeps every ticket's acceptance text identical."""

    EPIC = (
        "# SELLO-E01 — Foundation\n\n- **Status:** Done\n- **Goal:** Build.\n- **Exit:** Works.\n\n"
        "**Owner direction:** read [E02](E02-design.md) and [ADR](../../../decisions/0001-mvp-contract.md).\n\n"
        "## SELLO-001 — Complete a working task\n\n- **Type:** Task\n- **Priority:** P0\n- **Status:** Done\n"
        "- **Depends on:** None\n- **Gate:** G1\n\n" + SECTIONS +
        "\n### Execution progress\n\n- Started.\n\n### Delivery evidence\n\n- **Review:** merged in #1\n"
        "- **Quality run:** run q001; report docs/planning/mvp/quality-reports/SELLO-001.json sha256 abc\n\n"
        "## SELLO-002 — Second task\n\n- **Type:** Task\n- **Priority:** P0\n- **Status:** Backlog\n"
        "- **Depends on:** SELLO-001\n- **Gate:** G1\n\n" + SECTIONS
    )

    def test_split_separates_readme_tickets_and_evidence(self):
        readme, parts = tickets.split_epic_text(self.EPIC)
        self.assertTrue(readme.startswith("# SELLO-E01 — Foundation\n"))
        self.assertIn("**Owner direction:**", readme)
        self.assertEqual(["SELLO-001", "SELLO-002"], [identifier for identifier, _, _ in parts])
        first, second = parts[0], parts[1]
        self.assertTrue(first[1].startswith("# SELLO-001 — Complete a working task\n"))
        self.assertIn("### Execution progress", first[1])
        self.assertNotIn("Delivery evidence", first[1])
        self.assertIn("- **Review:** merged in #1", first[2])
        self.assertIsNone(second[2])

    def test_split_keeps_the_acceptance_text_identical(self):
        import fingerprint
        _, parts = tickets.split_epic_text(self.EPIC)
        for identifier, ticket_md, _ in parts:
            section = fingerprint.ticket_section(self.EPIC, identifier)
            self.assertEqual(fingerprint.acceptance_text(section), fingerprint.acceptance_text(ticket_md), identifier)


if __name__ == "__main__":
    unittest.main()
