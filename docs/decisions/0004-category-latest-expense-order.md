# 0004 — Category recency follows expense additions

**Owner:** project owner (user) · **Updated:** 2026-10-16
**Status:** approved product rule; supported by the current sorting code.

Último gasto agregado uses each category's newest original creation time among
nondeleted expenses in the selected month. Editing an expense or changing a
category or budget does not make it newer. Do not use a mutable update timestamp.

Backdated expenses belong to their effective month but keep their actual creation
time. Restore preserves that time. Deleting the newest expense falls back to the
next surviving addition. Financial-clock changes do not count as additions.

Reversing direction compares each category's newest expense; it never switches to
its oldest expense. Categories without eligible expenses stay last within their
finite/unlimited group in either direction. Unlimited categories stay last overall.
Ties use normalized name ascending, then stable category ID. The full ordering
contract is [D06](0001-mvp-contract.md#d06--ordering).

## Verification

Use controlled timestamps to cover additions, edits, backdating, deletion, restore,
empty categories, both directions and stable ties. O01–O14 in the
[financial examples](../testing/mvp-financial-examples.md) define expected cases.
