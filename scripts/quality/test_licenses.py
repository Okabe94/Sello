"""Each release library needs a reviewed licence, and the committed list must stay current."""
import unittest

import licenses

LOCK = """# This is a Gradle generated file for dependency locking.
androidx.core:core:1.16.0=debugRuntimeClasspath,releaseRuntimeClasspath
androidx.datastore:datastore-preferences-external-protobuf:1.2.1=releaseRuntimeClasspath
junit:junit:4.13.2=debugUnitTestRuntimeClasspath
org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2=releaseCompileClasspath,releaseRuntimeClasspath
empty=
"""

TABLE = {
    "groups": {"androidx": "Apache-2.0", "org.jetbrains": "Apache-2.0", "org.jetbrains.kotlinx": "MIT"},
    "modules": {"androidx.datastore:datastore-preferences-external-protobuf": "BSD-3-Clause"},
}


class LicencesTest(unittest.TestCase):
    def test_only_release_runtime_libraries_are_listed(self):
        self.assertEqual([
            ("androidx.core", "core", "1.16.0"),
            ("androidx.datastore", "datastore-preferences-external-protobuf", "1.2.1"),
            ("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.10.2"),
        ], licenses.release_modules(LOCK))

    def test_a_module_entry_overrides_its_group_and_the_longest_group_wins(self):
        self.assertEqual("BSD-3-Clause", licenses.licence(TABLE, "androidx.datastore", "datastore-preferences-external-protobuf"))
        self.assertEqual("Apache-2.0", licenses.licence(TABLE, "androidx.datastore", "datastore"))
        self.assertEqual("MIT", licenses.licence(TABLE, "org.jetbrains.kotlinx", "kotlinx-coroutines-core"))
        self.assertEqual("Apache-2.0", licenses.licence(TABLE, "org.jetbrains", "annotations"))

    def test_a_group_prefix_matches_whole_segments_only(self):
        self.assertIsNone(licenses.licence(TABLE, "androidxtra", "core"))

    def test_an_unreviewed_library_fails_with_its_name(self):
        with self.assertRaisesRegex(licenses.LicenceError, "com.example:new"):
            licenses.listing([("com.example", "new", "1.0")], TABLE)

    def test_the_listing_counts_and_names_every_library(self):
        text = licenses.listing(licenses.release_modules(LOCK), TABLE)
        self.assertIn("| Apache-2.0 | 1 |", text)
        self.assertIn("| BSD-3-Clause | 1 |", text)
        self.assertIn("| `org.jetbrains.kotlinx:kotlinx-coroutines-core` | 1.10.2 | MIT |", text)

    def test_the_repository_lists_every_locked_release_library(self):
        self.assertEqual(0, licenses.main(["check"]))


if __name__ == "__main__":
    unittest.main()
