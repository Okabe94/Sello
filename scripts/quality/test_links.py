"""The link checker fails on a missing file or heading and ignores external URLs and code blocks."""
import io
import tempfile
import unittest
from contextlib import redirect_stderr, redirect_stdout
from pathlib import Path

import links


class LinkTests(unittest.TestCase):
    def setUp(self):
        directory = tempfile.TemporaryDirectory()
        self.addCleanup(directory.cleanup)
        self.root = Path(directory.name)

    def write(self, relative, text):
        path = self.root / relative
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(text, encoding="utf-8")

    def problems(self):
        return links.broken_links(self.root)[0]

    def test_valid_relative_links_and_anchors_pass(self):
        self.write("docs/a.md", "# Title\n\n## SELLO-001 — Complete `it` (now)\n\nSee [b](sub/b.md#second-part) and [self](#sello-001--complete-it-now).\n")
        self.write("docs/sub/b.md", "# B\n\n## Second part\n\n[up](../a.md) ![img](pic.png) [dir](../)\n")
        self.write("docs/sub/pic.png", "")
        self.assertEqual([], self.problems())

    def test_missing_file_and_missing_heading_are_reported_with_location(self):
        self.write("docs/a.md", "# A\n\n[gone](gone.md)\n\n[no heading](b.md#nowhere)\n")
        self.write("docs/b.md", "# B\n")
        self.assertEqual(["docs/a.md:3: missing target gone.md", "docs/a.md:5: missing heading #nowhere in docs/b.md"], self.problems())

    def test_external_urls_and_code_blocks_are_ignored(self):
        self.write("docs/a.md", "# A\n\n[site](https://example.com/x) [mail](mailto:a@b.c)\n\n```bash\n[fake](missing.md)\n```\n")
        self.assertEqual([], self.problems())

    def test_duplicate_headings_get_numbered_anchors(self):
        self.write("docs/a.md", "# A\n\n## Same\n\n## Same\n\n[first](#same) [second](#same-1) [third](#same-2)\n")
        self.assertEqual(["docs/a.md:7: missing heading #same-2 in docs/a.md"], self.problems())

    def test_cli_reports_and_exits_nonzero_on_a_broken_link(self):
        self.write("README.md", "[x](nope.md)\n")
        out, err = io.StringIO(), io.StringIO()
        with redirect_stdout(out), redirect_stderr(err):
            self.assertEqual(1, links.main(root=self.root))
        self.assertIn("BROKEN: README.md:1: missing target nope.md", err.getvalue())
        self.write("README.md", "[x](docs/a.md)\n")
        self.write("docs/a.md", "# A\n")
        with redirect_stdout(out), redirect_stderr(err):
            self.assertEqual(0, links.main(root=self.root))
        self.assertIn("PASS: 1 relative links", out.getvalue())


if __name__ == "__main__":
    unittest.main()
