"""One-off, owner-approved network review of the release libraries. Never part of a gate.

For every `releaseRuntimeClasspath` module in app/gradle.lockfile it:
- asks osv.dev for published advisories affecting that exact version, sending only the
  public coordinates (`group:name`, version, ecosystem Maven);
- reads the module's published POM from Google's Maven repository or Maven Central,
  following parent POMs, and compares its licence with the reviewed table.
It writes docs/release/advisories.md, dated, and changes nothing else.
"""
import json
import sys
import urllib.request
import xml.etree.ElementTree as ElementTree
from datetime import date
from pathlib import Path

import licenses

ROOT = Path(__file__).resolve().parents[2]
REPORT = Path("docs/release/advisories.md")
OSV = "https://api.osv.dev/v1/querybatch"
REPOSITORIES = ("https://dl.google.com/dl/android/maven2", "https://repo1.maven.org/maven2")
SPDX = {
    "the apache software license, version 2.0": "Apache-2.0", "apache-2.0": "Apache-2.0",
    "apache license, version 2.0": "Apache-2.0", "the apache license, version 2.0": "Apache-2.0",
    "apache license 2.0": "Apache-2.0", "apache 2.0": "Apache-2.0", "bsd-3-clause": "BSD-3-Clause",
    "3-clause bsd license": "BSD-3-Clause", "bsd 3-clause license": "BSD-3-Clause",
}


def fetch(url, data=None):
    request = urllib.request.Request(url, data=data, headers={"Content-Type": "application/json"} if data else {})
    with urllib.request.urlopen(request, timeout=30) as response:
        return response.read()


def pom(group, name, version):
    path = f"{group.replace('.', '/')}/{name}/{version}/{name}-{version}.pom"
    for repository in REPOSITORIES:
        try:
            return fetch(f"{repository}/{path}")
        except OSError:
            continue
    return None


def strip(tag):
    return tag.split("}", 1)[-1]


def pom_licences(group, name, version, depth=0):
    text = pom(group, name, version)
    if text is None or depth > 5:
        return []
    root = ElementTree.fromstring(text)
    ns = root.tag.split("}")[0] + "}" if root.tag.startswith("{") else ""
    found = [element.text.strip() for element in root.findall(f"{ns}licenses/{ns}license/{ns}name") if element.text]
    if found:
        return found
    parent = next((element for element in root if strip(element.tag) == "parent"), None)
    if parent is None:
        return []
    values = {strip(child.tag): (child.text or "").strip() for child in parent}
    return pom_licences(values.get("groupId"), values.get("artifactId"), values.get("version"), depth + 1)


def advisories(modules):
    queries = [{"package": {"name": f"{g}:{n}", "ecosystem": "Maven"}, "version": v} for g, n, v in modules]
    answer = json.loads(fetch(OSV, json.dumps({"queries": queries}).encode("utf-8")))
    return [[vuln["id"] for vuln in result.get("vulns", [])] for result in answer["results"]]


def main():
    modules = licenses.release_modules((ROOT / licenses.LOCKFILE).read_text(encoding="utf-8"))
    table = json.loads(licenses.TABLE.read_text(encoding="utf-8"))
    found = advisories(modules)
    rows, mismatches, affected = [], [], []
    for (group, name, version), ids in zip(modules, found):
        published = pom_licences(group, name, version)
        normalised = sorted({SPDX.get(item.lower(), item) for item in published})
        reviewed = licenses.licence(table, group, name)
        if normalised != [reviewed]:
            mismatches.append(f"`{group}:{name}` {version}: published {', '.join(published) or 'none found'}; reviewed {reviewed}")
        if ids:
            affected.append(f"`{group}:{name}` {version}: {', '.join(ids)}")
        rows.append(f"| `{group}:{name}` | {version} | {', '.join(ids) or 'none'} | {', '.join(normalised) or 'not found'} |")
    lines = [
        "# Release library advisories and published licences",
        "",
        f"Checked on {date.today().isoformat()} with `python3 scripts/quality/advisories.py`, once, with",
        "the owner's permission for network access: [osv.dev](https://osv.dev) for advisories (Maven",
        "ecosystem, exact versions) and each library's published POM for its licence. Not a gate:",
        "a later dependency change needs this review again.",
        "",
        f"- Libraries: {len(modules)}",
        f"- With a published advisory: {len(affected)}",
        f"- Published licence differing from the reviewed table: {len(mismatches)}",
        "",
        *([f"- {item}" for item in affected] or ["No advisory affects these versions."]),
        "",
        *([f"- {item}" for item in mismatches] or ["Every published licence matches the reviewed table."]),
        "",
        "| Library | Version | Advisories | Published licence |",
        "| --- | --- | --- | --- |",
        *rows,
        "",
    ]
    (ROOT / REPORT).write_text("\n".join(lines), encoding="utf-8")
    print(f"Wrote {REPORT}: {len(affected)} with advisories, {len(mismatches)} licence differences")
    return 0


if __name__ == "__main__":
    sys.exit(main())
