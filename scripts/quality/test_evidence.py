import io
import json
import tempfile
import unittest
from pathlib import Path

import evidence
import runner
from support import EPIC, epic, git, make_repo, ticket, write
from test_runner import DEVICE, Harness


class EvidenceTests(unittest.TestCase):
    def setUp(self):
        directory = tempfile.TemporaryDirectory()
        self.addCleanup(directory.cleanup)
        self.root = make_repo(directory.name)
        self.harness = Harness(self.root)
        self.field = self.verify_and_retain()

    def verify(self, identifier="SELLO-004", gate=None):
        code, path = runner.run(
            identifier, self.root, requested_gate=gate, execute=self.harness.execute,
            environment=lambda root: {}, devices=lambda: [DEVICE], stream=io.StringIO(),
        )
        return code, path

    def verify_and_retain(self):
        code, path = self.verify()
        self.assertEqual(0, code)
        return evidence.retain(self.root, path)

    def check(self, identifier="SELLO-004", gate="G1", field=None, fresh=True):
        evidence.check(self.root, identifier, gate, self.field if field is None else field, fresh)

    def retained(self):
        return evidence.retained_path(self.root, "SELLO-004")

    def rewrite(self, change):
        report = json.loads(self.retained().read_text(encoding="utf-8"))
        change(report)
        self.retained().write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
        try:
            return evidence.quality_run_line(self.root, "SELLO-004")
        except evidence.EvidenceError:
            return self.field  # malformed on purpose; the check fails before the field is read

    def assert_rejected(self, message, **arguments):
        with self.assertRaisesRegex(evidence.EvidenceError, message):
            self.check(**arguments)

    def test_fresh_passing_report_is_accepted(self):
        self.check()

    def test_quality_run_line_identifies_the_run(self):
        report = json.loads(self.retained().read_text(encoding="utf-8"))
        self.assertIn(report["run_id"], self.field)
        self.assertIn("SELLO-004 G1 passed", self.field)
        self.assertIn("docs/planning/mvp/quality-reports/SELLO-004.json", self.field)

    def test_missing_report_is_rejected(self):
        self.retained().unlink()
        self.assert_rejected("no retained quality report")

    def test_unparseable_report_is_rejected(self):
        self.retained().write_text("{not json", encoding="utf-8")
        self.assert_rejected("not valid JSON")

    def test_report_missing_a_required_field_is_rejected(self):
        field = self.rewrite(lambda report: report.pop("checks"))
        self.assert_rejected("malformed report: checks is required", field=field)

    def test_report_with_wrong_type_is_rejected(self):
        field = self.rewrite(lambda report: report["snapshot"].update(dirty="no"))
        self.assert_rejected("malformed report: snapshot.dirty", field=field)

    def test_unsupported_schema_version_is_rejected(self):
        field = self.rewrite(lambda report: report.update(schema_version=2))
        self.assert_rejected("malformed report: schema_version", field=field)

    def test_failed_report_is_rejected(self):
        field = self.rewrite(lambda report: report.update(outcome="failed"))
        self.assert_rejected("outcome is failed", field=field)

    def test_failed_run_cannot_be_retained(self):
        self.harness.exit_codes["board-tests"] = 1
        code, path = self.verify()
        self.assertNotEqual(0, code)
        with self.assertRaisesRegex(evidence.EvidenceError, "only a passed run"):
            evidence.retain(self.root, path)

    def test_passed_report_with_a_failed_check_is_rejected(self):
        field = self.rewrite(lambda report: report["checks"][0].update(exit_code=1))
        self.assert_rejected("check board-check exited 1", field=field)

    def test_report_for_another_ticket_is_rejected(self):
        write(self.root, EPIC, epic(
            ticket("SELLO-003", status="Done"),
            ticket("SELLO-004", status="Done", dependencies="SELLO-003"),
            ticket("SELLO-005", status="Review", dependencies="SELLO-004"),
        ))
        other = evidence.retained_path(self.root, "SELLO-005")
        other.write_bytes(self.retained().read_bytes())
        field = evidence.quality_run_line(self.root, "SELLO-005")
        self.assert_rejected("report is for SELLO-004", identifier="SELLO-005", field=field)

    def test_lower_gate_does_not_satisfy_a_higher_one(self):
        self.assert_rejected("G1 does not satisfy G2", gate="G2")

    def test_higher_gate_satisfies_a_lower_one(self):
        code, path = self.verify(gate="G2")
        self.assertEqual(0, code)
        self.check(field=evidence.retain(self.root, path))

    def test_code_change_after_the_run_is_stale(self):
        write(self.root, "app/src/Main.kt", "fun main() = println(3)\n")
        self.assert_rejected("stale")

    def test_untracked_source_after_the_run_is_stale(self):
        write(self.root, "app/src/Later.kt", "class Later\n")
        self.assert_rejected("stale")

    def test_historical_report_survives_later_unrelated_changes(self):
        write(self.root, "app/src/Main.kt", "fun main() = println(3)\n")
        self.check(fresh=False)

    def test_acceptance_edit_invalidates_even_historical_evidence(self):
        text = (self.root / EPIC).read_text(encoding="utf-8")
        head, tail = text.split("## SELLO-004", 1)
        write(self.root, EPIC, head + "## SELLO-004" + tail.replace("keeps working", "mostly works"))
        self.assert_rejected("acceptance text changed", fresh=False)

    def test_another_tickets_acceptance_edit_keeps_historical_evidence(self):
        text = (self.root / EPIC).read_text(encoding="utf-8")
        write(self.root, EPIC, text.replace("keeps working", "mostly works", 1))
        self.check(fresh=False)

    def test_status_and_evidence_edits_do_not_stale_the_report(self):
        text = (self.root / EPIC).read_text(encoding="utf-8").replace("In Progress\n- **Depends", "Review\n- **Depends")
        write(self.root, EPIC, text + f"\n### Delivery evidence\n- **Quality run:** {self.field}\n")
        write(self.root, "docs/planning/mvp/BOARD.md", "regenerated\n")
        self.check()

    def test_field_naming_a_different_run_is_rejected(self):
        self.assert_rejected("does not name run", field="run 20260101T000000Z-00000000, passed")

    def test_report_edited_after_being_recorded_is_rejected(self):
        self.rewrite(lambda report: report["warnings"].append("edited"))
        self.assert_rejected("does not match the recorded sha256")

    def test_status_at_reads_a_ticket_status_from_a_commit(self):
        self.assertEqual("In Progress", evidence.status_at(self.root, "HEAD", "SELLO-004"))
        self.assertEqual("Done", evidence.status_at(self.root, "HEAD", "SELLO-003"))
        self.assertIsNone(evidence.status_at(self.root, "HEAD", "SELLO-777"))
        self.assertIsNone(evidence.status_at(self.root, "no-such-ref", "SELLO-004"))


if __name__ == "__main__":
    unittest.main()
