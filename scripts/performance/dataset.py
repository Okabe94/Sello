"""Synthetic Sello histories for performance runs, written as version 1 backup files.

The file goes into the app through Respaldo → Restaurar, so the app's own validation
and replacement are what put it in the database. Everything here is invented: the
names, amounts and notes come from fixed lists and a seeded generator, and the same
profile and day always make the same bytes. See docs/testing/performance-profile.md.
"""
import argparse
import json
import random
import sys
from dataclasses import dataclass
from datetime import date, datetime, timedelta, timezone
from pathlib import Path

ZONE = "America/Bogota"
# Bogotá has no daylight saving: local noon is always 17:00 UTC.
UTC_OFFSET_HOURS = 5
MAX_BYTES = 10 * 1024 * 1024
MAX_ELEMENTS = 100_000
MAX_AMOUNT = 999_999_999_999

CATEGORIES = (
    ("Mercado", "shopping_cart", 1_200_000), ("Restaurantes", "restaurant", 600_000),
    ("Transporte", "directions_bus", 350_000), ("Arriendo", "home", 2_100_000),
    ("Servicios", "home", 450_000), ("Salud", "medical_services", 300_000),
    ("Ropa", "checkroom", 250_000), ("Café", "local_cafe", 120_000),
    ("Educación", "school", 800_000), ("Mascotas", "pets", 200_000),
    ("Gimnasio", "fitness_center", 150_000), ("Viajes", "flight", 900_000),
    ("Celular", "phone_iphone", 90_000), ("Niños", "child_care", 500_000),
    ("Regalos", "shopping_cart", 200_000), ("Cine", "theaters", 80_000),
    ("Domicilios", "restaurant", 250_000), ("Libros", "school", 100_000),
    ("Hogar", "home", 300_000), ("Taxis", "directions_bus", 150_000),
)
NOTES = (
    "Plaza de mercado", "Almuerzo con el equipo", "Recarga", "Pasajes de la semana",
    "Cuota mensual", "Droguería", "Regalo de cumpleaños", "Arreglo de la nevera",
    "Domicilio del viernes", "Matrícula del semestre", "Concentrado para el perro",
    "Tinto y pandebono", "Factura del agua", "Camisa para la boda",
)
INCOME_SOURCES = ("salary", "freelance", "passive_income", "transfer", "other")
OTHER_INCOME_NAMES = ("Venta de bicicleta", "Reembolso", "Premio")


@dataclass(frozen=True)
class Profile:
    categories: int
    expenses: int
    incomes: int
    past_months: int


PROFILES = {
    "large": Profile(categories=20, expenses=10_000, incomes=2_000, past_months=12),
    "typical": Profile(categories=8, expenses=600, incomes=40, past_months=3),
}


class Ids:
    """Version 4 shaped identifiers from the seeded generator, so a file is repeatable."""

    def __init__(self, rng):
        self.rng = rng

    def next(self):
        value = self.rng.getrandbits(128)
        value = (value & ~(0xF << 76)) | (0x4 << 76)
        value = (value & ~(0x3 << 62)) | (0x2 << 62)
        text = f"{value:032x}"
        return f"{text[:8]}-{text[8:12]}-{text[12:16]}-{text[16:20]}-{text[20:]}"


def month_start(day, months_back):
    year, month = day.year, day.month - months_back
    while month < 1:
        year, month = year - 1, month + 12
    return date(year, month, 1)


def moment(day, seconds):
    local_noon = datetime(day.year, day.month, day.day, 12, tzinfo=timezone.utc)
    instant = local_noon + timedelta(hours=UTC_OFFSET_HOURS, seconds=seconds)
    return instant.strftime("%Y-%m-%dT%H:%M:%SZ")


def spread(count, first, last):
    """[count] days from [first] to [last], in order, as evenly as whole days allow."""
    days = (last - first).days + 1
    return [first + timedelta(days=index * days // count) for index in range(count)]


def build(profile, today, seed=35):
    if profile.categories > len(CATEGORIES):
        raise ValueError(f"at most {len(CATEGORIES)} categories")
    rng = random.Random(seed)
    ids = Ids(rng)
    first = month_start(today, profile.past_months)
    first_month = f"{first:%Y-%m}"
    created = moment(first, 0)

    categories, default_limits, month_limits = [], [], []
    for name, icon, limit in CATEGORIES[:profile.categories]:
        category_id = ids.next()
        categories.append({
            "id": category_id, "name": name, "icon": icon, "archived": False,
            "createdAt": created, "updatedAt": created,
        })
        default_limits.append({
            "categoryId": category_id, "from": first_month, "kind": "finite",
            "amount": limit, "currency": "COP",
        })
        # Every fourth category had one month with a different limit.
        if len(categories) % 4 == 0:
            month = month_start(today, rng.randrange(profile.past_months + 1))
            month_limits.append({
                "categoryId": category_id, "month": f"{month:%Y-%m}", "kind": "finite",
                "amount": limit + 100_000, "currency": "COP",
            })

    expenses = []
    for sequence, day in enumerate(spread(profile.expenses, first, today), start=1):
        category = rng.randrange(len(categories))
        ceiling = CATEGORIES[category][2] // 20
        at = moment(day, sequence % 3600)
        expenses.append({
            "id": ids.next(), "categoryId": categories[category]["id"],
            "date": f"{day:%Y-%m-%d}", "sequence": sequence,
            "amount": rng.randrange(1, max(2, ceiling // 100)) * 100,
            "currency": "COP", "note": rng.choice(NOTES) if rng.random() < 0.5 else None,
            "createdAt": at, "updatedAt": at,
        })

    incomes = []
    for sequence, day in enumerate(spread(profile.incomes, first, today), start=1):
        source = rng.choice(INCOME_SOURCES)
        at = moment(day, sequence % 3600)
        incomes.append({
            "id": ids.next(), "date": f"{day:%Y-%m-%d}", "sequence": sequence,
            "amount": rng.randrange(5, 120) * 1_000, "currency": "COP",
            "source": source,
            "sourceName": rng.choice(OTHER_INCOME_NAMES) if source == "other" else None,
            "note": None, "createdAt": at, "updatedAt": at,
        })

    return {
        "format": "sello-backup",
        "version": 1,
        "exportedAt": moment(today, 3600),
        "financial": {
            "sourceRevision": profile.expenses + profile.incomes + profile.categories,
            "zone": ZONE,
            "categories": categories,
            "defaultLimits": default_limits,
            "monthLimits": month_limits,
            "expenses": expenses,
            "incomes": incomes,
        },
        "preferences": {
            "theme": "light", "ink": "cobalto",
            "categoryOrder": "most_used", "categoryDirection": "descending",
        },
    }


def encode(backup):
    return json.dumps(backup, ensure_ascii=False, separators=(",", ":")).encode("utf-8")


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("profile", choices=sorted(PROFILES))
    parser.add_argument("output", type=Path)
    parser.add_argument("--today", type=date.fromisoformat, default=date.today(),
                        help="last day with records, yyyy-mm-dd (default: today)")
    args = parser.parse_args(argv)
    data = encode(build(PROFILES[args.profile], args.today))
    if len(data) > MAX_BYTES:
        sys.exit(f"{len(data)} bytes is over the backup limit")
    args.output.write_bytes(data)
    print(f"{args.output}: {len(data)} bytes")


if __name__ == "__main__":
    main()
