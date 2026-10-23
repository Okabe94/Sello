"""The synthetic histories must be files the app accepts, at the sizes the profile names."""
import json
import re
import unicodedata
import unittest
from datetime import date

import dataset

TODAY = date(2026, 10, 23)
UUID = re.compile(r"[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")
MOMENT = re.compile(r"\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z")


def folded(name):
    return unicodedata.normalize("NFD", name.casefold()).encode("ascii", "ignore")


class LargeProfileTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.data = dataset.encode(dataset.build(dataset.PROFILES["large"], TODAY))
        cls.backup = json.loads(cls.data)
        cls.financial = cls.backup["financial"]

    def test_it_has_the_profiles_counts(self):
        self.assertEqual(20, len(self.financial["categories"]))
        self.assertEqual(10_000, len(self.financial["expenses"]))
        self.assertEqual(2_000, len(self.financial["incomes"]))

    def test_it_spans_twelve_past_months_and_the_current_one(self):
        days = [row["date"] for row in self.financial["expenses"] + self.financial["incomes"]]
        self.assertEqual("2025-10-01", min(days))
        self.assertEqual("2026-10-23", max(days))
        self.assertEqual(13, len({day[:7] for day in days}))

    def test_it_fits_the_backup_size_and_element_limits(self):
        self.assertLess(len(self.data), 10 * 1024 * 1024)
        elements = sum(len(rows) for rows in self.financial.values() if isinstance(rows, list))
        self.assertLess(elements, 100_000)

    def test_identifiers_and_sequences_are_unique_and_well_formed(self):
        for key in ("categories", "expenses", "incomes"):
            ids = [row["id"] for row in self.financial[key]]
            self.assertEqual(len(ids), len(set(ids)), key)
            self.assertTrue(all(UUID.fullmatch(value) for value in ids), key)
        for key in ("expenses", "incomes"):
            sequences = [row["sequence"] for row in self.financial[key]]
            self.assertEqual(len(sequences), len(set(sequences)), key)

    def test_every_record_points_at_a_category_in_the_file(self):
        known = {row["id"] for row in self.financial["categories"]}
        for key in ("defaultLimits", "monthLimits", "expenses"):
            self.assertTrue(all(row["categoryId"] in known for row in self.financial[key]), key)

    def test_amounts_are_whole_pesos_within_the_record_range(self):
        for key in ("expenses", "incomes"):
            for row in self.financial[key]:
                self.assertIsInstance(row["amount"], int)
                self.assertTrue(1 <= row["amount"] <= 999_999_999_999, row)
                self.assertEqual("COP", row["currency"])

    def test_text_keeps_to_the_stored_form(self):
        names = [row["name"] for row in self.financial["categories"]]
        self.assertTrue(all(1 <= len(name) <= 24 and name == name.strip() for name in names))
        self.assertEqual(len(names), len({folded(name) for name in names}))
        for row in self.financial["expenses"]:
            note = row["note"]
            self.assertTrue(note is None or (0 < len(note) <= 60 and note == note.strip()))
        for row in self.financial["incomes"]:
            self.assertEqual(row["source"] == "other", row["sourceName"] is not None)

    def test_moments_are_utc_seconds(self):
        moments = [self.backup["exportedAt"]] + [
            row[key] for rows in ("categories", "expenses", "incomes")
            for row in self.financial[rows] for key in ("createdAt", "updatedAt")
        ]
        self.assertTrue(all(MOMENT.fullmatch(value) for value in moments))

    def test_the_same_profile_and_day_make_the_same_bytes(self):
        again = dataset.encode(dataset.build(dataset.PROFILES["large"], TODAY))
        self.assertEqual(self.data, again)


if __name__ == "__main__":
    unittest.main()
