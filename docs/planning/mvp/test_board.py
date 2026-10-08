import csv
import io
import tempfile
import unittest
from contextlib import redirect_stdout
from pathlib import Path
from unittest.mock import patch

import board


def epic_source(ticket_id="SELLO-001", status="Backlog", dependencies="None"):
    return f"""# SELLO-E01 — Foundation

- **Status:** Backlog
- **Goal:** Build a trustworthy foundation.
- **Exit:** Working app and verified contracts.

## {ticket_id} — Complete a working task

- **Type:** Task
- **Priority:** P0
- **Status:** {status}
- **Depends on:** {dependencies}
- **Gate:** G1

### Outcome
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


class BoardTests(unittest.TestCase):
    def parse(self, source):
        return board.parse_epic(Path("E01-foundation.md"), source)

    def test_parses_complete_task(self):
        epic = self.parse(epic_source(status="Ready"))
        board.validate([epic])
        self.assertEqual(epic.tickets[0].dependencies, ())
        self.assertEqual(epic.tickets[0].gate, "G1")

    def test_preserves_post_mvp_increment(self):
        source = epic_source().replace("- **Goal:**", "- **Increment:** Post-MVP\n- **Goal:**", 1)
        self.assertEqual(getattr(self.parse(source), "increment", "MVP"), "Post-MVP")

    def test_rejects_unknown_increment(self):
        source = epic_source().replace("- **Goal:**", "- **Increment:** Unknown\n- **Goal:**", 1)
        with self.assertRaisesRegex(ValueError, "invalid increment"):
            board.validate([self.parse(source)])

    def test_mvp_cannot_depend_on_post_mvp(self):
        mvp = self.parse(epic_source(dependencies="SELLO-002"))
        roadmap_source = epic_source(ticket_id="SELLO-002").replace("SELLO-E01", "SELLO-E02")
        roadmap_source = roadmap_source.replace("- **Goal:**", "- **Increment:** Post-MVP\n- **Goal:**", 1)
        with self.assertRaisesRegex(ValueError, "MVP depends on Post-MVP"):
            board.validate([mvp, self.parse(roadmap_source)])

    def test_csv_identifies_post_mvp_scope(self):
        source = epic_source().replace("- **Goal:**", "- **Increment:** Post-MVP\n- **Goal:**", 1)
        rows = list(csv.DictReader(io.StringIO(board.render_issue_csv([self.parse(source)], "Shared gates"))))
        self.assertIn("Increment: Post-MVP", rows[0]["Description"])
        self.assertIn("Increment: Post-MVP", rows[1]["Description"])

    def test_new_epic_requires_explicit_increment(self):
        with self.assertRaisesRegex(ValueError, "missing Increment"):
            self.parse(epic_source().replace("SELLO-E01", "SELLO-E09"))

    def test_roadmap_board_resolves_mvp_prerequisites_without_counting_them(self):
        mvp = self.parse(epic_source())
        source = epic_source(ticket_id="SELLO-002", dependencies="SELLO-001")
        source = source.replace("SELLO-E01", "SELLO-E09").replace("- **Goal:**", "- **Increment:** Post-MVP\n- **Goal:**", 1)
        roadmap = self.parse(source)
        board.validate([mvp, roadmap])
        output = board.render_board([roadmap], [mvp, roadmap], "Sello post-MVP roadmap")
        self.assertIn("# Sello post-MVP roadmap", output)
        self.assertIn("**Tickets Done:** 0/1", output)
        self.assertIn("[SELLO-001]", output)
        self.assertIn("[SELLO-002]", output)

    def test_generator_keeps_increment_exports_separate_and_checks_roadmap_freshness(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / "epics").mkdir()
            (root / "README.md").write_text(
                "## Definition of done — every ticket\nShared gates\n## Milestones and safe integration\n",
                encoding="utf-8",
            )
            (root / "epics/E01-foundation.md").write_text(epic_source(), encoding="utf-8")
            roadmap = epic_source(ticket_id="SELLO-002", dependencies="SELLO-001")
            roadmap = roadmap.replace("SELLO-E01", "SELLO-E09").replace("- **Goal:**", "- **Increment:** Post-MVP\n- **Goal:**", 1)
            (root / "epics/E09-carryover.md").write_text(roadmap, encoding="utf-8")
            with patch.object(board, "__file__", str(root / "board.py")), patch("sys.argv", ["board.py"]), redirect_stdout(io.StringIO()):
                self.assertEqual(board.main(), 0)
            mvp_rows = list(csv.DictReader(io.StringIO((root / "jira-import.csv").read_text())))
            roadmap_rows = list(csv.DictReader(io.StringIO((root / "roadmap-jira-import.csv").read_text())))
            self.assertEqual([row["Issue ID"] for row in mvp_rows], ["1001", "1"])
            self.assertEqual([row["Issue ID"] for row in roadmap_rows], ["1009", "2"])
            links = list(csv.DictReader(io.StringIO((root / "roadmap-dependency-links.csv").read_text())))
            self.assertEqual(links[0]["Source local ID"], "SELLO-001")
            self.assertEqual(links[0]["Target local ID"], "SELLO-002")
            (root / "ROADMAP.md").write_text("stale\n", encoding="utf-8")
            with patch.object(board, "__file__", str(root / "board.py")), patch("sys.argv", ["board.py", "--check"]), redirect_stdout(io.StringIO()), patch("sys.stderr", new_callable=io.StringIO) as errors:
                self.assertEqual(board.main(), 1)
                self.assertIn("ROADMAP.md", errors.getvalue())

    def test_rejects_missing_acceptance(self):
        with self.assertRaisesRegex(ValueError, "Acceptance criteria"):
            self.parse(epic_source().replace("### Acceptance criteria", "### Other"))

    def test_rejects_empty_deliverable(self):
        with self.assertRaisesRegex(ValueError, "Deliverables"):
            self.parse(epic_source().replace("An integrated implementation and evidence.", ""))

    def test_rejects_missing_executor_context(self):
        with self.assertRaisesRegex(ValueError, "Context and starting points"):
            self.parse(epic_source().replace("### Context and starting points", "### Other"))

    def test_rejects_missing_implementation_plan(self):
        with self.assertRaisesRegex(ValueError, "Implementation plan"):
            self.parse(epic_source().replace("### Implementation plan", "### Other"))

    def test_rejects_missing_verification_recipe(self):
        with self.assertRaisesRegex(ValueError, "Verification recipe"):
            self.parse(epic_source().replace("### Verification recipe", "### Other"))

    def test_rejects_missing_concrete_examples(self):
        with self.assertRaisesRegex(ValueError, "Concrete cases and pitfalls"):
            self.parse(epic_source().replace("### Concrete cases and pitfalls", "### Other"))

    def test_rejects_duplicate_ids(self):
        epic = self.parse(epic_source())
        epic.tickets.append(epic.tickets[0])
        with self.assertRaisesRegex(ValueError, "Duplicate ticket"):
            board.validate([epic])

    def test_rejects_missing_dependency(self):
        with self.assertRaisesRegex(ValueError, "Unknown dependency"):
            board.validate([self.parse(epic_source(dependencies="SELLO-999"))])

    def test_rejects_cycle(self):
        source = epic_source(dependencies="SELLO-002")
        other = epic_source(ticket_id="SELLO-002", dependencies="SELLO-001")
        source += "\n" + other[other.index("## SELLO-002") :]
        with self.assertRaisesRegex(ValueError, "cycle"):
            board.validate([self.parse(source)])

    def test_ready_requires_completed_dependencies(self):
        source = epic_source()
        other = epic_source(ticket_id="SELLO-002", status="Ready", dependencies="SELLO-001")
        source += "\n" + other[other.index("## SELLO-002") :]
        with self.assertRaisesRegex(ValueError, "unfinished dependencies"):
            board.validate([self.parse(source)])

    def test_done_requires_delivery_evidence(self):
        with self.assertRaisesRegex(ValueError, "Delivery evidence"):
            board.validate([self.parse(epic_source(status="Done"))])

    def test_downstream_task_requires_explicit_quality_dependency(self):
        with self.assertRaisesRegex(ValueError, "must depend on SELLO-004"):
            board.validate([self.parse(epic_source(ticket_id="SELLO-005"))])

    def test_review_requires_quality_run(self):
        with self.assertRaisesRegex(ValueError, "missing Quality run"):
            board.validate([self.parse(epic_source(ticket_id="SELLO-004", status="Review"))])

    def test_done_requires_quality_run(self):
        source = self.completed_source().replace("SELLO-001", "SELLO-004")
        with self.assertRaisesRegex(ValueError, "missing Quality run"):
            board.validate([self.parse(source)])

    def test_placeholder_quality_run_is_rejected(self):
        source = epic_source(ticket_id="SELLO-004", status="Review")
        source += "\n### Delivery evidence\n\n- **Quality run:** TODO\n"
        with self.assertRaisesRegex(ValueError, "placeholder Delivery evidence in Quality run"):
            board.validate([self.parse(source)])

    def test_review_with_quality_reference_is_accepted(self):
        source = epic_source(ticket_id="SELLO-004", status="Review")
        source += "\n### Delivery evidence\n\n- **Quality run:** retained run q004, G1, matching snapshot\n"
        board.validate([self.parse(source)])

    def completed_source(self):
        evidence = "\n### Delivery evidence\n\n" + "\n".join(
            f"- **{name}:** verified evidence" for name in board.DELIVERY_FIELDS
        )
        return epic_source(status="Done") + evidence

    def test_complete_delivery_evidence_is_accepted(self):
        board.validate([self.parse(self.completed_source())])

    def test_partial_delivery_evidence_is_rejected(self):
        source = self.completed_source().replace("- **Review:** verified evidence", "")
        with self.assertRaisesRegex(ValueError, "missing Review"):
            board.validate([self.parse(source)])

    def test_placeholder_delivery_evidence_is_rejected(self):
        source = self.completed_source().replace("**Gate results:** verified evidence", "**Gate results:** TODO")
        with self.assertRaisesRegex(ValueError, "placeholder Delivery evidence"):
            board.validate([self.parse(source)])

    def test_epic_cannot_be_done_with_unfinished_children(self):
        source = epic_source().replace("**Status:** Backlog", "**Status:** Done", 1)
        with self.assertRaisesRegex(ValueError, "unfinished children"):
            board.validate([self.parse(source)])

    def test_csv_preserves_hierarchy_and_complete_ticket_description(self):
        epic = self.parse(epic_source())
        output = board.render_issue_csv([epic], "Shared definition of done")
        rows = list(csv.DictReader(io.StringIO(output)))
        self.assertEqual(rows[1]["Parent"], rows[0]["Issue ID"])
        self.assertIn("### Tests", rows[1]["Description"])
        self.assertIn("Shared definition of done", rows[1]["Description"])
        self.assertIn("SELLO-001", rows[1]["Summary"])

    def test_csv_preserves_executor_sections_and_repository_source(self):
        epic = self.parse(epic_source())
        rows = list(csv.DictReader(io.StringIO(board.render_issue_csv([epic], "Shared gates"))))
        self.assertIn("Repository source: docs/planning/mvp/epics/E01-foundation.md", rows[1]["Description"])
        for section in ["Context and starting points", "Implementation plan", "Concrete cases and pitfalls", "Verification recipe"]:
            self.assertIn(f"### {section}", rows[1]["Description"])

    def test_dependency_link_points_from_prerequisite_to_dependent(self):
        source = epic_source()
        other = epic_source(ticket_id="SELLO-002", dependencies="SELLO-001")
        source += "\n" + other[other.index("## SELLO-002") :]
        output = board.render_dependency_csv([self.parse(source)])
        rows = list(csv.DictReader(io.StringIO(output)))
        self.assertEqual(rows[0]["Source local ID"], "SELLO-001")
        self.assertEqual(rows[0]["Target local ID"], "SELLO-002")
        self.assertEqual(rows[0]["Link type"], "blocks")

    def test_check_detects_stale_generated_views(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            outputs = {"BOARD.md": "current\n"}
            board.write_outputs(root, outputs, check=False)
            board.write_outputs(root, outputs, check=True)
            (root / "BOARD.md").write_text("stale\n", encoding="utf-8")
            with self.assertRaisesRegex(ValueError, "Stale"):
                board.write_outputs(root, outputs, check=True)


if __name__ == "__main__":
    unittest.main()
