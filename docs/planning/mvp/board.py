import argparse
import csv
import io
import re
import sys
from dataclasses import dataclass
from pathlib import Path


STATUSES = ("Backlog", "Ready", "In Progress", "Review", "Blocked", "Done")
REQUIRED_SECTIONS = (
    "Outcome", "Deliverables", "Acceptance criteria", "Tests", "Working checkpoint",
    "Context and starting points", "Implementation plan", "Concrete cases and pitfalls",
    "Verification recipe",
)
PRIORITIES = {"P0": "Highest", "P1": "High", "P2": "Medium"}
DELIVERY_FIELDS = ("Revision", "Requirement mapping", "Red / Green", "Gate results", "Device / Artifact", "Review")


@dataclass
class Ticket:
    identifier: str
    title: str
    epic_id: str
    source: Path
    issue_type: str
    priority: str
    status: str
    dependencies: tuple[str, ...]
    gate: str
    body: str


@dataclass
class Epic:
    identifier: str
    title: str
    source: Path
    status: str
    goal: str
    exit_criteria: str
    tickets: list[Ticket]
    increment: str = "MVP"


def metadata(text):
    entries = re.findall(r"^- \*\*([^*]+):\*\* (.+)$", text, re.MULTILINE)
    result = dict(entries)
    if len(result) != len(entries):
        raise ValueError("Duplicate metadata field")
    return result


def sections(text):
    return dict(re.findall(r"^### ([^\n]+)\n([\s\S]*?)(?=^### |\Z)", text, re.MULTILINE))


def require_fields(fields, required, identifier):
    for name in required:
        if not fields.get(name, "").strip():
            raise ValueError(f"{identifier}: missing {name}")


def parse_epic(source, text):
    heading = re.search(r"^# (SELLO-E\d{2}) — (.+)$", text, re.MULTILINE)
    if not heading:
        raise ValueError(f"{source}: invalid epic heading")
    epic_id, title = heading.groups()
    matches = list(re.finditer(r"^## (SELLO-\d{3}) — (.+)$", text, re.MULTILINE))
    if not matches:
        raise ValueError(f"{epic_id}: no tickets")
    epic_fields = metadata(text[: matches[0].start()])
    require_fields(epic_fields, ("Status", "Goal", "Exit"), epic_id)
    if int(epic_id.removeprefix("SELLO-E")) >= 9:
        require_fields(epic_fields, ("Increment",), epic_id)
    tickets = []
    for index, match in enumerate(matches):
        identifier, ticket_title = match.groups()
        end = matches[index + 1].start() if index + 1 < len(matches) else len(text)
        body = text[match.end() : end].strip()
        fields = metadata(body.split("###", 1)[0])
        require_fields(fields, ("Type", "Priority", "Status", "Depends on", "Gate"), identifier)
        content = sections(body)
        require_fields(content, REQUIRED_SECTIONS, identifier)
        raw_dependencies = fields["Depends on"]
        dependencies = () if raw_dependencies == "None" else tuple(
            dependency.strip() for dependency in raw_dependencies.split(",")
        )
        if len(set(dependencies)) != len(dependencies) or any(
            not re.fullmatch(r"SELLO-\d{3}", dependency) for dependency in dependencies
        ):
            raise ValueError(f"{identifier}: malformed or duplicate dependencies")
        tickets.append(Ticket(
            identifier, ticket_title, epic_id, source, fields["Type"], fields["Priority"],
            fields["Status"], dependencies, fields["Gate"], body,
        ))
    return Epic(epic_id, title, source, epic_fields["Status"], epic_fields["Goal"], epic_fields["Exit"], tickets,
                epic_fields.get("Increment", "MVP"))


def validate(epics, verify_report=None):
    if not epics:
        raise ValueError("No epic sources")
    epic_ids = [epic.identifier for epic in epics]
    if len(set(epic_ids)) != len(epic_ids):
        raise ValueError("Duplicate epic ID")
    tickets = {}
    increments = {}
    for epic in epics:
        if epic.increment not in ("MVP", "Post-MVP"):
            raise ValueError(f"{epic.identifier}: invalid increment")
        if epic.status not in STATUSES:
            raise ValueError(f"{epic.identifier}: unknown status")
        if epic.status == "Done" and any(ticket.status != "Done" for ticket in epic.tickets):
            raise ValueError(f"{epic.identifier}: unfinished children")
        for ticket in epic.tickets:
            if ticket.identifier in tickets:
                raise ValueError(f"Duplicate ticket ID: {ticket.identifier}")
            tickets[ticket.identifier] = ticket
            increments[ticket.identifier] = epic.increment
            if ticket.status not in STATUSES or ticket.priority not in PRIORITIES:
                raise ValueError(f"{ticket.identifier}: invalid status/priority")
            if ticket.issue_type not in ("Task", "Story") or ticket.gate not in ("G0", "G1", "G2", "G3"):
                raise ValueError(f"{ticket.identifier}: invalid type/gate")
    for ticket in tickets.values():
        requires_quality = int(ticket.identifier.removeprefix("SELLO-")) >= 4
        if int(ticket.identifier.removeprefix("SELLO-")) > 4 and "SELLO-004" not in ticket.dependencies:
            raise ValueError(f"{ticket.identifier}: must depend on SELLO-004")
        for dependency in ticket.dependencies:
            if dependency not in tickets:
                raise ValueError(f"{ticket.identifier}: Unknown dependency {dependency}")
            if increments[ticket.identifier] == "MVP" and increments[dependency] == "Post-MVP":
                raise ValueError(f"{ticket.identifier}: MVP depends on Post-MVP {dependency}")
        if ticket.status in ("Ready", "In Progress", "Review", "Done") and any(
            tickets[dependency].status != "Done" for dependency in ticket.dependencies
        ):
            raise ValueError(f"{ticket.identifier}: unfinished dependencies for {ticket.status}")
        if requires_quality and ticket.status in ("Review", "Done"):
            fields = metadata(sections(ticket.body).get("Delivery evidence", ""))
            require_fields(fields, ("Quality run",), ticket.identifier)
            if re.search(r"\b(TODO|TBD|Pending)\b|<[^>]+>", fields["Quality run"], re.IGNORECASE):
                raise ValueError(f"{ticket.identifier}: placeholder Delivery evidence in Quality run")
            if verify_report:
                verify_report(ticket, fields["Quality run"])
        if ticket.status == "Done":
            evidence = sections(ticket.body).get("Delivery evidence", "").strip()
            if not evidence:
                raise ValueError(f"{ticket.identifier}: missing Delivery evidence")
            fields = metadata(evidence)
            require_fields(fields, DELIVERY_FIELDS, ticket.identifier)
            for name in DELIVERY_FIELDS:
                if re.search(r"\b(TODO|TBD|Pending)\b|<[^>]+>", fields[name], re.IGNORECASE):
                    raise ValueError(f"{ticket.identifier}: placeholder Delivery evidence in {name}")
    visited, active = set(), set()

    def visit(identifier):
        if identifier in active:
            raise ValueError(f"Dependency cycle at {identifier}")
        if identifier in visited:
            return
        active.add(identifier)
        for dependency in tickets[identifier].dependencies:
            visit(dependency)
        active.remove(identifier)
        visited.add(identifier)

    for identifier in tickets:
        visit(identifier)


def slug(text):
    return re.sub(r"[^\w\- ]", "", text.lower()).replace(" ", "-")


def ticket_link(ticket):
    anchor = slug(f"{ticket.identifier} — {ticket.title}")
    return f"[{ticket.identifier}](epics/{ticket.source.name}#{anchor})"


def render_board(epics, context_epics=None, title="Sello MVP"):
    tickets = sorted((ticket for epic in epics for ticket in epic.tickets), key=lambda ticket: ticket.identifier)
    lookup = {ticket.identifier: ticket for epic in (context_epics if context_epics is not None else epics)
              for ticket in epic.tickets}
    done_count = sum(ticket.status == "Done" for ticket in tickets)
    epic_done = sum(epic.status == "Done" for epic in epics)
    counts = " · ".join(f"{status}: {sum(ticket.status == status for ticket in tickets)}" for status in STATUSES)
    lines = [
        f"# {title} — Kanban board", "", "Generated from `epics/*.md`; do not edit independently.", "",
        f"**Tickets Done:** {done_count}/{len(tickets)} · **Epics Done:** {epic_done}/{len(epics)}", "",
        counts, "", "[Workflow, gates and definition of done](README.md)", "", "## Epic outcomes", "",
        "| Epic | Outcome | Status | Done |", "| --- | --- | --- | --- |",
    ]
    for epic in epics:
        completed = sum(ticket.status == "Done" for ticket in epic.tickets)
        lines.append(f"| [{epic.identifier}](epics/{epic.source.name}) | {epic.goal} | {epic.status} | {completed}/{len(epic.tickets)} |")
    for status in ("Ready", "In Progress", "Review", "Blocked", "Backlog", "Done"):
        lines.extend(["", f"## {status}", ""])
        selected = [ticket for ticket in tickets if ticket.status == status]
        if not selected:
            lines.append("No tickets.")
            continue
        lines.extend(["| Ticket | Deliverable | Epic | Priority | Gate | Unmet dependencies |", "| --- | --- | --- | --- | --- | --- |"])
        for ticket in selected:
            unmet = ", ".join(ticket_link(lookup[dependency]) for dependency in ticket.dependencies if lookup[dependency].status != "Done") or "None"
            lines.append(f"| {ticket_link(ticket)} | {ticket.title} | {ticket.epic_id} | {ticket.priority} | {ticket.gate} | {unmet} |")
    eligible = [ticket_link(ticket) for ticket in tickets if ticket.status == "Backlog" and all(lookup[dependency].status == "Done" for dependency in ticket.dependencies)]
    lines.extend(["", "## Ready promotion candidates", "", ", ".join(eligible) or "None. Complete current Ready tickets first.", ""])
    return "\n".join(lines)


def csv_text(headers, rows):
    output = io.StringIO(newline="")
    writer = csv.writer(output, lineterminator="\n")
    writer.writerow(headers)
    writer.writerows(rows)
    return output.getvalue()


def render_issue_csv(epics, shared_contract):
    rows = []
    for epic in epics:
        parent = str(1000 + int(epic.identifier.split("E")[-1]))
        rows.append([parent, "Epic", f"{epic.identifier} — {epic.title}",
                     f"Local ID: {epic.identifier}\nIncrement: {epic.increment}\nGoal: {epic.goal}\nExit: {epic.exit_criteria}",
                     "", "Highest", epic.status, epic.identifier.lower()])
        for ticket in epic.tickets:
            repository_source = f"docs/planning/mvp/epics/{ticket.source.name}"
            description = (
                f"Local ID: {ticket.identifier}\nEpic: {epic.identifier}\nIncrement: {epic.increment}\n"
                f"Repository source: {repository_source}\n"
                "Resource links resolve relative to that repository file.\n"
                "Shared tools: docs/planning/mvp/EXECUTION_GUIDE.md\n\n"
                f"{ticket.body}\n\n{shared_contract}"
            )
            rows.append([str(int(ticket.identifier.split("-")[-1])), ticket.issue_type,
                         f"{ticket.identifier} — {ticket.title}", description, parent,
                         PRIORITIES[ticket.priority], ticket.status,
                         ticket.identifier.lower()])
    return csv_text(["Issue ID", "Issue Type", "Summary", "Description", "Parent", "Priority", "Status", "Labels"], rows)


def render_dependency_csv(epics):
    rows = [[dependency, "blocks", ticket.identifier] for epic in epics for ticket in epic.tickets for dependency in ticket.dependencies]
    return csv_text(["Source local ID", "Link type", "Target local ID"], rows)


def write_outputs(root, outputs, check):
    stale = []
    for filename, text in outputs.items():
        path = root / filename
        if check:
            if not path.exists() or path.read_text(encoding="utf-8") != text:
                stale.append(filename)
        else:
            path.write_text(text, encoding="utf-8")
    if stale:
        raise ValueError(f"Stale generated views: {', '.join(stale)}; run board.py")


def report_evidence(board_root, base):
    """Validate retained quality reports for Review/Done tickets from SELLO-004 onward."""
    def check(ticket, quality_run):
        repository = board_root.parents[2]
        sys.path.insert(0, str(repository / "scripts/quality"))
        try:
            import evidence
        except ImportError:
            raise ValueError(f"{ticket.identifier}: evidence validator scripts/quality/evidence.py is unavailable") from None
        # A ticket already Done at the base revision keeps its historical tested snapshot.
        fresh = ticket.status != "Done" or evidence.status_at(repository, base, ticket.identifier) != "Done"
        try:
            evidence.check(repository, ticket.identifier, ticket.gate, quality_run, fresh)
        except evidence.EvidenceError as error:
            raise ValueError(f"{ticket.identifier}: {error}") from None
    return check


def main():
    parser = argparse.ArgumentParser(description="Validate and generate the repository-owned Sello MVP and post-MVP boards.")
    parser.add_argument("--check", action="store_true", help="Validate without writing; fail on stale generated files.")
    parser.add_argument("--skip-evidence", action="store_true",
                        help="Check structure and generated views only. Used inside a quality run; never completion validation.")
    parser.add_argument("--base", default="HEAD",
                        help="Revision to compare statuses with; tickets not Done there need a report matching current inputs.")
    arguments = parser.parse_args()
    root = Path(__file__).resolve().parent
    try:
        epics = [parse_epic(path, path.read_text(encoding="utf-8")) for path in sorted((root / "epics").glob("*.md"))]
        validate(epics, None if arguments.skip_evidence else report_evidence(root, arguments.base))
        mvp_epics = [epic for epic in epics if epic.increment == "MVP"]
        roadmap_epics = [epic for epic in epics if epic.increment == "Post-MVP"]
        readme = (root / "README.md").read_text(encoding="utf-8")
        shared_contract = readme.split("## Definition of done — every ticket\n", 1)[1].split("## Milestones and safe integration", 1)[0]
        outputs = {
            "BOARD.md": render_board(mvp_epics, epics),
            "jira-import.csv": render_issue_csv(mvp_epics, "Shared definition of done and gates:\n" + shared_contract),
            "dependency-links.csv": render_dependency_csv(mvp_epics),
            "ROADMAP.md": render_board(roadmap_epics, epics, "Sello post-MVP roadmap"),
            "roadmap-jira-import.csv": render_issue_csv(roadmap_epics, "Shared definition of done and gates:\n" + shared_contract),
            "roadmap-dependency-links.csv": render_dependency_csv(roadmap_epics),
        }
        write_outputs(root, outputs, arguments.check)
    except (ValueError, OSError, IndexError) as error:
        print(f"Board validation failed: {error}", file=sys.stderr)
        return 1
    count = sum(len(epic.tickets) for epic in epics)
    print(f"PASS: {len(epics)} epics, {count} tickets; {'checked' if arguments.check else 'generated'} board and CSV views")
    return 0


if __name__ == "__main__":
    sys.exit(main())
